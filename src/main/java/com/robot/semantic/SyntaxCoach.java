package com.robot.semantic;

import java.io.BufferedReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.robot.syntax.CWSTagger;
import com.robot.syntax.DependencyTreeReader;
import com.robot.syntax.AnomalyInspecter;
import com.robot.syntax.AnomalyInspecter.Filter;
import com.robot.syntax.AnomalyInspecter.FilterPOS;
import com.robot.syntax.POSTagger;
import com.robot.syntax.Compiler;
import com.robot.syntax.Constituent;
import com.robot.syntax.SyntacticParser;
import com.robot.syntax.SyntacticTree;
import com.robot.QASystem;
import com.robot.Sentence;
import com.robot.Sentence.QATYPE;
import com.robot.semantic.RNN.ConstituentAbsorber;
import com.robot.semantic.RNN.RNNParaphrase;
import com.robot.semantic.RNN.RNNPhaticsClassifier;
import com.robot.semantic.RNN.RNNQAClassifier;
import com.robot.semantic.RNN.RNNTopicClassifier;
import com.util.Utility;

public class SyntaxCoach {
	SyntacticTree tree;
	boolean bCWS = false, bPOS = false, bDEP = false;
	boolean bTopic = false;

	enum Falg {
		CONTINUE, BREAK, EXIT;
	}

	void extract() throws Exception {
		if (tree != null) {
			if (bTopic) {
				Sentence sent = new Sentence(tree);
				//				System.out.println(tree);
				for (String topic : RNNTopicClassifier.instance.classify(sent.sentence)) {
					System.out.println(topic);
				}
			} else {
				Sentence sent = new Sentence(tree);
				String[] seg = sent.seg();
				String[] pos = sent.pos();
				String[] featureQA = new String[seg.length];
				ArrayList<Integer> arr = new ArrayList<Integer>();
				sent.dep();
				for (int j = 0; j < seg.length; j++) {
					if (sent.interrogativeExtractionRNN(j)) {
						arr.add(j);
					}
					featureQA[j] = RNNQAClassifier.instance.combination().combine(seg[j], pos[j]);
				}
				System.out.println("qatype : " + sent.qatype());
				if (arr.size() >= 0) {
					System.out.println(sent);

					for (String s : Utility.convertWithAlignment(seg, pos, sent.tree().getDEP(), featureQA, Utility.errorMark(seg.length, Utility.toArrayInteger(arr)))) {
						System.out.println(s);
					}
				}
				//				System.out.println(tree.toBinarizedTree(RNNQAClassifier.combination).toString());
				System.out.println(tree.toConstituentTree());
				System.out.println(tree);
				Filter filter = AnomalyInspecter.FilterSet.containsIrregulation(tree);
				if (filter != null) {
					System.out.println("Anomaly Inspected: " + filter.regulation);
				}
			}
		}
	}

