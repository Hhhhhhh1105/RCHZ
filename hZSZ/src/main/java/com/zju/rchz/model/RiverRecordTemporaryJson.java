package com.zju.rchz.model;

/**
 * Created by ZJTLM4600l on 2017/10/25.
 */

public class RiverRecordTemporaryJson {
    private String lnglist;//经度
    private String latlist;//纬度
    private long riverId;
    private long lakeId;
    private Integer passTime;//暂存的巡河时长。（单位：秒）

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

    public Integer getPassTime() {
        return passTime;
    }

    public void setPassTime(Integer passTime) {
        this.passTime = passTime;
    }
}
