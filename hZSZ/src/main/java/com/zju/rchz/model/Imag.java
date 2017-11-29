package com.zju.rchz.model;

/**
 * Created by ZJTLM4600l on 2017/9/6.
 */

public class Imag {
    private String imagPath;
    private Double longitude;
    private Double latitude;

    public String getImagPath() {
        return imagPath;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setImagPath(String imagPath) {
        this.imagPath = imagPath;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
