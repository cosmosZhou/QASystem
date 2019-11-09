package com.robot.semantic.RNN;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Set;

import com.robot.syntax.SyntacticTree;
import com.util.Utility;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.LabelFactory;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;

/**
 * A {@code LabeledScoredTreeNode} represents a tree composed of a root label, a
 * score, and an array of daughter parse trees. A parse tree derived from a rule
 * provides information about the category of the root as well as a composite of
 * the daughter categories.
 *
 * @author Christopher Manning
 */
public class LabeledScoredTreeNode extends Tree {

	private static final long serialVersionUID = -8992385140984593817L;

	/**
	 * Label of the parse tree.
	 */
	private Label label; // = null;

	/**
	 * Score of <code>TreeNode</code>
	 */
	private double score = Double.NaN;

	/**
	 * Daughters of the parse tree.
	 */
	private Tree[] daughterTrees; // = null;

	/**
	 * Create an empty parse tree.
	 */
	public LabeledScoredTreeNode() {
		setChildren(EMPTY_TREE_ARRAY);
	}

	/**
	 * Create a leaf parse tree with given word.
	 *
	 * @param label
	 *            the <code>Label</code> representing the <i>word</i> for this
	 *            new tree leaf.
	 */
	public LabeledScoredTreeNode(Label label) {
		this(label, Double.NaN);
	}

	/**
	 * Create a leaf parse tree with given word and score.
	 *
	 * @param label
	 *            The <code>Label</code> representing the <i>word</i> for
	 * @param score
	 *            The score for the node this new tree leaf.
	 */
	public LabeledScoredTreeNode(Label label, double score) {
		this();
		this.label = label;
		this.score = score;
	}

	/**
	 * Create parse tree with given root and array of daughter trees.
	 *
	 * @param label
	 *            root label of tree to construct.
	 * @param daughterTreesList
	 *            List of daughter trees to construct.
	 */
	public LabeledScoredTreeNode(Label label, List<Tree> daughterTreesList) {
		this.label = label;
		setChildren(daughterTreesList);
	}

	public LabeledScoredTreeNode(Label label, Tree... daughterTreesList) {
		this.label = label;
		setChildren(daughterTreesList);
	}

	/**
	 * Returns an array of children for the current node, or null if it is a
	 * leaf.
	 */
	@Override
	public Tree[] children() {
		return daughterTrees;
	}

	/**
	 * Sets the children of this <code>Tree</code>. If given <code>null</code>,
	 * this method sets the Tree's children to the canonical zero-length Tree[]
	 * array.
	 *
	 * @param children
	 *            An array of child trees
	 */
	@Override
	public void setChildren(Tree[] children) {
		if (children == null) {
			daughterTrees = EMPTY_TREE_ARRAY;
		} else {
			daughterTrees = children;
		}
	}

	/**
	 * Returns the label associated with the current node, or null if there is
	 * no label
	 */
	@Override
	public CoreLabel label() {
		return (CoreLabel) label;
	}

	/**
	 * Sets the label associated with the current node, if there is one.
	 */
	@Override
	public void setLabel(final Label label) {
		this.label = label;
	}

	/**
	 * Returns the score associated with the current node, or Nan if there is no
	 * score
	 */
	@Override
	public double score() {
		return score;
	}

	/**
	 * Sets the score associated with the current node, if there is one
	 */
	@Override
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * Return a <code>TreeFactory</code> that produces trees of the same type as
	 * the current <code>Tree</code>. That is, this implementation, will produce
	 * trees of type <code>LabeledScoredTree(Node|Leaf)</code>. The
	 * <code>Label</code> of <code>this</code> is examined, and providing it is
	 * not <code>null</code>, a <code>LabelFactory</code> which will produce
	 * that kind of <code>Label</code> is supplied to the
	 * <code>TreeFactory</code>. If the <code>Label</code> is <code>null</code>,
	 * a <code>StringLabelFactory</code> will be used. The factories returned on
	 * different calls a different: a new one is allocated each time.
	 *
	 * @return a factory to produce labeled, scored trees
	 */
	@Override
	public TreeFactory treeFactory() {
		LabelFactory lf = (label() == null) ? CoreLabel.factory() : label().labelFactory();
		return new LabeledScoredTreeFactory(lf);
	}

