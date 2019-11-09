
package com.robot.syntax;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.nndep.Config;
import edu.stanford.nlp.parser.nndep.DependencyTree;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.util.Utility;

/**
 * Describe the current configuration of a parser (i.e., parser state).
 *
 * This class uses an indexing scheme where an index of zero refers to the ROOT
 * node and actual word indices begin at one.
 *
 * @author Danqi Chen
 */
public class Configuration {
	final List<SyntacticTree> stack;
	final List<SyntacticTree> buffer;

	public Configuration(Configuration config) {
		stack = new ArrayList<>(config.stack);
		buffer = new ArrayList<>(config.buffer);
	}

	public Configuration(CoreMap sentence) {
		this.stack = new ArrayList<>();
		this.buffer = new ArrayList<>();
	}

	public boolean shift() {
		SyntacticTree k = getBuffer(0);
		if (k == null)
			return false;

		buffer.remove(0);
		stack.add(k);
		return true;
	}

	public boolean shiftWithAdjustment() {
		SyntacticTree k = getBuffer(0);
		if (k == null)
			return false;
		
		SyntacticParser.listener.push(new Utility.ICommand() {
			@Override
			public boolean run() {
				buffer.add(0, bufferFirst);
				stack.remove(stack.size() - 1);
				assert Configuration.this.toString().equals(status);
				return true;
			}

			List<SyntacticTree> stack = Configuration.this.stack;
			List<SyntacticTree> buffer = Configuration.this.buffer;
			SyntacticTree bufferFirst = buffer.get(0);
			String status = Configuration.this.toString();
		});

		buffer.remove(0);
		stack.add(k);
		return true;
	}

	public boolean removeSecondTopStack() {
		int nStack = getStackSize();
		if (nStack < 2)
			return false;
		stack.remove(nStack - 2);
		return true;
	}

	public boolean removeSecondTopStackWithAdjustment() {
		int nStack = getStackSize();
		if (nStack < 2)
			return false;

		SyntacticParser.listener.push(new Utility.ICommand() {
			@Override
			public boolean run() {
				stack.add(index, secondTop);
				assert Configuration.this.toString().equals(status);
				return true;
			}

			int index = nStack - 2;
			List<SyntacticTree> stack = Configuration.this.stack;
			SyntacticTree secondTop = stack.get(index);
			String status = Configuration.this.toString();
		});

		stack.remove(nStack - 2);

		return true;
	}

	public boolean removeTopStack() {
		int nStack = getStackSize();
		if (nStack < 1)
			return false;

		stack.remove(nStack - 1);
		return true;
	}

	public boolean removeTopStackWithAdjustment() {
		int nStack = getStackSize();
		if (nStack < 1)
			return false;

		SyntacticParser.listener.push(new Utility.ICommand() {
			@Override
			public boolean run() {
				stack.add(index, secondTop);
				assert Configuration.this.toString().equals(status);
				return true;
			}

			int index = nStack - 1;
			List<SyntacticTree> stack = Configuration.this.stack;
			SyntacticTree secondTop = stack.get(index);
			String status = Configuration.this.toString();
		});

		stack.remove(nStack - 1);
		return true;
	}

	public int getStackSize() {
		return stack.size();
	}

	public int getBufferSize() {
		return buffer.size();
	}

	/**
	 * @param k
	 *            Word index (zero = root node; actual word indexing begins at
	 *            1)
	 */
	public SyntacticTree getHead(SyntacticTree k) {
		if (k == null)
			return null;
		return k.parent;
	}

	/**
	 * @param k
	 *            Word index (zero = root node; actual word indexing begins at
	 *            1)
	 */
	public String getLabel(SyntacticTree k) {
		if (k == null)
			return Config.NULL;
		return k.dep;
	}

	/**
	 * Get the sentence index of the kth word on the stack.
	 *
	 * @return Sentence index or {@link Config#NONEXIST} if stack doesn't have
	 *         an element at this index
	 */
	public SyntacticTree getStack(int k) {
		int nStack = getStackSize();
		return (k >= 0 && k < nStack) ? stack.get(nStack - 1 - k) : null;
	}

	/**
	 * Get the sentence index of the kth word on the buffer.
	 *
	 * @return Sentence index or {@link Config#NONEXIST} if stack doesn't have
	 *         an element at this index
	 */
	public SyntacticTree getBuffer(int k) {
		return (k >= 0 && k < getBufferSize()) ? buffer.get(k) : null;
	}

	public String getWord(SyntacticTree k) {
		if (k == null)
			return Config.NULL;
		if (k.id < 0)
			return Config.ROOT;

		return k.seg;
	}

