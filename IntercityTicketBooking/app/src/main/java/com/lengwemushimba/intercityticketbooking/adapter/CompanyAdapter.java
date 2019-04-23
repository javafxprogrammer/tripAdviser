package com.lengwemushimba.intercityticketbooking.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
//import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.lengwemushimba.intercityticketbooking.Company;
import com.lengwemushimba.intercityticketbooking.CompanyBus;
import com.lengwemushimba.intercityticketbooking.Constants;
import com.lengwemushimba.intercityticketbooking.EditCompany;
import com.lengwemushimba.intercityticketbooking.LogIn;
import com.lengwemushimba.intercityticketbooking.R;
import com.lengwemushimba.intercityticketbooking.RequestHandler;
import com.lengwemushimba.intercityticketbooking.SharedPreferenceManager;
import com.lengwemushimba.intercityticketbooking.StaticVar;
import com.lengwemushimba.intercityticketbooking.ViewReview;
import com.lengwemushimba.intercityticketbooking.ViewStaff;
import com.lengwemushimba.intercityticketbooking.helper.BitmaptHelper;
import com.lengwemushimba.intercityticketbooking.model.CompanyData;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lengwe on 5/19/18.
 */

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.MyViewHolder> {


    private Context context;
    private List<CompanyData> companyData;
//    private String userType, userCompanyId;
    private ProgressDialog progressDialog;
    private static String TAG = CompanyAdapter.class.getSimpleName();
    private SharedPreferenceManager userPref;


    public CompanyAdapter(Context context, ArrayList<CompanyData> companyData) {
        this.context = context;
        this.companyData = companyData;
        userPref = SharedPreferenceManager.getInstance(this.context);
//        this.userType = userPref.getUserType();
//        this.userCompanyId = userPref.getUserCompanyId();
        progressDialog = new ProgressDialog(context);
    }


    private void styleRatingBar(View view) {
        int starColor = context.getResources().getColor(R.color.duskYellow);
        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.comapanyRatingBar);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(starColor, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(starColor, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(starColor, PorterDuff.Mode.SRC_ATOP);
    }

//    {
//
//    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.company_cardview, parent, false);
        styleRatingBar(view);
        return new MyViewHolder(view);
    }

    boolean bool = false;

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final CompanyData companyData = this.companyData.get(position);
        final String compantID = companyData.getCompanyID();
        final String companyName = companyData.getCompanyName();
        final String companyImage = companyData.getCompanyImage();
        final String companyRating = companyData.getCompanyRating();
        final String companyDetails = companyData.getCompanyDetails();

//        if (companyData.ge)
        holder.companyName.setText(companyData.getCompanyName());
        // TODO: 7/28/18 compressimage
        Picasso.get()
                .load(companyData.getCompanyImage())
                .into(holder.companyImage);
//        new BitmaptHelper(holder.companyImage).execute(
//                companyData.getCompanyImage(),
//                String.valueOf(400),
//                String.valueOf(400)
//        );

        holder.companyRating.setRating(Float.valueOf(companyData.getCompanyRating()));
        holder.companyRatingStats.setText(companyData.getCompanyRatingStats()); // TODO: 5/19/18 fix php
        holder.companyBusCount.setText(companyData.getCompanyBusCount() + " Available Buses"); // TODO: 5/19/18 fix php
        holder.companyDetails.setText(companyData.getCompanyDetails());

        holder.companyReviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticVar.map = new HashMap<>();
                StaticVar.map.put("companyID", compantID);
                StaticVar.map.put("companyName", companyName);
                StaticVar.map.put("companyImage", companyImage);
                Intent intent = new Intent(context, ViewReview.class);
                Pair<View, String> pair1 = Pair.create(holder.view.findViewById(R.id.comapanyImage), "sharedImage");
                Pair<View, String> pair2 = Pair.create(holder.view.findViewById(R.id.comapanyName), "sharedText");
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(Company.company, pair1, pair2);
                intent.putExtra("companyID", compantID);
                intent.putExtra("companyName", companyName);
                intent.putExtra("companyImage", companyImage);
                context.startActivity(intent, optionsCompat.toBundle());
            }
        });

        holder.companyBusesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticVar.map = new HashMap<>();
                StaticVar.map.put("companyID", compantID);
                StaticVar.map.put("companyName", companyName);
                StaticVar.map.put("companyImage", companyImage);
                Intent intent = new Intent(context, CompanyBus.class);
                Pair<View, String> pair1 = Pair.create(holder.view.findViewById(R.id.comapanyImage), "sharedImage");
                Pair<View, String> pair2 = Pair.create(holder.view.findViewById(R.id.comapanyName), "sharedText");
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(Company.company, pair1, pair2);
                intent.putExtra("companyID", compantID);
                intent.putExtra("companyName", companyName);
                intent.putExtra("companyImage", companyImage);
                context.startActivity(intent, optionsCompat.toBundle());
            }
        });


        holder.companyMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context wrapper = new ContextThemeWrapper(context, R.style.popupTheme);
                PopupMenu popupMenu = new PopupMenu(wrapper, holder.companyMore);
                popupMenu.getMenuInflater().inflate(R.menu.company_options_menu, popupMenu.getMenu()); // TODO: 5/19/18 addonclick

                if (!userPref.getUserType().equals("management") || !userPref.getUserCompanyId().equals(compantID)) {
                    popupMenu.getMenu().removeItem(R.id.delete);
                    popupMenu.getMenu().removeItem(R.id.edit);
                }

                if (!userPref.getUserCompanyId().equals(compantID)) {
                    popupMenu.getMenu().removeItem(R.id.addPromo);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.edit:
                                Intent intent = new Intent(context, EditCompany.class);
                                intent.putExtra("CompanyID", compantID);
                                context.startActivity(intent);
                                break;
                            case R.id.delete:
                                alert(compantID, position);
                                break;
                            case R.id.share:
                                share(companyName, companyDetails, companyRating);
                                break;
                            case R.id.addPromo:
                                addPromo(compantID);
                                break;
                            case R.id.viewStaff:
                                StaticVar.map = new HashMap<>();
                                StaticVar.map.put("companyID", compantID);
                                StaticVar.map.put("companyName", companyName);
                                StaticVar.map.put("companyImage", companyImage);
                                Intent viewStaffIntent = new Intent(context, ViewStaff.class);
                                viewStaffIntent.putExtra("companyID", compantID);
                                viewStaffIntent.putExtra("companyName", companyName);
                                viewStaffIntent.putExtra("companyImage", companyImage);
                                context.startActivity(viewStaffIntent);
                                break;

                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        if (!bool) {
            bool = true;
            removeCompany();
        }
    }

    private void removeCompany() {

        final View recyclerView = ((Activity) context).findViewById(R.id.companyRecyclerView);
        recyclerView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        for (int i = 0; i < companyData.size(); i++) {

                            if (companyData.get(i).getCompanyBusCount().equals("0")
                                    && !userPref.getUserCompanyId().equals(companyData.get(i).getCompanyID())) {
                                Log.d(TAG+ "removeed", companyData.get(i).getCompanyBusCount()+" ___ "+userPref.getUserCompanyId()+" __ "+companyData.get(i).getCompanyID());

                                CompanyAdapter.this.companyData.remove(i);
                                notifyItemRemoved(i);
                                notifyItemRangeChanged(i,
                                        CompanyAdapter.this.companyData.size() - i);

                            }
                        }
                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });


    }

    private void share(String companyName, String companyDetails,
                       String companyRating) {

        String shareBody = "Zambia's best Trip Adviser app available " +
                "now at\n" +
                "www.playstore.com/tripadiser\n" +
                "Get best deals\n" +
                companyName + "\n" +
                companyDetails + "\n" +
                "Rating: " + companyRating;
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setTypeAndNormalize("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "TRIP ADVISOR");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                shareBody);
        context.startActivity(Intent.createChooser(sharingIntent,
                "share using"));
    }

    private void addPromo(final String compantID) {
        View view = LayoutInflater.from(context).inflate(R.layout.promotion, null);

        final TextInputLayout detailsParent = view.findViewById(R.id.detailsParent);
        final TextInputLayout ticketCountParent = view.findViewById(R.id.ticketCountParent);
        final TextInputLayout discountParent = view.findViewById(R.id.discountParent);

        final TextInputEditText detailsChild = view.findViewById(R.id.detailsChild);
        final TextInputEditText ticketCountChild = view.findViewById(R.id.ticketCountChild);
        final TextInputEditText discountChild = view.findViewById(R.id.discountChild);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.alertDialogTheme);

        builder.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String details = detailsChild.getText().toString();
                final String tickeCount = ticketCountChild.getText().toString();
                final String discount = discountChild.getText().toString();

                if (TextUtils.isEmpty(details)) {
                    detailsParent.setErrorEnabled(true);
                    detailsParent.setError("Please enter promotion description");
                } else {
                    detailsParent.setErrorEnabled(false);
                }
                if (TextUtils.isEmpty(tickeCount)) {
                    ticketCountParent.setErrorEnabled(true);
                    ticketCountParent.setError("Please enter ticket count");
                } else {
                    ticketCountParent.setErrorEnabled(false);
                }
                if (TextUtils.isEmpty(discount)) {
                    discountParent.setErrorEnabled(true);
                    discountParent.setError("Please enter discount");
                } else {
                    discountParent.setErrorEnabled(false);
                }

                if (!TextUtils.isEmpty(details) && !TextUtils.isEmpty(tickeCount) && !TextUtils.isEmpty(discount)) {
                    submitPromo(compantID, details, tickeCount, discount, dialog);
                } else {
                    Log.d("madrid", details + ", " + tickeCount + ", " + discount);
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

    private void submitPromo(final String companyID, final String details, final String tickeCount, final String discount, final DialogInterface dialog) {

        Log.d("Called", companyID + ", " + details + ", " + tickeCount + ", " + discount);
        progressDialog.setMessage("Submiting promotion details...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.PROMOTION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d(TAG + " onResponseLL", response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Log.d(TAG + "message_", jsonObject.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
                Log.d(TAG + " onErrorResponse", error.getMessage());
                dialog.cancel();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("companyID", companyID);
                params.put("details", details);
                params.put("tickeCount", tickeCount);
                params.put("discount", discount);
                return params;
            }
        };
        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);
    }


    @Override
    public int getItemCount() {
        return companyData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView companyImage;
        public TextView companyName;
        public RatingBar companyRating;
        public TextView companyRatingStats;
        public TextView companyBusCount;
        public TextView companyDetails;
        public AppCompatButton companyReviewsBtn;
        public AppCompatButton companyBusesBtn;
        public ImageView companyMore;
        public View view;

        public MyViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            companyImage = (ImageView) itemView.findViewById(R.id.comapanyImage);
            companyName = (TextView) itemView.findViewById(R.id.comapanyName);
            companyRatingStats = (TextView) itemView.findViewById(R.id.companyRatingStats);
            companyRating = (RatingBar) itemView.findViewById(R.id.comapanyRatingBar);
            companyBusCount = (TextView) itemView.findViewById(R.id.companyBusCount);
            companyDetails = (TextView) itemView.findViewById(R.id.companyDetails);
            companyReviewsBtn = (AppCompatButton) itemView.findViewById(R.id.companyReview_btn);
            companyBusesBtn = (AppCompatButton) itemView.findViewById(R.id.companyBus_btn);
            companyMore = (ImageView) itemView.findViewById(R.id.companyMoreIcon);
        }
    }

    private static AlertDialog.Builder alertBuilder;
    private void alert(final String compantID, final int position){
        alertBuilder = new AlertDialog.Builder(context, R.style.alertDialogTheme);
        alertBuilder.setTitle("Unregister Company?");
        alertBuilder.setMessage("You will lose all your company details");

        alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCompany(compantID, position);
                dialog.dismiss();

            }
        });
        alertBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertBuilder.show();
    }

    private void deleteCompany(final String compantID, final int position) {

        progressDialog.setMessage("Deleting company details...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.COMPANY_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        //
                        Log.d(TAG + " onResponse", response);
                        // TODO: 9/16/18   check if query was successful before deleting
                        companyData.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, companyData.size() - position);
                        userPref.logOut();
                        Intent intent = new Intent(context, LogIn.class);
                        context.startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.hide();
                        Log.d(TAG + " onErrorResponse", volleyError.getMessage());
                        Toast.makeText(context, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("compID", compantID);
                return params;
            }
        };

        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);

    }

    public void setFilter(ArrayList<CompanyData> newList) {
        this.companyData = new ArrayList<>();
        companyData.addAll(newList);
        notifyDataSetChanged();
    }

    public List<CompanyData> getCompanyData() {
        return this.companyData;
    }

}
