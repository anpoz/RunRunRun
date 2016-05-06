package com.playcode.runrunrun.view;

import android.app.Dialog;
import android.content.Context;
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

import com.google.gson.GsonBuilder;
import com.playcode.runrunrun.R;
import com.playcode.runrunrun.model.MessageModel;
import com.playcode.runrunrun.utils.APIUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by anpoz on 2016/3/21.
 */
public class RegisterDialog extends Dialog {

    private TextInputLayout mUsernameInputLayout;
    private TextInputLayout mPasswordInputLayout;
    private TextInputLayout mEmailInputLayout;

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private EditText mEmailEditText;

    private Button mButtonOk;

    private ProgressBar mProgressBar;

    private Context mContext;

    private OnRegSuccessListener mOnRegSuccessListener;

    public void setOnRegSuccessListener(OnRegSuccessListener onRegSuccessListener) {
        mOnRegSuccessListener = onRegSuccessListener;
    }

    public interface OnRegSuccessListener{
        void success();
        void cancel();
    }

    public RegisterDialog(Context context) {
        super(context, R.style.AppTheme_Dialog);
        mContext = context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_register);
        initView();
        setupDialog();
    }

    private void setupDialog() {
        setTitle("注册");

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
        mUsernameInputLayout = (TextInputLayout) findViewById(R.id.til_reg_username);
        mPasswordInputLayout = (TextInputLayout) findViewById(R.id.til_reg_password);
        mEmailInputLayout = (TextInputLayout) findViewById(R.id.til_reg_email);

        mUsernameEditText = mUsernameInputLayout.getEditText();
        mPasswordEditText = mPasswordInputLayout.getEditText();
        mEmailEditText = mEmailInputLayout.getEditText();

        Button buttonCancel = (Button) findViewById(R.id.btn_cancel_register);
        mButtonOk = (Button) findViewById(R.id.btn_go_register);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_go_register);

        mUsernameInputLayout.setHint("昵称");
        mPasswordInputLayout.setHint("密码");
        mEmailInputLayout.setHint("E-mail");

        mUsernameInputLayout.setErrorEnabled(true);
        mPasswordInputLayout.setErrorEnabled(true);
        mEmailInputLayout.setErrorEnabled(true);

        buttonCancel.setOnClickListener((v) -> {
            dismiss();
            if (mOnRegSuccessListener!=null){
                mOnRegSuccessListener.cancel();
            }
        });

        mButtonOk.setOnClickListener(v -> {
            mButtonOk.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            //隐藏键盘
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

            String username = mUsernameEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
            String email = mEmailEditText.getText().toString();

            if (!checkUsernameType(username)) {
                mUsernameInputLayout.setError("昵称格式不合法");
                mButtonOk.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                return;
            } else if (!checkPasswordType(password)) {
                mPasswordInputLayout.setError("密码格式不合法");
                mButtonOk.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                return;
            } else if (!checkEmailType(email)) {
                mEmailInputLayout.setError("E-mail格式不合法");
                mButtonOk.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                return;
            } else {
                mUsernameInputLayout.setErrorEnabled(false);
                mPasswordInputLayout.setErrorEnabled(false);
                mEmailInputLayout.setErrorEnabled(false);
            }

            regNewUser(username, password, email);
        });
    }

    private void regNewUser(String username, String password, String email) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://codeczx.duapp.com/FitServer/")
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd HH:mm:ss.S")
                                .create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        retrofit.create(APIUtils.class)
                .userReg(username, password, email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MessageModel>() {
                    @Override
                    public void onCompleted() {
                        Log.d(getClass().toString(), "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        mButtonOk.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                        mEmailInputLayout.setError("注册失败~网络错误");
                    }

                    @Override
                    public void onNext(MessageModel messageModel) {
                        Log.d(getClass().toString(), messageModel.getMessage());
                        switch (messageModel.getResultCode()) {
                            case 0:
                                dismiss();
//                                Snackbar.make(getOwnerActivity().getCurrentFocus(), "注册成功！", Snackbar.LENGTH_SHORT).show();
                                Toast.makeText(mContext, "注册成功", Toast.LENGTH_SHORT).show();
                                if (mOnRegSuccessListener!=null){
                                    mOnRegSuccessListener.success();
                                }
                                break;
                            case 1:
                                mButtonOk.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.GONE);
                                mEmailInputLayout.setError("注册失败~E-mail已被占用");
                                break;
                            default:
                                mButtonOk.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.GONE);
                                mEmailInputLayout.setError("注册失败~网络错误");
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

    private boolean checkUsernameType(String s) {
        //最长不得超过7个汉字，或14个字节(数字，字母和下划线)
        String pattern = "^[\\u4e00-\\u9fa5]{1,7}$|^[\\dA-Za-z_]{1,14}$";

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
