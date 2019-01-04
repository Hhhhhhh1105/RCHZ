package com.zju.rchz.utils;

import java.util.HashMap;
import java.util.Map;

public class ValUtils {
	private static Map<String, String[]> valMap = null;

	public static String[] getYVals(String ename) {
		if (valMap == null) {
			valMap = new HashMap<String, String[]>();
			valMap.put("DO", new String[] { "12", "7.5", "6", "5", "3", "2", "0" });
			valMap.put("TP", new String[] { "5.0", "2.0", "1.5", "1.0", "0.8", "0.5", "0" });
			valMap.put("NH3N", new String[] { "20.0", "7.0", "3.0", "1.0", "0.2", "0.05", "0" });
			valMap.put("CODMn", new String[] { "800", "100", "75", "50", "25", "10", "0" });

			valMap.put("NO", new String[] { "-", "-", "-", "-", "-", "-", "-" });
		}
		if (valMap.containsKey(ename))
			return valMap.get(ename);
		else
			return valMap.get("NO");
	}
}
