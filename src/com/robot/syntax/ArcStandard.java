package com.robot.syntax;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.nndep.Config;
import edu.stanford.nlp.parser.nndep.DependencyTree;
import edu.stanford.nlp.parser.nndep.ParsingSystem;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.CollectionUtils;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.util.Utility;
import com.util.Utility.Listener;

/**
 * Defines an arc-standard transition-based dependency parsing system (Nivre,
 * 2004).
 *
 * @author Danqi Chen
 */
public class ArcStandard {
	private boolean singleRoot = true;
	/**
	 * Defines language-specific settings for this parsing instance.
	 */
	private final TreebankLanguagePack tlp;

	/**
	 * Dependency label used between root of sentence and ROOT node
	 */
	protected String rootLabel;

	public List<String> labels, transitions;

	/**
	 * Return the number of transitions.
	 */
	public int numTransitions() {
		return transitions.size();
	}
	// TODO pass labels as Map<String, GrammaticalRelation>; use
	// GrammaticalRelation throughout

	public int getTransitionID(String s) {
		int numTrans = numTransitions();
		for (int k = 0; k < numTrans; ++k)
			if (transitions.get(k).equals(s))
				return k;
		return -1;
	}

	private Set<String> getPunctuationTags() {
		if (tlp instanceof PennTreebankLanguagePack) {
			// Hack for English: match punctuation tags used in Danqi's paper
			return new HashSet<>(Arrays.asList("''", ",", ".", ":", "``", "-LRB-", "-RRB-"));
		} else {
			return CollectionUtils.asSet(tlp.punctuationTags());
		}
	}

	/**
	 * Evaluate performance on a list of sentences, predicted parses, and gold
	 * parses.
	 *
	 * @return A map from metric name to metric value
	 */
	public Map<String, Double> evaluate(List<CoreMap> sentences, List<DependencyTree> trees, List<DependencyTree> goldTrees) {
		Map<String, Double> result = new HashMap<>();

		// We'll skip words which are punctuation. Retrieve tags indicating
		// punctuation in this treebank.
		Set<String> punctuationTags = getPunctuationTags();

		if (trees.size() != goldTrees.size()) {
			log.error("Incorrect number of trees.");
			return null;
		}

		int correctArcs = 0;
		int correctArcsNoPunc = 0;
		int correctHeads = 0;
		int correctHeadsNoPunc = 0;

		int correctTrees = 0;
		int correctTreesNoPunc = 0;
		int correctRoot = 0;

		int sumArcs = 0;
		int sumArcsNoPunc = 0;

		for (int i = 0; i < trees.size(); ++i) {
			List<CoreLabel> tokens = sentences.get(i).get(CoreAnnotations.TokensAnnotation.class);

			if (trees.get(i).n != goldTrees.get(i).n) {
				log.error("Tree " + (i + 1) + ": incorrect number of nodes.");
				return null;
			}
			if (!trees.get(i).isTree()) {
				log.error("Tree " + (i + 1) + ": illegal.");
				return null;
			}

			int nCorrectHead = 0;
			int nCorrectHeadNoPunc = 0;
			int nNoPunc = 0;

			for (int j = 1; j <= trees.get(i).n; ++j) {
				if (trees.get(i).getHead(j) == goldTrees.get(i).getHead(j)) {
					++correctHeads;
					++nCorrectHead;
					if (trees.get(i).getLabel(j).equals(goldTrees.get(i).getLabel(j)))
						++correctArcs;
				}
				++sumArcs;

				String tag = tokens.get(j - 1).tag();
				if (!punctuationTags.contains(tag)) {
					++sumArcsNoPunc;
					++nNoPunc;
					if (trees.get(i).getHead(j) == goldTrees.get(i).getHead(j)) {
						++correctHeadsNoPunc;
						++nCorrectHeadNoPunc;
						if (trees.get(i).getLabel(j).equals(goldTrees.get(i).getLabel(j)))
							++correctArcsNoPunc;
					}
				}
			}
			if (nCorrectHead == trees.get(i).n)
				++correctTrees;
			if (nCorrectHeadNoPunc == nNoPunc)
				++correctTreesNoPunc;
			if (trees.get(i).getRoot() == goldTrees.get(i).getRoot())
				++correctRoot;
		}

		result.put("UAS", correctHeads * 100.0 / sumArcs);
		result.put("UASnoPunc", correctHeadsNoPunc * 100.0 / sumArcsNoPunc);
		result.put("LAS", correctArcs * 100.0 / sumArcs);
		result.put("LASnoPunc", correctArcsNoPunc * 100.0 / sumArcsNoPunc);

		result.put("UEM", correctTrees * 100.0 / trees.size());
		result.put("UEMnoPunc", correctTreesNoPunc * 100.0 / trees.size());
		result.put("ROOT", correctRoot * 100.0 / trees.size());

		return result;
	}

	public double getUAS(List<CoreMap> sentences, List<DependencyTree> trees, List<DependencyTree> goldTrees) {
		Map<String, Double> result = evaluate(sentences, trees, goldTrees);
		return result == null || !result.containsKey("UAS") ? -1.0 : result.get("UAS");
	}

