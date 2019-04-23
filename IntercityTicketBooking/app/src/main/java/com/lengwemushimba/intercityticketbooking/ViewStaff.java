package com.lengwemushimba.intercityticketbooking;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
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
import com.lengwemushimba.intercityticketbooking.adapter.ReviewAdapter;
import com.lengwemushimba.intercityticketbooking.adapter.StaffAdapter;
import com.lengwemushimba.intercityticketbooking.helper.BitmaptHelper;
import com.lengwemushimba.intercityticketbooking.helper.BottomNavigationHelper;
import com.lengwemushimba.intercityticketbooking.model.BusData;
import com.lengwemushimba.intercityticketbooking.model.ReviewData;
import com.lengwemushimba.intercityticketbooking.model.StaffData;

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

public class ViewStaff extends AppCompatActivity implements RecognitionListener {

    private static final String TAG = ViewStaff.class.getSimpleName();

    private String companyID;
    private ProgressDialog progressDialog;
    private StaffAdapter adapter;
    private ArrayList<StaffData> staffList;
    private GridLayoutManager gridLayoutManager;

    @BindView(R.id.addStaffFab)
    FloatingActionButton addStaffFab;
    @BindView(R.id.staffRecyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.bnve)
    BottomNavigationViewEx bnve;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsingtoolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.companyImage)
    ImageView companyImageView;
    @BindView(R.id.companyName)
    TextView companyName;
    private int xDim, yDim;
    private BottomNavigationHelper bottomNavigationHelper;
    private SharedPreferenceManager userPref;
    private String companyImage;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private static SearchView searchView;
    private ArrayList<StaffData> newList;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        xDim = companyImageView.getWidth();
        yDim = companyImageView.getHeight();

        Log.d(TAG + "xy-", "xDim = " + xDim + ", yDim = " + yDim);
        if (companyImage == null) {
            // TODO: 7/28/18
        }

        if (this.companyImageView.getDrawable() == null) {
            new BitmaptHelper(companyImageView).execute(companyImage,
                    String.valueOf(xDim),
                    String.valueOf(yDim));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_staff);
        ButterKnife.bind(this);

        this.userPref = SharedPreferenceManager.getInstance(ViewStaff.this);

        setSupportActionBar(toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            this.companyID = bundle.getString("companyID");
            String companyName = bundle.getString("companyName");
            this.companyImage = bundle.getString("companyImage");
            this.companyName.setText(companyName);
        }

        if (!userPref.getUserType().equalsIgnoreCase("management")
                && !String.valueOf(userPref.getUserId()).equals(companyID)){
            addStaffFab.setEnabled(false);
            addStaffFab.setVisibility(View.INVISIBLE);
        }

        progressDialog = new ProgressDialog(ViewStaff.this);

        staffList = new ArrayList<>();
        gridLayoutManager = new GridLayoutManager(ViewStaff.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

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
                ContextCompat.getDrawable(ViewStaff.this,
                        R.drawable.divider_white)
        );
        recyclerView.addItemDecoration(itemDecoration);

        bottomNavigationHelper = BottomNavigationHelper.getInstance(ViewStaff.this);
        bottomNavigationHelper.bottonNavSettings(bnve, R.id.home);

        getSatffFromServer();

    }

    private void getSatffFromServer() {
        progressDialog.setMessage("Getting Staff...");
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.STAFF_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String request) {
                        progressDialog.dismiss();
                        Log.d(TAG + " onResponse", request);
                        try {
                            JSONObject jsObj = new JSONObject(request);
                            if (!jsObj.getBoolean("errorStatus")) {
                                JSONArray jsArr = jsObj.getJSONArray("records");
                                for (int i = 0; i < jsArr.length(); i++) {
                                    JSONObject o = jsArr.getJSONObject(i);
                                    Log.d(TAG + " staffid = ", o.getString("userID"));
                                    staffList.add(new StaffData(
                                            o.getString("userID"),
                                            o.getString("firstName"),
                                            o.getString("lastName"),
                                            o.getString("userType"),
                                            o.getString("email"),
                                            o.getString("phone"),
                                            o.getString("profilePicture"),
                                            o.getString("companyID")));
                                }
                            } else {
                                Toast.makeText(ViewStaff.this, "No staff available", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adapter = new StaffAdapter(staffList, ViewStaff.this);
                        recyclerView.setAdapter(adapter);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
                Log.d(TAG + " onErrorResponse", error.getMessage());


            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("companyID_get", companyID);
                return params;
            }
        };
        RequestHandler.getInstance(ViewStaff.this).addToRequestQueue(stringRequest);
    }

    public void addStaff(View view) {
        Intent intent = new Intent(ViewStaff.this, AddStaff.class);
        intent.putExtra("companyID", companyID);
        startActivity(intent);
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

                for (StaffData data : staffList) {
                    String name = data.getName().toLowerCase();
                    String job = data.getJob().toLowerCase();
                    String phone = data.getPhone().toLowerCase();
                    String email = data.getEmail().toLowerCase();

                    if (name.contains(newText) || job.contains(newText) || phone.contains(newText) || email.contains(newText)) {
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
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.staff_sort_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.jobDesc:
                        sort("Desc");
                        break;
                    case R.id.jobAsc:
                        sort("Asc");
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void sort(final String order) {
        List<StaffData> ref = adapter.getStaffList();
        if (ref.hashCode() == staffList.hashCode()) {
            sortImpl(staffList, order);
        } else if (ref.hashCode() == newList.hashCode()) {
            sortImpl(newList, order);
        }
    }

    private void sortImpl(ArrayList<StaffData> reviewList, final String order) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            reviewList.sort(new Comparator<StaffData>() {
                @Override
                public int compare(StaffData o1, StaffData o2) {
                    String jobo2 = o2.getJob();
                    String jobo1 = o1.getJob();
                    if (order.equals("Desc")) {
                        Log.d(TAG, " Desc");
                        return jobo2.compareTo(jobo1);
                    } else if (order.equals("Asc")) {
                        Log.d(TAG, " Asc");
                        return jobo1.compareTo(jobo2);
                    }
                    return 0;
                }
            });
            adapter.setFilter(reviewList);
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
            Log.d(TAG + " myspeech", result);
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
}
