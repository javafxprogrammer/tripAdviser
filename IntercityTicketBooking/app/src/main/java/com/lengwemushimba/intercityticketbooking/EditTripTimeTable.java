package com.lengwemushimba.intercityticketbooking;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditTripTimeTable extends AppCompatActivity {


    @BindView(R.id.fromParent)
    TextInputLayout fromParent;
    @BindView(R.id.toParent)
    TextInputLayout toParent;
    @BindView(R.id.amountParent)
    TextInputLayout amountParent;

    @BindView(R.id.fromChild)
    TextInputEditText fromChild;
    @BindView(R.id.toChild)
    TextInputEditText toChild;
    @BindView(R.id.amountChild)
    TextInputEditText amountChild;

    @BindView(R.id.weekdayBtn)
    Button weekdayBtn;
    @BindView(R.id.departureTimeBtn)
    Button departureTimeBtn;

    private static final String TAG = EditTripTimeTable.class.getSimpleName();
    private ProgressDialog progressDialog;

    private String from_x;
    private String to_x;
    private String busID_x;
    private String companyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_trip_time_table);
        ButterKnife.bind(this);
        setDrawableListeners();
        progressDialog = new ProgressDialog(this);


        if (getIntent().hasExtra("companyID") && getIntent().hasExtra("from") && getIntent().hasExtra("to") && getIntent().hasExtra("busID")) {
            this.from_x = getIntent().getExtras().getString("from");
            this.to_x = getIntent().getExtras().getString("to");
            this.busID_x = getIntent().getExtras().getString("busID");
            this.companyID = getIntent().getExtras().getString("companyID");

            Log.d(TAG+" extras", from_x+", "+to_x+", "+busID_x+", "+companyID);
        }
    }

    public void editShedule(View view) {

        final String from = fromChild.getText().toString().trim();
        final String to = toChild.getText().toString().trim();
        final String amount = amountChild.getText().toString().trim();
        final String weekDay = weekdayBtn.getText().toString();
        final String time = departureTimeBtn.getText().toString();

        if (from.isEmpty()){
            fromParent.setErrorEnabled(true);
            fromParent.setError("Please enter start destination");
        } else {
            fromParent.setErrorEnabled(false);
        }
        if (to.isEmpty()){
            toParent.setErrorEnabled(true);
            toParent.setError("Please enter end destination");
        } else {
            toParent.setErrorEnabled(false);
        }
        if (amount.isEmpty()){
            amountParent.setErrorEnabled(true);
            amountParent.setError("Please enter booking amount");
        } else {
            amountParent.setErrorEnabled(false);
        }
        if (weekDay.isEmpty()){
            Toast.makeText(this, "Please enter traveling week-day", Toast.LENGTH_SHORT).show();
        }
        if (time.isEmpty()){
            Toast.makeText(this, "Please enter time of departure", Toast.LENGTH_SHORT).show();
        }

        if (!from.isEmpty() && !to.isEmpty() && !amount.isEmpty() && !weekDay.isEmpty() && !time.isEmpty()){

            progressDialog.setMessage("Editing schedule...");
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.TRIP_TIMETABLE_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG+" onResponse", response);

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (!jsonObject.getBoolean("error")){
                                    Intent intent = new Intent(EditTripTimeTable.this, CompanyBus.class);
//                                    intent.putExtra("companyID", companyID);
                                    intent.putExtra("companyID", StaticVar.map.get("companyID"));
                                    intent.putExtra("companyName", StaticVar.map.get("companyName"));
                                    intent.putExtra("companyImage", StaticVar.map.get("companyImage"));
                                    startActivity(intent);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.d(TAG+" onErrorResponse", volleyError.getMessage());
                            Toast.makeText(EditTripTimeTable.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("busID_updateSchedule_x", busID_x);
                    params.put("from_updateSchedule_x", from_x);
                    params.put("to_updateSchedule_x", to_x);
                    params.put("from_updateSchedule", from);
                    params.put("to_updateSchedule", to);
                    params.put("amount_updateSchedule", amount);
                    params.put("weekDay_updateSchedule", weekDay);
                    params.put("time_updateSchedule", time);
                    return params;
                }
            };
            RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

        }

    }

    private void setDrawableListeners() {
        fromChild.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (fromChild.getRight() - fromChild.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
//                        Log.d(TAG+" FromRightDrawable", "TRUE");
                        getPlaceFrom(FROM_REQUEST_CODE);
                        return true;

                    }
                }
                return false;
            }
        });

        toChild.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (toChild.getRight() - toChild.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
//                        Log.d(TAG+" ToRightDrawable", "TRUE");
                        getPlaceFrom(TO_REQUEST_CODE);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void pickWeekDay(View view) {

        Context wrapper = new ContextThemeWrapper(EditTripTimeTable.this, R.style.popupTheme);
        PopupMenu popupMenu = new PopupMenu(wrapper, findViewById(R.id.weekdayBtn));
        popupMenu.getMenuInflater().inflate(R.menu.weekdays_options_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.sunday:
                        weekdayBtn.setText(item.getTitle());
                        break;
                    case R.id.monday:
                        weekdayBtn.setText(item.getTitle());
                        break;
                    case R.id.tuesday:
                        weekdayBtn.setText(item.getTitle());
                        break;
                    case R.id.wednesday:
                        weekdayBtn.setText(item.getTitle());
                        break;
                    case R.id.thursday:
                        weekdayBtn.setText(item.getTitle());
                        break;
                    case R.id.friday:
                        weekdayBtn.setText(item.getTitle());
                        break;
                    case R.id.saturday:
                        weekdayBtn.setText(item.getTitle());
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public void pickTime(View view) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                                                                @Override
                                                                public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {

                                                                    String hr = String.valueOf(hourOfDay);
                                                                    String min = String.valueOf(minute);
                                                                    String sec = String.valueOf(second);

                                                                    if (hr.length() == 1){hr = "0"+hr;}
                                                                    if (min.length() == 1){min = "0"+min;}
                                                                    if (sec.equals("0")){sec = sec+"0";}

                                                                    String time = hr+":"+min+":"+sec;
                                                                    departureTimeBtn.setText(time);
                                                                }
                                                            },
                now.get(Calendar.HOUR),
                now.get(Calendar.MINUTE),
                true);
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    private Integer PLACE_PICKER_REQUEST_CODE;
    private Integer FROM_REQUEST_CODE = 10;
    private Integer TO_REQUEST_CODE = 11;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PLACE_PICKER_REQUEST_CODE){
                Place place = PlacePicker.getPlace(data, this);
                String address = place.getName().toString();

                if (PLACE_PICKER_REQUEST_CODE.equals(FROM_REQUEST_CODE)){
                    fromChild.setText(address);

                } else if (PLACE_PICKER_REQUEST_CODE.equals(TO_REQUEST_CODE)){
                    toChild.setText(address);
                }
                Toast.makeText(this, "Location: "+address, Toast.LENGTH_SHORT).show();
            }
//            if (requestCode == TO_REQUEST_CODE){
//                Place place = PlacePicker.getPlace(data, this);
//                String address = place.getName().toString()+"\n"+place.getAddress().toString();
//
//                toChild.setText(address);
//                Toast.makeText(this, "Location: "+address, Toast.LENGTH_SHORT).show();
//            }
        }
    }

    public void getPlaceFrom(Integer code) {
        PLACE_PICKER_REQUEST_CODE = code;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            Intent intent = builder.build(getApplicationContext());
            startActivityForResult(intent, code);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }
}
