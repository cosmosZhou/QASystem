package com.robot.semantic.RNN;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.robot.Sentence;
import com.robot.semantic.Synonym;
import com.robot.syntax.Constituent;
import com.robot.syntax.ConstituentGradient;
import com.robot.syntax.ConstituentGradientSingleton;
import com.robot.syntax.ConstituentTree;
import com.robot.syntax.SyntacticParser;
import com.robot.syntax.SyntacticTree;
import com.robot.syntax.SyntacticTree.BinarizedTree.Combination;
import com.robot.util.InstanceReader;
import com.util.Utility;
import com.util.Utility.Printer;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
//CoreNLPProtos,  PTBLexer, Morpha
public class RNNParaphrase extends RNNTraining {
	static public RNNParaphrase instance = new RNNParaphrase();

	public RNNParaphrase() {
		modelPath = Utility.workingDirectory + "models/Paraphrase.gz";
		try {
			model = RNNModel.loadSerialized(modelPath);
			model.op.trainOptions.epochs = 3;
		} catch (Exception e) {
			RNNOptions op = new RNNOptions();
			//			op.numHid = 50;
			//			op.trainOptions.batchSize = 37;
			op.numClasses = 2;
			op.trainOptions.epochs = 10;
			op.trainOptions.debugOutputEpochs = -1;
			op.simplifiedModel = false;
			op.combineClassification = false;

			model = new RNNModel(op);
		}
	}

	static Combination combination = new Combination() {
		@Override
		public String combine(String seg, String pos) {
			return seg;
		}
	};

	public Combination combination() {
		return combination;
	}

	public double similarity(SyntacticTree x, SyntacticTree y) {

		try {
			LabeledScoredTreeNode t = equality(x, y).toLabeledScoredTreeNode();
			return classify(t);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//			e.printStackTrace();
		}

		return 0;
	}

	public double similarity(String x, String y) throws Exception {
		return similarity(new Sentence(x), new Sentence(y));
	}

	public double similarity(Sentence x, Sentence y) throws Exception {
		double similarity = similarity(x.tree(), y.tree());

		//		similarity = Math.pow(similarity, Math.log(0.8) / Math.log(0.6));
		return similarity;
	}

	final static double threshold = 0.6;

	public double error(LabeledScoredTreeNode tree) {
		if (tree.score() != tree.score()) {
			CoreLabel coreLabel = (CoreLabel) tree.label();

			double probability = classify(tree);
			double score = coreLabel.get(RNNCoreAnnotations.GoldScore.class);
			tree.setScore(Math.abs(probability - score));
		}
		return tree.score();
	}

	public void testRNNModel() throws Exception {
		Printer printer = new Printer(Utility.workingDirectory + "corpus/debug.syn.txt");

		Comparator<LabeledScoredTreeNode> pred = new Comparator<LabeledScoredTreeNode>() {
			@Override
			public int compare(LabeledScoredTreeNode o1, LabeledScoredTreeNode o2) {
				return Double.compare(error(o1), error(o2));
			}
		};

		Utility.PriorityQueue<LabeledScoredTreeNode> queue = new Utility.PriorityQueue<LabeledScoredTreeNode>(pred);

		if (trainingTrees != null) {
			testRNNModel(trainingTrees, queue);
		}
		testRNNModel(generateTrainingSet(), queue);

		printer.close();
		System.out.println("rnn trained successfully");
	}

	public void testRNNModelForDevelopment() throws Exception {

		Comparator<LabeledScoredTreeNode> pred = new Comparator<LabeledScoredTreeNode>() {
			@Override
			public int compare(LabeledScoredTreeNode o1, LabeledScoredTreeNode o2) {
				return Double.compare(error(o1), error(o2));
			}
		};

		Utility.PriorityQueue<LabeledScoredTreeNode> queue = new Utility.PriorityQueue<LabeledScoredTreeNode>(pred);

		if (trainingTrees != null) {
			testRNNModel(trainingTrees, queue);
		}
		testRNNModel(generateTrainingSetForDevelopment(), queue);

		System.out.println("rnn trained successfully");
	}

