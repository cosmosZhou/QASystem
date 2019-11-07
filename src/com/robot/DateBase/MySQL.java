package com.robot.DateBase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.robot.QACouplet;
import com.robot.Sentence;
import com.robot.QACouplet.Origin;
import com.robot.Sentence.Protagonist;
import com.robot.util.PropertyConfig;
import com.robot.FAQ;
import com.util.Utility;
import com.util.Utility.Couplet;
import com.util.Utility.DataSource;
import com.util.Utility.DataSource.Query;

public class MySQL extends DataSource {
	private static Logger log = Logger.getLogger(MySQL.class);

	static public MySQL instance;

	static {
		instance = static_construct();
	}

	static MySQL static_construct(String url, String user, String password) {
		// instance = new MySQL(PropertyConfig.getProperty("url",
		// "jdbc:mysql://121.40.196.48:3306/ucc?"),
		// PropertyConfig.getProperty("user", "root"),
		// PropertyConfig.getProperty("password", "client1!"));
		return new MySQL(url, user, password);
	}

	static MySQL static_construct() {
		return static_construct(PropertyConfig.getProperty("url"), PropertyConfig.getProperty("user"), PropertyConfig.getProperty("password"));
	}

	public MySQL(String url, String user, String password) {
		super(url, user, password);
	}

	// of format : dddd-dd-dd
	public ArrayList<String> select(String company_pk, Date date) throws SQLException {
		return select(company_pk, new Date(date.getTime() - 86399 * 1000), date);
	}

	public ArrayList<String> select(String company_pk, String strDate) throws SQLException, ParseException {
		Date date = Utility.parseDateFormat(strDate + " 00:00:00");
		return select(company_pk, new Date(date.getTime() - 86400 * 1000), date);
	}

	// of format : dddd-dd-dd
	public ArrayList<String> select(String company_pk, Date startDate, Date endDate) throws SQLException {
		String start = '\'' + Utility.toString(startDate) + '\'';
		String end = '\'' + Utility.toString(endDate) + '\'';
		ArrayList<String> arr = new ArrayList<String>();

		String sql = "select msg_content from ecchatrecords WHERE company_pk = " + "\'" + company_pk + "\'" + " and msg_content is not null and msg_content != '' and insert_time BETWEEN " + start + " AND " + end;
		log.info("sql: \n" + sql);

		for (ResultSet result : new Query(sql)) {
			String content = result.getString("msg_content");
			log.info("content: " + content);
			arr.add(content);
		}
		return arr;
	}

	// of format : dddd-dd-dd
	public HashMap<String, ArrayList<String>> select(String startDate, String... previousDate) throws SQLException {
		String start = '\'' + Utility.toString(startDate) + '\'';
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		map = new MongodbServiceImpl().getMsgContent(startDate, previousDate);
		return map;
	}

	public Couplet<String, String> select_pk(String pk) throws SQLException {
		String sql = "select insert_time, company_pk from ecchatrecords WHERE pk = " + "'" + pk + "')";

		log.info("sql: \n" + sql);
		Query query = new Query(sql);
		String insert_time = null;
		String company_pk = null;
		if (query.hasNext()) {
			ResultSet result = query.next();
			insert_time = result.getString("insert_time");
			company_pk = result.getString("company_pk");
			if (query.hasNext()) {

			}
		} else {

		}

		return new Couplet<String, String>(company_pk, insert_time);
	}

	/**
	 * clear the duplicate instances if existed;
	 * 
	 * @param question
	 * @param answer
	 * @param coherence
	 * @param time
	 * @param FAQID
	 * @param company_pk
	 * @param origin
	 * @param respondent
	 * @throws SQLException
	 */
	public void insert(int question, int answer, double coherence, Date time, int FAQID, String company_pk, Origin origin, String respondent) throws SQLException {
		execute("delete from ecchatqacouplet where question = " + question + " and answer = " + answer + " and company_pk = \'" + company_pk + "\'");

		execute("insert into ecchatqacouplet(question, answer, coherence, time, FAQID, company_pk, origin, respondent) VALUES(" + question + "," + answer + "," + coherence + "," + "\'" + Utility.toString(time) + "\'," + FAQID + ",'" + company_pk + "', " + origin.ordinal() + ", '" + respondent + "')");
	}

	public void insert(String company_pk, Date time, int total) throws SQLException {
		execute("insert into ecchatreportupdate(company_pk, time, total) VALUES('" + company_pk + "','" + Utility.toString(time) + "'," + total + ")");
	}

