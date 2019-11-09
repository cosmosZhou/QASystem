package com.robot.syntax;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.robot.semantic.RNN.LabeledScoredTreeNode;
import com.robot.syntax.Constituent.Coefficient;
import com.robot.syntax.SyntacticTree.BinarizedTree.Combination;
import com.util.Utility;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.parser.nndep.DependencyTree;
import edu.stanford.nlp.trees.Tree;


public class SyntacticTree implements Serializable, Iterable<SyntacticTree> {

	public boolean containsIrregulation() throws Exception {
		return AnomalyInspecter.FilterSet.containsIrregulation(this) != null;
	}

	private static final long serialVersionUID = -4766669720074872942L;

	public String seg;
	public String lex;
	public String pos;

	/**
	 * 原句中的顺序id
	 */
	public int id;
	public int size = 1;

	/**
	 * dependency relation
	 */
	public String dep;
	public List<SyntacticTree> leftChildren;
	public List<SyntacticTree> rightChildren;
	/**
	 * parent node
	 */
	public SyntacticTree parent = null;

	public SyntacticTree(int id) {
		this(id, null, null, null);
	}

	public SyntacticTree(int id, String word) {
		this(id, word, null, null);
	}

	// change
	public SyntacticTree(int id, String word, String pos) {
		this(id, word, pos, null);
	}

	public SyntacticTree(int id, String word, String pos, String depClass) {
		this.lex = word;
		this.pos = pos;
		this.seg = Utility.simplifyString(word);
		this.id = id;
		this.dep = depClass;
		leftChildren = new ArrayList<SyntacticTree>();
		rightChildren = new ArrayList<SyntacticTree>();
	}

	public SyntacticTree(int id, String segOriginal, String word, String pos, String depClass) {
		this.lex = segOriginal;
		this.seg = word;
		this.pos = pos;
		this.id = id;
		this.dep = depClass;
		leftChildren = new ArrayList<SyntacticTree>();
		rightChildren = new ArrayList<SyntacticTree>();
	}

	public SyntacticTree(int id, String word, String pos, String depClass, SyntacticTree parent) {
		this(id, word, pos, depClass);
		this.parent = parent;
	}

	public SyntacticTree(SyntacticTree tree) {
		SyntacticTree clone = tree.clone();
		this.lex = clone.lex;
		this.pos = clone.pos;
		this.seg = clone.seg;
		this.id = clone.id;
		this.dep = clone.dep;
		leftChildren = clone.leftChildren;
		rightChildren = clone.rightChildren;
		this.validateBranches();
	}

	public SyntacticTree(int id, String word, String pos, String depClass, List<SyntacticTree> leftChildren, List<SyntacticTree> rightChildren) {
		this(id, word, pos, depClass);
		this.leftChildren = leftChildren;
		this.rightChildren = rightChildren;
		//		size = 1;
		for (SyntacticTree tree : leftChildren) {
			tree.parent = this;
			//			size += tree.size();
		}
		for (SyntacticTree tree : rightChildren) {
			tree.parent = this;
			//			size += tree.size();
		}
	}

	public String getDepClass() {
		return this.dep;
	}

	public void setDepClass(String depClass) {
		this.dep = depClass;
	}

	// add the new node as a left child;
	public SyntacticTree addLeftChild(SyntacticTree ch) {
		//		int id = ch.id;
		//		int i = 0;
		//		for (; i < leftChildren.size(); i++) {
		//			int cid = leftChildren.get(i).id;
		//			if (cid > id)
		//				break;
		//		}
		if (this.size != this.evaluateSize()) {
			log.info("size = " + size);
			log.info("evaluateSize = " + evaluateSize());
			throw new RuntimeException("this.size != this.evaluateSize()");
		}

		leftChildren.add(0, ch);
		ch.setParent(this);
		updatesize(ch.size);
		validateIndex();
		if (this.size != this.evaluateSize()) {
			log.info("size = " + size);
			log.info("evaluateSize = " + evaluateSize());
			throw new RuntimeException("this.size != this.evaluateSize()");
		}
		return this;
	}

	public SyntacticTree addRightChild(SyntacticTree ch) {
		rightChildren.add(ch);
		ch.setParent(this);
		updatesize(ch.size);
		validateIndex();
		if (this.size != this.evaluateSize()) {
			log.info("size = " + size);
			log.info("evaluateSize = " + evaluateSize());
			throw new RuntimeException("this.size != this.evaluateSize()");
		}
		return this;
	}

	/**
	 * 更新树大小
	 * 
	 * @param size
	 */
	private void updatesize(int size) {
		this.size += size;
		if (parent != null) {
			parent.updatesize(size);
		}

	}

	/**
	 * 设置父节点
	 * 
	 * @param tree
	 */
	private void setParent(SyntacticTree tree) {
		parent = tree;
	}

	public SyntacticTree getParent() {
		return parent;
	}

