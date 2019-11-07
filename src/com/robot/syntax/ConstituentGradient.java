package com.robot.syntax;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.robot.syntax.Constituent.Coefficient;
import com.util.Utility;
import com.util.Utility.Printer;

public class ConstituentGradient extends HashMap<Coefficient, Double> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final double learningRate = 0.01;

	@Override
	public ConstituentGradient clone() {
		ConstituentGradient gradient = new ConstituentGradient();
		gradient.putAll(this);
		return gradient;
	}

	public ConstituentGradient() {
	}

	ConstituentGradient mul(double lambda) {
		for (Map.Entry<Coefficient, Double> e : entrySet()) {
			put(e.getKey(), e.getValue() * lambda);
		}
		return this;
	}

	public ConstituentGradient add(Coefficient x, double delta) {
		if (containsKey(x)) {
			put(x, get(x) + delta);
		} else {
			put(x, delta);
		}
		return this;
	}

	public ConstituentGradient sub(Coefficient x, double delta) {
		if (containsKey(x)) {
			put(x, get(x) - delta);
		} else {
			put(x, -delta);
		}
		return this;
	}

	public ConstituentGradient add(ConstituentGradient gradient) {
		for (Map.Entry<Coefficient, Double> e : gradient.entrySet()) {
			add(e.getKey(), e.getValue());
		}
		return this;
	}

	ConstituentGradient sub(ConstituentGradient gradient) {
		for (Map.Entry<Coefficient, Double> e : gradient.entrySet()) {
			sub(e.getKey(), e.getValue());
		}
		return this;
	}

	public void applyGradientDescent() {
		for (Map.Entry<Coefficient, Double> e : entrySet()) {
			Constituent.updateEntropyMap(e.getKey(), e.getValue() * -learningRate);
		}

		try {
			Printer printer = new Printer(Utility.workingDirectory + "models/entropyMap.txt");
			for (Coefficient var : Coefficient.values()) {
				if (Constituent.entropyMap.containsKey(var))
					System.out.println(var + " = " + Constituent.entropyMap.get(var));
			}
			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
