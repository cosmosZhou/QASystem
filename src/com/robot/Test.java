package com.robot;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//http://www.cnblogs.com/exe19/p/5359885.html
public class Test {
	public static void main(String[] args) {
		//		ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(5));
		ExecutorService executor = Executors.newCachedThreadPool(); //创建一个缓冲池，缓冲池容量大小为Integer.MAX_VALUE

		for (int i = 0; i < 20; i++) {
			MyTask myTask = new MyTask(i);
			System.out.println("put the task " + i + " into the thread pool.");
			executor.execute(myTask);
//			System.out.println("number of threads in th thread pool = " + executor.getPoolSize());
//			System.out.println("threads waiting for execution in the array = " + executor.getQueue().size());
//			System.out.println("threads that have executed = " + executor.getCompletedTaskCount());
		}
		executor.shutdown();
	}
	
	static synchronized void synchronizedTask() throws InterruptedException{
		Thread.currentThread().sleep(4000);
	}
}

class MyTask implements Runnable {
	private int taskNum;

	public MyTask(int num) {
		this.taskNum = num;
	}

	@Override
	public void run() {
		System.out.println("executing task " + taskNum);
		try {
			Test.synchronizedTask();
//			Thread.currentThread().sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("task " + taskNum + " finished");
	}
}