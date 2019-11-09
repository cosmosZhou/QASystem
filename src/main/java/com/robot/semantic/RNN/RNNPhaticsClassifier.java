package com.robot.semantic.RNN;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.robot.Sentence;
import com.robot.syntax.SyntacticParser;
import com.robot.syntax.SyntacticTree;
import com.robot.syntax.SyntacticTree.BinarizedTree;
import com.robot.syntax.SyntacticTree.BinarizedTree.Combination;
import com.robot.util.InstanceReader;
import com.util.Utility;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.Timing;

public class RNNPhaticsClassifier extends RNNTraining {
	static public RNNPhaticsClassifier instance = new RNNPhaticsClassifier();

	public RNNPhaticsClassifier() {
		try {
			modelPath = Utility.workingDirectory + "models/RNNPhaticsClassifier.gz";
			model = RNNModel.loadSerialized(modelPath);
			model.op.trainOptions.epochs = 10;
			model.op.trainOptions.debugOutputEpochs = -1;
		} catch (Exception e) {
			e.printStackTrace();
			RNNOptions op = new RNNOptions();
			op.numHid = 25;
			op.numClasses = 2;
			op.trainOptions.epochs = 10;
			op.trainOptions.debugOutputEpochs = -1;
			//			op.simplifiedModel = false;
			//			op.combineClassification = false;

			model = new RNNModel(op);
		}

	}

	static Combination combination = new Combination() {
		@Override
		public String combine(String seg, String pos) {
			switch (pos) {
			case "CD":
			case "PU":
			case "O":
			case "M":
			case "LC":
				return pos;

			case "NT":

			case "NR":
			case "VI":
			case "VBG":

			case "JJ":
			case "DT":

			case "PN":
			case "NEG":
			case "QUE":
			case "CC":
			case "CS":
			case "P":
			case "VC":
			case "MD":
			case "IJ":
			case "AS":
			case "DE":

			case "AD":
			case "VT":
			case "VA":
			case "NN":
				return Utility.simplifyString(seg) + "/" + pos;

			default:
				throw new RuntimeException(pos);
			}
		}
	};

	public Combination combination() {
		return combination;
	}

	public static class SourceSentence implements CoreAnnotation<String> {
		@Override
		public Class<String> getType() {
			return String.class;
		}
	}

	public void establishTrainingCorpus() throws Exception {
		String trainDataPath = Utility.workingDirectory + "corpus/Phatics/";
		ArrayList<LabeledScoredTreeNode> listNEUTRAL = new ArrayList<LabeledScoredTreeNode>();
		ArrayList<LabeledScoredTreeNode> listPERTAIN = new ArrayList<LabeledScoredTreeNode>();
		trainingTrees = new ArrayList<Tree>();
		ArrayList<LabeledScoredTreeNode> list = new ArrayList<LabeledScoredTreeNode>();

		for (com.robot.util.Instance inst : new InstanceReader(trainDataPath)) {

			String sent = (String) inst.getData();
			if (sent.isEmpty()) {
				continue;
			}
			//				Utility.removeEndOfSentencePunctuation(sent);

			Sentence sentence = new Sentence(sent);
			//			System.out.println(sentence);
			BinarizedTree bTreeForTraining = null;
			try {
				bTreeForTraining = sentence.tree().toBinarizedTree(combination);
			} catch (Exception e) {
				e.printStackTrace();
				new Sentence(sent).tree();
				continue;
			}
			String target = (String) inst.getTarget();

			LabeledScoredTreeNode labeledScoredTreeNode;
			switch (target) {
			case "NEUTRAL":
				bTreeForTraining.classtype = 0;
				labeledScoredTreeNode = bTreeForTraining.toLabeledScoredTreeNode();

				listNEUTRAL.add(labeledScoredTreeNode);
				break;
			case "PERTAIN":
				bTreeForTraining.classtype = 1;
				labeledScoredTreeNode = bTreeForTraining.toLabeledScoredTreeNode();
				listPERTAIN.add(labeledScoredTreeNode);
				break;
			default:
				System.out.println(sent);
				throw new RuntimeException("String target = " + target);
			}

			labeledScoredTreeNode.label().set(SourceSentence.class, sent);

			list.add(labeledScoredTreeNode);
		}

		model.initRandomWordVectors(list);
		int neutralSize = 1000;
		int neutralError = 400;
		int pertainError = 600;

		Utility.shuffle(listNEUTRAL.subList(neutralError, listNEUTRAL.size()));
		for (int i = neutralSize - 1; i >= 0; --i) {
			trainingTrees.add(listNEUTRAL.get(i));
		}

		Utility.shuffle(listPERTAIN.subList(pertainError, listPERTAIN.size()));
		for (int i = neutralSize * multitude - 1; i >= 0; --i) {
			trainingTrees.add(listPERTAIN.get(i));
		}

		//		for (int i = 0; i <= neutralError; ++i) {
		//			System.out.println("NEUTRAL = " + listNEUTRAL.get(i).label().get(SourceSentence.class));
		//		}

		for (int i = 0; i <= pertainError; ++i) {
			System.out.println(listPERTAIN.get(i).label().get(SourceSentence.class));
		}

		DecimalFormat df = new DecimalFormat("00.00");
		System.out.println("percent for NEUTRAL = " + df.format(neutralSize * 1.0 / trainingTrees.size() * 100) + "%");
		System.out.println("total sentences  = " + trainingTrees.size());
	}

