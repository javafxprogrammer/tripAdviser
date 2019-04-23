package com.lengwemushimba.intercityticketbooking;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.lengwemushimba.intercityticketbooking.helper.BottomNavigationHelper;
import com.lengwemushimba.intercityticketbooking.helper.LocaleHelper;
import com.lengwemushimba.intercityticketbooking.services.MyService;
import com.lengwemushimba.intercityticketbooking.adapter.CompanyAdapter;
import com.lengwemushimba.intercityticketbooking.model.CompanyData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Company extends AppCompatActivity {

    private SharedPreferenceManager userPref;
    private static final String TAG = Company.class.getSimpleName();
    private Toolbar toolbar;
    private BottomNavigationHelper bottomNavigationHelper;
    private RecyclerView recyclerView;
    private CompanyAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private ArrayList<CompanyData> companyDataList;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder alertBuilder;
    private BottomNavigationViewEx bnve;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    public static Company company;

    private SearchView searchView;
    private ArrayList<CompanyData> newList;

//    private SharedPreferenceManager userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.company);
        company = this;
        userPref = SharedPreferenceManager.getInstance(Company.this);

        if (!userPref.isLoggedIn()) {
            finish();
            startActivity(new Intent(getApplicationContext(), LogIn.class));
            return;
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkPermission();

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
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
                final ArrayList<String> matches =
                        bundle.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    searchView.setQuery(matches.get(0), true);
                    asyncTask = new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... voids) {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                        @Override
                        protected void onPostExecute(String s) {
//                            searchView.setQuery("", false);
                            searchView.onActionViewCollapsed();
                            searchView.setQuery(matches.get(0), true);
                        }
                    };
                    asyncTask.execute();
                    Log.d(TAG+" myspeech", matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        bnve = (BottomNavigationViewEx) findViewById(R.id.bnve);
        bottomNavigationHelper = BottomNavigationHelper.getInstance(Company.this);
        bottomNavigationHelper.bottonNavSettings(bnve, R.id.home);

        recyclerView = (RecyclerView) findViewById(R.id.companyRecyclerView);
        gridLayoutManager = new GridLayoutManager(Company.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        companyDataList = new ArrayList<>();

        getCompanyRecordsFromDB();
            startMyService();
    }

    private void startMyService(){
        Intent intent = new Intent(Company.this, MyService.class);
        startService(intent);
    }
    private void stopMyService(){
        Intent intent = new Intent(Company.this, MyService.class);
        stopService(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // TODO: 5/18/18 NOT HERE -> check userType and companyID, then inflate the correct menu, all menus must have same search
//       getMenuInflater().inflate();
        getMenuInflater().inflate(R.menu.company_options_menu_toolbar, menu);
        //added
        if(!userPref.getUserType().equalsIgnoreCase("customer")){
            menu.getItem(3).getSubMenu().removeItem(R.id.addCompany);}
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                newText = newText.toLowerCase();
                newList = new ArrayList<>();

                for (CompanyData activity : companyDataList) {
                    String companyName = activity.getCompanyName().toLowerCase();
                    String rating = activity.getCompanyRating().toLowerCase();
                    if (companyName.contains(newText) || rating.contains(newText)) {
                        newList.add(activity);
                    }
                }
                    adapter.setFilter(newList);
                return true;
            }
        });
        return true;
    }

