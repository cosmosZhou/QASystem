package com.robot.syntax;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import java.util.Arrays;
import com.util.Utility;
import com.util.Utility.Printer;
import com.util.Utility.Text;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.crf.CRFDatum;
import edu.stanford.nlp.ie.crf.CRFLabel;
import edu.stanford.nlp.ie.crf.CRFLogConditionalObjectiveFunction;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.optimization.DiffFunction;
import edu.stanford.nlp.optimization.Evaluator;
import edu.stanford.nlp.optimization.Minimizer;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.sequences.FeatureFactory;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.HashIndex;
import edu.stanford.nlp.util.Index;
import edu.stanford.nlp.util.MaxSizeConcurrentHashSet;
import edu.stanford.nlp.util.Timing;
import edu.stanford.nlp.util.Triple;
import edu.stanford.nlp.wordseg.CorpusDictionary;
import edu.stanford.nlp.wordseg.Gale2007ChineseSegmenterFeatureFactory;
import edu.stanford.nlp.wordseg.TagAffixDetector;


public class CWSTagger extends CRFClassifier<CoreLabel> {
	private static Logger log = Logger.getLogger(CWSTagger.class);
	public static CWSTagger instance;
	public static String segSerializedFile = Utility.workingDirectory + "models/seg.gz";
	static {
		try {
			log.info("loading CWSTagger at " + segSerializedFile);
			instance = new CWSTagger(segSerializedFile);
			if (instance.flags.sighanPostProcessing) {
				log.info("instance.flags.sighanPostProcessing = " + instance.flags.sighanPostProcessing);
				instance.flags.sighanPostProcessing = false;
			}

		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String[] tag(String sentence) {
		return segmentStringToArray(sentence.toUpperCase(), defaultReaderAndWriter);
	}

	public static void main(String... args) throws Exception {
		//		String path = Utility.workingDirectory + "models//seg.txt";
		//		ArrayList<String> arr = new Utility.StringReader(path).collect(new ArrayList<String>());
		//		combine(arr);
		//		Utility.writeString(path, arr);
		//		String str = "你们公司项目进展得何如、、";
		//		Character[] puncs = new Character[] { '\u3001', '\u3002', '\u3003', '\u3008', '\u3009', '\u300a', '\u300b', '\u300c', '\u300d', '\u300e', '\u300f', '\u3010', '\u3011', '\u3014', '\u3015' };
		//
		//		if (instance.flags.sighanPostProcessing) {
		//			log.info("instance.flags.sighanPostProcessing = " + instance.flags.sighanPostProcessing);
		//			instance.flags.sighanPostProcessing = false;
		//		}
		//		log.info(Utility.toString(puncs, "  ", null, puncs.length));
		//		String[] res = instance.tag(str);
		//		log.info(Utility.toString(res, "  ", null, res.length));
		instance.trainingCorpus();
	}

	static int[] convertFromStringIndex(String str) {
		int[] index = new int[str.length()];
		for (int i = 0; i < index.length; ++i) {
			index[i] = str.charAt(i) - '0';
		}
		return index;
	}

	static String convertFromIntegerIndex(int index[]) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < index.length; ++i) {
			s.append((char) (index[i] + '0'));
		}
		return s.toString();
	}

	static void test_seg() {

		// annotators=segment, ssplit, pos, ner, parse,
		// ssplit.boundaryTokenRegex=[.]|[!?]+|[。]|[！？]+,
		// segment.sighanCorporaDict=../models/segmenter/chinese,
		// segment.serDictionary=../models/segmenter/chinese/dict-chris6.ser.gz,
		// ner.applyNumericClassifiers=false,
		// pos.model=../models/pos-tagger/chinese-distsim/chinese-distsim.tagger,
		// parse.model=../models/lexparser/chinesePCFG.ser.gz,
		// customAnnotatorClass.segment=edu.stanford.nlp.pipeline.ChineseSegmenterAnnotator,
		// ner.useSUTime=false,
		// segment.model=../models/segmenter/chinese/ctb.gz,
		// segment.sighanPostProcessing=true,
		// ner.model=../models/ner/chinese.misc.distsim.crf.ser.gz
		// }
		StanfordCoreNLP pipeline = new StanfordCoreNLP("StanfordCoreNLP-chinese");

		System.out.println("StanfordCoreNLP-chinese = \n" + pipeline.getProperties());

		Annotation annotation = new Annotation("怎么可能什么都查不到啊");
		pipeline.annotate(annotation);
		pipeline.prettyPrint(annotation, System.out);
	}

