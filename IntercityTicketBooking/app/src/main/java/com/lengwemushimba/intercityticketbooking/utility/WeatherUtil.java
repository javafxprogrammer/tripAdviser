package com.lengwemushimba.intercityticketbooking.utility;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lengwe on 7/5/18.
 */

public class WeatherUtil {


    public static final String TAG = WeatherUtil.class.getSimpleName();
    public static final String BASE_URL = "https://api.openweathermap.org/data/2.5/forecast?q=";
    public static final String ICON_URL = "http://openweathermap.org/img/w/";
    public static final String API_KEY = "ENTER YOUR OPENWEATHERMAP API KEY HERE"; // edit_this

    public static JSONObject getObject(String tagName, JSONObject jsonObject) throws JSONException {
        return jsonObject.getJSONObject(tagName);
    }

    public static JSONArray getArray(String tagName, JSONObject jsonObject) throws JSONException {
        return jsonObject.getJSONArray(tagName);
    }

    public static String getString(String tagName, JSONObject jsonObject) throws JSONException {
        return jsonObject.getString(tagName);
    }

    public static float getFloat(String tagName, JSONObject jsonObject) throws JSONException {
        return (float) jsonObject.getDouble(tagName);
    }

    public static double getDouble(String tagName, JSONObject jsonObject) throws JSONException {
        return jsonObject.getDouble(tagName);
    }

    public static int getInt(String tagName, JSONObject jsonObject) throws JSONException {
        return jsonObject.getInt(tagName);
    }

//        public static DateTime getDateTime(String dateTime){
//            Log.d(TAG+" dateTime", dateTime);
//            DateTime dateTimeObj = null;
//            dateTimeObj = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(dateTime);
//            return dateTimeObj;
//        }
}
