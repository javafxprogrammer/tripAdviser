package com.lengwemushimba.intercityticketbooking.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lengwemushimba.intercityticketbooking.AddBooking;
import com.lengwemushimba.intercityticketbooking.AddTripTimeTable;
import com.lengwemushimba.intercityticketbooking.CompanyBus;
import com.lengwemushimba.intercityticketbooking.Constants;
import com.lengwemushimba.intercityticketbooking.EditCompanyBus;
import com.lengwemushimba.intercityticketbooking.EditTripTimeTable;
import com.lengwemushimba.intercityticketbooking.R;
import com.lengwemushimba.intercityticketbooking.RequestHandler;
import com.lengwemushimba.intercityticketbooking.SharedPreferenceManager;
import com.lengwemushimba.intercityticketbooking.StaticVar;
import com.lengwemushimba.intercityticketbooking.model.BusData;
import com.lengwemushimba.intercityticketbooking.model.Logistics;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lengwe on 5/24/18.
 */

public class CompanyBusAdapter extends RecyclerView.Adapter<CompanyBusAdapter.MyViewHolder> {

    private static final String TAG = CompanyBusAdapter.class.getSimpleName();

    private Context context;
    private List<BusData> busList;
    private String userType, userCompanyId;

    AlertDialog.Builder builderSingle;
    ArrayAdapter<String> arrayAdapter;
    private String companyID;
    private ProgressDialog progressDialog;


