package com.nineclient.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.robot.Repertoire;
import com.robot.QACouplet;

import com.robot.QASystem;
import com.util.Native;
import com.util.PropertyConfig;
import com.util.Utility;
import com.util.Utility.Couplet;

//121.43.150.14  root   clienT1!2019  ssh:22
//http://localhost:8080/QASystem/Knowledge/main
//http://localhost:8080/QASystem/Knowledge/phatic/你们公司有些什么业务
//http://localhost:8080/QASystem/Knowledge/qatype/你们公司业务有哪些
//http://localhost:8080/QASystem/Knowledge/similarity/你们公司有些什么业务/你们公司业务有哪些
//http://localhost:8080/QASystem/Knowledge/update/00000000000000000000000000000000/你们公司有些什么业务/海南航空等
//http://localhost:8080/QASystem/Knowledge/update/00000000000000000000000000000000/你们公司有什么业务/信诚人寿等
//http://localhost:8080/QASystem/Knowledge/search/00000000000000000000000000000000/你们公司有些啥业务
//http://121.43.150.14:9000/QASystem/Knowledge/main
//http://121.43.150.14:9000/QASystem/Knowledge/phatic/你们公司有些什么业务
//http://121.43.150.14:9000/QASystem/Knowledge/qatype/你们公司业务有哪些
//http://121.43.150.14:9000/QASystem/Knowledge/similarity/你们公司有些什么业务/你们公司业务有哪些
//http://121.43.150.14:9000/QASystem/Knowledge/update/00000000000000000000000000000000/你们公司有些什么业务/海南航空等
//
//tail -100f tomcat/logs/catalina.out 
//sh tomcat/bin/startup.sh python3
//solution/pytext/gunicorn.py --cpp=eigen
//

/**
 * https://blog.csdn.net/qq_38685503/article/details/82495083 the way to invoke
 * the method:
 * 
 * @author Cosmos
 *
 */
