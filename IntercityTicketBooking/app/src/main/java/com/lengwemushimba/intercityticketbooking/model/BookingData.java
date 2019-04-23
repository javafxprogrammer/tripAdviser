package com.lengwemushimba.intercityticketbooking.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by lengwe on 6/10/18.
 */

public class BookingData {

    private int seatNumber;
    private String date;
    private String weekDay;
    private String time;
    private String from;
    private String to;
    private String busID;
    private String companyName;
    private String seatCount;

    public BookingData(int seatNumber, String date, String weekDay, String time, String from, String to, String busID, String companyName, String seatCount) {
        this.seatNumber = seatNumber;
        this.date = date;
        this.weekDay = weekDay;
        this.time = time;
        this.from = from;
        this.to = to;
        this.companyName = companyName;
        this.busID = busID;
        this.seatCount = seatCount;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public String getTime() {
        return time;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getBusID() {
        return busID;
    }

    public String getfromTo() {
        return from.toUpperCase() + " - " + to.toUpperCase();
    }

    public String getWeekDayTime() {
        return weekDay+ "   " + time.concat(" hrs");
    }

    public String getDateOG(){
        return date;
    }
    public String getSeatNumberOG(){
        return String.valueOf(seatNumber);
    }

    public String getSeatNumber() {
        return "Seat number " + seatNumber;
    }

    public String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date date = null;
        try {
            date = sdf.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateFormat.format(date);
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getSeatCount() {
        return seatCount;
    }
}