	public String toBracketString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(id);
		// if (word != null) {
		// sb.append("[");
		// sb.append(word);
		// sb.append("]");
		// }
		sb.append(" ");
		for (int i = 0; i < leftChildren.size(); i++) {
			sb.append(leftChildren.get(i));
		}
		sb.append("-");
		for (int i = 0; i < rightChildren.size(); i++) {
			sb.append(rightChildren.get(i));
		}
		sb.append("]");
		return sb.toString();
	}

	public int[] toHeadsArray() {
		int[] heads = new int[size];
		toHeadsArray(heads);
		heads[this.id] = -1;
		return heads;
	}

	public void toHeadsArray(int[] heads) {
		for (SyntacticTree ch : leftChildren) {
			heads[ch.id] = id;
			ch.toHeadsArray(heads);
		}

		for (SyntacticTree ch : rightChildren) {
			heads[ch.id] = id;
			ch.toHeadsArray(heads);
		}
	}

	public int size() {
		return size;
	}

	int evaluateSize() {
		int size = 1;
		for (SyntacticTree t : this.leftChildren) {
			size += t.evaluateSize();
		}
		for (SyntacticTree t : this.rightChildren) {
			size += t.evaluateSize();
		}
		return size;
	}

	public int validateSize() {
		size = 1;
		for (SyntacticTree tree : this.leftChildren) {
			size += tree.validateSize();
		}
		for (SyntacticTree tree : this.rightChildren) {
			if (tree == null) {
				System.out.println("tree = " + "is null");
			}
			size += tree.validateSize();
		}
		return size;
	}

	public boolean isValidate() {
		for (SyntacticTree tree : this) {
			if (!POSTagger.instance.tagSet().contains(tree.pos)) {
				return false;
			}
			if (!SyntacticParser.instance.tagSet().contains(tree.dep)) {
				return false;
			}
		}

		return true;
	}

	public List<SyntacticTree> getAllChild() {
		List<SyntacticTree> childs = new ArrayList<SyntacticTree>();
		childs.addAll(leftChildren);
		childs.addAll(rightChildren);
		return childs;
	}

	public boolean contain(SyntacticTree dt) {
		if (this.equals(dt))
			return true;
		for (SyntacticTree ch : leftChildren) {
			if (ch.contain(dt))
				return true;
		}
		for (SyntacticTree ch : rightChildren) {
			if (ch.contain(dt))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<List<String>> toList() {
		ArrayList<List<String>> lists = new ArrayList<List<String>>(size);
		for (int i = 0; i < size; i++) {
			lists.add(null);
		}
		toList(lists);
		return lists;
	}

	private void toList(ArrayList<List<String>> lists) {
		ArrayList<String> e = new ArrayList<String>();
		e.add(seg);
		e.add(pos);
		if (parent == null) {
			e.add(String.valueOf(-1));
			e.add("Root");
		} else {
			e.add(String.valueOf(parent.id));
			e.add(dep);
		}
		lists.set(id, e);
		for (int i = 0; i < leftChildren.size(); i++) {
			leftChildren.get(i).toList(lists);
		}
		for (int i = 0; i < rightChildren.size(); i++) {
			rightChildren.get(i).toList(lists);
		}
	}

	enum TAG {
		Seg, Pos, Dep, Lex
	}

	public String[] getSEG() {
		ArrayList<String> arr = new ArrayList<String>();
		getTag(arr, TAG.Seg);
		return arr.toArray(new String[arr.size()]);
	}

	public String[] getLEX() {
		ArrayList<String> arr = new ArrayList<String>();
		getTag(arr, TAG.Lex);
		return arr.toArray(new String[arr.size()]);
	}

	public String[] getPOS() {
		ArrayList<String> arr = new ArrayList<String>();
		getTag(arr, TAG.Pos);
		return arr.toArray(new String[arr.size()]);
	}

	public String[] getDEP() {
		ArrayList<String> arr = new ArrayList<String>();
		getTag(arr, TAG.Dep);
		return arr.toArray(new String[arr.size()]);
	}

	public void setDEP(String dep[]) {
		int i = 0;
		for (SyntacticTree t : this) {
			t.dep = dep[i++];
		}
		depSet = null;
	}

	public void setPOS(String pos[]) {
		int i = 0;
		for (SyntacticTree t : this) {
			t.pos = pos[i++];
		}
	}

	public SyntacticTree[] getSyntacticTree() {
		ArrayList<SyntacticTree> arr = new ArrayList<SyntacticTree>();
		getDependencyTree(arr);
		return arr.toArray(new SyntacticTree[arr.size()]);
	}

	/**
	 * 
	 * @param arr
	 *            = infix traversal of the tree;
	 */
	public void getTag(ArrayList<String> arr, TAG tag) {
		for (SyntacticTree tree : this.leftChildren) {
			tree.getTag(arr, tag);
		}
		switch (tag) {
		case Lex:
			arr.add(lex);
			break;
		case Seg:
			arr.add(seg);
			break;
		case Pos:
			arr.add(pos);
			break;
		case Dep:
			if (dep == null) {
				arr.add("root");
			} else
				arr.add(dep);
			break;
		}

		for (SyntacticTree tree : this.rightChildren) {
			tree.getTag(arr, tag);
		}
	}

	public void getDependencyTree(ArrayList<SyntacticTree> arr) {
		for (SyntacticTree tree : this.leftChildren) {
			tree.getDependencyTree(arr);
		}
		arr.add(this);

		for (SyntacticTree tree : this.rightChildren) {
			tree.getDependencyTree(arr);
		}
	}

	void match(String relation, List<SyntacticTree> list) {
		if (this.dep != null && this.dep.equals(relation)) {
			list.add(this);
			return;
		}

		for (SyntacticTree e : this.leftChildren) {
			e.match(relation, list);
		}

		for (SyntacticTree e : this.rightChildren) {
			e.match(relation, list);
		}
	}

	public List<SyntacticTree> match(String relation) {
		List<SyntacticTree> list = new ArrayList<SyntacticTree>();
		match(relation, list);
		return list;
	}

	// if relation is null, it is a predicate;
	public Utility.LNodeShadow buildShadowTree() {
		// recursive inorder scan used to build the shadow tree
		// create the new shadow tree;
		Utility.LNodeShadow newNode = new Utility.LNodeShadow(seg);
		// tree node
		if (leftChildren != null && leftChildren.size() > 0) {
			newNode.x = new Utility.LNodeShadow[leftChildren.size()];
			int i = 0;
			for (SyntacticTree tree : leftChildren)
				newNode.x[i++] = tree.buildShadowTree();
		}
		// allocate node for left child at next level in tree;

		if (rightChildren != null && rightChildren.size() > 0) {
			newNode.y = new Utility.LNodeShadow[rightChildren.size()];
			int i = 0;
			for (SyntacticTree tree : rightChildren)
				newNode.y[i++] = tree.buildShadowTree();
		}

		return newNode;
	}

	public String toString() {
		return toString(true, null);
	}

	public String toStringSegExclusive() {
		return toString(false, null);
	}

	public String toString(boolean bSegInclusive, int mark[]) {
		String tree = null;
		SyntacticTree parent = this.parent;
		this.parent = null;
		try {
			String[] pos = getPOS();
			String[] dep = getDEP();
			String[] seg = getLEX();

			int size = seg.length;
			if (size != pos.length || size != dep.length) {
				System.out.println("size != pos.length || size != dep.length");
			}

			int max_width = -1;
			for (int i = 0; i < size; ++i) {
				max_width = Utility.max(max_width, Utility.length(seg[i]), Utility.length(dep[i]), Utility.length(pos[i]));
			}

			++max_width;

			SyntacticTree.Iterator it = iterator();

			int length_lexeme[] = new int[size];
			int left_parenthesis[] = new int[size];
			int right_parenthesis[] = new int[size];

			int index = 0;
			while (it.hasNext() && index < size) {
				left_parenthesis[index] = it.left_parenthesis();
				right_parenthesis[index] = it.right_parenthesis();

				if (!seg[index].equals(it.next().lex)) {
					System.out.println("inconsistent lexeme");
				}

				seg[index] = protectParenthesis(seg[index]);
				length_lexeme[index] = Utility.length(seg[index]);

				int length = length_lexeme[index] + left_parenthesis[index];
				if (index > 0)
					length += right_parenthesis[index - 1];

				if (max_width < length) {
					max_width = length;
				}

				++index;
			}

			String infix = "";
			for (index = 0; index < size; ++index) {
				int length = max_width - length_lexeme[index] - left_parenthesis[index];
				if (index > 0)
					length -= right_parenthesis[index - 1];

				infix += Utility.toString(length, ' ');

				infix += Utility.toString(left_parenthesis[index], '(');
				infix += seg[index];
				infix += Utility.toString(right_parenthesis[index], ')');
			}

			if (!infix.replaceAll("\\s+", "").equals(this.infixExpression())) {
				System.out.println("infixExpression error");
				System.out.println(infix.replaceAll("\\s+", ""));
				System.out.println(this.infixExpression());
			}

			String segString = "";
			String posString = "";
			String depString = "";
			String markString = null;
			String[] marks = null;
			if (mark != null) {
				marks = Utility.errorMark(seg.length, mark);
				markString = "";
			}

			seg = getLEX();
			for (int i = 0; i < pos.length; ++i) {
				if (bSegInclusive)
					segString += Utility.toString(max_width - Utility.length(seg[i]), ' ') + seg[i];
				posString += Utility.toString(max_width - Utility.length(pos[i]), ' ') + pos[i];
				depString += Utility.toString(max_width - Utility.length(dep[i]), ' ') + dep[i];
				if (markString != null)
					markString += Utility.toString(max_width - Utility.length(marks[i]), ' ') + marks[i];
			}

			Utility.LNodeShadow LNodeShadow = buildShadowTree();
			LNodeShadow.max_width = max_width;

			tree = LNodeShadow.toString();
			//			tree = ";" + tree.replace("\r\n ", "\r\n;");

			tree += infix + Utility.lineSeparator;
			if (bSegInclusive)
				tree += segString + Utility.lineSeparator;
			tree += posString + Utility.lineSeparator;
			tree += depString + Utility.lineSeparator;
			if (markString != null)
				tree += markString + Utility.lineSeparator;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.parent = parent;
		}

		return ";" + this.unadornedExpression() + Utility.lineSeparator + tree;
	}

	public String toStringNonHierarchical() {
		String tree = null;
		SyntacticTree parent = this.parent;
		this.parent = null;
		try {
			String[] pos = getPOS();
			String[] dep = getDEP();
			String[] seg = getLEX();

			int size = seg.length;
			if (size != pos.length || size != dep.length) {
				System.out.println("size != pos.length || size != dep.length");
			}

			int max_width = -1;
			for (int i = 0; i < size; ++i) {
				max_width = Utility.max(max_width, Utility.length(seg[i]), Utility.length(dep[i]), Utility.length(pos[i]));
			}

			++max_width;

			SyntacticTree.Iterator it = iterator();

			int length_lexeme[] = new int[size];
			int left_parenthesis[] = new int[size];
			int right_parenthesis[] = new int[size];

			int index = 0;
			while (it.hasNext() && index < size) {
				left_parenthesis[index] = it.left_parenthesis();
				right_parenthesis[index] = it.right_parenthesis();

				if (!seg[index].equals(it.next().lex)) {
					System.out.println("inconsistent lexeme");
				}

				seg[index] = protectParenthesis(seg[index]);
				length_lexeme[index] = Utility.length(seg[index]);

				int length = length_lexeme[index] + left_parenthesis[index];
				if (index > 0)
					length += right_parenthesis[index - 1];

				if (max_width < length) {
					max_width = length;
				}

				++index;
			}

			String infix = "";
			for (index = 0; index < size; ++index) {
				int length = max_width - length_lexeme[index] - left_parenthesis[index];
				if (index > 0)
					length -= right_parenthesis[index - 1];

				infix += Utility.toString(length, ' ');

				infix += Utility.toString(left_parenthesis[index], '(');
				infix += seg[index];
				infix += Utility.toString(right_parenthesis[index], ')');
			}

			if (!infix.replaceAll("\\s+", "").equals(this.infixExpression())) {
				System.out.println("infixExpression error");
				System.out.println(infix.replaceAll("\\s+", ""));
				System.out.println(this.infixExpression());
			}

			String posString = "";
			String depString = "";
			for (int i = 0; i < pos.length; ++i) {
				posString += Utility.toString(max_width - Utility.length(pos[i]), ' ') + pos[i];
				depString += Utility.toString(max_width - Utility.length(dep[i]), ' ') + dep[i];
			}

			tree = infix + Utility.lineSeparator;

			tree += posString + Utility.lineSeparator;
			tree += depString + Utility.lineSeparator;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.parent = parent;
		}

		return tree;
	}

	public static String protectParenthesis(String word) {
		switch (word) {
		case "+":
			return "＋";
		case "(":
			return "（";
		case ")":
			return "）";
		case "/":
			return "／";
		case "\\":
			return "＼";
		default:
			return word;
		}
	}

	public String infixExpression() {
		String infix = "";
		for (SyntacticTree tree : this.leftChildren) {
			infix += "(" + tree.infixExpression() + ")";
		}
		infix += protectParenthesis(lex);
		for (SyntacticTree tree : this.rightChildren) {
			infix += "(" + tree.infixExpression() + ")";
		}
		return infix;
	}

	public String infixPOSExpression() {
		String infix = "";
		for (SyntacticTree tree : this.leftChildren) {
			infix += "(" + tree.infixPOSExpression() + ")";
		}
		infix += protectParenthesis(lex) + "/" + pos + "/" + dep;
		for (SyntacticTree tree : this.rightChildren) {
			infix += "(" + tree.infixPOSExpression() + ")";
		}
		return infix;
	}

	public String unadornedExpression() {
		String infix = "";
		for (SyntacticTree tree : this.leftChildren) {
			infix += tree.unadornedExpression();
		}
		infix += lex;
		for (SyntacticTree tree : this.rightChildren) {
			infix += tree.unadornedExpression();
		}
		return infix;
	}

	public String simplifiedExpression() {
		String infix = "";
		for (SyntacticTree tree : this.leftChildren) {
			infix += tree.simplifiedExpression();
		}
		infix += seg;
		for (SyntacticTree tree : this.rightChildren) {
			infix += tree.simplifiedExpression();
		}
		return infix;
	}

	public void questionConstituent(HashSet<SyntacticTree> treeSet) {
		if (this.pos.equals("QUE")) {
			treeSet.add(this);
		}
		for (SyntacticTree tree : this.leftChildren) {
			tree.questionConstituent(treeSet);
		}
		for (SyntacticTree tree : this.rightChildren) {
			tree.questionConstituent(treeSet);
		}
	}

	/**
	 * extract the interrogatives of the sentence.
	 * 
	 * @return
	 */
	public HashSet<SyntacticTree> questionConstituent() {
		HashSet<SyntacticTree> treeSet = new HashSet<SyntacticTree>();
		questionConstituent(treeSet);
		return treeSet;
	}

	public void negConstituent(HashSet<SyntacticTree> treeSet) {
		if (this.pos.equals("NEG")) {
			treeSet.add(this);
		}
		for (SyntacticTree tree : this.leftChildren) {
			tree.negConstituent(treeSet);
		}
		for (SyntacticTree tree : this.rightChildren) {
			tree.negConstituent(treeSet);
		}
	}

	public HashSet<SyntacticTree> negConstituent() {
		HashSet<SyntacticTree> treeSet = new HashSet<SyntacticTree>();
		negConstituent(treeSet);
		return treeSet;
	}

	public String mathInfixExp() throws Exception {
		String word = this.seg;
		switch (pos) {
		case "CD":
			if (Character.isDigit(word.charAt(0)))
				return word;
			else
				return ((Long) Utility.parseDigitFromChinese(unadornedExpression())).toString();
		case "O":
			switch (word) {
			case "加":
			case "加上":
				word = "+";
				break;
			case "减":
			case "减去":
				word = "-";
				break;
			case "乘":
			case "×":
			case "乘以":
			case "乘上":
				word = "*";
				break;
			case "除":
			case "除以":
				word = "/";
				break;
			default:
				break;
			}
		}

		String infix = "";
		for (SyntacticTree tree : this.leftChildren) {
			infix += tree.mathInfixExp();
		}

		infix += word;
		for (SyntacticTree tree : this.rightChildren) {
			infix += tree.mathInfixExp();
		}
		return infix;
	}

	// public String toString() {
	// StringBuffer sb = new StringBuffer();
	// for (int i = 0; i < leftChilds.size(); i++) {
	// sb.append(leftChilds.get(i).toString());
	// }
	// sb.append(id).append(" ");
	// sb.append(word);
	// sb.append(" ");
	// sb.append(pos);
	// sb.append(" ");
	// if(parent!=null)
	// sb.append(parent.id);
	// else
	// sb.append(-1);
	// sb.append(" ");
	// if(relation!=null)
	// sb.append(relation);
	// else
	// sb.append("root");
	// sb.append("\n");
	// for (int i = 0; i < rightChilds.size(); i++) {
	// sb.append(rightChilds.get(i).toString());
	// }
	// return sb.toString();
	// }

	static public SyntacticTree parse(String token[][], int INDICES[]) throws Exception {
		return parse(token[0], token[1], token[2], INDICES);
	}

	static public SyntacticTree parse(String seg[], String pos[], String dep[], int INDICES[]) throws Exception {
		SyntacticTree tree[] = new SyntacticTree[INDICES.length];

		int length = INDICES.length;
		if (seg.length != length || pos.length != length || dep.length != length) {
			log.info(Utility.toString(seg, "\t", null, seg.length));
			log.info(Utility.toString(pos, "\t", null, pos.length));
			log.info(Utility.toString(dep, "\t", null, dep.length));

			throw new Exception("lengths are not coherent!");
		}
		for (int i = 0; i < length; ++i) {
			tree[i] = new SyntacticTree(i, seg[i], pos[i], dep[i]);
		}

		int rootIndex = -1;
		for (int i = 0; i < length; ++i) {
			tree[i].size = 1;
			if (INDICES[i] >= 0) {
				SyntacticTree parent = tree[INDICES[i]];
				tree[i].parent = parent;
				if (i < INDICES[i]) {
					parent.leftChildren.add(tree[i]);
				} else if (i > INDICES[i]) {
					parent.rightChildren.add(tree[i]);
				} else {
					// throw new Exception("self loop occurred!");
				}
			} else {
				rootIndex = i;
			}
		}

		if (rootIndex < 0) {
			log.info(Utility.toString(seg, "\t", null, seg.length));
			log.info(Utility.toString(pos, "\t", null, pos.length));
			log.info(Utility.toString(dep, "\t", null, dep.length));
			throw new Exception("root of the tree is not detected!");
		}

		tree[rootIndex].validateSize();
		return tree[rootIndex];
	}

	boolean leftEmpty() {
		return leftChildren == null || leftChildren.size() == 0;
	}

	boolean rightEmpty() {
		return rightChildren == null || rightChildren.size() == 0;
	}

	public boolean isLeftChild() {
		if (parent == null || parent.leftEmpty()) {
			return false;
		}
		return parent.leftChildren.contains(this);
	}

	public boolean isRightChild() {
		if (parent == null || parent.rightEmpty()) {
			return false;
		}
		return parent.rightChildren.contains(this);
	}

	boolean isLastRightChild() {
		if (parent == null || parent.rightEmpty()) {
			return false;
		}
		return parent.parent != null && parent.rightChildren.indexOf(this) == parent.rightChildren.size() - 1;
	}

	SyntacticTree leftmost(int... hierarchy) {
		SyntacticTree tree = this;
		while (!tree.leftEmpty()) {
			tree = tree.leftChildren.get(0);
			++hierarchy[0];
		}
		return tree;
	}

	SyntacticTree leftmost() {
		SyntacticTree tree = this;
		while (!tree.leftEmpty()) {
			tree = tree.leftChildren.get(0);
		}
		return tree;
	}

	SyntacticTree rightmost() {
		SyntacticTree tree = this;
		while (!tree.rightEmpty()) {
			tree = tree.rightChildren.get(0);
		}
		return tree;
	}

	public static class Iterator implements java.util.Iterator<SyntacticTree> {
		Iterator(SyntacticTree node) {
			this.node = node;
			SyntacticTree ptr = this.node;
			while (ptr.isLeftChild()) {
				++left_parenthesis;
				ptr = ptr.parent;
			}

			if (node.rightEmpty()) {
				if (node.isRightChild() || node.isLeftChild())
					++right_parenthesis;
			}

		}

		SyntacticTree node;
		boolean drapeau = true;
		int left_parenthesis = 0;
		int right_parenthesis = 0;

		@Override
		public boolean hasNext() {
			return drapeau;
		}

		public int left_parenthesis() {
			return left_parenthesis;
		}

		public int right_parenthesis() {
			return right_parenthesis;
		}

		@Override
		public SyntacticTree next() {
			SyntacticTree res = node;
			try {
				drapeau = move_forward();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return res;
		}

		public boolean move_left() {
			return move_down(true);
		}

		public boolean move_right() {
			return move_down(false);
		}

		public boolean move_down(boolean bLeft) {
			List<SyntacticTree> children = bLeft ? node.parent.leftChildren : node.parent.rightChildren;
			int i = children.indexOf(node);
			++i;
			if (i < children.size()) {
				move_leftmost(children.get(i));
				return true;
			}
			return false;
		}

		public void move_leftmost(SyntacticTree ptr) {
			++left_parenthesis;
			int level[] = { 0 };
			node = ptr.leftmost(level);

			left_parenthesis += level[0];
			if (node.rightEmpty()) {
				++right_parenthesis;
				if (level[0] == 0) {
					SyntacticTree node = this.node;
					while (node.isLastRightChild()) {
						++right_parenthesis;
						node = node.parent;
					}
				}
			}
		}

		public void ProcessLeftChild() {
			if (move_left())
				return;

			node = node.parent;

			if (node.rightEmpty()) {
				SyntacticTree node = this.node;
				for (;;) {
					if (node.isLeftChild()) {
						++right_parenthesis;
						break;
					}

					if (node.isRightChild()) {
						++right_parenthesis;

						if (node.isLastRightChild()) {
							node = node.parent;
							continue;
						} else
							break;
					} else
						break;
				}
			}
		}

		public boolean move_forward() throws Exception {
			left_parenthesis = 0;
			right_parenthesis = 0;

			if (node.rightEmpty()) {
				if (node.isLeftChild()) {
					ProcessLeftChild();
					return true;
				}

				if (node.isRightChild()) {
					for (;;) {
						if (move_right())
							return true;

						node = node.parent;
						if (node.isLeftChild()) {
							ProcessLeftChild();
							return true;
						}
						if (node.isRightChild()) {
							continue;
						}

						return false;
					}
				}

				return false;
			}

			move_leftmost(node.rightChildren.get(0));
			return true;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public Iterator iterator() {
		// TODO Auto-generated method stub
		return new Iterator(this.leftmost());
	}

	static public List<SyntacticTree> clone(List<SyntacticTree> child) {
		List<SyntacticTree> arr = new ArrayList<SyntacticTree>();
		for (SyntacticTree tree : child) {
			arr.add(tree.clone());
		}
		return arr;
	}

	@Override
	public SyntacticTree clone() {
		SyntacticTree tree = new SyntacticTree(id, lex, pos, dep, clone(leftChildren), clone(rightChildren));
		tree.size = this.size;
		return tree;
	}

	public SyntacticTree clone(String dep) {
		SyntacticTree tree = new SyntacticTree(id, lex, pos, dep, clone(leftChildren), clone(rightChildren));
		tree.size = this.size;
		return tree;
	}

	public SyntacticTree searchNounPhrase() {
		if (pos.equals("NN")) {
			return this;
		}
		for (SyntacticTree tree : leftChildren) {
			SyntacticTree noun = tree.searchNounPhrase();
			if (noun != null)
				return noun;
		}
		for (SyntacticTree tree : rightChildren) {
			SyntacticTree noun = tree.searchNounPhrase();
			if (noun != null)
				return noun;
		}
		return null;
	}

	public SyntacticTree getSubject() {
		for (int i = 0; i < this.leftChildren.size(); ++i) {
			if (this.leftChildren.get(i).dep.equals("SUJ"))
				return this.leftChildren.get(i);
		}
		return null;
	}

	public SyntacticTree getObject() {
		for (int i = 0; i < this.rightChildren.size(); ++i) {
			if (this.rightChildren.get(i).dep.equals("OBJ"))
				return this.rightChildren.get(i);
		}
		return null;
	}

	public SyntacticTree getParticular(int id) {
		if (id > this.id)
			return getParticular(this.rightChildren, id);
		else if (id < this.id)
			return getParticular(this.leftChildren, id);
		else
			return this;
	}

	static public SyntacticTree getParticular(List<SyntacticTree> children, int id) {
		if (children.size() == 0) {
			return null;
		}
		int i;
		for (i = 0; i < children.size(); ++i) {
			if (id <= children.get(i).id) {
				SyntacticTree ret = children.get(i).getParticular(id);
				if (ret == null) {
					if (i > 0)
						return children.get(i - 1).getParticular(id);
					else
						return null;
				}
			}
		}
		return children.get(i - 1).getParticular(id);
	}

	public void removeClassScope(String classScope) {
		if (pos.equals("DE")) {
			if (this.leftChildren.get(0).unadornedExpression().equals(classScope)) {
				this.leftChildren.remove(0);
				return;
			}
		}

		for (SyntacticTree tree : this.leftChildren) {
			tree.removeClassScope(classScope);
		}
		for (SyntacticTree tree : this.rightChildren) {
			tree.removeClassScope(classScope);
		}
	}

	public void punctuated(String... punct) {
		for (String pu : punct) {
			rightChildren.add(new SyntacticTree(size, pu, "PU", "pu", this));
			++size;
		}
	}

	public void removePunctuationMark() {
		while (rightChildren.size() > 0 && "pu".equals(Utility.last(rightChildren).dep)) {
			rightChildren.remove(rightChildren.size() - 1);
			--this.size;
		}
	}

	public SyntacticTree removeRight(int j) {
		SyntacticTree kinder = rightChildren.remove(j);
		this.size -= kinder.size;
		this.validateIndex();
		return kinder;
	}

	public SyntacticTree removeLeft(int j) {
		SyntacticTree kinder = leftChildren.remove(j);
		this.size -= kinder.size;
		this.validateIndex();
		return kinder;
	}

	static public SyntacticTree toSyntacticTree(DependencyTree tree, List<CoreLabel> token) {
		int size = token.size();
		int rootID = tree.getRoot() - 1;
		SyntacticTree arr[] = new SyntacticTree[size];
		for (int i = 0; i < size; ++i) {
			arr[i] = new SyntacticTree(i, token.get(i).originalText(), token.get(i).word(), token.get(i).tag(), tree.getLabel(i + 1));
		}

		for (int i = 0; i < size; ++i) {
			int parentID = tree.getHead(i + 1);
			--parentID;
			if (parentID < 0)
				continue;
			arr[i].parent = arr[parentID];

			if (parentID > i) {
				arr[parentID].leftChildren.add(arr[i]);
			} else if (parentID < i) {
				arr[parentID].rightChildren.add(arr[i]);
			} else {
				throw new RuntimeException("parentID == i");
			}
		}
		arr[rootID].validateSize();
		return arr[rootID];
	}

	public boolean equals(SyntacticTree obj) {
		return Utility.equals(toHeadsArray(), obj.toHeadsArray()) && Utility.equals(this.getDEP(), obj.getDEP());
	}

	public void validateBranches() {
		for (SyntacticTree tree : leftChildren) {
			tree.parent = this;
		}
		for (SyntacticTree tree : rightChildren) {
			tree.parent = this;
		}
	}

	public SyntacticTree validateIndex() {
		int index = 0;
		for (SyntacticTree tree : this) {
			tree.id = index++;
		}
		if (index != this.size) {
			log.info("size = " + size);
			log.info("index = " + index);
			throw new RuntimeException("index != this.size");
		}
		return this;
	}

	public void preppend(String seg, String pos, String dep) {
		this.increaseIndex();
		leftChildren.add(0, new SyntacticTree(0, seg, pos, dep, this));
		++size;
	}

	void increaseIndex() {
		++id;
		for (SyntacticTree tree : leftChildren) {
			tree.increaseIndex();
		}
		for (SyntacticTree tree : rightChildren) {
			tree.increaseIndex();
		}
	}

	public void append(String seg, String pos, String dep) {
		rightChildren.add(new SyntacticTree(size, seg, pos, dep, this));
		++size;
	}

	/**
	 * used to create a binary tree version of the dependency tree
	 * 
	 * @author Cosmos
	 *
	 */

	public static HashMap<String, Integer> precedencyMap;

	static {
		precedencyMap = new HashMap<String, Integer>();

		String arrDep[] = { "cc", "adj", "as", "ij", "pu", "adv", "va", "sup", "obj", "suj", "o", "p", "de", "nt", "cs", "root", };

		for (int i = 0; i < arrDep.length; ++i) {
			precedencyMap.put(arrDep[i], i);
		}
	}

	public static class BinarizedTree {
		//		public static class Precedence {
		//			Precedence(int inputPrecedence, int stackPrecedence) {
		//				this.inputPrecedence = inputPrecedence;
		//				this.stackPrecedence = stackPrecedence;
		//
		//			}
		//
		//			int inputPrecedence;
		//			int stackPrecedence;
		//		}
		//

		SyntacticTree tree;
		BinarizedTree left, right;
		public int classtype = -1;
		String label;

		public LabeledScoredTreeNode toLabeledScoredTreeNode() {
			CoreLabel coreLabel = CoreLabel.wordFromString(label);
			coreLabel.set(RNNCoreAnnotations.GoldClass.class, classtype);
			if (isLeaf()) {
				return new LabeledScoredTreeNode(coreLabel);
			}
			List<Tree> daughterTreesList = new ArrayList<Tree>();
			if (this.left != null) {
				daughterTreesList.add(left.toLabeledScoredTreeNode());
			}
			if (this.right != null) {
				daughterTreesList.add(right.toLabeledScoredTreeNode());
			}
			return new LabeledScoredTreeNode(coreLabel, daughterTreesList);
		}

		public static interface Combination {
			public String combine(String seg, String pos);
		}

		public static HashMap<String, Integer> map = new HashMap<String, Integer>();
		public static boolean bErrorDiscrepancy = false;

		void setSyntacticTree(SyntacticTree tree) throws Exception {
			this.tree = tree;

			String infixExpression = this.labelInfixExpression();
			Integer value = map.get(infixExpression);
			if (value == null) {
				map.put(infixExpression, classtype);
			} else {
				if (classtype != value) {
					log.info(infixExpression + " is assigned " + value);
					log.info(infixExpression + " is assigned " + classtype);
					log.info(tree);
					bErrorDiscrepancy = true;
				}
			}
		}

		public BinarizedTree(String label, BinarizedTree left, BinarizedTree right) {
			this.label = label;
			this.left = left;
			this.right = right;
		}

		public BinarizedTree(String label, BinarizedTree right) {
			this.label = label;
			this.right = right;
		}

		public BinarizedTree(String label) {
			this.label = label;
		}

		int maxWordLength() {
			int max = Utility.length(this.label.toString());

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

		//added by Cosmos
		@Override
		public String toString() {
			String tree = null;
			try {
				int max_width = this.maxWordLength();
				++max_width;
				Utility.LNodeShadow LNodeShadow = buildShadowTree();
				LNodeShadow.max_width = max_width;

				tree = LNodeShadow.toString();
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
			Utility.LNodeShadow newNode = new Utility.LNodeShadow(this.label.toString());
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

		boolean isLeaf() {
			return left == null && right == null;
		}

		public String infixExpression() {
			if (isLeaf()) {
				return label.toString();
			}
			String infix = "";
			infix += this.classtype;
			if (left != null)
				infix += "(" + left.infixExpression() + ")";
			if (right != null)
				infix += "(" + right.infixExpression() + ")";

			return infix;
		}

		public String labelInfixExpression() {
			if (isLeaf()) {
				return label.toString();
			}
			String infix = "-1";
			//			infix += label.toString();
			if (left != null)
				infix += "(" + left.labelInfixExpression() + ")";
			if (right != null)
				infix += "(" + right.labelInfixExpression() + ")";

			return infix;
		}
	}

	// Precedence less, ie, left precedence < right precedence
	public static boolean lessPrecedence(String depLeft, String depRight) {
		return precedencyMap.get(depLeft) < precedencyMap.get(depRight);
	}

	public static boolean lessPrecedence(SyntacticTree depLeft, SyntacticTree depRight) {
		return lessPrecedence(depLeft.dep, depRight.dep);
	}

	public BinarizedTree toBinarizedTree(Combination combination) throws Exception {
		BinarizedTree leftChildrenBinarizedTree[] = new BinarizedTree[leftChildren.size()];
		BinarizedTree rightChildrenBinarizedTree[] = new BinarizedTree[rightChildren.size()];

		for (int i = 0; i < leftChildren.size(); ++i) {
			leftChildrenBinarizedTree[i] = leftChildren.get(i).toBinarizedTree(combination);
		}
		for (int i = 0; i < rightChildren.size(); ++i) {
			rightChildrenBinarizedTree[i] = rightChildren.get(i).toBinarizedTree(combination);
		}

		BinarizedTree bTree = new BinarizedTree(pos, new BinarizedTree(combination.combine(lex, pos)));
		int i = leftChildren.size() - 1;
		int j = 0;
		boolean bRight;
		for (;;) {
			if (i < 0) {
				if (j >= rightChildren.size()) {
					break;
				}
				bRight = true;
			} else {
				if (j >= rightChildren.size()) {
					bRight = false;
				} else {
					if (lessPrecedence(leftChildren.get(i), rightChildren.get(j))) {
						bRight = false;
					} else {
						bRight = true;
					}
				}
			}

			if (bRight) {
				bTree = new BinarizedTree(pos, bTree, rightChildrenBinarizedTree[j]);
				++j;
			} else {
				bTree = new BinarizedTree(pos, leftChildrenBinarizedTree[i], bTree);
				--i;
			}
		}

		return bTree;
	}

	public Constituent toConstituentTree() throws Exception {
		if (this.depSet().isEmpty()) {
			return new ConstituentLeaf(lex, Coefficient.valueOf(pos));
		}

		SplitStruct splitStruct = this.splitStruct(this.depSet.first());

		return new ConstituentTree(Coefficient.valueOf(splitStruct.dep), splitStruct.kinder.toConstituentTree(), splitStruct.parent.toConstituentTree()).declareSubject();
	}

	public BinarizedTree toBinarizedTreeWithGoldLabel(Combination combination) throws Exception {
		BinarizedTree leftChildrenBinarizedTree[] = new BinarizedTree[leftChildren.size()];
		BinarizedTree rightChildrenBinarizedTree[] = new BinarizedTree[rightChildren.size()];

		for (int i = 0; i < leftChildren.size(); ++i) {
			leftChildrenBinarizedTree[i] = leftChildren.get(i).toBinarizedTreeWithGoldLabel(combination);
		}
		for (int i = 0; i < rightChildren.size(); ++i) {
			rightChildrenBinarizedTree[i] = rightChildren.get(i).toBinarizedTreeWithGoldLabel(combination);
		}

		BinarizedTree bTree = new BinarizedTree(pos, new BinarizedTree(combination.combine(this.lex, this.pos)));
		bTree.setSyntacticTree(new SyntacticTree(0, lex, pos, dep));
		int i = leftChildren.size() - 1;
		int j = 0;
		boolean bRight;
		for (;;) {
			if (i < 0) {
				if (j >= rightChildren.size()) {
					break;
				}
				bRight = true;
			} else {
				if (j >= rightChildren.size()) {
					bRight = false;
				} else {
					if (lessPrecedence(leftChildren.get(i), rightChildren.get(j))) {
						bRight = false;
					} else {
						bRight = true;
					}
				}
			}

			if (bRight) {
				if (bTree.tree.validateSize() != bTree.tree.size) {
					throw new RuntimeException("bTree.tree.validateSize() != bTree.tree.size");
				}
				bTree = new BinarizedTree(pos, bTree, rightChildrenBinarizedTree[j]);
				if (bTree.left.tree.validateSize() != bTree.left.tree.size) {
					throw new RuntimeException("bTree.tree.validateSize() != bTree.tree.size");
				}

				bTree.setSyntacticTree(bTree.left.tree.clone().addRightChild(rightChildrenBinarizedTree[j].tree));
				if (bTree.tree.validateSize() != bTree.tree.size) {
					throw new RuntimeException("bTree.tree.validateSize() != bTree.tree.size");
				}
				++j;
			} else {
				if (bTree.tree.validateSize() != bTree.tree.size) {
					throw new RuntimeException("bTree.tree.validateSize() != bTree.tree.size");
				}
				bTree = new BinarizedTree(pos, leftChildrenBinarizedTree[i], bTree);

				if (bTree.right.tree.validateSize() != bTree.right.tree.size) {
					throw new RuntimeException("bTree.tree.validateSize() != bTree.tree.size");
				}

				bTree.setSyntacticTree(bTree.right.tree.clone().addLeftChild(leftChildrenBinarizedTree[i].tree));
				--i;
				if (bTree.tree.validateSize() != bTree.tree.size) {
					throw new RuntimeException("bTree.tree.validateSize() != bTree.tree.size");
				}
			}
		}

		if (!bTree.tree.clone().validateIndex().equals(this.clone().validateIndex())) {
			bTree.tree.clone().validateIndex().equals(this.clone().validateIndex());
			log.info(bTree.tree);
			log.info(this);
			throw new RuntimeException("!bTree.tree.equals(this)");
		}

		return bTree;
	}

	public static class SplitStruct {
		public SplitStruct(String dep, SyntacticTree kinder, SyntacticTree parent) {
			this.dep = dep;
			this.kinder = kinder;
			this.parent = parent;
		}

		public String dep;
		public SyntacticTree kinder, parent;

		@Override
		public String toString() {
			String str = dep;
			str += "\n";
			str += kinder.toString();
			str += "\n";
			str += parent.toString();
			return str;
		}
	}

	public TreeSet<String> depSet;

	static Comparator<String> precedencyComparator = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return -Integer.compare(precedencyMap.get(o1), precedencyMap.get(o2));
		}

	};

	public TreeSet<String> depSet() {
		if (depSet == null) {
			depSet = new TreeSet<String>(precedencyComparator);
			for (SyntacticTree tree : rightChildren) {
				depSet.add(tree.dep);
			}

			for (SyntacticTree tree : leftChildren) {
				depSet.add(tree.dep);
			}
		}

		return depSet;
	}

	SplitStruct splitStruct;

	public SplitStruct splitStruct() throws Exception {
		if (splitStruct == null) {
			boolean bRight;
			int i = 0;
			int j = rightChildren.size() - 1;
			if (leftChildren.isEmpty()) {
				if (rightChildren.isEmpty()) {
					return null;
				}
				bRight = false;
			} else {
				if (rightChildren.isEmpty()) {
					bRight = true;
				} else {
					if (lessPrecedence(leftChildren.get(i), rightChildren.get(j))) {
						bRight = false;
					} else {
						bRight = true;
					}

				}
			}

			SyntacticTree parent = this.clone();
			SyntacticTree kinder;
			if (bRight) {
				kinder = parent.removeLeft(i);
			} else {
				kinder = parent.removeRight(j);
			}
			splitStruct = new SplitStruct(kinder.dep, kinder, parent);
		}

		return splitStruct;
	}

	public SplitStruct splitStruct(String dep) throws Exception {
		int i = 0;
		int j = rightChildren.size() - 1;
		boolean bRight;

		for (; i < leftChildren.size(); ++i) {
			if (leftChildren.get(i).dep.equals(dep)) {
				break;
			}
		}

		if (i < leftChildren.size()) {
			bRight = false;
		} else {
			for (; j >= 0; --j) {
				if (rightChildren.get(j).dep.equals(dep)) {
					break;
				}

			}

			if (j >= 0) {
				bRight = true;
			} else {
				return null;
			}
		}

		SyntacticTree parent = this.clone();
		SyntacticTree kinder;
		if (bRight) {
			kinder = parent.removeRight(j);
		} else {
			kinder = parent.removeLeft(i);
		}

		return new SplitStruct(kinder.dep, kinder, parent);
	}

	void transformLabel(String oldLabel, String newLabel) {
		for (SyntacticTree tree : this) {
			if (tree.dep.equals(oldLabel)) {
				tree.dep = newLabel;
			}
		}
	}

	//	double similarityDegree(SyntacticTree autre) {
	//		
	//		return id;
	//	}
	//
	//	double similarityDegree() {
	//		
	//		return id;
	//	}
	//
	//	SyntacticTree disolve(SyntacticTree autre) {
	//
	//		return autre;
	//	}
	//

	public static void main(String[] args) throws Exception {
	}

	public SyntacticTree adjust() throws Exception {
		if (!containsIrregulation())
			return this;

		SyntacticTree tree = SyntacticParser.instance.parseWithAdjustment(getLEX(), getPOS());
		if (tree != null) {
			return tree;
		}
		
		return this;
	}

	public String simplifiedString() {
		return Utility.removeEndOfSentencePunctuation(simplifiedExpression());
	}

	private static Logger log = Logger.getLogger(SyntacticTree.class);
}
