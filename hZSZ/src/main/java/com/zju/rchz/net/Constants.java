package com.zju.rchz.net;

/**
 * 通信常量定义
 * 
 * @author Robin+
 * 
 */
public class Constants {

	public static final String SerUrl1 = "http://115.159.114.127:8080"+"/UniiedPlatform/background/app.htm"; // test
	public static final String SerUrl2 = "http://123.206.204.153:8080"+"/RongChengHeZhang/background/app.htm"; // release
	public static final String SerUrl3 = "http://192.168.100.250:8080"; // 本地调试
	public static final String rongChengHeZhang = "/RongChengHeZhang/background/app.htm";
	public static final String uniiedPlatform = "/UniiedPlatform/background/app.htm";


	public static final String SerUrl = SerUrl2;

//	public static final String ApiUrl = SerUrl + uniiedPlatform;
	public static final String ApiUrl = SerUrl ;
	//	public static final String ApiUrl = SerUrl1 + "/gzhd/background/app.htm";
	public static final String AppKey = "10000";
	public static final String AppSecret = "de85ac6fa475a0391d8c2e4e7413760";

	public static final int DefaultPageSize = 20;
}
