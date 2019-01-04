package com.zju.rchz.model;

import com.zju.rchz.activity.BaseActivity;

public class RiverRecord {
	public int recordId;
	public String recordSerNum;
	public DateTime recordDate;

	public int recordPersonAuthority; //巡河人的权限
	public long recordPersonId; //巡河人的userId

	public int flotage; // 河面是否存在漂浮物
	public String flotages; // 具体描述
	public int rubbish; // 河岸是否存在垃圾
	public String rubbishs; // 具体描述
	public int building; // 河岸是否存在违章建筑
	public String buildings; // 具体描述
	public int sludge; // 河底是否存在明显淤泥
	public String sludges; // 具体描述
	public int odour; // 水体是否存在臭味
	public String odours; // 具体描述
	public int outfall; // 水体是否存在排污口
	public String outfalls; // 具体描述
	public int riverinplace; // 保洁机制是否到位
	public String riverinplaces; // 具体描述
	public String otherquestion; // 其他问题
	public String deal; // 处理情况

	public String latlist;//显示轨迹纬度数组
	public String lnglist;//显示轨迹经度数组
	public Double longitude;
	public Double latitude;
	public String imgLnglist;//图片经纬度
	public String imgLatlist;

	public String picPath;	// 图片
	
	public int riverId; // 巡查的河道Id
	public String recordPersonName;
	public String recordRiverName;
	public int recordPersonAuth; //巡河人的等级
	public String isCorrect;//有效性判断 ，1表示有效，0表示无效
	public String judgeReason;//无效时的原因

	public String patrolLength;//巡河长度
	public String patrolTime;//巡河时长

	public String getIsCorrect() {
		return isCorrect;
	}

	public void setIsCorrect(String isCorrect) {
		this.isCorrect = isCorrect;
	}

	// local
	public River locRiver;
	public String locRiverName;

	public String getYMD2() {
		return recordDate != null ? recordDate.getYMD2(BaseActivity.getCurActivity()) : "";
	}

	public String getYMDHM() {
		return recordDate != null ? recordDate.getYMDHM(BaseActivity.getCurActivity()) : "";
	}

	public String[] recordPersonAuthStr = {"村级", "镇街级", "区级", "市级"};

	public String getRecordPersonAuthStr() {
		switch (recordPersonAuth) {
			case 2:
				return recordPersonAuthStr[1];
			case 8:
				return recordPersonAuthStr[0];
			case 9:
				return recordPersonAuthStr[2];
			case 10:
				return recordPersonAuthStr[3];
			default:
				return "";
		}
	}
}
