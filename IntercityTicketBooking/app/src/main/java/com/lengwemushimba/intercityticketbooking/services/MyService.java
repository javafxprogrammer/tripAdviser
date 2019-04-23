package com.lengwemushimba.intercityticketbooking.services;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lengwemushimba.intercityticketbooking.Company;
import com.lengwemushimba.intercityticketbooking.Constants;
import com.lengwemushimba.intercityticketbooking.R;
import com.lengwemushimba.intercityticketbooking.RequestHandler;
import com.lengwemushimba.intercityticketbooking.SharedPreferenceManager;
import com.lengwemushimba.intercityticketbooking.Utility;
import com.lengwemushimba.intercityticketbooking.ViewBookings;
import com.lengwemushimba.intercityticketbooking.broadcastRevievers.MyBroadcastReciever;
import com.lengwemushimba.intercityticketbooking.model.Notification;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lengwe on 6/26/18.
 */

public class MyService extends Service {

    private static final String TAG = MyService.class.getSimpleName();
//    public static MyService myService;

    final class MyThreadService implements Runnable {
        int serviceID;
        private SharedPreferenceManager userPref;
        private List<Notification> notificationList;
        private Utility utility;
        private final String PROMO_FILE = "promotions.txt";
        private final File PROMO_DIR = new File(MyService.this.getFilesDir(), "promotions");

        public MyThreadService(int serviceID) {
//            File f = new File(PROMO_DIR, PROMO_FILE);
//            if (f.exists()){
//                f.delete();
//                Log.d(TAG,"file_deleted");
//            }

//            Log.d("promoDir", PROMO_DIR.getAbsolutePath());
            utility = Utility.getInstance(MyService.this);
            this.serviceID = serviceID;
            this.userPref = SharedPreferenceManager.getInstance(MyService.this);
            this.notificationList = new ArrayList<>();
        }

