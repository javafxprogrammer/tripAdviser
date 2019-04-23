package com.lengwemushimba.intercityticketbooking.model;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by lengwe on 6/17/18.
 */

public class ReviewData {

    private String firstName;
    private String lastName;
    private String profilePicture;
    private String details;
    private String rating;
    private String date;
    private String companyID;
    private String reviewID;
    private int userID;


    public ReviewData(String userID, String firstName, String lastName, String profilePicture, String details, String rating, String date, String companyID, String reviewID) {
        this.userID = Integer.valueOf(userID);
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePicture = profilePicture;
        this.details = details;
        this.rating = rating;
        this.date = date;
        this.companyID = companyID;
        this.reviewID = reviewID;
    }

    public int getUserID() {
        return userID;
    }

    public String getNameAndDate(){

        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date date = null;
        try {
            date = sdf.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return firstName+" "+lastName+" - "+dateFormat.format(date);
    }

    public String getUserName() {
        return firstName+" "+lastName;
    }

    public String getDetails() {
        return details;
    }

    public String getDate() {
        return date;
    }

    public String getProfilePicture() {
        Log.d("getProfilePicture", profilePicture);
        return profilePicture;
    }

    public String getRating() {
        return rating;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCompanyID() {
        return companyID;
    }

    public String getReviewID() {
        return reviewID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
