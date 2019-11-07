package com.nineclient.mongodb.dao.impl;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;
import com.nineclient.mongodb.dao.MongoDao;
import com.nineclient.mongodb.utils.MongoDbUtils;

public class MongodbDaoImpl implements MongoDao {
	/**
	 * MongoClient的实例代表数据库连接池，是线程安全的，可以被多线程共享，客户端在多线程条件下仅维持一个实例即可
	 * Mongo是非线程安全的，目前mongodb API中已经建议用MongoClient替代Mongo
	 */
	private MongoClient mongoClient = null;

	public MongodbDaoImpl() {
		List<ServerAddress> saList = new ArrayList<ServerAddress>();
		if (mongoClient == null) {
			MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
			builder.connectionsPerHost(50);// 与目标数据库能够建立的最大connection数量为50
			builder.autoConnectRetry(true);// 自动重连数据库启动
			// 如果当前所有的connection都在使用中，则每个connection上可以有50个线程排队等待
			builder.threadsAllowedToBlockForConnectionMultiplier(50);
			// 如果当前所有的connection都在使用中，则每个connection上可以有50个线程排队等待
			builder.maxWaitTime(1000 * 60 * 2);
			// 与数据库建立连接的timeout设置为1分钟
			builder.connectTimeout(1000 * 60 * 1);
			MongoClientOptions clientOptions = builder.build();
			try {
				// 数据库连接实例
				ServerAddress address1 = new ServerAddress(MongoDbUtils.MONGO_IP, MongoDbUtils.MONGO_PORT);
				saList.add(address1);
				mongoClient = new MongoClient(saList, clientOptions);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

	// 单例模式
	private static final MongodbDaoImpl instance = new MongodbDaoImpl();

	public static MongodbDaoImpl getInstance() {
		return instance;
	}

	@Override
	public DB getDb(String dbName) {
		return mongoClient.getDB(dbName);
	}

	/**
	 * 判断collection不存在
	 * 
	 * @param dbName
	 * @param collectionName
	 * @return
	 */
	public boolean createCollection(String dbName, String collectionName) {
		boolean b = false;
		DB db = null;
		try {
			// 获取数据库实例
			db = mongoClient.getDB(dbName);
			if (!db.collectionExists(collectionName)) {
				DBObject options = new BasicDBObject();
				options.put("size", 20);
				options.put("capped", 20);
				options.put("max", 20);
				db.createCollection(collectionName, options);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	@Override
	public DBCollection getCollection(String dbName, String collectionName) {
		return mongoClient.getDB(dbName).getCollection(collectionName);
	}

	@Override
	public boolean insert(String dbName, String collectionName, String[] keys, Object[] values) {
		DB db = null;
		DBCollection dbCollection = null;
		WriteResult result = null;
		String resultStr = null;
		try {
			if (keys == null || values == null) {
				return false;
			}
			if (keys.length != values.length) {
				return false;
			}
			// 获取数据库实例
			db = mongoClient.getDB(dbName);
			// 获取数据库中指定的集合
			dbCollection = db.getCollection(collectionName);
			BasicDBObject insertObject = new BasicDBObject();
			for (int i = 0; i < keys.length; i++) {
				insertObject.put(keys[i], values[i]);
			}
			// 插入到mongodb对应的collection中
			result = dbCollection.insert(insertObject);
			resultStr = result.getError();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (db != null) {
				db.requestDone();// 请求结束后关闭db
				db = null;
			}
		}
		return (resultStr != null) ? false : true;
	}

	@Override
	public boolean insert(String dbName, String collectionName, DBObject insertObj) {
		DB db = null;
		DBCollection dbCollection = null;
		WriteResult result = null;
		String resultStr = null;
		try {
			if (insertObj == null) {
				return false;
			}
			// 获取数据库实例
			db = mongoClient.getDB(dbName);
			// 获取数据库中指定的结合
			dbCollection = db.getCollection(collectionName);
			// 创建BasicDBObject对象
			// BasicDBObject insertObject = new BasicDBObject();
			// Set<String> set = map.keySet();
			// Iterator<String> it = set.iterator();
			// while(it.hasNext()){
			// String key = it.next();
			// Object value = map.get(key);
			// insertObject.put(key, value);
			// }

			// 插入到mongodb对应的collection中
			result = dbCollection.insert(insertObj);
			resultStr = result.getError();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (db != null) {
				db.requestDone();// 请求结束后关闭db
				db = null;
			}
		}
		return (resultStr != null) ? false : true;
	}

	@Override
	public boolean delete(String dbName, String collectionName, String[] keys, Object[] values) {
		DB db = null;
		DBCollection dbCollection = null;
		WriteResult result = null;
		String resultString = null;
		try {
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			if (keys == null || values == null) {
				return false;
			}
			if (keys.length != values.length) {
				return false;
			}
			BasicDBObject dbObject = new BasicDBObject();
			for (int i = 0; i < keys.length; i++) {
				dbObject.put(keys[i], values[i]);
			}
			// 删除
			result = dbCollection.remove(dbObject);
			resultString = result.getError();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// 关闭db连接
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
		return (resultString != null) ? false : true;
	}

	@Override
	public boolean delete(String dbName, String collectionName, Map<String, Object> map) {
		DB db = null;
		DBCollection dbCollection = null;
		WriteResult result = null;
		String resultString = null;
		try {
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			if (map.isEmpty()) {
				return false;
			}
			BasicDBObject dbObject = new BasicDBObject();

			Set<String> set = map.keySet();
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String key = it.next();
				Object value = map.get(key);
				dbObject.put(key, value);
			}
			// 删除
			result = dbCollection.remove(dbObject);
			resultString = result.getError();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// 关闭db连接
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
		return (resultString != null) ? false : true;
	}

	@Override
	public List<DBObject> find(String dbName, String collectionName, String[] keys, Object[] values, int num) {
		DB db = null;
		DBCollection dbCollection = null;
		DBCursor cursor = null;
		List<DBObject> resultList = new ArrayList<DBObject>();
		try {
			if (keys == null || values == null) {
				return resultList;
			}
			if (keys.length != values.length) {
				return resultList;
			}
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			// 载入查询条件
			BasicDBObject queryObject = new BasicDBObject();
			for (int i = 0; i < keys.length; i++) {
				queryObject.put(keys[i], values[i]);
			}
			// 返回DBCursor对象
			cursor = dbCollection.find(queryObject);
			int count = 0;
			// 判断是否返回所有的数据，num=-1：返回查询全部数据，num!= -1:返回指定的num数据
			if (num != -1) {
				while (count < num && cursor.hasNext()) {
					resultList.add(cursor.next());
					count++;
				}
			} else {
				while (cursor.hasNext()) {
					resultList.add(cursor.next());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// 关闭db连接&cursor
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
		return resultList;
	}

	@Override
	public List<DBObject> find(String dbName, String collectionName, Map<String, Object> map, int num) {
		DB db = null;
		DBCollection dbCollection = null;
		DBCursor cursor = null;
		List<DBObject> resultList = new ArrayList<DBObject>();
		try {
			if (map.isEmpty()) {
				return resultList;
			}
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			// 载入查询条件
			BasicDBObject queryObject = new BasicDBObject();

			Set<String> set = map.keySet();
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String key = it.next();
				Object value = map.get(key);
				queryObject.put(key, value);
			}
			// 返回DBCursor对象
			cursor = dbCollection.find(queryObject);
			int count = 0;
			// 判断是否返回所有的数据，num=-1：返回查询全部数据，num!= -1:返回指定的num数据
			if (num != -1) {
				while (count < num && cursor.hasNext()) {
					resultList.add(cursor.next());
					count++;
				}
			} else {
				while (cursor.hasNext()) {
					resultList.add(cursor.next());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// 关闭db连接&cursor
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
		return resultList;
	}

	@Override
	public long getCollectionCount(String dbName, String collectionName) {
		DB db = null;
		DBCollection dbCollection = null;
		try {
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			return dbCollection.getCount();
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	@Override
	public long getCount(String dbName, String collectionName, DBObject obj) {
		DB db = null;
		DBCollection dbCollection = null;
		try {
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			if (obj != null) {
				return dbCollection.getCount(obj);
			} else {
				return dbCollection.getCount();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	@Override
	public List<DBObject> find(String dbName, String collectionName, DBObject query) {
		DB db = null;
		DBCollection dbCollection = null;
		DBCursor dbCursor = null;
		try {
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			dbCursor = dbCollection.find(query);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return dbCursor2List(dbCursor);
	}

	@SuppressWarnings("unchecked")
	public List<DBObject> findDistinct(String dbName, String collectionName, DBObject query, String distictKey) {
		List<DBObject> list = new ArrayList<DBObject>();
		DB db = null;
		DBCollection dbCollection = null;
		try {
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			list.addAll(dbCollection.distinct(distictKey, query));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return list;
	}

	/**
	 * 查询DBCursor转换为List
	 */
	public List<DBObject> dbCursor2List(DBCursor dbCursor) {
		List<DBObject> list = new ArrayList<DBObject>();
		if (dbCursor != null) {
			list = dbCursor.toArray();
		}
		return list;

	}

	@Override
	public List<DBObject> find(String dbName, String collectionName, DBObject query, DBObject sort) {
		DB db = null;
		DBCollection dbCollection = null;
		DBCursor dbCursor = null;
		try {
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			if (query != null) {
				dbCursor = dbCollection.find(query);
			} else {
				dbCursor = dbCollection.find();
			}
			if (sort != null) {
				dbCursor.sort(sort);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return dbCursor2List(dbCursor);
	}

	@Override
	public List<DBObject> find(String dbName, String collectionName, DBObject query, DBObject sort, int start,
			int limit) {
		DB db = null;
		DBCollection dbCollection = null;
		DBCursor dbCursor = null;
		try {
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			if (query != null) {
				dbCursor = dbCollection.find(query);
			} else {
				dbCursor = dbCollection.find();
			}
			if (sort != null) {
				dbCursor.sort(sort);
			}
			if (start < 0) {
				dbCursor.batchSize(start);
			} else {
				dbCursor.skip(start).limit(limit);
			}
			return dbCursor2List(dbCursor);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean update(String dbName, String collectionName, DBObject whereFields, DBObject setFields,
			boolean isUpdate) {
		DB db = null;
		DBCollection dbCollection = null;
		WriteResult result = null;
		String resultString = null;
		try {
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			if (setFields.equals(whereFields)) {
				return true;
			}
			/**
			 * 第一个true的参数：如果数据库不存在，是否添加
			 */
			result = dbCollection.update(whereFields, setFields, isUpdate, true);
			// dbCollection.updateMulti(setFields, whereFields);
			resultString = result.getError();

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// close db
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
		return (resultString != null) ? false : true;
	}

	@Override
	public boolean exists(String dbName, String collectionName, String key, Object value) {
		DB db = null;
		DBCollection dbCollection = null;
		try {
			if (key == null || value == null) {
				return false;
			}
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			BasicDBObject countDbObject = new BasicDBObject();
			countDbObject.put(key, value);

			long result = dbCollection.count(countDbObject);
			if (result > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
		return false;
	}

	@Override
	public DBObject findOne(String dbName, String collectionName, DBObject query) {
		DB db = null;
		DBCollection dbCollection = null;
		DBObject dbObject = null;
		try {
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			dbObject = dbCollection.findOne(query);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return dbObject;
	}

	@Override
	public List<DBObject> find(String dbName, String collectionName, DBObject query, DBObject sort, int limit) {
		DB db = null;
		DBCollection dbCollection = null;
		DBCursor dbCursor = null;
		try {
			db = mongoClient.getDB(dbName);
			dbCollection = db.getCollection(collectionName);
			if (query != null) {
				dbCursor = dbCollection.find(query);
			} else {
				dbCursor = dbCollection.find();
			}
			if (sort != null) {
				dbCursor.sort(sort);
			}
			dbCursor.limit(limit);
			return dbCursor2List(dbCursor);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
/*
112.124.26.89
端口 5017
josh  11:53:49
TEST_COMMENT_MATCH_KEYWORD_5
COMMENT_CONTENT 评论内容
DISPLAY_NAME 意见分类名称
*/