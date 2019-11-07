package com.robot.syntax;

import java.util.Map;

import com.robot.syntax.Constituent.Action;
import com.robot.syntax.Constituent.Coefficient;

public class ConstituentTrainer {
	Action predAction;
	Action goldAction;
	Map<Action, Double> actionMap;
	ConstituentGradientSingleton gradient;
	final static double weightDecay = 0.0025;

	ConstituentTrainer(Action predAction, Action goldAction, Map<Action, Double> actionMap, ConstituentGradientSingleton gradient) {
		this.predAction = predAction;
		this.goldAction = goldAction;
		this.actionMap = actionMap;
		this.gradient = gradient;
		gradient.modified = true;
	}

	void updateGradient(ConstituentTree x, ConstituentTree y) {
		double error = actionMap.get(predAction) - actionMap.get(goldAction);

		System.out.println("error = " + error);
		if (error != error)
			throw new RuntimeException("error != error");
		System.out.println("predAction = " + predAction + " = " + actionMap.get(predAction));
		System.out.println("goldAction = " + goldAction + " = " + actionMap.get(goldAction));

		switch (predAction) {
		case norm:
			x.normMutualInformation(y, gradient);
			break;
		case swap:
			x.swapMutualInformation(y, gradient);
			break;
		case left:
			x.leftMutualInformation(y, gradient);
			break;
		case right:
			x.rightMutualInformation(y, gradient);
			break;
		case _left:
			x._leftMutualInformation(y, gradient);
			break;
		case _right:
			x._rightMutualInformation(y, gradient);
			break;
		}

		ConstituentGradientSingleton _gradient = new ConstituentGradientSingleton(gradient.map);
		switch (goldAction) {
		case norm:
			x.normMutualInformation(y, _gradient);
			break;
		case swap:
			x.swapMutualInformation(y, _gradient);
			break;
		case left:
			x.leftMutualInformation(y, _gradient);
			break;
		case right:
			x.rightMutualInformation(y, _gradient);
			break;
		case _left:
			x._leftMutualInformation(y, _gradient);
			break;
		case _right:
			x._rightMutualInformation(y, _gradient);
			break;
		}

		gradient.sub(_gradient);
		weightDecay();
	}

	void weightDecay() {
		for (Map.Entry<Coefficient, Double> p : gradient.entrySet()) {
			Coefficient c = p.getKey();
			gradient.put(c, p.getValue() + 2 * Constituent.entropyMap.get(c) * weightDecay);
		}
	}

	void updateGradient(ConstituentTree x, ConstituentLeaf y) {
		double error = actionMap.get(predAction) - actionMap.get(goldAction);

		System.out.println("error = " + error);
		System.out.println("predAction = " + predAction + " = " + actionMap.get(predAction));
		System.out.println("goldAction = " + goldAction + " = " + actionMap.get(goldAction));

		switch (predAction) {
		case left:
			x.leftMutualInformation(y, gradient);
			break;
		case right:
			x.rightMutualInformation(y, gradient);
			break;
		}

		ConstituentGradientSingleton _gradient = new ConstituentGradientSingleton(gradient.map);
		switch (goldAction) {
		case left:
			x.leftMutualInformation(y, _gradient);
			break;
		case right:
			x.rightMutualInformation(y, _gradient);
			break;
		}
		gradient.sub(_gradient);
		weightDecay();
	}

	void updateGradient(ConstituentLeaf x, ConstituentTree y) {
		double error = actionMap.get(predAction) - actionMap.get(goldAction);

		System.out.println("error = " + error);
		System.out.println("predAction = " + predAction + " = " + actionMap.get(predAction));
		System.out.println("goldAction = " + goldAction + " = " + actionMap.get(goldAction));

		switch (predAction) {
		case _left:
			x._leftMutualInformation(y, gradient);
			break;
		case _right:
			x._rightMutualInformation(y, gradient);
			break;
		}

		ConstituentGradientSingleton _gradient = new ConstituentGradientSingleton(gradient.map);
		switch (goldAction) {
		case _left:
			x._leftMutualInformation(y, _gradient);
			break;
		case _right:
			x._rightMutualInformation(y, _gradient);
			break;
		}
		gradient.sub(_gradient);
		weightDecay();
	}
}
