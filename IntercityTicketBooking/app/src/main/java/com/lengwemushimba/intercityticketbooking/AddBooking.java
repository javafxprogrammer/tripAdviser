package com.lengwemushimba.intercityticketbooking;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.lengwemushimba.intercityticketbooking.Data.JSONWeatherParser;
import com.lengwemushimba.intercityticketbooking.Data.WeatherHttpClient;
import com.lengwemushimba.intercityticketbooking.model.Weather;
import com.lengwemushimba.intercityticketbooking.utility.WeatherUtil;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.internal.Util;

public class AddBooking extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = AddBooking.class.getSimpleName();
    private Utility utility;

    @BindView(R.id.weather)
    TextView weather_;
    @BindView(R.id.temp)
    TextView temp;
    @BindView(R.id.wind)
    TextView wind;
    @BindView(R.id.humidity)
    TextView humidity;
    @BindView(R.id.pressure)
    TextView pressure;
    @BindView(R.id.weatherIcon)
    ImageView weatherIcon;


    @BindView(R.id.bookingDetails)
    TextView bookingInformation;
    @BindView(R.id.chooseDate)
    Button bookingDateBtn;
    @BindView(R.id.chooseSeat)
    Button seatNumberBtn;

    private int userID;
    private int seatNumber;//remove
    private String date;
    private String dayOfWeek;
    private String time;
    private String from;
    private String to;
    private String busID;
    private int seatCount;
    private String bookingInfo;

    private ArrayList<Integer> bookedSeats;
    private Set<Integer> chosenSeats;

    private Set<Integer> weekDays = new HashSet<>(Arrays.asList(
            Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
            Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY,
            Calendar.SATURDAY));

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_booking);
        ButterKnife.bind(this);
        this.utility = Utility.getInstance(AddBooking.this);

        // TODO: 5/31/18 disable seatChoosing btn untill date is selected, then get booked seats via Volley 

        if (SharedPreferenceManager.getInstance(this).getBookingDetails() != null) {
            final Bundle bookingDetails = SharedPreferenceManager.getInstance(this).getBookingDetails();
            busID = bookingDetails.getString("busID");
            from = bookingDetails.getString("from");
            to = bookingDetails.getString("to");
            dayOfWeek = bookingDetails.getString("weekDay");
            time = bookingDetails.getString("time");
//            date = bookingDetails.getString("date");
            seatCount = bookingDetails.getInt("seatCount");
            bookingInfo = bookingDetails.getString("bookingInfo");
            bookingInformation.setText(bookingDetails.getString("bookingInfo"));
//            bookingDateBtn.setText(date);
            Log.d("sharedPref1", dayOfWeek);

        } else {
            Log.d("sharedPref1", "false");
        }

        if (!SharedPreferenceManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(getApplicationContext(), LogIn.class));
            return;
        }

        this.userID = SharedPreferenceManager.getInstance(this).getUserId();

        if (getIntent().hasExtra("busID") &&
                getIntent().hasExtra("from") &&
                getIntent().hasExtra("to") &&
                getIntent().hasExtra("day") &&
                getIntent().hasExtra("time") &&
                getIntent().hasExtra("seatCount") &&
                getIntent().hasExtra("bookingInfo")) {

            Bundle bundle = getIntent().getExtras();
            this.busID = bundle.getString("busID");
            this.from = bundle.getString("from");
            this.to = bundle.getString("to");
            this.dayOfWeek = bundle.getString("day");
            this.time = bundle.getString("time");
            this.seatCount = bundle.getInt("seatCount");
            bookingInfo = bundle.getString("bookingInfo");
            bookingInformation.setText(bookingInfo);
        }

        bookedSeats = new ArrayList<>();
        chosenSeats = new HashSet<>();

        switch (dayOfWeek) {
            case "Sunday":
                dayOfWeek = String.valueOf(Calendar.SUNDAY);
                break;
            case "Monday":
                dayOfWeek = String.valueOf(Calendar.MONDAY);
                break;
            case "Tuesday":
                dayOfWeek = String.valueOf(Calendar.TUESDAY);
                break;
            case "Wednesday":
                dayOfWeek = String.valueOf(Calendar.WEDNESDAY);
                break;
            case "Thursday":
                dayOfWeek = String.valueOf(Calendar.THURSDAY);
                break;
            case "Friday":
                dayOfWeek = String.valueOf(Calendar.FRIDAY);
                break;
            case "Saturday":
                dayOfWeek = String.valueOf(Calendar.SATURDAY);
                break;
        }

        weekDays.remove(Integer.parseInt(dayOfWeek));

        progressDialog = new ProgressDialog(this);
    }


    public void getDate() {

        Calendar weekday;
        List<Calendar> weekends = new ArrayList<>();
        int weeks = 1042;

        for (int i = -7; i < (weeks * 7); i = i + 7) {
            for (int x : weekDays) {
                weekday = Calendar.getInstance();
                weekday.add(Calendar.DAY_OF_YEAR, (x - weekday.get(Calendar.DAY_OF_WEEK) + 7 + i));
                weekends.add(weekday);
            }
        }

        Calendar[] disabledDays = weekends.toArray(new Calendar[weekends.size()]);

        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                AddBooking.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        Calendar cal = Calendar.getInstance();
        dpd.setMinDate(cal);
        dpd.setDisabledDays(disabledDays);
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    public void chooseDate(View view) {
        getDate();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        String monthOfYear_ = String.valueOf(monthOfYear + 1);
        String dayOfMonth_ = String.valueOf(dayOfMonth);

        if (monthOfYear_.length() == 1) {
            monthOfYear_ = "0" + monthOfYear_;
        }
        if (dayOfMonth_.length() == 1) {
            dayOfMonth_ = "0" + dayOfMonth_;
        }

        String bookingDate = year + "-" + monthOfYear_ + "-" + dayOfMonth_;
        this.bookingDateBtn.setText(bookingDate);
        this.date = bookingDate;

        seatAvailability();
        if (utility.hasIntenet()) {
            renderWeatherData(from + ",zm");
        } else {
            Toast.makeText(this, "No internet, please connect to show weather", Toast.LENGTH_SHORT).show();
        }

    }

    public void chooseSeat(View view) {
        if (date == null) {
            Toast.makeText(this, "Please choose date first", Toast.LENGTH_SHORT).show();
        } else {
            seatPicker();
        }
    }

    private void seatPicker() {

//        final View view = LayoutInflater.from(this).inflate(R.layout.bus_seats, null);
//        final TextView seatText = (TextView) view.findViewById(R.id.selectedSeats);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.alertDialogTheme);

        final String[] seatNums = new String[seatCount];

        Log.d("AddBooking sizeof", String.valueOf(seatNums.length));

        for (int i = 0; i < seatNums.length; i++) {
            if (!bookedSeats.contains(i + 1)) {
                seatNums[i] = String.valueOf(i + 1);
            } else {
                seatNums[i] = String.valueOf(i + 1) + " Booked, Not Available";
            }
            Log.d("AddBooking i", seatNums[i]);
        }


        final boolean[] seatStatus = new boolean[seatNums.length];
        Log.d("AddBooking seatNums", String.valueOf(seatNums.length));
        for (int i = 0; i < seatNums.length; i++) {
            seatStatus[i] = false;
        }

//        final List<String> seatList = Arrays.asList(seatNums);

        alertDialog.setTitle("Choose Seat");

        alertDialog.setMultiChoiceItems(seatNums, seatStatus, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                seatStatus[which] = isChecked;

                if (!seatNums[which].contains(" Booked, Not Available")) {
                    if (isChecked) {
                        chosenSeats.add(Integer.decode(seatNums[which]));
                    } else {
                        chosenSeats.remove(Integer.decode(seatNums[which]));
                    }
                } else {
                    Toast.makeText(AddBooking.this, "Please chose a different seat", Toast.LENGTH_SHORT).show();
                }

                String items = "Seats ";
                for (int x : chosenSeats) {
                    Log.d("chosenSeats", String.valueOf(x));
                    items = items + String.valueOf(x) + ", ";
                }
                seatNumberBtn.setText(items);
            }
        });
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = alertDialog.create(); // TODO: 6/3/18 fix
        dialog.show();
    }

    public void submitBooking(View view) {

        Gson gsonBuilder = new GsonBuilder().create();
        ArrayList<AddBooking.Booking> arrayList = new ArrayList<>();

        for (int i : chosenSeats) {
            arrayList.add(new Booking(userID, i, date, dayOfWeek, time, from, to, busID));
        }

        String bookingData = gsonBuilder.toJson(arrayList);
        Log.d("zzz_", bookingData);

        if (arrayList.isEmpty() || bookingData == null || bookingData.isEmpty()) {
            Toast.makeText(this, "Please complete booking", Toast.LENGTH_LONG).show();
        } else {
            sendBookingToServer(bookingData);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        intToWeekDay();
        outState.putString("busID", busID);
        outState.putString("from", from);
        outState.putString("to", to);
        outState.putString("weekDay", dayOfWeek);
        outState.putString("time", time);
        outState.putString("date", date);
        outState.putInt("seatCount", seatCount);
        outState.putString("bookingInfo", bookingInfo);
        SharedPreferenceManager.getInstance(this).setBookingDetails((Bundle) outState.clone());
        chosenSeats.clear();
        super.onSaveInstanceState(outState);
    }

    private void intToWeekDay() {

        if (dayOfWeek.equals(String.valueOf(Calendar.SUNDAY))) {
            dayOfWeek = "Sunday";

        } else if (dayOfWeek.equals(String.valueOf(Calendar.MONDAY))) {
            dayOfWeek = "Monday";

        } else if (dayOfWeek.equals(String.valueOf(Calendar.TUESDAY))) {
            dayOfWeek = "Tuesday";

        } else if (dayOfWeek.equals(String.valueOf(Calendar.WEDNESDAY))) {
            dayOfWeek = "Wednesday";

        } else if (dayOfWeek.equals(String.valueOf(Calendar.THURSDAY))) {
            dayOfWeek = "Thursday";

        } else if (dayOfWeek.equals(String.valueOf(Calendar.FRIDAY))) {
            dayOfWeek = "Friday";

        } else if (dayOfWeek.equals(String.valueOf(Calendar.SATURDAY))) {
            dayOfWeek = "Saturday";

        }
    }

    private void seatAvailability() {
        progressDialog.setMessage("Cheaking for seat availability...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.BOOKING_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        bookedSeats.clear();

                        Log.d(TAG+" - bookedSeats", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
//                            Log.d("AddBooking_json_response", (jsonObject.getString("records")));

                            final JSONArray array = jsonObject.getJSONArray("records");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = (JSONObject) array.get(i);
                                bookedSeats.add(object.getInt("seatNumber"));
                            }
                            if (bookedSeats.size() == seatCount) {

                                Toast.makeText(AddBooking.this, "No seats are available", Toast.LENGTH_SHORT).show();
                            } else if (bookedSeats.size() < seatCount) {

//                                seatNumberBtn.setEnabled(true);
                                Toast.makeText(AddBooking.this, String.valueOf(seatCount - (bookedSeats.size())) + " are available", Toast.LENGTH_SHORT).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.hide();
                        Toast.makeText(AddBooking.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                intToWeekDay();
                Map<String, String> params = new HashMap<>();
                Log.d("params", date + ", " + dayOfWeek + ", " + time + ", " + from + ", " + to + ", " + String.valueOf(busID));
                params.put("date", date);
                params.put("dayOfWeek", dayOfWeek);
                params.put("time", time);
                params.put("from", from);
                params.put("to", to);
                params.put("busID", String.valueOf(busID));
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void sendBookingToServer(final String bookingData) {
        progressDialog.setMessage("Booking processing...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.BOOKING_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d(TAG + "response", response);
                        try {
                            String failedBookings = "";
                            JSONObject jsonObject = new JSONObject(response);
                            final JSONArray jsonArray = jsonObject.getJSONArray("result");

                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    final JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                                    failedBookings = failedBookings + jsonObject1.getString("seatNumber") + ", ";
                                    Log.e(TAG + " errorMessage", jsonObject1.getString("errorMessage"));
                                }
                                Toast.makeText(AddBooking.this, "Failed to book seats " + failedBookings, Toast.LENGTH_LONG).show();
                                seatNumberBtn.setText("Choose Seat");
                            } else {
                                Toast.makeText(AddBooking.this, "Booking successfull", Toast.LENGTH_LONG).show();
                                seatNumberBtn.setText("Choose Seat");
                                seatAvailability();
                                startActivity(new Intent(AddBooking.this, ViewBookings.class));

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.hide();
                        Toast.makeText(AddBooking.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("bookingRecords", bookingData);
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    private static class Booking {
        private int userID;
        private int seatNumber;
        private String date;
        private String dayOfWeek;
        private String time;
        private String from;
        private String to;
        private String busID;

        public Booking(int userID, int seatNumber, String date, String dayOfWeek, String time, String from, String to, String busID) {
            this.userID = userID;
            this.seatNumber = seatNumber;
            this.date = date;
            this.dayOfWeek = dayOfWeek;
            this.time = time;
            this.from = from;
            this.to = to;
            this.busID = busID;
        }
    }

    public void renderWeatherData(String city) {
        WeatherTask weatherTask = new WeatherTask();
        weatherTask.execute(new String[]{city + "&units=metric"});
    }


    Weather weather = new Weather();

    private class WeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... strings) {
            String data = new WeatherHttpClient().getWeatherData(strings[0]);
            if (data == null){
                return null;
            }
            weather = JSONWeatherParser.getWeather(data, date);

            Log.d(TAG + " main temp", weather.mainW.getTemp());
            Log.d(TAG + " wind speed", weather.windW.getSpeed());
            Log.d(TAG + " weather desc", weather.weatherW.getDescription());
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            if (weather == null) {
                return;
            }
            weather_.setText(new StringBuilder("Weather: ").append(weather.weatherW.getDescription()).toString());
            wind.setText(new StringBuilder("Wind: ").append(weather.windW.getSpeed()).toString());
            humidity.setText(new StringBuilder("Humidity: ").append(weather.mainW.getHumidity()).toString());
            pressure.setText(new StringBuilder("Pressure: ").append(weather.mainW.getPressure()).toString());
            temp.setText(new StringBuilder("Temp: ").append(weather.mainW.getTemp()).toString());
            Picasso.get().load(WeatherUtil.ICON_URL + weather.weatherW.getIcon() + ".png").error(R.drawable.cloud_off).placeholder(R.drawable.cloud_off).into(weatherIcon);

        }
    }

}

