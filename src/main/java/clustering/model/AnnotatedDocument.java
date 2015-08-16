package clustering.model;

import java.util.List;

import ru.itbrains.gate.morph.MorphInfo;

import com.textocat.api.sdk.model.Document;

public class AnnotatedDocument extends ParsedDocument {
	private Object[] annotationResults;
	
	private AnnotatedDocument(Document d, List<MorphInfo> morhInfo) {
		super(d, morhInfo);
	}

	public AnnotatedDocument(ParsedDocument doc) {
		super(doc, doc.getMorphInfo());
	}
	
	
}
