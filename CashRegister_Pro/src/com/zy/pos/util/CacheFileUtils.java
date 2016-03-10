package com.zy.pos.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;



import android.content.Context;
import android.util.Log;

/**
 * <li>文件名称: CacheFileUtils.java</li>
 * <li>文件描述: 操作缓存本地文件的自定义类</li>
 */
public  class CacheFileUtils {
	private static String TAG="CacheFileUtils";
	public static String fileStrPath="";
	public static final int MODE_WORLD_WRITEABLE=0x0002;
		/**
		 * 缓存文件写入商品的IMEI列表到本地存储
		 * 
		 */
		public static String  writeSendFile(String str,String fileName,Context mContext){
			try {
				if(str==null){
					str="";
				}
				createFile(mContext);
//				Log.i(TAG,"dataMap_write_localPath:"+dataMapPath);
				FileOutputStream fos=new FileOutputStream(fileStrPath+fileName);
				ObjectOutputStream oos=new ObjectOutputStream(fos);
				oos.writeObject(str);
				oos.flush();
				oos.close();
				fos.flush();
				fos.close();
			} catch (Exception e) {
				Log.i(TAG,"Exception thrown during serializeToLocalPath"+e.getMessage());
//				e.printStackTrace();
			} 
			return str;
		}
		/**
		 * 设置本地缓存文件名
		 * @param mContext
		 */
		public static void createFile(Context mContext) {
			fileStrPath=mContext.getFilesDir().getAbsolutePath()+"/cacheFiles/";
			File destDir=new File(fileStrPath);
			if(!destDir.exists()){
				destDir.mkdirs();
			}else{
				return;
			}
			
		}

		
		/**删除本地缓存数据*/
		public static void deleteFile(String fileName,Context mContext){
			createFile(mContext);
					File file=new File(fileStrPath);
					if(file.isDirectory()){
						File[] fileList=file.listFiles();
						//遍历本地存储里面的每个文件
						for (int i= 0; i < fileList.length; i++) {
								if(fileList[i].getName().equals(fileName)){
									fileList[i].delete();
									Log.i(TAG,"del......"+fileList[i].getName());
								}
						}
						
				
				}
			
			}
		/**
		 * 查询本地缓存数据
		 * @param mContext
		 */
		public static String queryFile(Context mContext){
			createFile(mContext);
			File file=new File(fileStrPath);
			StringBuilder sb=null;
			if(file.isDirectory()){
				String[] fileList=file.list();
					 sb=new StringBuilder();
					for (String string : fileList) {
						sb.append(new File(string).getName());
					}
					Log.i(TAG,"return:"+fileStrPath+sb.toString());
			}
			return sb.toString();
		}
		
		/**
		 * 读取写入的文件内容
		 * @param mContext
		 * @return
		 */
	   public static String readFile(Context mContext){
			CacheFileUtils.createFile(mContext);
			String data="";
			String dataPath=CacheFileUtils.fileStrPath;
			File file=new File(dataPath);
				if(file.isDirectory()){
					File[] fileList=file.listFiles();
					if(fileList.length==0){
						Log.i(TAG,"缓存文件不存在.......");
						return "";
					}
					//只有一个文件，不用遍历本地存储里面的每个文件
//					for (int i = 0; i < fileList.length; i++) {
			
						File f=fileList[0];
						
						try {
							FileInputStream fis=new FileInputStream(f.getAbsolutePath());
							
							ObjectInputStream ois=new ObjectInputStream(fis);
							//读取缓存数据
							Object obj =ois.readObject();
							if(obj instanceof String){
								data=(String) obj;
							}
							//读取缓存的数据是哪个请求的
							
							ois.close();
							fis.close();
						} catch (Exception e) {
							e.printStackTrace();
							Log.i(TAG,e.getMessage());
						} 
//					}
				
		}else{
			Log.i(TAG,"文件夹路径不存在......");
		}
		return data;
		   
	   }
		
}
