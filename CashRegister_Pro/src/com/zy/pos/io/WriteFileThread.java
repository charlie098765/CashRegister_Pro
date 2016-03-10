package com.zy.pos.io;


import com.zy.pos.util.CacheFileUtils;

import android.content.Context;
/**
 * 写文件线程
 * @author Administrator
 *
 */
public class WriteFileThread  extends Thread {

	  private  Context mContext;
	  private String str;
	  private String fileName;
	  public WriteFileThread(Context context,String str,String fileName) {
		  mContext=context;
		  this.str=str;
		  this.fileName=fileName;
		  
	  }

	  @Override
	  public void run() {
		  CacheFileUtils.writeSendFile(str, fileName, mContext);
		  }

}
