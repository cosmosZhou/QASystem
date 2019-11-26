package com.robot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.robot.QACouplet.Origin;
import com.robot.Sentence.Protagonist;
import com.robot.Sentence.QATYPE;
import com.robot.DateBase.MySQL;
import com.util.Utility;
import com.util.Utility.Couplet;
import com.util.Utility.PriorityQueue;
import com.util.Utility.Timer;

public class Repertoire {
	static QueryParser parser = new QueryParser(Version.LUCENE_40, "question", new IKAnalyzer());
	// parser.setDefaultOperator(QueryParser.AND_OPERATOR);
	static QueryParser parserTeletext = new QueryParser(Version.LUCENE_40, "teletext", new IKAnalyzer());

	Repertoire(final String company_pk) throws Exception {
		this.company_pk = company_pk;
		this.companyName = company_name();		
		synchronized (this) {
			MySQL.instance.new Invoker<Object>() {
				@Override
				protected Object invoke() throws Exception {
					for (ResultSet res : MySQL.instance.new Query("select DISTINCT(FAQID) from ecchatfaqcorpus where company_pk = '" + company_pk + "'")) {
						int id = res.getInt("FAQID");
						keyGenerator.register_key(id);
						clusters.put(id, null);
					}
					return null;
				}
			}.execute();
		}

		threshold = this.threshold();
		
		if (!"00000000000000000000000000000000".equals(company_pk)) {
			deletePhatics();
			saveToSQL();
			createKeywordInvertedIndexer();
		}
	}

	@Override
	public String toString() {
		String msg = "";
		msg += "company_pk = " + company_pk + " ";
		return msg;
	}

	String company_pk;
	String companyName;

	Utility.KeyGenerator keyGenerator = new Utility.KeyGenerator();
	HashMap<Integer, FAQ> clusters = new HashMap<Integer, FAQ>();
	// better to combine together.
	double threshold;
	int cntDialogue = 0;
	IndexSearcher searcher;
	IndexSearcher searcherTeletext;

	void setChanged() {
		for (Entry<Integer, FAQ> entry : this.clusters.entrySet()) {
			FAQ faq = entry.getValue();
			if (faq != null)
				faq.isChanged = true;
		}
	}

	FAQ getFAQ(final int id) throws Exception {
		FAQ faq = clusters.get(id);
		if (faq != null) {
			return faq;
		}

		synchronized (clusters) {
			faq = MySQL.instance.new Invoker<FAQ>() {
				@Override
				protected FAQ invoke() throws Exception {
					FAQ faq = new FAQ(id);
					for (ResultSet res : MySQL.instance.new Query("select * from ecchatfaqcorpus where company_pk = '" + company_pk + "' and faqid = " + id)) {
						try {
							String question = res.getString("question");
							String answer = res.getString("answer");

							QACouplet qa = new QACouplet(new Sentence(question, Protagonist.CUSTOMER), new Sentence(answer, Protagonist.OPERATOR), res.getDouble("coherence"), res.getTimestamp("time"), res.getString("respondent"), QACouplet.parseIntToOrigin(res.getInt("origin")));

							log.info("loading faqid = " + id + "\n" + qa);
							faq.add(qa);
						} catch (Exception e) {
							log.info("question = " + res.getString("question"));
							log.info("answer = " + res.getString("answer"));
							log.info("coherence = " + res.getDouble("coherence"));
							log.info("time = " + res.getTimestamp("time"));
							log.info("company_pk = " + company_pk);
							e.printStackTrace();
						}
					}
					return faq;
				}
			}.execute();
			if (faq.isEmpty()) {
				log.info("error occurred, faq is Empty for id = " + id);
				return null;
			}

			clusters.put(id, faq);
			if (!keyGenerator.isRregistered(id)) {
				log.info("impossible to occur, this.keyGenerator.isRregistered(" + id + ")");
				keyGenerator.register_key(id);
			}

			return faq;
		}
	}

	public void createKeywordInvertedIndexer() throws IOException {
		log.info("indexing company = " + company_pk);

		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, new IKAnalyzer());

		File indexDir = indexerFolder();
		File[] files = indexDir.listFiles();
		if (files == null || indexDir.listFiles().length == 0 || cntDialogue > 1000) {
			this.setChanged();
			iwc.setOpenMode(OpenMode.CREATE);
		} else {
			iwc.setOpenMode(OpenMode.APPEND);
		}

		FSDirectory dir = FSDirectory.open(indexDir);

		IndexWriter indexWriter = new IndexWriter(dir, iwc);
		//		indexWriter.commit();
		// log.info(dir + "created successfully");
		try {
			// add document to the indexer
			for (Entry<Integer, FAQ> entry : this.clusters.entrySet()) {
				Document document = new Document();
				FAQ faq = entry.getValue();
				if (faq == null || !faq.isChanged)
					continue;
				for (QACouplet QACouplet : faq) {
					log.info("indexing question = " + QACouplet.que.sentence);

					Field fieldQue = new Field("question", QACouplet.que.sentence, Store.NO, Field.Index.ANALYZED);

					// log.info("indexing answer = " +
					// QACouplet.answer.sentence);
					// Field fieldAns = new Field("question",
					// QACouplet.answer.sentence, Store.NO,
					// Field.Index.ANALYZED);
					// Sets the boost factor on this field.
					// fieldAns.setBoost(0.1f);
					// document.add(fieldAns);
					document.add(fieldQue);
				}
				log.info("add FAQID = " + faq.id + " to the document.");
				document.add(new IntField("FAQID", faq.id, Store.YES));

				indexWriter.addDocument(document);
				faq.isChanged = false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			this.searcher = null;
			try {
				indexWriter.close();
			} finally {
				if (IndexWriter.isLocked(dir)) {
					IndexWriter.unlock(dir);
				}
			}
		}
	}

