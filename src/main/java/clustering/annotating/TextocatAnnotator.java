package clustering.annotating;

import java.util.LinkedList;
import java.util.List;

import com.google.common.util.concurrent.FutureCallback;
import com.textocat.api.sdk.EntityRecognition;
import com.textocat.api.sdk.TextocatFactory;
import com.textocat.api.sdk.model.AnnotatedBatch;
import com.textocat.api.sdk.model.AnnotatedDocument;
import com.textocat.api.sdk.model.Batch;
import com.textocat.api.sdk.model.BatchMetadata;
import com.textocat.api.sdk.model.Document;

public class TextocatAnnotator extends DocumentAnnotator {
	{
		annotator = AnnotatedWith.TEXTOCAT;
	}
	
	private static class MyFutureCallback implements FutureCallback<AnnotatedBatch> {
		private AnnotatedBatch result;
		private static final Object sync = new Object();
		
		public MyFutureCallback() {
			result = null;
		}
		
		@Override
		public void onSuccess(AnnotatedBatch result) {
			this.result = result;
		}

		@Override
		public void onFailure(Throwable t) {
			
		}
		
		public AnnotatedDocument[] getResult() {
			synchronized (sync) {
				if (result == null) {
					try {
						sync.wait();   //дождёмся получения результата из submit() 
						               //в InputCallBack
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			return result.getDocuments();
		}
	}

	final EntityRecognition entityRecognition;
	int counter = -1;
	final static Object sync = new Object();
		
	public TextocatAnnotator() {
		entityRecognition = TextocatFactory.
				getEntityRecognitionInstance("d516188545411ff4331256dddf9b3376");
	}
	
	@Override
	public Object[] annotate(final List<String> data) {
		final FutureCallback<AnnotatedBatch> outputCallback = new MyFutureCallback();
		
		FutureCallback<BatchMetadata> inputCallback = new FutureCallback<BatchMetadata>() {
			public void onSuccess(BatchMetadata batchMetadata) {
				synchronized (sync) {   //ждём здесь, когда получим результат
					entityRecognition.retrieve(outputCallback, batchMetadata);
				}
			}
			
			public void onFailure(Throwable t) {
				t.printStackTrace();
				System.out.println(counter);
			}
		};
		
		//для Tetocat нужен формат входных данных в виде Document[]
		Document[] docs = new Document[data.size()];    
		for (int i = 0; i < data.size(); i++) {
			docs[i] = new Document(data.get(i));
			
			/*counter++;
			Document[] doci = {docs[i]};
			entityRecognition.submit(new Batch(doci), inputCallback);*/
		}
		
		entityRecognition.submit(new Batch(docs), inputCallback);
			
		return ((MyFutureCallback) outputCallback).getResult();
	}

	@Override
	public Object annotate(String data) {
		List<String> list = new LinkedList<>();
		list.add(data);
		AnnotatedDocument[] result = (AnnotatedDocument[])annotate(list);
		
		return result[0];
	}
}