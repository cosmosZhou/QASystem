package com.robot.syntax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.hamcrest.core.IsInstanceOf;

import com.google.common.collect.Sets;
import com.robot.Sentence;
import com.robot.semantic.RNN.LabeledScoredTreeNode;
import com.robot.semantic.RNN.RNNParaphrase;
import com.robot.syntax.Compiler.HNode;
import com.robot.syntax.Compiler.HNodeCaret;
import com.robot.syntax.Compiler.HNodeParenthesis;
import com.robot.syntax.Constituent.Action;
import com.robot.syntax.Constituent.Coefficient;
import com.util.Utility;
import com.util.Utility.LNodeShadow;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations.GoldScore;

public abstract class Constituent {
	static Logger log = Logger.getLogger(Constituent.class);

	static class Cache {
		Map<Constituent, Map<Constituent, Double>> muturalInformation = new HashMap<Constituent, Map<Constituent, Double>>();
		Map<Constituent, Double> entropy = new HashMap<Constituent, Double>();
	}

	public enum Coefficient {
		AD, AS, CC, CD, CS, DE, DT, IJ, JJ, LC, M, MD, NEG, NN, NR, NT, O, P, PN, PU, QUE, VA, VBG, VC, VI, VT,

		EQU,

		adj, adv, as, cc, cs, de, ij, nt, o, obj, p, pu, suj, sup, va,

		biasNorm, biasNormHetero, biasSwap, biasSwapHetero, biasRoot, biasKinder,

		wPosHetero, wSeg, wScale, wDiminish
	}

	public static HashMap<Coefficient, Double> entropyMap = new HashMap<Coefficient, Double>();

	public static void updateEntropyMap(Coefficient x, double value) {
		if (value != value) {
			throw new RuntimeException("value != value");
		}
		entropyMap.put(x, entropyMap.get(x) + value);
	}

