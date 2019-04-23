package com.lengwemushimba.intercityticketbooking;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.lengwemushimba.intercityticketbooking.adapter.ReviewAdapter;
import com.lengwemushimba.intercityticketbooking.helper.BitmaptHelper;
import com.lengwemushimba.intercityticketbooking.helper.BottomNavigationHelper;
import com.lengwemushimba.intercityticketbooking.model.CompanyData;
import com.lengwemushimba.intercityticketbooking.model.ReviewData;
import com.lengwemushimba.intercityticketbooking.utility.ImageUtil;
import com.squareup.picasso.Picasso;

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
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class ViewReview extends AppCompatActivity implements RecognitionListener {

    @BindView(R.id.reviewRecyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.bnve)
    BottomNavigationViewEx bnve;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private static final String TAG = ViewReview.class.getSimpleName();
    private BottomNavigationHelper bottomNavigationHelper;
    private ProgressDialog progressDialog;
    private String companyID;
    private ReviewAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private ArrayList<ReviewData> reviewList;
    private SharedPreferenceManager userPref;

    private float rating;
    private String details;
    @BindView(R.id.collapsingtoolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.companyName)
    TextView companyName;
    @BindView(R.id.companyImage)
    ImageView companyImage;
    private String companyImageStr;
    private int xDim, yDim;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private static SearchView searchView;
    private ArrayList<ReviewData> newList;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        xDim = companyImage.getWidth();
        yDim = companyImage.getHeight();

        Log.d(TAG+"xy-", "xDim = "+xDim+", yDim = "+yDim);

        if (companyImageStr == null){
            companyImageStr = ImageUtil.getURLForResource(R.drawable.header);
        }

        if (this.companyImage.getDrawable() == null){
            new BitmaptHelper(companyImage).execute(
                    companyImageStr,
                    String.valueOf(xDim),
                    String.valueOf(yDim));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_review);
        ButterKnife.bind(this);

        userPref = SharedPreferenceManager.getInstance(ViewReview.this);

        gridLayoutManager = new GridLayoutManager(ViewReview.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        reviewList = new ArrayList<>();

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
        itemDecoration.setDrawable(ContextCompat.getDrawable(ViewReview.this, R.drawable.divider_white)
        );
        recyclerView.addItemDecoration(itemDecoration);

        bottomNavigationHelper = BottomNavigationHelper.getInstance(ViewReview.this);
        bottomNavigationHelper.bottonNavSettings(bnve, R.id.home);

        this.progressDialog = new ProgressDialog(ViewReview.this);

        if (getIntent().hasExtra("companyID") &&
                getIntent().hasExtra("companyName") &&
                getIntent().hasExtra("companyImage")) {

            Bundle bundle = getIntent().getExtras();
            this.companyID = bundle.getString("companyID");
            this.companyName.setText(bundle.getString("companyName"));
            this.companyImageStr = bundle.getString("companyImage");
            getUserReviews();
        }
    }

    private void getUserReviews() {
        progressDialog.setMessage("Getting user reviews...");
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.REVIEW_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d(TAG + " onResponse", response);

                        try {
                            JSONObject jsObj = new JSONObject(response);

                            if (!jsObj.getBoolean("errorStatus")) {
                                final JSONArray jsonArray = jsObj.getJSONArray("records");

                                for (int i=0; i<jsonArray.length(); i++){
                                    final JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                    reviewList.add(new ReviewData(
                                            jsonObject.getString("userID"),
                                            jsonObject.getString("firstName"),
                                            jsonObject.getString("lastName"),
                                            jsonObject.getString("profilePicture"),
                                            jsonObject.getString("details"),
                                            jsonObject.getString("stars"),
                                            jsonObject.getString("date"),
                                            jsonObject.getString("companyID"),
                                            jsonObject.getString("reviewID")));
                                }

                                adapter = new ReviewAdapter(ViewReview.this, reviewList);
                                recyclerView.setAdapter(adapter);
                                // TODO: 6/17/18 refresh activity
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
                        Toast.makeText(ViewReview.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("companyID_getReview", companyID);
                return params;
            }
        };
        RequestHandler.getInstance(ViewReview.this).addToRequestQueue(stringRequest);
    }

    public void addReview(View v) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(ViewReview.this, R.style.alertDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.addreviewdialog, null);
        RatingBar reviewRatingBar = (RatingBar) view.findViewById(R.id.reviewRatingBar);
        TextInputLayout reviewDetailsParent = (TextInputLayout) view.findViewById(R.id.reviewDetailsParent);
        final TextInputEditText reviewDetailsChild = (TextInputEditText) view.findViewById(R.id.reviewDetailsChild);

        reviewRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ViewReview.this.rating = rating;
                Log.d(TAG+" rating", String.valueOf(rating));
            }
        });


        builder.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ViewReview.this.details = reviewDetailsChild.getText().toString();

                if (!TextUtils.isEmpty(details)) {
                    submitReview();
                    dialog.dismiss();
                } else {
                    Toast.makeText(ViewReview.this, "Please write review, or cancel", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void submitReview() {

        progressDialog.setMessage("Submiting review...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.REVIEW_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d(TAG + " onResponse", response);

                        try {
                            JSONObject jsObj = new JSONObject(response);
                            if (!jsObj.getBoolean("error")) {
                                Toast.makeText(ViewReview.this, jsObj.getString("message"), Toast.LENGTH_SHORT).show();
                                // TODO: 6/17/18 intent
                                finish();
                                Intent intent = new Intent(ViewReview.this, ViewReview.class);
                                intent.putExtra("companyID", StaticVar.map.get("companyID"));
                                intent.putExtra("companyName", StaticVar.map.get("companyName"));
                                intent.putExtra("companyImage", StaticVar.map.get("companyImage"));
//                                reviewList.add(new ReviewData());
                                startActivity(intent);

                            } else {
                                Toast.makeText(ViewReview.this, jsObj.getString("message"), Toast.LENGTH_SHORT).show();
                                Log.e(TAG+" ->", jsObj.getString("message"));
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
                        Toast.makeText(ViewReview.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("companyID_addRating", companyID);
                params.put("rating_addRating", String.valueOf(rating));
                params.put("details_addRating", details);
                params.put("userID_addRating", String.valueOf(userPref.getUserId()));
                return params;
            }
        };
        RequestHandler.getInstance(ViewReview.this).addToRequestQueue(stringRequest);
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

                for (ReviewData review : reviewList) {
                    String companyName = review.getUserName().toLowerCase();
                    String rating = review.getRating().toLowerCase();
                    String comment = review.getDetails().toLowerCase();

                    if (companyName.contains(newText) || rating.contains(newText) || comment.contains(newText)) {
                        newList.add(review);
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
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.sort_menu, popupMenu.getMenu());
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
        List<ReviewData> ref = adapter.getReviewList();
        if (ref.hashCode() == reviewList.hashCode()){
            sortImpl(reviewList, order);
        } else if(ref.hashCode() == newList.hashCode()){
            sortImpl(newList, order);
        }
    }

    private void sortImpl(ArrayList<ReviewData> reviewList, final String order) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            reviewList.sort(new Comparator<ReviewData>() {
                @Override
                public int compare(ReviewData o1, ReviewData o2) {
                    Float rating2 = Float.valueOf(o2.getRating());
                    Float rating1 = Float.valueOf(o1.getRating());
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
            adapter.setFilter(reviewList);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        companyImage.setImageDrawable(null);
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
        Toast.makeText(this, "Recognition failed: error code "+error, Toast.LENGTH_SHORT).show();
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

    private static class SpeechHelper extends AsyncTask<String, Void, String>{

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
