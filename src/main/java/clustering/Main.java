package clustering;

import java.util.ArrayList;

import clustering.service.AttributeFile;
import clustering.service.attributes.ConceptAttribute;

public class Main {
	public static void main(String[] args) {
		DataPreparer dp = new DataPreparer("D:\\MyProgs\\Sources\\data.file.txt");
		
		ArrayList<String> data = dp.prepare(StoreData.DOCS);
		System.out.println("Документы подготоволены к разметке. ");
		dp.preprocessData();
		
		final AttributeFile arffFile = new AttributeFile(StoreData.PATH);	
		ConceptAttribute[] attributes = new ConceptAttribute[StoreData.concepts.size()];
		for (int i = 0; i < attributes.length; i++) {
			attributes[i] = new ConceptAttribute("C" + i, i);
		}
		arffFile.fillFile(data, attributes);
		
		final Object sync = new Object();
		//TODO: Решить, как выводить строки!!!
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				ClustererPreparer cp1 = new ClustererPreparer(arffFile);
				System.out.println("Старт кластеризации на потоке ACTORS");
				String s1 = cp1.clusterize("ACTORS", 1.5, 2);
				synchronized (sync) {
					System.out.println("Окончание кластеризации на потоке ACTORS. Вывод результатов.");
					System.out.println(s1);
				}
			}
		});
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				ClustererPreparer cp2 = new ClustererPreparer(arffFile);
				System.out.println("Старт кластеризации на потоке EVENTS");
				String s2 = cp2.clusterize("EVENTS", 4, 3);
				synchronized (sync) {
					System.out.println("Окончание кластеризации на потоке EVENTS. Вывод результатов.");
					System.out.println(s2);
				}
			}
		});
		Thread t3 = new Thread(new Runnable() {
			@Override
			public void run() {
				ClustererPreparer cp3 = new ClustererPreparer(arffFile);
				System.out.println("Старт кластеризации на потоке PLACES");
				String s3 = cp3.clusterize("PLACES", 2, 2);
				synchronized (sync) {
					System.out.println("Окончание кластеризации на потоке PLACES. Вывод результатов.");
					System.out.println(s3);
				}
			}
		});
		t1.start();
		t2.start();
		t3.start();
		try {
			t1.join();
			t2.join();
			t3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}