package com.bajajallianz.myweatherapp.utils;

public class Weather {
    public Weather(String dateTime, String temp, String tempMin, String tempMax, String description, String time) {
        this.dateTime = dateTime;
        this.temp = temp;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.description = description;
        this.time = time;
    }

    private String dateTime;
    private String temp;
    private String tempMin;
    private String tempMax;
    private String description;
    private String time;


    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getTempMin() {
        return tempMin;
    }

    public void setTempMin(String tempMin) {
        this.tempMin = tempMin;
    }

    public String getTempMax() {
        return tempMax;
    }

    public void setTempMax(String tempMax) {
        this.tempMax = tempMax;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
