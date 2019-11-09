package com.robot.syntax;

import com.util.Utility;

public class ConstituentEquality extends ConstituentTree {
	public ConstituentEquality(Constituent left, Constituent right) {
		super(Coefficient.EQU, left, right);
	}

	boolean findEquality() {
		return true;
	}

	@Override
	public Constituent clone() throws CloneNotSupportedException {
		return new ConstituentEquality(left.clone(), right.clone());
	}

	@Override
	public String toString() {
		return unadornedExpression();
	}

	// if relation is null, it is a predicate;
	public Utility.LNodeShadow buildShadowTree() {
		// recursive inorder scan used to build the shadow tree
		// create the new shadow tree;
		Utility.LNodeShadow newNode = new Utility.LNodeShadow(unadornedExpression());

		return newNode;
	}

	@Override
	public String infix() {
		return left.infix() + "/" + right.infix();
	}

	public String unadornedExpression() {
		return left.unadornedExpression() + "/" + right.unadornedExpression();
	}

	@Override
	public int maxWordLength() {
		return Utility.length(unadornedExpression());
	}
}
