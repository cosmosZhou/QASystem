package com.nineclient.rest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

import com.robot.QASystem;
import com.util.Utility;

public class QASystemOnLoad implements ServletContextListener {

	public static void main(String[] args) {
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			QAThread thread = new QAThread();
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class QAThread extends Thread {

	@Override
	public void run() {
		try {
			log.info("QASystem initializing:");
			if (SystemUtils.IS_OS_WINDOWS) {
				System.out.println("SystemUtils.IS_OS_WINDOWS");
//				Utility.workingDirectory = "D:\\360CloudEnterprise\\Cache\\35934193\\14801337072146233\\solution";
				Utility.workingDirectory = "D:\\360\\solution";
			} else {
				System.out.println("SystemUtils.IS_OS_LINUX");
				//				Utility.workingDirectory = "/opt/nfsdata/";
				Utility.workingDirectory = "/opt/ucc/intelligence/";
			}

			log.info("Utility.workingDirectory = " + Utility.workingDirectory);
			log.info("QASystem initialized successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			log.info("QASystem initialization failed.");
		}

	}

	public static Logger log = Logger.getLogger(QAThread.class);
}
