package com.zy.pos.io;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import android.content.Context;


public class ThreadPool{


	  private static ExecutorService single = null;
	  private static ThreadPool instance=null;
	  private ThreadPool(){
		  
	  }
	  /**
	   * 获取单线程池单例
	   * @return
	   */
	  public synchronized static ExecutorService getSinglePoolInstance() {
	    if (single == null) {
	      single =Executors.newSingleThreadExecutor();
	    }
	    return single;
	  }
	  public synchronized static ThreadPool getInstance() {
		
		  if(instance==null){
			  instance=new ThreadPool();
		  }
		  return instance;
	  }

	  /**
		 * 添加新线程到线程池
		 * @param context
		 */
	public static  void addThread(Runnable t){
		ThreadPool.getSinglePoolInstance().submit(t);
	}

	public static  void addWriteFileThread(Context context,String data,String fileName){
		Thread thread=new WriteFileThread(context, data, fileName);
		ThreadPool.getSinglePoolInstance().submit(thread);
	}
	/**
	 * 关闭线程池
	 */
	public static void closePool(){
		ThreadPool.getSinglePoolInstance().shutdown();
	}
}
