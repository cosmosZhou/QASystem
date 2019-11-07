package com.robot.semantic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import com.util.Utility;

public class Etymology {
	public Etymology(String word, HashSet<Etymology> equivalent, HashSet<Etymology> synonym, HashSet<Etymology> relative, HashSet<Etymology> antonym, HashSet<Etymology> hypernym) {
		this.word = word.intern();
		this.equivalent = equivalent;
		this.synonym = synonym;
		this.relative = relative;
		this.antonym = antonym;
		this.hypernym = hypernym;
	}

	public Etymology(String word) {
		this(word, new HashSet<Etymology>(), new HashSet<Etymology>(), new HashSet<Etymology>(), new HashSet<Etymology>(), new HashSet<Etymology>());
	}

	enum Langue {
		en, cn, fr, de,
	}

	String word;
	//	String langue;
	HashSet<Etymology> equivalent;
	HashSet<Etymology> synonym;
	HashSet<Etymology> relative;
	HashSet<Etymology> antonym;
	HashSet<Etymology> hypernym;
	HashSet<String> semanticSet;

	Langue language() {
		return Langue.cn;
	}

	HashSet<String> semanticSet() {
		if (semanticSet != null) {
			return semanticSet;
		}
		synchronized (this) {
			semanticSet = new HashSet<String>();
			semanticSet.add(word);
			for (Etymology t : hypernym) {
				semanticSet.addAll(t.semanticSet());
			}
		}
		return semanticSet;
	}

	double similarity(Etymology y) {
		if (this.equals(y) || equivalent.contains(y)) {
			return 1;
		}

		if (synonym.contains(y)) {
			return 0.9;
		}

		if (relative.contains(y)) {
			return 0.81;
		}

		if (antonym.contains(y)) {
			return -1;
		}

		//		double s1 = 0.9 * similarity(hypernym, y);
		//		double s2 = 0.9 * similarity(y.hypernym, this);
		//		return Utility.max(s1, s2);
		Etymology[] route = inheritanceRoute(y);
		if (route != null) {
			return Math.pow(0.9, route.length - 1);
		}
		route = y.inheritanceRoute(this);
		if (route != null) {
			return Math.pow(0.9, route.length - 1);
		}
		return 0;
		//		return 0.81 * similarity(hypernym, y.hypernym);
	}

	double similarity(HashSet<Etymology> y) {
		if (y.isEmpty())
			return 0;

		double maxSimilarity = -2;

		for (Etymology yy : y) {
			double similarity = similarity(yy);
			if (similarity > maxSimilarity) {
				maxSimilarity = similarity;
			}
		}

		return maxSimilarity;
	}

	boolean inheritanceRoute(Etymology ancester, ArrayList<Etymology> route) {
		route.add(this);
		for (Etymology parent : hypernym) {
			if (parent.equals(ancester)) {
				route.add(parent);
				return true;
			}
		}

		for (Etymology parent : hypernym) {
			if (parent.inheritanceRoute(ancester, route)) {
				return true;
			}
		}

		return false;
	}

	public Etymology[] inheritanceRoute(Etymology ancester) {
		ArrayList<Etymology> route = new ArrayList<Etymology>();
		if (inheritanceRoute(ancester, route)) {
			return route.toArray(new Etymology[route.size()]);
		}
		return null;
	}

	static double similarity(HashSet<Etymology> x, HashSet<Etymology> y) {
		if (x.isEmpty() || y.isEmpty())
			return 0;

		double maxSimilarity = -2;
		for (Etymology xx : x) {
			for (Etymology yy : y) {
				double similarity = xx.similarity(yy);
				if (similarity > maxSimilarity) {
					maxSimilarity = similarity;
				}
			}
		}

		return maxSimilarity;
	}

	static double similarity(HashSet<Etymology> x, Etymology y) {
		if (x.isEmpty())
			return 0;

		double maxSimilarity = -2;
		for (Etymology xx : x) {
			double similarity = xx.similarity(y);
			if (similarity > maxSimilarity) {
				maxSimilarity = similarity;
			}
		}

		return maxSimilarity;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Etymology) {
			return ((Etymology) obj).word.equals(this.word);
		}
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return word.hashCode();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return word;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public void addHypernym(HashSet<Etymology> hypernym) throws Exception {
		for (Etymology h : hypernym) {
			addHypernym(h);
		}
	}

	void addHypernym(Etymology ancester) throws Exception {
		hypernym.add(ancester);

		Etymology[] route = ancester.inheritanceRoute(this);
		if (route != null) {
			System.out.println("logical error: recursive inheritance");
			System.out.println(this + " < " + ancester);
			System.out.println(Utility.toString(route, " < ", null, route.length));
			throw new Exception("logical error: recursive inheritance");
		}
	}

	static void synchronizeHypernymSet(Etymology[] lexeme) throws Exception {
		HashSet<Etymology> hypernym = new HashSet<Etymology>();
		for (int i = 0; i < lexeme.length; ++i) {
			hypernym.addAll(lexeme[i].hypernym);
		}

		for (int i = 0; i < lexeme.length; ++i) {
			lexeme[i].addHypernym(hypernym);
		}
	}

	void addEquivalent(Etymology y) throws Exception {
		equivalent.add(y);
		y.equivalent.add(this);
		addHypernym(y.hypernym);
		y.addHypernym(hypernym);
	}
}
