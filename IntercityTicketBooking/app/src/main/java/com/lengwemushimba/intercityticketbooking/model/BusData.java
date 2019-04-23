package com.lengwemushimba.intercityticketbooking.model;

import java.util.ArrayList;

/**
 * Created by lengwe on 5/23/18.
 */

public class BusData {

    private int busID;
    private String name;
    private String desc;
    private int seats;
    private ArrayList<Logistics> logisticsArrayList;
    private String companyName;

    public BusData(String companyName, int busID, String name, String desc, int seats, ArrayList<Logistics> logisticsArrayList) {
        this.companyName = companyName;
        this.busID = busID;
        this.name = name;
        this.desc = desc;
        this.seats = seats;
        this.logisticsArrayList = new ArrayList<>(logisticsArrayList);
    }

    public int getBusID() {
        return busID;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public int getSeats() {
        return seats;
    }

    public ArrayList<Logistics> getLogisticsArrayList() {
        return logisticsArrayList;
    }

    public String getCompanyName() {
        return companyName;
    }
}
