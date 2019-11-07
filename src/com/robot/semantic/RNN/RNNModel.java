package com.robot.semantic.RNN;

import edu.stanford.nlp.util.logging.Redwood;

import java.io.Serializable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.neural.Embedding;
import edu.stanford.nlp.neural.NeuralUtils;
import edu.stanford.nlp.neural.SimpleTensor;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Generics;

public class RNNModel implements Serializable {

	/** A logger for this class */
	private static Redwood.RedwoodChannels log = Redwood.channels(RNNModel.class);
	/**
	 * Nx2N+1, where N is the size of the word vectors
	 */
	public final Map<String, SimpleMatrix> binaryTransform;

	/**
	 * 2Nx2NxN, where N is the size of the word vectors
	 */
	public final Map<String, SimpleTensor> binaryTensors;

	/**
	 * CxN+1, where N = size of word vectors, C is the number of classes
	 */
	public final Map<String, SimpleMatrix> binaryClassification;

	/**
	 * CxN+1, where N = size of word vectors, C is the number of classes
	 */
	public final Map<String, SimpleMatrix> unaryClassification;

	/**
	 * Map from vocabulary words to word vectors.
	 *
	 * @see #getWordVector(String)
	 */
	public Map<String, SimpleMatrix> wordVectors;
	transient public Map<String, SimpleMatrix> unknownVectors = new HashMap<String, SimpleMatrix>();

	/**
	 * How many classes the RNN is built to test against
	 */
	public final int numClasses;

	/**
	 * Dimension of hidden layers, size of word vectors, etc
	 */
	public final int numHid;

	/**
	 * Cached here for easy calculation of the model size; TwoDimensionalMap
	 * does not return that in O(1) time
	 */
	public int numBinaryMatrices;

	/** How many elements a transformation matrix has */
	public int binaryTransformSize;
	/** How many elements the binary transformation tensors have */
	public int binaryTensorSize;
	/** How many elements a classification matrix has */
	public int binaryClassificationSize;

	/**
	 * Cached here for easy calculation of the model size; TwoDimensionalMap
	 * does not return that in O(1) time
	 */
	public int numUnaryMatrices;

	/** How many elements a classification matrix has */
	public int unaryClassificationSize;

	/**
	 * A random number generator - keeping it here lets us reproduce results
	 */
	final Random rand;

	//	static final String UNKNOWN_WORD = "*UNK*";

	/**
	 * Will store various options specific to this model
	 */
	public final RNNOptions op;

	/*
	 * // An example of how you could read in old models with readObject to fix
	 * the serialization // You would first read in the old model, then
	 * reserialize it private void readObject(ObjectInputStream in) throws
	 * IOException, ClassNotFoundException { ObjectInputStream.GetField fields =
	 * in.readFields(); binaryTransform =
	 * ErasureUtils.uncheckedCast(fields.get("binaryTransform", null));
	 * 
	 * // transform binaryTensors binaryTensors = TwoDimensionalMap.treeMap();
	 * TwoDimensionalMap<String, String, edu.stanford.nlp.rnn.SimpleTensor>
	 * oldTensors = ErasureUtils.uncheckedCast(fields.get("binaryTensors",
	 * null)); for (String first : oldTensors.firstKeySet()) { for (String
	 * second : oldTensors.get(first).keySet()) { binaryTensors.put(first,
	 * second, new SimpleTensor(oldTensors.get(first, second).slices)); } }
	 * 
	 * binaryClassification =
	 * ErasureUtils.uncheckedCast(fields.get("binaryClassification", null));
	 * unaryClassification =
	 * ErasureUtils.uncheckedCast(fields.get("unaryClassification", null));
	 * wordVectors = ErasureUtils.uncheckedCast(fields.get("wordVectors",
	 * null));
	 * 
	 * if (fields.defaulted("numClasses")) { throw new RuntimeException(); }
	 * numClasses = fields.get("numClasses", 0);
	 * 
	 * if (fields.defaulted("numHid")) { throw new RuntimeException(); } numHid
	 * = fields.get("numHid", 0);
	 * 
	 * if (fields.defaulted("numBinaryMatrices")) { throw new
	 * RuntimeException(); } numBinaryMatrices = fields.get("numBinaryMatrices",
	 * 0);
	 * 
	 * if (fields.defaulted("binaryTransformSize")) { throw new
	 * RuntimeException(); } binaryTransformSize =
	 * fields.get("binaryTransformSize", 0);
	 * 
	 * if (fields.defaulted("binaryTensorSize")) { throw new RuntimeException();
	 * } binaryTensorSize = fields.get("binaryTensorSize", 0);
	 * 
	 * if (fields.defaulted("binaryClassificationSize")) { throw new
	 * RuntimeException(); } binaryClassificationSize =
	 * fields.get("binaryClassificationSize", 0);
	 * 
	 * if (fields.defaulted("numUnaryMatrices")) { throw new RuntimeException();
	 * } numUnaryMatrices = fields.get("numUnaryMatrices", 0);
	 * 
	 * if (fields.defaulted("unaryClassificationSize")) { throw new
	 * RuntimeException(); } unaryClassificationSize =
	 * fields.get("unaryClassificationSize", 0);
	 * 
	 * rand = ErasureUtils.uncheckedCast(fields.get("rand", null)); op =
	 * ErasureUtils.uncheckedCast(fields.get("op", null)); op.classNames =
	 * op.DEFAULT_CLASS_NAMES; op.equivalenceClasses =
	 * op.APPROXIMATE_EQUIVALENCE_CLASSES; op.equivalenceClassNames =
	 * op.DEFAULT_EQUIVALENCE_CLASS_NAMES; }
	 */

