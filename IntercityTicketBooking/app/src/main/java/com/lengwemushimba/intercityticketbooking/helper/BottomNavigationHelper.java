package com.lengwemushimba.intercityticketbooking.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.lengwemushimba.intercityticketbooking.AllBuses;
import com.lengwemushimba.intercityticketbooking.Company;
import com.lengwemushimba.intercityticketbooking.Constants;
import com.lengwemushimba.intercityticketbooking.EditUserProfile;
import com.lengwemushimba.intercityticketbooking.R;
import com.lengwemushimba.intercityticketbooking.RequestHandler;
import com.lengwemushimba.intercityticketbooking.SharedPreferenceManager;
import com.lengwemushimba.intercityticketbooking.Utility;
import com.lengwemushimba.intercityticketbooking.ViewBookings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

/**
 * Created by lengwe on 7/25/18.
 */

public class BottomNavigationHelper {

    private static BottomNavigationHelper bottomNavigationHelper;
    private static Context context;
    private static final String TAG = BottomNavigationHelper.class.getSimpleName();

    private BottomNavigationHelper(Context context) {
        this.context = context;
    }

    public static synchronized BottomNavigationHelper getInstance(Context context) {
        if (bottomNavigationHelper == null) {
            bottomNavigationHelper = new BottomNavigationHelper(context);
            return bottomNavigationHelper;
        }
        BottomNavigationHelper.context = context;
        return bottomNavigationHelper;
    }

    public void bottonNavSettings(BottomNavigationViewEx bnve, int checkedResource) {
        bnve.enableAnimation(false);
        bnve.enableShiftingMode(false);
        bnve.enableItemShiftingMode(false);
        bnve.getMenu().findItem(checkedResource).setChecked(true);

        bnve.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.home:
//                        finishActivity(context);
                        Intent homeIntent = new Intent(context, Company.class);
                        context.startActivity(homeIntent);
                        return true;
                    case R.id.buses:
//                        finishActivity(context);
                        Intent busIntent = new Intent(context, AllBuses.class);
                        context.startActivity(busIntent);
                        return true;
                    case R.id.bookings:
//                        finishActivity(context);
                        Intent bookingIntent = new Intent(context, ViewBookings.class);
                        context.startActivity(bookingIntent);
                        return true;
                    case R.id.profile:
//                        finishActivity(context);
                        Intent profileIntent = new Intent(context, EditUserProfile.class);
                        context.startActivity(profileIntent);
                        return true;
                }
                return false;
            }
        });
        getBookingCount(bnve);
    }

    private void getBookingCount(final BottomNavigationViewEx bnve) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.BOOKING_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG + " onResponse", response);

                        try {
                            JSONObject jsObj = new JSONObject(response);
                            if (!jsObj.getBoolean("error")) {
                                addBadgeAt(bnve, 2, jsObj.getInt("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.d(TAG + " onErrorResponse", volleyError.getMessage());
//                        Toast.makeText(context, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userID_getBookingCount", String.valueOf(SharedPreferenceManager.getInstance(context).getUserId()));
                return params;
            }

        };
        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);
    }

    private Badge addBadgeAt(BottomNavigationViewEx bnve, int position, int number) {

        return new QBadgeView(context)
                .setBadgeNumber(number)
                .setGravityOffset(12, 2, true)
                .bindTarget(bnve.getBottomNavigationItemView(position));
    }

    public void finishActivity(Context context) {
        Activity activity = (Activity) context;
        activity.finish();
        Log.d(TAG + " contxt", activity.getClass().getSimpleName());
    }
}
