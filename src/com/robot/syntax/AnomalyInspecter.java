package com.robot.syntax;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.robot.Sentence;
import com.util.Utility;
import com.util.Utility.Printer;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.io.TextTaggedFileReader;
import edu.stanford.nlp.tagger.maxent.TagCount;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.UTF8EquivalenceFunction;

public class AnomalyInspecter {
	public static String pathErr = Utility.workingDirectory + "corpus/err.txt";

	static HashSet<String> properNounCorrect = new HashSet<String>();
	static HashSet<String> normalNounCorrect = new HashSet<String>();

	static HashMap<String, HashSet<String>> lexicon = new HashMap<String, HashSet<String>>();

	static {
		for (String tag : POSTagger.instance.tagSet()) {
			lexicon.put(tag, new HashSet<String>());
		}
	}

	public static ArrayList<SyntacticTree> transform_corpus(String x, String y) throws Exception {
		return transform_corpus(x.trim().split("\\s+"), y.trim().split("\\s+"));
	}

	public static ArrayList<SyntacticTree> transform_corpus(String x[], String y[]) throws Exception {
		ArrayList<SyntacticTree> acceptionList = new ArrayList<SyntacticTree>();
		for (SyntacticTree tree : new DependencyTreeReader()) {
			String[] str = tree.getLEX();
			int index = Utility.containsSubstr(str, x);
			if (index >= 0) {
				//				System.out.println(tree);
				int d = -x.length + y.length;
				String[] cws = new String[str.length + d];

				int i = 0;
				for (i = 0; i < index; ++i) {
					cws[i] = str[i];
				}
				int mark[] = new int[y.length];
				for (; i - index < y.length; ++i) {
					cws[i] = y[i - index];
					mark[i - index] = i;
				}

				for (; i - d < str.length; ++i) {
					cws[i] = str[i - d];
				}

				tree = SyntacticParser.instance.parse(cws, POSTagger.instance.tag(cws)).adjust();
				System.out.println(tree.toString(true, mark));

				acceptionList.add(tree);
			}
		}
		return acceptionList;
	}

	public static int dep() throws Exception {
		FilterSet.initialize_err();
		ArrayList<SyntacticTree> acceptionList = new ArrayList<SyntacticTree>();
		TreeSet<String> exceptionList = new TreeSet<String>();

		int modified = 0;

		for (SyntacticTree tree : new DependencyTreeReader()) {
			if (tree.getDEP().length < 3) {
				continue;
			}

			Filter sift = FilterSet.containsIrregulation(tree);
			if (sift == null) {
				acceptionList.add(tree);
			} else {
				++modified;
				String sent = tree.unadornedExpression();
				System.out.println(tree.toString(true, sift.anomalySet(tree)));
				System.out.println("Anomaly Inspected: " + sift.regulation);

				SyntacticTree _tree = SyntacticParser.instance.parseWithAdjustment(tree.getLEX(), tree.getPOS());
				if (_tree != null && FilterSet.containsIrregulation(_tree) == null) {
					acceptionList.add(_tree);
				} else {
					acceptionList.add(tree);
					exceptionList.add(sent);
				}
			}
		}

		for (String str : new Utility.Text(DependencyTreeReader._depCorpus)) {
			SyntacticTree tree = new Sentence(str).tree();
			if (tree.getDEP().length < 3) {
				continue;
			}

			Filter filter = FilterSet.containsIrregulation(tree);
			if (filter == null) {
				acceptionList.add(tree);
				//				System.out.println(tree);

			} else {
				tree = SyntacticParser.instance.parseWithAdjustment(tree.getLEX(), tree.getPOS());
				if (tree != null && FilterSet.containsIrregulation(tree) == null) {
					acceptionList.add(tree);

				} else {
					exceptionList.add(str);
				}
			}
		}

		System.out.println(modified + " NEW CASES modified!");

		DependencyTreeReader.writeTrainingInstance(acceptionList);
		System.out.println(exceptionList.size() + " EXCEPTIONS CAUGHT!");

		Utility.writeString(DependencyTreeReader._depCorpus, exceptionList);

		System.out.println("Anomaly Inspection finished");
		return modified;
	}

	public static ArrayList dep(String regex) throws Exception {
		Filter sift = construct(regex);
		ArrayList correctionList = Utility.list();
		for (SyntacticTree tree : new DependencyTreeReader()) {
			if (sift.satisfy(tree)) {
				System.out.println(tree.toString(true, sift.anomalySet(tree)));
				correctionList.add(tree);
			}
		}
		return correctionList;
	}

