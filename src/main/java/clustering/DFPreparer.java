package clustering;

import static clustering.StoreData.PARSER;
//import static clustering.StoreData.annotatedDocuments;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.textocat.api.sdk.model.AnnotatedDocument;
import com.textocat.api.sdk.model.EntityAnnotation;

import ru.itbrains.gate.morph.MorphInfo;
import clustering.service.concepts.ConceptedDocument;

public class DFPreparer {
	private List<String> data = null;
	private static List<MorphInfo> info = new LinkedList<>();
	private boolean finished = false;
	private static final Object sync = new Object();
	private Map<String, Integer> map;
	private int counter = 0;
	private int length = 0;
	
	public static AnnotatedDocument[] annotatedDocuments;
	
	public DFPreparer() {
		
	}
	
	private Thread dataThread = new Thread(new Runnable() {
		@Override
		public void run() {
			finished = false;
			synchronized (sync) {
				for (int i = 0; i < data.size(); i++) {
					try {
						info = PARSER.runParser(data.get(i), "UTF-8"); //парсим документ
						length = data.get(i).length();  //запоминаем его длину
						if (info.size() == 0) {   //если документ пустой, он нам не нужен
							continue;
						} 
						counter = i;  //счётчик для второго потока
						
						sync.notify();
						//System.out.print(1);
						sync.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				finished = true;
			}
		}
	});
	private Thread workingThread = new Thread(new Runnable() {
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			List<MorphInfo> m;
			int num;
			map = new TreeMap<>();
			ConceptedDocument doc;
			
			while (!finished) {
				synchronized (sync) {
					if (info.size() == 0) {
						//System.out.print(2);
						try {
							sync.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					m = info;   //достаём распарсенный документ
					info = new LinkedList<>();  //обнуляем глобальную переменную
					num = counter; //достаём счётчик
					sync.notify();
				}
				//создаём новый ConceptDocument
				StoreData.conceptedDocuments[num] = 
							(doc = new ConceptedDocument(annotatedDocuments[num], length));
				//хранилище слов из аннотированных сущностей в начальной форме
				ArrayList<String>[] baseWords = 
						(ArrayList<String>[])Array.newInstance((ArrayList.class), doc.getEntities().length);
				for (int i = 0; i < baseWords.length; i++) {
					baseWords[i] = new ArrayList<>();
				}
				//массив местоимений
				boolean[] isPronoun = new boolean[doc.getEntities().length];
				//хранилище слов для событий
				Map<EntityAnnotation, Integer> localMap = new HashMap<>();
				int localCounter = 0;
				for (MorphInfo i : m) {
					Map<String, String> temp = i.getHomonymGrammems().get(0); //морфологические
																			  //характеристики слова
					String baseForm = temp.get("baseForm");  //начальная форма слова
					String pos = temp.get("pos");   //часть речи слова
					if (pos != null && 
							(pos.equals("verb") || pos.equals("substantive") || pos.endsWith("pronoun"))) {
						boolean is = pos.endsWith("pronoun");   //если true, то это - местоимение
						
						//пробуем обработать токен, как часть аннотированной сущности,
						//заодно выясняем, входит ли это слово в состав сущности
						boolean isEntity = false;
						for (int j = 0; j < doc.getEntities().length; j++) {
							if (doc.getEntities()[j].getSpan().indexOf(i.getOriginalWord()) != -1) {
								isEntity = true;
								isPronoun[j] = is;
								//выясняем, является ли это слово именем собственным
								if (temp.containsKey("geo") 
										|| temp.containsKey("first-name") 
										|| temp.containsKey("surname") 
										|| temp.containsKey("middle-name")
										|| temp.containsKey("abbreviation")) 
								{
									//заменяем первую букву на заглавную
									baseForm = (new StringBuilder())
														 .append(baseForm.toUpperCase().charAt(0))
														 .append(baseForm.substring(1))
														 .toString();
								}
								//--------------------------------------------------
								
								//добавляем в хранилище слов, если это не местоимение
								if (!is && !baseWords[j].contains(baseForm)) {
									baseWords[j].add(baseForm);
								}
							}
						}
						//если это не сущность, значит обрабатываем, как событие -----
						if (!isEntity && !is) {  //местоимения не включаем
							
							//достаём исходное слово для следующего шага
							String orWord = m.get(localCounter).getOriginalWord();
							
							//узнаём beginOffset и endOffset для этого токена, 
							//чтобы потом можно было полноценно работать с расстояниями
							int beginOffset = data.get(num).indexOf(orWord);
							EntityAnnotation ea = new EntityAnnotation(baseForm, //делаем сами новую сущность
										beginOffset, 
										beginOffset + orWord.length(), 
										null);
							
							localMap.put(ea, localMap.containsKey(ea) 
													? localMap.get(ea) + 1 
													: 1);
						}
						//------------------------------------------------------------
					}
					localCounter++;
				}
				Set<String> alredyAdded = new HashSet<>();
				//дополняем хранилище слов для idf
				for (Map.Entry<EntityAnnotation, Integer> entry : localMap.entrySet()) {
					//TODO: выяснить, учитывается ли где-то idf
					if (!alredyAdded.contains(entry.getKey().getSpan())) {
						map.put(entry.getKey().getSpan(),
								map.containsKey(entry.getKey().getSpan()) 
								? map.get(entry.getKey().getSpan()) + 1 
								: 1);
						alredyAdded.add(entry.getKey().getSpan());
					}
				}
				//--------------------------------
				
				doc.produceConcepts(baseWords, isPronoun, localMap);
			}
		}
	});

	public Map<String, Integer> makeDictionary(List<String> data) {
		this.data = data;
		dataThread.start();
		workingThread.start();
		try {
			dataThread.join();
			workingThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		annotatedDocuments = null;
		return map;
	}
}