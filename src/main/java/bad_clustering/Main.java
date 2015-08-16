package clustering;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import clustering.service.AttributeFile;
import clustering.service.attributes.ConceptAttribute;

public class Main {
	public static void main(String[] args) {
		DataCleaner dp = new DataCleaner("D:\\MyProgs\\Sources\\data.file.txt");
		
		final ArrayList<String> data = dp.prepare(StoreData.DOCS);
		System.out.println("Документы подготоволены к разметке. ");
		dp.preprocessData();
		
		final AttributeFile arffFile = new AttributeFile(StoreData.PATH);	
		ConceptAttribute[] attributes = new ConceptAttribute[StoreData.concepts.size()];
		for (int i = 0; i < attributes.length; i++) {
			String name = i + "):";
			if (StoreData.concepts.get(i) != null && StoreData.concepts.get(i).getWords() != null
					&& StoreData.concepts.get(i).getWords().size() > 0) {
				name += toString(StoreData.concepts.get(i).getWords());
			} else {
				name += "Concept" + i;
			}
			attributes[i] = new ConceptAttribute(name, i);
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
				
				//создаём папку ACTORS
				File actorsDir = new File(StoreData.PATH + "\\ACTORS"); 
				actorsDir.mkdir();
				//--------------------
				
				synchronized (sync) {
					System.out.println("Окончание кластеризации на потоке ACTORS. "
							+ "Вывод результатов в файл.");
					System.out.println(s1);
					
					//Вывод Evaluation Result String в файл
					/*File resultFile = new File(actorsDir.getAbsolutePath() + "\\ResultString.txt");
					try (PrintWriter pw = new PrintWriter(new FileWriter(resultFile), true)) {
						pw.println(s1);
					} catch (IOException e) {
						e.printStackTrace();
					}*/
					
					//Вывод содержимого кластеров в файлы
					int[][] content = cp1.getClustersContent();
					for (int i = 0; i < content.length; i++) {
						File clusterFile = new File(actorsDir.getAbsolutePath() + "\\Cluster" + 
										         (i + 1) + ".txt"); 
						PrintWriter pw = null;
						try {
							clusterFile.createNewFile();
							pw = new PrintWriter(new FileWriter(clusterFile), true);
							for (int j = 0; j < content[i].length; j++) {
								pw.println("\t" + data.get(content[i][j]));
								pw.println();
							}
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (pw != null) {
								pw.close();
							}
						}
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
				
				//создаём папку EVENTS
				File eventsDir = new File(StoreData.PATH + "\\EVENTS"); 
				eventsDir.mkdir();
				//--------------------
				
				synchronized (sync) {
					System.out.println("Окончание кластеризации на потоке EVENTS. "
							+ "Вывод результатов в файл.");
					System.out.println(s2);
					
					//Вывод Evaluation Result String в файл
					/*File resultFile = new File(eventsDir.getAbsolutePath() + "\\ResultString.txt");
					try (PrintWriter pw = new PrintWriter(new FileWriter(resultFile), true)) {
						pw.println(s2);
					} catch (IOException e) {
						e.printStackTrace();
					}*/
					
					//Вывод содержимого кластеров в файлы
					int[][] content = cp2.getClustersContent();
					for (int i = 0; i < content.length; i++) {
						File clusterFile = new File(eventsDir.getAbsolutePath() + "\\Cluster" + 
										         (i + 1) + ".txt"); 
						PrintWriter pw = null;
						try {
							clusterFile.createNewFile();
							pw = new PrintWriter(new FileWriter(clusterFile), true);
							for (int j = 0; j < content[i].length; j++) {
								pw.println("\t" + data.get(content[i][j]));
								pw.println();
							}
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (pw != null) {
								pw.close();
							}
						}
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
				
				//создаём папку PLACES
				File placesDir = new File(StoreData.PATH + "\\PLACES"); 
				placesDir.mkdir();
				//--------------------
				
				synchronized (sync) {
					System.out.println("Окончание кластеризации на потоке PLACES. "
							+ "Вывод результатов в файл.");
					System.out.println(s3);
					
					//Вывод Evaluation Result String в файл
					/*File resultFile = new File(placesDir.getAbsolutePath() + "\\ResultString.txt");
					try (PrintWriter pw = new PrintWriter(new FileWriter(resultFile), true)) {
						pw.println(s3);
					} catch (IOException e) {
						e.printStackTrace();
					}*/
					
					//Вывод содержимого кластеров в файлы
					int[][] content = cp3.getClustersContent();
					for (int i = 0; i < content.length; i++) {
						File clusterFile = new File(placesDir.getAbsolutePath() + "\\Cluster" + 
										         (i + 1) + ".txt"); 
						PrintWriter pw = null;
						try {
							clusterFile.createNewFile();
							pw = new PrintWriter(new FileWriter(clusterFile), true);
							for (int j = 0; j < content[i].length; j++) {
								pw.println("\t" + data.get(content[i][j]));
								pw.println();
							}
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (pw != null) {
								pw.close();
							}
						}
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
		System.out.println("Кластеризация завершена. ");
	}
	
	public static String toString(Collection<String> words) {
		StringBuilder s = new StringBuilder();
		for (String w : words) {
			s.append(w + "_");
		}
		if (s.length() > 0) {
			s.deleteCharAt(s.length() - 1);
		}
		
		return s.toString();
	}
}