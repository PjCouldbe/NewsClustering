package clustering;

import static com.textocat.api.sdk.model.EntityAnnotationCategory.GPE;
import static com.textocat.api.sdk.model.EntityAnnotationCategory.ORGANIZATION;
import static com.textocat.api.sdk.model.EntityAnnotationCategory.PERSON;

import java.io.File;
import java.io.IOException;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import com.textocat.api.sdk.model.EntityAnnotationCategory;

public class ClustererPreparer {
	private Clusterer clusterer;
	private File arffFile;
	private Instances data;
	
	public ClustererPreparer(File arffFile) {
		clusterer = new EM();
		this.arffFile = arffFile;;
	}
	
	public ClustererPreparer(File arffFile, Clusterer c) {
		clusterer = c;
		this.arffFile = arffFile;
	}
	
	public String clusterize(int numClusters, String dominanteAttributeGroup, 
						double linearCoef, double exponentialCoef) 
	{
		//первичная проверка
		if (!arffFile.getAbsolutePath().endsWith(".arff")) {
			System.err.println("Cannot read the file because of wrong format. Needs the arff file!");
			System.exit(1);
		}
		
		//настраиваем число кластеров
		try {
			((EM)clusterer).setNumClusters(numClusters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//кластеризация
		try {
			data = DataSource.read(arffFile.getAbsolutePath());
			for (Instance i : data) {
				for (int j = 0; j < i.numAttributes(); j++) {
					EntityAnnotationCategory cat = null;
					if (StoreData.concepts.get(j) != null) {
						cat = StoreData.concepts.get(j).getCategory();
					}
					String groupId;
					if (cat == null) {
						groupId = "EVENTS";
					} else {
						if (cat == GPE) {
							groupId = "PLACES";
						} else if (cat == PERSON || cat == ORGANIZATION) {
							groupId = "ACTORS";
						} else {
							groupId = null;
						}
					}
					if (groupId.equals(dominanteAttributeGroup) && groupId != null) {
						i.setValue(j, linearCoef * Math.pow(exponentialCoef, i.value(j)));
					}
				}
			}
			//normal
			/*String[] options = new String[2];
			options[0] = "-t";
			options[1] = arffFile.getAbsolutePath();
			System.out.println(ClusterEvaluation.evaluateClusterer(clusterer, options));*/
			
			//manual call
			clusterer.buildClusterer(data);
			ClusterEvaluation evaluator = new ClusterEvaluation();
			evaluator.setClusterer(clusterer);
			evaluator.evaluateClusterer(new Instances(data));
			return evaluator.clusterResultsToString();
			
			//cross-validation for density
			/*System.out.println("\n--> Cross-validation");
			clusterer = new EM();
			logLikelyhood = ClusterEvaluation.crossValidateModel(
					(DensityBasedClusterer)clusterer, data, 10, data.getRandomNumberGenerator(1));
			System.out.println("log-likelyhood: " + logLikelyhood);*/
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public int[][] getClustersContent() {
		//массив, отвечающий за количество экземпляров в каждом кластере
		int[] nums = new int[((EM)clusterer).getNumClusters()]; 
		//массив, отвечающий за принадлежность каждого документа определённому кластеру
		int[] classes = new int[data.size()]; 
		for (int i = 0; i < data.size(); i++) {
			try {
				int classifier = clusterer.clusterInstance(data.get(i)); //какому кластеру принадлежит
				classes[i] = classifier;  //запоминаем это здесь
				nums[classifier]++;  //в соответствующей ячейке увеличиваем количество на 1
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		int[][] res = new int[nums.length][];
		for (int i = 0; i < nums.length; i++) {
			res[i] = new int[nums[i]];
			int count = 0;  
			for (int j = 0; j < classes.length; j++) {
				if (classes[j] == i) {
					res[i][count] = j;
					count++;
				}
			}
		}
		return res;
	}
	
	public Clusterer getClusterer() {
		return clusterer;
	}
}