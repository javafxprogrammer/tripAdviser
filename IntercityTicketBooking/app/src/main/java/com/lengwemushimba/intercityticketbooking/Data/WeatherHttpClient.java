package com.lengwemushimba.intercityticketbooking.Data;

import android.util.Log;

import com.lengwemushimba.intercityticketbooking.utility.WeatherUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by lengwe on 7/5/18.
 */

public class WeatherHttpClient {

    public static final String TAG = WeatherHttpClient.class.getSimpleName();

    public String getWeatherData(String place)  {
        try {
        URL url = new URL(WeatherUtil.BASE_URL+place+WeatherUtil.API_KEY);
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.connect();

        StringBuffer stringBuffer = new StringBuffer();
        inputStream = connection.getInputStream();
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(inputStream));

        String line = null;

        while ((line = bufferedReader.readLine()) != null){
            stringBuffer.append(line+"\r\n");
        }

        bufferedReader.close();
        connection.disconnect();
        return stringBuffer.toString();

        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}


//            Log.d(TAG+" weather_url", WeatherUtil.BASE_URL+place+WeatherUtil.API_KEY);
