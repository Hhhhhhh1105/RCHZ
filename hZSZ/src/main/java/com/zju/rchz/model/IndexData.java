package com.zju.rchz.model;

public class IndexData {
	public Section[] sectionJsons;
	public River[] riverJsons;
	public District[] districtLists;
	public String sloganPicPath;
	public Industrialport[] industrialportlists;
	public SmallWater[] smallWaterSums;
	public Npc[] deputiesJsons; //人大代表
	public Lake[] lakeSumJsons; //湖泊水质
	public int tourriver; //5min内能否结束巡河 默认0 0：不能结束  1：能结束
	public int dialmobile;  //是否显示河长电话  默认0  0：显示 1：不显示
	public int timesOfRiverTour;//每天的巡河次数最多几次
}
