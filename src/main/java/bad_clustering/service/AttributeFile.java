package clustering.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import clustering.service.attributes.MyAttribute;

@SuppressWarnings("serial")
public class AttributeFile extends File{
	private final static String extension = ".arff";
	private static int serialNumber = 1;
	private PrintWriter writer;
	
	private LinkedList<MyAttribute> attributes;
	
	private boolean attributesAreFinished;
	private boolean dataIsFinished;
	
	public AttributeFile(String location) { 
		super(location + "\\myAtributefile" + serialNumber + extension);
		serialNumber++;
		try {
			this.createNewFile();
			writer = new PrintWriter(new FileWriter(this), true);
			addRelationDeclaration();
		} catch (IOException e) {
			e.printStackTrace();
		}
		attributes = new LinkedList<>();
		attributesAreFinished = false;
		dataIsFinished = false;
	}
	
	protected void addRelationDeclaration() {
		writer.println("@relation " + this.getName().substring(0, this.getName().length() - 5) + "\n");
	}
	
	public boolean addAtributeDeclaration(MyAttribute attribute) {
		if (attributesAreFinished) {
			return false;
		}
		writer.println("@attribute " + attribute.getName() + " " + attribute.getType());
		attributes.add(attribute);
		return true;
	}
	
	public boolean fillFile(ArrayList<String> data, MyAttribute... myAttributes) {
		if (myAttributes == null || myAttributes.length == 0) {
			throw new IllegalArgumentException(
					"Needs the classifying attributes to make valid .arff file");
		}
		for (MyAttribute attr : myAttributes) {
			addAtributeDeclaration(attr);
		}
		setAttributesFinished();
		addData(data);
		setDataFinished();
		return isFinished();
	}
	
	public boolean addData(ArrayList<String> data) {
		if (!attributesAreFinished | dataIsFinished) {
			return false;
		}
		
		for (int i = 0; i < data.size(); i++) {
			StringBuilder s = new StringBuilder();
			for (int j = 0; j < attributes.size(); j++) {
				if ((new StringTokenizer(data.get(i))).countTokens() <= 0) {
					break;
				}
				s.append(attributes.get(j).instantiateDataValue(data, i));
				if (j != attributes.size() - 1) {
					s.append(",");
				}
			}
			if (s.length() > 0) {
				writer.print(s);
				if (i != data.size() - 1) {
					writer.println();
				}
			}
		}
		return true;
	}
	
	public void setAttributesFinished() {
		attributesAreFinished = true;
		writer.println("\n@data");
	}
	
	public void setDataFinished()  {
		dataIsFinished = true;
	}
	
	public boolean isFinished() {
		return attributesAreFinished & dataIsFinished;
	}
}