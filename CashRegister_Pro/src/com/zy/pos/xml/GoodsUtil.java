package com.zy.pos.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import com.zy.pos.util.C;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

public class GoodsUtil {
	 private static final String TAG = "GoodsUtil";
	 public static JSONObject getGoodsInfo(Context mContext,String imei,int flag){
		 Log.i(TAG,TAG+" flag--->"+flag);
		 JSONObject goodsObj=new JSONObject();
		 //如果找到游戏传过来的id对应那么返回渠道的id，如果没有找到返回游戏id
			InputStream inputStream =mContext.getResources().openRawResource(getRawId(mContext, "goods_list"));
			try {
				List<GoodsBean> list = readXml(inputStream);
				for (GoodsBean goods : list) {
					if(imei.equals(goods.getImei())){
						goodsObj.put(C.GOODS_NAME,goods.getName() );
						 if((flag==0||flag==3)&&goods.getSpecial().equals(C.TWO_GET_ONE)){
				    		   goodsObj.put(C.GOODS_SPECIAL_OFFERS, goods.getSpecial() );
				    	   }else if(flag==1){
				    		   goodsObj.put(C.GOODS_SPECIAL_OFFERS, "" );
				    	   }else if((flag==2||flag==3)&&goods.getSpecial().equals(C.NINE_FIVE)){
				    		   goodsObj.put(C.GOODS_SPECIAL_OFFERS, goods.getSpecial());
				    	   }else{
				    		   goodsObj.put(C.GOODS_SPECIAL_OFFERS, "" );
				    	   }
						goodsObj.put(C.GOODS_PRICE,goods.getPrice() );
						goodsObj.put(C.GOODS_UNIT,goods.getUnit() );
						return goodsObj;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return goodsObj;
			}
		 return goodsObj;
	 }
	 public static String getGoodsIMEI(Context mContext,String name,int flag){
		 String imei="";
		 //如果找到游戏传过来的id对应那么返回渠道的id，如果没有找到返回游戏id
		 InputStream inputStream =mContext.getResources().openRawResource(getRawId(mContext, "goods_list"));
		 try {
			 List<GoodsBean> list = readXml(inputStream);
			 for (GoodsBean goods : list) {
				 if(name.equals(goods.getName())){
		
					 imei=goods.getImei();
					 return imei;
				 }
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
			 return imei;
		 }
		 return imei;
	 }
	 public static String getGoodsUnit(Context mContext,String name,int flag){
		 String unit="";
		 //如果找到游戏传过来的id对应那么返回渠道的id，如果没有找到返回游戏id
		 InputStream inputStream =mContext.getResources().openRawResource(getRawId(mContext, "goods_list"));
		 try {
			 List<GoodsBean> list = readXml(inputStream);
			 for (GoodsBean goods : list) {
				 if(name.equals(goods.getName())){
					 
					 unit=goods.getUnit();
					 return unit;
				 }
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
			 return unit;
		 }
		 return unit;
	 }
	 
 
	 private static List<GoodsBean> readXml(InputStream inputStream) throws Exception{
			XmlPullParser xpp = Xml.newPullParser(); // 创建一个xml解析器对象
			xpp.setInput(inputStream, "UTF-8");  // 设置输入流以及编码方式
			int eventType = xpp.getEventType(); // 获得事件的类型
			List<GoodsBean> list = new ArrayList<GoodsBean>();
			GoodsBean goods = null;
			 while (eventType != XmlPullParser.END_DOCUMENT) { // 判断事件类型是否为结束文档事件 如果不是就执行循环中的代码
				 if(eventType == XmlPullParser.START_TAG) {
					 if("goods".equals(xpp.getName())){ // 判断标签名字是否为 ProductId
						 goods = new GoodsBean();
						 goods.setName(xpp.getAttributeValue(0));
						 goods.setImei(xpp.getAttributeValue(1));
						 goods.setSpecial(xpp.getAttributeValue(2));
						 goods.setPrice(xpp.getAttributeValue(3));
						 goods.setUnit(xpp.getAttributeValue(4));
						 list.add(goods);
					 }
				 }
				 
				 eventType = xpp.next(); // 获取下一个事件类型
			 }
			 
			 return list;
		}
	 
	 private static int getRawId(Context paramContext, String paramString) {
			return paramContext.getResources().getIdentifier(paramString, "raw",
					paramContext.getPackageName());
		}
	 
 
 

}
