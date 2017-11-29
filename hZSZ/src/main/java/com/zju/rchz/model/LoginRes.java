package com.zju.rchz.model;

import com.zju.rchz.utils.StrUtils;

public class LoginRes extends TObjectRes<User> {
	@Override
	public boolean isSuccess() {
		return super.isSuccess() && data != null && !(StrUtils.isNullOrEmpty(data.uuid));
	}
}