	public void insert(String query, String reply, double confidence, Date time, String company_pk) throws SQLException {
		execute("delete from ecchatrepository where question = \'" + query + "\'" + " and answer = \'" + reply + "\'" + " and company_pk = \'" + company_pk + "\'");

		execute("insert into ecchatrepository(question, answer, confidence, time, company_pk) VALUES(" + "\'" + query + "\'," + "\'" + reply + "\'," + confidence + "," + "\'" + Utility.toString(time) + "\'," + "\'" + company_pk + "\')");

		// execute("COMMIT");
	}

	String bufferForReportQuery[] = new String[50];
	int bufferLengthForReportQuery = 0;

	public int[] executeBatchForReportQuery() throws SQLException {
		log.info("public int[] executeBatch() throws SQLException");
		log.info(Utility.toString(bufferForReportQuery, "\n", null, bufferLengthForReportQuery));
		log.info("bufferLength = " + bufferLengthForReportQuery);
		int bufferLength = this.bufferLengthForReportQuery;
		this.bufferLengthForReportQuery = 0;
		return execute(bufferForReportQuery, bufferLength);
	}

	String bufferForUnknownQuestion[] = new String[50];
	int bufferLengthForUnknownQuestion = 0;

	public int[] executeBatchForUnknownQuestion() throws SQLException {
		log.info(Utility.toString(bufferForUnknownQuestion, "\n", null, bufferLengthForUnknownQuestion));
		log.info("bufferLength = " + bufferLengthForUnknownQuestion);
		int bufferLength = this.bufferLengthForUnknownQuestion;
		this.bufferLengthForUnknownQuestion = 0;
		return execute(bufferForUnknownQuestion, bufferLength);
	}

	public boolean isBatchInProcessForReportQuery() {
		return this.bufferLengthForReportQuery > 0;
	}

	public boolean isBatchInProcessForUnknownQuestion() {
		return this.bufferLengthForUnknownQuestion > 0;
	}

	synchronized public void reportUnknownQuestion(String company_pk, String question, String time) throws SQLException {

		bufferForUnknownQuestion[bufferLengthForUnknownQuestion] = "insert into ecchatreportunknownquestion(company_pk, question, time) VALUES(" + "'" + company_pk + "', '" + question + "', '" + time + "')";

		if (++bufferLengthForUnknownQuestion == bufferForUnknownQuestion.length) {
			executeBatchForUnknownQuestion();
		}
	}