	public static class FilterSet {
		public static Filter[] syntacticIrregulation;
		static {
			try {
				FilterSet.initialize_err();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public static Filter containsIrregulation(SyntacticTree tree) throws Exception {
			for (int i = 0; i < syntacticIrregulation.length; ++i) {
				if (syntacticIrregulation[i].satisfy(tree)) {
					return syntacticIrregulation[i];
				}
			}
			return null;
		}

		static public void add(String sift) throws Exception {
			Utility.appendString(pathErr, sift);
			TreeSet<String> st = FilterSet.initialize_err();
			Utility.writeString(pathErr, st);
		}

		public static TreeSet<String> initialize_err() throws Exception {
			ArrayList<Filter> irregulation = new ArrayList<Filter>();
			TreeSet<String> set = new TreeSet<String>();

			for (String str : new Utility.Text(pathErr)) {
				Filter filter = construct(str);

				str = filter.regulation;
				if (!set.contains(str)) {
					irregulation.add(filter);
					set.add(str);
				} else {
					System.out.println("duplicate rules found! " + str);
				}
			}

			syntacticIrregulation = irregulation.toArray(new Filter[irregulation.size()]);
			return set;
		}
	}

	static public String[] findPossibleTags(String seg) throws Exception {
		seg = Utility.simplifyString(seg);
		Map<String, Set<String>> dict = null;
		Map<String, Set<String>> tagTokens = null;

		dict = Generics.newHashMap();
		tagTokens = Generics.newHashMap();

		TextTaggedFileReader textTaggedFileReader = new TextTaggedFileReader(DependencyTreeReader.depCorpus);
		for (List<TaggedWord> sent : textTaggedFileReader) {
			for (TaggedWord taggedWord : sent) {
				String word = taggedWord.word();
				String tag = taggedWord.tag();

				if (!dict.containsKey(word)) {
					dict.put(word, Generics.newHashSet());
				}
				dict.get(word).add(tag);

				if (!tagTokens.containsKey(tag)) {
					tagTokens.put(tag, Generics.newHashSet());
				}
				tagTokens.get(tag).add(word);
			}
		}

		if (POSTagger.instance.tagSet().contains(seg)) {
			return tagTokens.get(seg).toArray(new String[0]);
		}

		return dict.get(seg).toArray(new String[0]);
	}

	static public ArrayList pos(String infix) throws Exception {
		Filter sift = new FilterPOS(infix);
		ArrayList arr = Utility.list();
		for (SyntacticTree tree : new DependencyTreeReader()) {
			if (sift.satisfy(tree)) {
				System.out.println(tree.toString(true, sift.anomalySet(tree)));
				arr.add(tree);
			}
		}
		return arr;
	}

	static void scan(ArrayList<String> arr, ArrayList<String> arrAnomaly) throws UnsupportedEncodingException, FileNotFoundException, IOException {
	}

	static public int match(String[][] inst, String word, String tag) {
		for (int i = 0; i < inst[0].length; ++i) {
			if (inst[0][i].equals(word) && inst[1][i].equals(tag)) {
				return i;
			}
		}
		return -1;
	}

	static public int dismatch(String[][] inst, String word, String tag) {
		for (int i = 0; i < inst[0].length; ++i) {
			if (inst[0][i].equals(word) && !inst[1][i].equals(tag)) {
				return i;
			}
		}
		return -1;
	}

	static public boolean equals(String[] inst, int _i, String word[]) {
		for (int i = 0; i < word.length; ++i) {
			if (i + _i >= inst.length || !inst[i + _i].matches(word[i]))
				return false;
		}
		return true;
	}

	static public boolean equals(String[][] inst, int _i, String word[]) {
		for (int i = 0; i < word.length; ++i) {
			if (i + _i >= inst[0].length)
				return false;
			String[] res = Utility.regexSingleton(word[i], "(\\S+)/(\\S+)");

			if (res != null) {
				if (!inst[0][i + _i].matches(res[1]) || !inst[1][i + _i].equals(res[2]))
					return false;

			} else {
				if (!inst[0][i + _i].matches(word[i]))
					return false;
			}
		}
		return true;
	}

	static public void set(String[] inst, int _i, String tag[]) {
		for (int i = 0; i < tag.length; ++i) {
			inst[i + _i] = tag[i];
		}
	}

	static public int dismatch(String[][] inst, String word[], String tag[]) {
		for (int i = 0; i < inst[0].length; ++i) {
			if (equals(inst[0], i, word) && !equals(inst[1], i, tag)) {
				return i;
			}
		}
		return -1;
	}

	static public void dismatchAndSet(String[][] inst, String word[][], String tag[][]) {
		int length = inst[0].length;
		for (int i = 0; i < length; ++i) {
			for (int j = 0; j < word.length; ++j) {
				if (equals(inst, i, word[j])) {
					int lengthWord = word[j].length;
					if (tag[j].length == 1 && lengthWord > 1) {
						for (int t = 1; t < lengthWord; ++t) {
							inst[0][i] += inst[0][i + t];
							inst[0][i + t] = "";
							inst[1][i + t] = "";
						}
						inst[1][i] = tag[j][0];
						continue;
					}

					if (tag[j].length != word[j].length) {
						System.out.println(Utility.toString(word, " ", null) + " = " + Utility.toString(tag, " ", null));
						throw new RuntimeException("tag[j].length != word[j].length");
					}
					if (!equals(inst[1], i, tag[j])) {
						set(inst[1], i, tag[j]);
					}
				}
			}
		}
	}

	static public void matchRegex(String[][] inst) {
		for (int i = 0; i < inst[0].length; ++i) {
			if (inst[0][i].matches("[" + Utility.dightsChinese + "]+") && !inst[1][i].equals("CD")) {
				System.out.println("before setting WORD = " + inst[0][i]);
				System.out.println("before setting tag  = " + inst[1][i]);
				inst[1][i] = "CD";
			}

			if (i - 1 >= 0 && i - 1 < inst[0].length && inst[0][i].equals("点") && !inst[1][i].equals("O") && inst[0][i - 1].matches(".*[" + Utility.dightsChinese + "]$") && inst[0][i + 1].matches("^[" + Utility.dightsChinese + "].*")) {
				System.out.println("before setting WORD = " + inst[0][i - 1] + "\t" + inst[0][i] + "\t" + inst[0][i + 1]);
				System.out.println("before setting tag  = " + inst[1][i - 1] + "\t" + inst[1][i] + "\t" + inst[1][i + 1]);
				inst[1][i] = "O";
				inst[1][i + 1] = "CD";
				inst[1][i - 1] = "CD";
			}
		}
	}

	static public int match(String[][] inst, HashMap<String, HashSet<String>> map) {
		for (Map.Entry<String, HashSet<String>> entry : map.entrySet()) {
			for (String word : entry.getValue()) {
				int index = match(inst, word, entry.getKey());
				if (index >= 0) {
					return index;
				}
			}
		}

		return -1;
	}

	static public int matchLegal(String[][] inst, HashMap<String, HashSet<String>> map) {
		for (int i = 0; i < inst[0].length; ++i) {
			String word = inst[0][i];
			String tag = inst[1][i];

			if (tag.equals("CD") || tag.equals("NT"))
				continue;
			if (!map.get(tag).contains(new UTF8EquivalenceFunction().apply(word)) && !map.get(tag).contains(word))
				return i;
		}

		return -1;
	}

	static public int matchIllegal(String[][] inst, HashMap<String, HashSet<String>> map) throws IOException {
		for (int i = 0; i < inst[0].length; ++i) {
			String word = inst[0][i];
			String tag = inst[1][i];
			HashSet<String> set = map.get(tag);
			if (set != null && set.contains(word)) {
				System.out.println("the word to classift is " + word);
				System.out.println("type tag to classify it, type enter to quit, the context is : ");
				System.out.println(Utility.convertSegmentationToOriginal(inst[0]));
				for (String s : Utility.convertWithAlignment(inst[0], Utility.errorMark(inst[0].length, i), inst[1])) {
					System.out.println(s);
				}

				input = buffer.readLine();
				input = input.toUpperCase();
				if (input.length() > 0) {
					String pos[] = input.split("\\s+");
					if (pos.length == 1) {
						inst[1][i] = pos[0];
					} else if (pos.length == inst[1].length) {
						System.arraycopy(pos, 0, inst[1], 0, pos.length);
					}
					continue;
				}

				return i;
			}
		}

		return -1;
	}

	static void correct(String[][] inst, int i) throws IOException {
		String word = inst[0][i];
		String tag = inst[1][i];

		System.out.println("the word to classift is " + word);
		System.out.println("the unknown tag is " + tag);
		for (String s : Utility.convertWithAlignment(inst[0], Utility.errorMark(inst[0].length, i), inst[1])) {
			System.out.println(s);
		}

		System.out.println("type correct tag");
		input = buffer.readLine();
		input = input.toUpperCase();
		if (POSTagger.instance.tagSet().contains(input)) {
			inst[1][i] = input;
			lexicon.get(input).add(word);
		}
	}

	static public int scanLexicon(String[][] inst) throws IOException {
		for (int i = 0; i < inst[0].length; ++i) {
			String word = inst[0][i];
			String tag = inst[1][i].toUpperCase();
			if (!POSTagger.instance.tagSet().contains(tag)) {
				correct(inst, i);
				continue;
			}
			switch (tag) {
			case "NR":
				if (properNounCorrect.contains(word)) {
					continue;
				}
				if (normalNounCorrect.contains(word)) {
					inst[1][i] = "NN";
					continue;
				}
				if (lexicon.get("NN").contains(word)) {
					System.out.println("the word to classift is " + word);
					System.out.println("the context is ");
					System.out.println(Utility.convertSegmentationToOriginal(inst[0]));
					for (String s : Utility.convertWithAlignment(inst[0], Utility.errorMark(inst[0].length, i), inst[1])) {
						System.out.println(s);
					}

					System.out.println("type NR to classify it as NR, type NN to classify it as NN, type enter to quit.");
					input = buffer.readLine();
					input = input.toUpperCase();
					switch (input) {
					case "NR":
						lexicon.get("NR").remove(word);
						lexicon.get("NN").remove(word);
						properNounCorrect.add(word);
						inst[1][i] = "NR";
						continue;
					case "NN":
						lexicon.get("NR").remove(word);
						lexicon.get("NN").remove(word);
						normalNounCorrect.add(word);
						inst[1][i] = "NN";
						continue;
					default:
						if (input.length() > 0) {
							inst[1][i] = input;
							continue;
						}

						return i;
					}

				} else
					lexicon.get("NR").add(word);
				break;
			case "NN":
				if (properNounCorrect.contains(word)) {
					inst[1][i] = "NR";
					continue;
				}
				if (normalNounCorrect.contains(word)) {
					continue;
				}

				if (lexicon.get("NR").contains(word)) {
					System.out.println("the word to classify is " + word);
					System.out.println("the context is ");
					System.out.println(Utility.convertSegmentationToOriginal(inst[0]));
					for (String s : Utility.convertWithAlignment(inst[0], Utility.errorMark(inst[0].length, i), inst[1])) {
						System.out.println(s);
					}
					System.out.println("type NR to classify it as NR, type NN to classify it as NN, type enter to quit.");
					input = buffer.readLine();
					input = input.toUpperCase();
					switch (input) {
					case "NR":
						lexicon.get("NR").remove(word);
						lexicon.get("NN").remove(word);
						properNounCorrect.add(word);
						inst[1][i] = "NR";
						continue;
					case "NN":
						lexicon.get("NR").remove(word);
						lexicon.get("NN").remove(word);
						normalNounCorrect.add(word);
						inst[1][i] = "NN";
						continue;
					default:
						if (input.length() > 0) {
							inst[1][i] = input;
							continue;
						}

						return i;
					}
				} else
					lexicon.get("NN").add(word);
				break;
			case "VT":
				//				if (lexicon.get("VT").contains(word)) {
				//					continue;
				//				}
				//				if (lexicon.get("NR").contains(word)) {
				//					correct(inst, i);
				//					continue;
				//				} else
				//					lexicon.get("VT").add(word);
			case "VI":
			default:
			}
		}

		return -1;
	}

	static public void printIntersect(String p1, String p2) {
		HashSet<String> intersect;
		System.out.println("intersection of " + p1 + ", " + p2);
		intersect = Utility.intersect(lexicon.get(p1), lexicon.get(p2));
		System.out.println(intersect);
	}

	static public void scanLexiconToSetup() throws UnsupportedEncodingException, FileNotFoundException, IOException {
		for (SyntacticTree inst : new DependencyTreeReader()) {
			//			scanLexiconToSetup(inst);
		}

		printIntersect("NR", "VT");
		printIntersect("NR", "VI");
		printIntersect("NR", "VC");
		printIntersect("NR", "VA");
		printIntersect("NR", "JJ");
		printIntersect("NR", "AD");
		printIntersect("NR", "NN");
		printIntersect("NR", "VBG");
		printIntersect("NR", "IJ");
		printIntersect("NR", "NT");
		printIntersect("NR", "M");
		printIntersect("NR", "DT");
		printIntersect("NR", "CD");
		printIntersect("NR", "CS");
		printIntersect("NR", "LC");
		printIntersect("NR", "O");
		printIntersect("VBG", "NN");
		printIntersect("AD", "AS");
		printIntersect("AD", "MD");
		printIntersect("AD", "VI");
		printIntersect("AD", "VT");
		printIntersect("AS", "LC");
	}

	public static Filter construct(String exp) throws Exception {
		String[] res = Utility.regexSingleton(exp, "(\\S+)\\s*!=([\\s\\S]+)");
		if (res != null)
			return constructNegate(res[1], res[2]);

		res = Utility.regexSingleton(exp, "(\\S+)\\s*=([\\s\\S]+)");
		if (res != null)
			return construct(res[1], res[2]);

		return new FilterExpression(exp);

	}

	static ErrorLex[][] parseComponent(String x[]) {
		ErrorLex[][] res = new ErrorLex[x.length][];
		for (int i = 0; i < res.length; i++) {
			res[i] = parseComponent(x[i]);
		}
		return res;
	}

	static ErrorLex[] parseComponent(String x) {
		ErrorLex[] res = new ErrorLex[3];
		if (x.startsWith("(")) {
			if (x.endsWith(")")) {
				x = x.substring(1, x.length() - 1);
			} else
				throw new RuntimeException();
		}

		for (String s : x.split("/")) {
			boolean negate = false;
			if (s.startsWith("^")) {
				s = s.substring(1);
				negate = true;
			}

			String[] arr = s.split("\\|");
			if (arr.length == 1) {
				if (POSTagger.instance.tagSet().contains(s)) {
					res[1] = negate ? new ErrorLexSingleNegate(s) : new ErrorLexSingle(s);
				} else if (SyntacticParser.instance.tagSet().contains(s)) {
					res[2] = negate ? new ErrorLexSingleNegate(s) : new ErrorLexSingle(s);
				} else {
					s = Utility.simplifyString(s);
					res[0] = negate ? new ErrorLexSingleNegate(s) : new ErrorLexSingle(s);
				}
			} else {
				if (POSTagger.instance.tagSet().contains(arr[0])) {
					res[1] = negate ? new ErrorLexSequenceNegate(arr) : new ErrorLexSequence(arr);
				} else if (SyntacticParser.instance.tagSet().contains(arr[0])) {
					res[2] = negate ? new ErrorLexSequenceNegate(arr) : new ErrorLexSequence(arr);
				} else {
					arr = Utility.simplifyString(arr);
					res[0] = negate ? new ErrorLexSequenceNegate(arr) : new ErrorLexSequence(arr);
				}
			}
		}
		return res;
	}

	static String configureComponent(ErrorLex[] lex) {
		return configureComponent(lex, true);
	}

	static String configureComponent(ErrorLex[] lex, boolean bParenthesis) {
		String s = "";
		for (ErrorLex word : lex) {
			if (word == null)
				continue;
			if (!s.isEmpty())
				s += "/";
			s += word;
		}

		if (bParenthesis && s.contains("/"))
			return "(" + s + ")";
		return s;
	}

	static String configureComponent(ErrorLex[][] lex) {
		String[] str = new String[lex.length];
		for (int i = 0; i < str.length; i++) {
			str[i] = configureComponent(lex[i]);
		}
		return Utility.toString(str, ", ");
	}

	static boolean satisfy(SyntacticTree tree, ErrorLex component[]) {
		return (component[0] == null || component[0].equals(tree.seg))

				&& (component[1] == null || component[1].equals(tree.pos))

				&& (component[2] == null || component[2].equals(tree.dep));
	}

	static boolean satisfy(SyntacticTree tree, ErrorLex component[][]) {
		for (ErrorLex comp[] : component) {
			if (satisfy(tree, comp)) {
				return true;
			}
		}
		return false;
	}

	public static Filter construct(String exp1, String exp2) {
		String[] res = Utility.regexSingleton(exp1, "(\\S+)\\s*\\.\\s*parent");
		if (res != null) {

			String x = res[1];
			res = Utility.regexSingleton(exp2, "\\s*(\\S+)\\s*\\.\\s*parent");
			if (res != null) {
				String y = res[1];
				return new FilterCommonParent(x, y);
			} else {
				return new FilterFixedParent(x, exp2);
			}
		}
		res = Utility.regexSingleton(exp1, "(\\S+)\\s*\\.\\s*left");
		if (res != null) {
			String x = res[1];
			return new FilterFixedLeft(exp2, x);
		}
		res = Utility.regexSingleton(exp1, "(\\S+)\\s*\\.\\s*right");
		if (res != null) {
			String x = res[1];
			return new FilterFixedRight(exp2, x);
		}

		System.out.println(exp1);
		System.out.println(exp2);
		throw new RuntimeException();
	}

	public static Filter constructNegate(String exp1, String exp2) {
		if (exp1.matches("(\\S+)\\s*\\.\\s*parent")) {
			String[] res = Utility.regexSingleton(exp1, "(\\S+)\\s*\\.\\s*parent");
			String x = res[1];
			return new FilterFixedParentNegate(x, exp2);
		} else if (exp1.matches("(\\S+)\\s*\\.\\s*left")) {
			String[] res = Utility.regexSingleton(exp1, "(\\S+)\\s*\\.\\s*left");
			String x = res[1];
			return new FilterFixedLeftNegate(exp2, x);
		} else if (exp1.matches("(\\S+)\\s*\\.\\s*right")) {
			String[] res = Utility.regexSingleton(exp1, "(\\S+)\\s*\\.\\s*right");
			String x = res[1];
			return new FilterFixedRightNegate(exp2, x);
		} else
			throw new RuntimeException();
	}

	public static abstract class Filter {
		public abstract boolean satisfy(SyntacticTree tree) throws Exception;

		public int[] anomalySet(SyntacticTree tree) throws Exception {
			ArrayList<Integer> arr = new ArrayList<Integer>();
			anomalySet(tree, arr);
			return Utility.toArrayInteger(arr);
		}

		public boolean anomalySet(SyntacticTree tree, ArrayList<Integer> arr) throws Exception {

			boolean drapeau = true;
			for (SyntacticTree t : tree.leftChildren) {
				if (anomalySet(t, arr)) {
					drapeau = false;
				}
			}

			for (SyntacticTree t : tree.rightChildren) {
				if (anomalySet(t, arr)) {
					drapeau = false;
				}
			}

			if (drapeau) {
				if (satisfy(tree)) {
					arr.add(tree.id);
					return true;
				}
				return false;
			}
			return true;
		}

		public String regulation;

		@Override
		public String toString() {
			return regulation;
		}

		boolean isPOSRegulation() {
			return false;
		}
	}

	public static class FilterPOS extends Filter {
		private ErrorLex[] segSub;
		private ErrorLex[] posSub;

		public FilterPOS(String infix) {
			this.regulation = infix;
			String[] arr = infix.split("\\s+");
			this.segSub = new ErrorLex[arr.length];
			this.posSub = new ErrorLex[arr.length];

			for (int i = 0; i < arr.length; i++) {
				ErrorLex[] res = parseComponent(arr[i]);
				posSub[i] = res[1];
				segSub[i] = res[0];
			}
		}

		public boolean satisfy(SyntacticTree tree) {
			return search(tree) >= 0;
		}

		int search(SyntacticTree tree) {
			String[] pos = tree.getPOS();
			String[] seg = tree.getSEG();
			for (int i = 0; i <= seg.length - segSub.length; ++i) {
				if (AnomalyInspecter.equals(seg, segSub, i) && AnomalyInspecter.equals(pos, posSub, i))
					return i;
			}
			return -1;
		}

		public void clearDict() {
			for (int j = 0; j < segSub.length; ++j) {
				ErrorLex seg = segSub[j];
				ErrorLex pos = posSub[j];
				if (seg != null && pos != null)
					seg.alterLexicon(POSTagger.instance.dict.dict, pos);
			}
		}

		public SyntacticTree adjust(SyntacticTree tree) throws Exception {
			String[] lex = tree.getLEX();
			String[] seg = tree.getSEG();
			String[] pos = tree.getPOS();

			boolean bNeedAdjustment = false;
			int i = search(tree);
			assert i >= 0;

			for (int j = 0; j < segSub.length; ++j) {
				if (segSub[j] != null && posSub[j] != null) {
					pos[i + j] = "";
					bNeedAdjustment = true;
				}
			}

			if (bNeedAdjustment) {
				pos = POSTagger.instance.tag(seg, pos);
				tree = SyntacticParser.instance.parse(lex, pos).adjust();
			}
			return tree;
		}

		public int[] anomalySet(SyntacticTree tree) {

			int i = search(tree);
			if (i < 0)
				return null;
			ArrayList arr = Utility.list();

			for (int j = 0; j < segSub.length; ++j) {
				if (segSub[j] != null && posSub[j] != null)
					arr.add(i + j);
			}
			return Utility.toArrayInteger(arr);
		}

	}

	static boolean equals(String[] dep, ErrorLex[] depSub) {
		for (int i = 0; i < depSub.length; ++i) {
			if (depSub[i] != null && !depSub[i].equals(dep[i])) {
				return false;
			}
		}
		return true;
	}

	static boolean equals(String[] dep, ErrorLex[] depSub, int start) {
		for (int i = 0; i < depSub.length; ++i) {
			if (depSub[i] != null && !depSub[i].equals(dep[i + start])) {
				return false;
			}
		}
		return true;
	}

	static abstract class ErrorLex {
		abstract boolean equals(String word);

		public abstract String regex();

		public void alterLexicon(Map<String, TagCount> dict, ErrorLex pos) {
		}

		@Override
		public abstract String toString();
	}

	static class ErrorLexSingle extends ErrorLex {
		String tag;

		public ErrorLexSingle(String tag) {
			this.tag = tag;
		}

		@Override
		boolean equals(String word) {
			return tag.equals(word);
		}

		@Override
		public String toString() {
			return tag;
		}

		public void alterLexicon(Map<String, TagCount> lexicon, ErrorLex pos) {
			if (lexicon.containsKey(tag)) {
				if (pos instanceof ErrorLexSingle)
					lexicon.get(tag).removeTag(((ErrorLexSingle) pos).tag);
				else if (pos instanceof ErrorLexSingleNegate) {
					lexicon.get(tag).retainTag(((ErrorLexSingleNegate) pos).tag);
				}
			}
		}

		@Override
		public String regex() {
			return tag;
		}
	}

	static class ErrorLexSingleNegate extends ErrorLex {
		String tag;

		public ErrorLexSingleNegate(String tag) {
			this.tag = tag;
		}

		@Override
		boolean equals(String word) {
			return !tag.equals(word);
		}

		@Override
		public String toString() {
			return "^" + tag;
		}

		@Override
		public String regex() {
			return "(?!" + tag + ")[^\\(\\)]+";
		}

	}

	static class ErrorLexSequence extends ErrorLex {
		//return true if the given word matches any one of the pattern
		String tag[];

		ErrorLexSequence(String tag[]) {
			this.tag = tag;
		}

		public boolean equals(String word) {
			for (String w : this.tag) {
				if (w.equals(word))
					return true;
			}
			return false;
		}

		public String toString() {
			return Utility.toString(this.tag, "|");
		}

		@Override
		public String regex() {
			return "(" + toString() + ")";
		}

	}

	static class ErrorLexSequenceNegate extends ErrorLexSequence {
		/*
		 * return true if the given word does not match any one of the pattern
		 */
		ErrorLexSequenceNegate(String[] tag) {
			super(tag);
		}

		public boolean equals(String word) {
			for (String w : this.tag) {
				if (w.equals(word))
					return false;
			}
			return true;
		}

		public String toString() {
			return '^' + super.toString();
		}

		@Override
		public String regex() {
			return "(?!" + super.regex() + ")[^\\(\\)]+";
		}

	}

	static class FilterExpression extends Filter {

		String infix;

		static public String regexp(String lexme) {
			ErrorLex[] lex = parseComponent(lexme);
			String s = "";

			for (ErrorLex word : lex) {
				if (!s.isEmpty())
					s += "/";

				if (word == null) {
					s += "[^\\(\\)]+";
				} else {
					s += word.regex();
				}
			}

			return s;
		}

		public FilterExpression(String exp) throws Exception {
			this.regulation = exp;
			regulation = regulation.replaceAll("\\s+", "");

			infix = "";
			int start = 0;
			Matcher m = Pattern.compile("[\\(\\)\\+]+").matcher(regulation);

			while (m.find()) {
				if (m.start() > start) {
					String lexme = regulation.substring(start, m.start());
					infix += regexp(lexme);
				}

				infix += m.group(0).replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");

				start = m.end();
			}

			if (regulation.length() > start) {
				String lexme = regulation.substring(start);
				infix += regexp(lexme);
			}

		}

		@Override
		public boolean satisfy(SyntacticTree tree) throws Exception {
			Matcher m = Pattern.compile(infix).matcher(tree.infixPOSExpression());
			if (m.find()) {
				return true;
			}
			return false;
		}
	}

	static class FilterCommonParent extends Filter {
		ErrorLex lexx[], lexy[];

		public FilterCommonParent(String x, String y) {
			lexx = parseComponent(x);
			lexy = parseComponent(y);

			this.regulation = configureComponent(lexx) + ".parent = " + configureComponent(lexy) + ".parent";
		}

		@Override
		public boolean satisfy(SyntacticTree tree) {
			HashSet<SyntacticTree> set1 = new HashSet<SyntacticTree>();
			HashSet<SyntacticTree> set2 = new HashSet<SyntacticTree>();
			for (SyntacticTree t : tree.getSyntacticTree()) {
				if (AnomalyInspecter.satisfy(t, lexx)) {
					set1.add(t.parent);
				} else if (AnomalyInspecter.satisfy(t, lexy)) {
					set2.add(t.parent);
				}
			}
			return Utility.intersect(set1, set2).size() > 0;
		}
	}

	static class FilterFixedParent extends Filter {
		ErrorLex lex[];
		ErrorLex parent[][];

		public FilterFixedParent(String lex, String parent) {
			this.lex = parseComponent(lex);
			this.parent = parseComponent(parent.trim().split("\\s*,\\s*"));
			regulation = configureComponent(this.lex) + ".parent = " + configureComponent(this.parent);
		}

		@Override
		public boolean satisfy(SyntacticTree tree) {
			for (SyntacticTree t : tree.getSyntacticTree()) {
				if (t.parent == null) {
					continue;
				}

				if (AnomalyInspecter.satisfy(t, lex) && AnomalyInspecter.satisfy(t.parent, parent))
					return true;
			}
			return false;
		}
	}

	static class FilterFixedParentNegate extends Filter {
		ErrorLex lex[];
		ErrorLex parent[][];

		public FilterFixedParentNegate(String lex, String parent) {
			this.lex = parseComponent(lex);
			this.parent = parseComponent(parent.trim().split("\\s*,\\s*"));
			regulation = configureComponent(this.lex) + ".parent != " + configureComponent(this.parent);
		}

		@Override
		public boolean satisfy(SyntacticTree tree) {
			for (SyntacticTree t : tree.getSyntacticTree()) {
				if (t.parent == null) {
					continue;
				}

				if (AnomalyInspecter.satisfy(t, lex) && !AnomalyInspecter.satisfy(t.parent, parent))
					return true;
			}
			return false;
		}
	}

	static class FilterFixedLeft extends Filter {
		ErrorLex left[][];
		ErrorLex parent[];

		public FilterFixedLeft(String left, String parent) {
			this.left = parseComponent(left.trim().split("\\s*,\\s*"));
			this.parent = parseComponent(parent);
			regulation = configureComponent(this.parent) + ".left = " + configureComponent(this.left);
		}

		@Override
		public boolean satisfy(SyntacticTree tree) {
			for (SyntacticTree t : tree.getSyntacticTree()) {
				if (t.parent == null || !t.isLeftChild()) {
					continue;
				}
				if (AnomalyInspecter.satisfy(t, left) && AnomalyInspecter.satisfy(t.parent, parent))
					return true;
			}
			return false;
		}
	}

	static class FilterFixedLeftNegate extends Filter {
		ErrorLex left[][];
		ErrorLex parent[];

		public FilterFixedLeftNegate(String left, String parent) {
			this.left = parseComponent(left.trim().split("\\s*,\\s*"));
			this.parent = parseComponent(parent);
			regulation = configureComponent(this.parent) + ".left != " + configureComponent(this.left);
		}

		@Override
		public boolean satisfy(SyntacticTree tree) {
			for (SyntacticTree t : tree.getSyntacticTree()) {
				if (t.parent == null || !t.isLeftChild()) {
					continue;
				}
				if (!AnomalyInspecter.satisfy(t, left) && AnomalyInspecter.satisfy(t.parent, parent))
					return true;
			}
			return false;
		}
	}

	static class FilterFixedRight extends Filter {
		ErrorLex right[][];
		ErrorLex parent[];

		public FilterFixedRight(String right, String parent) {
			this.right = parseComponent(right.trim().split("\\s*,\\s*"));
			this.parent = parseComponent(parent);
			regulation = configureComponent(this.parent) + ".right = " + configureComponent(this.right);
		}

		@Override
		public boolean satisfy(SyntacticTree tree) {
			for (SyntacticTree t : tree.getSyntacticTree()) {
				if (t.parent == null || !t.isRightChild()) {
					continue;
				}
				if (AnomalyInspecter.satisfy(t, right) && AnomalyInspecter.satisfy(t.parent, parent))
					return true;
			}
			return false;
		}
	}

	static class FilterFixedRightNegate extends Filter {
		ErrorLex right[][];
		ErrorLex parent[];

		public FilterFixedRightNegate(String right, String parent) {
			this.right = parseComponent(right.trim().split("\\s*,\\s*"));
			this.parent = parseComponent(parent);
			regulation = configureComponent(this.parent) + ".right != " + configureComponent(this.right);
		}

		@Override
		public boolean satisfy(SyntacticTree tree) {
			for (SyntacticTree t : tree.getSyntacticTree()) {
				if (t.parent == null || !t.isRightChild()) {
					continue;
				}
				if (!AnomalyInspecter.satisfy(t, right) && AnomalyInspecter.satisfy(t.parent, parent))
					return true;
			}
			return false;
		}
	}

	public static void removeDE() throws IOException {
		ArrayList arr = Utility.list();
		for (SyntacticTree tree : new DependencyTreeReader()) {
			for (SyntacticTree t : tree) {
				if (t.dep.equals("de") && t.parent.pos.equals("DE")) {
					System.out.println(t.parent);
					t.dep = t.parent.dep;
					System.out.println(t.parent);
				}
			}
			arr.add(tree);
			System.out.println(tree);
		}

		DependencyTreeReader.writeTrainingInstance(arr);
	}

	public static void main(String[] args) throws Exception {
		removeDE();
	}

	static String input;
	static BufferedReader buffer = Utility.readFromStdin();

}
