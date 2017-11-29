package com.zju.rchz.model;

public class RiverLocationsRes extends TObjectRes<RiverLocations> {
	@Override
	public boolean isSuccess() {
		return super.isSuccess() && this.data != null && this.data.riverLocations != null;
	}
}
