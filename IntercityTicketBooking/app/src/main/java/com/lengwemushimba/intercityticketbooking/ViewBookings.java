package com.lengwemushimba.intercityticketbooking;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.lengwemushimba.intercityticketbooking.adapter.BookingAdapter;
import com.lengwemushimba.intercityticketbooking.adapter.CompanyAdapter;
import com.lengwemushimba.intercityticketbooking.helper.BitmaptHelper;
import com.lengwemushimba.intercityticketbooking.helper.BottomNavigationHelper;
import com.lengwemushimba.intercityticketbooking.model.BookingData;
import com.lengwemushimba.intercityticketbooking.model.CompanyData;
import com.lengwemushimba.intercityticketbooking.model.ReviewData;
import com.squareup.picasso.Picasso;

import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewBookings extends AppCompatActivity implements RecognitionListener {

    private static final String TAG = ViewBookings.class.getSimpleName();
    private BottomNavigationHelper bottomNavigationHelper;
//    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private ArrayList<BookingData> bookingDataList;
    private ProgressDialog progressDialog;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsingtoolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.image)
    ImageView profilePic;
    @BindView(R.id.heading)
    TextView heading;

    private SharedPreferenceManager userPref;

    private String userID;

    private int xDim, yDim;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private static SearchView searchView;
    private ArrayList<BookingData> newList;

    //Get the size of the Image view after the Activity has completely loaded
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        xDim = profilePic.getWidth();
        yDim = profilePic.getHeight();

        Log.d(TAG+"xy-", "xDim = "+xDim+", yDim = "+yDim);

        if (this.profilePic.getDrawable() == null){
            new BitmaptHelper(profilePic).execute( userPref.getUserPicture(),
                    String.valueOf(xDim),
                    String.valueOf(yDim));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_bookings);
        ButterKnife.bind(this);

        this.userPref = SharedPreferenceManager.getInstance(ViewBookings.this);

        if (!userPref.isLoggedIn()) {
            finish();
            startActivity(new Intent(getApplicationContext(), LogIn.class));
            return;
        }



        progressDialog = new ProgressDialog(ViewBookings.this);
        this.userID = String.valueOf(SharedPreferenceManager.getInstance(this).getUserId());

        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.viewBookingsRecyclerView);
        gridLayoutManager = new GridLayoutManager(ViewBookings.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        collapsingToolbarLayout.setTitleEnabled(false);
        heading.setText(userPref.getUserName()+"'s Bookings");

        checkPermission();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.setRecognitionListener(this);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                gridLayoutManager.getOrientation()
        );
        itemDecoration.setDrawable(
                ContextCompat.getDrawable(ViewBookings.this,
                        R.drawable.divider_white)
        );
        recyclerView.addItemDecoration(itemDecoration);

        BottomNavigationViewEx bnve = (BottomNavigationViewEx) findViewById(R.id.bnve);
        bottomNavigationHelper = BottomNavigationHelper.getInstance(ViewBookings.this);
        bottomNavigationHelper.bottonNavSettings(bnve, R.id.bookings);

        bookingDataList = new ArrayList<>();

        getBookingsFromDB();
    }


    private void getBookingsFromDB() {
        progressDialog.setMessage("Retrieving booking details...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.BOOKING_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d(TAG + " onResponse", response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("errorStatus")) {

                                JSONArray recordsJsonArr = jsonObject.getJSONArray("records");
                                for (int i = 0; i < recordsJsonArr.length(); i++) {
                                    JSONObject tmpObj = (JSONObject) recordsJsonArr.get(i);

                                    bookingDataList.add(new BookingData(
                                            tmpObj.getInt("seatNumber"),
                                            tmpObj.getString("date"),
                                            tmpObj.getString("dayOfWeek"),
                                            tmpObj.getString("time"),
                                            tmpObj.getString("from"),
                                            tmpObj.getString("to"),
                                            tmpObj.getString("busID"),
                                            tmpObj.getString("companyName"),
                                            tmpObj.getString("seatCount")

                                    ));
                                }

                                adapter = new BookingAdapter(ViewBookings.this, bookingDataList);
                                recyclerView.setAdapter(adapter);
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
//                        Log.d(TAG + " onErrorResponse", volleyError.getMessage());
//                        Toast.makeText(ViewBookings.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userID_getUserBookings", userID);
                return params;
            }
        };
        RequestHandler.getInstance(ViewBookings.this).addToRequestQueue(stringRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.company_options_menu_toolbar, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        menu.getItem(3).getSubMenu().getItem(1).setVisible(false);
        searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.toLowerCase();
                newList = new ArrayList<>();

                for (BookingData data : bookingDataList) {
                    String companyName = data.getCompanyName().toLowerCase();
                    String fromTo = data.getfromTo().toLowerCase();
                    String seat = data.getSeatNumberOG().toLowerCase();
                    String date = data.getDate().toLowerCase();

                    if (companyName.contains(newText) || fromTo.contains(newText) || seat.contains(newText)  || date.contains(newText)) {
                        newList.add(data);
                    }
                }
                adapter.setFilter(newList);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mic:
                searchView.onActionViewExpanded();
                speechRecognizer.startListening(speechRecognizerIntent);
                break;
            case R.id.logout:
                userPref.logOut();
                if (!userPref.isLoggedIn()){
                    Intent intent = new Intent(getApplicationContext(), LogIn.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            case R.id.sort:
                sortMenu();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void sortMenu() {
        View view = findViewById(R.id.sort);
        Context wrapper = new ContextThemeWrapper(this, R.style.popupTheme);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.getMenuInflater().inflate(R.menu.booking_sort_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.bookingDesc:
                        sort("Desc");
                        break;
                    case R.id.bookingAsc:
                        sort("Asc");
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void sort(final String order) {
        List<BookingData> ref = adapter.getBookingList();
        if (ref.hashCode() == bookingDataList.hashCode()){
            sortImpl(bookingDataList, order);
        } else if(ref.hashCode() == newList.hashCode()){
            sortImpl(newList, order);
        }
    }

    private void sortImpl(ArrayList<BookingData> bookingDataList, final String order) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            bookingDataList.sort(new Comparator<BookingData>() {
                @Override
                public int compare(BookingData o1, BookingData o2) {
                    String nameo2 = o2.getCompanyName();
                    String nameo1 = o1.getCompanyName();
                    if (order.equals("Desc")){
                        Log.d(TAG, " Desc");
                        return nameo2.compareTo(nameo1);
                    }else if (order.equals("Asc")){
                        Log.d(TAG, " Asc");
                        return nameo1.compareTo(nameo2);
                    }
                    return 0;
                }
            });
            adapter.setFilter(bookingDataList);
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        profilePic.setImageDrawable(null);
//    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

    }

    @Override
    public void onResults(Bundle bundle) {
        final ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null) {
            String result = matches.get(0);
            searchView.setQuery(result, true);
            new SpeechHelper().execute(result);
            Log.d(TAG+" myspeech", result);
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    private static class SpeechHelper extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return strings[0];
        }
        @Override
        protected void onPostExecute(String result) {
            searchView.onActionViewCollapsed();
            searchView.setQuery(result, true);
        }
    }

}
