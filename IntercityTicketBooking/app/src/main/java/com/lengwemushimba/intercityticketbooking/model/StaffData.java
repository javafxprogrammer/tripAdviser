package com.lengwemushimba.intercityticketbooking.model;

/**
 * Created by lengwe on 7/26/18.
 */

public class StaffData {

    private String id;
    private String name;
    private String job;
    private String email;
    private String phone;
    private String picture;
    private String companyID;

    public StaffData(String id, String fName, String lName, String job, String email, String phone, String picture, String companyID) {
        this.id = id;
        this.name = fName+" "+lName;
        this.job = job;
        this.email = email;
        this.phone = phone;
        this.picture = picture;
        this.companyID = companyID;
    }

    public String getName() {
        return name;
    }

    public String getJob() {
        return job;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPicture() {
        return picture;
    }

    public String getId() {
        return id;
    }

    public String getCompanyID() {
        return companyID;
    }
}
