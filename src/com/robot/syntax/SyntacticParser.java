package com.robot.syntax;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import com.util.Utility;
import com.util.Utility.Printer;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasTag;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.nndep.Classifier;
import edu.stanford.nlp.parser.nndep.Config;
import edu.stanford.nlp.parser.nndep.Dataset;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.parser.nndep.DependencyTree;
import edu.stanford.nlp.parser.nndep.ParsingSystem;
import edu.stanford.nlp.parser.nndep.Util;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;
import edu.stanford.nlp.stats.IntCounter;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.RuntimeInterruptedException;
import edu.stanford.nlp.util.Timing;
import py4j.EntryPoint;

public class SyntacticParser extends DependencyParser {
	public SyntacticParser() {
	}

	static String modelPath = Utility.workingDirectory + "models/dep.gz";

	protected ArcStandard system;

	/**
	 * Prepare for parsing after a model has been loaded.
	 */
	private void initialize(boolean verbose) {
		if (knownLabels == null)
			throw new IllegalStateException("Model has not been loaded or trained");

		// NOTE: remove -NULL-, and then pass the label set to the ParsingSystem
		List<String> lDict = new ArrayList<>(knownLabels);
		lDict.remove(0);

		system = new ArcStandard(config.tlp, lDict, verbose);

		// Pre-compute matrix multiplications
		if (config.numPreComputed > 0) {
			classifier.preCompute();
		}
	}

	protected void loadModelFile(String modelFile, boolean verbose) {
		try {
			log.info("Loading depparse model file: " + modelFile + " ... ");
			Object[] obj = Utility.loadFrom(modelFile, 11);

			int i = 0;
			double[][] W1 = (double[][]) obj[i++];
			double[] b1 = (double[]) obj[i++];
			double[][] W2 = (double[][]) obj[i++];
			double[][] E = (double[][]) obj[i++];

			knownWords = (List<String>) obj[i++];
			knownPos = (List<String>) obj[i++];
			knownLabels = (List<String>) obj[i++];
			wordIDs = (Map<String, Integer>) obj[i++];
			posIDs = (Map<String, Integer>) obj[i++];
			labelIDs = (Map<String, Integer>) obj[i++];
			preComputed = (List<Integer>) obj[i++];

			config.hiddenSize = b1.length;
			config.embeddingSize = E[0].length;
			classifier = new Classifier(config, E, W1, b1, W2, preComputed);
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeIOException(e);
		}

		// initialize the loaded parser
		initialize(verbose);
	}

	public SyntacticParser(String modelPath) {
		loadModelFile(modelPath, false);
	}

	private static final int POS_OFFSET = 18;
	private static final int DEP_OFFSET = 36;
	private static final int STACK_OFFSET = 6;
	private static final int STACK_NUMBER = 6;

	protected int[] getFeatureArray(Configuration c) {
		int[] feature = new int[config.numTokens]; // positions 0-17 hold fWord, 18-35 hold fPos, 36-47 hold fLabel

		for (int j = 2; j >= 0; --j) {
			SyntacticTree index = c.getStack(j);
			feature[2 - j] = getWordID(c.getWord(index));
			feature[POS_OFFSET + (2 - j)] = getPosID(c.getPOS(index));
		}

		for (int j = 0; j <= 2; ++j) {
			SyntacticTree index = c.getBuffer(j);
			feature[3 + j] = getWordID(c.getWord(index));
			feature[POS_OFFSET + 3 + j] = getPosID(c.getPOS(index));
		}

		for (int j = 0; j <= 1; ++j) {
			SyntacticTree k = c.getStack(j);

			SyntacticTree index = c.getLeftChild(k);
			feature[STACK_OFFSET + j * STACK_NUMBER] = getWordID(c.getWord(index));
			feature[POS_OFFSET + STACK_OFFSET + j * STACK_NUMBER] = getPosID(c.getPOS(index));
			feature[DEP_OFFSET + j * STACK_NUMBER] = getLabelID(c.getLabel(index));

			index = c.getRightChild(k);
			feature[STACK_OFFSET + j * STACK_NUMBER + 1] = getWordID(c.getWord(index));
			feature[POS_OFFSET + STACK_OFFSET + j * STACK_NUMBER + 1] = getPosID(c.getPOS(index));
			feature[DEP_OFFSET + j * STACK_NUMBER + 1] = getLabelID(c.getLabel(index));

			index = c.getLeftChild(k, 2);
			feature[STACK_OFFSET + j * STACK_NUMBER + 2] = getWordID(c.getWord(index));
			feature[POS_OFFSET + STACK_OFFSET + j * STACK_NUMBER + 2] = getPosID(c.getPOS(index));
			feature[DEP_OFFSET + j * STACK_NUMBER + 2] = getLabelID(c.getLabel(index));

			index = c.getRightChild(k, 2);
			feature[STACK_OFFSET + j * STACK_NUMBER + 3] = getWordID(c.getWord(index));
			feature[POS_OFFSET + STACK_OFFSET + j * STACK_NUMBER + 3] = getPosID(c.getPOS(index));
			feature[DEP_OFFSET + j * STACK_NUMBER + 3] = getLabelID(c.getLabel(index));

			index = c.getLeftChild(c.getLeftChild(k));
			feature[STACK_OFFSET + j * STACK_NUMBER + 4] = getWordID(c.getWord(index));
			feature[POS_OFFSET + STACK_OFFSET + j * STACK_NUMBER + 4] = getPosID(c.getPOS(index));
			feature[DEP_OFFSET + j * STACK_NUMBER + 4] = getLabelID(c.getLabel(index));

			index = c.getRightChild(c.getRightChild(k));
			feature[STACK_OFFSET + j * STACK_NUMBER + 5] = getWordID(c.getWord(index));
			feature[POS_OFFSET + STACK_OFFSET + j * STACK_NUMBER + 5] = getPosID(c.getPOS(index));
			feature[DEP_OFFSET + j * STACK_NUMBER + 5] = getLabelID(c.getLabel(index));
		}

		return feature;
	}

