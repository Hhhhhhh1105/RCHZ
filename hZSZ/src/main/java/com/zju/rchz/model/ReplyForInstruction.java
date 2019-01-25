package com.zju.rchz.model;

/**
 * Created by DONGHUI on 2019/1/18.
 * 批示的回复项
 */

public class ReplyForInstruction {
    private String replyPersonName;//内容
    private String content;//内容
    private String picPath;
    private DateTime replyDate;//回复批示的日期

    public void setReplyPersonName(String replyPersonName) {
        this.replyPersonName = replyPersonName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public void setReplyDate(DateTime replyDate) {
        this.replyDate = replyDate;
    }

    public String getReplyPersonName() {
        return replyPersonName;
    }

    public String getContent() {
        return content;
    }

    public String getPicPath() {
        return picPath;
    }

    public DateTime getReplyDate() {
        return replyDate;
    }
}