	synchronized public void reportQuery(String company_pk, String question, String time, String recommended, String selectedFAQ, String decision) throws SQLException {
		if (selectedFAQ.length() == 0) {
			selectedFAQ = null;
		} else {
			try {
				if (Integer.parseInt(selectedFAQ) < 0) {
					return;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return;
			}
		}

		bufferForReportQuery[bufferLengthForReportQuery] = "insert into ecchatreportquery(company_pk, question, time, recommended, selected, decision) VALUES(" + "'" + company_pk + "', '" + question + "', '" + time + "', '" + recommended + "', " + selectedFAQ + ", '" + decision + "')";

		if (++bufferLengthForReportQuery == bufferForReportQuery.length) {
			executeBatchForReportQuery();
		}
	}

	public int report_query(String company_pk, Date start, Date end) throws SQLException {
		String startTime = "'" + Utility.toString(start) + "'";
		String endTime = "'" + Utility.toString(end) + "'";
		Query query = new Query("select count(*) as cnt from ecchatreportquery where company_pk = '" + company_pk + "' AND time >= " + startTime + " AND time < " + endTime);
		int cnt = 0;
		if (query.hasNext()) {
			cnt = query.next().getInt("cnt");
			query.close();
		} else {

		}
		return cnt;
	}

	public int report_recommended(String company_pk, Date start, Date end) throws SQLException {
		String startTime = "'" + Utility.toString(start) + "'";
		String endTime = "'" + Utility.toString(end) + "'";
		Query query = new Query("select count(*) as cnt from ecchatreportquery where company_pk = '" + company_pk + "' and time >= " + startTime + " AND time < " + endTime + " and recommended is not null and recommended != ''");
		int cnt = 0;
		if (query.hasNext()) {
			cnt = query.next().getInt("cnt");
			query.close();
		} else {

		}
		return cnt;
	}

	public int report_selected(String company_pk, Date start, Date end) throws SQLException {
		String startTime = "'" + Utility.toString(start) + "'";
		String endTime = "'" + Utility.toString(end) + "'";
		Query query = new Query("select count(*) as cnt from ecchatreportquery where company_pk = '" + company_pk + "' and time >= " + startTime + " and time < " + endTime + " and selected is not null");
		int cnt = 0;
		if (query.hasNext()) {
			cnt = query.next().getInt("cnt");
			query.close();
		} else {

		}
		return cnt;
	}

	public int report_partially_selected(String company_pk, Date start, Date end) throws SQLException {
		String startTime = "'" + Utility.toString(start) + "'";
		String endTime = "'" + Utility.toString(end) + "'";
		Query query = new Query("select count(*) as cnt from ecchatreportquery where company_pk = '" + company_pk + "'and time >= " + startTime + " AND time < " + endTime + " and selected is not null and decision = 'PARTIAL'");
		int cnt = 0;
		if (query.hasNext()) {
			cnt = query.next().getInt("cnt");
			query.close();
		} else {

		}
		return cnt;
	}

	public int report_utterly_selected(String company_pk, Date start, Date end) throws SQLException {
		String startTime = "'" + Utility.toString(start) + "'";
		String endTime = "'" + Utility.toString(end) + "'";
		Query query = new Query("select count(*) as cnt from ecchatreportquery where company_pk = '" + company_pk + "'and time >= " + startTime + " AND time < " + endTime + " and selected is not null and decision = 'COMPLETE'");
		int cnt = 0;
		if (query.hasNext()) {
			cnt = query.next().getInt("cnt");
			query.close();
		} else {

		}
		return cnt;
	}

	// http://dev.mysql.com/doc/refman/5.7/en/string-functions.html
	public HashMap<Integer, Double> report_recommended(String company_pk, int nBest, Date start, Date end) throws SQLException {
		String startTime = "'" + Utility.toString(start) + "'";
		String endTime = "'" + Utility.toString(end) + "'";
		Query query = new Query("SELECT recommended FROM ecchatreportquery WHERE company_pk = '" + company_pk + "' and time between " + startTime + " and " + endTime);
		int cnt = 0;
		int arr[] = new int[nBest];
		HashMap<Integer, Double> map = new HashMap<Integer, Double>();
		for (ResultSet res : query) {
			int[] faqs = Utility.parseInt(res.getString("recommended"));
			for (int faq : faqs) {
				double score;
				if (map.containsKey(faq)) {
					score = map.get(faq);
				} else {
					score = 0;
				}
				map.put(faq, score + 1.0 / faqs.length);
			}
		}

		if (cnt < nBest) {
			arr = java.util.Arrays.copyOf(arr, cnt);
		}
		return map;
	}

	public int report_faq_totality(String company_pk, Date time) throws SQLException {
		Query query = new Query("SELECT total FROM ecchatreportupdate WHERE COMPANY_PK = '"

				+ company_pk

				+ "' and time <= '" + Utility.toString(time)

				+ "' ORDER BY time DESC");

		int maximum = -1;
		if (query.hasNext()) {
			maximum = query.next().getInt("total");
			query.close();
		} else {
			maximum = 0;
		}

		return maximum;
	}

	/**
	 * all the natural questions and answers for this particular faq will be
	 * cleared,
	 * 
	 * @param FAQID
	 * @param que
	 * @param ans
	 * @param hierarchy
	 * @param company_pk
	 * @param frequency
	 * @throws SQLException
	 */
	public void insert(FAQ faq, String company_pk) throws SQLException, Exception {
		BatchExecutive deleteExecutive = new BatchExecutive();
		BatchExecutive insertExecutive = new BatchExecutive();
		String sql = "delete from ecchatfaqcorpus where FAQID = " + faq.id + " and company_pk = \'" + company_pk + "\'";
		log.info("sql = " + sql);
		deleteExecutive.addBatch(sql);

		for (QACouplet qaCouplet : faq) {
			String question = Utility.remove_apostrophe(qaCouplet.que.toString());
			String answer = Utility.remove_apostrophe(qaCouplet.ans.toString());

			sql = "delete from ecchatfaqcorpus where question = '" + question + "' and answer = '" + answer + "' and company_pk = '" + company_pk + "'";
			//			log.info("delete " + question);
			deleteExecutive.addBatch(sql);

			sql = "insert into ecchatfaqcorpus(question, answer, coherence, time, FAQID, company_pk, origin, respondent, frequency) VALUES('" + question + "','" + answer + "'," + qaCouplet.coherence + "," + "'" + Utility.toString(qaCouplet.time) + "'," + faq.id + ",'" + company_pk + "', " + qaCouplet.origin.ordinal() + ", '" + qaCouplet.respondent + "', " + qaCouplet.frequency + ")";
			log.info("insert " + question);
			insertExecutive.addBatch(sql);
		}

		deleteExecutive.executeBatch();
		insertExecutive.executeBatch();
	}

	public static class TransferRecord {
		public String operator;
		public String trench;
		public String time;
		public int outDegree;
		public int inDegree;
	}

	/**
	 * 
	 * request_time in [startTime, endTime); endTime - startTime should be the
	 * time interval; by default, it should be half an hour.
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws SQLException
	 */

	public void ReadFromChatRecords(String startTime, String endTime) throws SQLException {
		startTime = "'" + startTime + "'";
		endTime = "'" + endTime + "'";
		Query query = new Query("SELECT trench, event_name, event_target_name, Request_time FROM ECCHATRECORDS " + " where event_name is not null and request_time >= " + startTime + " and request_time < " + endTime);

		HashMap<String, int[]> map = new HashMap<String, int[]>();
		for (ResultSet res : query) {
			String trench = res.getString("trench");
			String event_name[] = res.getString("event_name").split(";");
			String event_target_name[] = res.getString("event_target_name").split(";");
			String request_time = res.getString("request_time");
			for (int i = 0; i < event_name.length; ++i) {
				switch (event_name[i]) {
				case "接入": {
					String key = event_target_name[i] + ":" + trench;
					if (!map.containsKey(key)) {
						map.put(key, new int[2]);
					}
					++map.get(key)[0];
				}
					break;
				case "转移":
					String key = event_target_name[i] + ":" + trench;
					if (!map.containsKey(key)) {
						map.put(key, new int[2]);
					}
					++map.get(key)[0];
					break;
				case "接管":

					break;
				case "邀请":

					break;
				default:
					break;
				}

			}
		}
	}

	void retrieveDatabaseInfo() throws Exception {
		Connection conn = MySQL.instance.open();
		DatabaseMetaData metadata = MySQL.instance.getDatabaseMetaData();

		System.out.println("数据库已知的用户: " + metadata.getUserName());
		System.out.println("数据库的系统函数的逗号分隔列表: " + metadata.getSystemFunctions());
		System.out.println("数据库的时间和日期函数的逗号分隔列表: " + metadata.getTimeDateFunctions());
		System.out.println("数据库的字符串函数的逗号分隔列表: " + metadata.getStringFunctions());
		System.out.println("数据库供应商用于 'schema' 的首选术语: " + metadata.getSchemaTerm());
		System.out.println("数据库URL: " + metadata.getURL());
		System.out.println("是否允许只读:" + metadata.isReadOnly());
		System.out.println("数据库的产品名称:" + metadata.getDatabaseProductName());
		System.out.println("数据库的版本:" + metadata.getDatabaseProductVersion());
		System.out.println("驱动程序的名称:" + metadata.getDriverName());
		System.out.println("驱动程序的版本:" + metadata.getDriverVersion());

		System.out.println();
		System.out.println("数据库中使用的表类型");
		ResultSet rs = metadata.getTableTypes();
		while (rs.next()) {
			System.out.println(rs.getString(1));
		}
		rs.close();

		System.out.println();

		System.out.println("获取指定的数据库的所有表的类型");

		System.out.println("获取指定的数据库的表的主键");
		//获取指定的数据库的表的主键，第二个参数也是模式名称的模式,使用null了   
		System.out.println();

		System.out.println("DatabaseMetaData.getIndexInfo()方法返回信息:");

		MySQL.instance.close();
	}

	static String description[] = { "Field", "Type", "Null", "Key", "Default", "Extra", };

	public ArrayList<String[]> readFromTeletext() throws Exception {

		ArrayList<String[]> arr = new ArrayList<String[]>();

		String sql = "select * from kf_knowledge ";
		System.out.println("sql: \n" + sql);
		for (ResultSet result : new Query(sql)) {

			String pk = result.getString("pk");
			String company_pk = result.getString("company_pk");
			String title = result.getString("title");
			String description = result.getString("description");
			String content = result.getString("content");

			String res[] = { pk, company_pk, title, description, content };
			arr.add(res);
		}
		return arr;
	}

	public void insert(String x, String y, int similarity) throws SQLException {
		String sql = "select similarity from synonym where x = '" + x + "' and y = '" + y + "'";
//		System.out.println("sql = " + sql);
		Query query = new Query(sql);
		for (ResultSet result : query) {
//			System.out.println("keys already exist: " + x + ", " + y);
			query.close();

			execute("update synonym set x = '" + x + "', y = '" + y + "', similarity = " + similarity);
			return;
		}
		sql = "select similarity from synonym where x = '" + y + "' and y = '" + x + "'";
//		System.out.println("sql = " + sql);
		query = new Query(sql);
		for (ResultSet result : query) {
//			System.out.println("keys already exist: " + x + ", " + y);
			query.close();
			execute("update synonym set x = '" + x + "', y = '" + y + "', similarity = " + similarity);
			return;
		}

		execute("insert into synonym(x, y, similarity) VALUES(" + "'" + x + "', '" + y + "', " + similarity + ")");
	}

	public static void main(String[] args) throws Exception {
		instance.open();
		instance.close();
	}
}
