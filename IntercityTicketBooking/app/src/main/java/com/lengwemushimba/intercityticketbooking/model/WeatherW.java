package com.lengwemushimba.intercityticketbooking.model;

/**
 * Created by lengwe on 7/5/18.
 */

public class WeatherW {

    private String description ="";
    private String icon ="";

    public WeatherW(String description, String icon) {
        this.description = description;
        this.icon = icon;
    }

    public WeatherW() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
