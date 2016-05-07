package com.playcode.runrunrun.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.playcode.runrunrun.R;
import com.playcode.runrunrun.fragment.DataAnalysisFragment;
import com.playcode.runrunrun.fragment.MainFragment;
import com.playcode.runrunrun.fragment.RunCircleFragment;
import com.playcode.runrunrun.fragment.RunRecordFragment;
import com.playcode.runrunrun.utils.AccessUtils;
import com.playcode.runrunrun.utils.BOSUtils;
import com.playcode.runrunrun.utils.CircleTransformation;
import com.playcode.runrunrun.utils.ToastUtils;
import com.playcode.runrunrun.view.ConfirmWeightDialog;
import com.playcode.runrunrun.view.LoginDialog;
import com.squareup.picasso.Picasso;

import java.io.File;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final int PROFILE_CHANGED = 1;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ImageView mUserProfilePhoto;
    private int avatarSize;
    private TextView mUsername;

    private Toolbar mToolbar;

    private FragmentManager mFragmentManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(R.id.flContenter, new MainFragment())
                .commit();

        initView();
        setupToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupHeader();
    }

    private void initView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.vNavigation);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        avatarSize = getResources().getDimensionPixelSize(R.dimen.global_menu_avatar_size);
        View headerView = mNavigationView.getHeaderView(0);
        ImageButton logoutButton = (ImageButton) headerView.findViewById(R.id.btn_logout);
        mUserProfilePhoto = (ImageView) headerView.findViewById(R.id.ivUserProfilePhoto);
        mUsername = (TextView) headerView.findViewById(R.id.tvUsername);

        FloatingActionButton mfab = (FloatingActionButton) findViewById(R.id.fab);

        if (mfab != null) {
            mfab.setOnClickListener(v1 -> {
                if (!AccessUtils.isNetworkConnected(this)) {
//                    Toast.makeText(this, "网络未连接~", Toast.LENGTH_SHORT).show();
                    ToastUtils.showToast(this, "网络未连接~");
                    return;
                }
                SharedPreferences setting = getSharedPreferences("UserData", 0);
                String token = setting.getString("token", "");
                float weight = setting.getFloat("weight", 0);
                if (TextUtils.isEmpty(token)) {
                    Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                } else if (weight == 0) {
                    new ConfirmWeightDialog(this).show();
                } else {
                    Intent intent = new Intent(this, RunningActivity.class);
                    startActivity(intent);
                }
            });
        }

        mUserProfilePhoto.setOnClickListener(v -> {
            mDrawerLayout.closeDrawer(Gravity.LEFT);

            SharedPreferences setting = getSharedPreferences("UserData", 0);
            String token = setting.getString("token", "");
            if (TextUtils.isEmpty(token)) {
                if (!AccessUtils.isNetworkConnected(this)) {
//                    Toast.makeText(this, "网络未连接~", Toast.LENGTH_SHORT).show();
                    ToastUtils.showToast(this, "网络未连接~");
                    return;
                }
                startLogin();
            } else {
                startActivityForResult(new Intent(this, UserProfileActivity.class), PROFILE_CHANGED);
                overridePendingTransition(0, 0);
            }
        });

        logoutButton.setOnClickListener(v -> {
            SharedPreferences setting = getSharedPreferences("UserData", 0);
            SharedPreferences.Editor editor = setting.edit();
            editor.clear();
            editor.commit();
            recreate();
        });

        MenuItem homeItem = mNavigationView.getMenu().findItem(R.id.home);
        if (homeItem != null) {
            homeItem.setChecked(true);
        }

        mNavigationView.setNavigationItemSelectedListener(item -> {
            item.setChecked(true);
            switch (item.getItemId()) {
                case R.id.home:
                    mFragmentManager
                            .beginTransaction()
                            .replace(R.id.flContenter, new MainFragment())
                            .commit();
                    mToolbar.setTitle(R.string.app_name);
                    break;
                case R.id.start_run:
                    Fragment runningFragment = new RunRecordFragment();
                    mFragmentManager
                            .beginTransaction()
                            .replace(R.id.flContenter, runningFragment)
                            .commit();
                    mToolbar.setTitle("用户记录");
                    break;
                case R.id.data_statistics:
                    Fragment dataAnalysisFragment = new DataAnalysisFragment();
                    mFragmentManager
                            .beginTransaction()
                            .replace(R.id.flContenter, dataAnalysisFragment)
                            .commit();
                    mToolbar.setTitle("数据统计");
                    break;
                case R.id.run_circle:
                    Fragment runCircleFragment = new RunCircleFragment();
                    mFragmentManager
                            .beginTransaction()
                            .replace(R.id.flContenter, runCircleFragment)
                            .commit();
                    mToolbar.setTitle("跑步圈");
                    break;
                case R.id.night_mode_switch:
                    int curNightNightMode = getResources().getConfiguration().uiMode &
                            Configuration.UI_MODE_NIGHT_MASK;
                    switch (curNightNightMode) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            break;
                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            break;
                    }
                    MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.home);
                    if (menuItem != null) {
                        menuItem.setChecked(true);
                    }
                    recreate();
                    break;
                case R.id.abouts:
                    startActivity(new Intent(this, AboutActivity.class));
                    overridePendingTransition(0, 0);
                    break;
                default:

            }
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }


    private void setupToolbar() {
        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                mToolbar,
                R.string.open,
                R.string.close);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void setupHeader() {
        //设置头像
        SharedPreferences setting = getSharedPreferences("UserData", 0);
        String username = setting.getString("username", getString(R.string.login_please));
        String photoKey = setting.getString("photoUrl", "");

        mUsername.setText(username);

        setupPhoto(photoKey);
    }

    /**
     * 设置头像
     *
     * @param photoKey 头像文件的唯一key
     */
    private void setupPhoto(String photoKey) {
        if (!AccessUtils.isNetworkConnected(this)) {
            return;
        }
        File file = new File(String.format("%s%s%s.tmp", getExternalCacheDir().getPath(), File.separator, photoKey));
        BOSUtils.getInstance()
                .getFileWithKey(file, photoKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<File>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Picasso.with(MainActivity.this)
                                .load(R.mipmap.ic_launcher)
                                .placeholder(R.drawable.img_circle_placeholder)
                                .resize(avatarSize, avatarSize)
                                .centerCrop()
                                .transform(new CircleTransformation())
                                .into(mUserProfilePhoto);
                    }

                    @Override
                    public void onNext(File file1) {
                        Picasso.with(MainActivity.this)
                                .load(file1)
                                .placeholder(R.drawable.img_circle_placeholder)
                                .error(R.mipmap.ic_launcher)
                                .resize(avatarSize, avatarSize)
                                .centerCrop()
                                .transform(new CircleTransformation())
                                .into(mUserProfilePhoto);
                    }
                });

    }

    /**
     * 启动登录
     */
    private void startLogin() {
        LoginDialog dialog = new LoginDialog(this);
        dialog.setOnLoginListener(this::recreate);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROFILE_CHANGED && resultCode == RESULT_OK) {
            setupHeader();
        }
    }

}

