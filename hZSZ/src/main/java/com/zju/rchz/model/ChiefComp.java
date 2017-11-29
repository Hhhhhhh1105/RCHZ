package com.zju.rchz.model;

import com.zju.rchz.activity.BaseActivity;
import com.zju.rchz.utils.ResUtils;

public class ChiefComp {
	public String compId;
	public DateTime compDate;
	public String compSerNum;
	public String compTheme;
	public String compContent;

	public String advId;
	public DateTime advDate;
	public String advSerNum;
	public String advTheme;
	public String advContent;

	public String complaintsPicPath;
	public double longitude;
	public double latitude;
	// public int dubanStatus;
	public int dealStatus;


	// public int dubanId;

	public boolean isComp() {
		return compId != null;
	}

	public String getId() {
		return isComp() ? compId : advId;
	}

	public DateTime getDate() {
		return isComp() ? compDate : advDate;
	}

	public String getSerNum() {
		return isComp() ? compSerNum : advSerNum;
	}

	public String getContent() {
		return isComp() ? compContent : advContent;
	}

	public String getTheme() {
		return isComp() ? compTheme : advTheme;
	}

	public int getStatus() {
		return dealStatus;
	}

	public String getStatuss() {
		return BaseActivity.getCurActivity() != null ? BaseActivity.getCurActivity().getString(ResUtils.getHandleStatus(getStatus())) : "";
	}

	public boolean isHandling() {
		return dealStatus == 2;
	}

	public boolean isAddingHandle() {
		return dealStatus == 5;
	}


	public long id;
	public DateTime Time;
	public String serNum;
	public String theme;
	public String content;
	public int status;
	public String deadline;

	public long getDubanId() {
		return id;
	}

	public DateTime getDubanTime() {
		return Time;
	}

	public String getDubanSerNum() {
		return serNum;
	}

	public String getDubanTheme() {
		return theme;
	}

	public String getDubanContent() {
		return content;
	}

	public int getDubanStatus() {
		return status;
	}

	public String getDubanDeadline() {
		return deadline;
	}
}
