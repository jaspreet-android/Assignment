package com.demologin.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    private static class UtilsHelper {
        static final Utils ourInstance = new Utils();
    }

    public static Utils getUtils() {
        return UtilsHelper.ourInstance;
    }

    private Utils() {
    }

    public static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static void showKeyboard(Activity context, View clearFocusView) {
        View view = context.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            clearFocusView.clearFocus();
        }
    }

    public static void hideKeyboard(Activity context, View clearFocusView) {
        View view = context.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (null != clearFocusView)
                clearFocusView.clearFocus();
        }
    }

    public void generateHashkey(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo("com.demologin",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(),
                        Base64.DEFAULT);
                Log.d("FacebookIn", "keyHash: " + keyHash);
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {
        }
    }
}
