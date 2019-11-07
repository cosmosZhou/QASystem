package com.robot.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.common.collect.Sets;
import com.robot.semantic.RNN.LabeledScoredTreeNode;
import com.robot.syntax.Constituent.Action;
import com.robot.syntax.Constituent.Coefficient;
import com.robot.syntax.ConstituentTree.Struct;
import com.util.Utility;

import edu.stanford.nlp.ling.CoreLabel;

public class ConstituentTree extends Constituent {
	Constituent left, right;
	Coefficient dep;

	public boolean isPossessiveCase() {
		if (left instanceof ConstituentLeaf)
			return false;

		ConstituentTree left = (ConstituentTree) this.left;		

		if (!left.dep.equals(Coefficient.de))
			return false;
		if (left.right instanceof ConstituentLeaf) {
			ConstituentLeaf right = (ConstituentLeaf) left.right;
			if (right.pos.equals(Coefficient.DE)) {
				return false;
			}
		}

		return true;

	}

	public Constituent declareSubject() {
		switch (dep) {
		case suj:
		case de:
		case adj: // consider the case 你家的处所, and 消费者会受到这样的对待
			if (isPossessiveCase()) {
				ConstituentTree possessiveCase = (ConstituentTree) left;
				left = possessiveCase.right;
				possessiveCase.right = this;
				return possessiveCase;
			}

		}
		return this;
	}

	@Override
	public String infix() {
		return "(" + left.infix() + ")" + dep + "(" + right.infix() + ")";
	}

	public LabeledScoredTreeNode toLabeledScoredTreeNode() throws Exception {
		return new LabeledScoredTreeNode(CoreLabel.wordFromString(dep.toString()), left.toLabeledScoredTreeNode(), right.toLabeledScoredTreeNode());
	}

	public ConstituentTree(Coefficient label, Constituent left, Constituent right) {
		this.dep = label;
		this.left = left;
		this.right = right;
	}

	public int maxWordLength() {
		int max = Utility.length(this.dep.toString());

		int _max = Integer.MIN_VALUE;
		if (left != null) {
			_max = left.maxWordLength();
			if (_max > max) {
				max = _max;
			}
		}
		if (right != null) {
			_max = right.maxWordLength();
			if (_max > max) {
				max = _max;
			}
		}
		return max;
	}

	public String unadornedExpression() {
		return left.unadornedExpression() + right.unadornedExpression();
	}

