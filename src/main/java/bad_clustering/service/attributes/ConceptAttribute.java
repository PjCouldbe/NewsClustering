package clustering.service.attributes;

import java.util.List;

import clustering.StoreData;
import clustering.service.concepts.ConceptedDocument;

public class ConceptAttribute extends MyAttribute {
	private int conceptNumber;
	
	public ConceptAttribute(String name) {
		super(name);
	}
	
	public ConceptAttribute(String name, int conceptNumber) {
		super(name);
		this.conceptNumber = conceptNumber;
	}
	
	@Override
	public Object instantiateDataValue(List<String> data, int num) {
		ConceptedDocument cDoc = StoreData.conceptedDocuments[num];
		if (cDoc == null) {
			return 0.0;
		}
		
		for (int i = 0; i < cDoc.getConceptNumbers().length; i++) {
			if (conceptNumber == cDoc.getConceptNumbers()[i]) {
				return cDoc.getTf()[i];
			}
		}
		return 0.0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ConceptAttribute)) {
			return false;
		}
		ConceptAttribute tmp = (ConceptAttribute)obj;
		return this.getName().equals(tmp.getName()) && this.conceptNumber == tmp.conceptNumber;
	}
}