	static public String[] convertToSegmentation(String str) throws Exception {
		String regex = Utility.Punctuation.replace("[", "\\[").replace("]", "\\]");
		str = Utility.replace(str, "[" + regex + "]", new Utility.Replacer() {
			@Override
			public String replace(String str) {
				return " " + str + " ";
			}
		});

		str = Utility.replace(str, "([\\d０-９]+ [\\.．：:] )+[\\d０-９]+", new Utility.Replacer() {
			@Override
			public String replace(String str) {
				return str.replaceAll(" ", "");
			}
		});

		str = Utility.replace(str, "([" + regex + "]\\s+)+", new Utility.Replacer() {
			@Override
			public String replace(String str) {
				String[] pu = str.split("\\s+");
				String res = "";
				res += pu[0];
				for (int i = 1; i < pu.length; ++i) {
					if (!pu[i - 1].equals(pu[i])) {
						res += " ";
					}
					res += pu[i];
				}
				return res + " ";
			}
		});
		str = str.trim();
		return str.split("\\s+");
	}

	public static String[] translateLabledSequence(String str) throws Exception {

		StringBuilder[] strBuilder = new StringBuilder[2];
		strBuilder[0] = new StringBuilder();
		strBuilder[1] = new StringBuilder();
		for (String s : convertToSegmentation(str)) {
			if (s.length() == 0) {
				log.info("str = " + str);
				continue;
			}

			// for mono-syllable words
			// disyllable / multi-syllable words

			strBuilder[0].append(s.charAt(0));
			strBuilder[1].append('0');

			for (int i = 1; i < s.length(); ++i) {
				strBuilder[0].append(s.charAt(i));
				strBuilder[1].append('1');
			}
		}

		String[] ret = { strBuilder[0].toString(), '0' + strBuilder[1].toString() };

		return ret;
	}

	static void combine(ArrayList<String> array) throws Exception {
		String dights[] = { "0０零〇", "1１壹一十", "2２贰二两", "3３叁三", "4４肆四", "5５伍五", "6６陆六", "7７柒七", "8８捌八", "9９玖九", };

		String dightString = "";
		for (String s : dights) {
			dightString += s;
		}

		String regex = "(.*)";
		//		regex += "([" + dightString + "])";		
		regex += "([" + dightString + "]+)";
		regex += " ([月日号时])";
		regex += "(.*)";
		log.info("regex = " + regex);
		for (int i = 0; i < array.size(); ++i) {
			String str = array.get(i);
			while (str.matches(regex)) {

				String[] res = Utility.regexSingleton(str, regex);
				//				for (int i = 1; i < res.length; ++i) {
				//					log.info("res[" + i + "] = " + res[i]);
				//				}
				log.info(str);
				str = res[1] + res[2] + res[3] + res[4];
				//				log.info("after padding space");
				log.info(str);
			}

			array.set(i, str);
			//			log.info(array.get(i));
		}
	}

	static void combineDigit(ArrayList<String> array) throws Exception {

		String dights[] = { "0０零〇", "1１壹一十", "2２贰二两", "3３叁三", "4４肆四", "5５伍五", "6６陆六", "7７柒七", "8８捌八", "9９玖九", };

		String dightString = "";
		for (String s : dights) {
			dightString += s;
		}

		String regex = "(.*)\\s";
		regex += "([" + dightString + "])\\s";
		regex += "([^" + dightString + Utility.Punctuation.replace("[", "\\[").replace("]", "\\]") + "\\s])\\s";
		regex += "(.*)";
		log.info("regex = " + regex);
		for (int i = 0; i < array.size(); ++i) {
			String str = " " + array.get(i) + " ";
			while (str.matches(regex)) {

				String[] res = Utility.regexSingleton(str, regex);
				//				for (int i = 1; i < res.length; ++i) {
				//					log.info("res[" + i + "] = " + res[i]);
				//				}
				log.info(str);
				str = res[1] + " " + res[2] + res[3] + " " + res[4];
				//				log.info("after padding space");
				log.info(str);
			}

			array.set(i, str.trim());
			//			log.info(array.get(i));
		}
		log.info("regex = " + regex);

	}

