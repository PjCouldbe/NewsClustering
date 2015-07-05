package clustering;

import java.util.ArrayList;
import java.util.List;

import ru.itbrains.gate.morph.MorphParser;
import clustering.service.concepts.Concept;
import clustering.service.concepts.ConceptedDocument;

public class StoreData {
	public static final int DOCS = 50;
	public static ConceptedDocument[] conceptedDocuments;
	public static List<Concept> concepts = new ArrayList<>(100);
	static final String PATH = "src\\main\\resources";
	public static final MorphParser PARSER = new MorphParser();
	public static final Object sync = new Object();
}
