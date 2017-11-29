package com.zju.rchz.model;

import com.zju.rchz.activity.BaseActivity;
import com.zju.rchz.utils.StrUtils;

public class Mail {
	public long id;
	public int ifRead;
	public String theme;
	public String picPath;
	public DateTime updateTime;

	public String creatorname;
	public DateTime update_time;
	public DateTime release_time;
	public String content;

	public String getYMDHM() {
		DateTime dt = updateTime != null ? updateTime : (update_time != null ? update_time : release_time);
		return dt != null ? dt.getYMDHM(BaseActivity.getCurActivity()) : "";
	}

	public String getContentText() {
		return StrUtils.trimString(content);
	}

	public String getStatusText() {
		return !isReaded() ? "未签收" : "已签收";
	}

	public boolean isReaded() {
		return ifRead != 0;
	}
}
