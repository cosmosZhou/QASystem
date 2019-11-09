package com.robot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.robot.DateBase.MySQL;
import com.util.Utility;

public class HttpClientWebApp {
	HttpClient httpClient = new HttpClient();

	enum Environment {
		local, test, product
	}

	//	static Environment environment = Environment.local;
	static Environment environment = Environment.test;
	//	static Environment environment = Environment.product;
	static String server;
	static String companyPk;
	static {
		switch (environment) {
		case local:
			server = "http://127.0.0.1:8080";
			//			companyPk = "8a28ccbd4ff3073d015002726d3f15fa";
			companyPk = "ff8080815ca5319a015ca60fd4580577";
			break;
		case test:
			//			server = "http://121.40.130.192:8080";
			//			server = "http://192.168.11.94:7005";
			server = "http://192.168.11.61:8080";

			//			server = "http://ronghe.any800.com/";
			//			server = "http://121.41.80.95:8081";

			//			server = "http://192.168.11.60:8080";

			//						server = "http://192.168.11.55:8080";
			//			server = "http://116.239.24.10:8080";

			//			companyPk = "ff8080815ca5319a015ca60fd4580577";
			//			companyPk = "8a28ccbd4ff3073d015002726d3f15fa";
			break;
		case product:
			server = "http://172.16.0.7:7005";
			//			server = "http://172.16.16.118:8080";

			//			companyPk = "8a28ccbd4d51f3be014d564cc91417d4";
			companyPk = "2c9080885c5f009c015c5f1ef633041c";
			break;
		}
	}

	public HttpClientWebApp() {
		httpClient.setConnectionTimeout(50000);
		httpClient.setTimeout(50000);
	}

	String functionPathPrefix() {
		return server + "/QASystemInterface/resteasy/Knowledge/";
	}

	static class Parameter {
		Parameter(String field, String argument, boolean bEncode) {
			this.field = field;
			this.argument = argument;
			this.bEncode = bEncode;
		}

		Parameter(String field, String argument) {
			this(field, argument, false);
		}

		public String field;
		public String argument;
		public boolean bEncode;
	}

	String postMethod(String function) {
		return postMethod(function, new Parameter[0], false);
	}

	String postMethod(String function, boolean bEncode) {
		return postMethod(function, new Parameter[0], bEncode);
	}

	String postMethod(String function, Parameter[] args) {
		return postMethod(function, args, false);
	}

