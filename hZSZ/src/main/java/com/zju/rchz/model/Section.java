package com.zju.rchz.model;

/**
 * 断面
 * 
 * @author Robin
 * 
 */
public class Section {
	public int sectionId;
	public int waterType;
	public int sectionType;
	public String sectionName;
	public DateTime uploadTime;
	public String picPath;

	public SectionIndex[] indexDataJson;
	public IndexValue[] indexValues;

	public float longititude; /* 经度 */
	public float latitude; /* 纬度 */

//	public String getLevelName(Context context) {
//		return StrUtils.renderText(context, R.string.fmt_section_level, context.getString(ResUtils.getRiverLittleLevel(sectionType)));
//	}
}
