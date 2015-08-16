package clustering.annotating_test;

import org.junit.Assert;
import org.junit.Test;

import clustering.Resources;
import clustering.annotating.DocumentAnnotator;
import clustering.annotating.TextocatAnnotator;

import com.textocat.api.sdk.model.AnnotatedDocument;

public class TextocatAnnotatorTest {
	@Test
	public void testAnnotate() {
		DocumentAnnotator annotator = new TextocatAnnotator();
		Object[] annotatorResult = annotator.annotate(Resources.getExpected());
		
		//проверяем не полуили ли мы null и все ли документы проаннотированы
		boolean res = (annotatorResult != null);
		AnnotatedDocument[] annotatedDocuments = (AnnotatedDocument[])annotatorResult;
		for (AnnotatedDocument d : annotatedDocuments) {
			res = res && (d != null) && (d.getEntities() != null);
		}
		
		Assert.assertTrue(res);
	}
}
