package com.robot.semantic.RNN;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.robot.Sentence;
import com.robot.syntax.Constituent;
import com.robot.syntax.Constituent.Action;
import com.robot.syntax.ConstituentGradient;
import com.robot.syntax.ConstituentGradientSingleton;
import com.robot.syntax.ConstituentTree;
import com.robot.syntax.SyntacticTree;
import com.robot.syntax.SyntacticTree.BinarizedTree.Combination;
import com.robot.util.InstanceReader;
import com.util.Utility;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.trees.Tree;

public class ConstituentAbsorber {
	static public ConstituentAbsorber instance = new ConstituentAbsorber();

	public ConstituentAbsorber() {
		String modelPath = Utility.workingDirectory + "models/ConstituentAbsorber.gz";
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
			//			return classify(t);
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

	void print(LabeledScoredTreeNode tree) throws Exception {
		System.out.println("difference = " + tree.score());
		if (tree.label().containsKey(RNNCoreAnnotations.GoldScore.class)) {
			double score = tree.label().get(RNNCoreAnnotations.GoldScore.class);
			System.out.println("gold score = " + score);
			System.out.println("pred score = " + RNNCoreAnnotations.getPredictions(tree).get(1));			
		}
	}

	public void testRNNModel() throws Exception {
		//		Printer printer = new Printer(Utility.workingDirectory + "corpus/Constituent/debug.txt");

		int err = 0;
		int sum = 0;
		double deviation = 0.1;
		for (ConstituentInstance constituent : trainingTrees) {
			if (!constituent.pred().infix().equals(constituent.gold().infix())) {
				err++;
				constituent.print();
			}
			sum++;
		}

		DecimalFormat df = new DecimalFormat("00.00");
		System.out.println("testing trained set");

		System.out.println("correct percent = " + df.format((sum - err) * 1.0 / sum * 100) + "%");
		System.out.println("total comparisons = " + sum);

		//		printer.close();
	}

	public void executeOneTrainingBatch(RNNModel model, List<Tree> trainingBatch, double[] sumGradSquare) {
		RNNCostAndGradient gcFunc = new RNNCostAndGradient(model, trainingBatch);
		double[] theta = model.paramsToVector();
		// AdaGrad
		double eps = 1e-3;
		// TODO: do we want to iterate multiple times per batch?
		double[] gradf = gcFunc.derivativeAtForPolarityClassification(theta);
		double currCost = gcFunc.valueAt(theta);
		for (int feature = 0; feature < gradf.length; feature++) {
			if (gradf[feature] != gradf[feature]) {
				continue;
			}
			sumGradSquare[feature] = sumGradSquare[feature] + gradf[feature] * gradf[feature];

			theta[feature] = theta[feature] - (model.op.trainOptions.learningRate * gradf[feature] / (Math.sqrt(sumGradSquare[feature]) + eps));

			if (theta[feature] != theta[feature]) {
				theta[feature] = new Random().nextGaussian() * 0.1;
			}
		}

		model.vectorToParams(theta);
	}

	public void trainingCorpus() throws Exception {
		for (int i = 0; i < 30; ++i) {
			ConstituentGradient gradient = new ConstituentGradient();
			for (ConstituentInstance inst : this.trainingTrees) {
				gradient.add(inst.gradient());
			}

			if (gradient.isEmpty()) {
				break;
			}
			gradient.applyGradientDescent();
		}
	}

	public static void main(String[] args) throws Exception {
		instance.establishTrainingCorpus();
		instance.trainingCorpus();
		instance.testRNNModel();
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

	static class ConstituentInstance {
		ConstituentInstance(String comparison, Action action) throws Exception {
			String[] arr = comparison.split("/");
			left = new Sentence(arr[0].trim()).tree();
			right = new Sentence(arr[1].trim()).tree();
			goldAction = action;
		}

		ConstituentInstance(SyntacticTree left, SyntacticTree right, Action action) throws Exception {
			this.left = left;
			this.right = right;
			goldAction = action;
		}

		public ConstituentGradient gradient() throws Exception {
			ConstituentGradientSingleton gradient = new ConstituentGradientSingleton();
			Constituent gold = left.toConstituentTree().equality(right.toConstituentTree(), gradient, (ConstituentTree) gold());
			if (gradient.modified) {
				Utility.print(left);
				Utility.print(left.toConstituentTree());
				Utility.print(right);
				Utility.print(right.toConstituentTree());
				Utility.print(gold);
			}
			return gradient;
		}

		Constituent pred() throws Exception {
			return left.toConstituentTree().equality(right.toConstituentTree());
		}

		Constituent gold() throws Exception {
			return goldAction.construct(left.toConstituentTree(), right.toConstituentTree());
		}

		void print() throws Exception {
			System.out.println("left = " + left);
			System.out.println("right = " + right);
			Constituent leftConstituent = left.toConstituentTree();
			Constituent rightConstituent = right.toConstituentTree();
			System.out.println(leftConstituent);
			System.out.println(rightConstituent);
			Constituent pred = pred();
			System.out.println("predAction = " + leftConstituent.goldAction(rightConstituent, (ConstituentTree) pred));
			System.out.println("goldAction = " + goldAction);

			Constituent gold = gold();
			System.out.println("pred() = \r\n" + pred);
			System.out.println("pred.infix = " + pred.infix());
			System.out.println("gold() = \r\n" + gold);
			System.out.println("gold.infix = " + gold.infix());
			System.out.println("\r\n\r\n");
		}

		SyntacticTree left, right;
		Action goldAction;
	}

	Random random = new Random();
	ArrayList<ConstituentInstance> trainingTrees;

	public void establishTrainingCorpus() throws Exception {
		trainingTrees = new ArrayList<ConstituentInstance>();

//		int sum = 32;
//		int cnt = 0;
		for (com.robot.util.Instance inst : new InstanceReader(Utility.workingDirectory + "/corpus/Constituent/")) {

			String comparison = (String) inst.getData();

			String[] arr = comparison.split("/");
			SyntacticTree left = new Sentence(arr[0].trim()).tree();
			SyntacticTree right = new Sentence(arr[1].trim()).tree();

			Action action = Action.valueOf((String) inst.getTarget());
			trainingTrees.add(new ConstituentInstance(left, right, action));
			trainingTrees.add(new ConstituentInstance(right, left, action.inverse()));
//			if (++cnt == sum)
//				break;
		}
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

}
