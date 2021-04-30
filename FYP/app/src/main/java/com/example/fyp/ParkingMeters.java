package com.example.fyp;

import java.io.Serializable;
import java.util.Comparator;

public class ParkingMeters implements Serializable {
    String meterNumber, location, areaRef, numSpace, timesOfOperation, hourlyTariff, restrictions, latitude, longitude;
    double price, distance;

    public ParkingMeters() {
    }

    public ParkingMeters(String meterNumber, String location, String areaRef, String numSpace, String timesOfOperation, String hourlyTariff, String restrictions, String latitude
    , String longitude) {
        this.meterNumber = meterNumber;
        this.location = location;
        this.areaRef = areaRef;
        this.numSpace = numSpace;
        this.timesOfOperation = timesOfOperation;
        this.hourlyTariff = hourlyTariff;
        this.restrictions = restrictions;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ParkingMeters(String meterNumber, String location, String areaRef, String numSpace, String timesOfOperation, String hourlyTariff, String restrictions, String latitude
            , String longitude, double price, double distance) {
        this.meterNumber = meterNumber;
        this.location = location;
        this.areaRef = areaRef;
        this.numSpace = numSpace;
        this.timesOfOperation = timesOfOperation;
        this.hourlyTariff = hourlyTariff;
        this.restrictions = restrictions;
        this.latitude = latitude;
        this.longitude = longitude;
        this.price = price;
        this.distance=distance;
    }


    public String getMeterNumber() {
        return meterNumber;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAreaRef() {
        return areaRef;
    }

    public void setAreaRef(String areaRef) {
        this.areaRef = areaRef;
    }

    public String getNumSpace() {
        return numSpace;
    }

    public void setNumSpace(String numSpace) {
        this.numSpace = numSpace;
    }

    public String getTimesOfOperation() {
        return timesOfOperation;
    }

    public void setTimesOfOperation(String timesOfOperation) {
        this.timesOfOperation = timesOfOperation;
    }

    public void addTimesOfOperation(String timesOfOperation){
        this.timesOfOperation = this.timesOfOperation + " " + timesOfOperation;
    }

    public String getHourlyTariff() {
        return hourlyTariff;
    }

    public void setHourlyTariff(String hourlyTariff) {
        this.hourlyTariff = hourlyTariff;
    }

    public String getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(String restrictions) {
        this.restrictions = restrictions;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }


    public String toString(){
        return meterNumber + "|" + location + "|" + areaRef + "|" + numSpace + "|" + timesOfOperation + "|" + hourlyTariff + "|" + restrictions + "|" + latitude + "|" + longitude;
    }

    public String toStringForRV(){
        return "<b>Street:</b>&nbsp;&nbsp;&nbsp;" + location + "\n<b>Price:</b>    \u20ac" + hourlyTariff + "\n<b>Capacity:</b>   " + numSpace + "\n<b>Hours:</b>   " + timesOfOperation
                + "\n<b>Restrictions:</b>   " + restrictions + "\n<b>Area Reference:</b>   " + areaRef + "\n<b>Meter Number:</b>   " + meterNumber;
    }

    public String toStringForRV2(){
        return "<b>Street:</b>&nbsp;&nbsp;&nbsp;" + location + "\n<b>Price:</b>    \u20ac" + hourlyTariff + "\n<b>Capacity:</b>   " + numSpace + "\n<b>Hours:</b>   " + timesOfOperation
                + "\n<b>Restrictions:</b>   " + restrictions + "\n<b>Area Reference:</b>   " + areaRef + "\n<b>Meter Number:</b>   " + meterNumber
                + "\n\n<b>Price For Stay:</b> \u20ac" + price + "\n<b>Distance From Location:</b> " + distance + " meters";
    }
}

class SortByPrice implements Comparator<ParkingMeters>{

    @Override
    public int compare(ParkingMeters a, ParkingMeters b) {
        if(a.getPrice()<b.getPrice()){
            return -1;
        }
        if(a.getPrice()>b.getPrice()){
            return 1;
        }
        return 0;
    }
}

class SortByDistance implements Comparator<ParkingMeters>{

    @Override
    public int compare(ParkingMeters a, ParkingMeters b) {
        if(a.getDistance()<b.getDistance()){
            return -1;
        }
        if(a.getDistance()>b.getDistance()){
            return 1;
        }
        return 0;
    }
}
