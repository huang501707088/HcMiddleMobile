package com.android.hcframe.internalservice.signcls;

/**
 * Created by Administrator on 2016/4/28 0028.
 */
public class MapTun {
    private String address;
    private String addressOne;
    private double longitude;
    private double latitude;

    public MapTun() {

    }

    public MapTun(String address) {
        this.address = address;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressOne() {
        return addressOne;
    }

    public void setAddressOne(String addressOne) {
        this.addressOne = addressOne;
    }
}
