package com.lengwemushimba.intercityticketbooking;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

/**
 * Created by lengwe on 5/16/18.
 */

public class Utility {

    private static final String TAG = Utility.class.getSimpleName();
    private static Utility utility;
    private static Context context;
    private static List<Context> contextsList;

    private Utility(Context context) {
        Utility.context = context;
    }

    public static synchronized Utility getInstance(Context context) {
        if (utility == null) {
            utility = new Utility(context);
            contextsList = new ArrayList<>();
        }
        Utility.context = context;
        contextsList.add(Utility.context);
        return utility;
    }

//    public String getURLForResource(int resourcesid){
//        return Uri.parse("android.resource://"+R.class.getPackage().getName()+"/"+resourcesid).toString();
//    }
//
//    public static final Uri getUriToDrawable(@NonNull Context context,
//                                             @AnyRes int drawableId) {
//        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
//                "://" + context.getResources().getResourcePackageName(drawableId)
//                + '/' + context.getResources().getResourceTypeName(drawableId)
//                + '/' + context.getResources().getResourceEntryName(drawableId));
//        return imageUri;
//    }
    public static boolean isValidUrl(String url){
        return  Patterns.WEB_URL.matcher(url).matches();
    }
    public static boolean isValidPhone(String number){
        return Patterns.PHONE.matcher(number).matches();
    }

    public static boolean isValidEmail(String email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password){
        return password.length() > 5;
    }

    public static boolean hasIntenet() {
        boolean hasWifiConnection = false;
        boolean hasMobileConnection = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                if (ni.isConnected()) {
                    hasWifiConnection = true;
                }
            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (ni.isConnected()) {
                    hasMobileConnection = true;
                }
            }
        }
        return hasMobileConnection || hasWifiConnection;
    }

    public String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 37, bytes);
        byte[] imgbytes = bytes.toByteArray();
//        Log.d(TAG + " getAllocationByteCount", String.valueOf(bitmap.getAllocationByteCount()));
//        Log.d(TAG + " byteSize ", String.valueOf(imgbytes.length));
        System.gc();
        return Base64.encodeToString(imgbytes, Base64.DEFAULT);
    }


//    private static Bitmap imageBitmap;
//    private ProgressDialog progressDialog;
//    private static final int CAMERA_PERMISSINS_REQUEST_CODE = 1;
//    private Integer REQUEST_CAMERA = 2, SELECT_FILE = 3;

//    public void selectImage() {
//
//        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};
//        AlertDialog.Builder builder = new AlertDialog.Builder(AddCompany.addCompany);
//        builder.setTitle("Add Image");
//        builder.setItems(items, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int i) {
//
//                if (items[i].equals("Camera")) {
//                    if (AddCompany.addCompany.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//                        invokeCamera();
//                    } else {
//                        String[] permissinRequest = {Manifest.permission.CAMERA};
//                        AddCompany.addCompany.requestPermissions(permissinRequest, CAMERA_PERMISSINS_REQUEST_CODE);
//                    }
//
//                } else if (items[i].equals("Gallery")) {
//                    Intent intent = new Intent();
//                    intent.setType("image/*");
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    AddCompany.addCompany.startActivityForResult(intent, SELECT_FILE);
//
//                } else if (items[i].equals("Cancel")) {
//                    dialog.dismiss();
//                }
//            }
//        });
//        builder.show();
//    }
//
//    private void invokeCamera() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        AddCompany.addCompany.startActivityForResult(intent, REQUEST_CAMERA);
//    }
//
//
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        AddCompany.addCompany.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == CAMERA_PERMISSINS_REQUEST_CODE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                invokeCamera();
//            } else {
//                Toast.makeText(context, "Cannot take photo without permisions", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        AddCompany.addCompany.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == Activity.RESULT_OK && data != null) {
//
//            if (requestCode == REQUEST_CAMERA) {
//                Bundle bundle = data.getExtras();
//                imageBitmap = (Bitmap) bundle.get("data");
//                Uri uri = utility.getUriFromBitmap(imageBitmap);
//                Log.d("imageURI_cam: ", uri.toString());
//                Log.d("imageBitmap_cam: ", utility.bitmapToString(imageBitmap));
//
//            } else if (requestCode == SELECT_FILE) {
//                Uri path = data.getData();
//                try {
//                    imageBitmap = MediaStore.Images.Media.getBitmap(AddCompany.addCompany.getContentResolver(), path);
//                    Log.d("imageURI_file: ", path.toString());
//                    Log.d("imageBitmap_file: ", utility.bitmapToString(imageBitmap));
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    public Uri getUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        Uri uri = Uri.parse(path);
        return uri;
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

    public void finishActivity(Context context) {
        Activity activity;
        try {
            activity = (Activity) context;
            activity.finish();
            Log.d(TAG + " contxt", activity.getClass().getSimpleName());
        } catch (ClassCastException e) {
            Utility.context = contextsList.get(contextsList.size() - 2);
            contextsList.clear();
            finishActivity(Utility.context);
            e.printStackTrace();
        }
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
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d(TAG + " onErrorResponse", volleyError.getMessage());
                        Toast.makeText(context, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
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
                .bindTarget(bnve.getBottomNavigationItemView(position))
                .setOnDragStateChangedListener(new Badge.OnDragStateChangedListener() {
                    @Override
                    public void onDragStateChanged(int dragState, Badge badge, View targetView) {
                        if (Badge.OnDragStateChangedListener.STATE_SUCCEED == dragState)
                            Toast.makeText(context, "removed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public boolean isOnline() {
        int timeout = 1500;
        Socket socket = new Socket();
        SocketAddress socketAddress = new InetSocketAddress("8.8.8.8", 53);
        try {
            socket.connect(socketAddress, timeout);
            socket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
