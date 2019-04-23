package com.lengwemushimba.intercityticketbooking;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
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
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.lengwemushimba.intercityticketbooking.adapter.AllBusesAdapter;
import com.lengwemushimba.intercityticketbooking.helper.BitmaptHelper;
import com.lengwemushimba.intercityticketbooking.helper.BottomNavigationHelper;
import com.lengwemushimba.intercityticketbooking.helper.ImageHttpHelper;
import com.lengwemushimba.intercityticketbooking.model.BusData;
import com.lengwemushimba.intercityticketbooking.model.Logistics;
import com.lengwemushimba.intercityticketbooking.utility.ImageUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AllBuses extends AppCompatActivity implements RecognitionListener {

    public static final String TAG = AllBuses.class.getSimpleName();
    private ProgressDialog progressDialog;
    private AllBusesAdapter adapter;
    private ArrayList<BusData> busesList;
    private GridLayoutManager gridLayoutManager;
    //    private String companyID;
    @BindView(R.id.buseRecyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.bnve)
    BottomNavigationViewEx bnve;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private BottomNavigationHelper bottomNavigationHelper;
    private SharedPreferenceManager userPref;
    @BindView(R.id.collapsingtoolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.allBusesImage)
    ImageView allBusesImage;
    private int xDim, yDim;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private static SearchView searchView;
    private ArrayList<BusData> newList;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        xDim = allBusesImage.getWidth();
        yDim = allBusesImage.getHeight();

        Log.d(TAG + "xy-", "xDim = " + xDim + ", yDim = " + yDim);

        if (this.allBusesImage.getDrawable() == null) {
            new MyBitmaptHelper().execute(xDim, yDim);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_buses);
        ButterKnife.bind(this);

        this.userPref = SharedPreferenceManager.getInstance(AllBuses.this);

        gridLayoutManager = new GridLayoutManager(AllBuses.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        setSupportActionBar(toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);

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
                ContextCompat.getDrawable(AllBuses.this,
                        R.drawable.divider_white)
        );
        recyclerView.addItemDecoration(itemDecoration);
//        collapsingToolbarLayout.setTitle("Lusaka Inter-City Bus Terminus");

        progressDialog = new ProgressDialog(AllBuses.this);
        busesList = new ArrayList<>();


        bottomNavigationHelper = BottomNavigationHelper.getInstance(AllBuses.this);
        bottomNavigationHelper.bottonNavSettings(bnve, R.id.buses);

//        if (getIntent().hasExtra("companyID")) {
//            this.companyID = getIntent().getExtras().getString("companyID");
//        }

        getAllBuses();
    }

    private void getAllBuses() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.BUS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d(TAG + " onResponse", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (jsonObject.getBoolean("errorStatus") == false) {

                                int busID = 0;
                                String name = "";
                                int seats = 0;
                                String desc = "";
                                String companyName = "";

                                ArrayList<ArrayList<Logistics>> logisticsList = new ArrayList<>();
                                int j = 0;

                                String tmpBUSID = "";
                                boolean tmp = false;

                                JSONArray recordsJsonArr = jsonObject.getJSONArray("records");

                                for (int k = 0; k < recordsJsonArr.length(); k++) {
                                    logisticsList.add(new ArrayList<Logistics>());
                                }

                                Log.d("leng__", String.valueOf(recordsJsonArr.length()));
                                for (int i = 0; i < recordsJsonArr.length(); i++) {

                                    JSONObject tmpObj = (JSONObject) recordsJsonArr.get(i);
                                    final String BUSID = tmpObj.getString("busID");
                                    Log.d("i_count", String.valueOf(i));

                                    if (!BUSID.equals(tmpBUSID)) {
                                        if (tmp == true) {
                                            Log.d(TAG + " added", "busID = " + String.valueOf(busID) + ", name = " + name);
                                            busesList.add(new BusData(companyName, busID, name, desc, seats, logisticsList.get(j)));
                                            j++;
                                        }
                                        tmp = true;
                                    }

                                    tmpBUSID = BUSID;

                                    companyName = tmpObj.getString("companyName");
                                    busID = tmpObj.getInt("busID");
                                    name = tmpObj.getString("name");
                                    seats = tmpObj.getInt("seats");
                                    desc = tmpObj.getString("description");

                                    logisticsList.get(j).add(
                                            new Logistics(tmpObj.getString("from"),
                                                    tmpObj.getString("to"),
                                                    tmpObj.getDouble("amount"),
                                                    tmpObj.getString("day"),
                                                    tmpObj.getString("time")));

                                    if (i == (recordsJsonArr.length() - 1)) {
                                        Log.d(TAG + " added", "busID = " + String.valueOf(busID) + ", name = " + name);
                                        busesList.add(new BusData(companyName, busID, name, desc, seats, logisticsList.get(j)));
                                    }

                                }
                                adapter = new AllBusesAdapter(getApplicationContext(), busesList);
                                recyclerView.setAdapter(adapter);
                            } else {
                                Log.d(TAG + "", jsonObject.getString("message"));
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
                        Log.d(TAG + " onErrorResponse", volleyError.getMessage());
                        Toast.makeText(AllBuses.this, (volleyError.getMessage()), Toast.LENGTH_SHORT).show();

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
//                params.put("companyID_getAllbuses", "companyID_getAllbuses");
                params.put("userID_getAllbuses", String.valueOf(userPref.getUserId()));
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // TODO: 5/18/18 NOT HERE -> check userType and companyID, then inflate the correct menu, all menus must have same search
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
                newList.clear();
                Set<BusData> busDataSet = new HashSet<>();

                for (BusData bd : busesList) {
                    String seats = String.valueOf(bd.getSeats());
                    String busName = bd.getName();
                    String desc = bd.getDesc();
                    String company_Name = bd.getCompanyName();

                    ArrayList<Logistics> logiList = bd.getLogisticsArrayList();
                    for (Logistics l : logiList) {
                        String to = l.getTo().toLowerCase();
                        if (to.contains(newText)) {
                            busDataSet.add(bd);
                            break;
                        }
                    }

                    if (seats.contains(newText) ||
                            busName.toLowerCase().contains(newText) ||
                            desc.toLowerCase().contains(newText) ||
                            company_Name.toLowerCase().contains(newText)) {
                        busDataSet.add(bd);
                    }
                }
                newList.addAll(busDataSet);
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
                if (!userPref.isLoggedIn()) {
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
        popupMenu.getMenuInflater().inflate(R.menu.bus_sort_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.seatsDesc:
                        sort("Desc");
                        break;
                    case R.id.seatsAsc:
                        sort("Asc");
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void sort(final String order) {
        List<BusData> ref = adapter.getAllBusList();
        if (ref.hashCode() == busesList.hashCode()) {
            sortImpl(busesList, order);
        } else if (ref.hashCode() == newList.hashCode()) {
            sortImpl(newList, order);
        }
    }

    private void sortImpl(ArrayList<BusData> data, final String order) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data.sort(new Comparator<BusData>() {
                @Override
                public int compare(BusData o1, BusData o2) {
                    int o2Seats = o2.getSeats();
                    int o1Seats = o1.getSeats();
                    if (order.equals("Desc")) {
                        Log.d(TAG, " Desc");
                        if (o1Seats > o2Seats) {
                            return -1;
                        } else if (o1Seats < o2Seats) {
                            return 1;
                        }

                    } else if (order.equals("Asc")) {
                        Log.d(TAG, " Asc");
                        if (o1Seats < o2Seats) {
                            return -1;
                        } else if (o1Seats > o2Seats) {
                            return 1;
                        }
                    }
                    return 0;
                }
            });
            adapter.setFilter(data);
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
    public void onResults(Bundle results) {

        final ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null) {
            String result = matches.get(0);
            searchView.setQuery(result, true);
            new SpeechHelper().execute(result);
            Log.d(TAG + " myspeech", result);
        }

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

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

    public class MyBitmaptHelper extends AsyncTask<Integer, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Integer... integers) {
            return ImageHttpHelper.decodeSampledBitmapFromResource(getResources(), R.drawable.intercity, integers[0], integers[1]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            allBusesImage.setImageBitmap(bitmap);
        }
    }
}
