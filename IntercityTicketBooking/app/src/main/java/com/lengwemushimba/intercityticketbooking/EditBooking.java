package com.lengwemushimba.intercityticketbooking;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditBooking extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    @BindView(R.id.bookingDetails)
    TextView bookingInformation;
    @BindView(R.id.chooseDate)
    Button bookingDateBtn;
    @BindView(R.id.chooseSeat)
    Button seatNumberBtn;

    private static final String TAG = EditBooking.class.getSimpleName();

    private String seatNumber_x;
    private String date_x;

    private String date;
    private String dayOfWeek;
    private String time;
    private String from;
    private String to;
    private String busID;
    private int seatCount;
//    private int userID;
    private String bookingInfo;

    private ArrayList<Integer> bookedSeats;
    private int chosenSeats;

    private Set<Integer> weekDays = new HashSet<>(Arrays.asList(
            Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
            Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY,
            Calendar.SATURDAY));

    private ProgressDialog progressDialog;
    private SharedPreferenceManager userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_booking);
        ButterKnife.bind(this);

        userPref = SharedPreferenceManager.getInstance(EditBooking.this);

        if (!userPref.isLoggedIn()) {
            finish();
            startActivity(new Intent(getApplicationContext(), LogIn.class));
            return;
        }

//        this.userID = SharedPreferenceManager.getInstance(this).getUserId();

        bookedSeats = new ArrayList<>();
        progressDialog = new ProgressDialog(this);

        if (getIntent().hasExtra("seatNumber") && getIntent().hasExtra("date") &&
                getIntent().hasExtra("weekDay") && getIntent().hasExtra("time") &&
                getIntent().hasExtra("from") && getIntent().hasExtra("to") &&
                getIntent().hasExtra("busID") && getIntent().hasExtra("seatCount")) {

            this.seatNumber_x = getIntent().getExtras().getString("seatNumber");
            this.date_x = getIntent().getExtras().getString("date");
            this.dayOfWeek = getIntent().getExtras().getString("weekDay");
            this.time = getIntent().getExtras().getString("time");
            this.from = getIntent().getExtras().getString("from");
            this.to = getIntent().getExtras().getString("to");
            this.busID = getIntent().getExtras().getString("busID");
            this.seatCount = Integer.valueOf(getIntent().getExtras().getString("seatCount"));
            this.bookingInfo = "“From: " + from + "\nTo: " + to + "\nDay: " + dayOfWeek + "\nTime: " + time + "”";
            this.bookingInformation.setText(this.bookingInfo);
        }

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
                EditBooking.this,
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

    }

    public void chooseSeat(View view) {
        if (date == null) {
            Toast.makeText(this, "Please choose date first", Toast.LENGTH_SHORT).show();
        } else {
            seatPicker();
        }
    }

    private void seatPicker() {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.alertDialogTheme);
        alertDialog.setTitle("Choose Seat");

        final String[] seatNums = new String[seatCount];

        Log.d(TAG + " seatNums.length", String.valueOf(seatNums.length));

        for (int i = 0; i < seatNums.length; i++) {
            if (!bookedSeats.contains(i + 1)) {
                seatNums[i] = String.valueOf(i + 1);
            } else {
                seatNums[i] = String.valueOf(i + 1) + " Booked, Not Available";
            }
            Log.d(TAG + " seatNums[i]", seatNums[i]);
        }

        alertDialog.setSingleChoiceItems(seatNums, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG + " String.valueOf(which)", String.valueOf(which));
                Log.d(TAG + " seatNums[which]", seatNums[which]);
                if (!seatNums[which].contains(" Booked, Not Available")) {
                    chosenSeats = Integer.valueOf(seatNums[which]);
                    String seatTXT = "Seat " + seatNums[which];
                    seatNumberBtn.setText(seatTXT);
                } else {
                    Toast.makeText(EditBooking.this, "Please chose a different seat", Toast.LENGTH_SHORT).show();

                }
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
        AlertDialog dialog = alertDialog.create();
        dialog.show();
    }

    public void submitBooking(View view) {

        Gson gsonBuilder = new GsonBuilder().create();
        ArrayList<EditBooking.Booking> arrayList = new ArrayList<>();
        arrayList.add(new EditBooking.Booking(userPref.getUserId(), chosenSeats, date, dayOfWeek, time, from, to, busID));

        String bookingData = gsonBuilder.toJson(arrayList);
        Log.d(TAG + " bookingData", bookingData);
        Log.d(TAG + " date_x", date_x);
        Log.d(TAG + " seatNumber_x", seatNumber_x);

        if (bookingData == null || bookingData.isEmpty()) {
            Toast.makeText(this, "Please complete booking", Toast.LENGTH_LONG).show();
        } else if (chosenSeats == 0) {
            Toast.makeText(this, "Please choose a seat", Toast.LENGTH_LONG).show();
        } else {
            sendBookingToServer(bookingData);
        }
    }

    private void seatAvailability() {
        progressDialog.setMessage("Cheaking for seat availability...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.BOOKING_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        bookedSeats.clear();
                        Log.d(TAG + " onResponse", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            final JSONArray array = jsonObject.getJSONArray("records");

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = (JSONObject) array.get(i);
                                bookedSeats.add(object.getInt("seatNumber"));
                            }
                            if (bookedSeats.size() == seatCount) {
                                Toast.makeText(EditBooking.this, "No seats are available", Toast.LENGTH_SHORT).show();

                            } else if (bookedSeats.size() < seatCount) {
                                Toast.makeText(EditBooking.this, String.valueOf(seatCount - (bookedSeats.size())) + " are available", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(EditBooking.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
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
        progressDialog.setMessage("Updating booking...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.BOOKING_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d(TAG + " onResponse", response);
                        try {
                            JSONObject jsObj = new JSONObject(response);
                            if (!jsObj.getBoolean("error")) {
                                Toast.makeText(EditBooking.this, jsObj.getString("message"), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(EditBooking.this, ViewBookings.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(EditBooking.this, jsObj.getString("message"), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(EditBooking.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("bookingRecords_edit", bookingData);
                params.put("seatNumber_edit_x", seatNumber_x);
                params.put("date_edit_x", date_x);
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


}
