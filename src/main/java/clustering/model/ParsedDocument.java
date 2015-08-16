package clustering.model;

import java.util.List;

import ru.itbrains.gate.morph.MorphInfo;

import com.textocat.api.sdk.model.Document;

public class ParsedDocument extends Document {
	private List<MorphInfo> morphInfo;
	
	private ParsedDocument(String text) {
		super(text);
	}
	
	public ParsedDocument(Document d, List<MorphInfo> morhInfo) {
		super(d.getText(), d.getTag());
		this.morphInfo = morhInfo;
	}
	
	public List<MorphInfo> getMorphInfo() {
		return morphInfo;
	}
}
