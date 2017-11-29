package com.zju.rchz.model;

public class RiverQualityDataRes extends TObjectRes<RiverQualityData> {

	@Override
	public boolean isSuccess() {
		return super.isSuccess() && data.indexValues != null;
	}

}
