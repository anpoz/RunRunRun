package com.playcode.runrunrun.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;

import com.playcode.runrunrun.R;

/**
 * Created by anpoz on 2016/3/27.
 */
public class PasswordEditText extends AppCompatEditText {
    private int mShowPwdIcon = R.drawable.ic_visibility_24dp;
    private int mHidePwdIcon = R.drawable.ic_visibility_off_24dp;
    private boolean mIsIconShow;
    private Drawable mDrawableSide;

    public PasswordEditText(Context context) {
        this(context, null);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFields(attrs, 0);
    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFields(attrs, defStyleAttr);
    }

    private void initFields(AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {//从xml文件获取属性
            TypedArray styles = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.PasswordEditText, defStyleAttr, 0);
            try {
                mShowPwdIcon = styles.getResourceId(R.styleable.PasswordEditText_pet_iconShow, mShowPwdIcon);
                mHidePwdIcon = styles.getResourceId(R.styleable.PasswordEditText_pet_iconHide, mHidePwdIcon);
            } finally {
                styles.recycle();
            }
        }
        setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    showPasswordVisibilityIndicator(true);
                } else {
                    mIsIconShow = false;
                    restorePasswordIconVisibility(false);
                    showPasswordVisibilityIndicator(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * 存储状态
     *
     * @return Parcelable
     */
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable state = super.onSaveInstanceState();
        return new PwdSavedState(state, mIsIconShow);
    }

    /**
     * 恢复状态
     *
     * @param state Parcelable
     */
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        PwdSavedState pwdSavedState = (PwdSavedState) state;
        super.onRestoreInstanceState(pwdSavedState.getSuperState());
        mIsIconShow = pwdSavedState.isShowingIcon();
        restorePasswordIconVisibility(mIsIconShow);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDrawableSide == null) {
            return super.onTouchEvent(event);
        }
        final Rect bounds = mDrawableSide.getBounds();
        final int x = (int) event.getRawX();
        int iconX = (int) getTopRightCorner().x;

        //icon位置
        int iconLeft = iconX - bounds.width();

        if (x >= iconLeft) {
            togglePasswordIconVisibility();
            event.setAction(MotionEvent.ACTION_CANCEL);
            return false;
        }
        return super.onTouchEvent(event);
    }

    private void togglePasswordIconVisibility() {
        mIsIconShow = !mIsIconShow;
        restorePasswordIconVisibility(mIsIconShow);
        showPasswordVisibilityIndicator(true);
    }

    /**
     * 获取上右角的距离
     * @return PointF
     */
    private PointF getTopRightCorner() {
        float src[] = new float[8];
        float[] dst = new float[]{0, 0, getWidth(), 0, 0, getHeight(), getWidth(), getHeight()};
        getMatrix().mapPoints(src, dst);
        return new PointF(getX() + src[2], getY() + src[3]);
    }

    private void restorePasswordIconVisibility(boolean isPwdShow) {
        if (isPwdShow) {//密码处于可视状态
            setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {//密码处于不可见状态
            setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        }
        setSelection(getText().length());
    }

    private void showPasswordVisibilityIndicator(boolean showIcon) {
        if (showIcon) {
            Drawable drawable = mIsIconShow ?
                    ContextCompat.getDrawable(getContext(), R.drawable.ic_visibility_24dp) :
                    ContextCompat.getDrawable(getContext(), R.drawable.ic_visibility_off_24dp);
            //在view右侧显示drawable
            setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            mDrawableSide = drawable;
        } else {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            mDrawableSide = null;
        }
    }

    protected static class PwdSavedState extends BaseSavedState {
        private final boolean mShowingIcon;

        private PwdSavedState(Parcelable state, boolean isIconShow) {
            super(state);
            mShowingIcon = isIconShow;
        }

        private PwdSavedState(Parcel in) {
            super(in);
            mShowingIcon = in.readByte() != 0;
        }

        public boolean isShowingIcon() {
            return mShowingIcon;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (mShowingIcon ? 1 : 0));
        }

        public static final Parcelable.Creator<PwdSavedState> CREATOR = new Creator<PwdSavedState>() {
            @Override
            public PwdSavedState createFromParcel(Parcel source) {
                return new PwdSavedState(source);
            }

            @Override
            public PwdSavedState[] newArray(int size) {
                return new PwdSavedState[size];
            }
        };
    }
}