	public String getPOS(SyntacticTree k) {
		if (k == null)
			return Config.NULL;
		if (k.id < 0)
			return Config.ROOT;

		return k.pos;
	}

	/**
	 * @param h
	 *            Word index of governor (zero = root node; actual word indexing
	 *            begins at 1)
	 * @param t
	 *            Word index of dependent (zero = root node; actual word
	 *            indexing begins at 1)
	 * @param l
	 *            Arc label
	 * 
	 * @param h
	 *            = the head of the dependency relation
	 * @param t
	 *            = the tail of the dependency relation
	 * @param l
	 *            = the label of the dependency relation
	 */
	public void addArc(SyntacticTree h, SyntacticTree t, String l) {
		t.parent = h;
		if (h != null) {
			if (h.id > t.id) {
				//t is the left child;
				if (h.leftChildren.size() > 0 && t.id >= h.leftChildren.get(0).id)
					throw new RuntimeException("h.leftChildren.size() >0 && t.id >= h.leftChildren.get(0).id");
				h.leftChildren.add(0, t);
			} else {
				if (h.rightChildren.size() > 0 && t.id <= Utility.last(h.rightChildren).id)
					throw new RuntimeException("h.rightChildren.size() >0 && t.id <= Utility.last(h.rightChildren).id");
				h.rightChildren.add(t);
			}
			h.size += t.size();
		}
		t.dep = l;
	}

	static public void removeArc(SyntacticTree h, SyntacticTree t) {
		t.parent = null;
		t.dep = null;

		if (h.id > t.id) {
			//t is the left child;
			h.leftChildren.remove(0);
		} else {
			h.rightChildren.remove(h.rightChildren.size() - 1);
		}
		h.size -= t.size();
	}

	public boolean addArcWithAdjustment(SyntacticTree h, SyntacticTree t, String l) throws Exception {
		t.parent = h;
		t.dep = l;

		if (h != null) {
			if (h.id > t.id) {
				//t is the left child;
				h.leftChildren.add(0, t);
			} else {
				h.rightChildren.add(t);
			}
			h.size += t.size();

			if (h.id >= 0 && h.containsIrregulation()) {
				removeArc(h, t);
				return false;
			}
		}

		SyntacticParser.listener.push(new Utility.ICommand() {
			@Override
			public boolean run() {
				assert Configuration.this.toString().equals(status);
				
				removeArc(head, tail);				
				return true;
			}

			SyntacticTree head = h;
			SyntacticTree tail = t;
			String status = Configuration.this.toString();
		});

		return true;
	}

	public SyntacticTree getLeftChild(SyntacticTree k, int cnt) {
		if (k == null || cnt > k.leftChildren.size())
			return null;
		return k.leftChildren.get(cnt - 1);
	}

	public SyntacticTree getLeftChild(SyntacticTree k) {
		return getLeftChild(k, 1);
	}

	public SyntacticTree getRightChild(SyntacticTree k, int cnt) {
		if (k == null || cnt > k.rightChildren.size())
			return null;

		return k.rightChildren.get(k.rightChildren.size() - cnt);
	}

	public SyntacticTree getRightChild(SyntacticTree k) {
		return getRightChild(k, 1);
	}

	public int getLeftValency(SyntacticTree k) {
		if (k == null)
			return Config.NONEXIST;
		return k.leftChildren.size();
	}

	public int getRightValency(SyntacticTree k) {
		if (k == null)
			return Config.NONEXIST;
		return k.rightChildren.size();
	}

	public HashSet<String> getLeftLabelSet(SyntacticTree k) {
		if (k == null)
			return null;

		HashSet<String> labelSet = new HashSet<>();
		for (SyntacticTree t : k.leftChildren) {
			labelSet.add(t.dep);
		}

		return labelSet;
	}

	public HashSet<String> getRightLabelSet(SyntacticTree k) {
		if (k == null)
			return null;

		HashSet<String> labelSet = new HashSet<>();
		for (SyntacticTree t : k.rightChildren) {
			labelSet.add(t.dep);
		}

		return labelSet;
	}

	//returns a string that concatenates all elements on the stack and buffer, and head / label.
	public String toString() {
		String s = "";
		s += "stack status:\n";
		for (int i = 0; i < getStackSize(); ++i) {
			if (stack.get(i).id < 0) {
				s += "root,\t";
			} else
				s += stack.get(i).infixPOSExpression() + ",\t";
		}
		s += "\nbuffer status:\n";
		for (int i = 0; i < getBufferSize(); ++i) {
			if (buffer.get(i).id < 0) {
				s += "root,\t";
			} else
				s += buffer.get(i).infixPOSExpression() + ",\t";
		}
		return s;
	}
}