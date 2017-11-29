package com.zju.rchz.model;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class User {
	// public int userId = 3;
	public String userName = null;

	// public String userRealName = "未登录";
	public String uuid = null;

	public List<River> collections = null;
	public List<Integer> readNewsIds = null;
	public String statusCity = "";
	public int isLeader = 0;//是否 为报送 1是0否
	public int isLeaderDuban = 0;//是否是 领导督办功能块
	//用户的区划信息：普通用户为0，河长为对应区划
	public int districtId = 0;

	public int getDistrictId() {
		return districtId;
	}

	public String pwdmd5 = null;

	public boolean isLogined() {
		return uuid != null && uuid.length() > 0;
	}

	public List<River> getCollections() {
		if (collections == null) {
			collections = new ArrayList<River>();
		}
		return collections;
	}

	public boolean isLeader(){
		return isLeader==1;
	}
	public String getDisplayName() {
		if (isLogined()) {
			if (realName==""){
				return userName;
			}else {
				return realName;
			}
//			return userName;

		} else {
			return "未登录，请点击头像登录或注册";
		}
	}

	public void clearInfo() {
		// userName = null;
		uuid = null;
		pwdmd5 = null;
	}


	// 河长
	public int authority;
	public River[] riverSum = new River[] {};
	public int ifOnJob = 0;

	// 是否河长
	public boolean isChief() {
		return authority == 2 && ifOnJob == 0;
	}

	//是否是村级河长
	public boolean isVillageChief(){
		//村级河长的权限
		return authority == 8 && ifOnJob == 0;
	}
	//是否是河管员
	public boolean isCleaner(){
		//河管员的权限
		return authority == 7 && ifOnJob == 0;
	}
	public boolean isCoordinator(){
		//协管员权限
		return authority==6 && ifOnJob ==0;
	}
	public boolean isDucha(){
		//督察员权限
		return authority==13 && ifOnJob ==0;
	}

	//是否是区级河长
	public boolean isDistrictChief() {
		//区级河长的权限
		return authority == 9;
	}

	public boolean isCityChief() {
		//市级河长的权限

		return authority == 10 && statusCity.equals("1");
	}
	public boolean isCityLinkMan(){
		return authority == 10 && statusCity.equals("3");
	}

	//是否是人大代表
	public boolean isNpc() {
		return authority >= 20;
	}

	//河长权限
	public int getAuthority() {
		return authority;
	}

	public void setAuthority(int authority) {
		this.authority = authority;
	}

	//获取河长
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	// end 河长

	public String getDisplayRiver() {
		if (isLogined() && isChief()) {
			if (riverSum == null || riverSum.length == 0) {
				return "";
			} else if (riverSum.length == 1) {
				return "负责河道:" + riverSum[0].riverName;
			} else {
				return "负责河道:" + riverSum[0].riverName + "等" + riverSum.length + "条河道";
			}
		} else {
			return "";
		}

	}

	//用于人大代表进入“我的河道”界面
	public int getMyRiverId() {
		if (isLogined() && isNpc()) {
			if (riverSum == null || riverSum.length == 0) {
				return 0;
			} else if (riverSum.length == 1) {
				return riverSum[0].riverId;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}


	//人大-显示人大姓名
	public String realName;

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}


	//百度坐标-暂存
	public String baiduLatPoints = "";
	public String baiduLngPoints = "";

	public String getBaiduLatPoints() {
		return baiduLatPoints;
	}

	public void setBaiduLatPoints(String baiduLatPoints) {
		this.baiduLatPoints = baiduLatPoints;
	}

	public String getBaiduLngPoints() {
		return baiduLngPoints;
	}

	public void setBaiduLngPoints(String baiduLngPoints) {
		this.baiduLngPoints = baiduLngPoints;
	}


	public List<Integer> getReadNewsIds() {
		if (readNewsIds == null)
			readNewsIds = new ArrayList<Integer>();
		return readNewsIds;
	}

	public void readySave() {
		int max = 100;
		if (readNewsIds != null && readNewsIds.size() > (max * 2)) {
			List<Integer> list = new ArrayList<Integer>();
			int size = readNewsIds.size();
			for (int i = size - max; i < size; ++i) {
				list.add(readNewsIds.get(i));
			}
			readNewsIds = list;
		}
	}

	public boolean gpsdisable = false;
	public boolean notifyable = true;

	public boolean isNotifyable() {
		return this.isLogined() && notifyable;
	}

	public void setNotifyable(boolean ab) {
		notifyable = ab;
	}
}
