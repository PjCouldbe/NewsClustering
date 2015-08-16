package clustering.preparing;

import java.util.List;

public class DataCleaner {
	/*private static String monthsAndDays = "\\d\\d "
			+ "((январ|феврал|апрел|ма)я|(мар|авгус)та|ию[нл]я|(сентя|октя|ноя|дека)бря)";
	private static String dates = "^"
					+ monthsAndDays
					+ " \\d?\\d?\\d\\d, "
					+ "\\d\\d:\\d\\d"
					+ ".*"
					+ "$"; 
	*/
	
	public static List<String> cleanData(List<String> data, String externalMask, 
					String internalMask, String internalReplacement) {
		if (externalMask != null && externalMask.length() > 0) {
			data = cleanData(data, externalMask);
		}
		if (internalMask != null && internalMask.length() > 0) {
			data = cleanData(data, internalMask, internalReplacement);
		}
		
		return data;
	}
	
	//cleaning with only external mask
	private static List<String> cleanData(List<String> data, String externalMask) {
		int i = 0;
		while (i < data.size()) {
			if (!data.get(i).matches(externalMask)) {
				data.remove(i);
			}
		}
		
		return data;
	}
	
	//cleaning with only internal mask
	private static List<String> cleanData(List<String> data, String internalMask, String replacement) {
		if (replacement == null) {
			replacement = "";
		}
		
		for (String s : data) {
			s.replaceAll(internalMask, replacement);
		}
		return data;
	}
}