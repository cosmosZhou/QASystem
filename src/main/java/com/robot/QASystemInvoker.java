package com.robot;

import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import com.robot.Repertoire.SearchResult;
import com.robot.QASystem.Period;
import com.alibaba.fastjson.JSONArray;
import com.robot.Repertoire.AnsQuintuple;
import com.util.MySQL;
import com.util.Utility;
import com.util.Utility.Couplet;

public class QASystemInvoker {

	void add_test_data() throws Exception {
		final String company_pk = "8a28ccbd4ff3073d015002726d3f15fa";
		for (final String question : new Utility.Text(Utility.workingDirectory + "models/CUSTOMER-QUERY/NUM.data")) {
			MySQL.instance.new Invoker() {
				@Override
				protected Object invoke() throws Exception {
					MySQL.instance.reportUnknownQuestion(company_pk, question, Utility.toString(new Date()));
					return null;
				}
			}.execute();
		}
	}

	public void create_index() throws ParseException, Exception {
		String company = "8a28ccbd4d51f3be014d564cc91417d4";
		// QASystem.instance.getKnowledgeBank(company).initialize();
		QASystem.instance.getRepertoire(company).createKeywordInvertedIndexer();
	}

	public void searchQuestion() throws ParseException, Exception {
		String company = "ff8080815ce312bf015ce318b0620002";
		// QASystem.instance.getKnowledgeBank(company).initialize();
		TreeSet<SearchResult> result = QASystem.instance.getRepertoire(company).searchForQuestionByKeywords("健康");
		for (SearchResult searchResult : result) {
			log.info(searchResult.question);
			log.info(searchResult.index);
		}
	}

	public void set_threshold() throws ParseException, Exception {
		String company = "8a28ccbd4d51f3be014d564cc91417d4";
		double threshold = 0.1;
		// QASystem.instance.getKnowledgeBank(company).initialize();
		QASystem.instance.getRepertoire(company).set_threshold(threshold);
	}

	static public void update() throws ParseException, Exception {
		//		String arr[] = { "CUSTOMER: 汽车首保条件是什么？", "OPERATOR: 汽车的首保条件是汽车的使用期限不大于半年。" };
		String arr[] = { "CUSTOMER: 请问开发有多少人？", "OPERATOR: 10个。" };
		//		String arr[] = { "CUSTOMER: 请问测试几个人？", "OPERATOR: 10个。" };
		//		Sentence arr[] = new Sentence[3];
		//		int i = 0;

		//		arr[i++] = new Sentence("我的车漏水了", Protagonist.CUSTOMER);
		//		arr[i++] = new Sentence("怎么办？", Protagonist.CUSTOMER);
		//		arr[i++] = new Sentence("请到当地经销商处维修", Protagonist.OPERATOR);

		//		arr[i++] = new Sentence("朗逸快要上市了", Protagonist.OPERATOR);
		//		arr[i++] = new Sentence("什么时候？", Protagonist.CUSTOMER);
		//		arr[i++] = new Sentence("九月初三。", Protagonist.OPERATOR);

		String company = "8a28ccbd4ff3073d015002726d3f15fa";
		String content = Conversation.decompile(arr);
		log.info(content);
		QASystem.instance.update(company, content);

		//		QASystem.instance.execute(new Runnable() {
		//			@Override
		//			public void run() {
		//				try {
		//					QASystem.instance.getRepertoire(company).updateFromMessageContent(Utility.workingDirectory + "record.txt");
		//				} catch (Exception e) {
		//					// TODO Auto-generated catch block
		//					e.printStackTrace();
		//				}
		//			}
		//		});
	}

	// 假设，假若，假如，假定，如果，若，倘若，已知，假使，倘使， 若是，要是, 若使

	public void search() throws ParseException, Exception {
		// String company_pk = "8a28ccbd4d51f3be014d564cc91417d4";
		String company_pk = "8a28ccbd4ff3073d015002726d3f15fa";
		// String question = "已知汽车的使用期限是五个月,请问符合首保条件吗？";
		// String question = "假若汽车的使用期限是5年,请问满足首保条件吗？";
		// String question = "主要客户有哪些？";
		// String question = "怎么联系你们客服";

		// String question = "倘若汽车的使用期限是181天,请问满足首保条件吗？";
		// String question = "汽车首保条件是什么？";
		// String question = "你好，请问客服电话是多少";
		// String question = "公司有什么案例";
		String question = "我昨天开的户为什么现在还没有短信通过通知呢？";

		ArrayList<AnsQuintuple> arr = QASystem.instance.getRepertoire(company_pk).query(question);

		for (AnsQuintuple obj : arr) {
			String answer = obj.answer.sentence;
			double confidence = obj.confidence;
			int recommendedFAQ = obj.faqid;

			log.info("answer : " + answer + " \twith confidence = " + confidence + ", recommendedFAQ = " + recommendedFAQ);
		}
	}

