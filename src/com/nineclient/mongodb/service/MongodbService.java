package com.nineclient.mongodb.service;

import java.util.List;

//import com.nineclient.talk.service.dto.EcChatRecordsDTO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @author justin
 *
 */

public interface MongodbService {
	/**
	 * 保存对话记录到monggodb
	 * 
	 * @param model
	 */
//	public void saveEcChatRecords(EcChatRecordsDTO model);

	/**
	 * 查询operatorPk
	 * 
	 * @param companyPk
	 * @param visitorId
	 * @param distinctKey
	 * @return
	 */
	List<String> queryHistoryOperator(String companyPk, String visitorId, String distinctKey);

	/**
	 * 
	 * @Title: saveEcChatContent 
	 * @Description: 保存对话内容到mongodb 
	 * @param chatId 
	 * @param content 
	 * @return boolean 返回类型
	 */
	public boolean saveEcChatContent(String chatId, String content);

	/**
	 * 
	 * @Title: getMsgContent 
	 * @Description: 从mongodb中根据对话记录pk和companyPk查询对话内容 
	 * @param chatId
	 * @param companyPk 
	 * @return String
	 */
	public String getMsgContent(String chatId, String companyPk);

	/**
	 * 查询im的对话记录
	 * 
	 * @param pk
	 * @param messageId
	 * @param count
	 * @return
	 */
	public JSONArray getImRecords(String pk, String messageId, int count);

	/**
	 * 查询未读的IM对话
	 * 
	 * @param username
	 * @param pkList
	 * @return
	 */
	public JSONArray getUnreadRecord(String username, List<String> pkList);

	/**
	 * 设置消息已读
	 * 
	 * @param imPk
	 * @param username
	 * @return
	 */
	public void setRecordRead(String imPk, String username);
	
	/**
	 * 
	* @Title: isChatIdExist 
	* @Description: chatId是否存在 
	* @param @param chatId
	* @param @return    设定文件 
	* @return boolean    返回类型 
	 */
	public boolean isChatIdExist(String chatId);
	
	/**
	 * 
	* @Title: saveOperatorBehaviorTime 
	* @Description: IM群聊保存坐席离开群时间 
	* @param @param operatorName
	* @param @param groupId
	* @param @param leaveTime    设定文件 
	* @return void    返回类型 
	 */
	public void saveOperatorBehaviorTime(String operatorName,String groupId,String type,String leaveTime);
	
	/**
	 * 
	* @Title: getIMGroupRecords
	* @Description:获取群聊消息
	* @param groupId
	* @param messageId
	* @param count
	* @param operatorname
	* @return JSONArray
	 */
	public JSONArray getIMGroupRecords(String groupId,String messageId,int count,String operatorname);
	
	
	/**
	 * 
	* @Title: getIMGroupUnReadRecords
	* @Description:获取未读消息
	* @param operatorname
	* @param groupId
	* @return JSONObject
	 */
	public JSONObject getIMGroupUnReadRecords(String operatorname,String groupId);
}