	/**
	 * Given single matrices and sets of options, create the corresponding
	 * SentimentModel. Useful for creating a Java version of a model trained in
	 * some other manner, such as using the original Matlab code.
	 */
	static RNNModel modelFromMatrices(SimpleMatrix W, SimpleMatrix Wcat, SimpleTensor Wt, Map<String, SimpleMatrix> wordVectors, RNNOptions op) {
		if (!op.combineClassification || !op.simplifiedModel) {
			throw new IllegalArgumentException("Can only create a model using this method if combineClassification and simplifiedModel are turned on");
		}
		Map<String, SimpleMatrix> binaryTransform = Generics.newTreeMap();
		binaryTransform.put("", W);

		Map<String, SimpleTensor> binaryTensors = Generics.newTreeMap();
		binaryTensors.put("", Wt);

		Map<String, SimpleMatrix> binaryClassification = Generics.newTreeMap();

		Map<String, SimpleMatrix> unaryClassification = Generics.newTreeMap();
		unaryClassification.put("", Wcat);

		return new RNNModel(binaryTransform, binaryTensors, binaryClassification, unaryClassification, wordVectors, op);
	}

	private RNNModel(Map<String, SimpleMatrix> binaryTransform, Map<String, SimpleTensor> binaryTensors, Map<String, SimpleMatrix> binaryClassification, Map<String, SimpleMatrix> unaryClassification, Map<String, SimpleMatrix> wordVectors, RNNOptions op) {
		this.op = op;

		this.binaryTransform = binaryTransform;
		this.binaryTensors = binaryTensors;
		this.binaryClassification = binaryClassification;
		this.unaryClassification = unaryClassification;
		this.wordVectors = wordVectors;
		this.numClasses = op.numClasses;
		if (op.numHid <= 0) {
			int nh = 0;
			for (SimpleMatrix wv : wordVectors.values()) {
				nh = wv.getNumElements();
			}
			this.numHid = nh;
		} else {
			this.numHid = op.numHid;
		}
		this.numBinaryMatrices = binaryTransform.size();
		binaryTransformSize = numHid * (2 * numHid + 1);
		if (op.useTensors) {
			binaryTensorSize = numHid * numHid * numHid * 4;
		} else {
			binaryTensorSize = 0;
		}
		binaryClassificationSize = (op.combineClassification) ? 0 : numClasses * (numHid + 1);

		numUnaryMatrices = unaryClassification.size();
		unaryClassificationSize = numClasses * (numHid + 1);

		rand = new Random(op.randomSeed);
	}

