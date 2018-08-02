package com.zju.rchz.model;

/**
 * Created by ZJTLM4600l on 2017/10/25.
 */

public class RiverRecordTemporaryJson {
    private String lnglist;//经度
    private String latlist;//纬度
    private long riverId;
    private long lakeId;

    public long getLakeId() {
        return lakeId;
    }

    public void setLakeId(long lakeId) {
        this.lakeId = lakeId;
    }

    public long getRiverId() {
        return riverId;
    }

    public void setRiverId(long riverId) {
        this.riverId = riverId;
    }

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
