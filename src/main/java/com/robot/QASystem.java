package com.robot;

import java.sql.ResultSet;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
//http://poi.apache.org/apidocs/org/apache/poi/ss/usermodel/Row.html

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.robot.Repertoire.AnsQuintuple;
import com.robot.Sentence.Protagonist;
import com.robot.DateBase.MySQL;
import com.robot.semantic.SyntaxCoach;
import com.util.Utility;
import com.util.Utility.Couplet;
import com.util.Utility.Timer;

public class QASystem {
	public static Logger log = Logger.getLogger(QASystem.class);
	public static QASystem instance;

	static {
		try {
			instance = new QASystem();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	QASystem() {

	}

	HashMap<String, Repertoire> knowledgeBank = new HashMap<String, Repertoire>();

	public Repertoire getCommonSense() throws Exception {
		return this.getRepertoire("00000000000000000000000000000000");
	}

	public static void main(String[] args) throws Exception {
		Timer timer = new Timer();
		timer.start();
		String company_pk = "8a28ccbd4ff3073d015002726d3f15fa";
		String quest = "汽车首保有哪些条件？";
		// String quest = "主要客户有哪些？";
		// quest = "怎么联系你们客服";
		log.info("question: " + quest);

		ArrayList<AnsQuintuple> res = QASystem.instance.getRepertoire(company_pk).query(quest);
		if (res.size() == 0) {
			log.info("no answers.");
		} else {
			log.info("answers: ");
		}

		for (AnsQuintuple pair : res) {
			log.info(pair.answer + "\t" + pair.confidence);
		}
	}

	synchronized public Repertoire getRepertoire(String company_pk) throws Exception {
		if (!knowledgeBank.containsKey(company_pk)) {
			knowledgeBank.put(company_pk, new Repertoire(company_pk));
		}
		return knowledgeBank.get(company_pk);
	}

	//创建一个缓冲池，缓冲池容量大小为Integer.MAX_VALUE
	ExecutorService executor = Executors.newCachedThreadPool();

	public void execute(Runnable runnable) {
		executor.execute(new Thread(runnable));
	}

	public void update(final String company, final String content) throws Exception {

		execute(new Runnable() {
			@Override
			public void run() {
				try {
					QASystem.this.getRepertoire(company).update(content);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public void updateTeletext(final String company, final String pk, final String title, final String description, final String content) throws Exception {

		execute(new Runnable() {
			@Override
			public void run() {
				try {
					QASystem.this.getRepertoire(company).updateTeletext(pk, title, description, content);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public void updateFromDialogue(final String company, final String fileName) throws Exception {
		execute(new Runnable() {
			@Override
			public void run() {
				try {
					QASystem.this.getRepertoire(company).updateFromDialogue(fileName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	@SuppressWarnings("deprecation")
	synchronized static public void report(final String res) throws Exception {
		MySQL.instance.new Invoker() {
			@Override
			protected Object invoke() throws Exception {
				JSONObject js = JSONObject.parseObject(res);

				String selectedAnswer = js.get("selectedAnswer").toString();
				String actualAnswer = js.get("actualAnswer").toString();
				String decision;

				log.info("selected Answer = " + selectedAnswer);
				log.info("actual   Answer = " + actualAnswer);
				if (selectedAnswer.length() == 0) {
					decision = "INDECISION";
				} else if (selectedAnswer.trim().equals(actualAnswer.trim())) {
					decision = "COMPLETE";
				} else {
					decision = "PARTIAL";
				}
				log.info("decision = " + decision);

				MySQL.instance.reportQuery(js.getString("company_pk"), js.get("question").toString(), js.get("time").toString(), js.get("recommendedFAQ").toString(), js.get("selectedFAQ").toString(), decision);
				return null;
			}
		}.execute();
	}

	static public void report(final String company_pk, final String question, final String actualAnswer, final String selectedAnswer, final String time, final String recommendedFAQ, final String selectedFAQ) throws Exception {
		MySQL.instance.new Invoker() {
			@Override
			protected Object invoke() throws Exception {
				String decision;

				log.info("selected Answer = " + selectedAnswer);
				log.info("actual   Answer = " + actualAnswer);
				if (selectedAnswer.length() == 0) {
					decision = "INDECISION";
				} else if (selectedAnswer.trim().equals(actualAnswer.trim())) {
					decision = "COMPLETE";
				} else {
					decision = "PARTIAL";
				}
				log.info("decision = " + decision);

				MySQL.instance.reportQuery(company_pk, question, time, recommendedFAQ, selectedFAQ, decision);
				return null;
			}
		}.execute();
	}

	static public int report_query(final String company_pk, final Date start, final Date end) throws Exception {
		return MySQL.instance.new Invoker<Integer>() {
			@Override
			protected Integer invoke() throws Exception {
				return ((MySQL) this.getDataSource()).report_query(company_pk, start, end);
			}
		}.execute();
	}

	static public int report_recommended(final String company_pk, final Date start, final Date end) throws Exception {
		return MySQL.instance.new Invoker<Integer>() {
			@Override
			protected Integer invoke() throws Exception {
				return MySQL.instance.report_recommended(company_pk, start, end);
			}
		}.execute();
	}

	static public int report_selected(final String company_pk, final Date start, final Date end) throws Exception {
		return MySQL.instance.new Invoker<Integer>() {
			@Override
			protected Integer invoke() throws Exception {
				return MySQL.instance.report_selected(company_pk, start, end);
			}
		}.execute();
	}

	static public int report_utterly_selected(final String company_pk, final Date start, final Date end) throws Exception {
		return MySQL.instance.new Invoker<Integer>() {
			@Override
			protected Integer invoke() throws Exception {
				return MySQL.instance.report_utterly_selected(company_pk, start, end);
			}
		}.execute();
	}

	static public int report_partially_selected(final String company_pk, final Date start, final Date end) throws Exception {
		return MySQL.instance.new Invoker<Integer>() {
			@Override
			protected Integer invoke() throws Exception {
				return MySQL.instance.report_partially_selected(company_pk, start, end);
			}
		}.execute();
	}

	synchronized static public Couplet<String, int[]>[] report_top_concerns(String company_pk, int nBest, String start, String end) throws Exception {
		return report_top_concerns(company_pk, nBest, Utility.parseDateFormat(start), Utility.parseDateFormat(end));
	}

	static public Couplet<String, int[]>[] report_top_concerns(final String company_pk, final int nBest, final Date start, final Date end) throws Exception {
		return MySQL.instance.new Invoker<Couplet<String, int[]>[]>() {
			@Override
			protected Couplet<String, int[]>[] invoke() throws Exception {
				String startTime = "'" + Utility.toString(start) + "'";
				String endTime = "'" + Utility.toString(end) + "'";
				int tally = 0;
				Couplet<String, int[]> arr[] = new Couplet[nBest];

				MySQL.Query query;

				if (MySQL.instance.isBatchInProcessForReportQuery()) {
					MySQL.instance.executeBatchForReportQuery();
				}

				query = MySQL.instance.new Query("SELECT selected, (SELECT COUNT(*) FROM ecchatreportquery WHERE company_pk = '" + company_pk + "' and CONCAT(' ', recommended, ' ') like CONCAT('% ', t.selected, ' %')) as recommendedCnt, COUNT(company_pk) as selectedCnt FROM ecchatreportquery t WHERE company_pk = '" + company_pk + "' and selected is not null and time >= " + startTime + " and time <= " + endTime + " GROUP BY selected ORDER BY COUNT(company_pk) DESC");
				for (ResultSet res : query) {
					if (tally >= nBest) {
						query.close();
						break;
					}

					arr[tally] = new Couplet<String, int[]>();
					arr[tally].x = instance.getRepertoire(company_pk).clusters.get(res.getInt("selected")).epitome().que.sentence;
					arr[tally].y = new int[3];
					arr[tally].y[0] = res.getInt("recommendedCnt");
					arr[tally].y[1] = res.getInt("selectedCnt");
					arr[tally].y[2] = arr[tally].y[1] * 100 / arr[tally].y[0];

					++tally;
				}
				if (tally < nBest) {
					arr = Arrays.copyOf(arr, tally);
				}

				return arr;
			}

		}.execute();
	}

	static public enum Period {
		HOUR, DATE, MONTH, YEAR
	}

	synchronized static public Couplet<String, int[]>[] report(String company_pk, String start, String end, Period period) throws Exception {
		return report(company_pk, Utility.parseDateFormat(start), Utility.parseDateFormat(end), period);
	}

	@SuppressWarnings("deprecation")
	static public Couplet<String, int[]>[] report(final String company_pk, final Date start, final Date end, final Period period) throws Exception {
		return MySQL.instance.new Invoker<Couplet<String, int[]>[]>() {
			@Override
			protected Couplet<String, int[]>[] invoke() throws Exception {
				Date arr[] = null;
				int dif = 0;
				switch (period) {
				case YEAR:
					// month the month value between 0-11.
					start.setMonth(0);
					end.setMonth(0);
				case MONTH:
					// the day of the month value between 1-31.
					start.setDate(1);
					end.setDate(1);
				case DATE:
					start.setHours(0);
					end.setHours(0);
				case HOUR:
					// Calendar.set(Calendar.HOUR_OF_DAY, 0);
					start.setMinutes(0);
					start.setSeconds(0);
					end.setMinutes(0);
					end.setSeconds(0);
				}

				switch (period) {
				case YEAR:
					dif = end.getYear() - start.getYear();
					arr = new Date[dif + 2];
					arr[0] = start;
					for (int i = 1; i < arr.length; ++i) {
						arr[i] = (Date) arr[i - 1].clone();
						arr[i].setYear(arr[i].getYear() + 1);
					}
					break;
				case MONTH:
					dif = end.getYear() - start.getYear();
					dif *= 12;
					dif += end.getMonth() - start.getMonth();
					arr = new Date[dif + 2];
					arr[0] = start;
					for (int i = 1; i < arr.length; ++i) {
						arr[i] = (Date) arr[i - 1].clone();
						arr[i].setMonth(arr[i].getMonth() + 1);
					}
					break;
				case DATE:
					dif = (int) ((end.getTime() - start.getTime()) / 24 / 60 / 60 / 1000);
					arr = new Date[dif + 2];
					arr[0] = start;
					for (int i = 1; i < arr.length; ++i) {
						arr[i] = new Date(arr[i - 1].getTime() + 86400 * 1000);
					}
					break;
				case HOUR:
					dif = (int) ((end.getTime() - start.getTime()) / 60 / 60 / 1000);
					arr = new Date[dif + 2];
					arr[0] = start;
					for (int i = 1; i < arr.length; ++i) {
						arr[i] = new Date(arr[i - 1].getTime() + 3600 * 1000);
					}
					break;
				}

				final Couplet<String, int[]> res[] = (Couplet<String, int[]>[]) new Couplet[arr.length - 1];

				if (MySQL.instance.isBatchInProcessForReportQuery()) {
					MySQL.instance.executeBatchForReportQuery();
				}
				if (MySQL.instance.isBatchInProcessForReportQuery()) {
					log.info("isBatchInProcess error.");
				}

				for (int i = 0; i < res.length; ++i) {
					res[i] = new Couplet<String, int[]>();
					String date = Utility.toString(arr[i]);
					switch (period) {
					case YEAR:
						date = date.substring(0, 4);
						break;
					case MONTH:
						date = date.substring(0, 7);
						break;
					case DATE:
						date = date.substring(0, 10);
						break;
					case HOUR:
						date = date.substring(0, 13);
						break;
					}

					res[i].x = date;
					res[i].y = new int[5];

					res[i].y[0] = MySQL.instance.report_query(company_pk, arr[i], arr[i + 1]);
					res[i].y[1] = MySQL.instance.report_recommended(company_pk, arr[i], arr[i + 1]);
					res[i].y[2] = MySQL.instance.report_utterly_selected(company_pk, arr[i], arr[i + 1]);
					res[i].y[3] = MySQL.instance.report_partially_selected(company_pk, arr[i], arr[i + 1]);
					res[i].y[4] = MySQL.instance.report_faq_totality(company_pk, arr[i + 1]) - MySQL.instance.report_faq_totality(company_pk, arr[i]);
				}

				Utility.reverse(res);

				ArrayList<Couplet<String, int[]>> resArray = new ArrayList<Couplet<String, int[]>>();
				// clear the vacuous datum;
				for (Couplet<String, int[]> arg : res) {
					boolean occupied = false;
					for (int a : arg.y) {
						if (a != 0) {
							occupied = true;
							break;
						}
					}
					if (occupied) {
						resArray.add(arg);
					}
				}

				return resArray.toArray(new Couplet[0]);
			}
		}.execute();
	}

	void write_excel(String fileName, String name, Vector<QACouplet> faq) throws Exception {
		Workbook wb = Utility.read_excel(fileName);
		Sheet sheet = wb.getSheet(name);

		int rownum = 0;
		Row row = sheet.createRow(rownum++);
		int column = 0;
		row.createCell(column++).setCellValue("自然问");
		row.createCell(column++).setCellValue("自然答");
		row.createCell(column++).setCellValue("匹配自信值/%");
		for (QACouplet qa : faq) {
			row = sheet.createRow(rownum++);
			column = 0;
			row.createCell(column++).setCellValue(qa.que.sentence);
			row.createCell(column++).setCellValue(qa.ans.sentence);
			row.createCell(column++).setCellValue(qa.coherence * 100);
		}
		Utility.write_excel(wb, fileName);
	}

	public String automaticResponse(String companyPk, String question) throws Exception {
		String answer = null;
		question = Conversation.format(question);
		if (question != null) {
			Utility.Timer timer = new Utility.Timer();
			Sentence sentence = new Sentence(question, Protagonist.CUSTOMER);

			answer = getRepertoire(companyPk).automaticResponse(sentence);

			log.info("time for automaticResponse");
			timer.report();

			if (answer == null) {
				answer = getCommonSense().automaticResponseRandomly(sentence);
			}
		}

		return answer;
	}

	synchronized static public JSONArray report(String companyPk, String start, String end, String period) throws Exception {
		JSONArray array = new JSONArray();
		QASystem.Period periodEnum = null;
		switch (period) {
		case "0":
			periodEnum = QASystem.Period.HOUR;
			break;
		case "1":
			periodEnum = QASystem.Period.DATE;
			break;
		case "2":
			periodEnum = QASystem.Period.MONTH;
			break;
		case "3":
			periodEnum = QASystem.Period.YEAR;
			break;
		default:
			periodEnum = QASystem.Period.DATE;
			break;
		}

		Couplet<String, int[]>[] res;
		res = QASystem.report(companyPk, start, end, periodEnum);
		for (int i = 0, len = res.length; i < len; i++) {
			JSONObject object = new JSONObject();
			object.put("name", res[i].x);
			object.put("data", res[i].y);
			array.add(object);
		}

		return array;
	}

	public void updateFromMessageContent(String company) throws ParseException, Exception {
		getRepertoire(company).updateFromMessageContent();
	}
	
	public void learning() throws Exception {
		execute(new Runnable() {

			@Override
			public void run() {
				try {
					SyntaxCoach.learning();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
