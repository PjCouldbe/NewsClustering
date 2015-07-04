package clustering.service.concepts;

import static com.textocat.api.sdk.model.EntityAnnotationCategory.GPE;
import static com.textocat.api.sdk.model.EntityAnnotationCategory.ORGANIZATION;
import static com.textocat.api.sdk.model.EntityAnnotationCategory.PERSON;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

import clustering.StoreData;

import com.textocat.api.sdk.model.AnnotatedDocument;
import com.textocat.api.sdk.model.DocumentStatus;
import com.textocat.api.sdk.model.EntityAnnotation;

public class ConceptedDocument extends AnnotatedDocument {
	private int[] conceptNumbers = new int[20];
	private double[] tf = new double[20];
	private int length;
	
	private ConceptedDocument(DocumentStatus status, String tag,
			EntityAnnotation[] entities) {
		super(status, tag, entities);
	}
	
	public ConceptedDocument(AnnotatedDocument doc, int length) {
		super(doc.getStatus(), doc.getTag(), doc.getEntities());
		this.length = length;
	}
	
	@SuppressWarnings("unchecked")
	public void produceConcepts(ArrayList<String>[] baseWords, boolean[] isPronoun, 
			Map<EntityAnnotation, Integer> eventMap) { //TODO: проверить в DFPreparer на наличие null
													   //каждого из параметров
		//** 0) resources initialization ************************************************************
		Concept[] localConcepts = new Concept[10]; //массив концептов, формирующихся из этого документа
		int maxConcept = 0; 					   //localConcepts.size()
		
		//массив сущностей, складывается из аннотированных сущностей и сущностей событий
		EntityAnnotation[] eas = new EntityAnnotation[getEntities().length + eventMap.size()];
		System.arraycopy(getEntities(), 
						0, 			
						eas, 
						0, 
						getEntities().length); //аннотированные сущности
		System.arraycopy(eventMap.keySet().toArray(new EntityAnnotation[eventMap.size()]), 
						0, 
						eas, 
						getEntities().length, 
						eventMap.size());	//плюс разметка событий
		//------------------------------------------------------------------------------
		
		//массив слов, складывается из слов для аннотированных сущностей + слова для событий
		ArrayList<String>[] instead1 = 
				(ArrayList<String>[])Array.newInstance((ArrayList.class), eas.length); 
		System.arraycopy(baseWords, 0, instead1, 0, baseWords.length); //для аннотированных сущностей
			
			//в этом цикле записываем в общий массив слов слова для событий
			for (int i = baseWords.length; i < eas.length; i++) {
				instead1[i] = new ArrayList<>();
				instead1[i].add(eas[i].getSpan());
			}
			////////////////////////////////////////////////////////////////
		
		baseWords = instead1;
		//-----------------------------------------------------------------------------------
		
		//размеры массива isPronoun приводятся в соответствие размеру массива сущностей (eas)
		boolean[] instead = new boolean[eas.length];  
		System.arraycopy(isPronoun, 0, instead, 0, isPronoun.length);
		isPronoun = instead;
		
		//формирование массива расстояний -------------------------
		int[] distances = new int[eas.length * (eas.length + 1) / 2];
		//расстояние выясняем между всеми парами неодинаковых сущностей
		for (int i = 0; i < eas.length; i++) {
			for (int j = i + 1; j < eas.length; j++) {
				int val = (eas[i].getEndOffset() < eas[j].getBeginOffset()) 
								? eas[j].getBeginOffset() - eas[i].getEndOffset()
								: eas[i].getBeginOffset() - eas[j].getEndOffset();
				int tmp = index(eas.length, i, j);
				
				//расстояние между сущностями различных категорий 
				//не имеет смысла (принимается за 0)
				if (eas[i].getCategory() == eas[j].getCategory()) {
					distances[tmp] = val;
				} else {
					distances[tmp] = 0;
				}
				////////////////////////////////////////////
			}
		}
		//---------------------------------------------------------
		
		//массив принадлежности сущности концепту с определённым номером
		int[] conceptNumbersForEA = new int[eas.length];
		for (int i = 0; i < eas.length; i++) {
			conceptNumbersForEA[i] = -1;
		}
		//--------------------------------------------------------------
		//********************************************************************************************
		
		//** 1) filling the concepts *****************************************************************
		int j;
		//каждую i-ю сущность пытаемся присоединить к любому j-му существующему концепту,
		//иначе создаём новый концепт
		for (int i = 0; i < eas.length; i++) {
			if (!isPronoun[i]) { //присоединяемая сущность не должна быть местоимением
				for (j = 0; j < maxConcept; j++) {
					boolean b = localConcepts[j].joinEntityToConcept(baseWords[i], eas[i].getCategory());
					if (b) //если прошло, то инкреминируем tf концепта 
						   //и присваиваем сущности соответствующий номер концепта
					{ 
						tf[j]++;
						conceptNumbersForEA[i] = j;
						break;
					}
				}
				if (j == maxConcept) //если не прошёл ни в один существующий концепт 
				{    
					if (maxConcept == localConcepts.length) //если maxConcept сравнялся с длиной массива,
															//значит мы заполнили его и пора расширяться
					{ 
						//расширение массива концептов (localConcepts)
						Concept[] temp = new Concept[localConcepts.length + 30];
						System.arraycopy(localConcepts, 0, temp, 0, localConcepts.length);
						localConcepts = temp;
						//--------------------------------------------
						
						//расширение массива частот по документу (tf)
						double[] temp1 = new double[localConcepts.length]; //та же длина, что и 
						System.arraycopy(tf, 0, temp1, 0, tf.length);	   //у localConcepts
						tf = temp1;
						//-------------------------------------------
					}
					//инициализация нового концепта
					localConcepts[maxConcept] = new Concept(baseWords[i], eas[i].getCategory());
					tf[maxConcept]++;
					conceptNumbersForEA[i] = maxConcept;
					maxConcept++;
					//-----------------------------
				}
			}
		}
		//********************************************************************************************
		
		//** 2) union concepts ***********************************************************************
		double[] p = new double[maxConcept * (maxConcept + 1) / 2]; //массив вероятностей того, 
																	//что пару концептов можно объединить
		/*Принципы подсчёта вер-ти р:
		*1) в каждой паре меньший концепт присоединяем к большему						*
		*для чего выбираем тот концепт, у которого tf меньше и он будет присоединяемым	*
		*2) для каждого k-го концепта в паре c i-м вычисляем coef						*
		*3) считаем сумму: sum = tf[i] * coef[k], k=1..n, учитываем п. 1!!!				*
		*4) для каждого k-го концепта в паре c i-м вычисляем p1=(tf[i] * coef[k]) / sum	*
		*5) для каждого упоминания k-го концепта находим расстояние до ближайшего		*
		*упоминания i-го концепта и считаем произведение величин:  						*
		*p2 = П{t=1..tf[k]}(len - span[t]) / len, где len - длина текущего документа	*
		*  символах	  																	*
		*6) p = p1 * p2, (перемножаем величины из п. 4 и 5 и получаем нужную вер-ть p)	*/
		for (int i = 0; i < maxConcept; i++) {
			double sum = 0.0;  //сумма
			double[] coef = new double[maxConcept];
			for (int k = 0; k < maxConcept; k++) {
				//coef отвечает за соотношение наличия имён собственных у концептов
				if (k == i || localConcepts[k].getCategory() != localConcepts[i].getCategory()) {
					coef[k] = 0.0; //2 концепта с разными категориями не объединяются, поэтому coef = 0
				} else {
					if (!localConcepts[i].hasProper()) {
						coef[k] = 0.25; //если имени собственного нет у присоединемого концепта 
					} else {
						if (localConcepts[k].hasProper()) {
							coef[k] = 0.15; //если оно есть у обоих, смысл присоединять меньше
						} else {
							coef[k] = 1.0;  //если у присоединяемого есть, а у целевого нет, 
											//то смысл объединения многократно возрастает
						}
					}
					if (localConcepts[k].getCategory() == null) {
						coef[k] = 1.0;   //переменная coef не имеет смысла в случае событий, поэтому 1
					}
				}
				
				sum += tf[k] * coef[k];
			}
			for (int k = 0; k < maxConcept; k++) {
				double pLocal = 0.0;
				int index = index(maxConcept, i, k);
				if (index >= 0 && p[index] == 0) { //если index не -1 и вер-ть р ещё не сосчитана
					int i1; //присоединяемый концепт
					int k1; //целевой концепт
					if (tf[i] > tf[k]) {
						i1 = k;
						k1 = i;
					} else {
						i1 = i;
						k1 = k;
					}
					
					pLocal = tf[i1] * coef[k] / sum;
					int multiplier = 0; //множитель для расстояния, чем больше tf, 
										//тем больше вер-ть присоединить
					if (pLocal <= 1e-5) {
						pLocal = 0.0;
					} else {
						//*** Dinstances calculating *************************
						for (int iLocal = 0; iLocal < eas.length; iLocal++) { //ищем сущность
							if (conceptNumbersForEA[iLocal] == i1) 			//принадлежащую
							{												//присоединяемому концепту
								int minDistance = length;
								for (int jLocal = 0; jLocal < eas.length; jLocal++) { //ищем сущность
									if (conceptNumbersForEA[jLocal] == k1) 			//принадлежащую
									{												//целевому концепту
										if (index(maxConcept, iLocal, jLocal) >= 0 
												&& distances[index(maxConcept, iLocal, jLocal)] > 0 
												&& distances[index(maxConcept, iLocal, jLocal)] < minDistance) 
										{
											minDistance = distances[index(maxConcept, iLocal, jLocal)];
										}
									}
								}
								//высчитываем произведение кратчайших соотношений расстояний
								pLocal *= (minDistance != length) ? (length - minDistance) / (double)length 
																  : 1.0;
								multiplier++;
							}
						}
						//****************************************************
					}
					if (index >= 0) {
						p[index] = pLocal * multiplier;
					}
				}
			}
		}
		//ищем наибльшие р, чтобы объединить концепты правильно
		for (int i = 0; i < maxConcept; i++) {
			if (localConcepts[i] != null) { //TODO: здесь не должны быть null'ы!!! Почему???
				double max = 0.0;
				j = -1;
				for (int k = 0; k < maxConcept; k++) {
					if (i != k && localConcepts[k] != null) {
						if (index(maxConcept, i, k) >= 0 && p[index(maxConcept, i, k)] > max) {
							max = p[index(maxConcept, i, k)];
							j = k;
						}
					}
				}
				//если вер-ть не 0 и больше threashold-величины (пока что 0.15), тогда объединяем
				if (j > 0 && max > 0.15 && localConcepts[j] != null) { //TODO: и здесь тоже!!!
					//меньший присоединяем к большему!
					if (tf[i] >= tf[j]) {
						localConcepts[i].union(localConcepts[j]);
						localConcepts[j] = null;
						tf[i] += tf[j];
					} else {
						localConcepts[j].union(localConcepts[i]);
						localConcepts[i] = null;
						tf[j] += tf[i];
					}
				}
			}
		}
		//********************************************************************************************
		
		//** 3) working with pronouns ****************************************************************
		for (int i = 0; i < maxConcept; i++) { //высчитываем прибавку к сущностям по местоимениям
			double tfDifference = 0;
			for (j = 0; j < eas.length; j++) { 
				if (conceptNumbersForEA[j] == i) { //для каждой j-й сущности, принадлежащей i-му концепту
					for (int k = 0; k < eas.length; k++) {  //ищем местоимения из k-х сущностей
						int q = 0;  //множитель штрафа: сколько сущностей из других концептов 
									//стоит между текущей сущностью и местоимением
						double val = 1.0;
						if (isPronoun[k] && eas[j].getCategory() != null  //для событий местоимения 
								&& eas[k].getBeginOffset() - eas[j].getEndOffset() > 0) //не в счёт
						{
							int d = eas[k].getBeginOffset() - eas[j].getEndOffset(); //искомое расстояние
							for (int m = 0; m < eas.length; m++)	//ищем "мешающие" сущности 
							{  										//из других концептов
								if ((eas[m].getCategory() == PERSON 
										|| eas[m].getCategory() == ORGANIZATION
										|| eas[m].getCategory() == GPE) //только из этих 3-х категорий
										&& eas[k].getBeginOffset() - eas[m].getEndOffset() > 0 
										&& eas[k].getBeginOffset() - eas[m].getEndOffset() < d) 
								{
									q++;
								}
							}
							//высчитываем размер штрафа к прибавке
							for (int temp = 0; temp < q; temp++) {
								val *= 0.618; //цена штрафа - 0.618
							}
							tfDifference += val;  //+ к прибавке
						}
					}
				}
			}
			tf[i] += tfDifference;   //собственно прибавка
		}
		//********************************************************************************************
		
		//** Final: union with global concepts *******************************************************
		for (int i = 0 ; i < maxConcept; i++) {
			Concept c = localConcepts[i];
			//убираем все лишние концепты, больше с сущностями не работаем
			if (c == null || tf[i] < 2.0 
					|| (c.getCategory() != PERSON 
					&& c.getCategory() != ORGANIZATION
					&& c.getCategory() != GPE
					&& c.getCategory() != null)) {
				localConcepts[i] = null;
				maxConcept--;
			}
		}
		int cursize = 0; 
		//сдвигаем все ненуллевые концепты так, чтобы все null'ы сдвинулись в конец
		for (int i = 0; i < localConcepts.length; i++) {
			if (localConcepts[i] != null) {
				localConcepts[cursize] = localConcepts[i];
				tf[cursize] = tf[i];
				cursize++;
			}
		}
		//а после сдвига обрезаем массив ------------------------------------------
		Concept[] tempConcepts = new Concept[maxConcept];
		System.arraycopy(localConcepts, 0, tempConcepts, 0, maxConcept);
		localConcepts = tempConcepts;
		//-------------------------------------------------------------------------
		
		//инициализируем окончательные характеристики текущего ConceptDocument
		conceptNumbers = new int[maxConcept];
		double[] temp = new double[maxConcept];
		System.arraycopy(tf, 0, temp, 0, maxConcept);
		tf = temp;
		//--------------------------------------------------------------------
		
		//начало объединения с глобальными концептами
		double max = 0.0;
		int maxNum = 0;
		for (int i = 0; i < maxConcept; i++) {	//для каждого i-го локального концепта
			for (j = 0; j < StoreData.concepts.size(); j++) {  //для каждого j-го глобального концепта
				if (StoreData.concepts.get(j) != null && localConcepts[i] != null)  //TODO: здесь тоже не 
				{																	//должно быть null'ов!
					//TODO: подкорректировать эту функцию объединения, 
					//чтобы события объединялись не так шустро 
					double d = Concept.strictUnionChance(StoreData.concepts.get(j), localConcepts[i]);
					if (d > max) {
						max = d;
						maxNum = j;	  //номер концепта с d == max, чтобы знать, с каким нужно объединить
					}
				}
			}
			if (max > 0.45 && localConcepts[i] != null) {  //TODO: null'ы все убирали, откуда они тут взялись???
				StoreData.concepts.get(maxNum).union(localConcepts[i]);
				conceptNumbers[i] = maxNum;
			} else {
				StoreData.concepts.add(localConcepts[i]);
				conceptNumbers[i] = StoreData.concepts.size() - 1;
			}
		}
		
		//убираем повторения
		for (int i = 0; i < conceptNumbers.length; i++) {
			for (j = i + 1; j < conceptNumbers.length; j++) {
				if (conceptNumbers[i] == conceptNumbers[j] 
						&& (conceptNumbers[j] != -1 || conceptNumbers[i] != -1)) {
					tf[i] += tf[j];
					conceptNumbers[j] = -1;
				}
			}
		}
		//------------------
		
		//и обрезаем массивы ----
		int c = 0;
		while (c < conceptNumbers.length && conceptNumbers[c] >= 0) {
			c++;
		}
		
		int[] conceptsTmp = new int[c];
		System.arraycopy(conceptNumbers, 0, conceptsTmp, 0, c);
		conceptNumbers = conceptsTmp;
				
		double[] tfLocal = new double[c];
		System.arraycopy(tf, 0, tfLocal, 0, c);
		tf = tfLocal;
		//----------------------
		//********************************************************************************************
	}
	
	private int index(int capacity, int firstAnnotationNumber, int secondAnnotationNumber) {
		if (firstAnnotationNumber > secondAnnotationNumber) {
			int temp = firstAnnotationNumber;
			firstAnnotationNumber = secondAnnotationNumber;
			secondAnnotationNumber = temp;
		}
		if (firstAnnotationNumber == secondAnnotationNumber) {
			return -1;
		}
		
		return firstAnnotationNumber * capacity - 
				((firstAnnotationNumber + 1) * firstAnnotationNumber) / 2 
				+ secondAnnotationNumber - 1;
	}
	
	public int[] getConceptNumbers() {
		return conceptNumbers;
	}
	
	public double[] getTf() {
		return tf;
	}
}