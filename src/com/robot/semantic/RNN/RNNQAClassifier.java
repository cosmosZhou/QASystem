package com.robot.semantic.RNN;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.Timing;

public class RNNQAClassifier extends RNNTraining {
	static public RNNQAClassifier instance = new RNNQAClassifier();
	final static public double threshold = 0.50;
	static HashMap<String, String> lexiconMap = new HashMap<String, String>();

	static {
		try {
			for (String str : new Utility.Text(Utility.workingDirectory + "models/lexicon.txt")) {
				String[] res = Utility.regexSingleton(str, "(\\S+)\\s*=([\\S\\s]+)");
				if (res != null && res.length == 3) {
					String category = res[1];
					for (String noun : res[2].split("\\s+")) {
						lexiconMap.put(noun, category);
					}
				}
			}
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public RNNQAClassifier() {
		try {
			modelPath = Utility.workingDirectory + "models/RNNQAClassifier.gz";
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

	public static Combination combination = new Combination() {
		@Override
		public String combine(String seg, String pos) {
			switch (pos) {
			case "NR":
			case "VI":
			case "VBG":
			case "CD":
			case "NT":
			case "O":
			case "M":
			case "LC":
			case "JJ":
			case "DT":
				return pos;
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
				return seg + "/" + pos;
			case "PU":
				switch (seg.charAt(0)) {
				case '?':
				case '？':
					return "?";
				default:
					return pos;
				}

			case "AD":
				switch (seg) {
				case "难道":
				case "难到":
				case "就":
				case "到底":
					return seg + "/" + pos;

				default:
					return pos;
				}
			case "VT":
				if ("vt".equals(lexiconMap.get(seg))) {
					return seg + "/" + pos;
				}
				return pos;
			case "VA":
				switch (Utility.last(seg)) {
				case '否':
				case '不':
				case '吗':
					return "NEG";
				case '吧':
					return "OK";
				}
				return pos;
			case "NN": {
				String ner = lexiconMap.get(seg);
				if (ner != null) {
					return ner;
				}
				return pos;
			}
			default:
				throw new RuntimeException(pos);
			}
		}
	};

	public Combination combination() {
		return combination;
	}

	public void establishTrainingCorpus() throws Exception {
		BinarizedTree.map.clear();
		int errorDeclarative = 0;
		int sumDeclarative = 0;
		int errorInterrogative = 0;
		int sumInterrogative = 0;

		String trainDataPath = Utility.workingDirectory + "corpus/QACLASSIFIER/";
		ArrayList<String> queryList = new ArrayList<String>();
		ArrayList<String> replyList = new ArrayList<String>();
		trainingTrees = new ArrayList<Tree>();
		ArrayList<LabeledScoredTreeNode> list = new ArrayList<LabeledScoredTreeNode>();

		for (com.robot.util.Instance inst : new InstanceReader(trainDataPath)) {
			String sent = (String) inst.getData();
			//				Utility.removeEndOfSentencePunctuation(sent);

			Sentence sentence = new Sentence(sent);
			//			System.out.println(sentence);
			BinarizedTree bTree = sentence.tree().toBinarizedTreeWithGoldLabel(combination);
			BinarizedTree bTreeForTraining = sentence.tree().toBinarizedTree(combination);

			list.add(bTree.toLabeledScoredTreeNode());
			String target = (String) inst.getTarget();

			switch (target) {
			case "QUERY":
				++sumInterrogative;

				bTreeForTraining.classtype = 1;
				trainingTrees.add(bTreeForTraining.toLabeledScoredTreeNode());
				break;
			case "REPLY":
				++sumDeclarative;

				bTreeForTraining.classtype = 0;
				trainingTrees.add(bTreeForTraining.toLabeledScoredTreeNode());
				break;
			default:
				System.out.println(sent);
				throw new RuntimeException("String target = " + target);
			}
		}

		model.initRandomWordVectors(list);

		DecimalFormat df = new DecimalFormat("00.00");
		int error = errorDeclarative + errorInterrogative;
		int sum = sumDeclarative + sumInterrogative;
		System.out.println("correct percent for Declarative = " + df.format((sumDeclarative - errorDeclarative) * 1.0 / sumDeclarative * 100) + "%");
		System.out.println("correct percent for Interrogative = " + df.format((sumInterrogative - errorInterrogative) * 1.0 / sumInterrogative * 100) + "%");

		System.out.println("correct percent = " + df.format((sum - error) * 1.0 / sum * 100) + "%");
		System.out.println("total sentences  = " + sum);
	}

	public void testRNNModel() throws Exception {
		int errorDeclarative = 0;
		int sumDeclarative = 0;
		int errorInterrogative = 0;
		int sumInterrogative = 0;

		String trainDataPath = Utility.workingDirectory + "corpus/QACLASSIFIER/";
		ArrayList<String> queryErrorList = new ArrayList<String>();
		ArrayList<String> replyErrorList = new ArrayList<String>();
		ArrayList<String> queryList = new ArrayList<String>();
		ArrayList<String> replyList = new ArrayList<String>();
		for (com.robot.util.Instance inst : new InstanceReader(trainDataPath)) {
			String sent = (String) inst.getData();
			//				Utility.removeEndOfSentencePunctuation(sent);

			SyntacticTree tree = SyntacticParser.instance.parse(sent);
			//				System.out.println(sentence.tree());
			//			BinarizedTree bTree = sentence.tree().toBinarizedTree();
			//				System.out.println(bTree.toString());
			//				System.out.println(bTree.infixExpression());
			//			LabeledScoredTreeNode labeledScoredTreeNode = bTree.toLabeledScoredTreeNode();

			String target = (String) inst.getTarget();

			double probability = RNNQAClassifier.instance.classify(tree);
			String targetPred = "NEUTRAL";
			if (probability > threshold)
				targetPred = "QUERY";
			else
				targetPred = "REPLY";
			switch (target) {
			case "QUERY":
				++sumInterrogative;
				if (!target.equals(targetPred)) {
					++errorInterrogative;
					System.out.println(sent);
					queryErrorList.add(";" + sent);
				}
				queryList.add(sent);
				break;
			case "REPLY":
				++sumDeclarative;
				if (!target.equals(targetPred)) {
					++errorDeclarative;
					System.out.println(sent);
					replyErrorList.add(";" + sent);
				}
				replyList.add(sent);
				break;
			default:
				System.out.println(sent);
				throw new RuntimeException("String target = " + target);
			}
		}

		DecimalFormat df = new DecimalFormat("00.00");
		int error = errorDeclarative + errorInterrogative;
		int sum = sumDeclarative + sumInterrogative;
		System.out.println("correct percent for Declarative = " + df.format((sumDeclarative - errorDeclarative) * 1.0 / sumDeclarative * 100) + "%");
		System.out.println("correct percent for Interrogative = " + df.format((sumInterrogative - errorInterrogative) * 1.0 / sumInterrogative * 100) + "%");

		System.out.println("correct percent = " + df.format((sum - error) * 1.0 / sum * 100) + "%");
		System.out.println("total sentences  = " + sum);

		Utility.writeString(trainDataPath + "REPLY.data", replyList);
		Utility.writeString(trainDataPath + "QUERY.data", queryList);

		Utility.prependString(trainDataPath + "REPLY.data", replyErrorList);
		Utility.prependString(trainDataPath + "QUERY.data", queryErrorList);

		for (int i = 0; i < replyErrorList.size(); ++i) {
			replyErrorList.set(i, replyErrorList.get(i).substring(1));
		}

		for (int i = 0; i < queryErrorList.size(); ++i) {
			queryErrorList.set(i, queryErrorList.get(i).substring(1));
		}

		Utility.removeString(trainDataPath + "REPLY.data", replyErrorList);
		Utility.removeString(trainDataPath + "QUERY.data", queryErrorList);
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

	public static void main(String[] args) throws Exception {
		instance.establishTrainingCorpus();
		//		if (BinarizedTree.bErrorDiscrepancy) {
		//			BinarizedTree.bErrorDiscrepancy = false;
		//			return;
		//		}

		instance.trainingCorpus();
		instance.testRNNModel();
	}

	static Logger log = Logger.getLogger(RNNQAClassifier.class);
}
