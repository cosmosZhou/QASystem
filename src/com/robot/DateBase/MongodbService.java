package com.robot.DateBase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author justin
 *
 */

public interface MongodbService {

	/**
	 * 
	 * @param startDate
	 * @return
	 */
	public HashMap<String, ArrayList<String>> getMsgContent(String startDate,
			String[] previousDate);

}
