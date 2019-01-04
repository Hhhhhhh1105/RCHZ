package com.zju.rchz.model;

public class SectionListDataRes extends BaseRes {
	public SectionListData data;

	@Override
	public boolean isSuccess() {
		return super.isSuccess() && data != null && data.sectionJsons != null;
	}
}
