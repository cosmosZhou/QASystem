package com.nineclient.mongodb.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;
//import com.nineclient.common.StringHelper;
import com.nineclient.mongodb.dao.impl.MongodbDaoImpl;
import com.nineclient.mongodb.service.MongodbService;
import com.nineclient.mongodb.utils.MongoDbUtils;
//import com.nineclient.talk.service.dto.EcChatRecordsDTO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class MongodbServiceImpl implements MongodbService {

	public static MongodbDaoImpl daoImpl = MongodbDaoImpl.getInstance();

	private transient static Logger logger = LoggerFactory.getLogger(MongodbServiceImpl.class);

	/**
	 * 保存会话记录到mongodb
	 * 
	 * @param model
	 */
	// public void saveEcChatRecords(EcChatRecordsDTO model) {
	// if (model != null) {
	//
	// BasicDBObject insertObject = new BasicDBObject();
	//
	// insertObject.put("pk", model.getPk() == null ? "" : model.getPk());
	// insertObject.put("chat_start_time",
	// model.getChatStartTime() == null ? "" :
	// MongoDbUtils.formatDate(model.getChatStartTime(), 0));
	// insertObject.put("chat_end_time",
	// model.getChatEndTime() == null ? "" :
	// MongoDbUtils.formatDate(model.getChatEndTime(), 0));
	// insertObject.put("chat_duration", model.getChatDuration());
	// insertObject.put("chat_type", model.getChatType() == null ? "" :
	// model.getChatType());
	// insertObject.put("company_pk", model.getCompanyPk() == null ? "" :
	// model.getCompanyPk());
	// insertObject.put("delete_ip", model.getDeleteIp() == null ? "" :
	// model.getDeleteIp());
	// insertObject.put("delete_pk", model.getDeletePk() == null ? "" :
	// model.getDeletePk());
	// insertObject.put("delete_time",
	// model.getDeleteTime() == null ? "" :
	// MongoDbUtils.formatDate(model.getDeleteTime(), 0));
	// insertObject.put("department_pk", model.getDepartmentPk() == null ? "" :
	// model.getDepartmentPk());
	// insertObject.put("exit_type", model.getExitType() == null ? "" :
	// model.getExitType());
	// insertObject.put("insert_ip", model.getInsertIp() == null ? "" :
	// model.getInsertIp());
	// insertObject.put("insert_pk", model.getInsertPk() == null ? "" :
	// model.getInsertPk());
	// insertObject.put("insert_time",
	// model.getInsertTime() == null ? "" :
	// MongoDbUtils.formatDate(model.getInsertTime(), 0));
	// insertObject.put("is_delete", model.getIsDelete());
	// insertObject.put("keyword_pk", model.getKeywordPk() == null ? "" :
	// model.getKeywordPk());
	// insertObject.put("operator_pk", model.getOperatorPk() == null ? "" :
	// model.getOperatorPk());
	// insertObject.put("option_pk", model.getOptionPk() == null ? "" :
	// model.getOperatorPk());
	// insertObject.put("queue_start_time",
	// model.getQueueStartTime() == null ? "" :
	// MongoDbUtils.formatDate(model.getQueueStartTime(), 0));
	// insertObject.put("queue_end_time",
	// model.getQueueEndTime() == null ? "" :
	// MongoDbUtils.formatDate(model.getQueueEndTime(), 0));
	// insertObject.put("referrerurl_pk", model.getReferrerurlPk() == null ? ""
	// : model.getReferrerurlPk());
	// insertObject.put("request_time",
	// model.getRequestTime() == null ? "" :
	// MongoDbUtils.formatDate(model.getRequestTime(), 0));
	// insertObject.put("satisfaction_memo",
	// model.getSatisfactionMemo() == null ? "" : model.getSatisfactionMemo());
	// insertObject.put("satisfaction_pk", model.getSatisfactionPk() == null ?
	// "" : model.getSatisfactionPk());
	// insertObject.put("next_satisfaction_pk",
	// model.getNextSatisfactionPk() == null ? "" :
	// model.getNextSatisfactionPk());
	// insertObject.put("searchengine_pk", model.getSearchenginePk() == null ?
	// "" : model.getSearchenginePk());
	// insertObject.put("topic_memo", model.getTopicMemo() == null ? "" :
	// model.getTopicMemo());
	// insertObject.put("update_ip", model.getUpdateIp() == null ? "" :
	// model.getUpdateIp());
	// insertObject.put("update_pk", model.getUpdatePk() == null ? "" :
	// model.getUpdatePk());
	// insertObject.put("update_time",
	// model.getUpdateTime() == null ? "" :
	// MongoDbUtils.formatDate(model.getUpdateTime(), 0));
	// insertObject.put("version", model.getVersion());
	// insertObject.put("visitor_id", model.getVisitorId() == null ? "" :
	// model.getVisitorId());
	// insertObject.put("visitor_ip", model.getVisitorIp() == null ? "" :
	// model.getVisitorIp());
	// insertObject.put("visitor_ip_code", model.getVisitorIpCode() == null ? ""
	// : model.getVisitorIpCode());
	// insertObject.put("visitor_ip_name", model.getVisitorIpName() == null ? ""
	// : model.getVisitorIpName());
	// insertObject.put("visitor_name", model.getVisitorName() == null ? "" :
	// model.getVisitorName());
	// insertObject.put("memo", model.getMemo() == null ? "" : model.getMemo());
	// insertObject.put("score", model.getScore());
	// insertObject.put("score_name", model.getScoreName() == null ? "" :
	// model.getScoreName());
	// insertObject.put("score_time",
	// model.getScoreTime() == null ? "" :
	// MongoDbUtils.formatDate(model.getScoreTime(), 0));
	// insertObject.put("operator_conversations",
	// model.getOperatorConversations());
	// insertObject.put("visters_conversations",
	// model.getVistersConversations());
	// insertObject.put("response_start_time", model.getResponseStartTime() ==
	// null ? ""
	// : MongoDbUtils.formatDate(model.getResponseStartTime(), 0));
	// insertObject.put("response_end_time",
	// model.getResponseEndTime() == null ? "" :
	// MongoDbUtils.formatDate(model.getResponseEndTime(), 0));
	// insertObject.put("speed_response", model.getSpeedResponse());
	// insertObject.put("speed_response_num", model.getSpeedResponseNum());
	// insertObject.put("member_id", model.getMemberId() == null ? "" :
	// model.getMemberId());
	// insertObject.put("trench", model.getTrench() == null ? "" :
	// model.getTrench());
	// insertObject.put("is_satisfaction", model.getIsSatisfaction());
	// insertObject.put("user_source", model.getUserSource() == null ? "" :
	// model.getUserSource());
	// insertObject.put("topic_pks", model.getTopicPks() == null ? "" :
	// model.getTopicPks());
	// insertObject.put("topic_names", model.getTopicNames() == null ? "" :
	// model.getTopicNames());
	// insertObject.put("msg_content", model.getMsgContent() == null ? "" :
	// model.getMsgContent());
	// insertObject.put("wx_callback_url", model.getWxCallbackUrl() == null ? ""
	// : model.getWxCallbackUrl());
	// // 插入到mongodb中
	// /**
	// * TODO 判断pk若为空则添加，否则修改数据
	// */
	// if (model.getPk() == null || "".equals(model.getPk())) {
	// daoImpl.insert(MongoDbUtils.MONGO_DB, MongoDbUtils.MONGO_COLLECTION,
	// insertObject);
	//
	// } else {
	// BasicDBObject whereFields = new BasicDBObject();
	// whereFields.put("pk", model.getPk());
	//
	// DBObject setFields = new BasicDBObject("$set", insertObject);
	// daoImpl.update(MongoDbUtils.MONGO_DB, MongoDbUtils.MONGO_COLLECTION,
	// whereFields, setFields, true);
	// }
	// }
	// }

	public static void main(String args[]) {

		MongodbServiceImpl mongodbServiceImpl = new MongodbServiceImpl();
		mongodbServiceImpl.logging("clazz", "method", "message");

		if (true)
			return;
		// BasicDBObject o = new BasicDBObject();
		// o.put("pk", "22222");
		// o.put("length", "0");
		// o.put("from", "jack");
		// o.put("to", "stefen");
		// o.put("content", "444444444444444444444444");
		// o.put("time", String.valueOf(System.currentTimeMillis()));
		// // daoImpl.insert(MongoDbUtils.MONGO_DB, "test", o);
		//
		// BasicDBObject queryObject = new BasicDBObject();
		// queryObject.put("pk", "22222");
		// queryObject.put("messageId", "123");
		//
		// DBObject dbObject = daoImpl.findOne(MongoDbUtils.MONGO_DB,
		// MongoDbUtils.MONGO_IM_COLLECTION, queryObject);
		// System.out.println(JsonUtil.beanToJson(dbObject));
		// System.out.println("#################################################################");
		//
		// BasicDBObject queryObject1 = new BasicDBObject();
		// queryObject1.put("time", new BasicDBObject("$lt",
		// dbObject.get("time")));
		// queryObject1.put("pk", "22222");
		//
		// BasicDBObject sortObject = new BasicDBObject();
		// sortObject.put("time", -1);
		//
		// List<DBObject> list = daoImpl.find(MongoDbUtils.MONGO_DB,
		// MongoDbUtils.MONGO_IM_COLLECTION, queryObject1);
		// System.out.println(JsonUtil.beanToJson(list));

		// BasicDBObject queryObject = new BasicDBObject();
		// queryObject.put("pk", "22222");
		// BasicDBObject sortObject = new BasicDBObject();
		// sortObject.put("time", -1);
		//

		// // 查询条件的pk
		// BasicDBObject queryIndexObject = new BasicDBObject();
		// queryIndexObject.put("to",
		// "8a28ccbd544b921901544c20a9930bc6-tang002");
		// queryIndexObject.put("status", "0");
		//
		// // 按照时间倒叙
		// BasicDBObject sortObject = new BasicDBObject();
		// sortObject.put("time", -1);
		//
		// List<DBObject> dbList = null;
		//
		// dbList = daoImpl.find(MongoDbUtils.MONGO_DB,
		// MongoDbUtils.MONGO_IM_COLLECTION, queryIndexObject, sortObject);
		//
		// JSONArray array = new JSONArray();
		// JSONObject dbJson = null;
		// for (DBObject obj : dbList) {
		// dbJson = new JSONObject();
		// dbJson.put("pk", obj.get("pk"));
		// dbJson.put("length", obj.get("length"));
		// dbJson.put("from", obj.get("from"));
		// dbJson.put("to", obj.get("to"));
		// dbJson.put("content", obj.get("content"));
		// dbJson.put("time", obj.get("time"));
		// dbJson.put("messageId", obj.get("messageId"));
		// dbJson.put("resource", obj.get("resource"));
		// dbJson.put("nickname", obj.get("nickname"));
		// dbJson.put("status", obj.get("status"));
		// array.add(dbJson);
		// }
		//
		// System.out.println(JsonUtil.beanToJson(array));

		// 查询条件的pk
		BasicDBObject queryObject = new BasicDBObject();
		queryObject.put("pk", "8a28ccbd5668011b015668eac4520031");

		// 更新的数据
		BasicDBObject updateObject = new BasicDBObject();
		updateObject.put("status", "read");

		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", updateObject);

		// 更新mongo里的数据
		daoImpl.update(MongoDbUtils.MONGO_DB, MongoDbUtils.MONGO_COLLECTION, queryObject, updateObj, true);

	}

	/**
	 * 查询
	 */
	public List<String> queryHistoryOperator(String companyPk, String visitorId, String distinctKey) {
		List<String> operatorPkList = new ArrayList<String>();

		if (companyPk != null) {
			BasicDBObject queryObject = new BasicDBObject();

			queryObject.put("company_pk", companyPk);
			queryObject.put("visitor_id", visitorId);
			BasicDBObject sortObject = new BasicDBObject();
			sortObject.put("request_time", -1);
			List<DBObject> dbList = daoImpl.find(MongoDbUtils.MONGO_DB, MongoDbUtils.MONGO_COLLECTION, queryObject,
					sortObject);
			if (dbList != null && dbList.size() > 0) {
				HashMap<String, String> map = new HashMap<String, String>();
				for (int i = 0; i < dbList.size(); i++) {
					String pk = (String) dbList.get(i).get("operator_pk");
					if (pk != null && !"".equals(pk) && map.get(pk) == null) {
						map.put(pk, pk);
						operatorPkList.add(pk);
					}
				}
			}
		}
		return operatorPkList;
	}

	/**
	 * (non-Javadoc)
	 * <p>
	 * Title: saveEcChatContent
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param chatId
	 * @param content
	 * @see com.nineclient.mongodb.service.MongodbService#saveEcChatContent(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public boolean saveEcChatContent(String chatId, String content) {
		// TODO Auto-generated method stub
		BasicDBObject whereFields = new BasicDBObject();
		whereFields.put("pk", chatId);

		BasicDBObject insertObject = new BasicDBObject();
		insertObject.put("msg_content", content);
		DBObject setFields = new BasicDBObject("$set", insertObject);
		return daoImpl.update(MongoDbUtils.MONGO_DB, MongoDbUtils.MONGO_COLLECTION, whereFields, setFields, true);
	}

	/**
	 * (non-Javadoc)
	 * <p>
	 * Title: getMsgContent
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param chatId
	 * @param companyPk
	 * @return
	 * @see com.nineclient.mongodb.service.MongodbService#getMsgContent(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public String getMsgContent(String chatId, String companyPk) {
		// TODO Auto-generated method stub
		String msgContent = null;
		if (companyPk != null) {
			BasicDBObject queryObject = new BasicDBObject();
			queryObject.put("pk", chatId);
			queryObject.put("company_pk", companyPk);
			BasicDBObject sortObject = new BasicDBObject();
			sortObject.put("request_time", -1);
			List<DBObject> dbList = daoImpl.find(MongoDbUtils.MONGO_DB, MongoDbUtils.MONGO_COLLECTION, queryObject,
					sortObject);
			if (null != dbList && dbList.size() > 0) {
				for (int i = 0; i < dbList.size(); i++) {
					msgContent = (String) dbList.get(i).get("msg_content");
				}
			}
		}
		return msgContent;
	}

	/**
	 * (non-Javadoc)
	 * <p>
	 * Title: isChatIdExist
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param chatId
	 * @return
	 * @see com.nineclient.mongodb.service.MongodbService#isChatIdExist(java.lang.String)
	 */
	@Override
	public boolean isChatIdExist(String chatId) {
		// TODO Auto-generated method stub
		boolean isExist = false;
		// if(!StringHelper.checkNull(chatId)){
		// BasicDBObject queryObject = new BasicDBObject();
		// queryObject.put("pk", chatId);
		// queryObject.put("chat_start_time", QueryOperators.NE);
		// queryObject.put("chat_end_time", QueryOperators.NE);
		// BasicDBObject sortObject = new BasicDBObject();
		// sortObject.put("request_time", -1 );
		// List<DBObject> dbList = daoImpl.find(MongoDbUtils.MONGO_DB,
		// MongoDbUtils.MONGO_COLLECTION, queryObject,sortObject);
		// if(null != dbList && dbList.size() > 0){
		// isExist = true;
		// }else{
		// isExist = false;
		// }
		// }
		return isExist;
	}

	/**
	 * 查询im的对话记录
	 * 
	 * @param pk
	 * @param start
	 * @param count
	 * @return
	 */
	@Override
	public JSONArray getImRecords(String pk, String messageId, int count) {

		// 查询条件的pk
		BasicDBObject queryIndexObject = new BasicDBObject();
		queryIndexObject.put("pk", pk);

		// 按照时间倒叙
		BasicDBObject sortObject = new BasicDBObject();
		sortObject.put("time", -1);

		List<DBObject> dbList = null;
		// if (StringHelper.checkNull(messageId)) {
		//
		// dbList = daoImpl.find(MongoDbUtils.MONGO_DB,
		// MongoDbUtils.MONGO_IM_COLLECTION, queryIndexObject, sortObject,
		// 0, count);
		// } else {
		//
		// queryIndexObject.put("messageId", messageId);
		//
		// // 查询messageId为指定Id的数据
		// DBObject dbObject = daoImpl.findOne(MongoDbUtils.MONGO_DB,
		// MongoDbUtils.MONGO_IM_COLLECTION,
		// queryIndexObject);
		//
		// // 设定查询条件为时间小于前面查到数据
		// BasicDBObject queryLimitObject = new BasicDBObject();
		// queryLimitObject.put("time", new BasicDBObject("$lt",
		// dbObject.get("time")));
		// queryLimitObject.put("pk", pk);
		//
		// dbList = daoImpl.find(MongoDbUtils.MONGO_DB,
		// MongoDbUtils.MONGO_IM_COLLECTION, queryLimitObject, sortObject,
		// count);
		// }

		JSONArray array = new JSONArray();
		JSONObject dbJson = null;
		for (DBObject obj : dbList) {
			dbJson = new JSONObject();
			dbJson.put("pk", obj.get("pk"));
			dbJson.put("length", obj.get("length"));
			dbJson.put("from", obj.get("from"));
			dbJson.put("to", obj.get("to"));
			dbJson.put("content", obj.get("content"));
			dbJson.put("time", obj.get("time"));
			dbJson.put("messageId", obj.get("messageId"));
			array.add(dbJson);
		}
		return array;
	}

	/**
	 * 查询未读的IM对话
	 * 
	 * @param username
	 * @param pkList
	 * @return
	 */
	@Override
	public JSONArray getUnreadRecord(String username, List<String> pkList) {

		// 查询条件的pk
		BasicDBObject queryIndexObject = new BasicDBObject();
		queryIndexObject.put("to", username); // 接收者的username
		queryIndexObject.put("status", "unread"); // 消息是未读消息

		// 按照时间倒叙
		BasicDBObject sortObject = new BasicDBObject();
		sortObject.put("time", -1);

		List<DBObject> dbList = null;
		JSONObject unreadObject = null;
		DBObject lastObject = null;
		JSONArray array = new JSONArray();

		for (String pk : pkList) {

			// 查询的IM对话的pk
			queryIndexObject.put("pk", pk);

			// 从mongo里面查询对话未读的记录
			dbList = daoImpl.find(MongoDbUtils.MONGO_DB, MongoDbUtils.MONGO_IM_COLLECTION, queryIndexObject,
					sortObject);

			if (dbList == null || dbList.size() == 0) {
				// 如果这个坐席没有未读消息，不记录
				continue;
			}

			logger.info("坐席" + username + "  pk为 " + pk + "  的未读消息条数为：" + dbList.size());

			// 最后一条消息
			lastObject = dbList.get(0);

			unreadObject = new JSONObject();

			// 未读的条数
			unreadObject.put("unreadCount", dbList.size());

			// 最后一条未读的时间
			unreadObject.put("time", lastObject.get("time"));

			// 最后一条未读的内容
			unreadObject.put("content", lastObject.get("content"));

			// 最后一条未读的messageId
			unreadObject.put("messageId", lastObject.get("messageId"));

			// 最后一条未读的昵称
			unreadObject.put("nickname", lastObject.get("nickname"));

			// 发送者的username
			unreadObject.put("from", lastObject.get("from"));

			// 最后一条未读的设备
			unreadObject.put("resource", lastObject.get("resource"));

			// 最后一条未读的IM对话pk
			unreadObject.put("pk", lastObject.get("pk"));

			array.add(unreadObject);
		}

		return array;
	}

	/**
	 * 设置消息已读
	 * 
	 * @param imPk
	 * @param username
	 * @return
	 */
	@Override
	public void setRecordRead(String imPk, String username) {

		logger.info("已读的消息pk为---->" + imPk + "   接收者为---->" + username);

		// 查询条件的pk
		BasicDBObject queryObject = new BasicDBObject();
		queryObject.put("pk", imPk);
		queryObject.put("to", username);

		// 更新的数据
		BasicDBObject updateObject = new BasicDBObject();
		updateObject.put("status", "read");

		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", updateObject);

		// 更新mongo里的数据
		daoImpl.update(MongoDbUtils.MONGO_DB, MongoDbUtils.MONGO_IM_COLLECTION, queryObject, updateObj, false);
	}

	/**
	 * 
	 * @Title: saveOperatorBehaviorTime @Description:
	 *         IM群聊保存坐席离开群时间 @param @param operatorName @param @param
	 *         groupId @param @param leaveTime 设定文件 @return void 返回类型 @throws
	 */
	public void saveOperatorBehaviorTime(String operatorName, String groupId, String type, String time) {
		BasicDBObject o = new BasicDBObject();
		o.put("operatorName", operatorName);
		o.put("groupId", groupId);
		o.put("type", type);// type:leave
		o.put("time", time);
		o.put("insertTime", String.valueOf(System.currentTimeMillis()));
		daoImpl.insert(MongoDbUtils.MONGO_DB, MongoDbUtils.MONGO_OP_BEHAVIOR_COLLECTION, o);
	}

	public void logging(String clazz, String method, String message) {
		BasicDBObject o = new BasicDBObject();
		o.put("clazz", clazz);
		o.put("method", method);
		o.put("message", message);
		o.put("time", new Date());
		// o.put("insertTime", String.valueOf(System.currentTimeMillis()));
		daoImpl.insert(MongoDbUtils.MONGO_DB, MongoDbUtils.MONGO_COLLECTION, o);
	}

	/**
	 * (non-Javadoc)
	 * <p>
	 * Title: getIMGroupRecords
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param startTime
	 * @param endTime
	 * @param groupId
	 * @see com.nineclient.mongodb.service.MongodbService#getIMGroupRecords(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public JSONArray getIMGroupRecords(String groupId, String messageId, int count, String operatorname) {
		JSONArray array = null;
		// if(!StringHelper.checkNull(groupId)){
		// BasicDBObject queryObject = new BasicDBObject();
		// queryObject.put("groupId", groupId);
		// BasicDBObject sortObject = new BasicDBObject();
		// sortObject.put("sendtime", -1);
		// List<DBObject> dbList = null;
		// if(StringHelper.checkNull(messageId)){
		// dbList = daoImpl.find(MongoDbUtils.MONGO_DB,
		// MongoDbUtils.MONGO_IM_GROUP_COLLECTION, queryObject,
		// sortObject,0,count);
		// logger.info("getIMGroupRecords传入messageId为空，opratorname=[{}]",
		// new String[]{operatorname});
		// }else{
		// String time = getMessageTimeByMID(groupId, messageId);
		// queryObject.put("sendtime", new BasicDBObject("$lt",time));
		// dbList = daoImpl.find(MongoDbUtils.MONGO_DB,
		// MongoDbUtils.MONGO_IM_GROUP_COLLECTION, queryObject,
		// sortObject,count);
		// }
		// JSONObject object = null;
		// array = new JSONArray();
		// if (null != dbList && dbList.size() > 0) {
		// for(DBObject dbobj : dbList){
		// object = new JSONObject();
		// object.put("pk", dbobj.get("pk"));
		// // groupId
		// object.put("groupId", dbobj.get("groupId"));
		// // 发送者的username
		// object.put("from", dbobj.get("from"));
		// // 发送内容
		// object.put("content", dbobj.get("content"));
		// //语音视频时长
		// object.put("length", dbobj.get("length"));
		// // 发送时间
		// object.put("sendtime", dbobj.get("sendtime"));
		// // 昵称
		// object.put("nickname", dbobj.get("nickname"));
		// // 设备
		// object.put("resource", dbobj.get("resource"));
		// //messageId
		// object.put("messageId", dbobj.get("messageId"));
		//
		// array.add(object);
		// }
		// }
		// }else{
		// logger.info("getIMGroupRecords传递的groupId为空");
		// }
		return array;
	}

	/**
	 * public JSONArray getIMGroupRecords(String groupId,String messageId,int
	 * count,String operatorname) { JSONArray array = null;
	 * if(!StringHelper.checkNull(groupId)){ BasicDBObject queryObject = new
	 * BasicDBObject(); queryObject.put("groupId", groupId); BasicDBObject
	 * sortObject = new BasicDBObject(); sortObject.put("sendtime", -1);
	 * List<DBObject> dbList = null; String time = "";
	 * if(StringHelper.checkNull(messageId)){
	 * 
	 * logger.info("getIMGroupRecords传入time为空，opratorname=[{}],获取最后时间[{}]", new
	 * String[]{operatorname,time}); }else{
	 * 
	 * } if(!StringHelper.checkNull(time)){ queryObject.put("sendtime", new
	 * BasicDBObject("$lt",time)); dbList = daoImpl.find(MongoDbUtils.MONGO_DB,
	 * MongoDbUtils.MONGO_IM_GROUP_COLLECTION, queryObject, sortObject); }else{
	 * dbList = daoImpl.find(MongoDbUtils.MONGO_DB,
	 * MongoDbUtils.MONGO_IM_GROUP_COLLECTION, queryObject, sortObject,0,count);
	 * } JSONObject object = null; array = new JSONArray(); if (null != dbList
	 * && dbList.size() > 0) { for(DBObject dbobj : dbList){ object = new
	 * JSONObject(); object.put("pk", dbobj.get("pk")); // groupId
	 * object.put("groupId", dbobj.get("groupId")); // 发送者的username
	 * object.put("from", dbobj.get("from")); // 发送内容 object.put("content",
	 * dbobj.get("content")); //语音视频时长 object.put("length",
	 * dbobj.get("length")); // 发送时间 object.put("sendtime",
	 * dbobj.get("sendtime")); // 昵称 object.put("nickname",
	 * dbobj.get("nickname")); // 设备 object.put("resource",
	 * dbobj.get("resource")); //messageId object.put("messageId",
	 * dbobj.get("messageId"));
	 * 
	 * array.add(object); } } }else{
	 * logger.info("getIMGroupRecords传递的groupId为空"); } return array; }
	 */
	/**
	 * o.put("pk", pk); o.put("from", from); o.put("groupId", toGroupId);
	 * o.put("content", content); o.put("length", length); o.put("sendtime",
	 * String.valueOf(System.currentTimeMillis())); o.put("time",
	 * String.valueOf(System.currentTimeMillis())); o.put("nickname", nickname);
	 * o.put("resource", resource);
	 */

	/**
	 * 
	 * @Title: getOpBehaviorTime @Description: 获取用户行为时间 @param @param
	 *         operatorname @param @param groupId @param @return 设定文件 @return
	 *         String 返回类型 @throws
	 */
	private String getOpBehaviorTime(String operatorname, String groupId, String type) {
		// TODO Auto-generated method stub
		String time = null;
		// if(!StringHelper.checkNull(groupId)
		// && !StringHelper.checkNull(operatorname)){
		// BasicDBObject queryObject = new BasicDBObject();
		// queryObject.put("groupId", groupId);
		// queryObject.put("operatorName", operatorname);
		// queryObject.put("type", type);
		// BasicDBObject sortObject = new BasicDBObject();
		// sortObject.put("time", -1);
		// DBObject obj = null;
		// List<DBObject> dbList = daoImpl.find(MongoDbUtils.MONGO_DB,
		// MongoDbUtils.MONGO_OP_BEHAVIOR_COLLECTION, queryObject,
		// sortObject);
		// if (null != dbList && dbList.size() > 0) {
		// obj = dbList.get(0);
		// time = (String)obj.get("time");
		// }
		// }else{
		// logger.info("getOpBehaviorTime传递的groupId或者operatorname为空");
		// }
		return time;
	}

	/**
	 * 
	 * @Title: getMessageTimeByMID @Description:
	 *         TODO(这里用一句话描述这个方法的作用) @param @param groupId @param @param
	 *         messageId @param @return 设定文件 @return String 返回类型 @throws
	 */
	private String getMessageTimeByMID(String groupId, String messageId) {
		// TODO Auto-generated method stub
		String time = null;
		// if(!StringHelper.checkNull(groupId)
		// && !StringHelper.checkNull(messageId)){
		// BasicDBObject queryObject = new BasicDBObject();
		// queryObject.put("groupId", groupId);
		// queryObject.put("messageId", messageId);
		// BasicDBObject sortObject = new BasicDBObject();
		// sortObject.put("time", -1);
		// DBObject obj = null;
		// List<DBObject> dbList = daoImpl.find(MongoDbUtils.MONGO_DB,
		// MongoDbUtils.MONGO_IM_GROUP_COLLECTION, queryObject,
		// sortObject);
		// if (null != dbList && dbList.size() > 0) {
		// obj = dbList.get(0);
		// time = (String)obj.get("time");
		// }
		// }else{
		// logger.info("getMessageTimeByMID传递的groupId或者messageId为空");
		// }
		return time;
	}

	/**
	 * 
	 * @Title: @Description: 查询未读消息 @param @param groupId @param @param
	 *         operatorName @param @return 设定文件 @return JSONObject 返回类型 @throws
	 */
	public JSONObject getIMGroupUnReadRecords(String operatorName, String groupId) {
		JSONObject object = null;
		// if(!StringHelper.checkNull(groupId)
		// && !StringHelper.checkNull(operatorName)){
		// BasicDBObject queryObject = new BasicDBObject();
		// queryObject.put("groupId", groupId);
		//// queryObject.put("from", operatorName);
		// String time = getOpBehaviorTime(operatorName,groupId,"leave");
		// if(!StringHelper.checkNull(time)){
		// queryObject.put("sendtime", new BasicDBObject("$gt",time));
		// }
		// BasicDBObject sortObject = new BasicDBObject();
		// sortObject.put("sendtime", -1);
		// DBObject lastObject = null;
		// JSONArray array = new JSONArray();
		// List<DBObject> dbList = daoImpl.find(MongoDbUtils.MONGO_DB,
		// MongoDbUtils.MONGO_IM_GROUP_COLLECTION, queryObject,
		// sortObject);
		// if(null != dbList && dbList.size() > 0){
		// lastObject = dbList.get(0);
		// object = new JSONObject();
		// // 未读的条数
		// object.put("unreadCount", dbList.size());
		// // 最后一条未读的时间
		// object.put("time", lastObject.get("time"));
		// // 最后一条未读的内容
		// object.put("content", lastObject.get("content"));
		// // 最后一条未读的messageId
		// object.put("messageId", lastObject.get("messageId"));
		// // 最后一条未读的昵称
		// object.put("nickname", lastObject.get("nickname"));
		// // 发送者的username
		// object.put("from", lastObject.get("from"));
		// // 最后一条未读的设备
		// object.put("resource", lastObject.get("resource"));
		// // 最后一条未读的IM对话pk
		// object.put("pk", lastObject.get("pk"));
		// }
		// }
		return object;
	}
}
