package com.zy.pos.fragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.zy.pos.io.ThreadPool;
import com.zy.pos.outinterface.IPOSListener;
import com.zy.pos.util.C;
import com.zy.pos.util.CacheFileUtils;
import com.zy.pos.util.ResourceUtil;
import com.zy.pos.xml.GoodsUtil;
import com.zy.indicatorfragment.R;

public class BaseFragment extends Fragment implements OnClickListener ,IPOSListener{
	private final String TAG="BaseFragment";
    protected View mMainView;
    protected static ArrayList<Map<String, Object>> mlistItems;//所有商品列表
    protected static ArrayList<Map<String, Object>> mlistItems_bought;//所有已经购买的商品列表
    protected Context mContext;

   

    public BaseFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {

        try {
        	mPOSListener=(IPOSListener) activity;
        } catch (Exception e) {
        	throw new ClassCastException(activity.toString() + "must implement  mPOSListener");
        }
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
        
    }
    private IPOSListener mPOSListener;
    private static int pos_fragment=0;
    /**
     * 获取从activity里面传过来的值（指定当前是那个fragment）
     */
   @Override
   public void transferMsg(int flag_fragment) {
	   pos_fragment=flag_fragment;
	   Log.i(TAG,"pos_fragment---flag--->"+pos_fragment);
   }

   private static int flag=-1;
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	 mlistItems = new ArrayList<Map<String, Object>>();
    	 mMainView = inflater.inflate(R.layout.fragment_one, container, false);
         flag=container.getChildCount();
//        Log.i(TAG,"------->"+container.getChildCount());
         
