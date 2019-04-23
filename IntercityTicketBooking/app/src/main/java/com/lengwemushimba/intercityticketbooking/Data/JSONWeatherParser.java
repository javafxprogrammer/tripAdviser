package com.lengwemushimba.intercityticketbooking.Data;

import android.util.Log;

import com.lengwemushimba.intercityticketbooking.model.CoordW;
import com.lengwemushimba.intercityticketbooking.model.MainW;
import com.lengwemushimba.intercityticketbooking.model.Weather;
import com.lengwemushimba.intercityticketbooking.model.WeatherW;
import com.lengwemushimba.intercityticketbooking.model.WindW;
import com.lengwemushimba.intercityticketbooking.utility.WeatherUtil;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by lengwe on 7/5/18.
 */

public class JSONWeatherParser {

    public static final String TAG = JSONWeatherParser.class.getSimpleName();

    public static Weather getWeather(String data, String date) {
        Weather weather = new Weather();

        try {
            Log.d(TAG+" jsw", data);
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("list");

            for (int i=0; i<jsonArray.length(); i++){

                JSONObject jsObj = jsonArray.getJSONObject(i);
                String dt_txt = jsObj.getString("dt_txt");

                DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                DateTimeFormatter fmt1 = DateTimeFormat.forPattern("yyyy-MM-dd");

                LocalDateTime localDateTime = fmt.parseLocalDateTime(dt_txt);

                LocalDate localDate = localDateTime.toLocalDate();
                LocalTime localTime = localDateTime.toLocalTime();

                LocalDate bookingDate = fmt1.parseLocalDate(date);


                if (bookingDate.isEqual(localDate) && localTime.isEqual(new LocalTime(12, 0, 0))){
                        Log.d(TAG+" timeEq", localDateTime.toString());


                        MainW mainW = new MainW();
                        JSONObject mainObj = WeatherUtil.getObject("main", jsObj);
                        mainW.setTemp(WeatherUtil.getString("temp", mainObj));
                        mainW.setPressure(WeatherUtil.getString("pressure", mainObj));
                        mainW.setHumidity(WeatherUtil.getString("humidity", mainObj));
                        weather.mainW = mainW;

                        WindW windW = new WindW();
                        JSONObject windObj = WeatherUtil.getObject("wind", jsObj);
                        windW.setSpeed(WeatherUtil.getString("speed", windObj));
                        weather.windW = windW;

                        // TODO: 7/5/18 json array
                        WeatherW weatherW = new WeatherW();
                        JSONArray weatherArr = WeatherUtil.getArray("weather", jsObj);
                        JSONObject weatherObj = weatherArr.getJSONObject(0);
                        weatherW.setDescription(WeatherUtil.getString("description", weatherObj));
                        weatherW.setIcon(WeatherUtil.getString("icon", weatherObj));
                        weather.weatherW = weatherW;

                }
            }

            return weather;


        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