	void print(LabeledScoredTreeNode tree) throws Exception {
		System.out.println("difference = " + tree.score());
		if (tree.label().containsKey(RNNCoreAnnotations.GoldScore.class)) {
			double score = tree.label().get(RNNCoreAnnotations.GoldScore.class);
			SyntacticTree xTree = tree.label().get(com.robot.semantic.RNN.RNNParaphrase.EquationLeft.class);
			SyntacticTree yTree = tree.label().get(com.robot.semantic.RNN.RNNParaphrase.EquationRight.class);

			System.out.println(xTree.toString());
			System.out.println(yTree.toString());
			System.out.println("gold score = " + score);
			System.out.println("pred score = " + RNNCoreAnnotations.getPredictions(tree).get(1));
			System.out.println(RNNParaphrase.instance.equality(xTree, yTree).toString());
		}

	}

	void testRNNModel(List<? extends Tree> trainingTrees, Utility.PriorityQueue<LabeledScoredTreeNode> queue) throws Exception {

		int err = 0;
		int sum = 0;
		double deviation = 0.1;
		for (Tree tree : trainingTrees) {
			double confidence = this.error((LabeledScoredTreeNode) tree);
			if (confidence != confidence) {
				print((LabeledScoredTreeNode) tree);
				throw new RuntimeException("probability != probability");
			}

			CoreLabel coreLabel = (CoreLabel) tree.label();

			double probability = classify(tree);

			double score = coreLabel.get(RNNCoreAnnotations.GoldScore.class);

			if (score - deviation > probability || probability > score + deviation) {
				queue.add((LabeledScoredTreeNode) tree);
				err++;
			}
			sum++;
		}

		DecimalFormat df = new DecimalFormat("00.00");
		System.out.println("testing trained set");

		System.out.println("correct percent = " + df.format((sum - err) * 1.0 / sum * 100) + "%");
		System.out.println("total comparisons = " + sum);

		for (int i = 0; i < 3; ++i) {
			if (!queue.isEmpty()) {
				print(queue.poll());
			}
		}

	}

	void testRatio() {
		int positive = 0;
		int negative = 0;
		int bound = 80;
		for (int i = 0; i <= bound; ++i) {
			negative += getMaximumTrainingData(i / 100.0);
			System.out.println("count " + i / 100.0 + " = " + getMaximumTrainingData(i / 100.0));
		}
		for (int i = bound + 1; i <= 100; ++i) {
			positive += getMaximumTrainingData(i / 100.0);
			System.out.println("count " + i / 100.0 + " = " + getMaximumTrainingData(i / 100.0));
		}
		System.out.println("negative = " + negative);
		System.out.println("positive = " + positive);
		System.out.println("sum = " + (negative + positive));
		System.out.println("negative ratio = " + negative * 1.0 / (negative + positive));
		System.out.println("positive ratio = " + positive * 1.0 / (negative + positive));
	}

