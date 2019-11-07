package com.robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongodbServiceImpl implements MongodbService {

	public static MongodbDaoImpl daoImpl = MongodbDaoImpl.getInstance();

	private transient static Logger logger = LoggerFactory
			.getLogger(MongodbServiceImpl.class);

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
	public HashMap<String, ArrayList<String>> getMsgContent(String startDate,
			String[] previousDate) {
		String msgContent = null;
		String companyPk = null;
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		if (startDate != null && !("").equals(startDate)) {
			BasicDBObject queryObject = new BasicDBObject();
			queryObject.put("msg_content", new BasicDBObject("$ne", null));
			queryObject.put("msg_content", new BasicDBObject("$ne", ""));
			queryObject.put("chat_end_time",
					new BasicDBObject("$gt", startDate));
			BasicDBObject sortObject = new BasicDBObject();
			sortObject.put("chat_end_time", -1);
			List<DBObject> dbList = daoImpl.find(MongoDbUtils.MONGO_DB,
					MongoDbUtils.MONGO_COLLECTION, queryObject, sortObject);

			if (null != dbList && dbList.size() > 0) {
				previousDate[0] = (String) dbList.get(0).get("chat_end_time");

				for (int i = 0; i < dbList.size(); i++) {
					msgContent = (String) dbList.get(i).get("msg_content");
					companyPk = (String) dbList.get(i).get("company_pk");
					if (!map.containsKey(companyPk)) {
						map.put(companyPk, new ArrayList<String>());
					}
					map.get(companyPk).add(msgContent);
				}
			}
		}
		return map;
	}
}
