package com.lengwemushimba.intercityticketbooking.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.lengwemushimba.intercityticketbooking.AddBooking;
import com.lengwemushimba.intercityticketbooking.R;
import com.lengwemushimba.intercityticketbooking.SharedPreferenceManager;
import com.lengwemushimba.intercityticketbooking.model.BusData;
import com.lengwemushimba.intercityticketbooking.model.Logistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lengwe on 6/21/18.
 */

public class AllBusesAdapter extends RecyclerView.Adapter<AllBusesAdapter.MyViewHolder> {

    private Context context;
    private List<BusData> busList;
    private SharedPreferenceManager userPref;

    public AllBusesAdapter(Context context, List<BusData> busList) {
        this.context = context;
        this.busList = busList;
        this.userPref = SharedPreferenceManager.getInstance(this.context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.all_buses_cardview, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final BusData busData = this.busList.get(position);
        final int busID = busData.getBusID();

        holder.companyName.setText(busData.getCompanyName());
        holder.name.setText(busData.getName());
        holder.seats.setText("Seats (" + String.valueOf(busData.getSeats()) + ")");
        holder.details.setText(busData.getDesc());

        ArrayList<Logistics> logisticsList = busData.getLogisticsArrayList();

//        ArrayList<TableRow> rowList = new ArrayList<>();
//        for (int i = 0; i < logisticsList.size(); i++) {
//            rowList.add(new TableRow(context));
//        }

        int rowCount = holder.table.getChildCount();
        if (rowCount >= 2) {
            holder.table.removeViews(1, rowCount - 1);
        }

        for (int i = 0; i < logisticsList.size(); i++) {

//            final TableRow tableRow = rowList.get(i);
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

                        Intent intent = new Intent(context, AddBooking.class);
                        intent.putExtra("busID", String.valueOf(busID));
                        intent.putExtra("from", from);
                        intent.putExtra("to", to);
                        intent.putExtra("day", day);
                        intent.putExtra("time", time);
                        intent.putExtra("seatCount", busData.getSeats());
                        intent.putExtra("bookingInfo", bookingInfo);
                        context.startActivity(intent);
                }
            });
            holder.table.addView(tableRow);
        }

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
        public TextView companyName;

        public MyViewHolder(View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.busName);
            seats = (TextView) view.findViewById(R.id.seatCount);
            details = (TextView) view.findViewById(R.id.details);
            table = (TableLayout) view.findViewById(R.id.busTable);
            companyName = (TextView) view.findViewById(R.id.companyName);

        }
    }

    public void setFilter(ArrayList<BusData> newList) {
        this.busList = new ArrayList<>();
        this.busList.addAll(newList);
        notifyDataSetChanged();
    }

    public List<BusData> getAllBusList() {
        return this.busList;
    }

}