	/**
	 * Convenience method for {@link #predict(edu.stanford.nlp.util.CoreMap)}.
	 * The tokens of the provided sentence must also have tag annotations (the
	 * parser requires part-of-speech tags).
	 *
	 * @see #predict(edu.stanford.nlp.util.CoreMap)
	 */
	public SyntacticTree parse(List<? extends HasWord> sentence) {
		CoreLabel sentenceLabel = new CoreLabel();
		List<CoreLabel> tokens = new ArrayList<>();

		int i = 1;
		for (HasWord wd : sentence) {
			CoreLabel label;
			if (wd instanceof CoreLabel) {
				label = (CoreLabel) wd;
				if (label.tag() == null)
					throw new IllegalArgumentException("Parser requires words " + "with part-of-speech tag annotations");
			} else {
				label = new CoreLabel();
				label.setValue(wd.word());
				label.setWord(wd.word());

				if (!(wd instanceof HasTag))
					throw new IllegalArgumentException("Parser requires words " + "with part-of-speech tag annotations");

				label.setTag(((HasTag) wd).tag());
			}

			label.setIndex(i);
			i++;

			tokens.add(label);
		}

		sentenceLabel.set(CoreAnnotations.TokensAnnotation.class, tokens);

		return parse(sentenceLabel);
	}

	/**
	 * Determine the dependency parse of the given sentence.
	 * <p>
	 * This "inner" method returns a structure unique to this package; use
	 * {@link #predict(edu.stanford.nlp.util.CoreMap)} for general parsing
	 * purposes.
	 */
	/**
	 * Determine the dependency parse of the given sentence using the loaded
	 * model. You must first load a parser before calling this method.
	 *
	 * @throws java.lang.IllegalStateException
	 *             If parser has not yet been loaded and initialized (see
	 *             {@link #initialize(boolean)}
	 */
	public SyntacticTree parse(CoreMap sentence) {
		int numTrans = system.numTransitions();

		Configuration c = system.initialConfiguration(sentence);
		while (!system.isTerminal(c)) {
			if (Thread.interrupted()) { // Allow interrupting
				throw new RuntimeInterruptedException();
			}

			int[] feature = getFeatureArray(c);

			double[] scores = classifier.computeScores(feature);

			double optScore = Double.NEGATIVE_INFINITY;
			String optTrans = null;

			for (int j = 0; j < numTrans; ++j) {
				if (scores[j] > optScore && system.canApply(c, system.transitions.get(j))) {
					optScore = scores[j];
					optTrans = system.transitions.get(j);
				}
			}

			system.apply(c, optTrans);
		}
		SyntacticTree tree = c.stack.get(0);
		tree = tree.rightChildren.get(0);
		tree.parent = null;
		return tree;
	}

	public static Utility.Listener listener = new Utility.Listener();
	public static int maxIteration = 4;

	public SyntacticTree parseWithAdjustment(CoreMap sentence) throws Exception {
		int numTrans = system.numTransitions();

		Configuration c = system.initialConfiguration(sentence);
		int maxIteration = 10;
		int cntIteration = 0;
		while (!system.isTerminal(c)) {
			//			if (Thread.interrupted()) { // Allow interrupting
			//				throw new RuntimeInterruptedException();
			//			}

			int[] feature = getFeatureArray(c);

			double[] scores = classifier.computeScores(feature);

			Utility.PriorityQueue<Integer> pq = new Utility.PriorityQueue<Integer>(new Comparator<Integer>() {

				@Override
				public int compare(Integer o1, Integer o2) {
					return Double.compare(scores[o1], scores[o2]);
				}
			});

			for (int j = 0; j < numTrans; ++j) {
				if (system.canApply(c, system.transitions.get(j))) {
					//					optScore = scores[j];
					//					optTrans = system.transitions.get(j);
					pq.add(j);
				}
			}

			SyntacticParser.listener.push(new Utility.ICommand() {
				@Override
				public boolean run() throws Exception {
					assert c.toString().equals(status);

					while (true) {
						if (maxTrial <= 0 || priorityQueue.isEmpty()) {
							return true;
						}

						if (system.applyWithAdjustment(c, system.transitions.get(priorityQueue.poll()))) {
							--maxTrial;
							// no need to pop from the command stack							
							return false;
						}
					}
				}

				Utility.PriorityQueue<Integer> priorityQueue = pq;
				String status = c.toString();
				int maxTrial = 1;
			});

			while (true) {
				if (pq.isEmpty()) {
					//exhaust all the possible choices, now we should back-track!
					//in the undoing process, the information of Configuration c will be recovered. 
					if (listener.undo(++cntIteration / maxIteration)) {
						Utility.print(c);
						break;
					}

					return null;
				}

				if (system.applyWithAdjustment(c, system.transitions.get(pq.poll()))) {
					break;
				}
			}
		}
		SyntacticTree tree = c.stack.get(0);
		tree = tree.rightChildren.get(0);
		tree.parent = null;
		listener.clear();
		return tree;
	}

