package com.zju.rchz.model;

import com.zju.rchz.activity.BaseActivity;
import com.zju.rchz.utils.ResUtils;

/**
 * Created by ZJTLM4600l on 2017/9/17.
 */

public class DubanTopersonData {
    public String serNum;
    public String personName;
    public String teleNum;
    public DateTime submitTime;
    public String riverName;
    public String riverDist;
    public String content;
    public String picPath;
    public String dealPersonName;
    public String dealTeleNum;
    public DateTime dealTime;
    public String dealResult;
    public String theme;
    public String dealPicPath;
    public int ifAnonymous;
    public int dealStatus;
    public double longitude;
    public double latitude;
    public String imgLnglist;
    public String imgLatlist;
    public String deadLine;
    public int flag;

    private Integer signTochief; // 市级河长是否签收，0未签1签收
    private Integer signTocontact; // 市级河长联系人是否签收，0未签1签收
    private Integer signToRiverChief; // 镇级河长是否签收，0未签1签收

    private String secondResult;//镇级河长办或镇级河长二次反馈内容
    private String secondDealPicPath;//镇级河长办或镇级河长二次反馈照片

    private String dubanTheme;//督办主题
    private String dubanContent;//督办内容
    private Integer isRefused;//是否被督查员退回
    private String firstCheckContent;//第一次检查反馈的内容
    private String firstCheckPicPath;//第一次检查反馈的图片
    private String secondCheckContent;//第二次检查反馈的内容
    private String secondCheckPicPath;//第二次检查反馈的图片

    private String dealPersonName2; // 处理人姓名单独存入字段2
    private DateTime dubanTime;//河长办督办时间
    private DateTime secondDealTime;//河长第二次反馈时间
    private DateTime firstCheckTime;//督察员第一次验收单的时间
    private DateTime secondCheckTime;//督察员第二次验收的时间
    private Integer newState;//新状态字段 0待受理20已受理/待反馈30带查验40待审核41不合格50归档60转督

    private String instructionResult;//批示结果
    private String instructionPerson;//批示领导
    private DateTime instructionDate;//批示日期

    private String instructionFromSecretary;//书记批示结果
    private DateTime instructionFromSecretaryDate;//批示日期
    private String instructionFromMayor;//市长批示结果
    private DateTime instructionFromMayorDate;//批示日期
    private String instructionFromOtherMayor;//分管市长批示结果
    private DateTime instructionFromOtherMayorDate;//批示日期
    private String instructionFromCityChief;//市级河长批示结果
    private DateTime instructionFromCityChiefDate;//批示日期
    private String instructionFromBossChief;//总河长批示结果
    private DateTime instructionFromBossChiefDate;//批示日期

    private Integer riverOrLake;
    private String lakeName;
    private String lakeDist;

    public void setRiverOrLake(Integer riverOrLake) {
        this.riverOrLake = riverOrLake;
    }

    public void setLakeName(String lakeName) {
        this.lakeName = lakeName;
    }

    public void setLakeDist(String lakeDist) {
        this.lakeDist = lakeDist;
    }

    public Integer getRiverOrLake() {
        return riverOrLake;
    }

    public String getLakeName() {
        return lakeName;
    }

    public String getLakeDist() {
        return lakeDist;
    }

    public void setInstructionFromCityChief(String instructionFromCityChief) {
        this.instructionFromCityChief = instructionFromCityChief;
    }

    public void setInstructionFromCityChiefDate(DateTime instructionFromCityChiefDate) {
        this.instructionFromCityChiefDate = instructionFromCityChiefDate;
    }

    public String getInstructionFromCityChief() {
        return instructionFromCityChief;
    }

    public DateTime getInstructionFromCityChiefDate() {
        return instructionFromCityChiefDate;
    }

    public void setInstructionFromSecretary(String instructionFromSecretary) {
        this.instructionFromSecretary = instructionFromSecretary;
    }

    public void setInstructionFromSecretaryDate(DateTime instructionFromSecretaryDate) {
        this.instructionFromSecretaryDate = instructionFromSecretaryDate;
    }

    public void setInstructionFromMayor(String instructionFromMayor) {
        this.instructionFromMayor = instructionFromMayor;
    }

