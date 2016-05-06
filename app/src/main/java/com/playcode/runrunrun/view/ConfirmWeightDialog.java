package com.playcode.runrunrun.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.playcode.runrunrun.R;
import com.playcode.runrunrun.activity.RunningActivity;
import com.playcode.runrunrun.utils.APIUtils;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by anpoz on 2016/4/16.
 */
public class ConfirmWeightDialog extends Dialog {

    private Context mContext;
    private EditText mEditText;

    public ConfirmWeightDialog(Context context) {
        super(context, R.style.AppTheme_Dialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm_weight);
        initView();
        setupDialog();
    }

    private void setupDialog() {
        setTitle("请确认体重");

        Window dialogWindow = getWindow();

        WindowManager m = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        Point point = new Point();
        d.getSize(point);
//        lp.height = (int) (point.y * 0.6);
        lp.width = (int) (point.x * 0.85);
        dialogWindow.setAttributes(lp);
    }

    private void initView() {
        TextInputLayout textInputLayout = (TextInputLayout) findViewById(R.id.til_weight);
        mEditText = textInputLayout.getEditText();
        Button button = (Button) findViewById(R.id.btn_weight_confirm);

        textInputLayout.setHint("体重(kg)");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://codeczx.duapp.com/FitServer/")
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd HH:mm:ss.S")
                                .create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        SharedPreferences preferences = mContext.getSharedPreferences("UserData", 0);
        String token = preferences.getString("token", "");

        button.setOnClickListener(v -> {

            String str = mEditText.getText().toString();

            if (TextUtils.isEmpty(str)) {
                Toast.makeText(mContext, "体重不能为空！", Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }

            float weight = Float.valueOf(str);
            if (weight > 20 && weight < 200) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putFloat("weight", weight);
                editor.apply();

                retrofit.create(APIUtils.class)
                        .setWeigth(token, weight)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(commonResult -> {
                            if (commonResult.getResultCode() == 0) {

                                dismiss();
                                Intent intent = new Intent(mContext, RunningActivity.class);
                                mContext.startActivity(intent);
                            } else {
                                Toast.makeText(mContext, "请输入20~200之间的数", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        });

            } else {
                Toast.makeText(mContext, "请输入20~200之间的数", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }
}
