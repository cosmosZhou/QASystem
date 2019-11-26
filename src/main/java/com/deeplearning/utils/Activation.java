package com.deeplearning.utils;

import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;

import com.deeplearning.utils.Activator;

public enum Activation {
	hard_sigmoid, sigmoid, tanh, ReLU, linear, softmax;

	public FloatMatrix activate(FloatMatrix x) {
		switch (this) {
		case hard_sigmoid:
			return Activator.hard_sigmoid(x);
		case sigmoid:
			return Activator.logistic(x);
		case linear:
			return x;
		case tanh:
			return Activator.tanh(x);
		case ReLU:
			return Activator.ReLU(x);
		case softmax:
			return Activator.softmax(x);
		}

		return x;
	}

	public DoubleMatrix activate(DoubleMatrix x) {
		switch (this) {
		case hard_sigmoid:
			return Activator.hard_sigmoid(x);
		case sigmoid:
			return Activator.logistic(x);
		case linear:
			return x;
		case tanh:
			return Activator.tanh(x);
		case ReLU:
			return Activator.ReLU(x);
		case softmax:
			return Activator.softmax(x);
		}

		return x;
	}

	public DoubleMatrix activateDerivative(DoubleMatrix y) {
		switch (this) {
		case hard_sigmoid:
			return Activator.hard_sigmoidDerivative(y);
		case sigmoid:
			return Activator.logisticDerivative(y);
		case linear:
			return DoubleMatrix.ones(y.columns);
		case tanh:
			return Activator.tanhDerivative(y);
		case ReLU:
			return Activator.ReLUDerivative(y);
		case softmax:
			return null;
		}

		return null;
	}
}