	static void shrinkByHalf(ArrayList<String> array) throws Exception {
		final String delimiterPunctuation = ",:;!?，。；、！？…「」〈（）";
		ArrayList<String> arrayCopy = new ArrayList<String>();
		for (int i = 0; i < array.size(); ++i) {
			String str = array.get(i);
			if (str.length() > 90) {
				//				log.info(str);
				int index = -1;
				for (int k = str.length() / 2, j = 0; k + j < str.length() && k - j >= 0; ++j) {
					if (delimiterPunctuation.indexOf(str.charAt(k + j)) > 0) {
						index = k + j;
						break;
					} else if (delimiterPunctuation.indexOf(str.charAt(k - j)) > 0) {
						index = k - j;
						break;
					}
				}
				System.out.println(str);
				String s1 = str.substring(0, index + 1);
				String s2 = str.substring(index + 1);
				int minLength = 20;
				if (s1.length() < minLength || s2.length() < minLength) {
					System.out.println(s1);
					System.out.println(s2);
				}
				System.out.println(s1);
				System.out.println(s2);
				arrayCopy.add(s1);
				arrayCopy.add(s2);
			} else {
				arrayCopy.add(str);
			}
		}
		array.clear();
		array.addAll(arrayCopy);
	}

	static void splitDots(ArrayList<String> array) throws Exception {
		String dights[] = { "0０零〇", "1１壹一十", "2２贰二两", "3３叁三", "4４肆四", "5５伍五", "6６陆六", "7７柒七", "8８捌八", "9９玖九", "百千万亿" };

		String dightString = "";
		for (String s : dights) {
			dightString += s;
		}

		String regex = "(.*)";
		regex += "([" + dightString + "]+)";
		regex += "[\\s]+(点)";
		regex += "([" + dightString + "]+)";
		regex += "(.*)";
		log.info("regex = " + regex);

		for (int i = 0; i < array.size(); ++i) {
			String str = array.get(i);
			while (str.matches(regex)) {

				String[] res = Utility.regexSingleton(str, regex);
				//				for (int i = 1; i < res.length; ++i) {
				//					log.info("res[" + i + "] = " + res[i]);
				//				}
				log.info(str);
				str = res[1] + res[2] + " " + res[3] + " " + res[4] + res[5];
				//				log.info("after padding space");
				log.info(str);
			}
			array.set(i, str);
		}
	}

	static void splitPercent(ArrayList<String> array) throws Exception {
		String dights[] = { "0０零〇", "1１壹一十", "2２贰二两", "3３叁三", "4４肆四", "5５伍五", "6６陆六", "7７柒七", "8８捌八", "9９玖九", "百千万亿" };

		String dightString = "";
		for (String s : dights) {
			dightString += s;
		}

		String regex = "(.*)";
		regex += "(百分之)";
		regex += "([" + dightString + "]+)";
		regex += "(.*)";
		log.info("regex = " + regex);

		for (int i = 0; i < array.size(); ++i) {
			String str = array.get(i);
			while (str.matches(regex)) {

				String[] res = Utility.regexSingleton(str, regex);
				//				for (int i = 1; i < res.length; ++i) {
				//					log.info("res[" + i + "] = " + res[i]);
				//				}
				log.info(str);
				str = res[1] + res[2] + " " + res[3] + res[4];
				//				log.info("after padding space");
				log.info(str);
			}
			array.set(i, str);
		}
	}

	static void removeSentence(ArrayList<String> array) throws Exception {
		ArrayList<String> arrayCopy = new ArrayList<String>();
		String regex = "(.*)";
		regex += "[pP]\\. [\\d]";
		regex += "(.*)";
		log.info("regex = " + regex);

		for (int i = 0; i < array.size(); ++i) {
			String str = array.get(i);
			if (str.matches(regex)) {
				log.info(str);
			} else
				arrayCopy.add(str);
		}
		array.clear();
		array.addAll(arrayCopy);
	}

