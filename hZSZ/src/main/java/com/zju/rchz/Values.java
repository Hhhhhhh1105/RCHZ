package com.zju.rchz;

import com.zju.rchz.model.District;

public class Values {
	final public static String Ver = "2.1.5";
//	final public static String Ver = "1.3.2";
	final public static boolean DEBUG = false;
	final public static boolean RELEASE = false;
	public static String LastVer = null;

	public static long lastAuthCodeTime = 0;

	final public static int AUTHCODE_GAP_S = 60;

	public static District[] districtLists;

//	final public static String SMS_APPKEY = "7d9145a2169c";
	final public static String SMS_APPKEY = "1f2ec2b450534";
//	final public static String SMS_SECKEY = "dc1872330efab0b854bd7d7cc95a76f2";
	final public static String SMS_SECKEY = "d641cbbb4d110f0abc8fe82d80d54605";

//	private static String[] WXAppIDs = new String[] { "wx7c34b978e3add950", "wxd0c565c230eb7625" };
//	private static String[] WXAppSecrets = new String[] { "bb00b86efe44f194380d40eb001cd0b4", "341433d103a191072579fa7bb4eec4a4" };
	private static String[] WXAppIDs = new String[] { "wxd0c565c230eb7625", "wxd0c565c230eb7625" };
	private static String[] WXAppSecrets = new String[] { "341433d103a191072579fa7bb4eec4a4", "341433d103a191072579fa7bb4eec4a4" };

	private static int ix = 1;

	public static int UPLOAD_IMG_W = 400;
	public static int UPLOAD_IMG_H = 400;

	public static int MAP_ZOOM_LEVEL = 16;
	public static int MAP_ZOOM_MAX_LEVEL = 20;
	public static int MAP_ZOOM_MIN_LEVEL = 14;

	public static int getIx() {
		return RELEASE ? 1 : ix;
	}

	public static String getWXAppID() {
		return WXAppIDs[getIx()];
	}

	public static String getWXAppSecret() {
		return WXAppSecrets[getIx()];
	}

	//SystemParameters
	public static int tourriver;
	public static int dialmobile;
	public static int timesOfRiverTour;
}
