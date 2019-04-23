package com.lengwemushimba.intercityticketbooking.model;

/**
 * Created by lengwe on 5/25/18.
 */

public class Logistics {

    private String from;
    private String to;
    private Double amount;
    private String day;
    private String time;

    public Logistics(String from, String to, Double amount, String day, String time) {

        this.from = from;
        this.to = to;
        this.amount = amount;
        this.day = day;
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Double getAmount() {
        return amount;
    }

    public String getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }
}