	public int multitude = 19;
	final static public double threshold = 0.50;

	public void testRNNModel() throws Exception {
		int errorPERTAIN = 0;
		int sumPERTAIN = 0;
		int errorNEUTRAL = 0;
		int sumNEUTRAL = 0;

		String trainDataPath = Utility.workingDirectory + "corpus/Phatics/";
		ArrayList<String> errorListNEUTRAL = new ArrayList<String>();
		ArrayList<String> errorListPERTINENT = new ArrayList<String>();
		ArrayList<String> listNEUTRAL = new ArrayList<String>();
		ArrayList<String> listPERTINENT = new ArrayList<String>();
		for (com.robot.util.Instance inst : new InstanceReader(trainDataPath)) {
			String sent = (String) inst.getData();
			//				Utility.removeEndOfSentencePunctuation(sent);

			SyntacticTree tree = null;

			try {
				tree = SyntacticParser.instance.parse(sent);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			String target = (String) inst.getTarget();

			double probability = RNNPhaticsClassifier.instance.classify(tree);
			String targetPred;
			if (probability > threshold)
				targetPred = "PERTAIN";
			else
				targetPred = "NEUTRAL";

			switch (target) {
			case "NEUTRAL":
				++sumNEUTRAL;
				if (!target.equals(targetPred)) {
					++errorNEUTRAL;
					System.out.println(sent);
					errorListNEUTRAL.add(";" + sent);
				}
				listNEUTRAL.add(sent);
				break;
			case "PERTAIN":
				++sumPERTAIN;
				if (!target.equals(targetPred)) {
					++errorPERTAIN;
					System.out.println(sent);
					errorListPERTINENT.add(";" + sent);
				}
				listPERTINENT.add(sent);
				break;
			default:
				System.out.println(sent);
				throw new RuntimeException("String target = " + target);
			}
		}

		DecimalFormat df = new DecimalFormat("00.00");
		int err = errorPERTAIN + errorNEUTRAL;
		int sum = sumPERTAIN + sumNEUTRAL;
		System.out.println("correct percent for PERTAIN = " + df.format((sumPERTAIN - errorPERTAIN) * 1.0 / sumPERTAIN * 100) + "%,    errors = " + errorPERTAIN);
		System.out.println("correct percent for NEUTRAL = " + df.format((sumNEUTRAL - errorNEUTRAL) * 1.0 / sumNEUTRAL * 100) + "%,    errors = " + errorNEUTRAL);

		System.out.println("correct percent = " + df.format((sum - err) * 1.0 / sum * 100) + "%");
		System.out.println("total sentences  = " + sum);

		Utility.writeString(trainDataPath + "PERTAIN.data", listPERTINENT);
		Utility.writeString(trainDataPath + "NEUTRAL.data", listNEUTRAL);

		Utility.prependString(trainDataPath + "PERTAIN.data", errorListPERTINENT);
		Utility.prependString(trainDataPath + "NEUTRAL.data", errorListNEUTRAL);

		for (int i = 0; i < errorListPERTINENT.size(); ++i) {
			errorListPERTINENT.set(i, errorListPERTINENT.get(i).substring(1));
		}

		for (int i = 0; i < errorListNEUTRAL.size(); ++i) {
			errorListNEUTRAL.set(i, errorListNEUTRAL.get(i).substring(1));
		}

		Utility.removeString(trainDataPath + "PERTAIN.data", errorListPERTINENT);
		Utility.removeString(trainDataPath + "NEUTRAL.data", errorListNEUTRAL);
	}

	public void executeOneTrainingBatch(RNNModel model, List<Tree> trainingBatch, double[] sumGradSquare) {
		RNNCostAndGradient gcFunc = new RNNCostAndGradient(model, trainingBatch);
		double[] theta = model.paramsToVector();

		// AdaGrad
		double eps = 1e-3;
		// TODO: do we want to iterate multiple times per batch?
		double[] gradf = gcFunc.derivativeAt(theta);
		double currCost = gcFunc.valueAt(theta);
		log.info("batch cost: " + currCost);
		for (int feature = 0; feature < gradf.length; feature++) {
			sumGradSquare[feature] = sumGradSquare[feature] + gradf[feature] * gradf[feature];
			theta[feature] = theta[feature] - (model.op.trainOptions.learningRate * gradf[feature] / (Math.sqrt(sumGradSquare[feature]) + eps));
		}

		model.vectorToParams(theta);
	}

	public void train(RNNModel model, String modelPath, List<Tree> trainingTrees, List<Tree> devTrees) {
		Timing timing = new Timing();
		long maxTrainTimeMillis = model.op.trainOptions.maxTrainTimeSeconds * 1000;
		// double bestAccuracy = 0.0;

		// train using AdaGrad (seemed to work best during the dvparser project)
		double[] sumGradSquare = new double[model.totalParamSize()];
		Arrays.fill(sumGradSquare, model.op.trainOptions.initialAdagradWeight);

		int numBatches = trainingTrees.size() / model.op.trainOptions.batchSize + 1;
		log.info("Training on " + trainingTrees.size() + " trees in " + numBatches + " batches");
		log.info("Times through each training batch: " + model.op.trainOptions.epochs);
		double scoremMAX = -1;
		for (int epoch = 0; epoch < model.op.trainOptions.epochs; ++epoch) {
			log.info("======================================");
			log.info("Starting epoch " + epoch);
			if (epoch > 0 && model.op.trainOptions.adagradResetFrequency > 0 && (epoch % model.op.trainOptions.adagradResetFrequency == 0)) {
				log.info("Resetting adagrad weights to " + model.op.trainOptions.initialAdagradWeight);
				Arrays.fill(sumGradSquare, model.op.trainOptions.initialAdagradWeight);
			}

			List<Tree> shuffledSentences = Generics.newArrayList(trainingTrees);
			if (model.op.trainOptions.shuffleMatrices) {
				Collections.shuffle(shuffledSentences, model.rand);
			}
			for (int batch = 0; batch < numBatches; ++batch) {
				log.info("======================================");
				log.info("Epoch " + epoch + " batch " + batch);

				// Each batch will be of the specified batch size, except the
				// last batch will include any leftover trees at the end of
				// the list
				int startTree = batch * model.op.trainOptions.batchSize;
				int endTree = (batch + 1) * model.op.trainOptions.batchSize;
				if (endTree > shuffledSentences.size()) {
					endTree = shuffledSentences.size();
				}

				executeOneTrainingBatch(model, shuffledSentences.subList(startTree, endTree), sumGradSquare);

				long totalElapsed = timing.report();
				log.info("Finished epoch " + epoch + " batch " + batch + "; total training time " + (totalElapsed / 1000 / 60) + " minutes");

				if (maxTrainTimeMillis > 0 && totalElapsed > maxTrainTimeMillis) {
					// no need to debug output, we're done now
					break;
				}

			}

			if (model.op.trainOptions.debugOutputEpochs > 0 && (epoch + 1) % model.op.trainOptions.debugOutputEpochs == 0) {
				double score = 0.0;
				if (devTrees != null) {
					Evaluate eval = new Evaluate(model);
					eval.eval(devTrees);
					eval.printSummary();
					score = eval.exactNodeAccuracy() * 100.0;
				}

				if (score > scoremMAX) {
					scoremMAX = score;
				} else {
					break;
					//						model.saveSerialized(tempPath);	
				}
			}

			long totalElapsed = timing.report();

			if (maxTrainTimeMillis > 0 && totalElapsed > maxTrainTimeMillis) {
				log.info("Max training time exceeded, exiting");
				break;
			}
		}
	}

	public static boolean runGradientCheck(RNNModel model, List<Tree> trees) {
		RNNCostAndGradient gcFunc = new RNNCostAndGradient(model, trees);
		return gcFunc.gradientCheck(model.totalParamSize(), 50, model.paramsToVector());
	}

	void siftOut() throws UnsupportedEncodingException, FileNotFoundException, Exception {
		String trainDataPath = Utility.workingDirectory + "corpus/Phatics/";
		HashSet<String> listNEUTRAL = new HashSet<String>();
		HashSet<String> listPERTINENT = new HashSet<String>();
		for (String sent : new Utility.Text(trainDataPath + "NEUTRAL.txt")) {
			if (sent.startsWith(";")) {
				sent = sent.substring(1);
			}
			SyntacticTree tree = SyntacticParser.instance.parse(sent);

			double probability = RNNPhaticsClassifier.instance.classify(tree);

			if (probability >= threshold) {
				listPERTINENT.add(sent);
			} else {
				listNEUTRAL.add(sent);
			}
		}

		Utility.writeString(trainDataPath + "PERTAIN.txt", listPERTINENT);
		Utility.writeString(trainDataPath + "NEUTRAL.txt", listNEUTRAL);

	}

	public static void main(String[] args) throws Exception {
		instance.establishTrainingCorpus();
		instance.trainingCorpus();
		instance.testRNNModel();
		instance.siftOut();
	}

	static Logger log = Logger.getLogger(RNNPhaticsClassifier.class);
}
