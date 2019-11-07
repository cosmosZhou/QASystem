package com.robot.DateBase;

import java.util.List;
import java.util.Map;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * mongodb接口
 * 
 * @author Justin 2016年1月7日
 */
public interface MongoDao {

	/**
	 * 获取指定的mongodb数据库
	 * 
	 * @param dbName
	 * @return
	 */
	public DB getDb(String dbName);

	/**
	 * 获取指定Mongodb数据库的collection集合
	 * 
	 * @param dbName
	 * @param collectionName
	 * @return
	 */
	public DBCollection getCollection(String dbName, String collectionName);

	/**
	 * 向指定mongodb数据库的集合中keys和对应的values
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param keys
	 * @param values
	 * @return
	 */
	public boolean insert(String dbName, String collectionName, String[] keys, Object[] values);

	/**
	 * 向指定mongodb数据库的集合中keys和对应的values
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param map
	 * @return
	 */
	public boolean insert(String dbName, String collectionName, DBObject insertObj);

	/**
	 * 删除指定数据库中的keys和对应的values
	 * 
	 * @param dbName
	 * @param connectionName
	 * @param keys
	 * @param values
	 * @return
	 */
	public boolean delete(String dbName, String collectionName, String[] keys, Object[] values);

	/**
	 * 删除指定数据库中的keys和对应的values
	 * 
	 * @param dbName
	 * @param connectionName
	 * @param map
	 * @return
	 */
	public boolean delete(String dbName, String collectionName, Map<String, Object> map);

	/**
	 * 从mongodb中查询指定的keys和对应的values
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param keys
	 * @param values
	 * @param num
	 * @return
	 */
	public List<DBObject> find(String dbName, String collectionName, String[] keys, Object[] values, int num);

	/**
	 * 从mongodb中查询指定的keys和对应的values
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param map
	 * @param num
	 */
	public List<DBObject> find(String dbName, String collectionName, Map<String, Object> map, int num);

	public long getCollectionCount(String dbName, String collectionName);

	public long getCount(String dbName, String collectionName, DBObject obj);

	/**
	 * 查询
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param query
	 * @return
	 */
	public List<DBObject> find(String dbName, String collectionName, DBObject query);

	/**
	 * distinct
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param query
	 * @param distinctKey
	 * @return
	 */
	public List<DBObject> findDistinct(String dbName, String collectionName, DBObject query, String distinctKey);

	/**
	 * 排序查询
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param query
	 * @param sort
	 * @return
	 */
	public List<DBObject> find(String dbName, String collectionName, DBObject query, DBObject sort);

	/**
	 * 分页查询
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param query
	 * @param sort
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<DBObject> find(String dbName, String collectionName, DBObject query, DBObject sort, int start,
			int limit);

	/**
	 * 更新mongodb数据库，使用指定的newValue来更换oldVlaue
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param oldValue
	 * @param newValue
	 * @param isUpdate
	 */
	public boolean update(String dbName, String collectionName, DBObject whereFields, DBObject setFields,
			boolean isUpdate);

	/**
	 * 查询key和value在mongodb的collection中是否存在
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param key
	 * @param value
	 */
	public boolean exists(String dbName, String collectionName, String key, Object value);

	/**
	 * 查询一条记录
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param query
	 * @return
	 */
	public DBObject findOne(String dbName, String collectionName, DBObject query);

	/**
	 * 查到指定数据limit
	 * 
	 * @param dbName
	 * @param collectionName
	 * @param query
	 * @param sort
	 * @param limit
	 * @return
	 */
	public List<DBObject> find(String dbName, String collectionName, DBObject query, DBObject sort, int limit);

}
