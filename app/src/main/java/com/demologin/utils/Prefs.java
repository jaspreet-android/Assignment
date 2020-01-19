package com.demologin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {

    private static class PrefsHelper {
        private static final Prefs ourInstance = new Prefs();
    }

    public static Prefs getPrefs(Context context) {
        prefs = context.getSharedPreferences("demo_login_prefs", Context.MODE_PRIVATE);
        return PrefsHelper.ourInstance;
    }

    private Prefs() {
    }

    private static SharedPreferences prefs = null;

    public void setIsRegisterDone(boolean isDone) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREF_IS_REGISTER_DONE, isDone);
        editor.apply();
    }

    public boolean isRegisterDone() {
        return prefs.getBoolean(Constants.PREF_IS_REGISTER_DONE, false);
    }

    public void setIsSocial(boolean isSocial) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREF_IS_SOCIAL, isSocial);
        editor.apply();
    }

    public boolean isSocial() {
        return prefs.getBoolean(Constants.PREF_IS_SOCIAL, false);
    }

    public void setIsVerified(boolean isVerified) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREF_IS_VERIFIED, isVerified);
        editor.apply();
    }

    public boolean isVerified() {
        return prefs.getBoolean(Constants.PREF_IS_VERIFIED, false);
    }

    public void setEmail(String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREF_EMAIL, email);
        editor.apply();
    }

    public String getEmail() {
        return prefs.getString(Constants.PREF_EMAIL, null);
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }


    public void setFbId(String fbId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREF_FB_ID, fbId);
        editor.apply();
    }

    public String getFbId() {
        return prefs.getString(Constants.PREF_FB_ID, null);
    }

    public void setFbToken(String fbToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREF_FB_TOKEN, fbToken);
        editor.apply();
    }

    public String getFbToken() {
        return prefs.getString(Constants.PREF_FB_TOKEN, null);
    }

    public void setFbProfile(String fbProfile) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREF_FB_PROFILE, fbProfile);
        editor.apply();
    }

    public String getFbProfile() {
        return prefs.getString(Constants.PREF_FB_PROFILE, null);
    }

    public void setFacebookLoggedIn(boolean isSet) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREF_FB_LOGGEDIN, isSet);
        editor.apply();
    }

    public boolean isFacebookLoggedIn() {
        return prefs.getBoolean(Constants.PREF_FB_LOGGEDIN, false);
    }

    public void setGoogleLoggedIn(boolean isSet) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREF_GOOGLE_LOGGEDIN, isSet);
        editor.apply();
    }

    public boolean isGoogleLoggedIn() {
        return prefs.getBoolean(Constants.PREF_GOOGLE_LOGGEDIN, false);
    }

    public static SharedPreferences.Editor getSharedPrefrence(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        return editor;
    }

    public void setUserPassword(String password) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREF_PASSWORD, password);
        editor.apply();
    }

    public String getUserPassword() {
        return prefs.getString(Constants.PREF_PASSWORD, null);
    }

}