	public void submitSupervisedFAQ() {
		String company_pk = "8a28ccbd4ff3073d015002726d3f15fa";
		// String question = "能介绍一下在线客服吗?";
		// String answer =
		// "在线客服平台是一套可以通过一套客服系统平台处理来自微信、官网、APP等不同渠道进来的客户咨询，进行客户服务的系统平台";
		// String question = "现在大众汽车什么价格？";
		// String answer = "现在大众汽车市场指导价为10万元";

		// String question = "现在斯柯达汽车什么价格？";
		// String answer = "现在斯柯达汽车市场指导价为20万元";

		// String question = "现在凌渡汽车什么价格？";
		// String answer = "现在凌渡汽车市场指导价为20万元";

		// String question = "现在桑塔纳汽车什么价格？";
		// String answer = "现在桑塔纳汽车市场指导价为20万元";
		String question = "汽车首保有什么条件？";
		String answer = "汽车的首保条件是汽车的使用期限不超过半年";

		question = "现在大众汽车都有哪些前沿技术？";
		answer = "大众汽车技术很成熟";
		question = "Any800是什么？";
		answer = "Any800就是全渠道";

		question = "你知道什么是全渠道吗？";
		answer = "全渠道就是类似于Any800的东西";

		question = "怎样办理登机手续？";
		answer = "您可以到机场服务中心办理登机手续";

		question = "为什么你们这样对待消费者？";
		answer = "如您不满意，可以打人工投诉电话";

		try {
			QASystem.instance.getRepertoire(company_pk).submitSupervised(question, answer);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void submitSupervised() {
		String company_pk = "8a28ccbd4d51f3be014d564cc91417d4";
		String excelFile = "e:\\360\\solution\\UNKNOWNFAQ.xls";

		try {
			String[][] res = QASystem.instance.getRepertoire(company_pk).checkValidityForSupervisedInsertion(excelFile);
			if (res.length == 0) {
				QASystem.instance.getRepertoire(company_pk).submitSupervised(excelFile, "boss");
			} else {
				log.info(Utility.toString(res));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void submitUnknown() throws Exception {
		String company_pk = "8a28ccbd4d51f3be014d564cc91417d4";
		String excelFile = "e:\\360\\solution\\UNKNOWNFAQ.xls";

		QASystem.instance.getRepertoire(company_pk).submitUnknown(excelFile, "JADE");
	}

	public void deleteSupervisedFAQ() {
		String company_pk = "8a28ccbd4ff3073d015002726d3f15fa";
		// String question = "能介绍一下在线客服吗?";
		// String answer =
		// "在线客服平台是一套可以通过一套客服系统平台处理来自微信、官网、APP等不同渠道进来的客户咨询，进行客户服务的系统平台";
		// String question = "现在大众汽车什么价格？";
		// String answer = "现在大众汽车市场指导价为10万元";

		// String question = "现在斯柯达汽车什么价格？";
		// String answer = "现在斯柯达汽车市场指导价为20万元";

		// String question = "现在凌渡汽车什么价格？";
		// String answer = "现在凌渡汽车市场指导价为20万元";

		// String question = "现在桑塔纳汽车什么价格？";
		// String answer = "现在桑塔纳汽车市场指导价为20万元";
		String question = "汽车首保有什么条件？";
		String answer = "汽车的首保条件是汽车的使用期限不超过半年";

		question = "现在大众汽车都有哪些前沿技术？";
		answer = "大众汽车技术很成熟";
		try {
			QASystem.instance.getRepertoire(company_pk).deleteEntity(question, answer);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void automaticResponse() throws ParseException, Exception {
		String companyPk = "8a28ccbd4ff3073d015002726d3f15fa";
		companyPk = "40288ba05a1eb9f9015a1ef95313031c";
		String question = "你能否告知现在大众汽车的价格？";
		//		String question = "在不";
		String ans = QASystem.instance.getRepertoire(companyPk).automaticResponse(question);

		if (ans == null)
			ans = QASystem.instance.getCommonSense().automaticResponseRandomly(question);
		log.info("answer : " + ans);
	}

	public void export() throws ParseException, Exception {
		String fileName = "D:\\Solution\\excel.xlsx";

		String company_pk = "8a28ccbd4ff3073d015002726d3f15fa";

		QASystem.instance.getRepertoire(company_pk).export(fileName);
	}

	public void exportUnknown() throws ParseException, Exception {
		String fileName = "D:\\Solution\\excel.xlsx";

		String company_pk = "8a28ccbd4ff3073d015002726d3f15fa";

		QASystem.instance.getRepertoire(company_pk).exportUnknown(fileName);
		// log.info("res = " + res);
	}

	public void exportUnknownWithCriteria() throws ParseException, Exception {
		//		String companyPk = "8a28ccbd4ff3073d015002726d3f15fa";
		String companyPk = "40288ba05a1eb9f9015a1ef95313031c";
		String res = QASystem.instance.getRepertoire(companyPk).exportUnknownWithCriteria("生气");
		JSONArray array = JSONArray.parseArray(URLDecoder.decode(res));
		for (Object obj : array) {
			log.info("obj = \n" + obj);
		}
	}

	public void exportUnsupervisedWithCriteria() throws ParseException, Exception {
		//		String companyPk = "8a28ccbd4ff3073d015002726d3f15fa";
		String companyPk = "40288ba05a1eb9f9015a1ef95313031c";
		String res = QASystem.instance.getRepertoire(companyPk).exportUnsupervisedWithCriteria(null);
		JSONArray array = JSONArray.parseArray(URLDecoder.decode(res));
		for (Object obj : array) {
			log.info("obj = \n" + obj);
		}

	}

	void updateFromDialogue(String companyPk, String excelDisk) {
		float cnt;
		try {
			cnt = QASystem.instance.getRepertoire(companyPk).updateFromDialogueCheckingValidity(excelDisk) / 1200f;
			cnt = ((int) cnt * 100) / 100;
			log.info("cnt = " + cnt);

			QASystem.instance.updateFromDialogue(companyPk, excelDisk);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info(e.getMessage());
			e.printStackTrace();
		}

	}

	public void report() throws ParseException, Exception {
		String company_pk = "8a28ccbd4d51f3be014d564cc91417d4";
		String start = "2016-10-12 11:35:49";
		String end = "2016-12-26 17:57:11";
		Period period = Period.DATE;
		Couplet<String, int[]>[] res = QASystem.instance.report(company_pk, start, end, period);
		for (Couplet<String, int[]> e : res) {
			log.info(e.x + "\t" + Utility.toString(e.y, "\t"));
		}

	}

	//  cd E:\360\solution\models
	//	java -classpath .;E:\360\solution\QASystem\WebContent\WEB-INF\lib\* com.robot.QASystemInvoker

	//	 java -classpath 
	//	/opt/ucc/apache-tomcat-7.0.47/webapps/QASystem/WEB-INF/lib/QASystem.jar; 
	//	/opt/ucc/apache-tomcat-7.0.47/webapps/QASystem/WEB-INF/lib/Utility.jar
	//	com.robot.QASystemInvoker 
	public static void main(String[] args) throws ParseException, Exception {
		//		log.info("this is a message from the application.");

		QASystemInvoker tester = new QASystemInvoker();
		//		tester.updateFromDialogue("2c908088594a7757015a63f6929e5f91", "E:\\360\\solution\\凯翼对话记录-智能推荐话术.xls");
		//				QASystem.instance.getCommonSense().initializeFromExcel("e:\\360\\solution\\commonSense.xls");

		// tester.create_index();
		//		tester.searchQuestion();
		//		tester.update();
	
		// tester.search();
		// tester.deleteSupervisedFAQ();
		//		 tester.submitSupervisedFAQ();
		//		tester.automaticResponse();
		//		tester.exportUnknownWithCriteria();
		//		tester.exportUnsupervisedWithCriteria();
		// tester.submitUnknown();

		//		tester.submitSupervised();
		//		tester.submitUnknown();
		// tester.search();
		// tester.export();
		// tester.add_test_data();
		log.info("train finished!");
		System.exit(0);
	}

	public static Logger log = Logger.getLogger(QASystemInvoker.class);
}