	public double getUASnoPunc(List<CoreMap> sentences, List<DependencyTree> trees, List<DependencyTree> goldTrees) {
		Map<String, Double> result = evaluate(sentences, trees, goldTrees);
		return result == null || !result.containsKey("UASnoPunc") ? -1.0 : result.get("UASnoPunc");
	}

	public ArcStandard(TreebankLanguagePack tlp, List<String> labels, boolean verbose) {
		this.tlp = tlp;
		this.labels = new ArrayList<>(labels);

		//NOTE: assume that the first element of labels is rootLabel
		rootLabel = labels.get(0);
		makeTransitions();

		if (verbose) {
			log.info(Config.SEPARATOR);
			log.info("#Transitions: " + numTransitions());
			log.info("#Labels: " + labels.size());
			log.info("ROOTLABEL: " + rootLabel);
		}
	}

	public boolean isTerminal(Configuration c) {
		return (c.getStackSize() == 1 && c.getBufferSize() == 0);
	}

	/**
	 * the label root should not be included! it is not a dependency relation! R
	 * means a right child, L means a left child
	 */
	public void makeTransitions() {
		transitions = new ArrayList<>();

		// TODO store these as objects!
		for (String label : labels)
			transitions.add("L(" + label + ")");
		for (String label : labels)
			transitions.add("R(" + label + ")");

		transitions.add("S");
	}

	public Configuration initialConfiguration(CoreMap s) {
		Configuration c = new Configuration(s);
		List<CoreLabel> ant = s.get(CoreAnnotations.TokensAnnotation.class);
		int length = ant.size();

		// For each token, add dummy elements to the configuration's tree
		// and add the words onto the buffer
		for (int i = 1; i <= length; ++i) {
			c.buffer.add(new SyntacticTree(i - 1, ant.get(i - 1).originalText(), ant.get(i - 1).word(), ant.get(i - 1).tag(), (String) null));
		}

		// Put the ROOT node on the stack
		c.stack.add(new SyntacticTree(-1, null, null, null, (String) null));

		return c;
	}

	public Configuration initialConfiguration(SyntacticTree tree) {
		String[] seg = tree.getLEX();
		String[] pos = tree.getPOS();
		CoreMap s = SyntacticParser.toCoreMap(seg, pos);
		return initialConfiguration(s);
	}

	public boolean canApply(Configuration c, String t) {
		//		if (t.equals("R(root)")) {
		//			System.out.println(t);
		//			System.out.println("Configuration = \n" + c);
		//		}
		if (t.startsWith("L") || t.startsWith("R")) {
			String label = t.substring(2, t.length() - 1);
			SyntacticTree h = t.startsWith("L") ? c.getStack(0) : c.getStack(1);
			if (h == null)
				return false;

			if (h.id < 0 && !label.equals(rootLabel))
				return false;
			//if label is R(root), then h.id = -1, the label is applicable. 
			//if label is L(root), the label is never applicable.
			if (h.id >= 0 && label.equals(rootLabel))
				return false;
		}

		int nStack = c.getStackSize();
		int nBuffer = c.getBufferSize();

		if (t.startsWith("L"))
			return nStack > 2;
		else if (t.startsWith("R")) {
			if (singleRoot)
				return (nStack > 2) || (nStack == 2 && nBuffer == 0);
			else
				return nStack >= 2;
		} else
			return nBuffer > 0;
	}

	public void apply(Configuration c, String t) {
		SyntacticTree w1 = c.getStack(1);
		SyntacticTree w2 = c.getStack(0);
		if (t.startsWith("L")) {
			c.addArc(w2, w1, t.substring(2, t.length() - 1));
			c.removeSecondTopStack();
		} else if (t.startsWith("R")) {
			c.addArc(w1, w2, t.substring(2, t.length() - 1));
			c.removeTopStack();
		} else {
			c.shift();
		}
	}

	public boolean applyWithAdjustment(Configuration c, String t) throws Exception {
		SyntacticTree w1 = c.getStack(1);
		SyntacticTree w2 = c.getStack(0);
		if (t.startsWith("L")) {
			if (c.addArcWithAdjustment(w2, w1, t.substring(2, t.length() - 1))) {
				c.removeSecondTopStackWithAdjustment();
			} else
				return false;

		} else if (t.startsWith("R")) {
			if (c.addArcWithAdjustment(w1, w2, t.substring(2, t.length() - 1))) {
				c.removeTopStackWithAdjustment();
			} else
				return false;
		} else {
			c.shiftWithAdjustment();			
		}
		return true;
	}

	// O(n) implementation
	public String getOracle(Configuration c, SyntacticTree dTree) {
		SyntacticTree w1 = c.getStack(1);
		SyntacticTree w2 = c.getStack(0);
		if (w1 == null)
			return "S";

		SyntacticTree _w1 = dTree.getParticular(w1.id);
		SyntacticTree _w2 = dTree.getParticular(w2.id);

		if (_w1 != null && _w1.parent == _w2)
			return "L(" + _w1.dep + ")";
		else if (_w2.parent == _w1 && _w2.size == w2.size)
			return "R(" + _w2.dep + ")";
		else
			return "S";
	}

	private static Logger log = Logger.getLogger(ArcStandard.class);
}
