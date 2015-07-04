package clustering;

import java.util.List;

import com.google.common.util.concurrent.FutureCallback;
import com.textocat.api.sdk.EntityRecognition;
import com.textocat.api.sdk.TextocatFactory;
import com.textocat.api.sdk.model.AnnotatedBatch;
import com.textocat.api.sdk.model.Batch;
import com.textocat.api.sdk.model.BatchMetadata;
import com.textocat.api.sdk.model.Document;

public class DocumentAnnotator {
	//final EntityRecognition entityRecognition = null;
	int counter = -1;
	
	public DocumentAnnotator(String key, final List<String> data) {
		final EntityRecognition entityRecognition = 
				TextocatFactory.getEntityRecognitionInstance(key);
		final FutureCallback<AnnotatedBatch> outputCallback = new FutureCallback<AnnotatedBatch>() {
			@Override
			public void onSuccess(AnnotatedBatch result) {
				synchronized (StoreData.sync) {
					DFPreparer.annotatedDocuments = result.getDocuments();
					//DFPreparer.annotatedDocuments[counter] = result.getDocuments()[0];
					System.out.println("Документы проаннотированы! ");
					System.out.println("resurrected");
					StoreData.sync.notifyAll();
					/*if (counter >= StoreData.DOCS) {
						System.out.println("Документы проаннотированы! ");
						System.out.println("resurrected");
						StoreData.sync.notifyAll();
					}*/
				}
			}

			@Override
			public void onFailure(Throwable t) {
				t.printStackTrace();
				System.out.println(counter);
				//counter++;
			}
		};
		FutureCallback<BatchMetadata> inputCallback = new FutureCallback<BatchMetadata>() {
			public void onSuccess(BatchMetadata batchMetadata) {
				entityRecognition.retrieve(outputCallback, batchMetadata);
			}
			
			public void onFailure(Throwable t) {
				t.printStackTrace();
				System.out.println(counter);
			}
		};
		Document[] docs = new Document[data.size()];
		for (int i = 0; i < data.size(); i++) {
			docs[i] = new Document(data.get(i));
			
			/*counter++;
			Document[] doci = {docs[i]};
			entityRecognition.submit(new Batch(doci), inputCallback);*/
		}
		entityRecognition.submit(new Batch(docs), inputCallback);
	}
	
	/*public void annotate(final List<String> data) {
		//final AnnotatedDocument[] anDocs = new AnnotatedDocument[StoreData.DOCS]; 
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				final FutureCallback<AnnotatedBatch> outputCallback = new FutureCallback<AnnotatedBatch>() {
					@Override
					public void onSuccess(AnnotatedBatch result) {
						synchronized (StoreData.sync) {
							DFPreparer.annotatedDocuments = result.getDocuments();
							System.out.println("Документы проаннотированы! ");
							System.out.println("resurrected");
							StoreData.sync.notifyAll();
							if (counter >= 500) {
								System.out.println("resurrected");
								StoreData.sync.notifyAll();
							}
						}
					}

					@Override
					public void onFailure(Throwable t) {
						t.printStackTrace();
						System.out.println(counter);
						counter++;
					}
				};
				FutureCallback<BatchMetadata> inputCallback = new FutureCallback<BatchMetadata>() {
					@Override
					public void onSuccess(BatchMetadata result) {
						entityRecognition.retrieve(outputCallback, result);
					}

					@Override
					public void onFailure(Throwable t) {
						t.printStackTrace();
						System.out.println(counter);
						counter++;
					}
				};
				Document[] docs = new Document[data.size()];
				for (int i = 0; i < data.size(); i++) {
					counter++;
					if (counter== 482) {
						System.out.println(data.get(i));
					}
					docs[i] = new Document(data.get(i));
					Document[] doci = {docs[i]};
					entityRecognition.submit(new Batch(doci), inputCallback);
					//entityRecognition.submit(new Batch(docs), inputCallback);
					if (i > 480) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		try {
			t.start();
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}*/
}