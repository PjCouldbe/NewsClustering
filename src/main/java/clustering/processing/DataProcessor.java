package clustering.processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import ru.itbrains.gate.morph.MorphInfo;
import ru.itbrains.gate.morph.MorphParser;

import com.textocat.api.sdk.model.Document;

import clustering.annotating.DocumentAnnotator;
import clustering.annotating.DocumentAnnotator.AnnotatedWith;
import clustering.annotating.EventsAnnotator;
import clustering.annotating.TextocatAnnotator;
import clustering.model.AnnotatedDocument;
import clustering.model.ParsedDocument;

public class DataProcessor {
	private boolean[] annotators = new boolean[AnnotatedWith.values().length]; 
	private List<Document> data;  //null'ов в составе быть не должно уже на этапе конструктора!!!
	
	private static enum ReadyStatus {
		NONE, PARSED, ANNOTATED, CONCEPTED
	}
	private ReadyStatus status = ReadyStatus.NONE;
	
	public DataProcessor(List<String> data) {
		this.data = new ArrayList<>( data.size() );
		
		for (String s: data) {
			this.data.add( new Document(s) );
		}
	}
	
	public boolean parse() {
		MorphParser parser = new MorphParser();
		
		//отдельный список для обработанных данных
		List<Document> parsedData = new ArrayList<>(data.size());  
		
		for (Document d : data) {
			try {
				//отдельный список сделан для безопасности: если где-то в середине произойдёт ошибка, 
				//действие должно откатиться полностью, чтобы не было частичной обработки
				parsedData.add( new ParsedDocument(d, parser.runParser(d.getText(), "UTF-8")) );
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		data = parsedData;
		status = ReadyStatus.PARSED;
		return true;
	}
	
	public Object annotate(AnnotatedWith ...annotators) {
		final List<Document> tempData = new ArrayList<>( data.size() );
		final Object dataSync = new Object();
		
		int threadCount = annotators.length;   //для параллельной обработки сосчитаем, 
		Thread t = null;                       //сколько потоков нам понадобится (как минимум столько,
		                                       //сколько аннотаторов указано в аргументах)
		if (status != ReadyStatus.PARSED) {    //если документы ещё не распарсены, это придётся исправлять
			threadCount++;
			
			t = new Thread(new Runnable() { //поток парсера
				@Override
				public void run() {
					MorphParser parser = new MorphParser();
					
					for (Document d : data) {
						synchronized (dataSync) {
							Document newDoc = new ParsedDocument(d, parse(parser, d)); 
							if (d != null) {     //будет null, если ошибка
								tempData.add(d);
								dataSync.notifyAll();
							} else {
								dataSync.notifyAll();
								//TODO: все потоки должны быть остановлены!!!
								return;
							}
														
						}
					}	
				}
			});
		}
		
		Thread[] threads = new Thread[threadCount];
		int i = 0;
		if (t != null) { //если поток парсера инициализирован
			threads[0] = t;
			i = 1;
		}
		
		//простой способ инициализировать все нужные аннотаторы, заодно и от повторений избавляемся
		Map<AnnotatedWith, DocumentAnnotator> map = new HashMap<>(); 
		for (AnnotatedWith ann : annotators) {
			if (ann != null) {
				DocumentAnnotator da;
				switch (ann) {
					case TEXTOCAT:
						da = new TextocatAnnotator();
						break;
					case EVENTS:
						da = new EventsAnnotator();
						break;	
					default:
						da = null;
						break;
				}
				
				map.put(ann, da);
			}
		}
		
		for (Map.Entry<AnnotatedWith, DocumentAnnotator> entry : map.entrySet()) {
			if (entry.getValue() != null) {
				threads[i] = new Thread(new Runnable() {
					@Override
					public void run() {
						//TODO: дописать аннотацию! 
						for (int i = 0; i < tempData.size(); i++) {
							synchronized (dataSync) {
								if (tempData.get(i) == null) {
									try {
										dataSync.wait();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
							
							ParsedDocument pd = (ParsedDocument)tempData.get(i);
							pd = new AnnotatedDocument(pd);
						}
					}
				});
				
				i++;
			}
		}
				
		status = ReadyStatus.ANNOTATED;
		return null;
	}
	
	public Object conceptualize() {
		if (status != ReadyStatus.ANNOTATED) {
			throw new IllegalStateException(" Documents can't be conceptualized without any "
					+ "annotation info! ");
		}
		
		return null;
	}
	
	/* Метод объединяет в себе методы parse(), annotate() и conceptualize(). *
	 * Вся обработка при этом происходит параллельно, что достигается        *
	 * массивом потоков-обработчиков.                                        */ 
	public Object process(AnnotatedWith ...annotators) { 
		
		//размер массива потоков берётся потенциально на все аннотаторы + поток парсера 
		// + поток обработчика (концептуализатора)
		Thread[] threads = new Thread[AnnotatedWith.values().length + 2];
		
		//*** Инициализация потоков-обработчиков ***************************************
		Thread t = null; 
		if (status != ReadyStatus.PARSED) {
			t = new Thread(new Runnable() { //поток парсера
				@Override
				public void run() {
					MorphParser parser = new MorphParser();
					
					for (Document d : data) {
						d = new ParsedDocument(d, parse(parser, d));
					}
				}
			});
		}
		
		//******************************************************************************
		
		return null;
	}
	
	/* Здесь объявляем методы parse(), annotate() и conceptualize() **************
	 * для одного документа - понадобится для организации параллельной обработки ***/
	private List<MorphInfo> parse(MorphParser parser, Document d) {
		try {
			return parser.runParser(d.getText(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private Object annotate(DocumentAnnotator annotator, Document d) {
		if (status != ReadyStatus.PARSED) {
			throw new IllegalArgumentException(" Documents aren't parsed for some reasons! ");
		}
		
		return annotator.annotate(d.getText());
	}
	
	private Object conceptualize(Document d) {
		
		
		return null;
	}
	/*******************************************************************************/
}