	public SyntacticTree parseWithAdjustment(SyntacticTree tree) throws Exception {
		int numTrans = system.numTransitions();

		Configuration c = system.initialConfiguration(tree);

		while (!system.isTerminal(c)) {
			String oracle = system.getOracle(c, tree);

			int[] featureArr = getFeatureArray(c);

			String[] res = Utility.regexSingleton(oracle, "([LR])\\(null\\)");
			if (res != null) {
				double[] scores = classifier.computeScores(featureArr);

				Utility.PriorityQueue<Integer> pq = new Utility.PriorityQueue<Integer>(new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						return Double.compare(scores[o1], scores[o2]);
					}
				});

				String prefix = res[1];
				for (int j = 0; j < numTrans; ++j) {
					if (system.canApply(c, system.transitions.get(j)) && system.transitions.get(j).startsWith(prefix)) {
						pq.add(j);
					}
				}

				do {
					if (pq.isEmpty()) {
						return null;
					}

					String optTrans = system.transitions.get(pq.poll());
					if (system.applyWithAdjustment(c, optTrans)) {
						break;
					}
				} while (true);
			} else {
				system.apply(c, oracle);
			}

		}
		if (c.stack.size() > 1 || c.getBufferSize() != 0) {
			throw new RuntimeException("c._stack.size() > 1");
		}
		SyntacticTree _tree = c.stack.get(0);
		_tree = _tree.rightChildren.get(0);
		_tree.parent = null;

		return _tree;
	}

	public SyntacticTree parse(SyntacticTree tree) throws Exception {
		int numTrans = system.numTransitions();

		Configuration c = system.initialConfiguration(tree);

		while (!system.isTerminal(c)) {
			String oracle = system.getOracle(c, tree);

			int[] _featureArr = getFeatureArray(c);

			String[] res = Utility.regexSingleton(oracle, "([LR])\\(null\\)");
			if (res != null) {
				oracle = null;
				double[] scores = classifier.computeScores(_featureArr);
				double optScore = Double.NEGATIVE_INFINITY;
				String prefix = res[1];
				for (int j = 0; j < numTrans; ++j) {
					//					if (system.canApply(c, system.transitions.get(j)) != system._canApply(c, system.transitions.get(j))) {
					//						throw new RuntimeException("system.canApply(c, system.transitions.get(j)) != system._canApply(c, system.transitions.get(j))");
					//					}

					if (scores[j] > optScore && system.canApply(c, system.transitions.get(j)) && system.transitions.get(j).startsWith(prefix)) {
						optScore = scores[j];
						oracle = system.transitions.get(j);
					}
				}
				if (oracle == null) {
					throw new RuntimeException("oracle == null");
				}
			}

			system.apply(c, oracle);

		}
		if (c.stack.size() > 1 || c.getBufferSize() != 0) {
			throw new RuntimeException("c._stack.size() > 1");
		}
		SyntacticTree _tree = c.stack.get(0);
		_tree = _tree.rightChildren.get(0);
		_tree.parent = null;

		return _tree;
	}

	public static CoreMap toCoreMap(String seg[], String pos[]) {
		CoreLabel sentenceLabel = new CoreLabel();
		List<CoreLabel> tokens = new ArrayList<>();

		int i = 1;
		for (int j = 0; j < seg.length; ++j) {
			i = j + 1;

			CoreLabel label;
			label = new CoreLabel();
			label.setValue(seg[j]);
			label.setOriginalText(seg[j]);

			label.setWord(Utility.simplifyString(seg[j]));

			label.setTag(pos[j]);

			label.setIndex(i);
			i++;

			tokens.add(label);
		}

		sentenceLabel.set(CoreAnnotations.TokensAnnotation.class, tokens);

		return sentenceLabel;
	}

	public SyntacticTree parse(String seg[], String pos[]) {
		return parse(toCoreMap(seg, pos));
	}

	public SyntacticTree parseWithAdjustment(String seg[], String pos[]) throws Exception {
		return parseWithAdjustment(toCoreMap(seg, pos));
	}

	public SyntacticTree parse(String string) {
		String[] seg = CWSTagger.instance.tag(string);
		String[] pos = POSTagger.instance.tag(seg);
		return parse(seg, pos);
	}

	/**
	 * Train a new dependency parser model.
	 *
	 * @param trainFile
	 *            Training data
	 * @param devFile
	 *            Development data (used for regular UAS evaluation of model)
	 * @param modelFile
	 *            String to which model should be saved
	 * @param embedFile
	 *            File containing word embeddings for words used in training
	 *            corpus
	 */
	public void train(String trainFile, String modelFile, String preModel) {
		String devFile = null;
		String embedFile = null;
		log.info("Train File: " + trainFile);
		log.info("Model File: " + modelFile);
		log.info("Pre-trained Model File: " + preModel);

		log.info("Train File: " + trainFile);
		log.info("Dev File: " + devFile);
		log.info("Model File: " + modelFile);
		log.info("Embedding File: " + embedFile);
		log.info("Pre-trained Model File: " + preModel);

		List<CoreMap> trainSents = new ArrayList<>();
		List<DependencyTree> trainTrees = new ArrayList<>();
		try {
			for (SyntacticTree inst : new DependencyTreeReader(trainFile)) {
				log.info("\n" + inst.toString());
				log.info("\n" + inst.toString());
				//				trainSents.add(inst.sent);
				//				trainTrees.add(inst.tree);
			}
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		Util.loadConllFile(trainFile, trainSents, trainTrees, config.unlabeled, config.cPOS);
		Util.printTreeStats("Train", trainTrees);

		List<CoreMap> devSents = new ArrayList<>();
		List<DependencyTree> devTrees = new ArrayList<>();
		if (devFile != null) {
			Util.loadConllFile(devFile, devSents, devTrees, config.unlabeled, config.cPOS);
			Util.printTreeStats("Dev", devTrees);
		}
		generateDictionaries(null);

		//NOTE: remove -NULL-, and the pass it to ParsingSystem
		List<String> lDict = new ArrayList<>(knownLabels);
		lDict.remove(0);
		system = new ArcStandard(config.tlp, lDict, true);

		// Initialize a classifier; prepare for training
		setupClassifierForTraining(trainSents, trainTrees, embedFile, preModel);

		log.info(Config.SEPARATOR);
		config.printParameters();

		long startTime = System.currentTimeMillis();
		/**
		 * Track the best UAS performance we've seen.
		 */
		double bestUAS = 0;

		for (int iter = 0; iter < config.maxIter; ++iter) {
			log.info("##### Iteration " + iter);

			Classifier.Cost cost = classifier.computeCostFunction(config.batchSize, config.regParameter, config.dropProb);
			log.info("Cost = " + cost.getCost() + ", Correct(%) = " + cost.getPercentCorrect());
			classifier.takeAdaGradientStep(cost, config.adaAlpha, config.adaEps);

			log.info("Elapsed Time: " + (System.currentTimeMillis() - startTime) / 1000.0 + " (s)");

			// UAS evaluation
			if (devFile != null && iter % config.evalPerIter == 0) {
				// Redo precomputation with updated weights. This is only
				// necessary because we're updating weights -- for normal
				// prediction, we just do this once in #initialize
				classifier.preCompute();

				List<DependencyTree> predicted = devSents.stream().map(this::predictInner).collect(toList());

				double uas = config.noPunc ? system.getUASnoPunc(devSents, predicted, devTrees) : system.getUAS(devSents, predicted, devTrees);
				log.info("UAS: " + uas);

				if (config.saveIntermediate && uas > bestUAS) {
					System.err.printf("Exceeds best previous UAS of %f. Saving model file..%n", bestUAS);

					bestUAS = uas;
					writeModelFile(modelFile);
				}
			}

			// Clear gradients
			if (config.clearGradientsPerIter > 0 && iter % config.clearGradientsPerIter == 0) {
				log.info("Clearing gradient histories..");
				classifier.clearGradientHistories();
			}
		}

		classifier.finalizeTraining();

		if (devFile != null) {
			// Do final UAS evaluation and save if final model beats the
			// best intermediate one
			List<DependencyTree> predicted = devSents.stream().map(this::predictInner).collect(toList());
			double uas = config.noPunc ? system.getUASnoPunc(devSents, predicted, devTrees) : system.getUAS(devSents, predicted, devTrees);

			if (uas > bestUAS) {
				System.err.printf("Final model UAS: %f%n", uas);
				System.err.printf("Exceeds best previous UAS of %f. Saving model file..%n", bestUAS);

				writeModelFile(modelFile);
			}
		} else {
			writeModelFile(modelFile);
		}
	}

	/**
	 * Train a pre-trained ]dependency parser model.
	 *
	 * @param trainFile
	 *            Training data
	 * @param devFile
	 *            Development data (used for regular UAS evaluation of model)
	 * @param modelFile
	 *            String to which model should be saved
	 * @param embedFile
	 *            File containing word embeddings for words used in training
	 *            corpus
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public void train(String trainFile, String modelFile) throws UnsupportedEncodingException, FileNotFoundException {
		String devFile = null;
		String embedFile = null;
		log.info("Train File: " + trainFile);
		log.info("Model File: " + modelFile);

		log.info("Train File: " + trainFile);
		log.info("Dev File: " + devFile);
		log.info("Model File: " + modelFile);
		log.info("Embedding File: " + embedFile);

		List<SyntacticTree> trainTrees = new DependencyTreeReader(trainFile).collect();
		//		Util.loadConllFile(trainFile, trainSents, trainTrees, config.unlabeled, config.cPOS);
		//		Util.printTreeStats("Train", trainTrees);

		List<CoreMap> devSents = new ArrayList<>();
		List<DependencyTree> devTrees = new ArrayList<>();

		generateDictionaries(trainTrees);

		//NOTE: remove -NULL-, and the pass it to ParsingSystem
		List<String> lDict = new ArrayList<>(knownLabels);
		lDict.remove(0);
		system = new ArcStandard(config.tlp, lDict, true);

		// Initialize a classifier; prepare for training
		setupClassifierForTraining(trainTrees);

		log.info(Config.SEPARATOR);
		config.printParameters();

		long startTime = System.currentTimeMillis();
		/**
		 * Track the best UAS performance we've seen.
		 */
		double bestUAS = 0;

		int iterationPeroid = 50;
		double score = 0;
		double scoreMax = -1;
		while (score > scoreMax) {
			scoreMax = score;
			score = 0;
			for (int iter = 0; iter < iterationPeroid; ++iter) {
				log.info("##### Iteration " + iter);

				Classifier.Cost cost = classifier.computeCostFunction(config.batchSize, config.regParameter, config.dropProb);
				log.info("Cost = " + cost.getCost() + ", Correct(%) = " + cost.getPercentCorrect());
				classifier.takeAdaGradientStep(cost, config.adaAlpha, config.adaEps);

				score += cost.getPercentCorrect();
				log.info("Elapsed Time: " + (System.currentTimeMillis() - startTime) / 1000.0 + " (s)");

				// UAS evaluation
				if (devFile != null && iter % config.evalPerIter == 0) {
					// Redo precomputation with updated weights. This is only
					// necessary because we're updating weights -- for normal
					// prediction, we just do this once in #initialize
					classifier.preCompute();

					List<DependencyTree> predicted = devSents.stream().map(this::predictInner).collect(toList());

					double uas = config.noPunc ? system.getUASnoPunc(devSents, predicted, devTrees) : system.getUAS(devSents, predicted, devTrees);
					log.info("UAS: " + uas);

					if (config.saveIntermediate && uas > bestUAS) {
						System.err.printf("Exceeds best previous UAS of %f. Saving model file..%n", bestUAS);

						bestUAS = uas;
						writeModelFile(modelFile);
					}
				}

				// Clear gradients
				if (config.clearGradientsPerIter > 0 && iter % config.clearGradientsPerIter == 0) {
					log.info("Clearing gradient histories..");
					classifier.clearGradientHistories();
				}
			}
		}

		classifier.finalizeTraining();

		writeModelFile(modelFile);
	}

	public void writeModelFile(String modelFile) {
		try {
			Utility.saveTo(modelFile,

					classifier.getW1(), classifier.getb1(), classifier.getW2(), classifier.getE(),

					knownWords, knownPos, knownLabels, wordIDs, posIDs, labelIDs,

					preComputed);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Scan a corpus and store all words, part-of-speech tags, and dependency
	 * relation labels observed. Prepare other structures which support word /
	 * POS / label lookup at train- / run-time.
	 */
	protected void generateDictionaries(List<SyntacticTree> _trees) {
		// Collect all words (!), etc. in lists, tacking on one sentence
		// after the other
		List<String> word = new ArrayList<>();
		List<String> pos = new ArrayList<>();
		List<String> label = new ArrayList<>();
		String rootLabel = null;

		for (SyntacticTree tree : _trees) {
			for (SyntacticTree t : tree) {
				word.add(t.seg);
				pos.add(t.pos);
				if (t.parent == null) {
					rootLabel = t.dep;
				} else {
					label.add(t.dep);
				}
			}
		}

		// Generate "dictionaries," possibly with frequency cutoff
		knownWords = Util.generateDict(word, config.wordCutOff);
		knownPos = Util.generateDict(pos);
		knownLabels = Util.generateDict(label);
		knownLabels.add(0, rootLabel);

		// Avoid the case that rootLabel equals to one of the other labels
		for (int k = 1; k < knownLabels.size(); ++k)
			if (knownLabels.get(k).equals(rootLabel)) {
				knownLabels.remove(k);
				break;
			}

		knownWords.add(0, Config.UNKNOWN);
		knownWords.add(1, Config.NULL);
		knownWords.add(2, Config.ROOT);

		knownPos.add(0, Config.UNKNOWN);
		knownPos.add(1, Config.NULL);
		knownPos.add(2, Config.ROOT);

		knownLabels.add(0, Config.NULL);

		generateIDs();

		log.info(Config.SEPARATOR);
		log.info("#Word: " + knownWords.size());
		log.info("#POS:" + knownPos.size());
		log.info("#Label: " + knownLabels.size());
	}

	/**
	 * Generate unique integer IDs for all known words / part-of-speech tags /
	 * dependency relation labels.
	 *
	 * All three of the aforementioned types are assigned IDs from a continuous
	 * range of integers; all IDs 0 <= ID < n_w are word IDs, all IDs n_w <= ID
	 * < n_w + n_pos are POS tag IDs, and so on.
	 * 
	 * @throws Exception
	 */
	Map<String, double[]> wordEnbedding = new HashMap<>(), posEnbedding = new HashMap<>(), labelEnbedding = new HashMap<>();

	private void generateIDs() {
		double[][] E = classifier.getE();
		int index = wordIDs.size() + posIDs.size() + labelIDs.size();
		int maxIndex = -1;
		for (Map.Entry<String, Integer> entry : wordIDs.entrySet()) {
			wordEnbedding.put(entry.getKey(), E[entry.getValue()]);
			if (entry.getValue() > maxIndex) {
				maxIndex = entry.getValue();
			}
		}

		for (Map.Entry<String, Integer> entry : posIDs.entrySet()) {
			posEnbedding.put(entry.getKey(), E[entry.getValue()]);
			if (entry.getValue() > maxIndex) {
				maxIndex = entry.getValue();
			}
		}

		for (Map.Entry<String, Integer> entry : labelIDs.entrySet()) {
			labelEnbedding.put(entry.getKey(), E[entry.getValue()]);
			if (entry.getValue() > maxIndex) {
				maxIndex = entry.getValue();
			}
		}

		++maxIndex;
		if (maxIndex != index) {
			throw new RuntimeException("maxIndex != index");
		}

		wordIDs = new HashMap<>();
		posIDs = new HashMap<>();
		labelIDs = new HashMap<>();

		index = 0;
		for (String word : knownWords)
			wordIDs.put(word, (index++));
		for (String pos : knownPos)
			posIDs.put(pos, (index++));
		for (String label : knownLabels)
			labelIDs.put(label, (index++));
	}

	/**
	 * Prepare a classifier for training with the given dataset.
	 */
	protected void setupClassifierForTraining(List<SyntacticTree> _trainTrees) {
		//		double[][] W1 = new double[config.hiddenSize][config.embeddingSize * config.numTokens];
		//		double[] b1 = new double[config.hiddenSize];
		//		double[][] W2 = new double[system.numTransitions()][config.hiddenSize];

		double[][] E = new double[knownWords.size() + knownPos.size() + knownLabels.size()][];

		for (String word : knownWords) {
			double[] enbedding = wordEnbedding.get(word);
			if (enbedding == null) {
				E[wordIDs.get(word)] = new double[config.embeddingSize];
			} else
				E[wordIDs.get(word)] = enbedding;
		}

		for (String pos : knownPos) {
			double[] enbedding = posEnbedding.get(pos);
			if (enbedding == null) {
				E[posIDs.get(pos)] = new double[config.embeddingSize];
			} else
				E[posIDs.get(pos)] = enbedding;

		}

		for (String dep : knownLabels) {
			double[] enbedding = labelEnbedding.get(dep);
			if (enbedding == null) {
				E[labelIDs.get(dep)] = new double[config.embeddingSize];
			} else
				E[labelIDs.get(dep)] = enbedding;

		}

		double[][] W1 = classifier.getW1();
		double[] b1 = classifier.getb1();
		double[][] W2 = classifier.getW2();

		Dataset trainSet = genTrainingExamples(_trainTrees);
		classifier = new Classifier(config, trainSet, E, W1, b1, W2, preComputed);
	}

	public List<Integer> getFeatures(Configuration c) {
		// Presize the arrays for very slight speed gain. Hardcoded, but so is the current feature list.
		List<Integer> fWord = new ArrayList<>(18);
		List<Integer> fPos = new ArrayList<>(18);
		List<Integer> fLabel = new ArrayList<>(12);
		for (int j = 2; j >= 0; --j) {
			SyntacticTree index = c.getStack(j);
			fWord.add(getWordID(c.getWord(index)));
			fPos.add(getPosID(c.getPOS(index)));
		}
		for (int j = 0; j <= 2; ++j) {
			SyntacticTree index = c.getBuffer(j);
			fWord.add(getWordID(c.getWord(index)));
			fPos.add(getPosID(c.getPOS(index)));
		}
		for (int j = 0; j <= 1; ++j) {
			SyntacticTree k = c.getStack(j);
			SyntacticTree index = c.getLeftChild(k);
			fWord.add(getWordID(c.getWord(index)));
			fPos.add(getPosID(c.getPOS(index)));
			fLabel.add(getLabelID(c.getLabel(index)));

			index = c.getRightChild(k);
			fWord.add(getWordID(c.getWord(index)));
			fPos.add(getPosID(c.getPOS(index)));
			fLabel.add(getLabelID(c.getLabel(index)));

			index = c.getLeftChild(k, 2);
			fWord.add(getWordID(c.getWord(index)));
			fPos.add(getPosID(c.getPOS(index)));
			fLabel.add(getLabelID(c.getLabel(index)));

			index = c.getRightChild(k, 2);
			fWord.add(getWordID(c.getWord(index)));
			fPos.add(getPosID(c.getPOS(index)));
			fLabel.add(getLabelID(c.getLabel(index)));

			index = c.getLeftChild(c.getLeftChild(k));
			fWord.add(getWordID(c.getWord(index)));
			fPos.add(getPosID(c.getPOS(index)));
			fLabel.add(getLabelID(c.getLabel(index)));

			index = c.getRightChild(c.getRightChild(k));
			fWord.add(getWordID(c.getWord(index)));
			fPos.add(getPosID(c.getPOS(index)));
			fLabel.add(getLabelID(c.getLabel(index)));
		}

		List<Integer> feature = new ArrayList<>(48);
		feature.addAll(fWord);
		feature.addAll(fPos);
		feature.addAll(fLabel);
		return feature;
	}

	public Dataset genTrainingExamples(List<SyntacticTree> _trees) {
		int numTrans = system.numTransitions();
		Dataset ret = new Dataset(config.numTokens, numTrans);

		Counter<Integer> tokPosCount = new IntCounter<>();
		log.info(Config.SEPARATOR);
		log.info("Generate training examples...");

		for (int i = 0; i < _trees.size(); ++i) {

			if (i > 0) {
				if (i % 1000 == 0)
					log.info(i + " ");
				if (i % 10000 == 0 || i == _trees.size() - 1)
					log.info("\n");
			}

			Configuration c = system.initialConfiguration(_trees.get(i));

			while (!system.isTerminal(c)) {
				String oracle = system.getOracle(c, _trees.get(i));

				List<Integer> _feature = getFeatures(c);

				List<Integer> label = new ArrayList<>();
				for (int j = 0; j < numTrans; ++j) {
					String str = system.transitions.get(j);
					//					if (system.canApply(c, str) != system._canApply(c, str)) {
					//						throw new RuntimeException("system.canApply(c, str) != system._canApply(c, str)");
					//					}

					if (str.equals(oracle))
						label.add(1);
					else if (system.canApply(c, str))
						label.add(0);
					else
						label.add(-1);
				}

				ret.addExample(_feature, label);
				int mE = knownWords.size() + knownPos.size() + knownLabels.size();
				for (int j = 0; j < _feature.size(); ++j) {
					if (_feature.get(j) >= mE) {
						throw new RuntimeException("feature.get(" + j + ") = " + _feature.get(j) + " >= " + mE);
					}
					tokPosCount.incrementCount(_feature.get(j) * _feature.size() + j);
				}
				system.apply(c, oracle);
			}

		}
		log.info("#Train Examples: " + ret.n);

		List<Integer> sortedTokens = Counters.toSortedList(tokPosCount, false);
		preComputed = new ArrayList<>(sortedTokens.subList(0, Math.min(config.numPreComputed, sortedTokens.size())));

		return ret;
	}

	public Set<String> tagSet() {
		return new HashSet<String>(system.labels);
	}

	void transformLabel(String oldLabel, String newLabel) throws IOException {
		ArrayList<SyntacticTree> list = new ArrayList<SyntacticTree>();
		for (SyntacticTree goldTree : new DependencyTreeReader()) {
			list.add(goldTree);
		}

		int index = system.transitions.indexOf("L(" + oldLabel + ")");
		if (index >= 0) {
			system.transitions.set(index, "L(" + newLabel + ")");
		}
		index = system.transitions.indexOf("R(" + oldLabel + ")");
		if (index >= 0) {
			system.transitions.set(index, "R(" + newLabel + ")");
		}

		if (labelIDs.containsKey(oldLabel)) {
			index = this.labelIDs.get(oldLabel);
			labelIDs.remove(oldLabel);
			labelIDs.put(newLabel, index);
		}

		index = knownLabels.indexOf(oldLabel);
		if (index >= 0) {
			knownLabels.set(index, newLabel);
		}

		index = system.labels.indexOf(oldLabel);
		if (index >= 0) {
			system.labels.set(index, newLabel);
			if (index == 0) {
				system.rootLabel = system.labels.get(0);
			}
		}

		writeModelFile(modelPath);

		ArrayList<String> arr = new ArrayList<String>();
		for (SyntacticTree goldTree : list) {
			goldTree.transformLabel(oldLabel, newLabel);
			String seg[] = goldTree.getLEX();
			String pos[] = goldTree.getPOS();

			SyntacticTree predTree = this.parse(seg, pos);
			if (!goldTree.equals(predTree)) {
				log.info("pred tree = \n" + predTree);
				throw new RuntimeException("!goldTree.equals(predTree)");
			}

			arr.add(goldTree.toStringNonHierarchical());
		}

		Utility.writeString(DependencyTreeReader.depCorpus, arr);
		DependencyTreeReader.initializeDEPSupportedTags();
		test();
	}

	void transformLabelOdd(String oldLabel, String newLabel) throws IOException {
		ArrayList<SyntacticTree> list = new ArrayList<SyntacticTree>();
		for (SyntacticTree goldTree : new DependencyTreeReader()) {
			list.add(goldTree);
		}

		int index = system.transitions.indexOf(oldLabel);
		if (index >= 0) {
			system.transitions.set(index, newLabel);
		}

		oldLabel = oldLabel.substring(2, oldLabel.length() - 1);
		newLabel = newLabel.substring(2, newLabel.length() - 1);

		if (labelIDs.containsKey(oldLabel)) {
			index = this.labelIDs.get(oldLabel);
			labelIDs.remove(oldLabel);
			labelIDs.put(newLabel, index);
		}

		index = knownLabels.indexOf(oldLabel);
		if (index >= 0) {
			knownLabels.set(index, newLabel);
		}

		index = system.labels.indexOf(oldLabel);
		if (index >= 0) {
			system.labels.set(index, newLabel);
			if (index == 0) {
				system.rootLabel = system.labels.get(0);
			}
		}

		writeModelFile(modelPath);

		ArrayList<String> arr = new ArrayList<String>();
		for (SyntacticTree goldTree : list) {
			goldTree.transformLabel(oldLabel, newLabel);
			String seg[] = goldTree.getLEX();
			String pos[] = goldTree.getPOS();

			SyntacticTree predTree = this.parse(seg, pos);
			if (!goldTree.equals(predTree)) {
				log.info("pred tree = \n" + predTree);
				throw new RuntimeException("!goldTree.equals(predTree)");
			}

			arr.add(goldTree.toStringNonHierarchical());
		}

		Utility.writeString(DependencyTreeReader.depCorpus, arr);
		DependencyTreeReader.initializeDEPSupportedTags();
		test();
	}

	void transformLabel() throws IOException {

		//		String oldLabel[] = { "ROOT", "JJ", "AD", "SUJ", "OBJ", "PU", "DE", "VA", "P", "IJ", "CS", "AS", "CC", "CD", "SUP", "O" };
		//		String newLabel[] = { "root", "adj", "adv", "suj", "obj", "pu", "de", "va", "p", "ij", "cs", "as", "cc", "nt", "sup", "o" };

		String oldLabel[] = { "cd", };
		String newLabel[] = { "nt", };
		for (int i = 0; i < oldLabel.length; ++i) {
			transformLabel(oldLabel[i], newLabel[i]);
		}
	}

	public static void main(String[] args) {
		try {

			//			for (SyntacticTree inst : new DependencyTreeReader(trainFile)) {
			//				String sent = inst.unadornedExpression();
			//				sent = Utility.removeEndOfSentencePunctuation(sent);
			//
			//				System.out.println(inst.toStringNonHierarchical());
			//			}

			//			instance.trainingCorpus();
			//						instance.test();
			//			instance.transformLabelOdd("L(root)", "L(of)");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int test() throws IOException {
		int error = 0;
		int total = 0;
		Printer printer = new Printer(Utility.workingDirectory + "/corpus/debug.dep.txt");
		ArrayList<SyntacticTree> arrayGold = new ArrayList<SyntacticTree>();
		ArrayList<SyntacticTree> arrayError = new ArrayList<SyntacticTree>();
		for (SyntacticTree goldTree : new DependencyTreeReader()) {
			//			log.info("\n" + inst.toString());
			//			log.info("\n" + inst.toString());
			String seg[] = goldTree.getLEX();
			String pos[] = goldTree.getPOS();

			SyntacticTree predTree = this.parse(seg, pos);
			if (!goldTree.equals(predTree)) {
				++error;
				System.out.println("gold tree = \n" + goldTree);
				System.out.println("pred tree = \n" + predTree);
				arrayError.add(goldTree);
				//				arrayError.add(predTree);
			} else {
				arrayGold.add(goldTree);
			}
			++total;
		}

		System.out.println("error = " + error);
		System.out.println("total = " + total);
		System.out.println("correctness = " + (total - error) * 1.0 / total);
		printer.close();
		return error;
	}

	public void trainingCorpus() throws Exception {
		train(DependencyTreeReader.depCorpus, modelPath);

		List<String> knownWords = instance.knownWords;
		List<String> knownPos = instance.knownPos;
		List<String> knownLabels = instance.knownLabels;

		Map<String, Integer> wordIDs = instance.wordIDs;
		Map<String, Integer> posIDs = instance.posIDs;
		Map<String, Integer> labelIDs = instance.labelIDs;

		List<Integer> preComputed = instance.preComputed;

		int hiddenSize = instance.config.hiddenSize;
		int embeddingSize = instance.config.embeddingSize;

		double[][] E = instance.classifier.getE();
		double[][] W1 = instance.classifier.getW1();
		double[] b1 = instance.classifier.getb1();
		double[][] W2 = instance.classifier.getW2();

		//		instance.test();
		instance = null;
		instance = new SyntacticParser(modelPath);
		List<String> _knownWords = instance.knownWords;

		if (!Utility.equals(_knownWords, knownWords)) {
			throw new RuntimeException("!Utility.equals(_knownWords, knownWords)");
		}

		List<String> _knownPos = instance.knownPos;
		if (!Utility.equals(_knownPos, knownPos)) {
			throw new RuntimeException("!Utility.equals(_knownPos, knownPos)");
		}

		List<String> _knownLabels = instance.knownLabels;
		if (!Utility.equals(_knownLabels, knownLabels)) {
			throw new RuntimeException("!Utility.equals(_knownLabels, knownLabels)");
		}

		Map<String, Integer> _wordIDs = instance.wordIDs;
		if (!Utility.equals(_wordIDs, wordIDs)) {
			throw new RuntimeException("!Utility.equals(_wordIDs, wordIDs)");
		}

		Map<String, Integer> _posIDs = instance.posIDs;
		if (!Utility.equals(_posIDs, posIDs)) {
			throw new RuntimeException("!Utility.equals(_posIDs, posIDs)");
		}

		Map<String, Integer> _labelIDs = instance.labelIDs;
		if (!Utility.equals(_labelIDs, labelIDs)) {
			throw new RuntimeException("!Utility.equals(_labelIDs, labelIDs)");
		}

		List<Integer> _preComputed = instance.preComputed;
		if (!Utility.equals(_preComputed, preComputed)) {
			throw new RuntimeException("!Utility.equals(_preComputed, preComputed)");
		}

		int _hiddenSize = instance.config.hiddenSize;
		if (_hiddenSize != hiddenSize) {
			throw new RuntimeException("_hiddenSize != hiddenSize");
		}

		int _embeddingSize = instance.config.embeddingSize;
		if (_embeddingSize != embeddingSize) {
			throw new RuntimeException("_embeddingSize != embeddingSize");
		}

		double[][] _E = instance.classifier.getE();
		if (!Utility.equals(_E, E)) {
			throw new RuntimeException("!Utility.equals(_E, E)");
		}

		double[][] _W1 = instance.classifier.getW1();
		if (!Utility.equals(_W1, W1)) {
			throw new RuntimeException("!Utility.equals(_W1, W1)");
		}

		double[] _b1 = instance.classifier.getb1();
		if (!Utility.equals(_b1, b1)) {
			throw new RuntimeException("!Utility.equals(_b1, b1)");
		}

		double[][] _W2 = instance.classifier.getW2();
		if (!Utility.equals(_W2, W2)) {
			throw new RuntimeException("!Utility.equals(_W2, W2)");
		}

		log.info("Corpus has been trained successfully.");
		instance.test();
		EntryPoint.instance.notify("SyntacticParser has been trained successfully.");
	}

	private static Logger log = Logger.getLogger(SyntacticParser.class);
	public static SyntacticParser instance;
	static {
		try {
			instance = new SyntacticParser(modelPath);
		} catch (Exception e) {
			e.printStackTrace();
			instance = new SyntacticParser();
		}
	}

}
