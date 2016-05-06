package com.playcode.runrunrun.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by anpoz on 2016/5/3.
 */
public class ToastUtils {
    private static Toast mToast;

    public static void showToast(Context context, String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
//            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

}