@Path("Knowledge")
public class Knowledge {
	public static Logger log = Logger.getLogger(Knowledge.class);
	static {
		if (SystemUtils.IS_OS_WINDOWS) {
			Utility.workingDirectory = "D:/360/solution/";
		} else {
			Utility.workingDirectory = "/home/zhoulizhi/solution/";
		}

		log.info("workingDirectory = " + Utility.workingDirectory);
		log.info("Knowledge is initialized successfully!");
	}

	
	/**
	 * search For Question By Keywords from the client perspective. by the use
	 * lucene algorithm.
	 * 
	 * @param request
	 * @return
	 */
	@POST
	@Path("/searchForQuestionByKeywords")
	@Produces(MediaType.TEXT_PLAIN)
	public String searchForQuestionByKeywordsPOST(@Context HttpServletRequest request) {

		// companyPk
		String companyPk = request.getParameter("companyPk");

		// question
		String keyword = request.getParameter("question");

		String result = "";
		try {
			keyword = URLDecoder.decode(keyword, "UTF-8");
			log.info("keyword = " + keyword + " with companyPk = " + companyPk);

			result = URLEncoder.encode(QASystem.instance.getRepertoire(companyPk)
					.searchForQuestionByKeywordsJSONArray(keyword).toJSONString(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@GET
	@Path("/searchForQuestionByKeywords")
	@Produces(MediaType.TEXT_PLAIN)
	public String searchForQuestionByKeywordsGET(@Context HttpServletRequest request) {
		return this.searchForQuestionByKeywordsPOST(request);
	}

	@POST
	@Path("/search")
	@Produces(MediaType.TEXT_PLAIN)
	public String search(@Context HttpServletRequest request) {

		// companyPk
		String companyPk = request.getParameter("companyPk");

		// question
		String question = request.getParameter("question");

		// List<String> companyList = new ArrayList<String>();
		// companyList.add(companyPk);
		String result = "";
		try {
			question = URLDecoder.decode(question, "UTF-8");
			log.info("question = " + question + " with companyPk = " + companyPk);
			return Utility.jsonify(QASystem.instance.getRepertoire(companyPk).search(question));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@GET
	@Path("/search/{companyPk}/{question}")
	@Produces("text/plain;charset=utf-8")
	public String search(@PathParam("companyPk") String companyPk, @PathParam("question") String question) throws JsonProcessingException, Exception {
		return Utility.jsonify(QASystem.instance.getRepertoire(companyPk).search(question));
	}

	@POST
	@Path("/searchTeletext")
	@Produces(MediaType.TEXT_PLAIN)
	public String searchTeletextPOST(@Context HttpServletRequest request) {

		// companyPk
		String companyPk = request.getParameter("companyPk");

		// question
		String question = request.getParameter("question");

		// List<String> companyList = new ArrayList<String>();
		// companyList.add(companyPk);
		String result = "";
		try {
			question = URLDecoder.decode(question, "UTF-8");
			log.info("question = " + question + " with companyPk = " + companyPk);

			result = URLEncoder.encode(
					QASystem.instance.getRepertoire(companyPk).searchTeletextJSONArray(question).toJSONString(),
					"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@GET
	@Path("/searchTeletext")
	@Produces(MediaType.TEXT_PLAIN)
	public String searchTeletextGET(@Context HttpServletRequest request) {
		return this.searchTeletextPOST(request);
	}

	@POST
	@Path("/automaticResponse")
	@Produces(MediaType.TEXT_PLAIN)
	public String automaticResponsePOST(@Context HttpServletRequest request) {

		// companyPk
		String companyPk = request.getParameter("companyPk");

		// question
		String question = request.getParameter("question");

		String result = "";
		try {
			question = URLDecoder.decode(question, "UTF-8");
			log.info("question = " + question + " with companyPk = " + companyPk);

			String answer = QASystem.instance.automaticResponse(companyPk, question);

			JSONObject object = new JSONObject();
			if (answer == null) {
				object.put("answer", "je suis désolé");
				object.put("reply", false);
			} else {
				object.put("answer", answer);
				object.put("reply", true);
			}

			result = URLEncoder.encode(object.toJSONString(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@GET
	@Path("/automaticResponse")
	@Produces(MediaType.TEXT_PLAIN)
	public String automaticResponseGET(@Context HttpServletRequest request) {
		return this.automaticResponsePOST(request);
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_PLAIN)
	public String updatePOST(@Context HttpServletRequest request) {
		String companyPk = request.getParameter("companyPk");

		// content
		String content = request.getParameter("content");

		try {
			content = URLDecoder.decode(URLDecoder.decode(content, "UTF-8"), "UTF-8");

			log.info("companyPk = " + companyPk + " with content = " + content);
			QASystem.instance.update(companyPk, content);
		} catch (Exception e) {
			log.info("companyPk = " + companyPk + "content = " + content);
			e.printStackTrace();
		}

		return "update";
	}

	@GET
	@Path("update/{company_pk}/{question}/{answer}")
	@Produces("text/plain;charset=utf-8")
	public String update(@PathParam("company_pk") String company_pk, @PathParam("question") String question,
			@PathParam("answer") String answer) throws Exception {
		return String.valueOf(QASystem.instance.update(company_pk, question, answer));
	}

	@POST
	@Path("/updateTeletext")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateTeletextPOST(@Context HttpServletRequest request) {
		String companyPk = request.getParameter("companyPk");

		// content
		String content = request.getParameter("content");
		String pk = request.getParameter("pk");
		String title = request.getParameter("title");
		String description = request.getParameter("description");

		try {
			content = URLDecoder.decode(content, "UTF-8");
			title = URLDecoder.decode(title, "UTF-8");
			description = URLDecoder.decode(description, "UTF-8");

			log.info("companyPk = " + companyPk + " with title = " + title);
			QASystem.instance.updateTeletext(companyPk, pk, title, description, content);
		} catch (Exception e) {
			log.info("companyPk = " + companyPk + "description = " + description);
			e.printStackTrace();
		}

		return "updateTeletext";
	}

	@GET
	@Path("/updateTeletext")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateTeletextGET(@Context HttpServletRequest request) {
		return this.updateTeletextPOST(request);
	}

	@POST
	@Path("/updateThreshold")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateThresholdPOST(@Context HttpServletRequest request) {
		String companyPk = request.getParameter("companyPk");
		// threshold
		double threshold = Double.parseDouble(request.getParameter("threshold")) / 100;
		try {
			log.info("companyPk = " + companyPk + " with threshold = " + threshold);
			Repertoire repertoire = QASystem.instance.getRepertoire(companyPk);
			repertoire.updateThreshold(threshold);
		} catch (Exception e) {
			log.info("error in updateThreshold, companyPk = " + companyPk + ", threshold = " + threshold);
			e.printStackTrace();
		}

		return "updateThreshold";
	}

	@GET
	@Path("/updateThreshold")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateThresholdGET(@Context HttpServletRequest request) {
		return this.updateThresholdPOST(request);
	}

	@POST
	@Path("/updateFromDialogue")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateFromDialoguePOST(@Context HttpServletRequest request) {
		final String companyPk = request.getParameter("companyPk");

		final String httpPath = request.getParameter("excelFile");
		String size = request.getParameter("size");
		// java.io.File excelDiskFileToBeDeleted = null;
		String result = null;
		JSONObject object = new JSONObject();
		try {

			log.info("companyPk = " + companyPk);
			log.info("with httpPath = " + httpPath);
			double period = Integer.parseInt(size) / 1000.0;
			period = ((int) period * 100) / 100;
			object.put("success", true);
			object.put("duration", period);
			log.info("DURATION = " + period + " seconds");

			QASystem.instance.execute(new Runnable() {
				@Override
				public void run() {
					try {
						String excelDisk;
						if (httpPath.toLowerCase().startsWith("http://")) {
							excelDisk = Utility.readFileFromURL(httpPath);
							// excelDiskFileToBeDeleted = new java.io.File(excelDisk);
						} else {
							excelDisk = httpPath;
						}

						// float cnt =
						// QASystem.instance.getRepertoire(companyPk).updateFromDialogueCheckingValidity(excelDisk)
						// / 1200f;
						// QASystem.instance.updateFromDialogue(companyPk, excelDisk);
						QASystem.instance.getRepertoire(companyPk).updateFromDialogue(excelDisk);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			result = URLEncoder.encode(object.toJSONString(), "UTF-8");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// if (excelDiskFileToBeDeleted != null)
			// excelDiskFileToBeDeleted.delete();
		}

		return result;
	}

	@GET
	@Path("/updateFromDialogue")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateFromDialogueGET(@Context HttpServletRequest request) {
		return this.updateFromDialoguePOST(request);
	}

	@POST
	@Path("/submitUnknown")
	@Produces(MediaType.TEXT_PLAIN)
	public String submitUnknownPOST(@Context HttpServletRequest request) {

		String companyPk = request.getParameter("companyPk");
		// excelFile
		String httpPath = request.getParameter("excelFile");
		// operator
		String respondent = request.getParameter("respondent");

		java.io.File excelDiskFileToBeDeleted = null;
		try {
			respondent = URLDecoder.decode(respondent, "UTF-8");
			log.info("submiting excelFile = " + httpPath + " with companyPk = " + companyPk);
			String excelDisk;

			if (httpPath.toLowerCase().startsWith("http://")) {
				excelDisk = Utility.readFileFromURL(httpPath);
				excelDiskFileToBeDeleted = new java.io.File(excelDisk);
			} else {
				excelDisk = httpPath;
			}
			QASystem.instance.getRepertoire(companyPk).submitUnknown(excelDisk, respondent);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (excelDiskFileToBeDeleted != null)
				excelDiskFileToBeDeleted.delete();
		}

		return "submitUnknown";
	}

	@GET
	@Path("/submitUnknown")
	@Produces(MediaType.TEXT_PLAIN)
	public String submitUnknownGET(@Context HttpServletRequest request) {
		return this.submitUnknownPOST(request);
	}

	@POST
	@Path("/submitUnknownQuestion")
	@Produces(MediaType.TEXT_PLAIN)
	public String submitUnknownQuestionPOST(@Context HttpServletRequest request) {

		String companyPk = request.getParameter("companyPk");
		String question = request.getParameter("question");
		String answer = request.getParameter("answer");
		// operator
		String respondent = request.getParameter("respondent");

		try {
			respondent = URLDecoder.decode(respondent, "UTF-8");
			question = URLDecoder.decode(question, "UTF-8");
			answer = URLDecoder.decode(answer, "UTF-8");

			log.info("submiting question = " + question + ", answer = " + answer + " with companyPk = " + companyPk);

			QASystem.instance.getRepertoire(companyPk).submitUnknown(question, answer, respondent);
		} catch (Exception e) {
			e.printStackTrace();

		}

		return "submitUnknownQuestion";
	}

	@GET
	@Path("/submitUnknownQuestion")
	@Produces(MediaType.TEXT_PLAIN)
	public String submitUnknownQuestionGET(@Context HttpServletRequest request) {
		return this.submitUnknownQuestionPOST(request);
	}

	/**
	 * submit an excel file consisting of a set of questions and answers
	 * 
	 * @param request
	 * @return
	 */
	@POST
	@Path("/submitSupervised")
	@Produces(MediaType.TEXT_PLAIN)
	public String submitSupervisedPOST(@Context HttpServletRequest request) {

		final String companyPk = request.getParameter("companyPk");
		// excelFile
		String httpPath = request.getParameter("excelFile");

//		java.io.File excelDiskFileToBeDeleted = null;

		JSONObject object = new JSONObject();
		try {
			// operator
			final String respondent = URLDecoder.decode(request.getParameter("respondent"), "UTF-8");
			log.info("submiting excelFile = " + httpPath + " with companyPk = " + companyPk);
			final String excelDisk;

			if (httpPath.toLowerCase().startsWith("http://")) {
				excelDisk = Utility.readFileFromURL(httpPath);
//				excelDiskFileToBeDeleted = new java.io.File(excelDisk);
			} else {
				excelDisk = httpPath;
			}

			// String[][] errorInformation =
			// QASystem.instance.getRepertoire(companyPk).checkValidityForSupervisedInsertion(excelDisk);
			// log.info("errorInformation.length = " + errorInformation.length);
			// if (errorInformation.length == 0) {
			object.put("success", true);
			QASystem.instance.execute(new Runnable() {
				public void run() {
					try {
						log.info("running submitSupervised with companyPk = " + companyPk);
						QASystem.instance.getRepertoire(companyPk).submitSupervised(excelDisk, respondent);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			object.put("success", true);
			e.printStackTrace();

		} finally {
			// if (excelDiskFileToBeDeleted != null)
			// excelDiskFileToBeDeleted.delete();
		}

		try {
			return URLEncoder.encode(object.toJSONString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
	}

	@GET
	@Path("/submitSupervised")
	@Produces(MediaType.TEXT_PLAIN)
	public String submitSupervisedGET(@Context HttpServletRequest request) {
		return this.submitSupervisedPOST(request);
	}

	/**
	 * submit a single pair of question and answer a warning should be issued if you
	 * try to submit the same or similar pair of question and answer
	 * 
	 * @param request
	 * @return
	 */
	@POST
	@Path("/submitSupervisedFAQ")
	@Produces(MediaType.TEXT_PLAIN)
	public String submitSupervisedFAQPOST(@Context HttpServletRequest request) {
		final String companyPk = request.getParameter("companyPk");
		String oldQuestion = request.getParameter("oldQuestion");
		String oldAnswer = request.getParameter("oldAnswer");

		log.info("companyPk = " + companyPk);
		JSONObject object = new JSONObject();
		try {
			// operator
			final String respondent = URLDecoder.decode(request.getParameter("respondent"), "UTF-8");
			log.info("respondent = " + respondent);
			final String newQuestion = URLDecoder.decode(request.getParameter("newQuestion"), "UTF-8");
			final String newAnswer = URLDecoder.decode(request.getParameter("newAnswer"), "UTF-8");

			log.info("newQuestion = " + newQuestion);
			log.info("newAnswer = " + newAnswer);

			// LikeEntity likeEntity = null;
			if (oldQuestion != null) {
				oldQuestion = URLDecoder.decode(oldQuestion, "UTF-8");
				oldAnswer = URLDecoder.decode(oldAnswer, "UTF-8");

				// log.info("oldQuestion = " + oldQuestion);
				// log.info("oldAnswer = " + oldAnswer);

				// likeEntity =
				// QASystem.instance.getRepertoire(companyPk).checkValidityForSupervisedInsertion(newQuestion,
				// newAnswer, oldQuestion);
			} else {
				// likeEntity =
				// QASystem.instance.getRepertoire(companyPk).checkValidityForSupervisedInsertion(newQuestion,
				// newAnswer, null);

			}
			// if (likeEntity != null) {
			// object.put("question", likeEntity.question);
			// object.put("answer", likeEntity.answer);
			// object.put("conflict", true);
			// object.put("message", "Like entities have been detected.");
			//
			// log.info("Like entities have been detected.");
			// } else {

			QASystem.instance.execute(new Runnable() {
				@Override
				public void run() {
					try {
						QASystem.instance.getRepertoire(companyPk).submitSupervised(newQuestion, newAnswer, respondent);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			object.put("conflict", false);
			object.put("message", "insertion succeeded.");
			log.info("insertion succeeded.");
			// }
		} catch (Exception e) {
			e.printStackTrace();
			object.put("conflict", false);
			object.put("message", e.getMessage());
		}

		try {
			return URLEncoder.encode(object.toJSONString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
	}

	@GET
	@Path("/submitSupervisedFAQ")
	@Produces(MediaType.TEXT_PLAIN)
	public String submitSupervisedFAQGET(@Context HttpServletRequest request) {
		return this.submitSupervisedFAQPOST(request);
	}

	/**
	 * submit a list of question and answer; a warning should be issued if you try
	 * to submit the same or similar pair of question and answer
	 * 
	 * @param request
	 * @return
	 */
	@POST
	@Path("/submitSupervisedFAQs")
	@Produces(MediaType.TEXT_PLAIN)
	public String submitSupervisedFAQsPOST(@Context HttpServletRequest request) {
		final String companyPk = request.getParameter("companyPk");

		log.info("companyPk = " + companyPk);
		JSONObject object = new JSONObject();
		try {
			// operator
			final String respondent = URLDecoder.decode(request.getParameter("respondent"), "UTF-8");
			log.info("respondent = " + respondent);

			String vector = URLDecoder.decode(request.getParameter("vector"), "UTF-8");
			JSONArray arr = JSONArray.parseArray(vector);
			int length = arr.size();
			final String[] question = new String[length];
			final String[] answer = new String[length];
			for (int i = 0; i < answer.length; i++) {
				JSONObject js = (JSONObject) arr.get(i);
				question[i] = js.getString("question");
				answer[i] = js.getString("answer");

				log.info("question = " + question[i]);
				log.info("answer = " + answer[i]);
			}

			QASystem.instance.execute(new Runnable() {
				@Override
				public void run() {
					try {
						QASystem.instance.getRepertoire(companyPk).submitSupervised(question, answer, respondent);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			object.put("conflict", false);
			object.put("message", "insertion succeeded.");
			log.info("insertion succeeded.");

		} catch (Exception e) {
			e.printStackTrace();
			object.put("conflict", false);
			object.put("message", e.getMessage());
		}

		try {
			return URLEncoder.encode(object.toJSONString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
	}

	@GET
	@Path("/submitSupervisedFAQs")
	@Produces(MediaType.TEXT_PLAIN)
	public String submitSupervisedFAQsGET(@Context HttpServletRequest request) {
		return this.submitSupervisedFAQsPOST(request);
	}

	@POST
	@Path("/updateSupervisedFAQ")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateSupervisedFAQPOST(@Context HttpServletRequest request) {

		String companyPk = request.getParameter("companyPk");
		String question = request.getParameter("question");
		String answer = request.getParameter("answer");
		// operator
		String respondent = request.getParameter("respondent");

		try {
			respondent = URLDecoder.decode(respondent, "UTF-8");
			question = URLDecoder.decode(question, "UTF-8");
			answer = URLDecoder.decode(answer, "UTF-8");
			QASystem.instance.getRepertoire(companyPk).submitSupervised(question, answer, respondent);
		} catch (Exception e) {
			e.printStackTrace();
			return "updateSupervisedFAQ has failed";
		}

		return "updateSupervisedFAQ";
	}

	@GET
	@Path("/updateSupervisedFAQ")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateSupervisedFAQGET(@Context HttpServletRequest request) {
		return this.updateSupervisedFAQPOST(request);
	}

	@POST
	@Path("/deleteEntity")
	@Produces(MediaType.TEXT_PLAIN)
	public String deleteEntityPOST(@Context HttpServletRequest request) {

		String companyPk = request.getParameter("companyPk");
		String question = request.getParameter("question");
		String answer = request.getParameter("answer");

		try {
			question = URLDecoder.decode(question, "UTF-8");
			answer = URLDecoder.decode(answer, "UTF-8");
			log.info("question = " + question);
			log.info("answer = " + answer);
			boolean drapeau = QASystem.instance.getRepertoire(companyPk).deleteEntity(question, answer);
			if (drapeau) {
				log.info("deleteEntity invoked, succeeded!");
			} else {
				log.info("deleteEntity invoked, but failed!");
			}
			return "{\"success\":\"" + drapeau + "\"}";
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "{\"success\":\"false\"}";
	}

	@GET
	@Path("/deleteEntity")
	@Produces(MediaType.TEXT_PLAIN)
	public String deleteEntityGET(@Context HttpServletRequest request) {
		return this.deleteEntityPOST(request);
	}

	@POST
	@Path("/export")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportPOST(@Context HttpServletRequest request) {
		String companyPk = request.getParameter("companyPk");

		// excelFile
		// String excelFile = request.getParameter("excelFile");
		String excelFile = null;
		try {
			log.info("exporting companyPk = " + companyPk);

			// String workingDirectory =
			// request.getSession().getServletContext().getRealPath("/");
			// log.info("request.getSession().getServletContext().getRealPath(/)
			// = "
			// + request.getSession().getServletContext().getRealPath("/"));
			// workingDirectory = workingDirectory.substring(0,
			// workingDirectory.length() - "/QASystem/".length());
			String workingDirectory = PropertyConfig.config.get("property", "pwd");
			log.info("workingDirectory = " + workingDirectory);

			excelFile = Utility.createTemporaryFile(workingDirectory + "/cdatas", "xlsx");
			QASystem.instance.getRepertoire(companyPk).export(excelFile);

			excelFile = excelFile.replace(workingDirectory, PropertyConfig.config.get("property", "downloadpath"));
			log.info("res = " + excelFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return excelFile;
	}

	@GET
	@Path("/export")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportGET(@Context HttpServletRequest request) {
		return this.exportPOST(request);
	}

	/**
	 * export Unknown knowledge With Criteria
	 */
	@POST
	@Path("/exportUnknownWithCriteria")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportUnknownWithCriteriaPOST(@Context HttpServletRequest request) {
		String companyPk = request.getParameter("companyPk");
		String criteria = request.getParameter("criteria");

		try {
			if (criteria != null)
				criteria = URLDecoder.decode(criteria, "UTF-8");

			log.info("exporting UNKNOWN knowledge With Criteria, companyPk = " + companyPk);
			log.info("criteria = " + criteria);

			return QASystem.instance.getRepertoire(companyPk).exportUnknownWithCriteria(criteria);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "exportUnknownWithCriteria failed";
	}

	@GET
	@Path("/exportUnknownWithCriteria")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportUnknownWithCriteriaGET(@Context HttpServletRequest request) {
		return this.exportUnknownWithCriteriaPOST(request);
	}

	/**
	 * exporting UNKNOWN knowledge
	 */
	@POST
	@Path("/exportUnknown")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportUnknownPOST(@Context HttpServletRequest request) {
		String companyPk = request.getParameter("companyPk");
		// excelFile
		String excelFile = null;

		try {
			log.info("exporting UNKNOWN knowledge, companyPk = " + companyPk);

			String workingDirectory = PropertyConfig.config.get("property", "pwd");
			log.info("workingDirectory = " + workingDirectory);

			excelFile = Utility.createTemporaryFile(workingDirectory + "/cdatas", "xlsx");
			QASystem.instance.getRepertoire(companyPk).exportUnknown(excelFile);

			excelFile = excelFile.replace(workingDirectory, PropertyConfig.config.get("property", "downloadpath"));
			log.info("res = " + excelFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return excelFile;
	}

	@GET
	@Path("/exportUnknown")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportUnknownGET(@Context HttpServletRequest request) {
		return this.exportUnknownPOST(request);
	}

	@POST
	@Path("/exportSupervised")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportSupervisedPOST(@Context HttpServletRequest request) {
		String companyPk = request.getParameter("companyPk");
		// excelFile
		String excelFile = null;

		try {
			log.info("exporting Supervised knowledge, companyPk = " + companyPk);

			String workingDirectory = PropertyConfig.config.get("property", "pwd");
			log.info("workingDirectory = " + workingDirectory);

			excelFile = Utility.createTemporaryFile(workingDirectory + "/cdatas", "xlsx");
			QASystem.instance.getRepertoire(companyPk).exportSupervised(excelFile);

			excelFile = excelFile.replace(workingDirectory, PropertyConfig.config.get("property", "downloadpath"));
			log.info("res = " + excelFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return excelFile;
	}

	@GET
	@Path("/exportSupervised")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportSupervisedGET(@Context HttpServletRequest request) {
		return this.exportSupervisedPOST(request);
	}

	@POST
	@Path("/exportSupervisedWithCriteria")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportSupervisedWithCriteriaPOST(@Context HttpServletRequest request) {
		String companyPk = request.getParameter("companyPk");
		String criteria = request.getParameter("criteria");

		try {
			if (criteria != null)
				criteria = URLDecoder.decode(criteria, "UTF-8");

			log.info("exporting Supervised With Criteria, companyPk = " + companyPk);
			log.info("criteria = " + criteria);

			return QASystem.instance.getRepertoire(companyPk).exportSupervisedWithCriteria(criteria);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "exportSupervisedWithCriteria failed";
	}

	@GET
	@Path("/exportSupervisedWithCriteria")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportSupervisedWithCriteriaGET(@Context HttpServletRequest request) {
		return this.exportSupervisedWithCriteriaPOST(request);
	}

	@POST
	@Path("/exportUnsupervised")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportUnsupervisedPOST(@Context HttpServletRequest request) {
		String companyPk = request.getParameter("companyPk");
		// excelFile
		String excelFile = null;

		try {
			log.info("exporting Unsupervised knowledge, companyPk = " + companyPk);

			String workingDirectory = PropertyConfig.config.get("property", "pwd");
			log.info("workingDirectory = " + workingDirectory);

			excelFile = Utility.createTemporaryFile(workingDirectory + "/cdatas", "xlsx");
			QASystem.instance.getRepertoire(companyPk).exportUnsupervised(excelFile);

			excelFile = excelFile.replace(workingDirectory, PropertyConfig.config.get("property", "downloadpath"));
			log.info("res = " + excelFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return excelFile;
	}

	@GET
	@Path("/exportUnsupervised")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportUnsupervisedGET(@Context HttpServletRequest request) {
		return this.exportUnsupervisedPOST(request);
	}

	@POST
	@Path("/exportUnsupervisedWithCriteria")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportUnsupervisedWithCriteriaPOST(@Context HttpServletRequest request) {
		String companyPk = request.getParameter("companyPk");
		String criteria = request.getParameter("criteria");

		try {
			if (criteria != null)
				criteria = URLDecoder.decode(criteria, "UTF-8");

			log.info("exporting Unsupervised knowledge, companyPk = " + companyPk);
			log.info("criteria = " + criteria);

			return QASystem.instance.getRepertoire(companyPk).exportUnsupervisedWithCriteria(criteria);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "exportUnsupervisedWithCriteria failed";
	}

	@GET
	@Path("/exportUnsupervisedWithCriteria")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportUnsupervisedWithCriteriaGET(@Context HttpServletRequest request) {
		return this.exportUnsupervisedWithCriteriaPOST(request);
	}

	@POST
	@Path("/salient")
	@Produces(MediaType.TEXT_PLAIN)
	public String salientPOST(@Context HttpServletRequest request) {
		String companyPk = request.getParameter("companyPk");

		String nBest = request.getParameter("nBest");
		// excelFile
		String start = request.getParameter("start");
		String end = request.getParameter("end");
		String jeson = "";
		JSONArray jsonArray = new JSONArray();
		try {
			log.info("companyPk = " + companyPk);
			log.info("nBest = " + nBest);
			log.info("start = " + start);
			log.info("end = " + end);

			for (Couplet<String, int[]> couplet : QASystem.report_top_concerns(companyPk, Integer.parseInt(nBest),
					start, end)) {
				JSONObject object = new JSONObject();
				object.put("question", couplet.x);

				object.put("recommendedCnt", couplet.y[0]);
				object.put("selectedCnt", couplet.y[1]);
				object.put("percent", couplet.y[2]);

				jsonArray.add(object);
			}

			jeson = URLEncoder.encode(jsonArray.toJSONString(), "UTF-8");
			// jeson = jsonArray.toJSONString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jeson;
	}

	@GET
	@Path("/salient")
	@Produces(MediaType.TEXT_PLAIN)
	public String salientGET(@Context HttpServletRequest request) {
		return this.salientPOST(request);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@POST
	@Path("/recommendReport")
	@Produces(MediaType.TEXT_PLAIN)
	public String recommendReportPOST(@Context HttpServletRequest request) {
		String report = request.getParameter("report");

		try {
			// report = URLDecoder.decode(report);
			report = URLDecoder.decode(report, "UTF-8");
			log.info("recommendation Report--->" + report);
			QASystem.report(report);

		} catch (Exception e) {

			log.info("recommendation Report error infomation:" + e.getMessage());
			e.printStackTrace();
			return "{\"success\":\"false\"}";
		}

		return "{\"success\":\"true\"}";
	}

	/**
	 * 智能推荐报表
	 * 
	 * @param request
	 * @return
	 */
	@GET
	@Path("/recommendReport")
	@Produces(MediaType.TEXT_PLAIN)
	public String recommendReportGET(@Context HttpServletRequest request) {
		return this.recommendReportPOST(request);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@POST
	@Path("/recommendReportAndLearn")
	@Produces(MediaType.TEXT_PLAIN)
	public String recommendReportAndLearnPOST(@Context HttpServletRequest request) {
		String selectedAnswer = request.getParameter("selectedAnswer");
		String actualAnswer = request.getParameter("actualAnswer");
		String company_pk = request.getParameter("company_pk");
		String question = request.getParameter("question");
		String time = request.getParameter("time");
		String recommendedFAQ = request.getParameter("recommendedFAQ");
		String selectedFAQ = request.getParameter("selectedFAQ");

		try {
			selectedAnswer = URLDecoder.decode(selectedAnswer, "UTF-8");
			actualAnswer = URLDecoder.decode(actualAnswer, "UTF-8");
			company_pk = URLDecoder.decode(company_pk, "UTF-8");
			question = URLDecoder.decode(question, "UTF-8");
			time = URLDecoder.decode(time, "UTF-8");
			recommendedFAQ = URLDecoder.decode(recommendedFAQ, "UTF-8");
			selectedFAQ = URLDecoder.decode(selectedFAQ, "UTF-8");

			log.info("company_pk = " + company_pk);
			log.info("question = " + question);
			log.info("actualAnswer = " + actualAnswer);
			log.info("selectedAnswer = " + selectedAnswer);
			log.info("recommendedFAQ = " + recommendedFAQ);
			log.info("selectedFAQ = " + selectedFAQ);
			log.info("time = " + time);

			QASystem.report(company_pk, question, actualAnswer, selectedAnswer, time, recommendedFAQ, selectedFAQ);

			if (actualAnswer != null && !actualAnswer.isEmpty()) {
				QASystem.instance.getRepertoire(company_pk).insertQACoupletRobot(question, actualAnswer,
						Utility.parseDateFormat(time));
			}
		} catch (Exception e) {

			log.info("recommendation Report error infomation:" + e.getMessage());
			e.printStackTrace();
			return "{\"success\":\"false\"}";
		}

		return "{\"success\":\"true\"}";
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	@GET
	@Path("/recommendReportAndLearn")
	@Produces(MediaType.TEXT_PLAIN)
	public String recommendReportAndLearnGET(@Context HttpServletRequest request) {
		return this.recommendReportAndLearnPOST(request);
	}

	@POST
	@Path("/report")
	@Produces(MediaType.TEXT_PLAIN)
	public String reportPOST(@Context HttpServletRequest request) {
		String jeson = null;
		try {
			String start = request.getParameter("start");
			String end = request.getParameter("end");
			String period = request.getParameter("period");
			String companyPk = request.getParameter("companyPk");
			JSONArray array = QASystem.report(companyPk, start, end, period);
			jeson = array.toJSONString();
			// jeson = URLEncoder.encode(array.toJSONString(), "UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jeson;
	}

	@GET
	@Path("/report")
	@Produces(MediaType.TEXT_PLAIN)
	public String reportGET(@Context HttpServletRequest request) {
		return this.reportPOST(request);
	}

	@POST
	@Path("/corpusTraining")
	@Produces(MediaType.TEXT_PLAIN)
	public String corpusTrainingPOST(@Context HttpServletRequest request) {

		try {
			// CWSTagger.main(Utility.workingDirectory);
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"success\":\"false\"}";
		}
		return "{\"success\":\"true\"}";
	}

	@GET
	@Path("/corpusTraining")
	@Produces(MediaType.TEXT_PLAIN)
	public String corpusTrainingGET(@Context HttpServletRequest request) {
		return this.corpusTrainingPOST(request);
	}

	@POST
	@Path("/learningFromReservoir")
	@Produces(MediaType.TEXT_PLAIN)
	public String learningFromReservoirPOST(@Context HttpServletRequest request) {

		try {
			String companyPk = request.getParameter("companyPk");
			QASystem.instance.getRepertoire(companyPk).learningFromReservoir();
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"success\":\"false\"}";
		}
		return "{\"success\":\"true\"}";
	}

	@GET
	@Path("/learningFromReservoir")
	@Produces(MediaType.TEXT_PLAIN)
	public String learningFromReservoirGET(@Context HttpServletRequest request) {
		return this.learningFromReservoirPOST(request);
	}

	@POST
	@Path("/insertQACouplet")
	@Produces(MediaType.TEXT_PLAIN)
	public String insertQACoupletPOST(@Context HttpServletRequest request) {

		try {
			final String companyPk = request.getParameter("companyPk");
			final String question = URLDecoder.decode(request.getParameter("question"), "UTF-8");
			final String answer = URLDecoder.decode(request.getParameter("answer"), "UTF-8");
			final String respondent = request.getParameter("respondent");
			final int origin = Integer.parseInt(request.getParameter("origin"));

			log.info("Insert into Reservoir: ");
			log.info("companyPk = " + companyPk);
			log.info("question = " + question);
			log.info("answer = " + answer);
			log.info("respondent = " + respondent);
			log.info("origin = " + origin + " = " + QACouplet.parseIntToOrigin(origin));

			QASystem.instance.execute(new Runnable() {
				@Override
				public void run() {
					try {
						QASystem.instance.getRepertoire(companyPk).insertQACouplet(question, answer, respondent,
								origin);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			});

		} catch (Exception e) {
			e.printStackTrace();
			return "{\"success\":\"false\"}";
		}
		return "{\"success\":\"true\"}";
	}

	@GET
	@Path("/insertQACouplet")
	@Produces(MediaType.TEXT_PLAIN)
	public String insertQACoupletGET(@Context HttpServletRequest request) {
		return this.insertQACoupletPOST(request);
	}

	@POST
	@Path("/updateQACouplet")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateQACoupletPOST(@Context HttpServletRequest request) {

		try {
			final String companyPk = request.getParameter("companyPk");
			final String oldQuestion = URLDecoder.decode(request.getParameter("oldQuestion"), "UTF-8");
			final String oldAnswer = URLDecoder.decode(request.getParameter("oldAnswer"), "UTF-8");
			final String newQuestion = URLDecoder.decode(request.getParameter("newQuestion"), "UTF-8");
			final String newAnswer = URLDecoder.decode(request.getParameter("newAnswer"), "UTF-8");
			final String respondent = request.getParameter("respondent");
			final int origin = Integer.parseInt(request.getParameter("origin"));

			log.info("Update the Reservoir: ");
			log.info("companyPk = " + companyPk);
			log.info("oldQuestion = " + oldQuestion);
			log.info("oldAnswer = " + oldAnswer);
			log.info("newQuestion = " + newQuestion);
			log.info("newAnswer = " + newAnswer);
			log.info("respondent = " + respondent);
			log.info("origin = " + origin + " = " + QACouplet.parseIntToOrigin(origin));

			QASystem.instance.execute(new Runnable() {
				@Override
				public void run() {
					try {
						QASystem.instance.getRepertoire(companyPk).updateQACouplet(oldQuestion, oldAnswer, newQuestion,
								newAnswer, respondent, origin);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			});

		} catch (Exception e) {
			e.printStackTrace();
			return "{\"success\":\"false\"}";
		}
		return "{\"success\":\"true\"}";
	}

	@GET
	@Path("/updateQACouplet")
	@Produces(MediaType.TEXT_PLAIN)
	public String updateQACoupletGET(@Context HttpServletRequest request) {
		return this.updateQACoupletPOST(request);
	}

	@GET
	@Path("phatic/{text}")
	@Produces("text/plain;charset=utf-8")
	public String phatic(@PathParam("text") String text) throws Exception {
		return String.valueOf(Native.phatic(text));
	}

	@POST
	@Path("phatic")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/plain;charset=utf-8")
	public String phatic(@Context HttpServletRequest request) throws Exception {
		return String.valueOf(Native.phatic(request.getParameter("text")));
	}

	@GET
	@Path("qatype/{text}")
	@Produces("text/plain;charset=utf-8")
	public String qatype(@PathParam("text") String text) throws Exception {
		return String.valueOf(Native.qatype(text));
	}

	@POST
	@Path("qatype")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/plain;charset=utf-8")
	public String qatype(@Context HttpServletRequest request) throws Exception {
		return String.valueOf(Native.qatype(request.getParameter("text")));
	}

	@GET
	@Path("similarity/{x}/{y}")
	@Produces("text/plain;charset=utf-8")
	public String similarity(@PathParam("x") String x, @PathParam("y") String y) throws Exception {
		return String.valueOf(Native.similarity(x, y));
	}

	@POST
	@Path("similarity")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("text/plain;charset=utf-8")
	public String similarity(@Context HttpServletRequest request) throws Exception {
		return String.valueOf(Native.similarity(request.getParameter("x"), request.getParameter("y")));
	}

	@GET
	@Path("main")
	@Produces("text/plain;charset=utf-8")
	public String main() throws Exception {
		return String.valueOf(Native.main());
	}

}
