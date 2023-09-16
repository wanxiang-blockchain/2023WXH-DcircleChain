package com.base.baseui.widget.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.jokar.multilanguages.library.MultiLanguage;

import java.util.Locale;

public class SPUtil {

    private final String SP_NAME = "language_setting";
    private final String TAG_LANGUAGE = "language_select";
    private static volatile SPUtil instance;

    private final SharedPreferences mSharedPreferences;

    private Locale systemCurrentLocal = null;
    private Context context = null;


    public SPUtil(Context context) {
        this.context = context;
        mSharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }


    public int getSelectLanguage() {
        return mSharedPreferences.getInt(TAG_LANGUAGE, 0);
    }



    public int getBadgeMarginLeft(){
        return mSharedPreferences.getInt("badgeMarginLeft",0);
    }

    public void setBadgeMarginLeft(int badgeMargin){
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt("badgeMarginLeft", badgeMargin);
        edit.apply();
    }

    public int getBadgeMarginTop(){
        return mSharedPreferences.getInt("badgeMarginTop",0);
    }

    public void setBadgeMarginTop(int badgeMargin){
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt("badgeMarginTop", badgeMargin);
        edit.apply();
    }

    public Locale getSystemCurrentLocal() {
        if (systemCurrentLocal == null){
            systemCurrentLocal = MultiLanguage.getSystemLocal(context);
        }
        return systemCurrentLocal;
    }

    public void setSystemCurrentLocal(Locale local) {
        systemCurrentLocal = local;
    }

    public static SPUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (SPUtil.class) {
                if (instance == null) {
                    instance = new SPUtil(context);
                }
            }
        }
        return instance;
    }
}