package clustering.service.attributes;

import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import weka.core.stemmers.SnowballStemmer;

public class ModeAttribute extends MyAttribute {
	private String maxElement = null;
	private int maxCount = 0;
	
	public ModeAttribute(String name) {
		super(name);
		type = "STRING";
	}

	@Override
	public Object instantiateDataValue(List<String> data, int num) {
		SnowballStemmer stemmer = new SnowballStemmer();
		stemmer.setStemmer("russian");
		StringTokenizer st = new StringTokenizer(data.get(num).toLowerCase(), " \n\r\t\f\\d\'\"_-");
		
		HashMap<String, Integer> stemOccurenceMap = new HashMap<>();
		while (st.hasMoreTokens()) {
			String key = stemmer.stem(st.nextToken());
			Integer oldValue = stemOccurenceMap.get(key);
			stemOccurenceMap.put(key, 
					(oldValue == null ? 0 : oldValue + 1));
			if (maxElement == null) {
				maxElement = key;
				maxCount = 1;
			} else if (maxCount == oldValue) {
				maxElement = key;
				maxCount++;
			}
		}
		
		return maxElement;
	}
	
	public String getMaxElement() {
		return maxElement;
	}
	
	public int getMaxCount() {
		return maxCount;
	}
}