	/**
	 * The traditional way of initializing an empty model suitable for training.
	 */
	public RNNModel(RNNOptions op, List<LabeledScoredTreeNode> trainingTrees) {
		this(op);

		if (op.randomWordVectors) {
			initRandomWordVectors(trainingTrees);
		} else {
			readWordVectors();
		}
	}

	public RNNModel(RNNOptions op) {
		this.op = op;
		rand = new Random(op.randomSeed);

		if (op.numHid > 0) {
			this.numHid = op.numHid;
		} else {
			int size = 0;
			for (SimpleMatrix vector : wordVectors.values()) {
				size = vector.getNumElements();
				break;
			}
			this.numHid = size;
		}

		this.numClasses = op.numClasses;

		binaryTransform = Generics.newTreeMap();
		binaryTensors = Generics.newTreeMap();

		binaryClassification = Generics.newTreeMap();

		numBinaryMatrices = binaryTransform.size();
		binaryTransformSize = numHid * (2 * numHid + 1);
		if (op.useTensors) {
			binaryTensorSize = numHid * numHid * numHid * 4;
		} else {
			binaryTensorSize = 0;
		}
		binaryClassificationSize = (op.combineClassification) ? 0 : numClasses * (numHid + 1);

		unaryClassification = Generics.newTreeMap();

		numUnaryMatrices = unaryClassification.size();
		unaryClassificationSize = numClasses * (numHid + 1);

		//log.info(this);
	}

	/**
	 * Dumps *all* the matrices in a mostly readable format.
	 */
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();

		if (binaryTransform.size() > 0) {
			if (binaryTransform.size() == 1) {
				output.append("Binary transform matrix\n");
			} else {
				output.append("Binary transform matrices\n");
			}
			//			for (Map.Entry<String, SimpleMatrix> matrix : binaryTransform) {
			//				if (!matrix.getFirstKey().equals("") || !matrix.getSecondKey().equals("")) {
			//					output.append(matrix.getFirstKey() + " " + matrix.getSecondKey() + ":\n");
			//				}
			//				output.append(NeuralUtils.toString(matrix.getValue(), "%.8f"));
			//			}
		}

		if (binaryTensors.size() > 0) {
			if (binaryTensors.size() == 1) {
				output.append("Binary transform tensor\n");
			} else {
				output.append("Binary transform tensors\n");
			}
			//			for (TwoDimensionalMap.Entry<String, String, SimpleTensor> matrix : binaryTensors) {
			//				if (!matrix.getFirstKey().equals("") || !matrix.getSecondKey().equals("")) {
			//					output.append(matrix.getFirstKey() + " " + matrix.getSecondKey() + ":\n");
			//				}
			//				output.append(matrix.getValue().toString("%.8f"));
			//			}
		}

		if (binaryClassification.size() > 0) {
			if (binaryClassification.size() == 1) {
				output.append("Binary classification matrix\n");
			} else {
				output.append("Binary classification matrices\n");
			}
			//			for (TwoDimensionalMap.Entry<String, String, SimpleMatrix> matrix : binaryClassification) {
			//				if (!matrix.getFirstKey().equals("") || !matrix.getSecondKey().equals("")) {
			//					output.append(matrix.getFirstKey() + " " + matrix.getSecondKey() + ":\n");
			//				}
			//				output.append(NeuralUtils.toString(matrix.getValue(), "%.8f"));
			//			}
		}

		if (unaryClassification.size() > 0) {
			if (unaryClassification.size() == 1) {
				output.append("Unary classification matrix\n");
			} else {
				output.append("Unary classification matrices\n");
			}
			for (Map.Entry<String, SimpleMatrix> matrix : unaryClassification.entrySet()) {
				if (!matrix.getKey().equals("")) {
					output.append(matrix.getKey() + ":\n");
				}
				output.append(NeuralUtils.toString(matrix.getValue(), "%.8f"));
			}
		}

		output.append("Word vectors\n");
		for (Map.Entry<String, SimpleMatrix> matrix : wordVectors.entrySet()) {
			output.append("'" + matrix.getKey() + "'");
			output.append("\n");
			output.append(NeuralUtils.toString(matrix.getValue(), "%.8f"));
			output.append("\n");
		}

