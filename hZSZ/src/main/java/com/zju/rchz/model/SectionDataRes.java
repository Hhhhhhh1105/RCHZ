package com.zju.rchz.model;

public class SectionDataRes extends BaseRes {
	public Section data;

	@Override
	public boolean isSuccess() {
		return super.isSuccess() && data != null && data != null;
	}
}
