package com.util;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

public class MySQL extends DataSource {
	private static Logger log = Logger.getLogger(MySQL.class);

	static public MySQL instance;

	static {
		synchronized (MySQL.class) {
//		String url = "jdbc:mysql://192.168.2.39:3306/corpus?";
			String url = "jdbc:mysql://localhost:3306/corpus?";
			String user = "root";
//		

			if (SystemUtils.IS_OS_WINDOWS) {
				String serverTimezone = "UTC";
				String password = "123456";
//			String serverTimezone = "GMT";
				instance = static_construct(url, user, password, serverTimezone);
			} else {
				String password = "";
				instance = static_construct(url, user, password);
			}
		}
	}

	static MySQL static_construct(String url, String user, String password) {
		// instance = new MySQL(PropertyConfig.getProperty("url",
		// "jdbc:mysql://121.40.196.48:3306/ucc?"),
		// PropertyConfig.getProperty("user", "root"),
		// PropertyConfig.getProperty("password", "client1!"));
		return new MySQL(url, user, password);
	}

	static MySQL static_construct(String url, String user, String password, String serverTimezone) {
		return new MySQL(url, user, password, serverTimezone);
	}

	static MySQL static_construct() {
		return static_construct(PropertyConfig.getProperty("url"), PropertyConfig.getProperty("user"),
				PropertyConfig.getProperty("password"));
	}

	public MySQL(String url, String user, String password) {
		super(url, user, password, Driver.mysql);
	}

	public MySQL(String url, String user, String password, String serverTimezone) {
		super(url, user, password, Driver.mysql, serverTimezone);
	}

	public void selectDialogClassificationLabel(String topicPK, ArrayList<String> nameList) throws SQLException {

		String parentpk = null;
		String name = null;
		String pk = topicPK;
		do {
			for (ResultSet res : new Query("select name, parentpk from ectopic WHERE pk = '" + pk + "'")) {
				parentpk = res.getString("parentpk");
				name = res.getString("name");
			}
			// if (name == null || parentpk == null)
			// break;
			if (parentpk == null)
				break;

			// topicArr.add(pk);
			nameList.add(name);
			pk = parentpk;
		} while (!"-1".equals(pk));

		// if (topicArr.isEmpty())
		// return;

		// Collections.reverse(topicArr);
		Collections.reverse(nameList);
	}

	public void selectDialogClassification(String pkDialog, ArrayList<String> arr) throws SQLException {

		String sql = "select msg_content from ecchatrecords WHERE pk = '" + pkDialog + "'";

		log.info("sql: \n" + sql);

		for (ResultSet result : new Query(sql)) {
			String content = result.getString("msg_content");
			log.info("content: " + content);
			arr.add(content);
		}
	}

	public HashMap<String, String> select_repertoire(String service) throws Exception {
		HashMap<String, String> dict = new HashMap<String, String>();
		try (DataSource inst = open()) {

			String services = null;
			switch (service) {
			case "map":
			case "localsearch":
				services = "'map', 'localsearch'";
				break;
			case "audio":
			case "music":
			case "joke":
				services = "'audio', 'music', 'joke'";
				break;
			case "sms":
			case "contact":
			case "call":
				services = "'sms', 'contact', 'call'";
				break;
			default:
				break;
			}

			for (ResultSet res : new Query(
					String.format("select text, slot from tbl_repertoire where service in (%s)", services))) {
				dict.put(res.getString("text"), res.getString("slot"));
//				System.out.println(res.getString("text") + " = " + res.getString("slot"));
			}
		}
		return dict;
	}

	public String[] tbl_service_distinct_category() throws Exception {
		ArrayList<String> arr = new ArrayList<String>();
		String sql = "select distinct service from tbl_service order by service";
//		String sql = "select distinct service from tbl_service order by service desc";
		try (DataSource inst = open()) {
			for (ResultSet res : new Query(sql)) {
				arr.add(res.getString("service"));
			}
		}

		return Utility.toArray(arr);

	}

	public void selectTopicClassificationFromParent(String classPK, HashSet<String> pkMap) throws SQLException {
		for (ResultSet res : new Query("select pk from comment_category WHERE parent_pk = '" + classPK + "'")) {
			pkMap.add(res.getString("pk"));
		}
	}

	public void selectDialogClassificationFromRoot(String classPK, HashSet<String> pkMap) throws SQLException {
		for (ResultSet res : new Query(
				"select pk from ectopic WHERE parentpk = '-1' and companyPK = '" + classPK + "'")) {
			pkMap.add(res.getString("pk"));
		}
	}

	public void selectTopicClassificationFromRoot(String classPK, HashSet<String> pkMap) throws SQLException {
		for (ResultSet res : new Query(
				"select pk from comment_category WHERE deepth = 1 and company_PK = '" + classPK + "'")) {
			pkMap.add(res.getString("pk"));
		}
	}

	String bufferForReportQuery[] = new String[50];
	int bufferLengthForReportQuery = 0;

	String bufferForUnknownQuestion[] = new String[50];
	int bufferLengthForUnknownQuestion = 0;

	public boolean isBatchInProcessForReportQuery() {
		return this.bufferLengthForReportQuery > 0;
	}

	public boolean isBatchInProcessForUnknownQuestion() {
		return this.bufferLengthForUnknownQuestion > 0;
	}

	void retrieveDatabaseInfo() throws Exception {
		DataSource conn = MySQL.instance.open();
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
		// 获取指定的数据库的表的主键，第二个参数也是模式名称的模式,使用null了
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
		// System.out.println("sql = " + sql);
		Query query = new Query(sql);
		for (ResultSet result : query) {
			// System.out.println("keys already exist: " + x + ", " + y);
			query.close();

			execute("update synonym set x = '" + x + "', y = '" + y + "', similarity = " + similarity);
			return;
		}
		sql = "select similarity from synonym where x = '" + y + "' and y = '" + x + "'";
		// System.out.println("sql = " + sql);
		query = new Query(sql);
		for (ResultSet result : query) {
			// System.out.println("keys already exist: " + x + ", " + y);
			query.close();
			execute("update synonym set x = '" + x + "', y = '" + y + "', similarity = " + similarity);
			return;
		}

		execute("insert into synonym(x, y, similarity) VALUES(" + "'" + x + "', '" + y + "', " + similarity + ")");
	}

	public static void main(String[] args) throws Exception {
		instance.select_repertoire("map");
	}
}
