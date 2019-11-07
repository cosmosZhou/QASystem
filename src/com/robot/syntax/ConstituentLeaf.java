package com.robot.syntax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.robot.Sentence;
import com.robot.semantic.RNN.LabeledScoredTreeNode;
import com.robot.semantic.RNN.RNNParaphrase;
import com.robot.syntax.Constituent.Action;
import com.robot.syntax.Constituent.Coefficient;
import com.util.Utility;
import com.util.Utility.LNodeShadow;

import edu.stanford.nlp.ling.CoreLabel;

public class ConstituentLeaf extends Constituent {
	public ConstituentLeaf(String seg, Coefficient pos) {
		this.seg = seg;
		this.pos = pos;
	}

	public Constituent clone() throws CloneNotSupportedException {
		return new ConstituentLeaf(seg, pos);
	}

	String seg;
	Coefficient pos;

	@Override
	int maxWordLength() {
		return Utility.length(seg);
	}

	@Override
	public LNodeShadow buildShadowTree() {
		// recursive inorder scan used to build the shadow tree
		// create the new shadow tree;
		Utility.LNodeShadow newNode = new Utility.LNodeShadow(this.toString());

		return newNode;
	}

	@Override
	public String toString() {
		return seg;
	}

	@Override
	public LabeledScoredTreeNode toLabeledScoredTreeNode() {
		return new LabeledScoredTreeNode(CoreLabel.wordFromString(pos.toString()), new LabeledScoredTreeNode(CoreLabel.wordFromString(seg)));
	}

	@Override
	public Constituent equality(Constituent right, Cache cache) {
		return right._equality(this, cache);
	}

	@Override
	public Constituent _equality(ConstituentLeaf right, Cache cache) {
		return right.equality(this, cache);
	}

	@Override
	public Constituent _equality(ConstituentTree right, Cache cache) {
		return right.equality(this, cache);
	}

	public Constituent equality(ConstituentTree y, Cache cache) {
		if (mutualInformation(y.right, cache) > mutualInformation(y.left, cache))
			y.right = equality(y.right, cache);
		else
			y.left = equality(y.left, cache);
		return y;
	}

	@Override
	public Constituent equality(ConstituentLeaf y, Cache cache) {
		return new ConstituentEquality(this, y);
	}

	@Override
	public Constituent equality(Constituent right, ConstituentGradientSingleton gradient, ConstituentTree gold) {
		return right._equality(this, gradient, gold);
	}

	@Override
	public Constituent _equality(ConstituentLeaf right, ConstituentGradientSingleton gradient, ConstituentTree gold) {
		return right.equality(this, gradient, gold);
	}

	@Override
	public Constituent _equality(ConstituentTree right, ConstituentGradientSingleton gradient, ConstituentTree gold) {
		return right.equality(this, gradient, gold);
	}

	@Override
	public Action goldAction(ConstituentTree y, ConstituentTree gold) {
		String[] leftSet = gold.left.lexemeSet();
		String[] rightSet = gold.right.lexemeSet();
		String[] setX = lexemeSet();
		String[] leftSetY = y.left.lexemeSet();
		String[] rightSetY = y.right.lexemeSet();

		if (Utility.equals(Utility.merge_sort(setX, leftSetY), leftSet)

				&& Utility.equals(rightSetY, rightSet)) {
			return Action._left;
		}

		if (Utility.equals(Utility.merge_sort(setX, rightSetY), rightSet)

				&& Utility.equals(leftSetY, leftSet)) {
			return Action._right;
		}

		throw new RuntimeException();
	}

	public Constituent equality(ConstituentTree y, ConstituentGradientSingleton gradient, ConstituentTree gold) {
		Action goldAction = goldAction(y, gold);

		double _leftAbsorb = mutualInformation(y.left, gradient.map);
		double _rightAbsorb = mutualInformation(y.right, gradient.map);
		Map<Action, Double> actionMap = new HashMap<>();

		actionMap.put(Action._left, _leftAbsorb);
		actionMap.put(Action._right, _rightAbsorb);

		Action predAction = _rightAbsorb > _leftAbsorb ? Action._right : Action._left;
		if (!predAction.equals(goldAction)) {
			new ConstituentTrainer(predAction, goldAction, actionMap, gradient).updateGradient(this, y);
		}

		return goldAction.absorb(this, y, gradient, gold);

	}

	@Override
	public Constituent equality(ConstituentLeaf y, ConstituentGradientSingleton gradient, ConstituentTree gold) {
		return new ConstituentEquality(this, y);
	}

	public String unadornedExpression() {
		return seg;
	}

