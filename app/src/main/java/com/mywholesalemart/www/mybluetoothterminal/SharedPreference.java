package com.mywholesalemart.www.mybluetoothterminal;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {

    public SharedPreference(){

    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE);
    }

    public static void storeFaultCode(Context context, String ecuData) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("FAULTCODE",ecuData);
        editor.apply();
    }

    public static String getFaultCode(Context context) {
        return getSharedPreferences(context).getString("FAULTCODE", null);
    }

    public static void storeEcuData(Context context, String ecuData) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("ECUDATA",ecuData);
        editor.apply();
    }

    public static String getEcuData(Context context) {
        return getSharedPreferences(context).getString("ECUDATA", null);
    }


}
