package com.playcode.runrunrun.view;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.playcode.runrunrun.App;
import com.playcode.runrunrun.R;
import com.playcode.runrunrun.model.LoginModel;
import com.playcode.runrunrun.model.UserModel;
import com.playcode.runrunrun.utils.APIUtils;
import com.playcode.runrunrun.utils.RetrofitHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by anpoz on 2016/3/23.
 */
public class LoginDialog extends Dialog {
    private Context mContext;

    private TextInputLayout mEmailInputLayout;
    private TextInputLayout mPasswordInputLayout;

    private EditText mEmailEditText;
    private EditText mPasswordEditText;

    private Button mButtonOk;

    private ProgressBar mProgressBar;

    private OnLoginListener mOnLoginListener;

    public interface OnLoginListener {
        void success();
    }

    public void setOnLoginListener(OnLoginListener onLoginListener) {
        mOnLoginListener = onLoginListener;
    }

    public LoginDialog(Context context) {
        super(context, R.style.AppTheme_Dialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_login);

        initView();
        setupDialog();
    }

    private void setupDialog() {
        setTitle(R.string.login);

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
        mEmailInputLayout = (TextInputLayout) findViewById(R.id.til_login_email);
        mPasswordInputLayout = (TextInputLayout) findViewById(R.id.til_login_password);

        mEmailEditText = mEmailInputLayout.getEditText();
        mPasswordEditText = mPasswordInputLayout.getEditText();

        mButtonOk = (Button) findViewById(R.id.btn_go_login);
        Button buttonSwitch = (Button) findViewById(R.id.btn_switch_register);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_go_login);

        mEmailInputLayout.setHint(mContext.getString(R.string.e_mail));
        mPasswordInputLayout.setHint(mContext.getString(R.string.password));
        mEmailInputLayout.setErrorEnabled(true);
        mPasswordInputLayout.setErrorEnabled(true);

        buttonSwitch.setOnClickListener(v -> {
            dismiss();
            RegisterDialog dialog = new RegisterDialog(mContext);
            dialog.setOnRegSuccessListener(new RegisterDialog.OnRegSuccessListener() {
                @Override
                public void success() {
                    show();
                }

                @Override
                public void cancel() {
                    show();
                }
            });
            dialog.show();
        });

        mButtonOk.setOnClickListener(v -> {
            mButtonOk.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);

            //隐藏键盘
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

            String email = mEmailEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();

            if (!checkEmailType(email)) {
                mEmailInputLayout.setError(mContext.getString(R.string.email_format_error));
                mButtonOk.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                return;
            } else if (!checkPasswordType(password)) {
                mPasswordInputLayout.setError(mContext.getString(R.string.password_format_error));
                mButtonOk.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                return;
            } else {
                mPasswordInputLayout.setErrorEnabled(false);
                mEmailInputLayout.setErrorEnabled(false);
            }

            doLogin(email, password);
        });
    }

    private void doLogin(final String email, String password) {
        RetrofitHelper.getInstance()
                .getService(APIUtils.class)
                .userLogin(password, email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LoginModel>() {
                    @Override
                    public void onCompleted() {
                        mButtonOk.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mButtonOk.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                        mEmailInputLayout.setError(mContext.getString(R.string.login_failed_network_error));
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(LoginModel loginModel) {
                        Log.d(getClass().toString(), loginModel.getMessage());
                        switch (loginModel.getResultCode()) {
                            case 0://success
                                dismiss();
                                Toast.makeText(mContext, R.string.login_success, Toast.LENGTH_SHORT).show();
                                getUser(loginModel.getToken());
                                break;
                            case 1:
                                break;
                            default:
                        }
                    }
                });
    }

    private void getUser(String token) {
        RetrofitHelper.getInstance()
                .getService(APIUtils.class)
                .getUser(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserModel>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(UserModel userModel) {
                        if (userModel.getResultCode() == 0) {
                            SharedPreferences preferences = mContext.getSharedPreferences("UserData", 0);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("username", userModel.getUser().getName());
                            editor.putString("photoUrl", userModel.getUser().getPhoto());
                            editor.putString("email", userModel.getUser().getEmail());
                            editor.putString("token", userModel.getUser().getToken());
                            editor.putFloat("weight", userModel.getUser().getWeight());
                            editor.commit();
                            if (mOnLoginListener != null) {
                                mOnLoginListener.success();
                            }
                            App.setServerMode(App.SERVER_MODE.WITH_SERVER);
                        }
                    }
                });
    }

    private boolean checkPasswordType(String s) {
        //以字母开头，长度在6~20之间，只能包含字符、数字和下划线
        String pattern = "^[a-zA-Z]\\w{6,20}$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(s);
        return m.find();
    }

    private boolean checkEmailType(String s) {
        String pattern = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(s);
        return m.find();
    }
}