        setListItemContent(flag);
        listView = (ListView) mMainView.findViewById(R.id.list);
        SimpleAdapter adapter = new SimpleAdapter(mContext, mlistItems,
                R.layout.listview_item, new String[] {
                        C.GOODS_NAME, C.GOODS_SPECIAL_OFFERS,C.GOODS_PRICE,C.GOODS_AMOUNT
                }, new int[] {
                        R.id.tv_goods_name, R.id.tv_goods_special_offers,R.id.tv_goods_price,R.id.tv_goods_amount
                });
        listView.setAdapter(adapter);
        
        
        btn_print_id=ResourceUtil.getId(mContext, "btn_print");
        mMainView.findViewById(btn_print_id).setOnClickListener(this);
        btn_buy_id=ResourceUtil.getId(mContext, "btn_buy");
        mMainView.findViewById(btn_buy_id).setOnClickListener(this);
        return mMainView;
        
        
    }
    
    int btn_print_id;                          //购买按钮id
    int btn_buy_id;                          //购买按钮id
    
	private TextView tv_goods_name;          //商品名称
	private TextView tv_goods_special_offers;//商品对应的优惠活动
	private TextView tv_goods_price;         //商品单价
	private TextView tv_goods_amount;        //商品数量
	
	private ListView listView;
    
	/**
	 * 购买按钮监听
	 * 获取listview数据
	 */
	@Override
	public void onClick(View v) {
		if(v.getId()==btn_buy_id){
			//购买结算，输入IMEI号，此处缓存存入本地文件夹中，模拟发送给服务器
			inputIMEIList();
			
		}else if(v.getId()==btn_print_id){
			//查询本地是否有文件夹
			CacheFileUtils.queryFile(mContext);
			
			//读取本地缓存的商品imei信息，模拟从网络端获取的数据
			String goods_data=CacheFileUtils.readFile(mContext);
//			Toast.makeText(mContext, "获取本地文件的数据：\n"+CacheFileUtils.readFile(mContext), Toast.LENGTH_LONG).show();
			
			//打印商品清单
			printShoppingList(goods_data);
			
		}
	
	}

	private void inputIMEIList() {
		StringBuffer sb_imei=new StringBuffer("[\n");
		for (int i = 0; i < listView.getCount(); i++) {
			tv_goods_name = (TextView)listView.getChildAt(i).findViewById(ResourceUtil.getId(mContext, "tv_goods_name"));
			String goods_name= tv_goods_name.getText().toString();	
			String imei=GoodsUtil.getGoodsIMEI(mContext, goods_name,pos_fragment );
			String unit=GoodsUtil.getGoodsUnit(mContext, goods_name,pos_fragment );
//			Log.i(TAG,"input all imei--->"+imei);
			tv_goods_amount = (TextView)listView.getChildAt(i).findViewById(ResourceUtil.getId(mContext, "tv_goods_amount"));
			int goods_amount= Integer.valueOf(tv_goods_amount.getText().toString().equals("")?"0":tv_goods_amount.getText().toString());	
			
			
			if(unit.equals(C.APPLE_UNIT)){
				//单位为"斤"的添加上数量后缀
				if(goods_amount!=0)
				sb_imei.append("'"+imei+"-"+goods_amount+"',\n");
				
			}else{
				for (int j = 0; j < goods_amount; j++) {
					sb_imei.append("'"+imei+"',\n");
				}
			}
			
		}
		String imei_list=sb_imei.substring(0,sb_imei.length()-2).toString()+"\n]";
		
		Toast.makeText(mContext, imei_list, Toast.LENGTH_SHORT).show();
		//缓存入本地文件中
		ThreadPool.addWriteFileThread(mContext,imei_list , "shopping_imei_list");
	}
	/**
	 * 打印购买的商品清单列表
	 * @param data TODO
	 */
	private void printShoppingList(String data) {
	try {	
		JSONObject goodsObj=getGoodsJsonObject(data);
		
		mlistItems_bought = new ArrayList<Map<String, Object>>();
		float total=0;
		float subtotal=0;
		float all_save=0;
		float save=0;
		
		 Iterator<?> keyIter = goodsObj.keys();
        String key="";
        int countJsonKey=0;
    	while ( keyIter.hasNext()) {
    		keyIter.next();
        	countJsonKey++;
        }
        int flag=0;
        for( Iterator<?> keyt = goodsObj.keys(); keyt.hasNext();)
        {
        	
            key = (String)keyt.next();
		
			
			JSONObject goods = (JSONObject) goodsObj.get(key);
			float goods_price=  Float.parseFloat(goods.get(C.GOODS_PRICE).toString());
			
			String goods_special_offers=goods.get(C.GOODS_SPECIAL_OFFERS).toString();	
			int goods_amount=Integer.parseInt( goods.get(C.GOODS_AMOUNT).toString());	
			String goods_unit=goods.get(C.GOODS_UNIT).toString();
				
			
		
		    
		    if(goods_special_offers.equals("")){
		    	//无活动
		    	subtotal=goods_price*goods_amount;
		    }else if(goods_special_offers.equals(C.NINE_FIVE)){
		    	//95折
		    	subtotal=(float) (goods_price*goods_amount*0.95);
		    }else if(goods_special_offers.equals(C.TWO_GET_ONE)){
		    	//买二赠一
		    	subtotal=(float) Math.floor(goods_amount/3)*2*goods_price+goods_amount%3*goods_price;
		    	
		    	//买二赠一所赠送的商品数量列表
		    	goods.put(C.SPECIAL_AMOUNT, Integer.valueOf((int) Math.floor(goods_amount/3))+goods_unit);
		    	
		    }
		    
		    save= goods_price*goods_amount-subtotal;
		    //每项添加上小计和节省的钱
		    goods.put(C.SUBTOTAL,formatPrice(subtotal)+"");
		    goods.put(C.SAVE, formatPrice(save)+"");
		   
		    total+=subtotal;
		    all_save+=save;
//		    mlistItems_bought.add(map);
		    goodsObj.put(key, goods);
		    if(flag==countJsonKey-1){
		    	
		    	//最后添加上总计和小计部分
//		    	Map<String, Object> map_total = new HashMap<String, Object>();
		    	JSONObject totalObj=new JSONObject();
		    	totalObj.put(C.TOTAL, formatPrice(total)+"");
		    	totalObj.put(C.ALL_SAVE,formatPrice(all_save) +"");
		    	goodsObj.put(C.TOTAL_LIST,totalObj);
		    }
			flag++;
		    setDataToShoppingListView(goodsObj);
		}
	
//		Log.i(TAG,mlistItems_bought.toString()+"");
//			Toast.makeText(mContext, mlistItems_bought.toString(), Toast.LENGTH_LONG).show();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private JSONObject getGoodsJsonObject(String data) {
		JSONObject goodsObj=new JSONObject();
		data=data.substring(3, data.length()-3);
		String[] str=data.split("',\n'");		
		try {
			JSONObject goods;
			String imei="";
			int flag=0;
			for (int i = 0; i < str.length; i++) {
				if(!str[i].contains("-")){
					
					if(imei.equals(str[i])){
						imei=str[i];
						flag+=1;
					}else if(!imei.equals("")){
						goods=GoodsUtil.getGoodsInfo(mContext, str[i-1],pos_fragment );
						goods.put(C.GOODS_AMOUNT, flag);
						goodsObj.put(str[i-1], goods);
						imei=str[i];
						flag=1;
					}else{
						if(i!=str.length-1){
							imei=str[i];
							flag=1;
						}else{
							goods=GoodsUtil.getGoodsInfo(mContext, str[i],pos_fragment );
							goods.put(C.GOODS_AMOUNT, 1);
							goodsObj.put(str[i], goods);
						}
					}
				}else{
					if(i!=0){
						//先记录之前有的数据
						goods=GoodsUtil.getGoodsInfo(mContext, str[i-1],pos_fragment );
						goods.put(C.GOODS_AMOUNT, flag);
						goodsObj.put(str[i-1], goods);
						}
					
					//称重的商品序列号处理
					String new_imei=str[i].split("-")[0];
					String amount=str[i].split("-")[1];
					goods=GoodsUtil.getGoodsInfo(mContext, new_imei,pos_fragment );
					goods.put(C.GOODS_AMOUNT, amount);
					goodsObj.put(new_imei, goods);
					flag=0;
					
				}
			}
			Log.i(TAG,"json--->"+goodsObj.toString());
			Toast.makeText(mContext, goodsObj.toString(), Toast.LENGTH_SHORT).show();
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return goodsObj;
	}
	/**
	 * 获取商品购物清单
	 */
	private void setDataToShoppingListView(JSONObject goodsObj) {
			
		mMainView.findViewById(ResourceUtil.getId(mContext, "ll_shopping_list")).setVisibility(View.VISIBLE);
		TextView tv_all_buy_goods=(TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "tv_all_buy_goods"));
		TextView tv_specal_amount_list=(TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "tv_specal_amount_list"));
		TextView tv_total_and_save=(TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "tv_total_and_save"));
		
		TextView tv_divided_line1=(TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "tv_divided_line1"));
		TextView tv_specal_amount_list_title=(TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "tv_specal_amount_list_title"));
		
		LinearLayout ll_special=(LinearLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "ll_special_offer_list"));
		
		StringBuffer sb_all_buy_goods=new StringBuffer();
		StringBuffer sb_specal_amount_list=new StringBuffer("");
		StringBuffer sb_total_and_save=new StringBuffer();
		
		try{
			JSONObject goods= new JSONObject();
		boolean flag_special_amount=false;
		Iterator  keyIter = goodsObj.keys();
        String key="";
        Log.i(TAG,"Obj keys--->"+goodsObj.keys());
        while( keyIter.hasNext())
        {
        	
            key = (String)keyIter.next();
            goods = (JSONObject) goodsObj.get(key);
//            valueMap.put(key, value);
//        }
        
//		for (int i = 0; i < newStr.length; i++) {
			if(!key.equals(C.TOTAL_LIST)){
				String	goods_info="名称："+goods.get(C.GOODS_NAME).toString()
									+" ,数量："+goods.get(C.GOODS_AMOUNT).toString()
									+" ,单价："+goods.get(C.GOODS_PRICE).toString().trim()+"(元)"
									+" ,小计："+goods.get(C.SUBTOTAL).toString()+"(元)"
									+(goods.get(C.SAVE).toString().equals("0")?"":(",节省："+goods.get(C.SAVE).toString()+"(元)"))
									+"\n";
				sb_all_buy_goods=sb_all_buy_goods.append(goods_info);
				String special_amount_info="";
				if(goods.has(C.SPECIAL_AMOUNT)){
					 special_amount_info="名称："+goods.get(C.GOODS_NAME).toString()
							 		 	+" ,数量："+goods.get(C.SPECIAL_AMOUNT).toString()+"\n";
					 
					 sb_specal_amount_list=sb_specal_amount_list.append(special_amount_info);
					 flag_special_amount=true;
					 tv_specal_amount_list.setText(sb_specal_amount_list.substring(0,sb_specal_amount_list.length()-1));
						
				}else{
					if(!flag_special_amount){
						if(ll_special.getChildCount()!=0){
						ll_special.removeAllViews();
						}
					}else{
						if(ll_special.getChildCount()==0){
							ll_special.removeAllViews();
							ll_special.addView(tv_divided_line1,0);
							ll_special.addView(tv_specal_amount_list_title,1);
							ll_special.addView(tv_specal_amount_list,2);
						}
						tv_divided_line1.setVisibility(View.VISIBLE);
						tv_specal_amount_list_title.setVisibility(View.VISIBLE);
						
					}
				}
			}else{
				//最后一个为总计
				String total_and_save="总计："+goods.get(C.TOTAL).toString()+"(元) "
			 		 	+(goods.get(C.ALL_SAVE).toString().equals("0")?"":("\n节省："+goods.get(C.ALL_SAVE).toString())+"(元)");
				 
				sb_total_and_save.append(total_and_save);
			}
			
		}
		tv_all_buy_goods.setText(sb_all_buy_goods.substring(0,sb_all_buy_goods.length()-1));
		tv_total_and_save.setText(sb_total_and_save);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 保留小数点后两位
	 * @param str
	 * @return
	 */
	private String formatPrice(Object str) {
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(str);
	}
	/**
	 * 初始化设置listview item的值，由于没有网络，此处写死
	 * @param flag
	 */
	private void setListItemContent(int flag) {
		for (int i = 0; i < 3; i++) {
           Map<String, Object> map = new HashMap<String, Object>();
           
    	   map.put(C.GOODS_NAME,  i==0?C.COCA_COLA:(i==1?C.BADMINTON:C.APPLE));
    	   if(flag==0&&i<2){
    		   map.put(C.GOODS_SPECIAL_OFFERS, C.TWO_GET_ONE );
    	   }else if(flag==1){
    		   map.put(C.GOODS_SPECIAL_OFFERS, "" );
    	   }else if(flag==2&&i==2){
    		   map.put(C.GOODS_SPECIAL_OFFERS, C.NINE_FIVE);
    	   }else if(flag==3){
    		   map.put(C.GOODS_SPECIAL_OFFERS, i<2?C.TWO_GET_ONE:C.NINE_FIVE);
    	   }else{
    		   map.put(C.GOODS_SPECIAL_OFFERS, "" );
    	   }
    	   
    	   
    	   map.put(C.GOODS_PRICE,  "单价:"+(i==0?"3.00":(i==1?"1.00":"5.50"))+"(元)");
    	   map.put(C.GOODS_AMOUNT,   i==0?"3":(i==1?"5":"2"));
    	   
            
    	   mlistItems.add(map);
        }
	}

}