	String postMethod(String function, Parameter[] args, boolean bEncode) {
		PostMethod method = new PostMethod(functionPathPrefix() + function);
		try {
			for (Parameter para : args) {

				method.addParameter(para.field, para.bEncode ? URLEncoder.encode(para.argument, "UTF-8") : para.argument);
			}
		} catch (IllegalArgumentException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String result = null;

		try {

			httpClient.executeMethod(method);
			String resStr = new String(method.getResponseBodyAsString().getBytes("UTF-8"));
			// String resStr = method.getResponseBodyAsStream().toString();
			result = bEncode ? URLDecoder.decode(resStr, "UTF-8") : resStr;
			method.releaseConnection();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public void update(String content) {
		String jeson;

		try {
			log.info(content);
			Parameter[] args = new Parameter[2];
			args[0] = new Parameter("companyPk", companyPk);
			args[1] = new Parameter("content", content, true);
			String res = postMethod("update", args);
			log.info("res = " + res);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void topicClassification() throws UnsupportedEncodingException, FileNotFoundException, InterruptedException {
		String comment = "物流真的是飞快了 在b站被喜欢的up主安利种草的 收藏加购了很久终于趁女王节搞活动拍了 今晚就上牙试试不知道好不好用 感觉这个救急应急应该挺好的 长期维持可能不太现实 有没有效等用了几次再来追评吧 好评着";
		Parameter[] args = new Parameter[] { new Parameter("comment", comment, true) };
		String res = postMethod("classify", args, true);
		log.info("res = " + res);
		args = new Parameter[] {

				new Parameter("comment", "嗯一键启动", true),

				new Parameter("label", "可提供性,方法", true), };
		res = postMethod("confirm", args, true);
		log.info("res = " + res);

		res = postMethod("training", false);
		log.info("res = " + res);

	}

	public void update() throws UnsupportedEncodingException, FileNotFoundException, InterruptedException {
		companyPk = "ff8080815ce312bf015ce318b0621111";
		//		ff8080815ce312bf015ce318b0620002
		//		String regex = "INSERT INTO `ecchatrecords_copy` VALUES \\('([\\s\\S]+)', [\\S]+, [\\S]+\\);";
		//		for (String str : new Utility.StringReader(Utility.workingDirectory + "record.txt")) {
		//			//			log.info(str);
		//			//			String[] res = Utility.regexSingleton(str, regex);
		//			//			if (res == null)
		//			//				continue;
		//			if (str.length() <= 2)
		//				continue;
		//
		//			if (str.startsWith("\"")) {
		//				str = str.substring(1, str.length() - 1);
		//			}
		//
		//			log.info(str);
		//			//			content = content.replace("\\\'", "'");
		//			//			content = content.replace("\\\"", """);
		//			update(str);
		//			Thread.sleep(8 * 1000);
		//		}
		//		companyPk = "ff8080815ce312bf015ce318b0620002";
		String question = "你好周末你去哪里玩";
		String answer = "你好去佘山啊!";

		//		String question = "请问测试有多少人";
		//		String answer = "测试有8人";

		String arr[] = { "CUSTOMER: " + question, "OPERATOR: " + answer };

		String jeson;

		try {
			jeson = Conversation.decompile(arr);
			log.info(jeson);
			Parameter[] args = new Parameter[2];
			args[0] = new Parameter("companyPk", companyPk);
			args[1] = new Parameter("content", jeson, true);
			String res = postMethod("update", args);
			log.info("res = " + res);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateTeletext() throws UnsupportedEncodingException, FileNotFoundException, InterruptedException {

		try {
			MySQL.instance.new Invoker() {
				@Override
				protected Object invoke() throws Exception {
					for (String[] res : MySQL.instance.readFromTeletext()) {
						int i = 0;
						Parameter[] args = new Parameter[5];
						args[i] = new Parameter("pk", res[i]);
						++i;
						args[i] = new Parameter("companyPk", res[i]);
						++i;
						args[i] = new Parameter("title", res[i], true);
						++i;
						args[i] = new Parameter("description", res[i], true);
						++i;
						args[i] = new Parameter("content", res[i], true);
						++i;
						String result = postMethod("updateTeletext", args);
						log.info("res = " + result);
					}

					return null;
				}
			}.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateFromExcel() throws Exception {
		companyPk = "ff8080815d777c84015d778c0224002d";
		InformationExtraction informationExtraction = new InformationExtraction(Utility.workingDirectory + "chatRecordTest.xls");

		for (Conversation conversation : informationExtraction.new DialogueDialysis()) {
			String jeson = conversation.toHTML();
			try {
				log.info(jeson);
				Parameter[] args = new Parameter[2];
				args[0] = new Parameter("companyPk", companyPk);
				args[1] = new Parameter("content", jeson, true);
				String res = postMethod("update", args);
				log.info("res = " + res);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void update(String question, String answer) {
		String arr[] = { "CUSTOMER: " + question, "OPERATOR: " + answer };

		String jeson;

		try {
			jeson = Conversation.decompile(arr);
			log.info(jeson);
			Parameter[] args = new Parameter[2];
			args[0] = new Parameter("companyPk", companyPk);
			args[1] = new Parameter("content", jeson, true);
			String res = postMethod("update", args);
			log.info("res = " + res);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateFromDialogue() {
		//		companyPk = "40288ba05a1eb9f9015a1ef95313031c";
		companyPk = "2c908088594a7757015a63f6929e5f99";
		String fileName = "E:\\360\\solution\\chatRecord.xls";
		//		String fileName = "E:\\360\\solution\\凯翼对话记录-智能推荐话术.xls";

		try {
			//			QASystem.instance.getRepertoire(companyPk).updateFromDialogue(fileName);
			Parameter[] args = new Parameter[2];
			args[0] = new Parameter("companyPk", companyPk, false);
			args[1] = new Parameter("excelFile", fileName, false);

			String res = postMethod("updateFromDialogue", args, true);
			log.info("res = " + res);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void corpusTraining() throws ParseException, Exception {
		String res = postMethod("corpusTraining");
		log.info("res = " + res);
	}

	public void learning() throws ParseException, Exception {
		String res = postMethod("learning");
		log.info("res = " + res);
	}

	public void search() {
		companyPk = "ff808081622904140162625ffda403ac";
		String question = "预算是多少";
		//		你的心情怎样啊
		//		companyPk = "2c9090f05b180ead015b18406dfe0009";
		Parameter[] args = new Parameter[2];
		args[0] = new Parameter("companyPk", companyPk);

		// String question = "倘若汽车的使用期限是五个月,请问满足首保条件吗？",
		//		String question = "倘若汽车的使用期限是半年,请问符合首保条件吗？";

		args[1] = new Parameter("question", question, true);
		String res = postMethod("search", args, true);

		log.info("res = " + res);
		for (Object obj : JSONArray.parseArray(res)) {
			JSONObject js = (JSONObject) obj;
			String answer = js.get("answer").toString();
			String confidence = js.get("confidence").toString();
			String recommendedFAQ = js.get("recommendedFAQ").toString();
			String time = js.get("time").toString();

			log.info("answer : " + answer + " \twith confidence = " + confidence + ", recommendedFAQ = " + recommendedFAQ + ", time = " + time);
		}
	}

	public void searchTeletext() {
		companyPk = "2c9090f05b180ead015b18406dfe0009";
		companyPk = "40288b0d594023140159402398c70001";

		String question = "cosmos";

		Parameter[] args = new Parameter[2];
		args[0] = new Parameter("companyPk", companyPk);

		args[1] = new Parameter("question", question, true);
		String res = postMethod("searchTeletext", args, true);

		log.info("res = " + res);
		for (Object obj : JSONArray.parseArray(res)) {
			JSONObject js = (JSONObject) obj;
			String pk = js.get("pk").toString();
			String confidence = js.get("confidence").toString();

			log.info("pk : " + pk + " \twith confidence = " + confidence);
		}
	}

	public void searchForQuestionByKeywords() {
		companyPk = "ff8080815ce312bf015ce318b0620002";
		String question = "健康";
		Parameter[] args = new Parameter[2];
		args[0] = new Parameter("companyPk", companyPk);
		args[1] = new Parameter("question", question, true);

		// String question = "案例", true);
		// String question = "公司", true);
		// String question = "在线客服", true);

		String res;

		try {
			res = postMethod("searchForQuestionByKeywords", args, true);
			log.info("res = " + res);
			for (Object obj : JSONArray.parseArray(res)) {
				JSONObject js = (JSONObject) obj;
				question = js.get("question").toString();
				String highlightedArray = js.get("highlightedArray").toString();

				log.info("question : " + question + " \twith highlightedArray = " + highlightedArray);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void automaticResponse() {
		companyPk = "ff8080815d5fc2f7015d62d371940003";
		//		String question = "你们公司怎么样？";
		//		String question = "告诉我你们公司的地址。";
		//		String question = "你们公司的地址在哪。";
		String question = "今天我心情很好";
		question = "你好";
		question = "您好";

		Parameter[] args = new Parameter[2];
		args[0] = new Parameter("companyPk", companyPk);
		args[1] = new Parameter("question", question, true);

		String res;
		try {
			res = postMethod("automaticResponse", args, true);
			log.info("res = " + res);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	public void export() {
		// String fullUploadPath = Utility.createTemporaryFile("xlsx");
		Parameter[] args = new Parameter[1];
		args[0] = new Parameter("companyPk", companyPk);
		// args[1] = new Parameter("excelFile", fullUploadPath);

		String res = null;
		try {
			res = postMethod("export", args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		log.info("res = " + res);
	}

	public void exportUnknown() throws ParseException, Exception {
		// String fullUploadPath = Utility.createTemporaryFile("xlsx");
		Parameter[] args = new Parameter[1];
		args[0] = new Parameter("companyPk", companyPk);

		String res = postMethod("exportUnknown", args);

		log.info("res = " + res);
	}

	public void exportUnknownWithCriteria() throws ParseException, Exception {
		// String fullUploadPath = Utility.createTemporaryFile("xlsx");
		Parameter[] args = { new Parameter("companyPk", companyPk), };
		String res = postMethod("exportUnknownWithCriteria", args, true);

		log.info("res = " + res);
	}

	public void exportSupervised() throws ParseException, Exception {
		Parameter[] args = new Parameter[1];
		args[0] = new Parameter("companyPk", companyPk);

		String res = postMethod("exportSupervised", args);

		log.info("res = " + res);
	}

	public void exportUnsupervised() throws ParseException, Exception {
		// String fullUploadPath = Utility.createTemporaryFile("xlsx");
		companyPk = "2c908088594a7757015a63f6929e5f92";
		Parameter[] args = new Parameter[1];
		args[0] = new Parameter("companyPk", companyPk);
		// args[1] = new Parameter("excelFile", fullUploadPath);

		String res = postMethod("exportUnsupervised", args);

		log.info("res = " + res);
	}

	public void exportSupervisedWithCriteria() throws ParseException, Exception {
		// String fullUploadPath = Utility.createTemporaryFile("xlsx");
		Parameter[] args = new Parameter[2];
		args[0] = new Parameter("companyPk", companyPk);
		args[1] = new Parameter("criteria", "在线客服", true);

		// args[1] = new Parameter("excelFile", fullUploadPath);

		String res = postMethod("exportSupervisedWithCriteria", args, true);

		log.info("res = " + res);
	}

	public void exportUnsupervisedWithCriteria() throws ParseException, Exception {
		companyPk = "40288ba05a1eb9f9015a1ef95313031c";
		// String fullUploadPath = Utility.createTemporaryFile("xlsx");
		//		Parameter[] args = { new Parameter("companyPk", companyPk), new Parameter("criteria", "婴儿", true), };
		Parameter[] args = { new Parameter("companyPk", companyPk), };

		// args[1] = new Parameter("excelFile", fullUploadPath);

		String res = postMethod("exportUnsupervisedWithCriteria", args, true);

		JSONArray array = JSONArray.parseArray(URLDecoder.decode(res));
		for (Object obj : array) {
			log.info("obj = \n" + obj);
		}

		//		log.info("res = " + res);
	}

	public void report() throws ParseException, Exception {
		Parameter[] args = { new Parameter("companyPk", "8a28ccbd583e330b01584cd008fb0674"), new Parameter("start", "2016-11-29 00:00:00"), new Parameter("end", "2016-11-29 20:06:00"), new Parameter("period", "0") };

		String res = postMethod("report", args);

		log.info("res = " + res);
	}

	public void salient() throws ParseException, Exception {
		Parameter[] args = { new Parameter("companyPk", "8a28ccbd583e330b01584cd008fb0674"), new Parameter("start", "2016-11-29 00:00:00"), new Parameter("end", "2016-11-29 20:06:00"), new Parameter("nBest", "10") };

		String res = postMethod("salient", args, true);

		log.info("res = " + res);
	}

	public void recommendReportAndLearn() throws ParseException, Exception {
		Parameter[] args = new Parameter[7];
		int i = 0;
		args[i++] = new Parameter("company_pk", "8a28ccbd4d51f3be014d564cc91417d4");
		args[i++] = new Parameter("question", "who are you?");
		args[i++] = new Parameter("actualAnswer", "I'm an operator");
		args[i++] = new Parameter("selectedAnswer", "I'm an operator.");
		args[i++] = new Parameter("recommendedFAQ", "222 222 222 222 4 5 7");
		args[i++] = new Parameter("selectedFAQ", "222");
		args[i++] = new Parameter("time", "2017-5-2 16:37:45");

		String res = postMethod("recommendReportAndLearn", args, true);

		log.info("res = " + res);
	}

	public void learningFromReservoir() throws ParseException, Exception {
		Parameter[] args = new Parameter[1];
		int i = 0;
		args[i++] = new Parameter("companyPk", "8a28ccbd4d51f3be014d564cc91417d4");

		String res = postMethod("learningFromReservoir", args, false);

		log.info("res = " + res);
	}

	public void submitUnknown() throws ParseException, Exception {
		String fullUploadPath = Utility.createTemporaryFile("xlsx");
		fullUploadPath = "D:/solution/submitUnknown.xlsx";
		Parameter[] args = new Parameter[2];
		args[0] = new Parameter("companyPk", companyPk);
		args[1] = new Parameter("excelFile", fullUploadPath);

		String res = postMethod("submitUnknown", args);

		log.info("res = " + res);
	}

	public void submitUnknownQuestion() throws ParseException, Exception {
		Parameter[] args = new Parameter[3];
		args[0] = new Parameter("companyPk", companyPk);
		String question = "你们是否有在线客服??";
		String answer = "是的，有的。..";

		String res = postMethod("submitUnknownQuestion", args);

		log.info("res = " + res);
	}

	public void submitSupervised() throws ParseException, Exception {
		//		String fileName = Utility.workingDirectory + "submitSupervised.xlsx";
		String fileName = Utility.workingDirectory + "faqtest.xls";
		companyPk = "2d9080895c64830e015c67929ed603ef";
		//		ArrayList<String[]> list = Utility.readFromExcel(fileName, "Supervised", 2);
		//		ArrayMap<String, String> map = new ArrayMap<String, String>();
		//		for (String[] arr : list) {
		//			String question = arr[0];
		//			if (question == null)
		//				continue;
		//			String answer = arr[1];
		//			if (answer == null)
		//				continue;
		//			answer = Utility.format(answer);
		//			//			log.info("question = " + question);
		//			System.out.println(answer);
		//			if (answer == null)
		//				continue;
		//			//			map.put(question, answer);
		//			//			submitSupervisedFAQ(question, answer);
		//		}
		//
		//		for (String[] arr : list) {
		//			String question = arr[0];
		//			if (question == null)
		//				continue;
		//			String answer = arr[1];
		//			if (answer == null)
		//				continue;
		//			answer = Utility.format(answer);
		//			log.info("question = " + question);
		//			log.info("answer = " + answer);
		//			if (answer == null)
		//				continue;
		//			map.put(question, answer);
		//			submitSupervisedFAQ(question, answer);
		//		}
		//
		//		ArrayList<String[]> listNatural = Utility.readFromExcel(fileName, "related", 2);
		//		for (String[] arr : listNatural) {
		//			String question1 = arr[0];
		//			if (question1 == null)
		//				continue;
		//			String question = arr[1];
		//			if (question == null)
		//				continue;
		//			String answer = map.get(question1);
		//			log.info("question = " + question);
		//			log.info("answer = " + answer);
		//			update(question, answer);
		//		}

		Parameter[] args = new Parameter[3];
		args[0] = new Parameter("companyPk", companyPk);
		args[1] = new Parameter("excelFile", fileName);
		args[2] = new Parameter("respondent", "respondent");

		String res = postMethod("submitSupervised", args, true);

		log.info("res = " + res);
	}

	public void submitSupervisedFAQ() {
		Parameter[] args = new Parameter[4];
		args[0] = new Parameter("companyPk", companyPk);

		// String question = "能介绍一下在线客服吗?";
		// String answer =
		// "在线客服平台是一套可以通过一套客服系统平台处理来自微信、官网、APP等不同渠道进来的客户咨询，进行客户服务的系统平台";
		// String question = "在线客服是男的还是女的?";
		// String answer = "男的女的都有的。";
		// String question = "全员客服是什么？";
		// String answer = "全员客服是指客服利用移动APP应用，进行抢单服务，并以激励机制激发全员积极性。";
		// String question = "汽车首保有什么条件？";
		// String answer = "汽车的首保条件是汽车的使用期限不超过半年";
		// String question = "我昨天开的户为什么现在还没有短信通过通知呢？";
		// String answer = "账号需要您所选的开户营业部为您操作开户后，您才会收到。如果着急可以致电开户营业部咨询开户情况";
		// String question = "现在斯柯达汽车什么价格？";
		// String answer = "现在斯柯达汽车市场指导价为15万元";
		// String question = "现在大众汽车什么价格？";
		// String answer = "现在大众汽车市场指导价为13万元";
		// String question = "现在凌渡汽车是什么价格？";
		// String answer = "现在凌渡汽车市场指导价为20万元";
		// String question = "Any800是什么？";
		// String answer = "Any800就是全渠道";

		//		String question = "请告诉我你们公司的地址。";
		//		String answer = "我们公司在上海市。(业务知识库)";
		//		String question = "请问测试有几人";
		//		String answer = "测试有6人";

		String question = "请问测试有多少人";
		String answer = "测试有8人";
		//				String question = "请问测试有哪些人";
		//				String answer = "测试有A,B,C等";

		//		String question = "我想知道你们公司的联系方式。";
		//		String answer = "我们的联系方式是010-12345678。(业务知识库)";

		//		String question = "怎样办理登机手续？";
		//		String answer = "您可以到机场服务中心办理登机手续";

		// String question = "你好吗？";
		// String answer = "您好，欢迎使用自助问答系统。";

		args[1] = new Parameter("question", question, true);
		args[2] = new Parameter("answer", answer, true);
		args[3] = new Parameter("respondent", "respondent", true);
		String res;
		try {
			// res = postMethod("submitSupervisedFAQ", args, true);
			res = postMethod("updateSupervisedFAQ", args, true);

			log.info("res = " + res);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void submitSupervisedFAQ(String question, String answer) {
		Parameter[] args = new Parameter[4];
		args[0] = new Parameter("companyPk", companyPk);
		args[1] = new Parameter("question", question, true);
		args[2] = new Parameter("answer", answer, true);
		args[3] = new Parameter("respondent", "Cosmos", true);
		String res;
		try {
			res = postMethod("updateSupervisedFAQ", args, true);

			log.info("res = " + res);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteEntity() {
		//		companyPk = "00000000000000000000000000000000";
		companyPk = "ff8080815ce312bf015ce318b0621111";

		//		String question = "香蕉多少钱一斤";
		//		String answer = "5元钱一斤";
		String question = "周末你去哪里玩";
		String answer = "去佘山啊!";

		//		String question = "香蕉多少钱一斤";
		//		String answer = "10元一斤";
		//		

		Parameter[] args = new Parameter[3];

		args[0] = new Parameter("companyPk", companyPk);
		args[1] = new Parameter("question", question, true);
		args[2] = new Parameter("answer", answer, true);

		String res;
		try {
			res = postMethod("deleteEntity", args, true);
			log.info("res = " + res);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws ParseException, Exception {
		HttpClientWebApp webApp = new HttpClientWebApp();
		//		log.info("regex = " + CWSTagger.InstanceReader.regex);
		//		webApp.learning();
		//				webApp.updateFromDialogue();
		//		webApp.corpusTraining();

		//				webApp.updateTeletext();
		//						webApp.searchTeletext();
		//				webApp.updateFromExcel();
		//				webApp.topicClassification();
		webApp.update();
		webApp.search();
		//		webApp.recommendReportAndLearnPOST();
		//		webApp.deleteEntity();
		//		 webApp.submitSupervised();
		//		webApp.search();
		//		webApp.searchForQuestionByKeywords();

		//		webApp.submitSupervised();
		//		webApp.exportSupervised();
		//		webApp.deleteEntity();
		//				webApp.exportSupervised();
		//		webApp.submitSupervisedFAQ();
		// Utility.Timer timer = new Utility.Timer();
		// timer.start();
		// for (int i = 0;i < 10000; ++i){
		//		webApp.automaticResponse();
		//		webApp.exportUnsupervised();
		//		webApp.updateFromDialogue();

		// }
		// timer.cease();
		// webApp.submitUnknown();

		//		 webApp.search();
		//		webApp.exportSupervised();
		// webApp.exportSupervisedWithCriteria();
		//				webApp.exportUnsupervised();

		//		webApp.exportUnsupervisedWithCriteria();
		//		webApp.exportUnknownWithCriteria();
		// webApp.submitUnknownQuestion();
		//		 webApp.export();
		// webApp.exportUnknown();
		//		webApp.report();
		// webApp.salient();
		//		webApp.recommendReportAndLearn();
		//		webApp.learningFromReservoir();
	}

	public static Logger log = Logger.getLogger(HttpClientWebApp.class);
}
