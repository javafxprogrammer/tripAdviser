package com.lengwemushimba.intercityticketbooking.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Adapter;
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
import com.lengwemushimba.intercityticketbooking.EditBooking;
import com.lengwemushimba.intercityticketbooking.R;
import com.lengwemushimba.intercityticketbooking.RequestHandler;
import com.lengwemushimba.intercityticketbooking.ViewBookings;
import com.lengwemushimba.intercityticketbooking.model.BookingData;
import com.lengwemushimba.intercityticketbooking.model.ReviewData;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeParser;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lengwe on 6/10/18.
 */

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.MyViewHolder> {

    private static final String TAG = BookingAdapter.class.getSimpleName();
    private Context context;
    private List<BookingData> bookingList;
    private ProgressDialog progressDialog;

    public BookingAdapter(Context context, List<BookingData> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
        this.progressDialog = new ProgressDialog(this.context); //fix className.obj
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_bookings_cardview, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final BookingData bookingData = this.bookingList.get(position);

        holder.fromTo.setText(bookingData.getfromTo());
        holder.weekDayTime.setText(bookingData.getWeekDayTime());
        holder.date.setText(bookingData.getDate());
        holder.seatNumber.setText(bookingData.getSeatNumber());
        holder.comapanyBusName.setText(bookingData.getCompanyName());

        final String weekDay = bookingData.getWeekDay();
        final String date = bookingData.getDateOG();
        final String time = bookingData.getTime();
        final String from = bookingData.getFrom();
        final String to = bookingData.getTo();
        final String busID = bookingData.getBusID();
        final String seatNumber = bookingData.getSeatNumberOG();
        final String seatCount = bookingData.getSeatCount();

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context wrapper = new ContextThemeWrapper(context, R.style.popupTheme);
                PopupMenu popupMenu = new PopupMenu(wrapper, holder.more);
                popupMenu.getMenuInflater().inflate(R.menu.view_bookings_options_menu, popupMenu.getMenu());

                DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");
                DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

                LocalTime localTime = timeFormatter.parseLocalTime(time);
                LocalDate localDate = dateFormatter.parseLocalDate(date);

                LocalDateTime localDateTime = localDate.toLocalDateTime(localTime);

                if (localDateTime.isBefore(LocalDateTime.now())){
                    popupMenu.getMenu().removeItem(R.id.edit);
                    popupMenu.getMenu().removeItem(R.id.cancel);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    //seatNumber, date_, dayOfWeek, time_, from_, to_, busID
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.edit:
                                Intent intent = new Intent(context, EditBooking.class);
                                intent.putExtra("seatNumber", seatNumber);
                                intent.putExtra("date", date);
                                intent.putExtra("weekDay", weekDay);
                                intent.putExtra("time", time);
                                intent.putExtra("from", from);
                                intent.putExtra("to", to);
                                intent.putExtra("busID", busID);
                                intent.putExtra("seatCount", seatCount);
                                context.startActivity(intent);
                                break;
                            case R.id.cancel:
                                alert(seatNumber, date, weekDay, time, from, to, busID, position);
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
    private void alert(final String seatNumber, final String date, final
    String weekDay, final String time, final String from, final String to, final
                       String busID, final int position){
        alertBuilder = new AlertDialog.Builder(context, R.style.alertDialogTheme);
        alertBuilder.setTitle("Cancel Booking?");
        alertBuilder.setMessage("You will lose your seat");

        alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelBooking(seatNumber, date, weekDay, time, from, to, busID, position);
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

    private void cancelBooking(final String seatNumber, final String date, final
    String weekDay, final String time, final String from, final String to, final
    String busID, final int position) {

        progressDialog.setMessage("Canceling booking...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.BOOKING_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d(TAG + " onResponse", response);
                        try {
                            JSONObject jsObj = new JSONObject(response);
                            if (!jsObj.getBoolean("error")){
                                bookingList.remove(position);
//                                notifyDataSetChanged();
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, bookingList.size()-position);
                                Toast.makeText(context, "Trip cancelled successfully", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(context, jsObj.getString("message"), Toast.LENGTH_LONG).show();
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
                        Log.d(TAG + " onErrorResponse", error.getMessage());
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("seatNumber_delete", seatNumber);
                params.put("date_delete", date);
                params.put("weekDay_delete", weekDay);
                params.put("time_delete", time);
                params.put("from_delete", from);
                params.put("to_delete", to);
                params.put("busID_delete", busID);
                return params;
            }
        };
        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView fromTo;
        TextView weekDayTime;
        TextView date;
        TextView seatNumber;
        TextView comapanyBusName;
        ImageView more;

        public MyViewHolder(View itemView) {
            super(itemView);
            fromTo = (TextView)itemView.findViewById(R.id.fromTo);
            weekDayTime = (TextView)itemView.findViewById(R.id.weekDayTime);
            date = (TextView)itemView.findViewById(R.id.date);
            seatNumber = (TextView)itemView.findViewById(R.id.seatNumber);
            comapanyBusName = (TextView)itemView.findViewById(R.id.companyBusName);
            more = (ImageView)itemView.findViewById(R.id.moreVert);
        }
    }

    public void setFilter(ArrayList<BookingData> newList) {
        this.bookingList = new ArrayList<>();
        this.bookingList.addAll(newList);
        notifyDataSetChanged();
    }

    public List<BookingData> getBookingList(){
        return this.bookingList;
    }

}
