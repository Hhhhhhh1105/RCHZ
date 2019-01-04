package com.zju.rchz.model;

public class NewsDataRes extends TObjectRes<NewsData> {
//	public NewsData data;

	@Override
	public boolean isSuccess() {
		return super.isSuccess() && data != null;
	}
}