    public void setInstructionFromMayorDate(DateTime instructionFromMayorDate) {
        this.instructionFromMayorDate = instructionFromMayorDate;
    }

    public void setInstructionFromOtherMayor(String instructionFromOtherMayor) {
        this.instructionFromOtherMayor = instructionFromOtherMayor;
    }

    public void setInstructionFromOtherMayorDate(DateTime instructionFromOtherMayorDate) {
        this.instructionFromOtherMayorDate = instructionFromOtherMayorDate;
    }

    public void setInstructionFromBossChief(String instructionFromBossChief) {
        this.instructionFromBossChief = instructionFromBossChief;
    }

    public void setInstructionFromBossChiefDate(DateTime instructionFromBossChiefDate) {
        this.instructionFromBossChiefDate = instructionFromBossChiefDate;
    }

    public String getInstructionFromSecretary() {
        return instructionFromSecretary;
    }

    public DateTime getInstructionFromSecretaryDate() {
        return instructionFromSecretaryDate;
    }

    public String getInstructionFromMayor() {
        return instructionFromMayor;
    }

    public DateTime getInstructionFromMayorDate() {
        return instructionFromMayorDate;
    }

    public String getInstructionFromOtherMayor() {
        return instructionFromOtherMayor;
    }

    public DateTime getInstructionFromOtherMayorDate() {
        return instructionFromOtherMayorDate;
    }

    public String getInstructionFromBossChief() {
        return instructionFromBossChief;
    }

    public DateTime getInstructionFromBossChiefDate() {
        return instructionFromBossChiefDate;
    }

    public void setInstructionResult(String instructionResult) {
        this.instructionResult = instructionResult;
    }

    public void setInstructionPerson(String instructionPerson) {
        this.instructionPerson = instructionPerson;
    }

    public void setInstructionDate(DateTime instructionDate) {
        this.instructionDate = instructionDate;
    }

    public String getInstructionResult() {
        return instructionResult;
    }

    public String getInstructionPerson() {
        return instructionPerson;
    }

    public DateTime getInstructionDate() {
        return instructionDate;
    }

    public void setDealPersonName2(String dealPersonName2) {
        this.dealPersonName2 = dealPersonName2;
    }

    public void setDubanTime(DateTime dubanTime) {
        this.dubanTime = dubanTime;
    }

    public void setSecondDealTime(DateTime secondDealTime) {
        this.secondDealTime = secondDealTime;
    }

    public void setFirstCheckTime(DateTime firstCheckTime) {
        this.firstCheckTime = firstCheckTime;
    }

    public void setSecondCheckTime(DateTime secondCheckTime) {
        this.secondCheckTime = secondCheckTime;
    }

    public String getDealPersonName2() {
        return dealPersonName2;
    }

    public DateTime getDubanTime() {
        return dubanTime;
    }

    public DateTime getSecondDealTime() {
        return secondDealTime;
    }

    public DateTime getFirstCheckTime() {
        return firstCheckTime;
    }

    public DateTime getSecondCheckTime() {
        return secondCheckTime;
    }

    public void setSignTochief(Integer signTochief) {
        this.signTochief = signTochief;
    }

    public void setSignTocontact(Integer signTocontact) {
        this.signTocontact = signTocontact;
    }

    public void setSignToRiverChief(Integer signToRiverChief) {
        this.signToRiverChief = signToRiverChief;
    }

    public void setSecondResult(String secondResult) {
        this.secondResult = secondResult;
    }

    public void setSecondDealPicPath(String secondDealPicPath) {
        this.secondDealPicPath = secondDealPicPath;
    }

    public void setDubanTheme(String dubanTheme) {
        this.dubanTheme = dubanTheme;
    }

    public void setDubanContent(String dubanContent) {
        this.dubanContent = dubanContent;
    }

    public void setIsRefused(Integer isRefused) {
        this.isRefused = isRefused;
    }

    public void setFirstCheckContent(String firstCheckContent) {
        this.firstCheckContent = firstCheckContent;
    }

    public void setFirstCheckPicPath(String firstCheckPicPath) {
        this.firstCheckPicPath = firstCheckPicPath;
    }