	static {
		try {
			for (String statement : new Utility.Text(Utility.workingDirectory + "models/entropyMap.txt")) {
				String[] arr = statement.split("\\s*=\\s*");
				entropyMap.put(Coefficient.valueOf(arr[0]), Double.valueOf(arr[1]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Constituent compile(String infix) throws Exception {
		return HNode.compile(infix).toConstituent();
	}

	public static boolean isGrammaticalConstituent(String[] seg) {
		for (String s : seg) {
			if (SyntacticParser.instance.tagSet().contains(s))
				return true;
		}
		return false;
	}

	@Override
	public abstract Constituent clone() throws CloneNotSupportedException;

	Map<Constituent, Double> getMap(Cache cache) {
		Map<Constituent, Double> mapInformation = cache.muturalInformation.get(this);
		if (mapInformation == null) {
			mapInformation = new HashMap<Constituent, Double>();
			cache.muturalInformation.put(this, mapInformation);
		}
		return mapInformation;
	}

	abstract double mutualInformation(Constituent y, Cache cache);

	abstract double mutualInformation(ConstituentTree y, Cache cache);

	abstract double mutualInformation(ConstituentLeaf y, Cache cache);

	abstract double _mutualInformation(ConstituentTree y, Cache cache);

	abstract double _mutualInformation(ConstituentLeaf y, Cache cache);

	abstract double mutualInformation(Constituent y, ConstituentGradientSingleton gradient);

	abstract double mutualInformation(ConstituentTree y, ConstituentGradientSingleton gradient);

	abstract double mutualInformation(ConstituentLeaf y, ConstituentGradientSingleton gradient);

	abstract double _mutualInformation(ConstituentTree y, ConstituentGradientSingleton gradient);

	abstract double _mutualInformation(ConstituentLeaf y, ConstituentGradientSingleton gradient);

	public abstract String unadornedExpression();

	public HashSet<ConstituentTree> constituent() {
		return new HashSet<ConstituentTree>();
	}

	abstract int maxWordLength();

	public abstract LNodeShadow buildShadowTree();

	public abstract LabeledScoredTreeNode toLabeledScoredTreeNode() throws Exception;

	public LabeledScoredTreeNode toLabeledScoredTreeNode(double similarity) throws Exception {
		LabeledScoredTreeNode labeledScoredTreeNode = toLabeledScoredTreeNode();
		labeledScoredTreeNode.label().set(GoldScore.class, similarity);
		return labeledScoredTreeNode;
	}

	public Constituent equality(Constituent right) {
		return equality(right, new Cache());
	}

	public abstract Constituent equality(Constituent right, Cache cache);

	public abstract Constituent equality(ConstituentLeaf right, Cache cache);

	public abstract Constituent equality(ConstituentTree right, Cache cache);

	public abstract Constituent _equality(ConstituentLeaf right, Cache cache);

	public abstract Constituent _equality(ConstituentTree right, Cache cache);

	public abstract Constituent equality(Constituent right, ConstituentGradientSingleton gradient, ConstituentTree gold) throws Exception;

	public abstract Constituent equality(ConstituentLeaf right, ConstituentGradientSingleton gradient, ConstituentTree gold);

	public abstract Constituent equality(ConstituentTree right, ConstituentGradientSingleton gradient, ConstituentTree gold) throws Exception;

	public abstract Constituent _equality(ConstituentLeaf right, ConstituentGradientSingleton gradient, ConstituentTree gold);

	public abstract Constituent _equality(ConstituentTree right, ConstituentGradientSingleton gradient, ConstituentTree gold) throws Exception;

	public abstract String infix();

	public String[] lexemeSet() {
		ArrayList<String> arr = new ArrayList<String>();
		lexemeSet(arr);
		String[] res = Utility.toArrayString(arr);
		Arrays.sort(res);
		return res;
	}

	public enum Action {
		norm, swap, left, _left, right, _right;

		public Action inverse() {
			switch (this) {
			case norm:
				return norm;
			case swap:
				return swap;
			case left:
				return _left;
			case _left:
				return left;
			case right:
				return _right;
			case _right:
				return right;
			}

			return null;
		}

		public Constituent construct(Constituent xConstituent, Constituent yConstituent) throws Exception {
			Cache cache = new Cache();
			ConstituentTree xTree = null;
			ConstituentTree yTree = null;
			if (xConstituent instanceof ConstituentTree) {
				xTree = (ConstituentTree) xConstituent;
			}
			if (yConstituent instanceof ConstituentTree) {
				yTree = (ConstituentTree) yConstituent;
			}

			switch (this) {
			case norm:
				xTree.left = xTree.left.equality(yTree.left, cache);
				xTree.right = xTree.right.equality(yTree.right, cache);
				return xTree;

			case swap:
				xTree.left = xTree.left.equality(yTree.right, cache);
				xTree.right = xTree.right.equality(yTree.left, cache);
				return xTree;

			case left:
				xTree.left = xTree.left.equality(yConstituent, cache);
				return xTree;

			case right:
				xTree.right = xTree.right.equality(yConstituent, cache);
				return xTree;

			case _left:
				yTree.left = xConstituent.equality(yTree.left, cache);
				return yTree;

			case _right:
				yTree.right = xConstituent.equality(yTree.right, cache);
				return yTree;
			default:
				throw new RuntimeException(name());
			}

		}

		Constituent absorb(ConstituentTree x, ConstituentTree y, ConstituentGradientSingleton gradient, ConstituentTree gold) throws Exception {
			switch (this) {
			case norm:
				x.left = x.left.equality(y.left, gradient, (ConstituentTree) gold.left);
				x.right = x.right.equality(y.right, gradient, (ConstituentTree) gold.right);
				return x;

			case swap:
				x.left = x.left.equality(y.right, gradient, (ConstituentTree) gold.left);
				x.right = x.right.equality(y.left, gradient, (ConstituentTree) gold.right);
				return x;

			case left:
				x.left = x.left.equality(y, gradient, (ConstituentTree) gold.left);
				return x;

			case right:
				x.right = x.right.equality(y, gradient, (ConstituentTree) gold.right);
				return x;

			case _left:
				y.left = x.equality(y.left, gradient, (ConstituentTree) gold.left);
				return y;

			case _right:
				y.right = x.equality(y.right, gradient, (ConstituentTree) gold.right);
				return y;
			default:
				throw new RuntimeException(name());
			}
		}

		Constituent absorb(ConstituentTree x, ConstituentLeaf y, ConstituentGradientSingleton gradient, ConstituentTree gold) {
			switch (this) {
			case left:
				x.left = x.left.equality(y, gradient, (ConstituentTree) gold.left);
				return x;

			case right:
				x.right = x.right.equality(y, gradient, (ConstituentTree) gold.right);
				return x;
			default:
				throw new RuntimeException(name());
			}
		}

		Constituent absorb(ConstituentLeaf x, ConstituentTree y, ConstituentGradientSingleton gradient, ConstituentTree gold) {
			switch (this) {
			case _left:
				y.left = x.equality(y.left, gradient, (ConstituentTree) gold.left);
				return y;

			case _right:
				y.right = x.equality(y.right, gradient, (ConstituentTree) gold.right);
				return y;
			default:
				throw new RuntimeException(name());
			}

		}
	}

	public abstract double entropy(Cache cache);

	public abstract void lexemeSet(ArrayList<String> arr);

	double _leftMutualInformation(ConstituentTree y, Cache cache) {
		double rightEntropy = y.right.entropy(cache);
		return mutualInformation(y.left, cache) * decadent(y.left.entropy(cache), rightEntropy) - rightEntropy;
	}

	double _rightMutualInformation(ConstituentTree y, Cache cache) {
		double leftEntropy = y.left.entropy(cache);
		return mutualInformation(y.right, cache) * decadent(y.right.entropy(cache), leftEntropy) - leftEntropy;
	}

	double _leftMutualInformation(ConstituentTree y, ConstituentGradientSingleton gradient) {
		ConstituentGradientSingleton mutualGradient = new ConstituentGradientSingleton(gradient.map);
		double mutualInformation = mutualInformation(y.left, mutualGradient);
		ConstituentGradientSingleton rightGradient = new ConstituentGradientSingleton(gradient.map);
		ConstituentGradientSingleton leftGradient = new ConstituentGradientSingleton(gradient.map);
		ConstituentGradientSingleton decadentGradient = new ConstituentGradientSingleton(gradient.map);
		double rightEntropy = y.right.entropy(rightGradient);
		double leftEntropy = y.left.entropy(leftGradient);

		double decadent = decadent(leftEntropy, rightEntropy, leftGradient, rightGradient.clone(), decadentGradient);
		if (decadent <= 0 || decadent >= 1) {
			log.info("decadent = " + decadent);
			//			throw new RuntimeException("decadent <= 0 || decadent >= 1");
		}

		gradient.add(mutualGradient.mul(decadent));
		gradient.add(decadentGradient.mul(mutualInformation));
		gradient.sub(rightGradient);
		return mutualInformation * decadent - rightEntropy;
	}

	double _rightMutualInformation(ConstituentTree y, ConstituentGradientSingleton gradient) {
		ConstituentGradientSingleton mutualGradient = new ConstituentGradientSingleton(gradient.map);
		double mutualInformation = mutualInformation(y.right, mutualGradient);
		ConstituentGradientSingleton rightGradient = new ConstituentGradientSingleton(gradient.map);
		ConstituentGradientSingleton leftGradient = new ConstituentGradientSingleton(gradient.map);
		ConstituentGradientSingleton decadentGradient = new ConstituentGradientSingleton(gradient.map);
		double rightEntropy = y.right.entropy(rightGradient);
		double leftEntropy = y.left.entropy(leftGradient);

		double decadent = decadent(rightEntropy, leftEntropy, rightGradient, leftGradient.clone(), decadentGradient);
		if (decadent <= 0 || decadent >= 1) {
			log.info("decadent = " + decadent);
			//			throw new RuntimeException("decadent <= 0 || decadent >= 1");
		}

		gradient.add(mutualGradient.mul(decadent));
		gradient.add(decadentGradient.mul(mutualInformation));
		gradient.sub(leftGradient);
		return mutualInformation * decadent - leftEntropy;
	}

	public abstract double entropy(ConstituentGradientSingleton gradient);

	public abstract Action goldAction(Constituent y, ConstituentTree gold) throws Exception;

	public abstract Action goldAction(ConstituentTree y, ConstituentTree gold) throws Exception;

	public abstract Action goldAction(ConstituentLeaf y, ConstituentTree gold);

	public abstract Action _goldAction(ConstituentLeaf constituentLeaf, ConstituentTree gold);

	public abstract Action _goldAction(ConstituentTree constituentLeaf, ConstituentTree gold) throws Exception;

	static public double decadent(double x, double y) {
		return 1 - Math.tanh((y / x) * entropyMap.get(Coefficient.wScale)) * entropyMap.get(Coefficient.wDiminish);
	}

	static public double decadent(double x, double y, ConstituentGradient xGradient, ConstituentGradient yGradient, ConstituentGradientSingleton gradient) {
		double wScale = entropyMap.get(Coefficient.wScale);
		double wDiminish = entropyMap.get(Coefficient.wDiminish);
		double r = y / x;
		double tanh = Math.tanh(r * wScale);
		double sech = 1 - tanh * tanh;
		gradient.add(Coefficient.wDiminish, -tanh);
		gradient.add(Coefficient.wScale, -wDiminish * sech * r);
		gradient.add(xGradient.mul(wDiminish * sech * y / (x * x)));
		gradient.add(yGradient.mul(-wDiminish * sech / x));
		return 1 - tanh * wDiminish;
	}

	abstract public boolean equals(Constituent obj);

	abstract public boolean _equals(ConstituentLeaf obj);

	abstract public boolean _equals(ConstituentTree obj);

	abstract public boolean equals(ConstituentLeaf obj);

	abstract public boolean equals(ConstituentTree obj);
}