	public void executeOneTrainingBatch(RNNModel model, List<Tree> trainingBatch, double[] sumGradSquare) {
		RNNCostAndGradient gcFunc = new RNNCostAndGradient(model, trainingBatch);
		double[] theta = model.paramsToVector();
		// AdaGrad
		double eps = 1e-3;
		// TODO: do we want to iterate multiple times per batch?
		double[] gradf = gcFunc.derivativeAtForPolarityClassification(theta);
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

	public static void main(String[] args) throws Exception {
		//		instance.establishTrainingCorpus();
		//		instance.trainingCorpus();
		//		instance.testRNNModel();
	}

	String synonymPathStruct = Utility.workingDirectory + "/corpus/paraphrase/Struct.txt";

	String[][] inequalityStruct;
	String[][] equalityStruct;
	public int numOfStruct = 3000;
	public int multitude = 2;

	public static class EquationLeft implements CoreAnnotation<SyntacticTree> {
		@Override
		public Class<SyntacticTree> getType() {
			return SyntacticTree.class;
		}
	}

	public static class EquationRight implements CoreAnnotation<SyntacticTree> {
		@Override
		public Class<SyntacticTree> getType() {
			return SyntacticTree.class;
		}
	}

	static int getMaximumTrainingData(double score) {
		double ratio = Utility.sinusoidalHyperbolicTangent(score, 1 / Math.PI);
		return (int) (10000 * ratio * ratio);
	}

	void insertTrainingExample(String[] str, ArrayList<LabeledScoredTreeNode> infixExpression, double score) throws Exception {
		SyntacticTree xTree = SyntacticParser.instance.parse(str[0]);
		SyntacticTree yTree = SyntacticParser.instance.parse(str[1]);

		if (random.nextBoolean()) {
			SyntacticTree tmp = xTree;
			xTree = yTree;
			yTree = tmp;
		}

		LabeledScoredTreeNode labeledScoredTreeNode = equality(xTree, yTree).toLabeledScoredTreeNode(score);
		labeledScoredTreeNode.label().set(EquationLeft.class, xTree);
		labeledScoredTreeNode.label().set(EquationRight.class, yTree);

		infixExpression.add(labeledScoredTreeNode);
	}

	ArrayList<LabeledScoredTreeNode> generateTrainingSet() throws Exception {
		ArrayList<LabeledScoredTreeNode> infixExpression = new ArrayList<LabeledScoredTreeNode>();

		Map<Double, ArrayList<String[]>> map = new HashMap<Double, ArrayList<String[]>>();

		for (com.robot.util.Instance inst : new InstanceReader(Utility.workingDirectory + "/corpus/paraphrase/")) {

			String sent = (String) inst.getData();
			double score = Double.parseDouble((String) inst.getTarget());
			String str[];
			if (sent.matches(".+==.+")) {
				str = sent.split("\\s*==\\s*");
			} else if (sent.matches(".+/.+=.+")) {
				str = Utility.regexSingleton(sent, "(.+)/(.+=).+");
				Utility.trim(str);
			} else if (sent.matches(".+/.+")) {
				str = sent.split("\\s*/\\s*");
			} else {
				continue;
			}
			if (!map.containsKey(score)) {
				map.put(score, new ArrayList<String[]>());
			}

			if (str.length != 2) {
				throw new RuntimeException(sent);
			}

			map.get(score).add(str);
		}

		for (Map.Entry<Double, ArrayList<String[]>> p : map.entrySet()) {
			ArrayList<String[]> set = p.getValue();
			double score = p.getKey();
			int max = getMaximumTrainingData(score);
			Utility.shuffle(set);
			max = Math.min(max, set.size());
			for (int j = 0; j < max; ++j)
				insertTrainingExample(set.get(j), infixExpression, score);
		}

		return infixExpression;
	}

	ArrayList<LabeledScoredTreeNode> generateSynonymSet() throws Exception {
		ArrayList<LabeledScoredTreeNode> infixExpression = new ArrayList<LabeledScoredTreeNode>();

		String[] arr = Utility.toArrayString(Synonym.instance.keyWord_Identifier_HashMap.keySet());

		Utility.shuffle(arr);

		int tally = 10000;
		for (int i = 0; i < tally * 2; i += 2) {
			String key1 = arr[i];
			String key2 = arr[i + 1];
			SyntacticTree xTree = SyntacticParser.instance.parse(key1);
			SyntacticTree yTree = SyntacticParser.instance.parse(key2);

			LabeledScoredTreeNode labeledScoredTreeNode = equality(xTree, yTree).toLabeledScoredTreeNode(Synonym.similarity(key1, key2));
			labeledScoredTreeNode.label().set(EquationLeft.class, xTree);
			labeledScoredTreeNode.label().set(EquationRight.class, yTree);

			infixExpression.add(labeledScoredTreeNode);
		}

		for (int i = tally * 2; i < tally * 3; ++i) {
			String key1 = arr[i];
			SyntacticTree xTree = SyntacticParser.instance.parse(key1);

			LabeledScoredTreeNode labeledScoredTreeNode = equality(xTree, xTree).toLabeledScoredTreeNode(1.0);
			labeledScoredTreeNode.label().set(EquationLeft.class, xTree);
			labeledScoredTreeNode.label().set(EquationRight.class, xTree);

			infixExpression.add(labeledScoredTreeNode);
		}

		return infixExpression;
	}

	ArrayList<LabeledScoredTreeNode> generateTrainingSetForDevelopment() throws Exception {
		ArrayList<LabeledScoredTreeNode> infixExpression = new ArrayList<LabeledScoredTreeNode>();

		for (String exp : new Utility.Text(Utility.workingDirectory + "/corpus/paraphrase/dev.txt")) {
			String[] res = Utility.regexSingleton(exp, "(.+)/(.+)=(.+)");

			double score = Double.parseDouble(res[3]);

			String str[] = { res[1], res[2] };
			if (str.length != 2) {
				throw new RuntimeException(exp);
			}

			insertTrainingExample(str, infixExpression, score);
		}

		return infixExpression;
	}

	Random random = new Random();

	public void establishTrainingCorpus() throws Exception {
		ArrayList<LabeledScoredTreeNode> infixExpressionPositive = new ArrayList<LabeledScoredTreeNode>();

		trainingTrees = new ArrayList<Tree>();

		infixExpressionPositive.addAll(generateTrainingSet());
		infixExpressionPositive.addAll(generateSynonymSet());

		trainingTrees.addAll(infixExpressionPositive);
		model.initRandomWordVectors(infixExpressionPositive, false);

		String binaryProductions[] = "CD DE EQU JJ M MD NN NR NT O P QUE VA VC VI VT VBG LC NEG CS AS PN AD DT PU IJ CC adj adv as nt cs de ij o obj p pu suj sup va cc".split("\\s+");
		String unaryProductions[] = "AD AS CD DE DT IJ JJ M MD NEG NN NR NT O P PN PU QUE VA VC VI VT VBG LC CS CC".split("\\s+");

		model.initializeTensors(binaryProductions, unaryProductions);
	}

	public void establishTrainingCorpusForDevelopment() throws Exception {
		ArrayList<LabeledScoredTreeNode> infixExpressionPositive = new ArrayList<LabeledScoredTreeNode>();

		trainingTrees = new ArrayList<Tree>();

		infixExpressionPositive.addAll(generateTrainingSetForDevelopment());

		trainingTrees.addAll(infixExpressionPositive);
		model.initRandomWordVectors(infixExpressionPositive, false);

		String binaryProductions[] = "CD DE EQU JJ M MD NN NR NT O P QUE VA VC VI VT VBG LC NEG CS AS PN AD DT PU IJ CC adj adv as nt cs de ij o obj p pu suj sup va cc".split("\\s+");
		String unaryProductions[] = "AD AS CD DE DT IJ JJ M MD NEG NN NR NT O P PN PU QUE VA VC VI VT VBG LC CS CC".split("\\s+");

		model.initializeTensors(binaryProductions, unaryProductions);
	}

	enum Direction {
		parent_right, kinder_right, left_kinder, left_parent, normal, reverse;
	}

	public Constituent equality(SyntacticTree left, SyntacticTree right) throws Exception {
		Constituent x = left.toConstituentTree();
		Constituent y = right.toConstituentTree();
		return x.equality(y);
	}

	public ConstituentGradient equality(SyntacticTree left, SyntacticTree right, ConstituentTree gold) throws Exception {
		Constituent x = left.toConstituentTree();
		Constituent y = right.toConstituentTree();
		ConstituentGradientSingleton gradient = new ConstituentGradientSingleton();
		Constituent _gold = x.equality(y, gradient, gold);

		return gradient;
	}

	public ConstituentGradient equality(String left, String right, String gold) throws Exception {
		Constituent constituent = Constituent.compile(gold);

		return equality(new Sentence(left).tree(), new Sentence(right).tree(), (ConstituentTree) constituent);
	}

	public Constituent equality(String left, String right) throws Exception {
		return equality(new Sentence(left).tree(), new Sentence(right).tree());
	}

	public Constituent equalityDebug(SyntacticTree left, SyntacticTree right) throws Exception {
		Constituent x = left.toConstituentTree();
		System.out.println("left = \r\n" + x);
		Constituent y = right.toConstituentTree();
		System.out.println("right = \r\n" + y);
		return x.equality(y);
	}

	class Struct implements Comparable<Struct> {
		Struct(String identifier, double degree) {
			this.identifier = identifier;
			this.degree = degree;
		}

		String identifier;
		double degree;

		@Override
		public int compareTo(Struct o) {
			// TODO Auto-generated method stub
			return Double.compare(degree, o.degree);
		}
	}

	static Logger log = Logger.getLogger(RNNParaphrase.class);
}