        @Override
        public void run() {
            synchronized (this) {
//                int i = 0;
                while (true) {
                    try {
                        // TODO: 6/27/18 alarm manager
                        if (Utility.hasIntenet()) {
                            Log.d(TAG+" internetStatus ", "true");
                            getNotification();
                            wait(8000);
                            getPromotion();
                            wait(60000);
                        } else {
                            Log.d(TAG+" internetStatus ", "false");
                            wait(10000);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
//                stopSelf(serviceID);
            }
        }

        private void getPromotion() {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.PROMOTION_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG + " getPromotion onResponse", response);
                            try {
                                JSONObject jsObj = new JSONObject(response);
                                if (!jsObj.getBoolean("errorStatus")) {

                                    JSONArray recordsJsonArr = jsObj.getJSONArray("records");
                                    for (int i = 0; i < recordsJsonArr.length(); i++) {
                                        JSONObject tmpObj = (JSONObject) recordsJsonArr.get(i);

//                                        int bookingCount = Integer.parseInt(tmpObj.getString("numberOfBookings"));
//                                        int ticketCount = Integer.parseInt(tmpObj.getString("ticketCountForDiscount"));
//                                        double discount = Double.parseDouble(tmpObj.getString("discount"));
//                                        String companyName = tmpObj.getString("companyName");
//                                        String companyID = tmpObj.getString("companyID");
//
//                                        if (ticketCount <= bookingCount){
//                                            int numberOfDiscounts = bookingCount / ticketCount;
//
//
//
//
//                                        }

                                        String promoData = tmpObj.getString("userID") +
                                                tmpObj.getString("numberOfBookings") +
                                                tmpObj.getString("companyID");
                                        String promoContent = tmpObj.getString("CompanyName") + " | " + tmpObj.getString("discount") + "% off";

                                        if (createDirFile()) {
                                            try {
                                                if (!promoExists(promoData)) {
                                                    showPromoNotification(promoContent);
                                                    savePromo(promoData);
                                                } else {
                                                    File file = new File(PROMO_DIR, PROMO_FILE);
                                                    BufferedReader bfr = new BufferedReader(new FileReader(file));
                                                    String line = bfr.readLine();
                                                    while (line != null) {
                                                        Log.d(TAG + " lineByline", line);
                                                        line = bfr.readLine();
                                                    }
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
//                            Log.d(TAG + " getPromotion onErrorResponse", error.getMessage());
//                            Toast.makeText(MyService.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("userID_getPromo", String.valueOf(userPref.getUserId()));
                    return params;
                }
            };
            RequestHandler.getInstance(MyService.this).addToRequestQueue(stringRequest);
        }

        private void showPromoNotification(String content) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(MyService.this);
            builder.setSmallIcon(R.drawable.notifications_active);
            builder.setContentTitle("Promotion");
            builder.setContentText(content);

            Intent intent = new Intent(MyService.this, Company.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(MyService.this);
            stackBuilder.addParentStack(Company.class);
            stackBuilder.addNextIntent(intent);

            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            int randInt = (int) (new Date().getTime());
//            Log.d(TAG+ "Promo rand = ", String.valueOf(randInt));
            nm.notify(10, builder.build());

        }

        private boolean savePromo(String promoData) {
            File file = new File(PROMO_DIR, PROMO_FILE);
            PrintWriter printWriter;
            try {
                printWriter = new PrintWriter(new FileWriter(file, true));
                printWriter.println(promoData);
                printWriter.flush();
                printWriter.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean promoExists(String promoData) throws IOException {
            File file = new File(PROMO_DIR, PROMO_FILE);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            String line = bufferedReader.readLine();

            while (line != null) {
                if (line.equals(promoData)) {
                    return true;
                }
                line = bufferedReader.readLine();
            }
            return false;
        }

        private boolean createDirFile() {

            if (!PROMO_DIR.exists()) {
                PROMO_DIR.mkdir();
            }
            File file = new File(PROMO_DIR, PROMO_FILE);
            try {
                file.createNewFile();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }


        public void getNotification() {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.NOTIFICATION_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Log.d(TAG + " getNotification onResponse", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                if (!jsonObject.getBoolean("errorStatus")) {

                                    JSONArray recordsJsonArr = jsonObject.getJSONArray("records");
                                    for (int i = 0; i < recordsJsonArr.length(); i++) {
                                        JSONObject tmpObj = (JSONObject) recordsJsonArr.get(i);

                                        notificationList.add(new Notification(
                                                tmpObj.getString("seatNumber"), tmpObj.getString("date"),
                                                tmpObj.getString("dayOfWeek"),
                                                tmpObj.getString("time"), tmpObj.getString("from"),
                                                tmpObj.getString("to"), tmpObj.getString("busID")));
                                    }

                                    List<Notification> tmpNotiflist = new ArrayList<>();

                                    for (Notification n : notificationList) {

                                        LocalDate now = LocalDate.now();
                                        LocalDate dbDate = LocalDate.parse(n.getDate());
                                        Period diff = new Period(now, dbDate);
                                        if (diff.getDays() == 1) {
                                            tmpNotiflist.add(n);

                                            String content = n.getFrom() + " - " + n.getTo() + " | Tomorrow " +
                                                    n.getTime() + " hrs | Seat "+ n.getSeatNumber();


                                            Intent intent = new Intent(MyService.this, ViewBookings.class);
                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(MyService.this);
                                            stackBuilder.addParentStack(ViewBookings.class);
                                            stackBuilder.addNextIntent(intent);
                                            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(MyService.this);
                                            builder.setSmallIcon(R.drawable.notifications_active);
                                            builder.setContentTitle("Travel");
                                            builder.setContentText(content);
                                            builder.setContentIntent(pendingIntent);

                                            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                            int randInt = (int) (new Date().getTime());
//                                            Log.d(TAG+ "valueOfInt = ", String.valueOf(randInt));
                                            nm.notify(randInt, builder.build());

                                            updateNotif(n.getSeatNumber(), n.getDate(), n.getWeekDay(), n.getTime(), n.getFrom(),
                                                    n.getTo(), n.getBusID());
                                        }
                                    } notificationList.clear();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
//                    Toast.makeText(MyService.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    Log.d(TAG + " userID", String.valueOf(userPref.getUserId()));
                    params.put("userID_getNotificationData", String.valueOf(userPref.getUserId()));
                    return params;
                }
            };
            RequestHandler.getInstance(MyService.this).addToRequestQueue(stringRequest);
        }

        public void updateNotif(final String seatNumber, final String date, final String weekDay, final String time, final String from, final String to, final String busID) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.NOTIFICATION_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG + " updateNotif onResponse", response);

                            try {
                                JSONObject jsObj = new JSONObject(response);
                                if (!jsObj.getBoolean("error")) {

                                } else {
                                    Log.d(TAG + " updateNotif message", jsObj.getString("message"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.d(TAG + "updateNotif onErrorResponse", volleyError.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("seatNumber_u", seatNumber);
                    params.put("date_u", date);
                    params.put("dayOfWeek_u", weekDay);
                    params.put("time_u", time);
                    params.put("from_u", from);
                    params.put("to_u", to);
                    params.put("busID_u", busID);
                    return params;
                }
            };
            RequestHandler.getInstance(MyService.this).addToRequestQueue(stringRequest);
        }
    }

    public static Thread thread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();
        thread = new Thread(new MyThreadService(startId));
        thread.start();
        System.gc();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
//        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