	boolean validate() {
		int y = Integer.MIN_VALUE;
		for (Couplet.Integer pair : keyGenerator) {
			if (pair.x < pair.y)
				continue;
			if (pair.x > y) {
				y = pair.y;
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * update the threshold for search operation;
	 * 
	 * @param threshold
	 * @throws Exception
	 */
	public void updateThreshold(double threshold) throws Exception {
		this.threshold = threshold;
	}

	public void update(Conversation conversation) throws Exception {
		update(conversation.compile());
	}

	void updateTeletext(String pk, String title, String description, String content) throws Exception {
		synchronized (company_pk) {
			log.info("indexing company for teletext : " + company_pk);

			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, new IKAnalyzer());

			File indexDir = teletextFolder();
			File[] files = indexDir.listFiles();
			if (files == null || indexDir.listFiles().length == 0) {
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				iwc.setOpenMode(OpenMode.APPEND);
			}

			FSDirectory dir = FSDirectory.open(indexDir);

			IndexWriter indexWriter = new IndexWriter(dir, iwc);

			try {
				// add document to the indexer
				Document document = new Document();

				log.info("indexing teletext = " + title);

				Field fieldTitle = new Field("teletext", title, Store.NO, Field.Index.ANALYZED);
				document.add(fieldTitle);

				Field fieldDescription = new Field("teletext", description, Store.NO, Field.Index.ANALYZED);
				fieldDescription.setBoost(0.9f);
				document.add(fieldDescription);

				Field fieldContent = new Field("teletext", content, Store.NO, Field.Index.ANALYZED);
				fieldContent.setBoost(0.5f);
				document.add(fieldContent);

				log.info("add teletext = " + pk + " to the document.");
				document.add(new StringField("pk", pk, Store.YES));

				indexWriter.addDocument(document);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

				this.searcherTeletext = null;
				try {
					indexWriter.close();
				} finally {
					if (IndexWriter.isLocked(dir)) {
						IndexWriter.unlock(dir);
					}
				}
			}
		}
	}

	void update(String content) throws Exception {
		update(new Conversation(content));
	}

	//procedure for knowledge updating
	public void update(ArrayList<QACouplet> array) throws Exception {
		//remove redundant pairs
		HashSet<QACouplet> set = new HashSet<QACouplet>();
		for (int i = array.size() - 1; i >= 0; --i) {
			QACouplet qa = array.get(i);
			if (set.contains(qa)) {
				array.remove(i);
			} else {
				set.add(qa);
			}
		}

		synchronized (company_pk) {
			log.info("update this.company_pk = " + company_pk);

			int keys[] = new int[array.size()];
			int i = 0;
			for (QACouplet qACouplet : array) {
				int key = keyGenerator.generate_key();
				FAQ faq = new FAQ(key, qACouplet);
				faq.isChanged = true;
				// log.info("clusters.size = " + clusters.size());
				clusters.put(key, faq);
				keys[i++] = key;

				switch (qACouplet.origin) {
				case ROBOT_RESERVOIR:
					++cntDialogue;
					break;
				case SYSTEM_RESERVOIR:
					cntDialogue += 7;
					break;
				case INDIVIDUAL_RESERVOIR:
					cntDialogue += 7;
					break;
				case SUPERVISOR_RESERVOIR:
					cntDialogue += 49;
					break;
				}
			}

			for (int key : keys) {
				KruskalAgglomerativeClustering(clusters.get(key));
			}

			//		for (QACouplet qa : array) {
			//			try {
			//				semanticOntology(qa);
			//			} catch (Exception e) {
			//				log.info(e.getMessage());
			//				// e.printStackTrace();
			//			}
			//		}

			saveToSQL();
			createKeywordInvertedIndexer();
			if (cntDialogue > 1000) {
				cntDialogue = 0;
			}
		}
	}

	/**
	 * Kruskal is a greedy algorithm for finding the minimum spanning tree.
	 * perform Agglomerative Clutering
	 * 
	 * @param qACouplet
	 * @throws Exception
	 */

	public void KruskalAgglomerativeClustering(FAQ faq) throws Exception {
		QACouplet qACouplet = faq.epitome();

		ArrayList<Utility.Couplet<Integer, Float>> indices = getRecommendedFAQ(qACouplet.que.toString(), 10);
		Queue<EdgeOfFAQ> edges = new LinkedList<EdgeOfFAQ>();
		for (Utility.Couplet<Integer, Float> faqID : indices) {
			FAQ f = getFAQ(faqID.x);
			if (f == null) {
				log.info("retrieved faq is null, faqID = " + faqID);
				continue;
			}
			try {
				EdgeOfFAQ edge = new EdgeOfFAQ(f, faq);
				if (edge.similarity >= THRESHOLD)
					edges.add(edge);
			} catch (Exception e) {
				log.info(e.getMessage());
			}
		}

		while (edges.size() != 0) {
			EdgeOfFAQ best = Utility.max(edges);
			edges.remove(best);

			QACouplet supervisedEpitomeX = best.x.supervisedEpitome();
			QACouplet supervisedEpitomeY = best.y.supervisedEpitome();
			if (supervisedEpitomeX != null && supervisedEpitomeY != null) {
				log.info("two similar supervised QA couplets haved occurred. it might be an error from the supervisor");
				log.info(supervisedEpitomeX.toString());
				log.info(supervisedEpitomeY.toString());
				if (supervisedEpitomeX.similarity(supervisedEpitomeY) < 0.8) {
					// they are not similar enough, this might be an error
					// from the robot; so these two faqs should not be combined;
					continue;
				}
				supervisedEpitomeX.origin = Origin.ROBOT_RESERVOIR;
			}

			removeEmptyFAQ(best.y);
			best.x.merge(best.y);

			Queue<EdgeOfFAQ> _edges = new LinkedList<EdgeOfFAQ>();
			for (EdgeOfFAQ e : edges) {
				if (best.x == e.x) {
					log.info("error detected: f == faq");
					continue;
				}
				EdgeOfFAQ edge = new EdgeOfFAQ(e.x, best.x);
				if (edge.similarity >= THRESHOLD)
					_edges.add(edge);
			}
			edges = _edges;
		}
	}

	void print() {
		int sum = 0;
		for (Map.Entry<Integer, FAQ> entry : clusters.entrySet()) {
			sum += entry.getValue().size();
			log.info("key = " + entry.getKey() + " frequency = " + entry.getValue().size());

			for (QACouplet Sentence : entry.getValue()) {
				log.info(Sentence);
			}
		}

		log.info("clusters.size() = " + clusters.size());
		log.info("total sum = " + sum);
		// log.info("total sum = " + size);
		log.info("clusters rate = " + clusters.size() * 1.0 / sum);
	}

	void removeEmptyFAQ(FAQ faq) {
		try {
			keyGenerator.unregister_key(faq.id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info(e.getMessage());
		}
		clusters.remove(faq.id);
		if (clusters.containsKey(faq.id)) {
			log.info("error detected: !clusters.containsKey(" + faq.id + ")");
		}
	}

	public void saveToSQL() throws Exception {
		MySQL.instance.new Invoker() {
			@Override
			protected Object invoke() throws Exception {
				int sum = 0;
				HashSet<FAQ> setToBeDeleted = new HashSet<FAQ>();
				for (Map.Entry<Integer, FAQ> entry : clusters.entrySet()) {
					FAQ faq = entry.getValue();
					if (faq == null)
						continue;
					sum += faq.size();

					if (!faq.isChanged) {
						continue;
					}

					log.info("key = " + entry.getKey() + " actual frequency = " + faq.size());

					MySQL.instance.insert(faq, company_pk);
					// no need to set this value, for it will be reset when
					// indexing.
					// cluster.isChanged = false;
					if (faq.size() == 0) {
						// ConcurrentModificationException will be triggered if
						// it is deleted here. so deletion should be delayed
						// after the for loop;
						setToBeDeleted.add(faq);
					}
				}

				for (FAQ faq : setToBeDeleted) {
					removeEmptyFAQ(faq);
				}

				log.info("clusters.size() = " + clusters.size());
				log.info("total sum = " + sum);
				// log.info("clusters rate = " + clusters.size() * 1.0 / sum);
				MySQL.instance.insert(company_pk, new Date(), clusters.size());
				return null;
			}

		}.execute();
	}

	double threshold() throws Exception {
		return MySQL.instance.new Invoker<Double>() {
			@Override
			protected Double invoke() throws Exception {
				double threshold;
				MySQL.Query query;

				String sql = "select threshold from ecoperatorbasicsettings where companypk = " + "\'" + company_pk + "\'";
				query = MySQL.instance.new Query(sql);
				// log.info("sql = " + sql);
				if (query.hasNext()) {
					threshold = query.next().getInt("threshold");
					threshold /= 100;
					if (query.hasNext()) {
						log.info("error detected: multiple thresholds for company_pk");
						throw new Exception("multiple thresholds for company_pk " + company_pk + " detected");
					}
				} else {
					log.info("error detected: " + "threshold for company_pk " + company_pk + " not found");
					threshold = 0.6;
					// throw new Exception("threshold for company_pk " +
					// company_pk + " not found");
				}
				// log.info("reading time interval for threshold ");
				// timer.cease();
				return threshold;
			}
		}.execute();
	}

	String company_name() throws Exception {
		return MySQL.instance.new Invoker<String>() {
			@Override
			protected String invoke() throws Exception {
				String name;
				MySQL.Query query;

				String sql = "select EXT2 from eccompany where pk = " + "\'" + company_pk + "\'";
				query = MySQL.instance.new Query(sql);
				// log.info("sql = " + sql);
				if (query.hasNext()) {
					name = query.next().getString("EXT2");
					query.close();
				} else {
					name = "company" + company_pk;
				}
				return name;
			}
		}.execute();
	}

	public void set_threshold(final double threshold) throws Exception {
		MySQL.instance.new Invoker() {
			@Override
			protected Object invoke() throws Exception {
				String sql = "update ecoperatorbasicsettings threshold = " + threshold * 100 + " where companypk = " + "\'" + company_pk + "\'";
				MySQL.instance.execute(sql);
				// log.info("reading time interval for threshold ");
				// timer.cease();
				return null;
			}
		}.execute();
	}

	/**
	 * a quintuplet structure for an answer;
	 * 
	 * @author Cosmos
	 *
	 */
	public class AnsQuintuple implements Comparable<AnsQuintuple> {
		AnsQuintuple(Sentence answer, int id, double confidence, Origin origin, String respondent) {
			this.answer = answer;
			this.faqid = id;
			this.confidence = confidence;
			this.origin = origin;
			this.respondent = respondent;
		}

		public Sentence answer;
		public int faqid;
		public double confidence;
		public Origin origin;
		public String respondent;

		@Override
		public int compareTo(AnsQuintuple o) {
			return Double.compare(confidence, o.confidence);
		}
	}

	static double average(double retrievalScore, double similarity, double coherence) {
		double confidence = 0.1 * retrievalScore * retrievalScore + 0.4 * coherence * coherence + 0.5 * similarity * similarity;
		confidence = Math.sqrt(confidence);		
		return confidence;
	}

	/**
	 * the structure to store the result of a search by keywords
	 * 
	 * @author Cosmos
	 *
	 */
	public static class SearchResult implements Comparable<SearchResult> {
		SearchResult(String question, float score, ArrayList<Integer> index) {
			this.question = question;
			this.score = score;
			this.index = index;
		}

		public String question;
		public float score;
		/**
		 * record the positions of each keyword that should be marked red.
		 */
		public ArrayList<Integer> index;

		@Override
		public int compareTo(SearchResult o) {
			if (question.equals(o.question))
				return 0;
			int cmp = Double.compare(score, o.score);
			if (cmp != 0)
				return cmp;
			return question.compareTo(o.question);
		}
	}

	/**
	 * 
	 * @param questionOriginal
	 *            = the keyword to search for
	 * @return
	 * @throws Exception
	 */
	public static String regexPunctuation = "[" + Utility.sPunctuation.replace("[", "\\[").replace("]", "\\]") + "]";

	public TreeSet<SearchResult> searchForQuestionByKeywords(String questionOriginal) throws Exception {
		TreeSet<SearchResult> set = new TreeSet<SearchResult>();
		String question = Conversation.format(questionOriginal);
		if (question == null)
			return set;

		question = question.replaceAll(regexPunctuation, "");

		ArrayList<Utility.Couplet<Integer, Float>> arr = getRecommendedFAQ(question, 10);
		log.info("recommended FAQs: " + Utility.toString(arr));

		for (Utility.Couplet<Integer, Float> faqid : arr) {
			FAQ faq = getFAQ(faqid.x);
			if (faq == null) {
				continue;
			}

			QACouplet couple = faq.supervisedEpitome();

			if (couple == null)
				continue;
			ArrayList<Integer> integerArrayList = new ArrayList<Integer>();

			String sentence = couple.que.sentence;
			for (int i = 0; i < sentence.length(); ++i) {
				if (question.indexOf(sentence.charAt(i)) >= 0) {
					integerArrayList.add(i);
				}
			}

			if (!integerArrayList.isEmpty()) {
				set.add(new SearchResult(sentence, faqid.y, integerArrayList));
			}
		}

		log.info("res.size() = " + set.size());
		for (SearchResult searchResult : set) {
			log.info("searchResult = " + searchResult.question);
		}
		return set;
	}

	/**
	 * automatic response for the client side
	 * 
	 * @param questionOriginal
	 * @return
	 * @throws Exception
	 */
	public String automaticResponse(String questionOriginal) throws Exception {
		final String question = Conversation.format(questionOriginal);
		if (question == null)
			return null;

		Sentence sentence = new Sentence(question, Protagonist.CUSTOMER);
		return automaticResponse(sentence);
	}

	public String automaticResponseRandomly(String questionOriginal) throws Exception {
		final String question = Conversation.format(questionOriginal);
		if (question == null)
			return null;

		Sentence sentence = new Sentence(question, Protagonist.CUSTOMER);
		return automaticResponseRandomly(sentence);
	}

	/**
	 * automatic response for the client side
	 * 
	 * @param questionOriginal
	 * @return
	 * @throws Exception
	 */
	public String automaticResponse(Sentence sentence) throws Exception {
		Utility.Timer timer = new Utility.Timer();

		ArrayList<Utility.Couplet<Integer, Float>> arr = getRecommendedFAQ(sentence.sentence, 5);
		log.info("time span for getRecommendedFAQ ");
		timer.report();

		log.info("recommended FAQs: " + Utility.toString(arr));
	
		log.info("syntactic tree = \n" + sentence.tree());

		log.info("time span for Sentence analysis ");
		timer.report();

		DecimalFormat df = new DecimalFormat("0.000");

		String answer = null;
		double confidence = 0;

		for (Utility.Couplet<Integer, Float> faqid : arr) {
			FAQ faq = getFAQ(faqid.x);

			if (faq == null) {
				continue;
			}

			QACouplet epitome = faq.supervisedEpitome();
			if (epitome == null) {
				continue;
			}

			for (QACouplet qACouplet : faq) {
				double similarity = qACouplet.que.questionSimilarity(sentence);
				if (similarity > confidence) {
					confidence = similarity;
				}

				log.info("analyzing database question = " + qACouplet.que);
				log.info("analyzing database answer   = " + qACouplet.ans);

				log.info(df.format(similarity) + "\t\t......similarity between user question and database question");
				if (confidence >= 0.9) {
					break;
				}
			}

			if (confidence >= 0.9) {
				answer = epitome.ans.sentence;
				break;
			}
		}

		log.info("time span for similarity analysis ");
		timer.report();

		return answer;
		//		if (answer != null) {
		//			return answer;
		//		}
		//
		//		return performLogicalAnalysis(sentence);
	}

	/**
	 * automatic response for the client side Randomly select an answer from the
	 * correct answers.
	 * 
	 * @param questionOriginal
	 * @return
	 * @throws Exception
	 */
	public String automaticResponseRandomly(Sentence sentence) throws Exception {
		ArrayList<Utility.Couplet<Integer, Float>> arr = getRecommendedFAQ(sentence.sentence, 5);
		log.info("recommended FAQs: " + Utility.toString(arr));

		// log.info("time span for getRecommendedFAQ ");
		// timer.cease();
		// timer.start();

		log.info("syntactic tree = \n" + sentence.tree());
		// log.info("time span for Sentence analysis ");
		// timer.cease();
		// timer.start();

		DecimalFormat df = new DecimalFormat("0.000");

		ArrayList<String> answerList = new ArrayList<String>();
		for (Utility.Couplet<Integer, Float> faqid : arr) {
			FAQ faq = getFAQ(faqid.x);

			if (faq == null) {
				continue;
			}

			for (QACouplet qACouplet : faq) {
				double similarity = qACouplet.que.questionSimilarity(sentence);
				log.info("analyzing database question = " + qACouplet.que);
				log.info("analyzing database answer   = " + qACouplet.ans);

				log.info(df.format(similarity) + "\t\t......similarity between user question and database question");
				if (similarity < 0.9) {
					continue;
				}

				answerList.add(qACouplet.ans.toString());
			}
		}

		log.info("answerList.size() = " + answerList.size());
		if (answerList.size() > 0) {
			Random random = new Random();
			int index = random.nextInt(answerList.size());
			log.info("random index = " + index);
			return answerList.get(index);
		}
		return null;
		//		return performLogicalAnalysis(sentence);
	}

	final static int maxRecommendation = 5;

	public ArrayList<Couplet<String, Float>> searchTeletext(final String questionOriginal) throws Exception {
		//		Timer timer = new Timer();
		//		log.info("questionOriginal = " + questionOriginal);
		final String question = Conversation.format(questionOriginal);
		if (question == null)
			return new ArrayList<Couplet<String, Float>>();

		//		log.info("question = " + question);
		// this.print();
		ArrayList<Couplet<String, Float>> arr = getRecommendedFAQTeletext(question, 5);
		log.info("recommended FAQs: " + Utility.toString(arr));

		return arr;
	}

	/**
	 * recommend 5 answers for the agent side
	 * 
	 * @param questionOriginal
	 * @return
	 * @throws Exception
	 */
	public ArrayList<AnsQuintuple> query(final String questionOriginal) throws Exception {
		Timer timer = new Timer();
		//		log.info("questionOriginal = " + questionOriginal);
		ArrayList<AnsQuintuple> res = new ArrayList<AnsQuintuple>();
		final String question = Conversation.format(questionOriginal);
		if (question == null)
			return res;

		//		log.info("question = " + question);
		// this.print();
		ArrayList<Utility.Couplet<Integer, Float>> arr = getRecommendedFAQ(question, 5);
		log.info("recommended FAQs: " + Utility.toString(arr));

		Sentence sentence = new Sentence(question, Protagonist.CUSTOMER);

		log.info("syntactic tree = \n" + sentence.tree());
//		log.info("semantic vector = \n" + sentence.interrogativeStruct());

		DecimalFormat df = new DecimalFormat("0.000");

		log.info(df.format(threshold) + "\t\t......threshold for selecting in the company = " + company_pk);
		// log.info("time span for database ");
		// timer.cease();
		// timer.start();

		PriorityQueue<AnsQuintuple> priorityQueue = new PriorityQueue<AnsQuintuple>();
		for (Utility.Couplet<Integer, Float> faqid : arr) {
			FAQ faq = getFAQ(faqid.x);

			if (faq == null) {
				continue;
			}

			double score = faqid.y;
			log.info(df.format(score) + "\t\t......retrieval score from lucene");

			QACouplet newest = faq.newest();
			int maxIteration = 0;
			for (QACouplet qACouplet : faq) {
				if (++maxIteration > maxRecommendation)
					break;

				log.info(df.format(qACouplet.frequency) + "\t\t......fortnight(s) half life of decay as for faq = " + faq.id);

				double similarity = qACouplet.que.questionSimilarity(sentence);
				double efficacy = qACouplet.efficacy(newest);
				double confidence = average(score, similarity, qACouplet.coherence) * efficacy;
				if (confidence < threshold)
					continue;

				log.info("analyzing database question = " + qACouplet.que);
				log.info("analyzing database answer   = " + qACouplet.ans);

				log.info(df.format(similarity) + "\t\t......similarity between user question and database question");
				log.info(df.format(qACouplet.coherence) + "\t\t......coherence between database question and answer");
				log.info(df.format(efficacy) + "\t\t......efficacy with respect to time");
				log.info(df.format(confidence) + "\t\t......confidence in the final analysis\n\n");

				priorityQueue.add(new AnsQuintuple(qACouplet.ans, faq.id, confidence, qACouplet.origin, qACouplet.respondent));
			}
		}

		// log.info("priorityQueue.size = " + priorityQueue.size());
		HashSet<String> set = new HashSet<String>();

		while (res.size() < maxRecommendation) {
			AnsQuintuple pSentence = priorityQueue.poll();
			if (pSentence == null)
				break;

			if (!set.contains(pSentence.answer.sentence)) {
				res.add(pSentence);
				set.add(pSentence.answer.sentence);
			}
		}

		if (res.isEmpty()) {
			MySQL.instance.new Invoker() {
				@Override
				protected Object invoke() throws Exception {
					MySQL.instance.reportUnknownQuestion(company_pk, question, Utility.toString(new Date()));
					return null;
				}
			}.execute();
		}

		log.info("recommended answers:");
		for (AnsQuintuple t : res) {
			log.info(df.format(t.confidence) + "\t\t......" + t.answer);
		}

		//		for (int i = 0; i < res.size(); ++i) {
		//			int J = -1;
		//			for (int j = i + 1; j < res.size(); ++j) {
		//				double answerSimilarity = res.get(i).answer.answerSimilarity(res.get(j).answer);
		//				if (answerSimilarity >= 0.6) {
		//					J = j;
		//					break;
		//				}
		//			}
		//
		//			if (J >= i + 2) {
		//				int j = i + 1;
		//				for (; j < J; ++j) {
		//					res.get(j).confidence = res.get(j + 1).confidence;
		//				}
		//				res.get(j).confidence = res.get(i + 1).confidence;
		//				res.add(i + 1, res.remove(J));
		//			}
		//		}
		//
		log.info("time span for search analysis ");
		timer.report();

		return res;
	}

	IndexSearcher searcher() {
		if (searcher != null)
			return searcher;

		synchronized (company_pk) {
			File indexDir = indexerFolder();
			File[] files = indexDir.listFiles();
			if (files == null || files.length == 0) {
				return null;
			}

			try {
				searcher = new IndexSearcher(IndexReader.open(FSDirectory.open(indexDir)));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("Exception caught in getIndexSearcher " + e.getMessage());
				e.printStackTrace();
			}
			return searcher;
		}
	}

	IndexSearcher searcherTeletext() {
		if (searcherTeletext != null)
			return searcherTeletext;

		synchronized (company_pk) {
			File indexDir = teletextFolder();
			File[] files = indexDir.listFiles();
			if (files == null || files.length == 0) {
				return null;
			}

			try {
				searcherTeletext = new IndexSearcher(IndexReader.open(FSDirectory.open(indexDir)));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("Exception caught in searcherTeletext " + e.getMessage());
				e.printStackTrace();
			}
			return searcherTeletext;
		}
	}

	/**
	 * get top n faqs from application from lucene;
	 * 
	 * @param question
	 * @param nBest
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */

	public ArrayList<Utility.Couplet<Integer, Float>> getRecommendedFAQ(String question, int nBest) throws IOException {

		ArrayList<Utility.Couplet<Integer, Float>> list = new ArrayList<Utility.Couplet<Integer, Float>>();
		IndexSearcher searcher = searcher();
		if (searcher == null) {
			return list;
		}
		org.apache.lucene.search.Query cnQuery;
		try {
			cnQuery = parser.parse(question.replaceAll("\\pP|\\pS", " ").trim());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info("Exception caught in getRecommendedFAQ when parsing " + question);
			//			e.printStackTrace();
			return list;
		}

		// cnQuery.setBoost(1.7f);

		TopDocs topDocs = searcher.search(cnQuery, nBest);
		ScoreDoc[] hits = topDocs.scoreDocs;

		float maxScore = topDocs.getMaxScore();
		if (maxScore < 1)
			maxScore = 1;
		int prevID = -1;
		for (int i = 0; i < hits.length; i++) {
			float score = (float) Math.pow(hits[i].score / maxScore, 0.25);

			int id = searcher.doc(hits[i].doc).getField("FAQID").numericValue().intValue();
			if (id != prevID) {
				list.add(new Utility.Couplet<Integer, Float>(id, score));
				prevID = id;
			}
		}

		return list;
	}

	public ArrayList<Utility.Couplet<String, Float>> getRecommendedFAQTeletext(String question, int nBest) throws IOException {

		ArrayList<Utility.Couplet<String, Float>> list = new ArrayList<Utility.Couplet<String, Float>>();
		IndexSearcher searcher = searcherTeletext();
		if (searcher == null) {
			return list;
		}
		org.apache.lucene.search.Query cnQuery;
		try {
			cnQuery = parserTeletext.parse(question.replaceAll("\\pP|\\pS", " ").trim());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info("Exception caught in getRecommendedFAQTeletext when parsing " + question);
			//			e.printStackTrace();
			return list;
		}

		TopDocs topDocs = searcher.search(cnQuery, nBest);
		ScoreDoc[] hits = topDocs.scoreDocs;

		float maxScore = topDocs.getMaxScore();
		if (maxScore < 1)
			maxScore = 1;
		String prevID = null;
		for (int i = 0; i < hits.length; i++) {
			float score = (float) Math.pow(hits[i].score / maxScore, 0.25);

			String id = searcher.doc(hits[i].doc).getField("pk").stringValue();
			if (id != null && !id.equals(prevID)) {
				list.add(new Utility.Couplet<String, Float>(id, score));
				prevID = id;
			}
		}

		return list;
	}

	public File indexerFolder() {
		return new File(Utility.workingDirectory + "models/index/" + this.company_pk);
	}

	public File teletextFolder() {
		return new File(Utility.workingDirectory + "models/teletext/" + this.company_pk);
	}

	public static void main(String[] args) throws Exception {
		String str = "<a href='http://www.qq.com'>QQ</a><script>";
		/**
		 * apache的StringEscapeUtils进行转义
		 */
		//&lt;a href='http://www.qq.com'&gt;QQ&lt;/a&gt;&lt;script&gt;
		System.out.println(org.apache.commons.lang.StringEscapeUtils.escapeHtml(str));

		/**
		 * apache的StringEscapeUtils进行还原
		 */
		//<a href='http://www.qq.com'>QQ</a><script>
		System.out.println(org.apache.commons.lang.StringEscapeUtils.unescapeHtml("&lt;a href='http://www.qq.com'&gt;QQ&lt;/a&gt;&lt;script&gt;"));
	}

	public static Logger log = Logger.getLogger(Repertoire.class);
	static final double THRESHOLD = 0.8;

	/**
	 * learning from the system reservoir or individual reservoir, extracting
	 * information of the respondent, time, etc.
	 * 
	 * @param companyPk
	 * @throws Exception
	 */

	public void learningFromReservoir() throws Exception {
		ArrayList<QACouplet> arr = MySQL.instance.new Invoker<ArrayList<QACouplet>>() {
			@Override
			protected ArrayList<QACouplet> invoke() throws Exception {
				ArrayList<QACouplet> arr = new ArrayList<QACouplet>();
				for (ResultSet res : MySQL.instance.new Query("select content, title, storetype, inserttime, updatetime, operatorpk from eccommonstored where storefiletype = 1 and isdelete = 0 and companypk = '" + company_pk + "'")) {
					Date time = res.getDate("updatetime");
					if (time == null) {
						time = res.getDate("inserttime");
					}

					Origin origin = QACouplet.parseIntToOrigin(res.getInt("storetype"));
					String respondent = null;
					if (origin == Origin.INDIVIDUAL_RESERVOIR) {
						for (ResultSet result : MySQL.instance.new Query("SELECT USERNAME FROM ecoperator where PK = '" + res.getString("operatorpk") + "'")) {
							respondent = result.getString("USERNAME").split("-")[1];
						}

						if (respondent == null)
							log.info("respondent == null");
					} else {
						// throw new Exception("respondent == null");
					}

					arr.add(new QACouplet(res.getString("title"), res.getString("content"), 1.0, time, respondent, origin));
				}
				return arr;
			}
		}.execute();

		update(arr);
	}

	/**
	 * learning from the system reservoir or individual reservoir, extracting
	 * information of the respondent, time, etc. insert a single question-answer
	 * pair to the knowledge bank;
	 * 
	 * @param companyPk
	 * @throws Exception
	 */

	public void insertQACouplet(String question, String answer, String respondent, int origin) throws Exception {
		ArrayList<QACouplet> arr = new ArrayList<QACouplet>();
		arr.add(new QACouplet(question, answer, 1.0, new Date(), respondent, QACouplet.parseIntToOrigin(origin)));

		update(arr);
	}

	public void updateQACouplet(String oldQuestion, String oldAnswer, String newQuestion, String newAnswer, String respondent, int origin) throws Exception {
		deleteEntity(oldQuestion, oldAnswer);
		insertQACouplet(newQuestion, newAnswer, respondent, origin);
	}

	Couplet<FAQ, QACouplet> searchForRequiredCouplet(String question, String answer) {
		for (Map.Entry<Integer, FAQ> entry : this.clusters.entrySet()) {
			for (QACouplet couplet : entry.getValue()) {
				if (couplet.que.sentence.equals(question) && couplet.ans.sentence.equals(answer)) {
					return new Couplet<FAQ, QACouplet>(entry.getValue(), couplet);
				}
			}
		}
		return null;
	}

	public void export(String fileName) throws Exception {
		Workbook wb = Utility.createWorkbook(fileName.endsWith("xlsx"));
		Sheet sheet = Utility.createSheet(wb, "knowledgeBank");

		int rownum = 0;
		Row row = sheet.createRow(rownum++);

		int column = 0;
		row.createCell(column++).setCellValue("知识点标识符");
		row.createCell(column++).setCellValue("自然问");
		row.createCell(column++).setCellValue("自然答");
		row.createCell(column++).setCellValue("标准问");
		row.createCell(column++).setCellValue("标准答");
		row.createCell(column++).setCellValue("问答匹配度");
		row.createCell(column++).setCellValue("作答时间");
		row.createCell(column++).setCellValue("来源");
		row.createCell(column++).setCellValue("作答者");

		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		CellStyle dateCellStyle = wb.createCellStyle();
		dateCellStyle.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));

		synchronized (clusters.keySet()) {
			for (int key : clusters.keySet()) {
				FAQ faq = getFAQ(key);
				QACouplet epitome = faq.epitome();

				for (QACouplet couplet : faq) {
					row = sheet.createRow(rownum++);
					column = 0;
					row.createCell(column++).setCellValue(key);
					row.createCell(column++).setCellValue(couplet.que.toString());
					row.createCell(column++).setCellValue(couplet.ans.toString());
					row.createCell(column++).setCellValue(epitome.que.toString());
					row.createCell(column++).setCellValue(epitome.ans.toString());
					row.createCell(column++).setCellValue((int) (couplet.coherence * 100));

					// row.setCellType(Cell.CELL_TYPE_NUMERIC);
					Cell cell = row.createCell(column++);
					cell.setCellValue(couplet.time);
					cell.setCellStyle(dateCellStyle);

					row.createCell(column++).setCellValue(QACouplet.toString(couplet.origin));
					row.createCell(column++).setCellValue(couplet.respondent);
				}
			}
		}
		sheet.setColumnWidth(6, 20 * 256);
		Utility.write_excel(wb, fileName);
	}

	public void exportSupervised(String fileName) throws Exception {
		Workbook wb = Utility.createWorkbook(fileName.endsWith("xlsx"));
		Sheet sheet = Utility.createSheet(wb, "knowledgeBankSupervised");

		int rownum = 0;
		Row row = sheet.createRow(rownum++);

		int column = 0;
		row.createCell(column++).setCellValue("FAQID");
		row.createCell(column++).setCellValue("标准问");
		row.createCell(column++).setCellValue("标准答");
		row.createCell(column++).setCellValue("作答时间");
		row.createCell(column++).setCellValue("相关问个数");

		synchronized (clusters.keySet()) {
			for (int key : clusters.keySet()) {
				FAQ faq = getFAQ(key);
				QACouplet epitome = faq.supervisedEpitome();

				if (epitome != null && epitome.origin == Origin.SUPERVISOR_RESERVOIR) {
					row = sheet.createRow(rownum++);
					column = 0;
					row.createCell(column++).setCellValue(key);
					row.createCell(column++).setCellValue(epitome.que.toString());
					row.createCell(column++).setCellValue(epitome.ans.toString());
					row.createCell(column++).setCellValue(Utility.toString(epitome.time));
					row.createCell(column++).setCellValue(faq.size());
				}
			}
		}
		Utility.write_excel(wb, fileName);
	}

	public void exportUnsupervised(String fileName) throws Exception {
		Workbook wb = Utility.createWorkbook(fileName.endsWith("xlsx"));
		Sheet sheet = Utility.createSheet(wb, "Unsupervised");

		int rownum = 0;
		Row row = sheet.createRow(rownum++);

		int column = 0;
		row.createCell(column++).setCellValue("FAQID");
		row.createCell(column++).setCellValue("自然问");
		row.createCell(column++).setCellValue("自然答");

		row.createCell(column++).setCellValue("问答匹配度");
		row.createCell(column++).setCellValue("作答时间");
		row.createCell(column++).setCellValue("来源");
		row.createCell(column++).setCellValue("作答者");
		row.createCell(column++).setCellValue("自然问个数");
		synchronized (clusters.keySet()) {
			for (int key : clusters.keySet()) {
				FAQ faq = getFAQ(key);
				QACouplet epitome = faq.epitome();
				if (epitome.origin == Origin.ROBOT_RESERVOIR) {
					row = sheet.createRow(rownum++);
					column = 0;
					row.createCell(column++).setCellValue(key);
					row.createCell(column++).setCellValue(epitome.que.toString());
					row.createCell(column++).setCellValue(epitome.ans.toString());
					row.createCell(column++).setCellValue(epitome.coherence);
					row.createCell(column++).setCellValue(Utility.toString(epitome.time));
					row.createCell(column++).setCellValue(QACouplet.toString(epitome.origin));
					row.createCell(column++).setCellValue(epitome.respondent);
					row.createCell(column++).setCellValue(faq.size());
				}
			}
		}
		Utility.write_excel(wb, fileName);
	}

	public String exportSupervisedWithCriteria(String criteria) throws Exception {
		JSONArray jSONArray = new JSONArray();
		Utility.PriorityQueue<QACouplet> pq = new Utility.PriorityQueue<QACouplet>(new Comparator<QACouplet>() {
			@Override
			public int compare(QACouplet o1, QACouplet o2) {
				return o1.time.compareTo(o2.time);
			}
		});

		synchronized (clusters.keySet()) {
			for (int key : clusters.keySet()) {
				FAQ faq = this.getFAQ(key);
				QACouplet epitome = faq.supervisedEpitome(Origin.SUPERVISOR_RESERVOIR);
				if (epitome == null) {
					continue;
				}

				if (criteria == null || epitome.que.sentence.contains(criteria) || epitome.ans.sentence.contains(criteria)) {
					pq.add(epitome);
				}
			}
		}

		while (!pq.isEmpty()) {
			QACouplet epitome = pq.poll();
			JSONObject jSONObject = new JSONObject();
			jSONObject.put("question", epitome.que.toString());
			jSONObject.put("answer", epitome.ans.toString());
			jSONObject.put("time", Utility.toString(epitome.time));
			jSONObject.put("operator", epitome.respondent);
			jSONArray.add(jSONObject);
		}

		return URLEncoder.encode(jSONArray.toJSONString(), "UTF-8");
	}

	public String exportUnsupervisedWithCriteria(String criteria) throws Exception {
		JSONArray jSONArray = new JSONArray();
		DecimalFormat df = new DecimalFormat("#.00");

		Utility.PriorityQueue<QACouplet> pq = new Utility.PriorityQueue<QACouplet>(new Comparator<QACouplet>() {
			@Override
			public int compare(QACouplet o1, QACouplet o2) {
				return o1.time.compareTo(o2.time);
			}
		});

		synchronized (clusters.keySet()) {
			for (int key : clusters.keySet()) {
				FAQ faq = getFAQ(key);
				QACouplet epitome = faq.epitome();
				if (epitome == null || epitome.origin != Origin.ROBOT_RESERVOIR)
					continue;
				if (criteria == null || epitome.que.sentence.contains(criteria) || epitome.ans.sentence.contains(criteria)) {
					pq.add(epitome);
				}
			}
		}

		while (!pq.isEmpty()) {
			QACouplet epitome = pq.poll();

			JSONObject jSONObject = new JSONObject();
			jSONObject.put("question", epitome.que.toString());
			jSONObject.put("answer", epitome.ans.toString());
			jSONObject.put("coherence", df.format((int) (epitome.coherence * 100.0 * 100) / 100.00d));
			jSONObject.put("time", Utility.toString(epitome.time));
			jSONObject.put("origin", QACouplet.toString(epitome.origin));
			jSONObject.put("respondent", epitome.respondent);
			jSONArray.add(jSONObject);
		}

		return URLEncoder.encode(jSONArray.toJSONString(), "UTF-8");
	}

	String searchForExactAnswer(String question) throws Exception {
		synchronized (clusters.keySet()) {
			for (int key : clusters.keySet()) {
				FAQ faq = getFAQ(key);
				for (QACouplet couplet : faq) {
					if (couplet.que.sentence.equals(question)) {
						return couplet.ans.toString();
					}
				}
			}
		}

		return "";
	}

	public void exportErrorInformation(String fileName, String errorInformation[][]) throws Exception {
		Workbook wb = Utility.createWorkbook(fileName.endsWith("xlsx"));
		Sheet sheet = Utility.createSheet(wb, "errorInformation");

		int rownum = 0;
		Row row = sheet.createRow(rownum++);

		int column = 0;
		row.createCell(column++).setCellValue("行号");
		row.createCell(column++).setCellValue("标准问");
		row.createCell(column++).setCellValue("标准答");
		row.createCell(column++).setCellValue("错误原因");

		for (String[] res : errorInformation) {
			row = sheet.createRow(rownum++);
			column = 0;
			row.createCell(column++).setCellValue(res[0]);
			row.createCell(column++).setCellValue(res[1]);
			row.createCell(column++).setCellValue(res[2]);
			row.createCell(column++).setCellValue(res[3]);
		}

		Utility.write_excel(wb, fileName);
	}

	public void exportUnknown(final String fileName) throws Exception {
		MySQL.instance.new Invoker() {

			@Override
			protected Object invoke() throws Exception {
				HashSet<String> set = new HashSet<String>();
				if (MySQL.instance.isBatchInProcessForUnknownQuestion()) {
					MySQL.instance.executeBatchForUnknownQuestion();
				}
				if (MySQL.instance.isBatchInProcessForUnknownQuestion()) {
					log.info("isBatchInProcess error.");
				}

				Workbook wb = Utility.createWorkbook(fileName.endsWith("xlsx"));
				Sheet sheet = Utility.createSheet(wb, "unknown");

				int rownum = 0;
				Row row = sheet.createRow(rownum++);
				int column = 0;
				row.createCell(column++).setCellValue("自然问");
				row.createCell(column++).setCellValue("自然答");

				for (ResultSet res : MySQL.instance.new Query("select question from ecchatreportunknownquestion WHERE company_pk = '" + company_pk + "' ORDER BY time")) {
					String question = res.getString("question");
					if (!set.contains(question)) {
						row = sheet.createRow(rownum++);
						column = 0;

						row.createCell(column++).setCellValue(question);
						row.createCell(column++).setCellValue(searchForExactAnswer(question));
						set.add(question);
					}
				}
				Utility.write_excel(wb, fileName);
				return null;
			}

		}.execute();
	}

	public String exportUnknownWithCriteria(final String criteria) throws Exception {
		return MySQL.instance.new Invoker<String>() {

			@Override
			protected String invoke() throws Exception {
				JSONArray jSONArray = new JSONArray();
				HashSet<String> set = new HashSet<String>();
				if (MySQL.instance.isBatchInProcessForUnknownQuestion()) {
					MySQL.instance.executeBatchForUnknownQuestion();
				}
				if (MySQL.instance.isBatchInProcessForUnknownQuestion()) {
					log.info("isBatchInProcess error.");
				}

				for (ResultSet res : MySQL.instance.new Query("select question from ecchatreportunknownquestion WHERE company_pk = '" + company_pk + "' ORDER BY time")) {

					String question = res.getString("question");
					if (set.contains(question))
						continue;

					set.add(question);

					if (criteria == null || question.contains(criteria)) {
						JSONObject jSONObject = new JSONObject();
						jSONObject.put("question", question);
						jSONObject.put("answer", searchForExactAnswer(question));

						jSONArray.add(jSONObject);
					}
				}
				return URLEncoder.encode(jSONArray.toJSONString(), "UTF-8");
			}
		}.execute();
	}

	/**
	 * submit unknown knowledge from excel file
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public void submitUnknown(String fileName, String respondent) throws Exception {
		Sheet sheet = Utility.read_excel(fileName, "unknown");

		Iterator<Row> rows = sheet.rowIterator();
		Utility.skipLinePremiere(rows);

		ArrayList<QACouplet> array = new ArrayList<QACouplet>();
		while (rows.hasNext()) {
			Row row = rows.next(); // 获得行数据
			Cell cell0 = row.getCell(0);
			if (cell0 == null)
				continue;
			String question = cell0.getStringCellValue();
			Cell cell1 = row.getCell(1);
			if (cell1 == null)
				continue;
			String answer = cell1.getStringCellValue();

			log.info("question = " + question);
			log.info("answer = " + answer);
			if (answer == null)
				continue;

			answer = answer.trim();
			if (answer.length() == 0)
				continue;

			// log.info("time = " + Utility.toString(time));

			QACouplet couplet = new QACouplet(question, answer, 1, new Date(), respondent, Origin.ROBOT_RESERVOIR);
			array.add(couplet);
		}

		update(array);
		MySQL.instance.new Invoker() {

			@Override
			protected Object invoke() throws Exception {
				MySQL.instance.execute("delete from ecchatreportunknownquestion where company_pk ='" + company_pk + "'");

				return null;
			}
		}.execute();
	}

	/**
	 * submit unknown knowledge from excel file
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public void submitUnknown(final String question, String answer, String respondent) throws Exception {
		ArrayList<QACouplet> array = new ArrayList<QACouplet>();
		log.info("question = " + question);
		log.info("answer = " + answer);
		if (answer == null)
			return;

		answer = answer.trim();
		if (answer.length() == 0)
			return;

		// log.info("time = " + Utility.toString(time));

		QACouplet couplet = new QACouplet(question, answer, 1, new Date(), respondent, Origin.ROBOT_RESERVOIR);
		array.add(couplet);

		update(array);
		MySQL.instance.new Invoker() {

			@Override
			protected Object invoke() throws Exception {
				MySQL.instance.execute("delete from ecchatreportunknownquestion where company_pk ='" + company_pk + "' and question = '" + question + "'");

				return null;
			}
		}.execute();
	}

	public void submitSupervised(String fileName, String respondent) throws Exception {
		ArrayList<QACouplet> array = new ArrayList<QACouplet>();
		for (String[] arr : Utility.readFromExcel(fileName, "Supervised", 2)) {

			String question = arr[0];
			if (question == null)
				continue;
			String answer = arr[1];
			if (answer == null)
				continue;
			log.info("question = " + question);
			log.info("answer = " + answer);

			if (question.length() >= 256 || answer.length() > 1000)
				continue;
			QACouplet couplet = new QACouplet(question, answer, 1, new Date(), respondent, Origin.SUPERVISOR_RESERVOIR);
			array.add(couplet);
		}

		update(array);
	}

	/**
	 * like entity detected in the supervised knowledge bank;
	 * 
	 * @author Cosmos
	 *
	 */
	public static class LikeEntity {
		public LikeEntity(String question, String answer) {
			this.question = question;
			this.answer = answer;
		}

		@Override
		public String toString() {
			return "QUE = " + question + "\tANS = " + answer;
		}

		public String question;
		public String answer;
	}

	public String[][] checkValidityForSupervisedInsertion(String fileName) throws Exception {

		ArrayList<String[]> data = Utility.readFromExcel(fileName, null, 2);
		ArrayList<String[]> res = new ArrayList<String[]>();

		int rowCount = 0;
		for (String[] sstr : data) {
			++rowCount;
			String question = sstr[0];
			String answer = sstr[1];

			question = question == null ? "" : question.trim();
			answer = answer == null ? "" : answer.trim();

			log.info("question = " + question);
			log.info("answer = " + answer);

			boolean bQuestionEmpty = question == null || question.length() == 0;
			boolean bAnswerEmpty = answer == null || answer.length() == 0;

			if (bQuestionEmpty && bAnswerEmpty) {
				continue;
			}

			if (bQuestionEmpty || question.length() > 1000 || bAnswerEmpty || answer.length() > 1000) {
				String[] str = { Integer.toString(rowCount), question, answer, "" };
				if (bQuestionEmpty)
					str[3] = "问题长度为0";
				else if (question.length() > 255)
					str[3] = "问题长度大于255";
				else if (bAnswerEmpty)
					str[3] = "答案长度为0";
				else if (answer.length() > 1000)
					str[3] = "答案长度大于1000";

				res.add(str);
				continue;
			}

			LikeEntity likeEntity = checkValidityForSupervisedInsertion(question, answer, null);
			if (likeEntity != null) {
				String[] str = { Integer.toString(rowCount), question, answer, "该问答对与以下问答对相似或相同：" + likeEntity };

				res.add(str);
				continue;
			}
		}

		return res.toArray(new String[res.size()][]);
	}

	public LikeEntity checkValidityForSupervisedInsertion(String question, String answer, String exceptQuestion) throws Exception {
		log.info("checking Validity For Insertion (old question and answer excluded): ");
		log.info("question = " + question);
		log.info("answer = " + answer);
		QACouplet couplet = new QACouplet(question, answer, 1, new Date(), null, Origin.SUPERVISOR_RESERVOIR);
		synchronized (clusters.keySet()) {
			for (int key : clusters.keySet()) {
				FAQ faq = getFAQ(key);
				QACouplet epitome = faq.supervisedEpitome(Origin.SUPERVISOR_RESERVOIR);

				if (epitome != null) {
					if (epitome.similarity(couplet) >= 0.9) {
						if (epitome.que.toString().equals(exceptQuestion))
							continue;
						return new LikeEntity(epitome.que.toString(), epitome.ans.toString());
					}
				}
			}
		}
		return null;
	}

	/**
	 * submit a single faq into the knowledge bank;
	 * 
	 * @param question
	 * @param answer
	 * @throws Exception
	 */
	public void submitSupervised(String question, String answer, String respondent) throws Exception {

		ArrayList<QACouplet> array = new ArrayList<QACouplet>();

		log.info("question = " + question);
		log.info("answer = " + answer);

		QACouplet couplet = new QACouplet(question, answer, 1, new Date(), respondent, Origin.SUPERVISOR_RESERVOIR);
		array.add(couplet);

		update(array);
	}

	/**
	 * submit a single faq into the knowledge bank;
	 * 
	 * @param question
	 * @param answer
	 * @throws Exception
	 */
	public void submitSupervised(String[] question, String[] answer, String respondent) throws Exception {

		ArrayList<QACouplet> array = new ArrayList<QACouplet>();

		if (question.length != answer.length) {
			throw new RuntimeException("question.length != answer.length");
		}

		log.info("question = " + question);
		log.info("answer = " + answer);

		for (int i = 0; i < question.length; ++i) {
			QACouplet couplet = new QACouplet(question[i], answer[i], 1, new Date(), respondent, Origin.SUPERVISOR_RESERVOIR);
			array.add(couplet);
		}

		update(array);
	}

	//	consider the case for the ampersand & in the format "&amp;"
	//	" = "&quot;"
	//	& = "&amp;"
	//	< = "&lt;"
	//	> = "&gt;"
	//	non-breaking space = "&nbsp;"
	//	see reference http://tool.oschina.net/commons?type=2
	public boolean deleteEntity(String question, String answer) throws Exception {
		synchronized (clusters) {
			for (Map.Entry<Integer, FAQ> entry : this.clusters.entrySet()) {
				FAQ faq = entry.getValue();
				for (int i = 0; i < faq.size(); ++i) {
					QACouplet couplet = faq.get(i);

					if (couplet.que.sentence.equals(question) && couplet.ans.sentence.equals(answer)) {
						faq.remove(i);
						//						faq.clear();
						faq.isChanged = true;
						return true;
					}
				}
			}
		}
		return false;
	}

	// String file = "D:\\solution\\commonSense.xls";
	public void initializeFromExcel(String file) throws Exception {
		Sheet sheet = Utility.read_excel(file, "commonSense");
		Iterator<Row> rows = sheet.rowIterator();
		Utility.skipLinePremiere(rows);
		while (rows.hasNext()) {
			Row row = rows.next(); // 获得行数据
			Cell cellCatalog = row.getCell(0);
			Cell cellQuestion = row.getCell(1);
			Cell cellAnswer = row.getCell(2);

			if (cellCatalog == null || cellQuestion == null || cellAnswer == null)
				continue;
			String catalog = cellCatalog.getStringCellValue();
			String question = cellQuestion.getStringCellValue();
			String answer = cellAnswer.getStringCellValue();

			// log.info("answer = " + answer);
			if (catalog == null || question == null || answer == null)
				continue;

			catalog = catalog.trim();
			question = question.trim();
			answer = answer.trim();

			if (catalog.length() == 0 || question.length() == 0 || answer.length() == 0)
				continue;
			String catalogs[] = catalog.split("~");
			if (catalogs.length == 2) {
				catalog = "";
			} else {
				catalog = catalogs[2];
			}

			log.info("catalog = " + catalog);
			log.info("question = " + question);

			int key = keyGenerator.generate_key();
			FAQ faq = new FAQ(key);

			String answers[] = answer.split("//");
			log.info("answer = ");
			for (String ans : answers) {
				log.info(ans);
				faq.add(new QACouplet(question, ans, 1, new Date(), "", Origin.ROBOT_RESERVOIR));
			}

			clusters.put(key, faq);
			clusters.get(key).isChanged = true;
		}
		saveToSQL();
	}

	public void updateFromDialogue(String fileName) throws Exception {
		InformationExtraction knowledgeExtraction = new InformationExtraction(fileName);

		for (Conversation dialog : knowledgeExtraction.new DialogueDialysis()) {
			try {
				update(dialog);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public int updateFromDialogueCheckingValidity(String fileName) throws Exception {
		InformationExtraction knowledgeExtraction = new InformationExtraction(fileName);

		int cnt = 0;
		for (Conversation conversation : knowledgeExtraction.new DialogueDialysis()) {
			String customer = conversation.visitor;
			if (customer == null || customer.isEmpty()) {
				throw new Exception("访客昵称为空,行号为 " + cnt);
			}

			String content = conversation.content;
			if (content == null || content.isEmpty()) {
				throw new Exception("对话内容为空,行号为 " + cnt);
			}

			++cnt;
		}
		return cnt;
	}

	public void updateFromMessageContent(String txt) throws UnsupportedEncodingException, FileNotFoundException {
		for (String str : new Utility.Text(txt)) {
			if (str.length() <= 2)
				continue;

			try {
				if (str.startsWith("\"")) {
					str = str.substring(1, str.length() - 1);
				}

				log.info(str);
				this.update(str);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void insertQACoupletRobot(String question, String answer, Date time) throws Exception {
		ArrayList<QACouplet> arr = new ArrayList<QACouplet>();
		arr.add(new QACouplet(question, answer, 1.0, time, "robot", Origin.ROBOT_RESERVOIR));

		update(arr);
	}

	public JSONArray searchForQuestionByKeywordsJSONArray(String keyword) {
		JSONArray jsonArray = new JSONArray();
		try {
			for (Repertoire.SearchResult searchResult : searchForQuestionByKeywords(keyword)) {
				JSONObject object = new JSONObject();
				object.put("question", searchResult.question);
				object.put("highlightedArray", searchResult.index);
				jsonArray.add(object);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonArray;
	}

	public JSONArray searchTeletextJSONArray(String question) throws Exception {
		DecimalFormat df = new DecimalFormat("#.00");
		JSONArray jsonArray = new JSONArray();
		// yyyy-mm-dd HH:mm:ss
		for (Couplet<String, Float> quintuplet : searchTeletext(question)) {
			JSONObject object = new JSONObject();
			object.put("pk", quintuplet.x);
			object.put("confidence", df.format((int) (quintuplet.y * 100.0 * 100) / 100.00d));
			jsonArray.add(object);
		}
		return jsonArray;

	}

	public void updateFromMessageContent() throws SQLException, InterruptedException, Exception {
		String sql = "select msg_content from ecchatrecords WHERE company_pk = '" + company_pk + "' and msg_content is not null and msg_content != '' order by chat_start_time desc";
		log.info("sql: \n" + sql);

		MySQL.instance.new Invoker<Object>() {
			@Override
			protected Object invoke() throws Exception {
				int cnt = 0;
				for (ResultSet result : MySQL.instance.new Query(sql)) {
					String content = result.getString("msg_content");
					log.info("content: " + content);
					try {
						Repertoire.this.update(content);
						Thread.sleep(8 * 1000);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}

					if (++cnt > 10000) {
						break;
					}

				}
				return null;
			}
		}.execute();
	}

	public JSONArray search(String questionOriginal) throws Exception {
		return toJSONObject(query(questionOriginal));
	}

	static public JSONArray toJSONObject(ArrayList<AnsQuintuple> arr) {
		JSONArray jsonArray = new JSONArray();
		DecimalFormat df = new DecimalFormat("#.00");
		String date = Utility.toString(new Date());
		for (Repertoire.AnsQuintuple quintuplet : arr) {
			JSONObject object = new JSONObject();
			object.put("answer", quintuplet.answer.sentence);
			object.put("confidence", df.format((int) (quintuplet.confidence * 100.0 * 100) / 100.00d));
			object.put("recommendedFAQ", String.valueOf(quintuplet.faqid));
			object.put("time", date);
			object.put("respondent", quintuplet.respondent == null ? "" : quintuplet.respondent);
			object.put("origin", QACouplet.toString(quintuplet.origin));
			jsonArray.add(object);
		}
		return jsonArray;
	}

	void deletePhatics() throws Exception {
		synchronized (clusters.keySet()) {
			for (int key : clusters.keySet()) {
				FAQ faq = getFAQ(key);
				for (int i = 0; i < faq.size(); ++i) {
					QACouplet qaCouplet = faq.get(i);
					if (!qaCouplet.que.qatype().equals(QATYPE.QUERY) || !qaCouplet.ans.qatype().equals(QATYPE.REPLY)) {
						faq.remove(i);
						faq.isChanged = true;
					}
				}
			}
		}
	}
}
