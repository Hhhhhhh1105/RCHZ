package com.zju.rchz.model;

/**
 * Created by ZJTLM4600l on 2017/10/25.
 */

public class RiverRecordTemporaryJson {
    private String lnglist;//经度
    private String latlist;//纬度

    public void setLnglist(String lnglist) {
        this.lnglist = lnglist;
    }

    public void setLatlist(String latlist) {
        this.latlist = latlist;
    }

    public String getLnglist() {
        return lnglist;
    }

    public String getLatlist() {
        return latlist;
    }
}
