package com.lengwemushimba.intercityticketbooking.model;

/**
 * Created by lengwe on 6/26/18.
 */

public class Notification {
//"seatNumber" => $row["seatNumber"], "date_" => $row["date_"], "dayOfWeek" => $row["dayOfWeek"],
//"time_" => $row["time_"], "from_" => $row["from_"], "to_" => $row["to_"], "busID" => $row["busID"]));

//    PRIMARY KEY(seatNumber, date_, dayOfWeek, time_, from_, to_, busID)
    private String seatNumber;
    private String date;
    private String weekDay;
    private String time;
    private String from;
    private String to;
    private String busID;

    public Notification(String seatNumber, String date, String weekDay, String time, String from, String to, String busID) {
        this.seatNumber = seatNumber;
        this.date = date;
        this.weekDay = weekDay;
        this.time = time;
        this.from = from;
        this.to = to;
        this.busID = busID;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public String getDate() {
        return date;
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
}
