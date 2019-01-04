package com.zju.rchz.model;

import com.zju.rchz.activity.BaseActivity;

/**
 * Created by ZJTLM4600l on 2018/6/12.
 */

public class LakeRecord {
    public long lakeId;//巡查湖道Id
    public String recordSerNum;//编号RR-XH-00001

    public int flotage;
    public String flotages;//漂浮物
    public int rubbish;
    public String rubbishs;//垃圾
    public int  building;
    public String buildings;//建筑物
    public int sludge;
    public String sludges;//淤泥
    public int odour;
    public String odours;//臭味
    public int outfall;
    public String outfalls;//排污口
    public int lakeinplace;//湖道长效管理机制和保洁机制是否到位
    public String lakeinplaces;
    public String otherquestion;//其他问题
    public String deal;//处理情况
    public DateTime recordDate;//巡湖日期
    public String picPath;//图片地址

    public String latlist; //经度数组
    public String lnglist; //纬度数组
    public String recordLakeName;
    public String recordPersonName;
    public String imgLnglist;//照片的经度
    public String imgLatlist;//照片的纬度

    public long recordId;
    public long recordPersonId; //巡湖人的userId
    public int recordPersonAuthority;  //巡湖人的authority

    public String isCorrect;//有效性判断 ，1表示有效，0表示无效
    public String judgeReason;//无效时的原因

    public String patrolLength;//巡河长度
    public String patrolTime;//巡河时长

    // local
    public Lake locLake;
    public String locLakeName;

    public String getYMD2() {
        return recordDate != null ? recordDate.getYMD2(BaseActivity.getCurActivity()) : "";
    }

    public String getYMDHM() {
        return recordDate != null ? recordDate.getYMDHM(BaseActivity.getCurActivity()) : "";
    }
}
