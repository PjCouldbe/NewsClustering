package clustering.model;

import java.util.List;

import ru.itbrains.gate.morph.MorphInfo;

import com.textocat.api.sdk.model.AnnotatedDocument;
import com.textocat.api.sdk.model.Document;
import com.textocat.api.sdk.model.EntityAnnotationCategory;

public class EntitiedDocument extends ParsedDocument {
	MyEntityAnnotation[] entities;
	private static class MyEntityAnnotation {
		private int beginOffset;
		private int endOffset;
		private EntityAnnotationCategory category;
		
		//Проверять endOffset на выход за пределы document.length() нужно самостоятельно 
		//до вызова конструктора!
		public MyEntityAnnotation(int beginOffset, int endOffset, EntityAnnotationCategory category) {
			if (beginOffset < 0) {
				throw new IllegalArgumentException(" Beginning of the entity can't be negative! ");
			}
			if (endOffset <= beginOffset) {
				throw new IllegalArgumentException( "The end of entity can't be before its beginning! "
						+ "Maybe you should swap the arguments." );
			}
			if (category == null) {
				throw new IllegalArgumentException( "The annotation category must be meaningful, "
						+ "not null!" );
			}
			
			this.beginOffset = beginOffset;
			this.endOffset = endOffset;
			this.category = category;
		}
		
		public int getBeginOffset() {
			return beginOffset;
		}
		
		public int getEndOffset() {
			return endOffset;
		}
		
		public EntityAnnotationCategory getCategory() {
			return category;
		}
	}
	
	private EntitiedDocument(Document d, List<MorphInfo> morhInfo) {
		super(d, morhInfo);
	}

	public EntitiedDocument(ParsedDocument doc, AnnotatedDocument atd) {
		super(doc, doc.getMorphInfo());
	}
	
	
}
