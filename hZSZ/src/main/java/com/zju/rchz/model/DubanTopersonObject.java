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

    private Integer replyState;//(有没有批示未回复)
    //1：有批示需回复、2：批示已回复、null/0暂无批示
    //（每当有领导提交批示，置为1；在用户提交批示回复时，当且仅当replyForInstrction里全部都有回复的批示才能置为2）。

    public String getReplyStateTxt(){//批示状态文字提示
        String replyStateTxt = "暂无批示";
        if (replyState != null){
            if(replyState.intValue() == 1){
                replyStateTxt = "有批示需回复";
            }else if(replyState.intValue() == 2){
                replyStateTxt = "批示已回复";
            }
        }
        return replyStateTxt;
    }
    public Integer getReplyState() {
        return replyState;
    }

    public void setReplyState(Integer replyState) {
        this.replyState = replyState;
    }

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

    public boolean isChiefHandle(){
        //是否需要河长进行反馈（有些督办只需要进行回复批示，当前不需要河长处理）
        //true:需要河长进行处理督办单
        //false：不需要河长进行处理督办单
        if(newState.intValue() == 30 || newState.intValue()==40 || newState.intValue()==50 || newState.intValue()==60){
            return false;
        }else{
            return true;
        }

    }
}
