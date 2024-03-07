/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.flexispot.ble.gui.threadMill;

import android.content.Context;
import android.content.SharedPreferences;

import com.flexispot.ble.LuMan;


public final class PreferencesUtility {

    private static final String file_name = "kotlin_mvp_file";
    private static PreferencesUtility sInstance;
    private static SharedPreferences mPreferences;

    public PreferencesUtility(final Context context) {
        mPreferences = LuMan.Companion.getContext().getSharedPreferences(file_name, Context.MODE_PRIVATE);
    }

    public static final PreferencesUtility getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesUtility(context.getApplicationContext());
        }
        return sInstance;
    }


    public String getToken() {
        return mPreferences.getString("token", "");
    }

    public Boolean getUserBindScreen() {
        return mPreferences.getBoolean("user_bind_screen", false);
    }



    //设置m1速度
    public void setM1(final int speed) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("m1", speed);
        editor.commit();
    }

    public int getM1() {
        return mPreferences.getInt("m1", -1);
    }

    //设置m2速度
    public void setM2(final int speed) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("m2", speed);
        editor.commit();
    }

    public int getM2() {
        return mPreferences.getInt("m2", -1);
    }

    //设置m3速度
    public void setM3(final int speed) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("m3", speed);
        editor.commit();
    }

    public int getM3() {
        return mPreferences.getInt("m3", -1);
    }

    //设置待机时间
    public void setStandby(final int standby) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("standby", standby);
        editor.commit();
    }

    public int getStandby() {
        return mPreferences.getInt("standby", 0);
    }


    //判断新手模式是否成功1是0不是
    public void setNovice(final int novice) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("novice", novice);
        editor.commit();
    }

    public int getNovice() {
        return mPreferences.getInt("novice", 0);
    }



    //判断公英制0公制1英制
    public void setSystem(final int system) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("system", system);
        editor.commit();
    }

    public int getSystem() {
        return mPreferences.getInt("system", 0);
    }


    //版本
    public void setVersion(final int system,String s) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("s", system);
        editor.commit();
    }

    public int getVersion(String s) {
        return mPreferences.getInt("s", 0);
    }

    //判断是否同意协议 0不同意
    public void setUser(final int system) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("accept_user_agree", system);
        editor.commit();
    }

    public int getUser() {
        return mPreferences.getInt("accept_user_agree", 0);
    }
}