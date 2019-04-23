package com.lengwemushimba.intercityticketbooking.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lengwemushimba.intercityticketbooking.Constants;
import com.lengwemushimba.intercityticketbooking.EditStaff;
import com.lengwemushimba.intercityticketbooking.R;
import com.lengwemushimba.intercityticketbooking.RequestHandler;
import com.lengwemushimba.intercityticketbooking.SharedPreferenceManager;
import com.lengwemushimba.intercityticketbooking.helper.BitmaptHelper;
import com.lengwemushimba.intercityticketbooking.model.StaffData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by lengwe on 7/26/18.
 */

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.MyViewHolder> {

    private static final String TAG = StaffAdapter.class.getSimpleName();
    private ProgressDialog progressDialog;
    private List<StaffData> staffList;
    private Context context;
    SharedPreferenceManager userPrefs;

    public StaffAdapter(List<StaffData> staffList, Context context) {
        this.staffList = staffList;
        this.context = context;
        this.progressDialog = new ProgressDialog(context);
        this.userPrefs = SharedPreferenceManager.getInstance(this.context);

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StaffAdapter.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_staff_cardview, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        StaffData staffData = staffList.get(position);
        final String staffId = staffData.getId();

        final String companyID = staffData.getCompanyID();
        holder.name.setText(staffData.getName());
        holder.job.setText(staffData.getJob());
        holder.email.setText(staffData.getEmail());
        holder.phone.setText(staffData.getPhone());
        new BitmaptHelper(holder.image).execute(
                staffData.getPicture(),
                String.valueOf(80),
                String.valueOf(80)
        );

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context wrapper = new ContextThemeWrapper(context, R.style.popupTheme);
                PopupMenu popupMenu = new PopupMenu(wrapper, holder.more);
                popupMenu.getMenuInflater().inflate(R.menu.review_option_menu, popupMenu.getMenu());

                if (!userPrefs.getUserType().equals("management")
                        || !userPrefs.getUserCompanyId().equals(companyID)){
                    popupMenu.getMenu().removeItem(R.id.update);
                    popupMenu.getMenu().removeItem(R.id.delete);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.update:
                                Intent intent = new Intent(context, EditStaff.class);
                                intent.putExtra("staffID", staffId);
                                context.startActivity(intent);
                                break;
                            case R.id.delete:
                                alert(staffId, position);
                                break;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });

    }

    private static AlertDialog.Builder alertBuilder;
    private void alert(final String staffId, final int position){
        alertBuilder = new AlertDialog.Builder(context, R.style.alertDialogTheme);
        alertBuilder.setTitle("Delete Staff?");
        alertBuilder.setMessage("This staff will be permanently deleted");

        alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteStaff(staffId, position);
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

    private void deleteStaff(final String staffId, final int position) {
        progressDialog.setMessage("Deleting staff...");
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.STAFF_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
//                        {"error":false,"message":"Staff has successfully been deleted"}
                        Log.d(TAG + " onResponse", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")){
                                staffList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, staffList.size()-position);
                            }else if (jsonObject.getBoolean("error")){
//                                Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, jsonObject.getString("message"));
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

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userID_delete", staffId);
                return params;
            }
        };
        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView more;
        CircleImageView image;
        TextView name;
        TextView job;
        TextView email;
        TextView phone;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView)view.findViewById(R.id.staffName);
            job = (TextView)view.findViewById(R.id.staffJob);
            email = (TextView)view.findViewById(R.id.staffEmail);
            phone = (TextView)view.findViewById(R.id.staffPhone);
            image = (CircleImageView)view.findViewById(R.id.profile_image);
            more = (ImageView)view.findViewById(R.id.moreVert);
        }
    }

    public void setFilter(ArrayList<StaffData> newList) {
        this.staffList = new ArrayList<>();
        this.staffList.addAll(newList);
        notifyDataSetChanged();
    }

    public List<StaffData> getStaffList(){
        return this.staffList;
    }

}
