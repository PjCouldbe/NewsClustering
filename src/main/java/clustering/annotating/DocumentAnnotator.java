package clustering.annotating;

import java.util.List;

public abstract class DocumentAnnotator {
	protected AnnotatedWith annotator = null;
	public static enum AnnotatedWith {
		TEXTOCAT, UIMA, EVENTS;
	}
	
	abstract public Object[] annotate(final List<String> data);
	
	abstract public Object annotate(final String data);
}
