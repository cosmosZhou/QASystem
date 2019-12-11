package com.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

public class HttpClientWebApp4NER {
//	HttpClient httpClient = new HttpClient();
	HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());

	enum Environment {
		local, test, product
	}

//	static Environment environment = Environment.test;
	static Environment environment = Environment.local;
	// static Environment environment = Environment.product;
	static String server;
	static {
		switch (environment) {
		case local:
			server = "http://127.0.0.1:8000/";
			break;
		case test:
			server = "http://192.168.2.39:8000/";
			break;
		case product:
			server = "http://172.16.0.7:7005/";
			break;
		}
	}

	public HttpClientWebApp4NER() {
		httpClient.setConnectionTimeout(50000);
		httpClient.setTimeout(50000);
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

	String postMethod(String function) throws HttpException, IOException {
		return postMethod(function, new Parameter[0], false);
	}

	String postMethod(String function, boolean bEncode) throws HttpException, IOException {
		return postMethod(function, new Parameter[0], bEncode);
	}

	String postMethod(String function, Parameter[] args) throws HttpException, IOException {
		return postMethod(function, args, false);
	}

	String postMethod(String function, Parameter[] args, boolean bEncode) throws HttpException, IOException {
		PostMethod method = new PostMethod(server + function);
		try {
			for (Parameter para : args) {

				method.addParameter(para.field,
						para.bEncode ? URLEncoder.encode(para.argument, "UTF-8") : para.argument);
			}
		} catch (IllegalArgumentException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		method.addRequestHeader("content-type", "application/x-www-form-urlencoded;charset=utf8");
		String result = null;

		httpClient.executeMethod(method);
		result = method.getResponseBodyAsString();

		method.releaseConnection();

		return result;
	}

	public String postMethod(String function, Map<String, String> data) throws HttpException, IOException {
		PostMethod method = new PostMethod(server + function);

		for (Map.Entry<String, String> p : data.entrySet()) {
			method.addParameter(p.getKey(), p.getValue());
		}

		method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf8");

		httpClient.executeMethod(method);

		String result = method.getResponseBodyAsString();
		method.releaseConnection();

		return result;
	}

	public String ner(String text, String service, String intent) throws HttpException, IOException {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("text", text);
		data.put("service", service);
		data.put("intent", intent);

		System.out.println("text = " + text);
		System.out.println("service = " + service);
		String res = postMethod("ner", data);
		log.info("res = " + res);
		return res;
	}

	public String python(String function, Map<String, String> data) throws HttpException, IOException {
		String res = postMethod(function, data);
		log.info("res = " + res);
		return res;
	}

	public String service(String text) throws HttpException, IOException {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("text", text);

		System.out.println("text = " + text);

		String res = postMethod("service", data);
		log.info("res = " + res);
		return res;
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

	public void corpusTraining() throws ParseException, Exception {
		String res = postMethod("corpusTraining");
		log.info("res = " + res);
	}

	public void learning() throws ParseException, Exception {
		String res = postMethod("learning");
		log.info("res = " + res);
	}

	public void test() throws ParseException, Exception {
		String res = postMethod("test");
		log.info("res = " + res);
	}

	public void report() throws ParseException, Exception {
		Parameter[] args = { new Parameter("companyPk", "8a28ccbd583e330b01584cd008fb0674"),
				new Parameter("start", "2016-11-29 00:00:00"), new Parameter("end", "2016-11-29 20:06:00"),
				new Parameter("period", "0") };

		String res = postMethod("report", args);

		log.info("res = " + res);
	}

	public void salient() throws ParseException, Exception {
		Parameter[] args = { new Parameter("companyPk", "8a28ccbd583e330b01584cd008fb0674"),
				new Parameter("start", "2016-11-29 00:00:00"), new Parameter("end", "2016-11-29 20:06:00"),
				new Parameter("nBest", "10") };

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

	public void classifyTopic() throws Exception {

		String brand = "ff8080813c25622d013c38913c694933";
		String sentiment = "3";
		String comment = "贵了，京东40多点，哎，非得到处比价才下单么。盒子关不紧";

		Parameter[] args = new Parameter[3];

		args[0] = new Parameter("brand", brand);
		args[1] = new Parameter("sentiment", sentiment);
		args[2] = new Parameter("comment", comment, true);

		String res;
		try {
			res = postMethod("classify", args);
			log.info("res = " + res);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static HttpClientWebApp4NER instance;

	static {
		synchronized (HttpClientWebApp4NER.class) {
			instance = new HttpClientWebApp4NER();
		}
	}

	public static void main(String[] args) throws ParseException, Exception {

		// webApp.classifyTopic();
		// log.info("regex = " + CWSTagger.InstanceReader.regex);
		instance.ner("播放一首寂寞沙洲冷", "music", "");
	}

	public static Logger log = Logger.getLogger(HttpClientWebApp4NER.class);
}