	@Override
	public String toString() {
		String tree = null;
		try {
			int max_width = this.maxWordLength();
			++max_width;
			Utility.LNodeShadow lNodeShadow = buildShadowTree();
			lNodeShadow.max_width = max_width;

			tree = lNodeShadow.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

		return tree;
	}

	// if relation is null, it is a predicate;
	public Utility.LNodeShadow buildShadowTree() {
		// recursive inorder scan used to build the shadow tree
		// create the new shadow tree;
		Utility.LNodeShadow newNode = new Utility.LNodeShadow(this.dep.toString());
		// tree node
		if (left != null) {
			newNode.x = new Utility.LNodeShadow[1];
			newNode.x[0] = left.buildShadowTree();
		}
		// allocate node for left child at next level in tree;

		if (right != null) {
			newNode.y = new Utility.LNodeShadow[1];
			newNode.y[0] = right.buildShadowTree();
		}

		return newNode;
	}

	void setChild(Constituent child, Constituent autre) {
		if (left == child) {
			left = autre;
		} else {
			if (right != child)
				throw new RuntimeException("right != child");
			right = autre;
		}
	}

	double leftMutualInformation(ConstituentTree y, Cache cache) {
		double rightEntropy = right.entropy(cache);
		return left.mutualInformation(y, cache) * decadent(left.entropy(cache), rightEntropy) - rightEntropy;
	}

	double leftMutualInformation(Constituent y, ConstituentGradientSingleton gradient) {
		ConstituentGradientSingleton mutualGradient = new ConstituentGradientSingleton(gradient.map);
		double mutualInformation = left.mutualInformation(y, mutualGradient);
		ConstituentGradientSingleton rightGradient = new ConstituentGradientSingleton(gradient.map);
		ConstituentGradientSingleton leftGradient = new ConstituentGradientSingleton(gradient.map);
		ConstituentGradientSingleton decadentGradient = new ConstituentGradientSingleton(gradient.map);
		double rightEntropy = right.entropy(rightGradient);
		double leftEntropy = left.entropy(leftGradient);

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

	double rightMutualInformation(ConstituentTree y, Cache cache) {
		double leftEntropy = left.entropy(cache);
		return right.mutualInformation(y, cache) * decadent(right.entropy(cache), leftEntropy) - leftEntropy;
	}

	double rightMutualInformation(Constituent y, ConstituentGradientSingleton gradient) {
		ConstituentGradientSingleton mutualGradient = new ConstituentGradientSingleton(gradient.map);
		double mutualInformation = right.mutualInformation(y, mutualGradient);
		ConstituentGradientSingleton rightGradient = new ConstituentGradientSingleton(gradient.map);
		ConstituentGradientSingleton leftGradient = new ConstituentGradientSingleton(gradient.map);
		ConstituentGradientSingleton decadentGradient = new ConstituentGradientSingleton(gradient.map);
		double rightEntropy = right.entropy(rightGradient);
		double leftEntropy = left.entropy(leftGradient);

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

	double normMutualInformation(ConstituentTree y, Cache cache) {
		double bias;
		if (dep.equals(y.dep)) {
			bias = entropyMap.get(dep) * entropyMap.get(Coefficient.biasNorm);
		} else {
			double average = Utility.average(entropyMap.get(dep), entropyMap.get(y.dep));
			bias = average * entropyMap.get(Coefficient.biasNormHetero);
		}
		double infoLeft = left.mutualInformation(y.left, cache);
		double infoRight = right.mutualInformation(y.right, cache);
		return bias + infoLeft + infoRight;
	}

	double normMutualInformation(ConstituentTree y, ConstituentGradientSingleton gradient) {
		double bias;
		if (dep.equals(y.dep)) {
			bias = entropyMap.get(dep) * entropyMap.get(Coefficient.biasNorm);

			gradient.add(dep, entropyMap.get(Coefficient.biasNorm));
			gradient.add(Coefficient.biasNorm, entropyMap.get(dep));
		} else {
			double average = Utility.average(entropyMap.get(dep), entropyMap.get(y.dep));
			bias = average * entropyMap.get(Coefficient.biasNormHetero);
			gradient.add(dep, entropyMap.get(Coefficient.biasNormHetero) / 2);
			gradient.add(y.dep, entropyMap.get(Coefficient.biasNormHetero) / 2);
			gradient.add(Coefficient.biasNormHetero, average);
		}

		double infoLeft = left.mutualInformation(y.left, gradient);
		double infoRight = right.mutualInformation(y.right, gradient);
		return bias + infoLeft + infoRight;
	}

	double swapMutualInformation(ConstituentTree y, Cache cache) {
		double bias;
		if (dep.equals(y.dep)) {
			bias = entropyMap.get(dep) * entropyMap.get(Coefficient.biasSwap);
		} else {
			bias = Utility.average(entropyMap.get(dep), entropyMap.get(y.dep)) * entropyMap.get(Coefficient.biasSwapHetero);
		}
		double infoLeft = left.mutualInformation(y.right, cache);
		double infoRight = right.mutualInformation(y.left, cache);
		return bias + infoLeft + infoRight;
	}

	double swapMutualInformation(ConstituentTree y, ConstituentGradientSingleton gradient) {
		double bias;
		if (dep.equals(y.dep)) {
			bias = entropyMap.get(dep) * entropyMap.get(Coefficient.biasSwap);

			gradient.add(dep, entropyMap.get(Coefficient.biasSwap));
			gradient.add(Coefficient.biasSwap, entropyMap.get(dep));
		} else {
			double average = Utility.average(entropyMap.get(dep), entropyMap.get(y.dep));
			bias = average * entropyMap.get(Coefficient.biasSwapHetero);
			gradient.add(dep, entropyMap.get(Coefficient.biasSwapHetero) / 2);
			gradient.add(y.dep, entropyMap.get(Coefficient.biasSwapHetero) / 2);
			gradient.add(Coefficient.biasSwapHetero, average);
		}
		double infoLeft = left.mutualInformation(y.right, gradient);
		double infoRight = right.mutualInformation(y.left, gradient);
		return bias + infoLeft + infoRight;
	}

	double mutualInformation(ConstituentTree y, Cache cache) {
		Map<Constituent, Double> mapInformation = getMap(cache);
		if (mapInformation.containsKey(y)) {
			return mapInformation.get(y);
		}

		double mutualInformation = Utility.max(normMutualInformation(y, cache),

				swapMutualInformation(y, cache),

				leftMutualInformation(y, cache),

				rightMutualInformation(y, cache),

				_leftMutualInformation(y, cache),

				_rightMutualInformation(y, cache));
		mapInformation.put(y, mutualInformation);
		return mutualInformation;
	}

	double mutualInformation(ConstituentTree y, ConstituentGradientSingleton gradient) {
		double leftAbsorb = leftMutualInformation(y, gradient.map);
		double rightAbsorb = rightMutualInformation(y, gradient.map);
		double _leftAbsorb = _leftMutualInformation(y, gradient.map);
		double _rightAbsorb = _rightMutualInformation(y, gradient.map);
		double noAbsorb = normMutualInformation(y, gradient.map);
		double swapAbsorb = swapMutualInformation(y, gradient.map);

		Struct arr[] = { new Struct(Action.norm, noAbsorb), new Struct(Action.swap, swapAbsorb), new Struct(Action.left, leftAbsorb), new Struct(Action.right, rightAbsorb), new Struct(Action._left, _leftAbsorb), new Struct(Action._right, _rightAbsorb) };
		Utility.PriorityQueue<Struct> pq = new Utility.PriorityQueue<Struct>(arr);
		switch (pq.peek().action) {
		case norm:
			normMutualInformation(y, gradient);
			break;

		case swap:
			swapMutualInformation(y, gradient);
			break;

		case left:
			leftMutualInformation(y, gradient);
			break;

		case right:
			rightMutualInformation(y, gradient);
			break;

		case _left:
			_leftMutualInformation(y, gradient);
			break;

		case _right:
			_rightMutualInformation(y, gradient);
			break;
		default:
			throw new RuntimeException(pq.peek().action.name());
		}

		return pq.peek().degree;
	}

	static class Struct implements Comparable<Struct> {
		Struct(Action identifier, double degree) {
			this.action = identifier;
			this.degree = degree;
		}

		Action action;
		double degree;

		@Override
		public int compareTo(Struct o) {
			// TODO Auto-generated method stub
			return Double.compare(degree, o.degree);
		}
	}

	@Override
	public Constituent _equality(ConstituentTree right, Cache cache) {
		return right.equality(this, cache);
	}

	@Override
	public Constituent _equality(ConstituentLeaf right, Cache cache) {
		return right.equality(this, cache);
	}

	@Override
	public Constituent equality(Constituent right, Cache cache) {
		return right._equality(this, cache);
	}

	public Constituent equality(ConstituentTree y, Cache cache) {
		double leftAbsorb = leftMutualInformation(y, cache);
		double rightAbsorb = rightMutualInformation(y, cache);
		double _leftAbsorb = _leftMutualInformation(y, cache);
		double _rightAbsorb = _rightMutualInformation(y, cache);

		double noAbsorb = normMutualInformation(y, cache);
		double swapAbsorb = swapMutualInformation(y, cache);

		Struct arr[] = { new Struct(Action.norm, noAbsorb), new Struct(Action.swap, swapAbsorb), new Struct(Action.left, leftAbsorb), new Struct(Action.right, rightAbsorb), new Struct(Action._left, _leftAbsorb), new Struct(Action._right, _rightAbsorb) };
		Utility.PriorityQueue<Struct> pq = new Utility.PriorityQueue<Struct>(arr);
		switch (pq.peek().action) {
		case norm:
			left = left.equality(y.left, cache);
			right = right.equality(y.right, cache);
			return this;

		case swap:
			left = left.equality(y.right, cache);
			right = right.equality(y.left, cache);
			return this;

		case left:
			left = left.equality(y, cache);
			return this;

		case right:
			right = right.equality(y, cache);
			return this;

		case _left:
			y.left = this.equality(y.left, cache);
			return y;

		case _right:
			y.right = this.equality(y.right, cache);
			return y;
		default:
			throw new RuntimeException(pq.peek().action.name());
		}
	}

	@Override
	public Constituent equality(ConstituentLeaf y, Cache cache) {
		if (right.mutualInformation(y, cache) > left.mutualInformation(y, cache))
			right = right.equality(y, cache);
		else
			left = left.equality(y, cache);
		return this;
	}

	@Override
	public Constituent _equality(ConstituentTree right, ConstituentGradientSingleton gradient, ConstituentTree gold) throws Exception {
		return right.equality(this, gradient, gold);
	}

	@Override
	public Constituent _equality(ConstituentLeaf right, ConstituentGradientSingleton gradient, ConstituentTree gold) {
		return right.equality(this, gradient, gold);
	}

	@Override
	public Constituent equality(Constituent right, ConstituentGradientSingleton gradient, ConstituentTree gold) throws Exception {
		return right._equality(this, gradient, gold);
	}

	public Action goldAction(ConstituentTree y, ConstituentTree gold) throws Exception {

		String[] leftSet = gold.left.lexemeSet();
		String[] rightSet = gold.right.lexemeSet();
		String[] leftSetX = left.lexemeSet();
		String[] rightSetX = right.lexemeSet();
		String[] leftSetY = y.left.lexemeSet();
		String[] rightSetY = y.right.lexemeSet();

		if (Utility.equals(Utility.merge_sort(leftSetX, leftSetY), leftSet)

				&& Utility.equals(Utility.merge_sort(rightSetX, rightSetY), rightSet) && gold.dep.equals(this.dep)) {
			return Action.norm;
		}

		if (Utility.equals(Utility.merge_sort(leftSetX, leftSetY, rightSetY), leftSet)

				&& Utility.equals(rightSetX, rightSet) && gold.dep.equals(this.dep)) {
			if (Utility.equals(Utility.merge_sort(leftSetX, rightSetX, leftSetY), leftSet)

					&& Utility.equals(rightSetY, rightSet) && gold.dep.equals(y.dep)) {

				if (Action._left.construct(this.clone(), y.clone()).equals(gold))
					return Action._left;
			}

			return Action.left;
		}

		if (Utility.equals(Utility.merge_sort(rightSetX, leftSetY, rightSetY), rightSet)

				&& Utility.equals(leftSetX, leftSet) && gold.dep.equals(this.dep)) {
			if (Utility.equals(Utility.merge_sort(leftSetX, rightSetX, rightSetY), rightSet)

					&& Utility.equals(leftSetY, leftSet) && gold.dep.equals(y.dep)) {
				if (Action._right.construct(this.clone(), y.clone()).equals(gold))
					return Action._right;
			}

			return Action.right;
		}

		if (Utility.equals(Utility.merge_sort(leftSetX, rightSetX, leftSetY), leftSet)

				&& Utility.equals(rightSetY, rightSet) && gold.dep.equals(y.dep)) {
			return Action._left;
		}

		if (Utility.equals(Utility.merge_sort(leftSetX, rightSetX, rightSetY), rightSet)

				&& Utility.equals(leftSetY, leftSet) && gold.dep.equals(y.dep)) {
			return Action._right;
		}

		if (Utility.equals(Utility.merge_sort(leftSetX, rightSetY), leftSet)

				&& Utility.equals(Utility.merge_sort(rightSetX, leftSetY), rightSet) && gold.dep.equals(this.dep)) {
			return Action.swap;
		}

		throw new RuntimeException();
	}

	@Override
	public Action goldAction(ConstituentLeaf y, ConstituentTree gold) {
		String[] leftSet = gold.left.lexemeSet();
		String[] rightSet = gold.right.lexemeSet();
		String[] leftSetX = left.lexemeSet();
		String[] rightSetX = right.lexemeSet();
		String[] setY = y.lexemeSet();

		if (Utility.equals(Utility.merge_sort(leftSetX, setY), leftSet)

				&& Utility.equals(rightSetX, rightSet)) {
			return Action.left;
		}

		if (Utility.equals(Utility.merge_sort(rightSetX, setY), rightSet)

				&& Utility.equals(leftSetX, leftSet)) {
			return Action.right;
		}

		throw new RuntimeException();
	}

	public Constituent equality(ConstituentTree y, ConstituentGradientSingleton gradient, ConstituentTree gold) throws Exception {
		Action goldAction = goldAction(y, gold);
		double leftAbsorb = leftMutualInformation(y, gradient.map);
		double rightAbsorb = rightMutualInformation(y, gradient.map);
		double _leftAbsorb = _leftMutualInformation(y, gradient.map);
		double _rightAbsorb = _rightMutualInformation(y, gradient.map);
		double normAbsorb = normMutualInformation(y, gradient.map);
		double swapAbsorb = swapMutualInformation(y, gradient.map);

		Map<Action, Double> actionMap = new HashMap<>();
		actionMap.put(Action.norm, normAbsorb);
		actionMap.put(Action.swap, swapAbsorb);
		actionMap.put(Action.left, leftAbsorb);
		actionMap.put(Action.right, rightAbsorb);
		actionMap.put(Action._left, _leftAbsorb);
		actionMap.put(Action._right, _rightAbsorb);

		Struct arr[] = { new Struct(Action.norm, normAbsorb), new Struct(Action.swap, swapAbsorb), new Struct(Action.left, leftAbsorb), new Struct(Action.right, rightAbsorb), new Struct(Action._left, _leftAbsorb), new Struct(Action._right, _rightAbsorb) };
		Utility.PriorityQueue<Struct> pq = new Utility.PriorityQueue<Struct>(arr);

		Action predAction = pq.peek().action;
		if (!goldAction.equals(predAction))
			new ConstituentTrainer(predAction, goldAction, actionMap, gradient).updateGradient(this, y);

		return goldAction.absorb(this, y, gradient, gold);
	}

	@Override
	public Constituent equality(ConstituentLeaf y, ConstituentGradientSingleton gradient, ConstituentTree gold) {
		Action goldAction = goldAction(y, gold);

		double leftAbsorb = left.mutualInformation(y, gradient.map);
		double rightAbsorb = right.mutualInformation(y, gradient.map);
		Map<Action, Double> actionMap = new HashMap<>();

		actionMap.put(Action.left, leftAbsorb);
		actionMap.put(Action.right, rightAbsorb);

		Action predAction = rightAbsorb > leftAbsorb ? Action.right : Action.left;
		if (!predAction.equals(goldAction))
			new ConstituentTrainer(predAction, goldAction, actionMap, gradient).updateGradient(this, y);

		return goldAction.absorb(this, y, gradient, gold);
	}

	@Override
	public Constituent clone() throws CloneNotSupportedException {
		return new ConstituentTree(dep, left.clone(), right.clone());
	}

	@Override
	double mutualInformation(Constituent y, Cache cache) {
		return y._mutualInformation(this, cache);
	}

	@Override
	double mutualInformation(Constituent y, ConstituentGradientSingleton gradient) {
		return y._mutualInformation(this, gradient);
	}

	@Override
	double mutualInformation(ConstituentLeaf y, Cache cache) {
		Map<Constituent, Double> mapInformation = getMap(cache);
		if (mapInformation.containsKey(y)) {
			return mapInformation.get(y);
		}
		double mutualInformation = Math.max(left.mutualInformation(y, cache), right.mutualInformation(y, cache));
		mapInformation.put(y, mutualInformation);
		return mutualInformation;
	}

	@Override
	double mutualInformation(ConstituentLeaf y, ConstituentGradientSingleton gradient) {

		double leftEntropy = left.mutualInformation(y, gradient.map);
		double rightEntropy = right.mutualInformation(y, gradient.map);
		if (leftEntropy > rightEntropy) {
			left.mutualInformation(y, gradient);
		} else {
			right.mutualInformation(y, gradient);
		}
		double mutualInformation = Math.max(leftEntropy, rightEntropy);

		return mutualInformation;

	}

	@Override
	double _mutualInformation(ConstituentTree y, Cache cache) {
		return y.mutualInformation(this, cache);
	}

	@Override
	double _mutualInformation(ConstituentTree y, ConstituentGradientSingleton gradient) {
		return y.mutualInformation(this, gradient);
	}

	@Override
	double _mutualInformation(ConstituentLeaf y, Cache cache) {
		return y.mutualInformation(this, cache);
	}

	@Override
	double _mutualInformation(ConstituentLeaf y, ConstituentGradientSingleton gradient) {
		return y.mutualInformation(this, gradient);
	}

	@Override
	public void lexemeSet(ArrayList<String> arr) {
		left.lexemeSet(arr);
		right.lexemeSet(arr);
	}

	@Override
	public double entropy(Cache cache) {
		if (!cache.entropy.containsKey(this)) {
			cache.entropy.put(this, entropyMap.get(this.dep) * entropyMap.get(Coefficient.biasRoot) + (left.entropy(cache) + right.entropy(cache)) * entropyMap.get(Coefficient.biasKinder));
		}

		return cache.entropy.get(this);
	}

	@Override
	public double entropy(ConstituentGradientSingleton gradient) {
		ConstituentGradientSingleton gradientLeft = new ConstituentGradientSingleton(null);
		ConstituentGradientSingleton gradientRight = new ConstituentGradientSingleton(null);

		double biasKinder = left.entropy(gradientLeft) + right.entropy(gradientRight);

		gradient.add(dep, entropyMap.get(Coefficient.biasRoot));
		gradient.add(Coefficient.biasRoot, entropyMap.get(dep));
		gradient.add(Coefficient.biasKinder, biasKinder);
		gradient.add(gradientLeft.mul(entropyMap.get(Coefficient.biasKinder)));
		gradient.add(gradientRight.mul(entropyMap.get(Coefficient.biasKinder)));

		return entropyMap.get(dep) * entropyMap.get(Coefficient.biasRoot) + biasKinder * entropyMap.get(Coefficient.biasKinder);
	}

	@Override
	public Action goldAction(Constituent y, ConstituentTree gold) throws Exception {
		return y._goldAction(this, gold);
	}

	@Override
	public Action _goldAction(ConstituentLeaf x, ConstituentTree gold) {
		return x.goldAction(this, gold);
	}

	@Override
	public Action _goldAction(ConstituentTree x, ConstituentTree gold) throws Exception {
		// TODO Auto-generated method stub
		return x.goldAction(this, gold);
	}

	public boolean equals(Constituent obj) {
		return obj._equals(this);
	}

	public boolean _equals(ConstituentLeaf obj) {
		return obj.equals(this);
	}

	public boolean _equals(ConstituentTree obj) {
		return obj.equals(this);
	}

	public boolean equals(ConstituentLeaf obj) {
		return false;
	}

	public boolean equals(ConstituentTree obj) {
		return this.dep.equals(obj.dep) && left.equals(obj.left) && right.equals(obj.right);
	}

}
