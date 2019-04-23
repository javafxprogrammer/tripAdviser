package com.lengwemushimba.intercityticketbooking.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.lengwemushimba.intercityticketbooking.Constants;
import com.lengwemushimba.intercityticketbooking.R;
import com.lengwemushimba.intercityticketbooking.RequestHandler;
import com.lengwemushimba.intercityticketbooking.SharedPreferenceManager;
import com.lengwemushimba.intercityticketbooking.StaticVar;
import com.lengwemushimba.intercityticketbooking.ViewReview;
import com.lengwemushimba.intercityticketbooking.helper.BitmaptHelper;
import com.lengwemushimba.intercityticketbooking.model.ReviewData;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

/**
 * Created by lengwe on 6/17/18.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {

    private static final String TAG = ReviewAdapter.class.getSimpleName();
    private Context context;
    private List<ReviewData> reviewList;
    private ProgressDialog progressDialog;
    private SharedPreferenceManager userPref;

    public ReviewAdapter(Context context, List<ReviewData> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
        this.progressDialog = new ProgressDialog(context);
        userPref = SharedPreferenceManager.getInstance(context);
    }

    private void styleRatingBar(View view) {
        int starColor = context.getResources().getColor(R.color.duskYellow);
        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.reviewRatingBar);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(starColor, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(starColor, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(starColor, PorterDuff.Mode.SRC_ATOP);
    }

    @NonNull
    @Override
    public ReviewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_review_cardview, parent, false);
        styleRatingBar(view);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ReviewAdapter.MyViewHolder holder, final int position) {
        final ReviewData reviewData = reviewList.get(position);

        new BitmaptHelper(holder.profile_image).execute(
                reviewData.getProfilePicture(),
                String.valueOf(80),
                String.valueOf(80)
                );
        final int userID = reviewData.getUserID();
        holder.nameAndDate.setText(reviewData.getNameAndDate());
        holder.reviewRatingBar.setRating(Float.valueOf(reviewData.getRating()));
        holder.reviewRatingStats.setText(reviewData.getRating());
        holder.reviewDetails.setText(reviewData.getDetails());

        final String companyID = reviewData.getCompanyID();
        final String reviewID = reviewData.getReviewID();

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context wrapper = new ContextThemeWrapper(context, R.style.popupTheme);
                PopupMenu popupMenu = new PopupMenu(wrapper, holder.more);
                popupMenu.getMenuInflater().inflate(R.menu.review_option_menu, popupMenu.getMenu());

                if (userPref.getUserId() != userID){
                    popupMenu.getMenu().removeItem(R.id.update);
                    popupMenu.getMenu().removeItem(R.id.delete);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.update:
                                updateReview(companyID, reviewID);
                                break;
                            case R.id.delete:
                                alert(companyID, reviewID, position);
                                break;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView profile_image;
        TextView nameAndDate;
        RatingBar reviewRatingBar;
        TextView reviewRatingStats;
        TextView reviewDetails;
        ImageView more;

        public MyViewHolder(View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.profile_image);
            nameAndDate = itemView.findViewById(R.id.nameAndDate);
            reviewRatingBar = itemView.findViewById(R.id.reviewRatingBar);
            reviewRatingStats = itemView.findViewById(R.id.reviewRatingStats);
            reviewDetails = itemView.findViewById(R.id.reviewDetails);
            more = itemView.findViewById(R.id.moreVert);

        }
    }

    private float rating;
    private String details;

    private void updateReview(final String companyID, final String reviewID) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.addreviewdialog, null);
        RatingBar reviewRatingBar = (RatingBar) view.findViewById(R.id.reviewRatingBar);
        TextInputLayout reviewDetailsParent = (TextInputLayout) view.findViewById(R.id.reviewDetailsParent);
        final TextInputEditText reviewDetailsChild = (TextInputEditText) view.findViewById(R.id.reviewDetailsChild);

        reviewRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ReviewAdapter.this.rating = rating;
                Log.d(TAG+" rating", String.valueOf(rating));
            }
        });


        builder.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ReviewAdapter.this.details = reviewDetailsChild.getText().toString();

                if (!TextUtils.isEmpty(details)) {
                    submitReview(companyID, reviewID);
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Please write review, or cancel", Toast.LENGTH_SHORT).show();
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

    private void submitReview(final String companyID, final String reviewID) {

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
                                Toast.makeText(context, jsObj.getString("message"), Toast.LENGTH_SHORT).show();
                                // TODO: 6/17/18 intent
//                                finish();
                                Intent intent = new Intent(context, ViewReview.class);
//                                intent.putExtra("companyID", companyID);
                                intent.putExtra("companyID", StaticVar.map.get("companyID"));
                                intent.putExtra("companyName", StaticVar.map.get("companyName"));
                                intent.putExtra("companyImage", StaticVar.map.get("companyImage"));
                                context.startActivity(intent);

                            } else {
                                Toast.makeText(context, jsObj.getString("message"), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("reviewID_editReview", reviewID);
                params.put("rating_editReview", String.valueOf(rating));
                params.put("details_editReview", details);
                return params;
            }
        };
        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);
    }

    private static AlertDialog.Builder alertBuilder;
    private void alert(final String companyID, final String reviewID, final int position){
        alertBuilder = new AlertDialog.Builder(context, R.style.alertDialogTheme);
        alertBuilder.setTitle("Delete Review?");
        alertBuilder.setMessage("Your review will be permanently deleted");

        alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteReview(companyID, reviewID, position);
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

    private void deleteReview(final String companyID, final String reviewID, final int position) {

        progressDialog.setMessage("Deleting review...");
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
                                Toast.makeText(context, jsObj.getString("message"), Toast.LENGTH_SHORT).show();
                                // TODO: 6/17/18 intent
//                                finish();
                                reviewList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, reviewList.size()-position);


                            } else {
                                Toast.makeText(context, jsObj.getString("message"), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("reviewID_deleteReview", reviewID);
                return params;
            }
        };
        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);

    }

    public void setFilter(ArrayList<ReviewData> newList) {
        this.reviewList = new ArrayList<>();
        this.reviewList.addAll(newList);
        notifyDataSetChanged();
    }

    public List<ReviewData> getReviewList(){
        return this.reviewList;
    }

}
