package clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.textocat.api.sdk.model.AnnotatedDocument;

import clustering.service.concepts.ConceptedDocument;

public class DataPreparer {
	private static String monthsAndDays = "\\d\\d "
			+ "((январ|феврал|апрел|ма)я|(мар|авгус)та|ию[нл]я|(сентя|октя|ноя|дека)бря)";
	private static String dates = "^"
					+ monthsAndDays
					+ " \\d?\\d?\\d\\d, "
					+ "\\d\\d:\\d\\d"
					+ ".*"
					+ "$"; 
	private File datafile;
	
	private ArrayList<String> data;
	
	public DataPreparer(String dataFileName) {
		datafile = new File(dataFileName);
	}
	
	public ArrayList<String> prepare(int capacity) {
		StoreData.conceptedDocuments = new ConceptedDocument[StoreData.DOCS];
		DFPreparer.annotatedDocuments = new AnnotatedDocument[StoreData.DOCS];
		ArrayList<String> res = new ArrayList<>(capacity);
		try (BufferedReader in = new BufferedReader(new FileReader(datafile))) {
			int counter = capacity;
			while (in.ready() && counter > 0) {
				String s = in.readLine();
				if (!s.matches(dates)) {
					s = s.replaceAll(" *(\\\\n)[(\\\\n) \t]*", ". ");
					s = s.replaceAll(" *\\.[\\. ]*", ". ");
					res.add(s);
				}
				counter--;
			}
		} catch (IOException e) {
			e.printStackTrace();
			res = null;
		}
		return data = res;
	}
	
	public ArrayList<String> prepareAll() {
		int counter = 0;
		ArrayList<String> res = new ArrayList<>(500);
		try (BufferedReader in = new BufferedReader(new FileReader(datafile))) {
			while (in.ready()) {
				counter++;
				String s = in.readLine();
				if (!s.matches(dates)) {
					s = s.replaceAll("[(\\\\n) \t]+", ". ");
					s = s.replaceAll("[. ]+", ". ");
					res.add(s);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			res = null;
			counter = 0;
		}
		StoreData.conceptedDocuments = new ConceptedDocument[counter];
		DFPreparer.annotatedDocuments = new AnnotatedDocument[StoreData.DOCS];
		return data = res;
	}
	
	public Map<String, Integer> preprocessData() {
		if (data == null) {
			return null;
		}
		DocumentAnnotator da = new DocumentAnnotator("d516188545411ff4331256dddf9b3376", data);
		//da.annotate(data);
		synchronized (StoreData.sync) {
			try {
				System.out.println("stopped");
				StoreData.sync.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("continued");
		
		System.out.println("Старт словаря.");
		DFPreparer preparer = new DFPreparer();
		Map<String, Integer> m =  preparer.makeDictionary(data);
		System.out.println("Документы подготовлены! Все данные для анализа собраны! ");
		return m;
	}
}