	static void splitSuffix(ArrayList<String> array) throws Exception {

		String regex = "(.*)";
		regex += "[\\S]{2,}([者党])\\s";
		regex += "(.*)";
		log.info("regex = " + regex);

		for (int i = 0; i < array.size(); ++i) {
			String str = array.get(i);
			while (str.matches(regex)) {

				String[] res = Utility.regexSingleton(str, regex);
				//				for (int i = 1; i < res.length; ++i) {
				//					log.info("res[" + i + "] = " + res[i]);
				//				}
				log.info(str);
				str = res[1] + res[2] + " " + res[3] + res[4];
				//				log.info("after padding space");
				log.info(str);
			}
			array.set(i, str);
		}
	}

	static void format_corpus(String segFile) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		ArrayList<String> arr = new ArrayList<String>();
		HashSet<String> set = new HashSet<String>();
		for (String s : new Text(segFile)) {
			if (s.startsWith(";")) {
				s = s.substring(1);
			}
			String original = Utility.convertSegmentationToOriginal(s.split("\\s+"));
			original = Utility.removeEndOfSentencePunctuation(original);
			if (set.contains(original)) {
				log.info("duplicated instances detected : " + original);
			} else {
				arr.add(Utility.convertFromSegmentation(s.split("\\s+")));
				set.add(original);
			}
		}

