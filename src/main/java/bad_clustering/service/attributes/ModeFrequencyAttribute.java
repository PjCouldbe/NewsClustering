package clustering.service.attributes;

import java.util.List;

public class ModeFrequencyAttribute extends MyAttribute{
	private ModeAttribute attr;
	
	public ModeFrequencyAttribute(String name) {
		super(name);
		type = "INTEGER";
	}
	
	public ModeFrequencyAttribute(String name, ModeAttribute attr) {
		super(name);
		type = "INTEGER";
		this.attr = attr;
	}
	
	@Override
	public Object instantiateDataValue(List<String> data, int num) {
		if (attr.getMaxCount() == 0) {
			throw new IllegalStateException();
		}
		return attr.getMaxCount();
	}
}