	public Falg debug(String infix) throws Exception {
		if (infix == null || infix.length() == 0) {
			return Falg.BREAK;
		}

		infix = infix.trim();
		if (DependencyTreeReader.isInfix(infix)) {
			String[] seg = Compiler.parse(infix);
			if (Constituent.isGrammaticalConstituent(seg)) {
				Constituent constituent;
				constituent = Constituent.compile(infix);

				System.out.println(constituent);
				System.out.println(constituent.infix());
				return Falg.CONTINUE;
			}

			if (tree != null && Utility.equals(tree.getLEX(), seg)) {
				tree = Compiler.compile(infix, tree.getPOS());
			} else {
				tree = Compiler.compile(infix);
				if (tree.pos == null) {
					tree.setPOS(POSTagger.instance.tag(tree.getSEG()));
					tree = SyntacticParser.instance.parse(tree);
				}
			}

			Filter filter = AnomalyInspecter.FilterSet.containsIrregulation(tree);
			if (filter != null) {
				System.out.println("Anomaly Inspected: " + filter.regulation);
				SyntacticTree _tree = Compiler.parseWithAdjustment(tree.infixExpression(), tree.getPOS());
				if (_tree != null) {
					tree = _tree;
					//					tree = Compiler.compile(infix);
				}
			}

			extract();
			return Falg.CONTINUE;
		}
		{
			String[] arr = null;
			if (infix.matches(".+==.+")) {
				arr = infix.split("==");
			} else if (infix.matches(".+!=.+")) {
				arr = infix.split("!=");
			} else if (infix.matches(".+=.+")) {
				arr = infix.split("=");
			} else if (infix.matches(".+/.+=.+")) {
				Utility.prependString(Utility.workingDirectory + "corpus/paraphrase/dev.txt", infix);
				RNNParaphrase.instance.establishTrainingCorpusForDevelopment();
				RNNParaphrase.instance.trainingCorpus();
				RNNParaphrase.instance.testRNNModelForDevelopment();

				return Falg.CONTINUE;
			} else if (infix.matches(".+/.+")) {
				arr = infix.split("/");
			}

			if (arr != null) {
				String str1 = arr[0].trim();
				String str2 = arr[1].trim();
				Sentence sent1 = new Sentence(str1);
				Sentence sent2 = new Sentence(str2);

				System.out.println("sent1 = " + sent1.tree());
				System.out.println("sent2 = " + sent2.tree());

				Constituent binarizedSimilarity = RNNParaphrase.instance.equalityDebug(sent2.tree(), sent1.tree());
				System.out.println("conversely, binarizedSimilarity = \n" + binarizedSimilarity);
				System.out.println("infix = " + binarizedSimilarity.infix());
				double similarity = RNNParaphrase.instance.similarity(sent2, sent1);
				System.out.println("similarity(str2, str1) = " + similarity);

				binarizedSimilarity = RNNParaphrase.instance.equalityDebug(sent1.tree(), sent2.tree());
				System.out.println("binarizedSimilarity = \n" + binarizedSimilarity);
				System.out.println("infix = " + binarizedSimilarity.infix());
				similarity = RNNParaphrase.instance.similarity(sent1, sent2);
				System.out.println("similarity(str1, str2) = " + similarity);

				return Falg.CONTINUE;
			}
		}

		switch (infix.toLowerCase()) {
		case "topic":
			bTopic = true;
			return Falg.BREAK;

		case "train":
			if (bCWS) {
				QASystem.instance.execute(new Runnable() {
					@Override
					public void run() {
						try {
							CWSTagger.instance.trainingCorpus();
							CWSTagger.instance = null;
							String segSerializedFile = Utility.workingDirectory + "models/seg.gz";
							CWSTagger.instance = new CWSTagger(segSerializedFile);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				bCWS = false;
			}
			if (bPOS) {
				QASystem.instance.execute(new Runnable() {

					@Override
					public void run() {
						try {
							POSTagger.instance.trainingCorpus();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				bPOS = false;
			}

			if (bDEP) {
				QASystem.instance.execute(new Runnable() {
					@Override
					public void run() {
						try {
							SyntacticParser.instance.trainingCorpus();

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				bDEP = false;
			}

			return Falg.BREAK;

		case "pos":
			System.out.println("input the regulations to perform part of speech (press enter to skip / confirm):");
			infix = buffer.readLine();
			if (infix.matches("[^/\\s]+")) {
				for (String s : AnomalyInspecter.findPossibleTags(infix)) {
					Utility.print(s);
				}
			} else {
				int err = 0;
				ArrayList<SyntacticTree> arr = AnomalyInspecter.pos(infix);

				System.out.println("error = " + arr.size());
				if (arr.isEmpty()) {
					return Falg.BREAK;
				}

				System.out.println("yes/ENTER = confirm to change, or no = cancel");
				String cmd = buffer.readLine();
				switch (cmd.toLowerCase()) {
				case "":
				case "yes":
					bPOS = true;
					FilterPOS sift = new AnomalyInspecter.FilterPOS(infix);
					sift.clearDict();
					for (int i = 0; i < arr.size(); ++i) {
						arr.set(i, sift.adjust(arr.get(i)));
					}
					DependencyTreeReader.addTrainingInstance(arr);

				case "no":
				default:
					return Falg.BREAK;
				}
			}

			bPOS = true;
			return Falg.BREAK;

		case "seg":
			bCWS = true;
			return Falg.BREAK;
		case "dep":
			System.out.println("input the regulations to perform dependency tree checking (press enter to skip / confirm):");
			infix = buffer.readLine().trim();

			if (infix.isEmpty()) {
				AnomalyInspecter.dep();

				bDEP = true;
			} else {
				int err = 0;
				ArrayList<SyntacticTree> arr = AnomalyInspecter.dep(infix);

				System.out.println("error = " + arr.size());

				if (arr.isEmpty()) {
					return Falg.BREAK;
				}
				System.out.println("yes/enter = confirm to change, or no = cancel");
				String cmd = buffer.readLine();

				switch (cmd.toLowerCase()) {
				case "":
				case "yes":
					AnomalyInspecter.FilterSet.add(infix);

					for (int i = 0; i < arr.size(); ++i) {
						arr.set(i, arr.get(i).adjust());
					}

					DependencyTreeReader.addTrainingInstance(arr);

					bDEP = true;
				case "no":
					return Falg.BREAK;
				default:
					System.out.println("invalid input, ignored.");
					return Falg.BREAK;
				}

			}

			return Falg.BREAK;
		case "cws":
			System.out.println("input the segmented sentence or the segmentation rules for Chinese Word Segmentation:");
			infix = buffer.readLine();
			String[] res = Utility.regexSingleton(infix, "([\\s\\S]+?)=([\\S\\s]+)");
			if (res != null) {
				ArrayList<SyntacticTree> correction = AnomalyInspecter.transform_corpus(res[1], res[2]);
				System.out.println("error = " + correction.size());
				System.out.println("yes/enter = confirm to change, or no = cancel");
				infix = buffer.readLine();
				switch (infix.toLowerCase()) {
				case "":
				case "yes":
					bCWS = true;
					DependencyTreeReader.addTrainingInstance(correction);
				case "no":
					return Falg.BREAK;
				default:
					System.out.println("invalid input, ignored.");
					return Falg.BREAK;
				}

			} else {
				String[] cws = CWSTagger.convertToSegmentation(infix);
				tree = SyntacticParser.instance.parse(cws, POSTagger.instance.tag(cws));
				DependencyTreeReader.addTrainingInstance(tree);
				extract();
			}
			bCWS = true;
			return Falg.BREAK;
		case "qa":
			RNNQAClassifier.instance.establishTrainingCorpus();
			//			if (BinarizedTree.bErrorDiscrepancy) {
			//				BinarizedTree.bErrorDiscrepancy = false;
			//				return Falg.BREAK;
			//			}
			RNNQAClassifier.instance.trainingCorpus();
			RNNQAClassifier.instance.testRNNModel();
			return Falg.BREAK;
		case "phatics":
			RNNPhaticsClassifier.instance.establishTrainingCorpus();
			RNNPhaticsClassifier.instance.trainingCorpus();
			RNNPhaticsClassifier.instance.testRNNModel();
			return Falg.BREAK;
		case "paraphrase": {
			RNNParaphrase.instance.establishTrainingCorpus();
			RNNParaphrase.instance.trainingCorpus();
			RNNParaphrase.instance.testRNNModel();
			return Falg.BREAK;
		}
		case "constituent": {
			ConstituentAbsorber.instance.establishTrainingCorpus();
			ConstituentAbsorber.instance.trainingCorpus();
			ConstituentAbsorber.instance.testRNNModel();
			return Falg.BREAK;
		}
		case "delete":
			System.out.println("input the sentence to be deleted:");
			infix = buffer.readLine().trim();
			ArrayList<SyntacticTree> list = new ArrayList<SyntacticTree>();
			for (SyntacticTree tree : new DependencyTreeReader()) {
				if (tree.unadornedExpression().equals(infix)) {
					System.out.println(tree);
					this.tree = tree;
				} else {
					list.add(tree);
				}
			}

			DependencyTreeReader.writeTrainingInstance(list);
			bDEP = true;

			return Falg.BREAK;

		case "test": {
			int sumNEUTRAL = 0;
			int errNEUTRAL = 0;

			for (String[] str : Utility.readFromExcel(Utility.workingDirectory + "corpus/TEST.xlsx", "NEUTRAL", 1)) {
				Sentence sent = new Sentence(str[0]);
				if (!QATYPE.NEUTRAL.equals(sent.qatype())) {
					++errNEUTRAL;
					System.out.println(sent + " is wrongly classified as " + sent.qatype());
				}
				++sumNEUTRAL;
			}

			int sumQUERY = 0;
			int errQUERY = 0;

			for (String[] str : Utility.readFromExcel(Utility.workingDirectory + "corpus/TEST.xlsx", "QUERY", 1)) {
				Sentence sent = new Sentence(str[0]);
				if (!QATYPE.QUERY.equals(sent.qatype())) {
					++errQUERY;
					System.out.println(sent + " is wrongly classified as " + sent.qatype());
				}
				++sumQUERY;
			}

			int sumREPLY = 0;
			int errREPLY = 0;

			for (String[] str : Utility.readFromExcel(Utility.workingDirectory + "corpus/TEST.xlsx", "REPLY", 1)) {
				Sentence sent = new Sentence(str[0]);
				if (!QATYPE.REPLY.equals(sent.qatype())) {
					++errREPLY;
					System.out.println(sent + " is wrongly classified as " + sent.qatype());
				}
				++sumREPLY;
			}

			int sumSYNONYMY = 0;
			int errSYNONYMY = 0;
			for (String[] str : Utility.readFromExcel(Utility.workingDirectory + "corpus/TEST.xlsx", "SYNONYMY", 2)) {
				double similarity = RNNParaphrase.instance.similarity(new Sentence(str[0]), new Sentence(str[1]));
				if (similarity < 0.8) {
					++errSYNONYMY;
					System.out.println(str[0]);
					System.out.println(str[1]);
					System.out.println("are wrongly considered as UNRELATED.");
				}
				++sumSYNONYMY;
			}

			int sumUNRELATED = 0;
			int errUNRELATED = 0;

			for (String[] str : Utility.readFromExcel(Utility.workingDirectory + "corpus/TEST.xlsx", "UNRELATED", 2)) {
				double similarity = RNNParaphrase.instance.similarity(new Sentence(str[0]), new Sentence(str[1]));
				if (similarity > 0.8) {
					++errUNRELATED;
					System.out.println(str[0]);
					System.out.println(str[1]);
					System.out.println("are wrongly considered as SYNONYMY.");

				}
				++sumUNRELATED;
			}

			DecimalFormat df = new DecimalFormat("00.00");
			System.out.println("correct percent for NEUTRAL   sentences = " + df.format((sumNEUTRAL - errNEUTRAL) * 1.0 / sumNEUTRAL * 100) + "%");
			System.out.println("correct percent for QUERY     sentences = " + df.format((sumQUERY - errQUERY) * 1.0 / sumQUERY * 100) + "%");
			System.out.println("correct percent for REPLY     sentences = " + df.format((sumREPLY - errREPLY) * 1.0 / sumREPLY * 100) + "%");
			System.out.println("correct percent for SYNONYMY  sentences = " + df.format((sumSYNONYMY - errSYNONYMY) * 1.0 / sumSYNONYMY * 100) + "%");
			System.out.println("correct percent for UNRELATED sentences = " + df.format((sumUNRELATED - errUNRELATED) * 1.0 / sumUNRELATED * 100) + "%");
			return Falg.CONTINUE;
		}
		}

		String pos[] = infix.split("\\s+");

		if (DependencyTreeReader.isPOSsequence(pos)) {
			if (tree == null) {
				System.out.println("illegal input for pos");
				return Falg.CONTINUE;
			}

			tree = SyntacticParser.instance.parse(tree.getLEX(), Utility.toUpperCase(pos)).adjust();
			DependencyTreeReader.addTrainingInstance(tree);
			bPOS = true;
			extract();
			return Falg.BREAK;
		}

		if (DependencyTreeReader.isDEPsequence(pos)) {
			if (tree == null) {
				System.out.println("illegal input for dep");
				return Falg.CONTINUE;
			}

			tree.setDEP(Utility.toLowerCase(pos));
			System.out.println(tree);
			DependencyTreeReader.addTrainingInstance(tree);
			bDEP = true;
			extract();
			return Falg.BREAK;
		}

		infix = infix.split("\\t")[0];
		if (infix.startsWith(";")) {
			infix = infix.substring(1);
		}
		tree = SyntacticParser.instance.parse(infix).adjust();

		extract();
		return Falg.CONTINUE;
	}

	BufferedReader buffer = Utility.readFromStdin();

	String interrogationMark = "？?;；。.";

	void training() throws Exception {
		for (;;) {
			String infix = null;
			try {
				infix = buffer.readLine();
				switch (debug(infix)) {
				case CONTINUE:
					break;
				case BREAK:
					continue;
				case EXIT:
					return;
				default:
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			Utility.workingDirectory = args[0];
		} else {

		}

		isEqual("测试有几个人", "测试是哪些人");
		SyntaxCoach syntaxCoach = new SyntaxCoach();

		syntaxCoach.training();
		//		

		//		InterrogativeStruct.constructDebug("我是在去哪网定的票");
		//		InterrogativeStruct.constructDebug("你们公司项目进展好不好？");

	}

	static boolean isEqual(String question0, String question) throws Exception {
		System.out.println("comparing strings:");
		System.out.println(question0);
		System.out.println(question);

		double s = RNNParaphrase.instance.similarity(question0, question);
		System.out.println("similarity = " + s);
		if (s < 0.9) {
			return false;
		}
		s = RNNParaphrase.instance.similarity(question, question0);
		System.out.println("similarity, vice versa = " + s);
		if (s < 0.9) {
			return false;
		}
		return true;
	}

	public static void learning() throws Exception {
		Synonym.instance.learning();
	}
}
