package clustering.service.attributes;

import java.util.List;

public class MyAttribute {
	protected String name;
	protected String type;
	
	public MyAttribute(String name) {
		this.name = name;
		this.type = "NUMERIC";
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Object instantiateDataValue(List<String> data, int num) {
		return data;
	}
}