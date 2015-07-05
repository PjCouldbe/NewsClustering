package clustering;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import clustering.service.AttributeFile;
import clustering.service.attributes.ConceptAttribute;

public class Main {
	public static void main(String[] args) {
		DataPreparer dp = new DataPreparer("D:\\MyProgs\\Sources\\data.file.txt");
		
		final ArrayList<String> data = dp.prepare(StoreData.DOCS);
		System.out.println("Документы подготоволены к разметке. ");
		dp.preprocessData();
		
		final AttributeFile arffFile = new AttributeFile(StoreData.PATH);	
		ConceptAttribute[] attributes = new ConceptAttribute[StoreData.concepts.size()];
		for (int i = 0; i < attributes.length; i++) {
			attributes[i] = new ConceptAttribute("Concept" + i, i);
		}
		arffFile.fillFile(data, attributes);
		
		final Object sync = new Object();
		//TODO: Решить, как выводить строки!!!
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				ClustererPreparer cp1 = new ClustererPreparer(arffFile);
				System.out.println("Старт кластеризации на потоке ACTORS");
				String s1 = cp1.clusterize(6, "ACTORS", 2, 3);
				synchronized (sync) {
					System.out.println("Окончание кластеризации на потоке ACTORS. "
							+ "Вывод результатов в файл.");
					//System.out.println(s1);
					try (PrintWriter writer = new PrintWriter(
												new FileWriter(
													new File(StoreData.PATH + "\\ACTORS.txt")), true)) 
					{
						int[][] content = cp1.getClustersContent();
						for (int i = 0; i < content.length; i++) {
							writer.println("Cluster " + (i + 1) + ": ");
							for (int j = 0; j < content[i].length; j++) {
								writer.println("\t" + data.get(content[i][j]));
							}
							writer.println();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				ClustererPreparer cp2 = new ClustererPreparer(arffFile);
				System.out.println("Старт кластеризации на потоке EVENTS");
				String s2 = cp2.clusterize(6, "EVENTS", 4, 3);
				synchronized (sync) {
					System.out.println("Окончание кластеризации на потоке EVENTS. "
							+ "Вывод результатов в файл.");
					//System.out.println(s2);
					try (PrintWriter writer = new PrintWriter(
												new FileWriter(
													new File(StoreData.PATH + "\\EVENTS.txt")), true)) 
					{
						int[][] content = cp2.getClustersContent();
						for (int i = 0; i < content.length; i++) {
							writer.println("Cluster " + (i + 1) + ": ");
							for (int j = 0; j < content[i].length; j++) {
								writer.println("\t" + data.get(content[i][j]));
							}
							writer.println();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		Thread t3 = new Thread(new Runnable() {
			@Override
			public void run() {
				ClustererPreparer cp3 = new ClustererPreparer(arffFile);
				System.out.println("Старт кластеризации на потоке PLACES");
				String s3 = cp3.clusterize(6, "PLACES", 2, 3);
				synchronized (sync) {
					System.out.println("Окончание кластеризации на потоке PLACES. "
							+ "Вывод результатов в файл.");
					//System.out.println(s3);
					try (PrintWriter writer = new PrintWriter(
												new FileWriter(
													new File(StoreData.PATH + "\\PLACES.txt")), true)) 
					{
						int[][] content = cp3.getClustersContent();
						for (int i = 0; i < content.length; i++) {
							writer.println("Cluster " + (i + 1) + ": ");
							for (int j = 0; j < content[i].length; j++) {
								writer.println("\t" + data.get(content[i][j]));
							}
							writer.println();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
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