//    private static long t1;
    private AsyncTask<Void, Void, String> asyncTask;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_search:
                break;
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
            case R.id.addCompany:
                isAuthenticated();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortMenu() {
        View view = findViewById(R.id.sort);
        Context wrapper = new ContextThemeWrapper(this, R.style.popupTheme);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.getMenuInflater().inflate(
                R.menu.sort_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.ratingsDesc:
                        sort("Desc");
                        break;
                    case R.id.ratingsAsc:
                        sort("Asc");
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void sort(final String order) {
            List<CompanyData> ref = adapter.getCompanyData();
            if (ref.hashCode() == companyDataList.hashCode()){
                sortImpl(companyDataList, order);
            } else if(ref.hashCode() == newList.hashCode()){
                sortImpl(newList, order);
            }
    }

    private void sortImpl(ArrayList<CompanyData> companyDataList, final String order) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            companyDataList.sort(new Comparator<CompanyData>() {
                @Override
                public int compare(CompanyData o1, CompanyData o2) {
                    Float rating2 = Float.valueOf(o2.getCompanyRating());
                    Float rating1 = Float.valueOf(o1.getCompanyRating());
                    if (order.equals("Desc")){
                        Log.d(TAG, " Desc");
                        if (rating1 > rating2){
                            return -1;
                        }else if (rating1 < rating2){
                            return 1;
                        }

                    }else if (order.equals("Asc")){
                        Log.d(TAG, " Asc");
                        if (rating1 < rating2){
                            return -1;
                        }else if (rating1 > rating2){
                            return 1;
                        }
                    }
                    return 0;
                }
            });
            adapter.setFilter(companyDataList);
        }
    }

    private void isAuthenticated() {

        this.alertBuilder = new AlertDialog.Builder(this, R.style.alertDialogTheme);
        alertBuilder.setTitle("Register Company?");
        alertBuilder.setMessage("Please contact us to get a password");

        final TextInputLayout passwordParent = new TextInputLayout(this);
        final TextInputEditText password = new TextInputEditText(Company.this);

        passwordParent.setHint(getResources().getString(R.string.password));
        passwordParent.setPasswordVisibilityToggleEnabled(true);
        passwordParent.getPasswordVisibilityToggleDrawable().setTint(getResources().getColor(R.color.lightGray));

        passwordParent.addView(password);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        password.setTextColor(getResources().getColor(R.color.primary_text));
        password.setHintTextColor(getResources().getColor(R.color.lightGray));

        FrameLayout container = new FrameLayout(this);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params.leftMargin = getResources().getDimensionPixelOffset(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelOffset(R.dimen.dialog_margin);

        passwordParent.setLayoutParams(params);
        container.addView(passwordParent);

        alertBuilder.setView(container);
        alertBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                authenticateUser(password.getText().toString());
                dialog.dismiss();

            }
        });
        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertBuilder.show();
    }

    private void authenticateUser(final String password) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Authentication...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.COMPANY_AUTH_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        Log.d("CompanyAuth_json_onResponse", "[" + response + "]");

                        try {
                            JSONObject jsonObject = jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("error") == false) {
                                startActivity(new Intent(getApplicationContext(), AddCompany.class));
                            } else {
                                Toast.makeText(Company.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        progressDialog.hide();
//                        Log.d("CompanyAuth_json_onErrorResponse", "[" + volleyError.getMessage() + "]");
                        Toast.makeText(Company.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("password", password);
                return params;
            }

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String>  params = new HashMap<String, String>();
//                params.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240 ");
//                params.put("Cookie", "__test=21172a400f413127151fd92f61ac6f06; expires=Friday, January 1, 2038 at 1:55:55 AM; path=/");
//                return params;
//            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

    }

    private void getCompanyRecordsFromDB() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.COMPANY_URL,
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

                                    companyDataList.add(new CompanyData(
                                            tmpObj.getString("companyID"), tmpObj.getString("image"),
                                            tmpObj.getString("name"), tmpObj.getString("details"),
                                            tmpObj.getString("phone"), tmpObj.getString("email"),
                                            tmpObj.getString("website"), tmpObj.getString("rating"),
                                            tmpObj.getString("ratingStats"), tmpObj.getString("busCount")));
                                }
                                adapter = new CompanyAdapter(Company.this, companyDataList);
                                recyclerView.setAdapter(adapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.hide();
                Toast.makeText(Company.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
//                volleyError.printStackTrace();
            }
        });
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
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