		Utility.writeString(segFile, arr);
	}

	public void trainingCorpus() throws Exception {
		Collection<List<CoreLabel>> list = new ArrayList<List<CoreLabel>>();
		for (SyntacticTree tree : new DependencyTreeReader()) {
			String str = Utility.convertFromSegmentation(tree.getLEX());
			String[] res = translateLabledSequence(str);
			String originalString = res[0];
			int[] indexGold = convertFromStringIndex(res[1]);
			List<CoreLabel> document = makeObjectBankFromString(originalString);
			setGoldTags(document, indexGold);
			list.add(document);
		}

		flags.multiThreadGrad = Runtime.getRuntime().availableProcessors();
		train(list);

		clearCached();
		int error = 0;
		Printer printer = new Printer(Utility.workingDirectory + "/corpus/debug.seg.txt");
		for (SyntacticTree tree : new DependencyTreeReader()) {
			String str = Utility.convertFromSegmentation(tree.getLEX());
			String strOriginal = Utility.convertSegmentationToOriginal(str.split("\\s+"));
			String[] predRes = instance.tag(strOriginal);
			String[] goldRes = convertToSegmentation(str.toUpperCase());
			if (!Utility.equals(predRes, goldRes)) {
				System.out.println("training failed for ");
				System.out.println(str);
				System.out.println("strOriginal = \n" + strOriginal);
				System.out.println(Utility.toString(goldRes, "  ", null, goldRes.length));
				System.out.println(Utility.toString(predRes, "  ", null, predRes.length));

				error++;
			}
		}
		System.out.println("error = " + error);
		printer.close();
		saveToFile(segSerializedFile);
	}

	public void clearCached() {
		cliquePotentialFunction = null;
		map = null;
		this.nodeFeatureIndicesMap = null;
		this.edgeFeatureIndicesMap = null;
		//		this.featureIndexToTemplateIndex = null;
	}

	public void train(Collection<List<CoreLabel>> objectBankWrapper) {
		DocumentReaderAndWriter<CoreLabel> readerAndWriter = defaultReaderAndWriter;

		Timing timer = new Timing();

		Collection<List<CoreLabel>> docs = new ArrayList<>();
		for (List<CoreLabel> doc : objectBankWrapper) {
			docs.add(doc);
		}

		Collection<List<CoreLabel>> totalDocs = loadAuxiliaryData(docs, readerAndWriter);

		makeAnswerArraysAndTagIndexAmendment(totalDocs);

		long elapsedMs = timer.stop();
		log.info("Time to convert docs to feature indices: " + Timing.toSecondsString(elapsedMs) + " seconds");

		for (int i = 0; i <= flags.numTimesPruneFeatures; i++) {
			timer.start();
			Triple<int[][][][], int[][], double[][][][]> dataAndLabelsAndFeatureVals = documentsToDataAndLabels(docs);
			elapsedMs = timer.stop();
			log.info("Time to convert docs to data/labels: " + Timing.toSecondsString(elapsedMs) + " seconds");

			Evaluator[] evaluators = null;
			if (flags.numTimesPruneFeatures == i) {
				docs = null; // hopefully saves memory
			}
			// save feature index to disk and read in later
			File featIndexFile = null;

			// CRFLogConditionalObjectiveFunction.featureIndex = featureIndex;
			// int numFeatures = featureIndex.size();
			if (flags.saveFeatureIndexToDisk) {
				try {
					log.info("Writing feature index to temporary file.");
					featIndexFile = IOUtils.writeObjectToTempFile(featureIndex, "featIndex" + i + ".tmp");
					featureIndex = null;
				} catch (IOException e) {
					throw new RuntimeException("Could not open temporary feature index file for writing.");
				}
			}

			// first index is the number of the document
			// second index is position in the document also the index of the
			// clique/factor table
			// third index is the number of elements in the clique/window these
			// features are for (starting with last element)
			// fourth index is position of the feature in the array that holds
			// them
			// element in data[i][j][k][m] is the index of the mth feature
			// occurring
			// in position k of the jth clique of the ith document
			int[][][][] data = dataAndLabelsAndFeatureVals.first();
			// first index is the number of the document
			// second index is the position in the document
			// element in labels[i][j] is the index of the correct label (if it
			// exists) at position j in document i
			int[][] labels = dataAndLabelsAndFeatureVals.second();
			double[][][][] featureVals = dataAndLabelsAndFeatureVals.third();

			double[] oneDimWeights = trainWeightsConcise(data, labels, evaluators, i, featureVals);
			if (oneDimWeights != null) {
				this.weights = to2D(oneDimWeights, labelIndices, map);
			}

			// if (flags.useFloat) {
			// oneDimWeights = trainWeightsUsingFloatCRF(data, labels,
			// evaluators, i, featureVals);
			// } else if (flags.numLopExpert > 1) {
			// oneDimWeights = trainWeightsUsingLopCRF(data, labels, evaluators,
			// i, featureVals);
			// } else {
			// oneDimWeights = trainWeightsUsingDoubleCRF(data, labels,
			// evaluators, i, featureVals);
			// }

			// save feature index to disk and read in later
			if (flags.saveFeatureIndexToDisk) {
				try {
					log.info("Reading temporary feature index file.");
					featureIndex = IOUtils.readObjectFromFile(featIndexFile);
				} catch (Exception e) {
					throw new RuntimeException("Could not open temporary feature index file for reading.");
				}
			}

			if (i != flags.numTimesPruneFeatures) {
				dropFeaturesBelowThreshold(flags.featureDiffThresh);
				log.info("Removing features with weight below " + flags.featureDiffThresh + " and retraining...");
			}
		}
	}

	public CWSTagger(String fileSerialized) throws ClassCastException, IOException, ClassNotFoundException {
		super();
		Object[] res = Utility.loadFrom(fileSerialized, 13);
		int i = 0;
		this.weights = (double[][]) res[i++];
		this.featureIndex = (Index<String>) res[i++];
		labelIndices = (List<Index<CRFLabel>>) res[i++];
		classIndex = (Index<String>) res[i++];
		flags = (SeqClassifierFlags) res[i++];
		featureFactories = (List<FeatureFactory<CoreLabel>>) res[i++];
		windowSize = (int) res[i++];
		knownLCWords = (MaxSizeConcurrentHashSet<String>) res[i++];
		pad = (CoreLabel) res[i++];
		defaultReaderAndWriter = (DocumentReaderAndWriter<CoreLabel>) res[i++];
		plainTextReaderAndWriter = (DocumentReaderAndWriter<CoreLabel>) res[i++];
		Gale2007ChineseSegmenterFeatureFactory gale2007ChineseSegmenterFeatureFactory = (Gale2007ChineseSegmenterFeatureFactory) featureFactories.get(0);
		gale2007ChineseSegmenterFeatureFactory.taDetector = (TagAffixDetector) res[i++];
		gale2007ChineseSegmenterFeatureFactory.outDict = (CorpusDictionary) res[i++];
		flags.multiThreadGrad = Runtime.getRuntime().availableProcessors();
	}

	protected void makeAnswerArraysAndTagIndexAmendment(Collection<List<CoreLabel>> ob) {

		Set<String>[] featureIndices = new HashSet[windowSize];
		for (int i = 0; i < windowSize; i++) {
			featureIndices[i] = Generics.newHashSet();
		}

		Index<CRFLabel> labelIndex = labelIndices.get(windowSize - 1);

		Set<String>[] seenBackgroundFeatures = new HashSet[2];
		seenBackgroundFeatures[0] = Generics.newHashSet();
		seenBackgroundFeatures[1] = Generics.newHashSet();

		for (List<CoreLabel> doc : ob) {
			for (int j = 0, docSize = doc.size(); j < docSize; j++) {
				CRFDatum<List<String>, CRFLabel> d = makeDatum(doc, j, featureFactories);
				labelIndex.add(d.label());

				List<List<String>> features = d.asFeatures();
				for (int k = 0, fSize = features.size(); k < fSize; k++) {
					Collection<String> cliqueFeatures = features.get(k);
					if (k < 2 && flags.removeBackgroundSingletonFeatures) {
						String ans = doc.get(j).get(CoreAnnotations.AnswerAnnotation.class);
						boolean background = ans.equals(flags.backgroundSymbol);
						if (k == 1 && j > 0 && background) {
							ans = doc.get(j - 1).get(CoreAnnotations.AnswerAnnotation.class);
							background = ans.equals(flags.backgroundSymbol);
						}
						if (background) {
							for (String f : cliqueFeatures) {
								if (!featureIndices[k].contains(f)) {
									if (seenBackgroundFeatures[k].contains(f)) {
										seenBackgroundFeatures[k].remove(f);
										featureIndices[k].add(f);
									} else {
										seenBackgroundFeatures[k].add(f);
									}
								}
							}
						} else {
							seenBackgroundFeatures[k].removeAll(cliqueFeatures);
							featureIndices[k].addAll(cliqueFeatures);
						}
					} else {
						featureIndices[k].addAll(cliqueFeatures);
					}
				}
			}
		}

		//		for (int i = 0; i < windowSize; i++) {
		//			for (String feature : featureIndices[i]) {
		//				if (featureIndex.contains(feature)) {
		//					log.info("Feature " + feature + " is included in the featureIndex already.");
		//				} else {
		//					//					log.info("Feature " + feature + " is not included in the featureIndex yet.");
		//				}
		//			}
		//		}

		log.info("featureIndex.size() = " + featureIndex.size());
		log.info("weights.length = " + weights.length);
		if (weights.length != featureIndex.size()) {
			throw new RuntimeException("weights.length != featureIndex.size()");
		}

		HashMap<String, double[]> mapEmbedding = new HashMap<String, double[]>();
		for (String feature : featureIndex) {
			int index = featureIndex.indexOf(feature);
			if (weights[index].length == 2) {
				if (!featureIndices[0].contains(feature)) {
					log.info("Feature " + feature + " is a node feature not contained.");
				}
				//				featureIndices[0].add(feature);
			} else if (weights[index].length == 4) {
				if (!featureIndices[1].contains(feature)) {
					log.info("Feature " + feature + " is a edge feature not contained.");
				}
				//				featureIndices[1].add(feature);
			} else {
				throw new RuntimeException("weights[" + index + "].length = " + weights[index].length);
			}
			mapEmbedding.put(feature, weights[index]);
		}
		featureIndex.clear();
		int numFeatures = 0;
		for (int i = 0; i < windowSize; i++) {
			numFeatures += featureIndices[i].size();
		}
		log.info("numFeatures = " + numFeatures);

		map = new int[numFeatures];

		for (int i = 0; i < windowSize; i++) {
			Index<Integer> featureIndexMap = new HashIndex<>();

			featureIndex.addAll(featureIndices[i]);
			for (String str : featureIndices[i]) {
				int index = featureIndex.indexOf(str);
				if (index < 0)
					throw new RuntimeException("index < 0");
				map[index] = i;
				featureIndexMap.add(index);
			}
			// it seems like it only supports first order CRF
			if (i == 0) {
				nodeFeatureIndicesMap = featureIndexMap;
				// log.info("setting nodeFeatureIndicesMap, size="+nodeFeatureIndicesMap.size());
			} else {
				edgeFeatureIndicesMap = featureIndexMap;
				// log.info("setting edgeFeatureIndicesMap, size="+edgeFeatureIndicesMap.size());
			}
		}

		weights = new double[numFeatures][];
		for (String feature : featureIndex) {
			int index = featureIndex.indexOf(feature);
			int length = labelIndices.get(map[index]).size();

			double[] weight = mapEmbedding.get(feature);
			if (weight != null) {
				if (weight.length != length) {
					throw new RuntimeException("weight.length != length");
				}
				weights[index] = weight;
			} else {
				weights[index] = new double[length];
			}
		}

		//			for (int i = 0, fiSize = featureIndex.size(); i < fiSize; i++) {
		//				System.out.println(i + ": " + featureIndex.get(i));
		//			}
	}

	protected double[] trainWeightsConcise(int[][][][] data, int[][] labels, Evaluator[] evaluators, int pruneFeatureItr, double[][][][] featureVals) {

		CRFLogConditionalObjectiveFunction func = getObjectiveFunction(data, labels);
		cliquePotentialFunctionHelper = func;

		Minimizer<DiffFunction> minimizer = getMinimizer(pruneFeatureItr, evaluators);

		double[] initialWeights = null;

		initialWeights = to1D();

		log.info("numWeights: " + initialWeights.length);

		// check gradient
		if (flags.checkGradient) {
			if (func.gradientCheck()) {
				log.info("gradient check passed");
			} else {
				throw new RuntimeException("gradient check failed");
			}
		}
		this.weights = null;
		return minimizer.minimize(func, flags.tolerance, initialWeights);
	}

	public void saveToFile(String fileSerialized) throws ClassCastException, IOException, ClassNotFoundException {
		Gale2007ChineseSegmenterFeatureFactory gale2007ChineseSegmenterFeatureFactory = (Gale2007ChineseSegmenterFeatureFactory) featureFactories.get(0);

		Utility.saveTo(fileSerialized, weights, featureIndex, labelIndices, classIndex, flags, featureFactories, windowSize, knownLCWords, pad, defaultReaderAndWriter, plainTextReaderAndWriter, gale2007ChineseSegmenterFeatureFactory.taDetector, gale2007ChineseSegmenterFeatureFactory.outDict);
	}

	public double[] to1D() {
		int dimension = 0;
		for (int index : map) {
			dimension += labelIndices.get(index).size();
		}

		double[] newWeights = new double[dimension];

		int index = 0;
		for (int i = 0; i < weights.length; i++) {
			int length = labelIndices.get(map[i]).size();
			if (length != weights[i].length)
				throw new RuntimeException("length != weights[i].length");
			System.arraycopy(weights[i], 0, newWeights, index, length);
			index += length;
		}
		return newWeights;
	}

	public void setGoldTags(List<CoreLabel> document, int[] bestSequence) {
		for (int j = 0, docSize = document.size(); j < docSize; j++) {
			CoreLabel wi = document.get(j);
			String guess = classIndex.get(bestSequence[j + windowSize - 1]);
			wi.set(CoreAnnotations.AnswerAnnotation.class, guess);
		}
	}

	public List<CoreLabel> makeObjectBankFromString(String sentence) {
		ObjectBank<List<CoreLabel>> docs = makeObjectBankFromString(sentence, defaultReaderAndWriter);

		for (List<CoreLabel> doc : docs) {
			return doc;
		}
		return null;
	}

	public String[] segmentStringToArray(String sentence, DocumentReaderAndWriter<CoreLabel> readerAndWriter) {
		ObjectBank<List<CoreLabel>> docs = makeObjectBankFromString(sentence, readerAndWriter);

		StringWriter stringWriter = new StringWriter();
		PrintWriter stringPrintWriter = new PrintWriter(stringWriter);
		for (List<CoreLabel> doc : docs) {
			classify(doc);
			readerAndWriter.printAnswers(doc, stringPrintWriter);
			stringPrintWriter.println();
		}
		stringPrintWriter.close();
		String segmented = stringWriter.toString();

		return segmented.split("\\s+");
	}
}
