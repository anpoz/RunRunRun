package com.playcode.runrunrun.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.playcode.runrunrun.R;
import com.playcode.runrunrun.model.MessageModel;
import com.playcode.runrunrun.utils.APIUtils;
import com.playcode.runrunrun.utils.AccessUtils;
import com.playcode.runrunrun.utils.BOSUtils;
import com.playcode.runrunrun.utils.CircleTransformation;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UserProfileActivity extends AppCompatActivity {

    private final static int RESULT_LOAD_IMAGE = 1;
    private final static int RESULT_CROP_IMAGE = 2;

    private Retrofit mRetrofit;

    private ImageView mImageView;
    private int avatarSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);

        initView();

        //初始化retrofit
        mRetrofit = new Retrofit.Builder()
                .baseUrl("http://codeczx.duapp.com/FitServer/")
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd HH:mm:ss.S")
                                .create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("用户资料");
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        TextView tvEmail = (TextView) findViewById(R.id.tvEmail);
        TextView tvName = (TextView) findViewById(R.id.tvName);
        TextView tvWeight = (TextView) findViewById(R.id.tvWeight);
        mImageView = (ImageView) findViewById(R.id.ivUserProfilePhoto);

        avatarSize = getResources().getDimensionPixelSize(R.dimen.big_avatar_size);

        if (mImageView != null) {
            mImageView.setOnClickListener(v -> {
                if (!AccessUtils.isNetworkConnected(this)) {
                    Toast.makeText(this, "网络未连接~", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            });
        }

        SharedPreferences setting = getSharedPreferences("UserData", 0);

        if (tvEmail != null) {
            tvEmail.setText(setting.getString("email", ""));
        }
        if (tvName != null) {
            tvName.setText(setting.getString("username", ""));
        }
        if (tvWeight != null) {
            tvWeight.setText(String.valueOf(setting.getFloat("weight", 0f)));
        }
        String photoKey = setting.getString("photoUrl", "");

        setupPhoto(photoKey);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case RESULT_LOAD_IMAGE:
                    startZoomPhoto(data.getData());
                    break;
                case RESULT_CROP_IMAGE:
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        uploadPhoto(extras);
                    }
                    break;
            }
        }
    }

    private void uploadPhoto(Bundle extras) {
        SharedPreferences setting = getSharedPreferences("UserData", 0);
        String photoName = UUID.randomUUID().toString();
        String token = setting.getString("token", "");
        String photoKey = setting.getString("photoUrl", "");
        if (token.equals("")) {
            Toast.makeText(this, "登录失效", Toast.LENGTH_SHORT).show();
            this.setResult(RESULT_CANCELED);
            this.finish();
            return;
        }
        Observable.just(extras)
                .subscribeOn(Schedulers.io())
                .flatMap(bundle -> {//将bitmap转为inputstream
                    Bitmap bitmap = extras.getParcelable("data");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    assert bitmap != null;
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
                    return Observable.just(inputStream);
                })
                .flatMap(inputStream -> BOSUtils.getInstance()
                        .uploadFile(inputStream, photoName))
                .map(s -> {
                    if (!photoKey.equals(""))
                        BOSUtils.getInstance()
                                .deleteFile(photoKey);
                    return s;
                })
                .flatMap(s -> mRetrofit.create(APIUtils.class).updatePhotoInfo(photoName, token))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MessageModel>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(MessageModel messageModel) {
                        if (messageModel.getResultCode() == 0) {
                            SharedPreferences.Editor editor = setting.edit();
                            editor.putString("photoUrl", photoName);
                            editor.commit();
                            setupPhoto(photoName);
                            Toast.makeText(UserProfileActivity.this, messageModel.getMessage(), Toast.LENGTH_SHORT).show();
                            UserProfileActivity.this.setResult(RESULT_OK);
                        }
                    }
                });
    }


    private void setupPhoto(String photoKey) {
        if (!AccessUtils.isNetworkConnected(this)) {
            return;
        }
        File file = new File(String.format("%s%s%s.tmp", getExternalCacheDir().getPath(), File.separator, photoKey));
        BOSUtils.getInstance()
                .getFileWithKey(file, photoKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(file1 -> {
                    Picasso.with(UserProfileActivity.this)
                            .load(file1)
                            .placeholder(R.drawable.img_circle_placeholder)
                            .error(R.mipmap.ic_launcher)
                            .resize(avatarSize, avatarSize)
                            .centerCrop()
                            .transform(new CircleTransformation())
                            .into(mImageView);
                });
    }

    private void startZoomPhoto(Uri uri) {//裁剪
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, RESULT_CROP_IMAGE);
    }
}
