package com.zju.rchz.model;

import com.zju.rchz.activity.BaseActivity;
import com.zju.rchz.utils.ResUtils;

/**
 * Created by ZJTLM4600l on 2017/9/16.
 */

public class DubanTopersonObject {
    public String id;
    public DateTime time;
    public String serNum;
    public String theme;
    public String content;
    public int overdue;
    public int sendtochief;

    private Integer riverOrLake;
    private Integer newState;//新状态字段 0待受理20已受理/待反馈30带查验40待审核41不合格50归档60转督

    public Integer getRiverOrLake() {
        return riverOrLake;
    }

    public void setRiverOrLake(Integer riverOrLake) {
        this.riverOrLake = riverOrLake;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public int status;
    public String deadline;

    public Integer getNewState() {
        return newState;
    }

    public void setNewState(Integer newState) {
        this.newState = newState;
    }

    public void setSendtochief(int sendtochief) {
        this.sendtochief = sendtochief;
    }

    public int getSendtochief() {
        return sendtochief;
    }

    public int getOverdue() {
        return overdue;
    }

    public void setOverdue(int overdue) {
        this.overdue = overdue;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public void setSerNum(String serNum) {
        this.serNum = serNum;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public DateTime getTime() {
        return time;
    }

    public String getSerNum() {
        return serNum;
    }

    public String getTheme() {
        return theme;
    }

    public String getContent() {
        return content;
    }

    public int getStatus() {
        return status;
    }

    public String getDeadline() {
        return deadline;
    }
    public String getStatuss() {
        return BaseActivity.getCurActivity() != null ? BaseActivity.getCurActivity().getString(ResUtils.getHandleStatus01(getStatus())) : "";
    }
    public String getNewMark(int newStatus){
        return BaseActivity.getCurActivity() != null ? BaseActivity.getCurActivity().getString(ResUtils.getHandleStatus01(newStatus)) : "";
    }
    public boolean isHandled(){return status==2;}
}
