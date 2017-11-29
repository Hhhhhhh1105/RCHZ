package com.zju.rchz.model;

import com.zju.rchz.utils.StrUtils;

public class CompSugs {
	public String compTheme;
	public int compStatus;
	public int complaintsId;
	public String complaintsNum;
	public String complaintsContent;
	public String complaintsPicPath;
	public int compPersonId; //投诉人的Id

	public void setCompTheme(String compTheme) {
		this.compTheme = compTheme;
	}

	public void setCompStatus(int compStatus) {
		this.compStatus = compStatus;
	}

	public void setComplaintsId(int complaintsId) {
		this.complaintsId = complaintsId;
	}

	public void setComplaintsNum(String complaintsNum) {
		this.complaintsNum = complaintsNum;
	}

	public void setComplaintsContent(String complaintsContent) {
		this.complaintsContent = complaintsContent;
	}

	public void setComplaintsPicPath(String complaintsPicPath) {
		this.complaintsPicPath = complaintsPicPath;
	}

	public void setCompPersonId(int compPersonId) {
		this.compPersonId = compPersonId;
	}

	public String getCompTheme() {
		return compTheme;

	}

	public int getCompStatus() {
		return compStatus;
	}

	public int getComplaintsId() {
		return complaintsId;
	}

	public String getComplaintsNum() {
		return complaintsNum;
	}

	public String getComplaintsContent() {
		return complaintsContent;
	}

	public String getComplaintsPicPath() {
		return complaintsPicPath;
	}

	public int getCompPersonId() {
		return compPersonId;
	}

	public int adviseId;
	public String adviseNum;
	public String adviseContent;
	public DateTime releaseTime;
	public int advStatus;
	public String advTheme;
	public String advicePicPath;

	public String problemReportTheme;
	public int problemReportStatus;
	public int problemReportId;
	public String problemReportNum;
	public String problemReportContent;

	public void setProblemReportTheme(String problemReportTheme) {
		this.problemReportTheme = problemReportTheme;
	}

	public void setProblemReportStatus(int problemReportStatus) {
		this.problemReportStatus = problemReportStatus;
	}

	public void setProblemReportId(int problemReportId) {
		this.problemReportId = problemReportId;
	}

	public void setProblemReportNum(String problemReportNum) {
		this.problemReportNum = problemReportNum;
	}

	public void setProblemReportContent(String problemReportContent) {
		this.problemReportContent = problemReportContent;
	}

	public void setProblemReportPicPath(String problemReportPicPath) {
		this.problemReportPicPath = problemReportPicPath;
	}

	public void setProblemReportPersonId(int problemReportPersonId) {
		this.problemReportPersonId = problemReportPersonId;
	}

	public String problemReportPicPath;

	public String getProblemReportTheme() {
		return problemReportTheme;
	}

	public int getProblemReportStatus() {
		return problemReportStatus;
	}

	public int getProblemReportId() {
		return problemReportId;
	}

	public String getProblemReportNum() {
		return problemReportNum;
	}

	public String getProblemReportContent() {
		return problemReportContent;
	}

	public String getProblemReportPicPath() {
		return problemReportPicPath;
	}

	public int getProblemReportPersonId() {
		return problemReportPersonId;
	}

	public int problemReportPersonId; //上报人的Id

	public boolean isComp() {
		return complaintsId != 0;
	}

	public int getId() {
		return isComp() ? complaintsId : adviseId;
	}

	public int getStatus() {
		return isComp() ? compStatus : advStatus;
	}

	private final String[] STATUSES = new String[] { "待受理", "已受理", "已处理", "已评价" };

	public String getStatuss() {
		int s = getStatus();
		--s;
		if (s >= 0 && s < STATUSES.length) {
			return STATUSES[s];
		} else {
			return "未知";
		}
	}

	public String getNum() {
		return isComp() ? complaintsNum : adviseNum;
	}

	public DateTime getDateTime() {
		return releaseTime;
	}

	public String getContent() {
		return StrUtils.trimString(isComp() ? complaintsContent : adviseContent);
	}

	public String getTheme() {
		return isComp() ? compTheme : advTheme;
	}

	public String getPicPath() {
		return isComp() ? complaintsPicPath : advicePicPath;
	}
}
