package com.lengwemushimba.intercityticketbooking.model;

/**
 * Created by lengwe on 5/19/18.
 */

public class CompanyData {

    private String companyID;
    private String companyImage;
    private String companyName;
    private String companyDetails;
    private String companyPhone;
    private String companyEmail;
    private String companyWebsite;
    private String companyRating;
    private String companyRatingStats;
    private String companyBusCount;

    public CompanyData(String companyID, String companyImage, String companyName, String companyDetails, String companyPhone, String companyEmail, String companyWebsite, String companyRating, String companyRatingStats, String companyBusCount) {
        this.companyID = companyID;
        this.companyImage = companyImage;
        this.companyName = companyName;
        this.companyDetails = companyDetails;
        this.companyPhone = companyPhone;
        this.companyEmail = companyEmail;
        this.companyWebsite = companyWebsite;
        this.companyRating = companyRating;
        this.companyRatingStats = companyRatingStats;
        this.companyBusCount = companyBusCount;
    }

    public String getCompanyID() {
        return companyID;
    }

    public String getCompanyImage() {
        return companyImage;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyDetails() {
        return companyDetails;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public String getCompanyRating() {
        return companyRating;
    }

    public String getCompanyRatingStats() {
        return companyRatingStats;
    }

    public String getCompanyBusCount() {
        return companyBusCount;
    }
}
