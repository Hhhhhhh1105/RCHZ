package com.zju.rchz.utils;

import com.zju.rchz.R;

/**
 * 资源转换
 * 
 * @author Robin
 * 
 */
public class ResUtils {
	public static final int QUALITY_SMALL_IMGS[] = new int[] { R.drawable.im_quality_small_1, R.drawable.im_quality_small_2, R.drawable.im_quality_small_3, R.drawable.im_quality_small_4, R.drawable.im_quality_small_5, R.drawable.im_quality_small_6 };

	public static int getQuiltySmallImg(int qt) {
		--qt;
		if (qt >= 0 && qt < QUALITY_SMALL_IMGS.length)
			return QUALITY_SMALL_IMGS[qt];
		else
			return R.drawable.im_quality_small_error;
	}

	public static final int QUALITY_IMGS[] = new int[] { R.drawable.im_quality_1, R.drawable.im_quality_2, R.drawable.im_quality_3, R.drawable.im_quality_4, R.drawable.im_quality_5, R.drawable.im_quality_6 };

	public static int getQuiltyImg(int qt) {
		--qt;
		if (qt >= 0 && qt < QUALITY_IMGS.length)
			return QUALITY_IMGS[qt];
		else
			return R.drawable.im_quality_error;
	}

	// public static final int RIVER_LEVELS[] = new int[] {
	// R.string.level_river_1, R.string.level_river_2, R.string.level_river_3,
	// R.string.level_river_4 };
	//
	// public static int getRiverLevel(int lv) {
	// --lv;
	// if (lv >= 0 && lv < RIVER_LEVELS.length)
	// return RIVER_LEVELS[lv];
	// else
	// return R.string.utils_str_err;
	// }
	//
	// public static final int RIVER_LITTLE_LEVELS[] = new int[] {
	// R.string.level_1, R.string.level_2, R.string.level_3, R.string.level_4 };
	//
	// public static int getRiverLittleLevel(int lv) {
	// --lv;
	// if (lv >= 0 && lv < RIVER_LITTLE_LEVELS.length)
	// return RIVER_LITTLE_LEVELS[lv];
	// else
	// return R.string.utils_str_err;
	// }

	public static final int WEEKS[] = new int[] { R.string.week_0, R.string.week_1, R.string.week_2, R.string.week_3, R.string.week_4, R.string.week_5, R.string.week_6 };

	public static int getWeek(int i) {
		if (i >= 0 && i < WEEKS.length)
			return WEEKS[i];
		else
			return R.string.utils_str_err;
	}

	public static final int QUALITY_COLORS[] = new int[] { R.color.quality_1, R.color.quality_2, R.color.quality_3, R.color.quality_4, R.color.quality_5, R.color.quality_6 };

	public static int getQuiltyColor(int qt) {
		--qt;
		if (qt >= 0 && qt < QUALITY_COLORS.length) {
			return QUALITY_COLORS[qt];
		} else {
			return R.color.utils_color_err;
		}
	}

	public static final int RIVER_S_LEVELS[] = new int[] { R.string.level_river_1,R.string.level_river_2,R.string.level_river_3, R.string.level_river_4 };

	public static int getRiverSLevel(int lv) {
		--lv;
		if (lv >= 0 && lv < RIVER_S_LEVELS.length)
			return RIVER_S_LEVELS[lv];
		else
			return R.string.utils_str_err;
	}

	public static final String NPC_TITLE[] = new String[] { "乡镇代表", "区级代表", "市级代表" };

	public static String getNpcTitle(int authority) {
		authority = authority - 21;
		if (authority >= 0 && authority < NPC_TITLE.length)
			return NPC_TITLE[authority];
		else
			return " ";
	}

	public static final int RIVER_S_LITTLE_LEVELS[] = new int[] { R.string.level_1, R.string.level_2, R.string.level_3, R.string.level_4 };

	public static int getRiverSLittleLevel(int lv) {
		--lv;
		if (lv >= 0 && lv < RIVER_S_LITTLE_LEVELS.length)
			return RIVER_S_LITTLE_LEVELS[lv];
		else
			return R.string.utils_str_err;
	}

	/*
	 * public static final int SECTION_LEVELS[] = new int[] {
	 * R.string.section_main, R.string.section_important, R.string.section_admin
	 * };
	 * 
	 * public static int getSectionLevel(int lv) { --lv; if (lv >= 0 && lv <
	 * SECTION_LEVELS.length) return SECTION_LEVELS[lv]; else return
	 * R.string.utils_str_err; }
	 */
	public static final int SECTION_C_LEVELS[] = new int[] { R.string.section_main, R.string.section_important, R.string.section_admin };

	public static int getSectionCLevel(int lv) {
		--lv;
		if (lv >= 0 && lv < SECTION_C_LEVELS.length)
			return SECTION_C_LEVELS[lv];
		else
			return R.string.utils_str_err;
	}

	public static final int HANDLE_STATUSES[] = new int[] { R.string.handle_status1, R.string.handle_status2, R.string.handle_status3, R.string.handle_status4, R.string.handle_status5, R.string.handle_status6, R.string.handle_status7, R.string.handle_status8, R.string.handle_status9 };

	public static final int HANDLE01_STATUSES[] = new int[] {R.string.handle01_status0,R.string.handle01_status20,R.string.handle01_status21,R.string.handle01_status30,R.string.handle01_status40,R.string.handle01_status41
			,R.string.handle01_status50,R.string.handle01_status60};

	public static int getHandleStatus(int lv) {
		--lv;
		if (lv >= 0 && lv < HANDLE_STATUSES.length)
			return HANDLE_STATUSES[lv];
		else
			return R.string.utils_str_err;
	}
//得到已处理还是未处理  两种状态
	public static int getHandleStatus01(int lv) {
		--lv;
		if (lv >= 0 && lv < HANDLE01_STATUSES.length)
			return HANDLE01_STATUSES[lv];
		else
			return R.string.utils_str_err;
	}
}