	// extra class guarantees correct lazy loading (Bloch p.194)
	private static class TreeFactoryHolder {
		static final TreeFactory tf = new LabeledScoredTreeFactory();
	}

	/**
	 * Return a <code>TreeFactory</code> that produces trees of the
	 * <code>LabeledScoredTree{Node|Leaf}</code> type. The factory returned is
	 * always the same one (a singleton).
	 *
	 * @return a factory to produce labeled, scored trees
	 */
	public static TreeFactory factory() {
		return TreeFactoryHolder.tf;
	}

	/**
	 * Return a <code>TreeFactory</code> that produces trees of the
	 * <code>LabeledScoredTree{Node|Leaf}</code> type, with the
	 * <code>Label</code> made with the supplied <code>LabelFactory</code>. The
	 * factory returned is a different one each time
	 *
	 * @param lf
	 *            The LabelFactory to use
	 * @return a factory to produce labeled, scored trees
	 */
	public static TreeFactory factory(LabelFactory lf) {
		return new LabeledScoredTreeFactory(lf);
	}

	private static final NumberFormat nf = new DecimalFormat("0.000");

	@Override
	public String nodeString() {
		StringBuilder buff = new StringBuilder();
		buff.append(super.nodeString());
		if (!Double.isNaN(score)) {
			buff.append(" [").append(nf.format(-score)).append("]");
		}
		return buff.toString();
	}

	int maxWordLength() {
		int max = Utility.length(this.label.toString());
		for (Tree t : this.daughterTrees) {
			int _max = ((LabeledScoredTreeNode) t).maxWordLength();
			if (_max > max) {
				max = _max;
			}
		}
		return max;
	}

	//added by Cosmos
	@Override
	public String toString() {
		String tree = null;
		//		SyntacticTree parent = this.parent;
		//		this.parent = null;
		try {
			//			String[] pos = getPOS();
			//			String[] dep = getDEP();
			//			String[] seg = getLEX();

			//			int size = seg.length;
			//			if (size != pos.length || size != dep.length) {
			//				System.out.println("size != pos.length || size != dep.length");
			//			}

			int max_width = this.maxWordLength();
			//			for (int i = 0; i < size; ++i) {
			//				max_width = Utility.max(max_width, Utility.length(seg[i]), Utility.length(dep[i]), Utility.length(pos[i]));
			//			}

			++max_width;

			//			SyntacticTree.Iterator it = iterator();

			//			int length_lexeme[] = new int[size];
			//			int left_parenthesis[] = new int[size];
			//			int right_parenthesis[] = new int[size];
			//
			//			int index = 0;
			//			while (it.hasNext() && index < size) {
			//				left_parenthesis[index] = it.left_parenthesis();
			//				right_parenthesis[index] = it.right_parenthesis();
			//
			//				if (!seg[index].equals(it.next().segOriginal)) {
			//					System.out.println("inconsistent lexeme");
			//				}
			//
			//				seg[index] = protectParenthesis(seg[index]);
			//				length_lexeme[index] = Utility.length(seg[index]);
			//
			//				int length = length_lexeme[index] + left_parenthesis[index];
			//				if (index > 0)
			//					length += right_parenthesis[index - 1];
			//
			//				if (max_width < length) {
			//					max_width = length;
			//				}
			//
			//				++index;
			//			}

			//			String infix = "";
			//			for (index = 0; index < size; ++index) {
			//				int length = max_width - length_lexeme[index] - left_parenthesis[index];
			//				if (index > 0)
			//					length -= right_parenthesis[index - 1];
			//
			//				infix += Utility.toString(length, ' ');
			//
			//				infix += Utility.toString(left_parenthesis[index], '(');
			//				infix += seg[index];
			//				infix += Utility.toString(right_parenthesis[index], ')');
			//			}
			//
			//			if (!infix.replaceAll("\\s+", "").equals(this.infixExpression())) {
			//				System.out.println("infixExpression error");
			//				System.out.println(infix.replaceAll("\\s+", ""));
			//				System.out.println(this.infixExpression());
			//			}
			//
			String segString = "";
			String posString = "";
			String depString = "";
			//			seg = getLEX();
			//			for (int i = 0; i < pos.length; ++i) {
			//				if (bSegInclusive)
			//					segString += Utility.toString(max_width - Utility.length(seg[i]), ' ') + seg[i];
			//				posString += Utility.toString(max_width - Utility.length(pos[i]), ' ') + pos[i];
			//				depString += Utility.toString(max_width - Utility.length(dep[i]), ' ') + dep[i];
			//			}

			Utility.LNodeShadow LNodeShadow = buildShadowTree();
			LNodeShadow.max_width = max_width;

			tree = LNodeShadow.toString();
			//			tree = ('\n' + tree).replace("\n ", "\n;").substring(1);

			//			tree += infix + "\n";
			//			if (bSegInclusive)
			//				tree += segString + "\n";
			//			tree += posString + "\n";
			//			tree += depString + "\n";
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//			this.parent = parent;		
		}

		return tree;
		//		return tree + "\n" + this.infixExpression();
	}

