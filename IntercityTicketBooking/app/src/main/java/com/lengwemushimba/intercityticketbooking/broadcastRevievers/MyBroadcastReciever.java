package com.lengwemushimba.intercityticketbooking.broadcastRevievers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lengwemushimba.intercityticketbooking.Constants;
import com.lengwemushimba.intercityticketbooking.R;
import com.lengwemushimba.intercityticketbooking.RequestHandler;
import com.lengwemushimba.intercityticketbooking.SharedPreferenceManager;
import com.lengwemushimba.intercityticketbooking.ViewBookings;
import com.lengwemushimba.intercityticketbooking.model.Notification;
import com.lengwemushimba.intercityticketbooking.services.MyService;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lengwe on 6/28/18.
 */

public class MyBroadcastReciever extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

    }
//
//    private static final String TAG = MyBroadcastReciever.class.getSimpleName();
//    private List<Notification> notificationList;
//    private SharedPreferenceManager userPref;
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        this.notificationList = new ArrayList<>();
//        this.userPref = SharedPreferenceManager.ImageUploadHelper(MyService.myService);
//        Log.d(TAG+" called", "PPP");
//        getNotification();
//    }
//
//    public void getNotification() {
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.NOTIFICATION_URL,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//
//                        Log.d(TAG + " onResponse", response);
//                        try {
//                            JSONObject jsonObject = new JSONObject(response);
//
//                            if (!jsonObject.getBoolean("errorStatus")) {
//
//                                JSONArray recordsJsonArr = jsonObject.getJSONArray("records");
//                                for (int i = 0; i < recordsJsonArr.length(); i++) {
//                                    JSONObject tmpObj = (JSONObject) recordsJsonArr.get(i);
//
//                                    notificationList.add(new Notification(
//                                            tmpObj.getString("seatNumber"), tmpObj.getString("date"),
//                                            tmpObj.getString("dayOfWeek"),
//                                            tmpObj.getString("time"), tmpObj.getString("from"),
//                                            tmpObj.getString("to"), tmpObj.getString("busID")));
//                                }
//
//                                List<Notification> tmpNotiflist = new ArrayList<>();
//
//                                for (Notification n : notificationList) {
//
//                                    LocalDate now = LocalDate.now();
//                                    LocalDate dbDate = LocalDate.parse(n.getDate());
//                                    Period diff = new Period(now, dbDate);
////                                        int days = Math.abs(diff.getDays());
//                                    if (diff.getDays() == 1) {
//                                        tmpNotiflist.add(n);
//
//                                        String content = n.getFrom() + " - " + n.getTo() + "\n" +
//                                                n.getDate() + "  " + n.getWeekDay() + "\n" +
//                                                n.getTime() + "hrs";
//
//
//                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyService.myService);
//                                        builder.setSmallIcon(R.drawable.notifications);
//                                        builder.setContentTitle("Travel Reminder");
//                                        builder.setContentText(content);
//
//                                        Intent intent = new Intent(MyService.myService, ViewBookings.class);
//                                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(MyService.myService);
//                                        stackBuilder.addParentStack(ViewBookings.class);
//                                        stackBuilder.addNextIntent(intent);
//
//                                        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//                                        builder.setContentIntent(pendingIntent);
//
//                                        NotificationManager nm = (NotificationManager) MyService.myService.getSystemService(Context.NOTIFICATION_SERVICE);
//                                        nm.notify(0, builder.build());
////                                            thread.notifyAll();
//
//                                        updateNotif(n.getSeatNumber(), n.getDate(), n.getWeekDay(), n.getTime(), n.getFrom(),
//                                                n.getTo(), n.getBusID());
//
//                                    }
//                                }
//
////                                    showNotification();
//
//                                // TODO: 6/26/18 1.show notification, 2.update db,
////                                    notificationList.removeAll(tmpNotiflist);  // you dont need to remove just update db
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//
//            }
//        }) {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//                params.put("userID_getNotificationData", String.valueOf(userPref.getUserId()));
//                return params;
//            }
//        };
//        RequestHandler.ImageUploadHelper(MyService.myService).addToRequestQueue(stringRequest);
//    }
//
//    public void updateNotif(final String seatNumber, final String date, final String weekDay, final String time, final String from, final String to, final String busID) {
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.NOTIFICATION_URL,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.d(TAG + " onResponse_u", response);
//
//                        try {
//                            JSONObject jsObj = new JSONObject(response);
//                            if (!jsObj.getBoolean("error")){
//
//                            } else {
//                                Log.d(TAG + "message_u", jsObj.getString("message"));
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//                Log.d(TAG + " onErrorResponse", volleyError.getMessage());
//            }
//        }){
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//                params.put("seatNumber_u", seatNumber);
//                params.put("date_u", date);
//                params.put("dayOfWeek_u", weekDay);
//                params.put("time_u", time);
//                params.put("from_u", from);
//                params.put("to_u", to);
//                params.put("busID_u", busID);
//                return params;
//            }
//        }; RequestHandler.ImageUploadHelper(MyService.myService).addToRequestQueue(stringRequest);
//    }

}
