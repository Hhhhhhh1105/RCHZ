package com.zju.rchz.model;

public class CompDataRes extends TObjectRes<CompData> {

	@Override
	public boolean isSuccess() {
		return super.isSuccess() && data != null;
	}

}