		return output.toString();
	}

	SimpleTensor randomBinaryTensor() {
		double range = 1.0 / (4.0 * numHid);
		SimpleTensor tensor = SimpleTensor.random(numHid * 2, numHid * 2, numHid, -range, range, rand);
		return tensor.scale(op.trainOptions.scalingForInit);
	}

	SimpleMatrix randomTransformMatrix() {
		SimpleMatrix binary = new SimpleMatrix(numHid, numHid * 2 + 1);
		// bias column values are initialized zero
		binary.insertIntoThis(0, 0, randomTransformBlock());
		binary.insertIntoThis(0, numHid, randomTransformBlock());
		return binary.scale(op.trainOptions.scalingForInit);
	}

	SimpleMatrix randomTransformBlock() {
		double range = 1.0 / (Math.sqrt((double) numHid) * 2.0);
		return SimpleMatrix.random(numHid, numHid, -range, range, rand).plus(SimpleMatrix.identity(numHid));
	}

	/**
	 * Returns matrices of the right size for either binary or unary (terminal)
	 * classification
	 */
	SimpleMatrix randomClassificationMatrix() {
		SimpleMatrix score = new SimpleMatrix(numClasses, numHid + 1);
		double range = 1.0 / (Math.sqrt((double) numHid));
		score.insertIntoThis(0, 0, SimpleMatrix.random(numClasses, numHid, -range, range, rand));
		// bias column goes from 0 to 1 initially
		score.insertIntoThis(0, numHid, SimpleMatrix.random(numClasses, 1, 0.0, 1.0, rand));
		return score.scale(op.trainOptions.scalingForInit);
	}

	SimpleMatrix randomWordVector() {
		return randomWordVector(op.numHid, rand);
	}

	static SimpleMatrix randomWordVector(int size, Random rand) {
		return NeuralUtils.randomGaussian(size, 1, rand).scale(0.1);
	}

	void initRandomWordVectors(List<LabeledScoredTreeNode> trainingTrees) {
		initRandomWordVectors(trainingTrees, true);
	}

	void initRandomWordVectors(List<LabeledScoredTreeNode> trainingTrees, boolean bRemoveDuplicate) {
		if (op.numHid == 0) {
			throw new RuntimeException("Cannot create random word vectors for an unknown numHid");
		}
		Set<String> words = Generics.newHashSet();
		for (LabeledScoredTreeNode tree : trainingTrees) {
			//			if (!op.simplifiedModel) {
			//				tree.forwardPropagateTree(binaryProductions, unaryProductions);
			//			}

			List<Tree> leaves = tree.getLeaves();
			for (Tree leaf : leaves) {
				String word = leaf.label().value();
				if (op.uppercaseWordVectors) {
					word = word.toUpperCase();
				}
				words.add(word);
			}
		}

		//		if (additionalWords != null)
		//			words.addAll(additionalWords);
		initRandomWordVectors(words, bRemoveDuplicate);

		if (op.simplifiedModel) {
			if (binaryTransform.isEmpty()) {
				binaryTransform.put("", randomTransformMatrix());
				if (op.useTensors) {
					binaryTensors.put("", randomBinaryTensor());
				}

				// When making a flat model (no symantic untying) the
				// basicCategory function will return the same basic category for
				// all labels, so all entries will map to the same matrix
				unaryClassification.put("", randomClassificationMatrix());
			}

			// When making a flat model (no symantic untying) the
			// basicCategory function will return the same basic category for
			// all labels, so all entries will map to the same matrix
			if (!binaryClassification.isEmpty()) {
				throw new RuntimeException("!binaryClassification.isEmpty()");
			}

			numBinaryMatrices = binaryTransform.size();
			binaryTransformSize = numHid * (2 * numHid + 1);
			if (op.useTensors) {
				binaryTensorSize = numHid * numHid * numHid * 4;
			} else {
				binaryTensorSize = 0;
			}

			binaryClassificationSize = 0;

			numUnaryMatrices = unaryClassification.size();
			unaryClassificationSize = numClasses * (numHid + 1);
		}
	}

	void initializeTensors(String binaryProductions[], String unaryProductions[]) {
		for (String key : binaryProductions) {
			if (binaryTransform.containsKey(key)) {
				continue;
			}
			System.out.println("binary pos : " + key);

			binaryTransform.put(key, randomTransformMatrix());
			if (op.useTensors) {
				binaryTensors.put(key, randomBinaryTensor());
			}
			if (!op.combineClassification) {
				binaryClassification.put(key, randomClassificationMatrix());
			}
		}
		List<String> arrBinaryProductions = Arrays.asList(binaryProductions);
		binaryTransform.keySet().retainAll(arrBinaryProductions);
		binaryClassification.keySet().retainAll(arrBinaryProductions);
		binaryTensors.keySet().retainAll(arrBinaryProductions);

		for (String unary : unaryProductions) {
			unary = basicCategory(unary);
			if (unaryClassification.containsKey(unary)) {
				continue;
			}
			System.out.println("unary pos : " + unary);
			unaryClassification.put(unary, randomClassificationMatrix());
		}

		List<String> arrUnaryProductions = Arrays.asList(unaryProductions);
		unaryClassification.keySet().retainAll(arrUnaryProductions);

		numBinaryMatrices = binaryTransform.size();
		binaryTransformSize = numHid * (2 * numHid + 1);
		if (op.useTensors) {
			binaryTensorSize = numHid * numHid * numHid * 4;
		} else {
			binaryTensorSize = 0;
		}

		binaryClassificationSize = (op.combineClassification) ? 0 : numClasses * (numHid + 1);

		numUnaryMatrices = unaryClassification.size();
		unaryClassificationSize = numClasses * (numHid + 1);
	}

	Set<String> getWordSet(List<LabeledScoredTreeNode> trainingTrees) {
		if (op.numHid == 0) {
			throw new RuntimeException("Cannot create random word vectors for an unknown numHid");
		}
		Set<String> words = Generics.newHashSet();
		for (Tree tree : trainingTrees) {
			List<Tree> leaves = tree.getLeaves();
			for (Tree leaf : leaves) {
				String word = leaf.label().value();
				if (op.uppercaseWordVectors) {
					word = word.toUpperCase();
					//					word = word.toLowerCase();
				}
				words.add(word);
			}
		}
		return words;
	}

	void initRandomWordVectors(Set<String> words, boolean bRemoveDuplicate) {
		//		words.add(UNKNOWN_WORD);
		if (wordVectors == null) {
			this.wordVectors = Generics.newTreeMap();
		}

		for (String word : words) {
			if (wordVectors.containsKey(word))
				continue;
			SimpleMatrix vector = randomWordVector();
			wordVectors.put(word, vector);
		}

		if (bRemoveDuplicate)
			wordVectors.keySet().retainAll(words);
		System.out.println("wordVectors.size() = " + wordVectors.size());
	}

	void readWordVectors() {
		Embedding embedding = new Embedding(op.wordVectors, op.numHid);
		this.wordVectors = Generics.newTreeMap();
		//    Map<String, SimpleMatrix> rawWordVectors = NeuralUtils.readRawWordVectors(op.wordVectors, op.numHid);
		//    for (String word : rawWordVectors.keySet()) {
		for (String word : embedding.keySet()) {
			// TODO: factor out unknown word vector code from DVParser
			wordVectors.put(word, embedding.get(word));
		}

		String unkWord = op.unkWord;
		SimpleMatrix unknownWordVector = wordVectors.get(unkWord);
		//		wordVectors.put(UNKNOWN_WORD, unknownWordVector);
		if (unknownWordVector == null) {
			throw new RuntimeException("Unknown word vector not specified in the word vector file");
		}

	}

	public int totalParamSize() {
		int totalSize = 0;
		// binaryTensorSize was set to 0 if useTensors=false
		totalSize = numBinaryMatrices * (binaryTransformSize + binaryClassificationSize + binaryTensorSize);
		totalSize += numUnaryMatrices * unaryClassificationSize;
		totalSize += wordVectors.size() * numHid;
		return totalSize;
	}

	public double[] paramsToVector() {
		int totalSize = totalParamSize();
		return NeuralUtils.paramsToVector(totalSize, binaryTransform.values().iterator(), binaryClassification.values().iterator(), SimpleTensor.iteratorSimpleMatrix(binaryTensors.values().iterator()), unaryClassification.values().iterator(), wordVectors.values().iterator());
	}

	public void vectorToParams(double[] theta) {
		NeuralUtils.vectorToParams(theta, binaryTransform.values().iterator(), binaryClassification.values().iterator(), SimpleTensor.iteratorSimpleMatrix(binaryTensors.values().iterator()), unaryClassification.values().iterator(), wordVectors.values().iterator());
	}

	// TODO: combine this and getClassWForNode?
	public SimpleMatrix getWForNode(Tree node) {
		if (node.children().length == 2) {
			//			String leftLabel = node.children()[0].value();
			//			String leftBasic = basicCategory(leftLabel);
			//			String rightLabel = node.children()[1].value();
			//			String rightBasic = basicCategory(rightLabel);			
			return binaryTransform.get(node.label().value());
		} else if (node.children().length == 1) {
			throw new AssertionError("No unary transform matrices, only unary classification");
		} else {
			throw new AssertionError("Unexpected tree children size of " + node.children().length);
		}
	}

	public SimpleTensor getTensorForNode(Tree node) {
		if (!op.useTensors) {
			throw new AssertionError("Not using tensors");
		}
		if (node.children().length == 2) {
			//			String leftLabel = node.children()[0].value();
			//			String leftBasic = basicCategory(leftLabel);
			//			String rightLabel = node.children()[1].value();
			//			String rightBasic = basicCategory(rightLabel);			
			return binaryTensors.get(node.label().value());
		} else if (node.children().length == 1) {
			throw new AssertionError("No unary transform matrices, only unary classification");
		} else {
			throw new AssertionError("Unexpected tree children size of " + node.children().length);
		}
	}

	public SimpleMatrix getClassWForNode(Tree node) {
		if (op.combineClassification) {
			return unaryClassification.get("");
		} else if (node.children().length == 2) {
			//			String leftLabel = node.children()[0].value();
			//			String leftBasic = basicCategory(leftLabel);
			//			String rightLabel = node.children()[1].value();
			//			String rightBasic = basicCategory(rightLabel);			
			return binaryClassification.get(node.label().value());
		} else if (node.children().length == 1) {
			String unaryLabel = node.children()[0].value();
			String unaryBasic = basicCategory(unaryLabel);
			return unaryClassification.get(unaryBasic);
		} else {
			throw new AssertionError("Unexpected tree children size of " + node.children().length);
		}
	}

	/**
	 * Retrieve a learned word vector for the given word.
	 *
	 * If the word is Out Of Vocabulary, returns a vector associated with an
	 * {@code <unk>} term.
	 */
	public SimpleMatrix getWordVector(String word) {
		String _word = getVocabWord(word);
		if (_word != null) {
			return wordVectors.get(_word);
		}

		if (unknownVectors == null) {
			unknownVectors = new HashMap<String, SimpleMatrix>();
		}

		if (!word.matches(".+/.+")) {
			if (!unknownVectors.containsKey(word)) {
				unknownVectors.put(word, randomWordVector());
			}

		} else {
			String pos = word.split("/")[1];

			synchronized (unknownVectors) {
				if (!unknownVectors.containsKey(pos)) {
					SimpleMatrix sum = new SimpleMatrix(op.numHid, 1);
					int cnt = 0;
					for (Map.Entry<String, SimpleMatrix> pair : wordVectors.entrySet()) {
						String[] lexeme = pair.getKey().split("/");
						if (lexeme.length == 2 && pos.equals(lexeme[1])) {
							sum = sum.plus(pair.getValue());
							++cnt;
						}
					}
					if (cnt != 0)
						unknownVectors.put(pos, sum.divide(cnt));
					else
						unknownVectors.put(pos, randomWordVector());
				}
			}
			word = pos;
		}
		return unknownVectors.get(word);
	}

	/**
	 * Get the known vocabulary word associated with the given word.
	 *
	 * @return The form of the given word known by the model, or
	 *         {@link #UNKNOWN_WORD} if this word has not been observed
	 */
	public String getVocabWord(String word) {
		if (op.uppercaseWordVectors) {
			//			word = word.toLowerCase();
			word = word.toUpperCase();
		}
		if (wordVectors.containsKey(word)) {
			return word;
		}
		// TODO: go through unknown words here
		//		return UNKNOWN_WORD;
		return null;
	}

	public String basicCategory(String category) {
		if (op.simplifiedModel) {
			return "";
		}
		String basic = op.langpack.basicCategory(category);
		if (basic.length() > 0 && basic.charAt(0) == '@') {
			basic = basic.substring(1);
		}
		return basic;
	}

	public SimpleMatrix getUnaryClassification(String category) {
		category = basicCategory(category);
		return unaryClassification.get(category);
	}

	public SimpleMatrix getBinaryClassification(String left) {
		if (op.combineClassification) {
			return unaryClassification.get("");
		} else {
			left = basicCategory(left);
			//			right = basicCategory(right);
			return binaryClassification.get(left);
		}
	}

	public SimpleMatrix getBinaryTransform(String left) {
		left = basicCategory(left);
		//		right = basicCategory(right);
		return binaryTransform.get(left);
	}

	public SimpleTensor getBinaryTensor(String left) {
		left = basicCategory(left);
		//		right = basicCategory(right);
		return binaryTensors.get(left);
	}

	public void saveSerialized(String path) {
		try {
			IOUtils.writeObjectToFile(this, path);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	public static RNNModel loadSerialized(String path) {
		try {
			return IOUtils.readObjectFromURLOrClasspathOrFileSystem(path);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeIOException(e);
		}
	}

	public void printParamInformation(int index) {
		int curIndex = 0;
		//		for (TwoDimensionalMap.Entry<String, String, SimpleMatrix> entry : binaryTransform) {
		//			if (curIndex <= index && curIndex + entry.getValue().getNumElements() > index) {
		//				log.info("Index " + index + " is element " + (index - curIndex) + " of binaryTransform \"" + entry.getFirstKey() + "," + entry.getSecondKey() + "\"");
		//				return;
		//			} else {
		//				curIndex += entry.getValue().getNumElements();
		//			}
		//		}

		//		for (TwoDimensionalMap.Entry<String, String, SimpleMatrix> entry : binaryClassification) {
		//			if (curIndex <= index && curIndex + entry.getValue().getNumElements() > index) {
		//				log.info("Index " + index + " is element " + (index - curIndex) + " of binaryClassification \"" + entry.getFirstKey() + "," + entry.getSecondKey() + "\"");
		//				return;
		//			} else {
		//				curIndex += entry.getValue().getNumElements();
		//			}
		//		}

		//		for (TwoDimensionalMap.Entry<String, String, SimpleTensor> entry : binaryTensors) {
		//			if (curIndex <= index && curIndex + entry.getValue().getNumElements() > index) {
		//				log.info("Index " + index + " is element " + (index - curIndex) + " of binaryTensor \"" + entry.getFirstKey() + "," + entry.getSecondKey() + "\"");
		//				return;
		//			} else {
		//				curIndex += entry.getValue().getNumElements();
		//			}
		//		}

		for (Map.Entry<String, SimpleMatrix> entry : unaryClassification.entrySet()) {
			if (curIndex <= index && curIndex + entry.getValue().getNumElements() > index) {
				log.info("Index " + index + " is element " + (index - curIndex) + " of unaryClassification \"" + entry.getKey() + "\"");
				return;
			} else {
				curIndex += entry.getValue().getNumElements();
			}
		}

		for (Map.Entry<String, SimpleMatrix> entry : wordVectors.entrySet()) {
			if (curIndex <= index && curIndex + entry.getValue().getNumElements() > index) {
				log.info("Index " + index + " is element " + (index - curIndex) + " of wordVector \"" + entry.getKey() + "\"");
				return;
			} else {
				curIndex += entry.getValue().getNumElements();
			}
		}

		log.info("Index " + index + " is beyond the length of the parameters; total parameter space was " + totalParamSize());
	}

	private static final long serialVersionUID = 1;
}