	@Override
	public String infix() {
		return seg;
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
	double mutualInformation(ConstituentTree y, Cache cache) {
		Map<Constituent, Double> mapInformation = getMap(cache);
		if (mapInformation.containsKey(y)) {
			return mapInformation.get(y);
		}

		double mutualInformation = Math.max(mutualInformation(y.left, cache), mutualInformation(y.right, cache));
		mapInformation.put(y, mutualInformation);
		return mutualInformation;
	}

	@Override
	double mutualInformation(ConstituentTree y, ConstituentGradientSingleton gradient) {
		double leftEntropy = mutualInformation(y.left, gradient.map);
		double rightEntropy = mutualInformation(y.right, gradient.map);
		if (leftEntropy > rightEntropy) {
			mutualInformation(y.left, gradient);
		} else {
			mutualInformation(y.right, gradient);
		}
		double mutualInformation = Math.max(leftEntropy, rightEntropy);

		return mutualInformation;
	}

	public double similarity(ConstituentLeaf y) {
		try {
			LabeledScoredTreeNode t = new ConstituentEquality(this, y).toLabeledScoredTreeNode();
			return RNNParaphrase.instance.classify(t);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//			e.printStackTrace();
		}

		return 0;
	}

	@Override
	double mutualInformation(ConstituentLeaf y, Cache cache) {
		Map<Constituent, Double> mapInformation = getMap(cache);
		if (mapInformation.containsKey(y)) {
			return mapInformation.get(y);
		}

		double similarity = 0;
		if (seg.equals(y.seg)) {
			similarity = 1;
		} else {
			similarity = similarity(y);
		}

		similarity *= entropyMap.get(Coefficient.wSeg);

		double posWeight = 0;
		if (pos.equals(y.pos)) {
			posWeight = entropyMap.get(pos);
		} else {
			posWeight = Utility.average(entropyMap.get(pos), entropyMap.get(y.pos)) * entropyMap.get(Coefficient.wPosHetero);
		}

		double mutualInformation = posWeight * similarity;
		mapInformation.put(y, mutualInformation);
		return mutualInformation;
	}

	@Override
	double mutualInformation(ConstituentLeaf y, ConstituentGradientSingleton gradient) {
		double similarityWords = 0;
		if (seg.equals(y.seg)) {
			similarityWords = 1;
		} else {
			similarityWords = similarity(y);
		}

		double similarity = similarityWords * entropyMap.get(Coefficient.wSeg);

		double posWeight = 0;
		if (pos.equals(y.pos)) {
			posWeight = entropyMap.get(pos);

			gradient.add(pos, similarity);
		} else {
			double average = Utility.average(entropyMap.get(pos), entropyMap.get(y.pos));
			posWeight = average * entropyMap.get(Coefficient.wPosHetero);

			gradient.add(pos, similarity * entropyMap.get(Coefficient.wPosHetero) / 2);
			gradient.add(y.pos, similarity * entropyMap.get(Coefficient.wPosHetero) / 2);
		}

		gradient.add(Coefficient.wSeg, posWeight * similarityWords);
		double mutualInformation = posWeight * similarity;
		return mutualInformation;
	}

	@Override
	double _mutualInformation(ConstituentTree y, Cache cache) {
		return y.mutualInformation(this, cache);
	}

	@Override
	double _mutualInformation(ConstituentLeaf y, Cache cache) {
		return y.mutualInformation(this, cache);
	}

	@Override
	double _mutualInformation(ConstituentTree y, ConstituentGradientSingleton gradient) {
		return y.mutualInformation(this, gradient);
	}

	@Override
	double _mutualInformation(ConstituentLeaf y, ConstituentGradientSingleton gradient) {
		return y.mutualInformation(this, gradient);
	}

	@Override
	public void lexemeSet(ArrayList<String> arr) {
		arr.add(seg + '/' + pos);
	}

	@Override
	public double entropy(Cache cache) {
		if (!cache.entropy.containsKey(this)) {
			cache.entropy.put(this, entropyMap.get(this.pos) * entropyMap.get(Coefficient.biasRoot));
		}

		return cache.entropy.get(this);
	}

	@Override
	public double entropy(ConstituentGradientSingleton gradient) {
		gradient.add(pos, entropyMap.get(Coefficient.biasRoot));
		gradient.add(Coefficient.biasRoot, entropyMap.get(pos));
		return entropyMap.get(pos) * entropyMap.get(Coefficient.biasRoot);
	}

	@Override
	public Action goldAction(Constituent y, ConstituentTree gold) {
		return y._goldAction(this, gold);
	}

	@Override
	public Action goldAction(ConstituentLeaf y, ConstituentTree gold) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Action _goldAction(ConstituentLeaf x, ConstituentTree gold) {
		return x.goldAction(this, gold);
	}

	@Override
	public Action _goldAction(ConstituentTree x, ConstituentTree gold) {
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
		return seg.equals(obj.seg) && pos.equals(obj.pos);
	}

	public boolean equals(ConstituentTree obj) {
		return false;
	}

}
