package com.util;

import java.io.File;

import org.apache.commons.lang.SystemUtils;
import org.ini4j.ConfigParser;

public class PropertyConfig {
	public static ConfigParser config;
	static {
		try {
			config = new ConfigParser();
			String config_path;
			if (SystemUtils.IS_OS_LINUX) {
				config_path = new File(PropertyConfig.class.getResource("").getFile()).getParentFile().getParentFile()
						.getParentFile().getParent() + "config.ini";
			} else {				
				config_path = "D:/360/solution/QASystem/config.ini";
			}
			System.out.println("config_path = " + config_path);
			config.read(config_path);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args) {
		System.out.println(config);
	}

}