	// if relation is null, it is a predicate;
	public Utility.LNodeShadow buildShadowTree() {
		// recursive inorder scan used to build the shadow tree
		// create the new shadow tree;
		Utility.LNodeShadow newNode = new Utility.LNodeShadow(this.label.toString());
		// tree node
		LabeledScoredTreeNode leftChildren[] = new LabeledScoredTreeNode[daughterTrees.length / 2];
		LabeledScoredTreeNode rightChildren[] = new LabeledScoredTreeNode[daughterTrees.length - leftChildren.length];

		int index = 0;
		for (int i = 0; i < leftChildren.length; i++) {
			leftChildren[i] = (LabeledScoredTreeNode) this.daughterTrees[index++];

		}
		for (int i = 0; i < rightChildren.length; i++) {
			rightChildren[i] = (LabeledScoredTreeNode) this.daughterTrees[index++];

		}
		if (leftChildren != null && leftChildren.length > 0) {
			newNode.x = new Utility.LNodeShadow[leftChildren.length];
			int i = 0;
			for (LabeledScoredTreeNode tree : leftChildren)
				newNode.x[i++] = tree.buildShadowTree();
		}
		// allocate node for left child at next level in tree;

		if (rightChildren != null && rightChildren.length > 0) {
			newNode.y = new Utility.LNodeShadow[rightChildren.length];
			int i = 0;
			for (LabeledScoredTreeNode tree : rightChildren)
				newNode.y[i++] = tree.buildShadowTree();
		}

		return newNode;
	}

	public String infixExpression() {
		if (this.isLeaf()) {
			return label.toString();
		}
		String infix = "";
		//		infix += ((CoreLabel) label).get(PredictedClass.class);
		infix += label.toString();
		for (Tree tree : this.daughterTrees) {
			infix += "(" + ((LabeledScoredTreeNode) tree).infixExpression() + ")";
		}
		return infix;
	}

	public void forwardPropagateTree(Set<String> binaryProductions, Set<String> unaryProductions) {
		if (isLeaf()) {
			// We do nothing for the leaves.  The preterminals will
			// calculate the classification for this word/tag.  In fact, the
			// recursion should not have gotten here (unless there are
			// degenerate trees of just one leaf)
			throw new AssertionError("We should not have reached leaves in forwardPropagate");
		} else if (isPreTerminal()) {
			unaryProductions.add(label().value());
		} else if (children().length == 1) {
			throw new AssertionError("Non-preterminal nodes of size 1 should have already been collapsed");
		} else if (children().length == 2) {
			((LabeledScoredTreeNode) children()[0]).forwardPropagateTree(binaryProductions, unaryProductions);
			((LabeledScoredTreeNode) children()[1]).forwardPropagateTree(binaryProductions, unaryProductions);

			//			String leftCategory = children()[0].label().value();
			//			String rightCategory = children()[1].label().value();
			//			System.out.println(children()[0].label().value() + " + " + children()[1].label().value() + " = " + label().value());
			binaryProductions.add(label().value());
		} else {
			throw new AssertionError("Tree not correctly binarized");
		}
	} // end forwardPropagateTree
}
