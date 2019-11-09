package com.robot.semantic.RNN;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.ejml.simple.SimpleMatrix;

import com.robot.Sentence;
import com.robot.syntax.SyntacticTree;
import com.robot.syntax.SyntacticTree.BinarizedTree.Combination;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.Timing;

public abstract class RNNTraining {
	public RNNModel model;

	abstract public Combination combination();

	/**
	 * return the probability of being positive
	 * 
	 * @param collapsedUnary
	 * @return
	 */
	public double classify(Tree collapsedUnary) {
		RNNCostAndGradient scorer = new RNNCostAndGradient(model, null);
		scorer.forwardPropagateTree(collapsedUnary);

		//		int sentiment = RNNCoreAnnotations.getPredictedClass(collapsedUnary);
		SimpleMatrix predictions = RNNCoreAnnotations.getPredictions(collapsedUnary);
		return predictions.get(1, 0);
	}

	public double[] classifyManifold(Tree collapsedUnary) {
		RNNCostAndGradient scorer = new RNNCostAndGradient(model, null);
		scorer.forwardPropagateTree(collapsedUnary);

		//		int sentiment = RNNCoreAnnotations.getPredictedClass(collapsedUnary);
		SimpleMatrix predictions = RNNCoreAnnotations.getPredictions(collapsedUnary);
		double[] vec = new double[predictions.numRows()];
		for (int i = 0; i < vec.length; i++) {
			vec[i] = predictions.get(i, 0);
		}
		return vec;
	}

	public double[] classifyManifold(SyntacticTree tree) throws Exception {
		LabeledScoredTreeNode t = tree.toBinarizedTree(combination()).toLabeledScoredTreeNode();
		return classifyManifold(t);
	}

	public double[] classifyManifold(Sentence sent[]) throws Exception {
		if (sent.length > 1) {
			double[] sum = new double[this.model.numClasses];
			double divisor = 0;
			for (Sentence s : sent) {
				LabeledScoredTreeNode t = s.tree().toBinarizedTree(combination()).toLabeledScoredTreeNode();
				double[] score = classifyManifold(t);

				double entropy = s.entropyInformation();
				for (int i = 0; i < sum.length; ++i) {
					sum[i] += score[i] * entropy;
				}
				divisor += entropy;
			}

			for (int i = 0; i < sum.length; ++i) {
				sum[i] /= divisor;
			}
			return sum;
		} else {
			Sentence s = sent[0];
			LabeledScoredTreeNode t = s.tree().toBinarizedTree(combination()).toLabeledScoredTreeNode();
			double[] score = classifyManifold(t);

			return score;
		}
	}

	public double classify(SyntacticTree tree) throws Exception {
		LabeledScoredTreeNode t = tree.toBinarizedTree(combination()).toLabeledScoredTreeNode();
		return classify(t);
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
			if (gradf[feature] != gradf[feature]) {
				continue;
			}
			sumGradSquare[feature] = sumGradSquare[feature] + gradf[feature] * gradf[feature];

			theta[feature] = theta[feature] - (model.op.trainOptions.learningRate * gradf[feature] / (Math.sqrt(sumGradSquare[feature]) + eps));

			if (theta[feature] != theta[feature]) {
				theta[feature] = new Random().nextGaussian() * 0.1;
				log.info("numeric overflow, reset theta[" + feature + "] = " + theta[feature]);
			}
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
			//			log.info("======================================");
			//			log.info("Starting epoch " + epoch);
			if (epoch > 0 && model.op.trainOptions.adagradResetFrequency > 0 && (epoch % model.op.trainOptions.adagradResetFrequency == 0)) {
				log.info("Resetting adagrad weights to " + model.op.trainOptions.initialAdagradWeight);
				Arrays.fill(sumGradSquare, model.op.trainOptions.initialAdagradWeight);
			}

			List<Tree> shuffledSentences = Generics.newArrayList(trainingTrees);
			if (model.op.trainOptions.shuffleMatrices) {
				Collections.shuffle(shuffledSentences, model.rand);
			}
			for (int batch = 0; batch < numBatches; ++batch) {
				//				log.info("======================================");
				//				log.info("Epoch " + epoch + " batch " + batch);

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

	/**
	 * Trains a sentiment model. The -trainPath argument points to a labeled
	 * sentiment treebank. The trees in this data will be used to train the
	 * model parameters (also to seed the model vocabulary). The -devPath
	 * argument points to a second labeled sentiment treebank. The trees in this
	 * data will be used to periodically evaluate the performance of the model.
	 * We won't train on this data; it will only be used to test how well the
	 * model generalizes to unseen data. The -model argument specifies where to
	 * save the learned sentiment model.
	 *
	 * @param args
	 *            Command line arguments
	 */
	String modelPath = null;
	List<Tree> trainingTrees;

	public void trainingCorpus() {
		//		RNNOptions op = new RNNOptions();
		boolean runGradientCheck = false;
		boolean runTraining = true;

		//		boolean filterUnknown = true;

		// read in the trees
		//				List<Tree> trainingTrees = SentimentUtils.readTreesWithGoldLabels(trainPath);
		//		log.info("Read in " + trainingTrees.size() + " training trees");
		//		if (filterUnknown) {
		//			trainingTrees = SentimentUtils.filterUnknownRoots(trainingTrees);
		//			log.info("Filtered training trees: " + trainingTrees.size());
		//		}

		//		for (Tree t : trainingTrees) {
		//			log.info(t.toString());
		//			log.info(((LabeledScoredTreeNode) t).infixExpression());
		//		}

		List<Tree> devTrees = null;

		//		devTrees = SentimentUtils.readTreesWithGoldLabels(devPath);
		//		log.info("Read in " + devTrees.size() + " dev trees");
		//		if (filterUnknown) {
		//			devTrees = SentimentUtils.filterUnknownRoots(devTrees);
		//			log.info("Filtered dev trees: " + devTrees.size());
		//		}
		//
		// TODO: binarize the trees, then collapse the unary chains.
		// Collapsed unary chains always have the label of the top node in
		// the chain
		// Note: the sentiment training data already has this done.
		// However, when we handle trees given to us from the Stanford Parser,
		// we will have to perform this step

		// build an uninitialized SentimentModel from the binary productions
		//		log.info("Sentiment model options:\n" + op);
		//		SentimentModel model = new SentimentModel(op, trainingTrees);

		//		if (op.trainOptions.initialMatrixLogPath != null) {
		//			StringUtils.printToFile(new File(op.trainOptions.initialMatrixLogPath), model.toString(), false, false, "utf-8");
		//		}

		// TODO: need to handle unk rules somehow... at test time the tree
		// structures might have something that we never saw at training
		// time.  for example, we could put a threshold on all of the
		// rules at training time and anything that doesn't meet that
		// threshold goes into the unk.  perhaps we could also use some
		// component of the accepted training rules to build up the "unk"
		// parameter in case there are no rules that don't meet the
		// threshold

		if (runGradientCheck) {
			runGradientCheck(model, trainingTrees);
		}

		if (runTraining) {
			train(model, modelPath, trainingTrees, devTrees);
			model.saveSerialized(modelPath);
		}
	}

	public static void main(String[] args) throws Exception {
	}
	static Logger log = Logger.getLogger(RNNTraining.class);
}