    public CompanyBusAdapter(Context context, ArrayList<BusData> busList, String companyID) {
        this.context = context;
        this.busList = busList;

        builderSingle = new AlertDialog.Builder(CompanyBus.companyBus, R.style.alertDialogTheme);
        progressDialog = new ProgressDialog(CompanyBus.companyBus);
//        builderSingle.setTitle("Select One Option");
//        arrayAdapter = new ArrayAdapter<String>(CompanyBus.companyBus, android.R.layout.select_dialog_singlechoice);
        arrayAdapter = new ArrayAdapter<String>(CompanyBus.companyBus, R.layout.singlechiocemenu);
        arrayAdapter.add("Book");
        arrayAdapter.add("Edit");
        arrayAdapter.add("Delete");
        this.companyID = companyID;

        this.userType = SharedPreferenceManager.getInstance(context).getUserType();
        this.userCompanyId = SharedPreferenceManager.getInstance(context).getUserCompanyId();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.company_bus_cardview, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
//        Log.d(TAG, "posi= " + position);
        final BusData busData = this.busList.get(position);
        final int busID = busData.getBusID();

        holder.name.setText(busData.getName());
        holder.seats.setText("Seats (" + String.valueOf(busData.getSeats()) + ")");
        holder.details.setText(busData.getDesc());

        ArrayList<Logistics> logisticsList = busData.getLogisticsArrayList();
//        Log.d("logisticsListSize ", String.valueOf(logisticsList.size()));

        int rowCount = holder.table.getChildCount();
        if (rowCount >= 2) {
            holder.table.removeViews(1, rowCount - 1);
        }

        for (int i = 0; i < logisticsList.size(); i++) {

            final TableRow tableRow = new TableRow(context);

            tableRow.setId(100 + i);
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView from = new TextView(context);
            from.setId(200 + i);
            from.setText(logisticsList.get(i).getFrom());
            from.setPadding(0, 0, 56, 0);
            tableRow.addView(from);

            TextView to = new TextView(context);
            to.setId(300 + i);
            to.setText(logisticsList.get(i).getTo());
            to.setPadding(0, 0, 56, 0);
            tableRow.addView(to);

            TextView amount = new TextView(context);
            amount.setId(400 + i);
//            amount.setText("K" + String.valueOf(logisticsList.get(i).getAmount()));
            double roundOff = (double) Math.round(logisticsList.get(i).getAmount() * 100) / 100;
            amount.setText("K" +String.valueOf(roundOff));
            amount.setPadding(0, 0, 56, 0);
            tableRow.addView(amount);

            TextView day = new TextView(context);
            day.setId(500 + i);
            day.setText(logisticsList.get(i).getDay());
            day.setPadding(0, 0, 56, 0);
            tableRow.addView(day);

            TextView time = new TextView(context);
            time.setId(600 + i);
            time.setText(String.valueOf(logisticsList.get(i).getTime()));
            time.setPadding(0, 0, 0, 0);
            tableRow.addView(time);

            tableRow.setPadding(0, 12, 0, 12);

            tableRow.setClickable(true);
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tableRow.setBackground(context.getDrawable(R.drawable.ripple_effect));
                    TableRow tr = (TableRow) v;
                    TextView fromTXT = (TextView) tr.getChildAt(0);
                    TextView toTXT = (TextView) tr.getChildAt(1);
                    TextView amountTXT = (TextView) tr.getChildAt(2);
                    TextView dayTXT = (TextView) tr.getChildAt(3);
                    TextView timeTXT = (TextView) tr.getChildAt(4);

                    final String from = fromTXT.getText().toString();
                    final String to = toTXT.getText().toString();
                    final String amount = amountTXT.getText().toString();
                    final String day = dayTXT.getText().toString();
                    final String time = timeTXT.getText().toString();

                    String bookingInfo = "“From: " + from + "\nTo: " + to + "\nAmount: " + amount + "\nDay: " + day + "\nTime: " + time + "”";

//                    if (userType.equals("managment") && userCompanyId.equals(companyID)) {
                    createDialog(String.valueOf(busID), from, to, amount, day, time, busData.getSeats(), bookingInfo);
//                    } else {
//                        Intent intent = new Intent(context.getApplicationContext(), AddBooking.class);
//                        intent.putExtra("busID", busID);
//                        intent.putExtra("from", from);
//                        intent.putExtra("to", to);
//                        intent.putExtra("day", day);
//                        intent.putExtra("time", time);
//                        intent.putExtra("seatCount", busData.getSeats());
//                        intent.putExtra("bookingInfo", bookingInfo);
//                        context.startActivity(intent);
//                    }
                }
            });

            holder.table.addView(tableRow);
        }

        Log.d(TAG + " childCount", String.valueOf(holder.table.getChildCount()));



        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context wrapper = new ContextThemeWrapper(context, R.style.popupTheme);
                PopupMenu popupMenu = new PopupMenu(wrapper, holder.more);
                popupMenu.getMenuInflater().inflate(R.menu.company_bus_options_menu, popupMenu.getMenu());

                if (!userCompanyId.equals(companyID)){
                    popupMenu.getMenu().removeItem(R.id.edit);
                    popupMenu.getMenu().removeItem(R.id.delete);
                    popupMenu.getMenu().removeItem(R.id.add);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        //add fab fro add bus if user !=customer to companyBus
                        switch (item.getItemId()) {
                            case R.id.edit:
                                Intent intent = new Intent(context, EditCompanyBus.class);
                                intent.putExtra("busID", String.valueOf(busID));
                                intent.putExtra("companyID", companyID);
                                context.startActivity(intent);
                                break;
                            case R.id.delete:
                                alert(String.valueOf(busID), position);
                                break;
                            case R.id.add:
                                Intent intent_add = new Intent(context, AddTripTimeTable.class);
                                intent_add.putExtra("busID", String.valueOf(busID));
                                intent_add.putExtra("companyID", companyID);
                                context.startActivity(intent_add);
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
        return busList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name;
        public TextView seats;
        public TextView details;
        public TableLayout table;
        public ImageView more;

        public MyViewHolder(View view) {
            super(view);

//            image = (ImageView) view.findViewById(R.id.companyBusImage);
            name = (TextView) view.findViewById(R.id.companyBusName);
            seats = (TextView) view.findViewById(R.id.companySeatCount);
            details = (TextView) view.findViewById(R.id.companyDetails);
            table = (TableLayout) view.findViewById(R.id.companyBustable);
            more = (ImageView) view.findViewById(R.id.moreVert);


        }
    }

    private void alert(final String from, final String to, final String busID) {
        alertBuilder = new AlertDialog.Builder(CompanyBus.companyBus, R.style.alertDialogTheme);
        alertBuilder.setTitle("Delete Schedule?");
        alertBuilder.setMessage("You will lose this schedule, ["+from+" - "+to+"]");

        alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTripAndTimeTable(from, to, busID);
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

    private void createDialog(final String busID, final String from, final String to, String amount, final String day, final String time, final int seats, final String bookingInfo) {

        builderSingle.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (!userCompanyId.equals(companyID)){
            arrayAdapter.remove("Edit");
            arrayAdapter.remove("Delete");
        }

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String strName = arrayAdapter.getItem(which);

                if (strName.equals("Book")) {
                    Intent intent = new Intent(context, AddBooking.class);
                    intent.putExtra("busID", busID);
                    intent.putExtra("from", from);
                    intent.putExtra("to", to);
                    intent.putExtra("day", day);
                    intent.putExtra("time", time);
                    intent.putExtra("seatCount", seats);
                    intent.putExtra("bookingInfo", bookingInfo);
                    context.startActivity(intent);
                } else if (strName.equals("Edit")) {
                    Intent intent = new Intent(context, EditTripTimeTable.class);
                    intent.putExtra("from", from);
                    intent.putExtra("to", to);
                    intent.putExtra("busID", busID);
                    intent.putExtra("companyID", companyID);
                    context.startActivity(intent);
                } else if (strName.equals("Delete")) {
                    alert(from, to, String.valueOf(busID));
                }
            }
        });
        builderSingle.show();
    }

    private static AlertDialog.Builder alertBuilder;
    private void alert(final String busID, final int position){
        alertBuilder = new AlertDialog.Builder(CompanyBus.companyBus, R.style.alertDialogTheme);
        alertBuilder.setTitle("Delete Bus?");
        alertBuilder.setMessage("You will lose this buses and its schedules");

        alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCompanyBus(busID, position);
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

    private void deleteCompanyBus(final String busID, final int position) {
        progressDialog.setMessage("Deleting bus...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.BUS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d(TAG + " onResponse", response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                busList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, busList.size() - position);
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
                params.put("busID_deleteBus", busID);
                return params;
            }
        };
        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);
    }


    private void deleteTripAndTimeTable(final String from, final String to, final String busID) {

        progressDialog.setMessage("Deleting Schedule...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.TRIP_TIMETABLE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d(TAG + " onResponse", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                Intent intent = new Intent(context, CompanyBus.class);
                                intent.putExtra("companyID", StaticVar.map.get("companyID"));
                                intent.putExtra("companyName", StaticVar.map.get("companyName"));
                                intent.putExtra("companyImage", StaticVar.map.get("companyImage"));
                                context.startActivity(intent);
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
                params.put("from_deleteSchedule", from);
                params.put("to_deleteSchedule", to);
                params.put("busID_deleteSchedule", busID);
                return params;
            }
        };
        RequestHandler.getInstance(context).addToRequestQueue(stringRequest);

    }

    public void setFilter(ArrayList<BusData> newList) {
        this.busList = new ArrayList<>();
        this.busList.addAll(newList);
        notifyDataSetChanged();
    }

    public List<BusData> getCompanyBusData() {
        return this.busList;
    }
}
