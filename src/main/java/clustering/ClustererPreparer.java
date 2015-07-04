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
	
	public ClustererPreparer(File arffFile) {
		clusterer = new EM();
		try {
			((EM)clusterer).setNumClusters(6);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.arffFile = arffFile;;
	}
	
	public ClustererPreparer(File arffFile, Clusterer c) {
		clusterer = c;
		this.arffFile = arffFile;
	}
	
	public String clusterize(String dominanteAttributeGroup, double linearCoef, double exponentialCoef) {
		if (!arffFile.getAbsolutePath().endsWith(".arff")) {
			System.err.println("Cannot read the file because of wrong format. Needs the arff file!");
			System.exit(1);
		}
		try {
			Instances data = DataSource.read(arffFile.getAbsolutePath());
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
			/*options = new String[2];
			options[0] = "-t";
			options[1] = arffFile.getAbsolutePath();
			System.out.println(ClusterEvaluation.evaluateClusterer(new EM(), options));*/
			
			//manual call
			clusterer.buildClusterer(data);
			ClusterEvaluation evaluator = new ClusterEvaluation();
			evaluator.setClusterer(clusterer);
			evaluator.evaluateClusterer(new Instances(data));
			/*for (Instance i : data) {
				i.classAttribute();
			}*/
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
	
	public Clusterer getClusterer() {
		return clusterer;
	}
}