    public void setSecondCheckContent(String secondCheckContent) {
        this.secondCheckContent = secondCheckContent;
    }

    public void setSecondCheckPicPath(String secondCheckPicPath) {
        this.secondCheckPicPath = secondCheckPicPath;
    }

    public void setNewState(Integer newState) {
        this.newState = newState;
    }

    public Integer getSignTochief() {
        return signTochief;
    }

    public Integer getSignTocontact() {
        return signTocontact;
    }

    public Integer getSignToRiverChief() {
        return signToRiverChief;
    }

    public String getSecondResult() {
        return secondResult;
    }

    public String getSecondDealPicPath() {
        return secondDealPicPath;
    }

    public String getDubanTheme() {
        return dubanTheme;
    }

    public String getDubanContent() {
        return dubanContent;
    }

    public Integer getIsRefused() {
        return isRefused;
    }

    public String getFirstCheckContent() {
        return firstCheckContent;
    }

    public String getFirstCheckPicPath() {
        return firstCheckPicPath;
    }

    public String getSecondCheckContent() {
        return secondCheckContent;
    }

    public String getSecondCheckPicPath() {
        return secondCheckPicPath;
    }

    public Integer getNewState() {
        return newState;
    }
    public String getNewMark(int newStatus){
        return BaseActivity.getCurActivity() != null ? BaseActivity.getCurActivity().getString(ResUtils.getHandleStatus01(newStatus)) : "";
    }
    public String getDealPersonName() {
        return dealPersonName;
    }

    public void setDealPersonName(String dealPersonName) {
        this.dealPersonName = dealPersonName;
    }

    public String getTeleNum() {
        return teleNum;
    }

    public void setTeleNum(String teleNum) {
        this.teleNum = teleNum;
    }

    public void setSerNum(String serNum) {
        this.serNum = serNum;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public void setSubmitTime(DateTime submitTime) {
        this.submitTime = submitTime;
    }

    public void setRiverName(String riverName) {
        this.riverName = riverName;
    }

    public void setRiverDist(String riverDist) {
        this.riverDist = riverDist;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public void setDealTeleNum(String dealTeleNum) {
        this.dealTeleNum = dealTeleNum;
    }

    public void setDealTime(DateTime dealTime) {
        this.dealTime = dealTime;
    }

    public void setDealResult(String dealResult) {
        this.dealResult = dealResult;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setDealPicPath(String dealPicPath) {
        this.dealPicPath = dealPicPath;
    }

    public void setIfAnonymous(int ifAnonymous) {
        this.ifAnonymous = ifAnonymous;
    }

    public void setDealStatus(int dealStatus) {
        this.dealStatus = dealStatus;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setImgLnglist(String imgLnglist) {
        this.imgLnglist = imgLnglist;
    }

    public void setImgLatlist(String imgLatlist) {
        this.imgLatlist = imgLatlist;
    }

    public void setDeadLine(String deadLine) {
        this.deadLine = deadLine;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getSerNum() {
        return serNum;
    }

    public String getPersonName() {
        return personName;
    }

    public DateTime getSubmitTime() {
        return submitTime;
    }

    public String getRiverOrLakeName() {
        if(getRiverOrLake().intValue() == 0)
            return riverName;
        else
            return lakeName;
    }
    public String getRiverName() {
        return riverName;
    }

    public String getRiverDist() {
        return riverDist;
    }

    public String getContent() {
        return content;
    }

    public String getPicPath() {
        return picPath;
    }

    public String getDealTeleNum() {
        return dealTeleNum;
    }

    public DateTime getDealTime() {
        return dealTime;
    }

    public String getDealResult() {
        return dealResult;
    }

    public String getTheme() {
        return theme;
    }

    public String getDealPicPath() {
        return dealPicPath;
    }

    public int getIfAnonymous() {
        return ifAnonymous;
    }

    public int getDealStatus() {
        return dealStatus;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getImgLnglist() {
        return imgLnglist;
    }

    public String getImgLatlist() {
        return imgLatlist;
    }

    public String getDeadLine() {
        return deadLine;
    }

    public int getFlag() {
        return flag;
    }
}
