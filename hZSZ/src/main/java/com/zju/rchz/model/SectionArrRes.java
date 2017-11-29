package com.zju.rchz.model;

public class SectionArrRes extends TArrayRes<Section> {

	@Override
	public boolean isSuccess() {
		return super.isSuccess() && data.length > 0;
	}
}
