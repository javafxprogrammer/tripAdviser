package com.lengwemushimba.intercityticketbooking;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by lengwe on 5/18/18.
 */

public class SharedPreferenceManager {

    private static SharedPreferenceManager sharedPreferenceManager;
    private static Context context;
    private static final String SHARED_PREF_NAME = "userDetails";
    private static final String KEY_USER_ID = "userID";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_COMPANY_ID = "userCompanyID";
    private static final String KEY_USER_PICTURE = "userPicture";

    private SharedPreferenceManager(Context context){
        SharedPreferenceManager.context = context;
    }

    public static synchronized SharedPreferenceManager getInstance(Context context){
        if (sharedPreferenceManager == null){
            sharedPreferenceManager = new SharedPreferenceManager(context);
        }
        return sharedPreferenceManager;
    }

    public boolean userLogIn(int userID, String userType, String userName, String userEmail,
                             String userPhone, String userCompanyID, String userPicture){

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, userID);
        editor.putString(KEY_USER_TYPE, userType);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putString(KEY_USER_PHONE, userPhone);
        editor.putString(KEY_USER_COMPANY_ID, userCompanyID);
        editor.putString(KEY_USER_PICTURE, userPicture);
        editor.apply();
        return true;

    }

    // TODO: 6/10/18 select from db first coz they may be deleted, yet still present in sharedpreference 
    public boolean isLoggedIn(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.getString(KEY_USER_NAME, null) != null){
            return true;
        }
        return false;
    }

    public boolean logOut(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        return true;
    }

    public int getUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_USER_ID, 0);
    }

    public String getUserType() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_TYPE, null);
    }

    public String getUserName() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    public String getUserEmail() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public String getUserPhone() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_PHONE, null);
    }

    public String getUserCompanyId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_COMPANY_ID, null);
    }

    public String getUserPicture() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_PICTURE, null);
    }

    private Bundle tmpBookingDetails;
    public void setBookingDetails(Bundle bookingDetails){
        tmpBookingDetails = bookingDetails;
    }
    public Bundle getBookingDetails(){
        return tmpBookingDetails;
    }

//    public boolean updateDetails(){
//        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putStringSet("")
//    }

    private static final String KEY_USER_NOTIFICATION = "userNotification";
    public boolean saveNotificationStatus(boolean status){

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_USER_NOTIFICATION, status);
        editor.apply();
        return true;
    }
    public boolean doesNotificationExist(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        boolean notif = sharedPreferences.getBoolean(KEY_USER_NOTIFICATION, false);
        if (notif){
            return true;
        }
        return false;
    }
}
