/*
 * @author http://blog.csdn.net/singwhatiwanna
 */
package com.zy.pos.ui;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.zy.indicatorfragment.R;
import com.zy.pos.adapter.MyAdapter;
import com.zy.pos.outinterface.IPOSListener;
import com.zy.pos.util.ResourceUtil;

@SuppressWarnings("static-access")
public abstract class IndicatorFragmentActivity extends FragmentActivity implements OnPageChangeListener,IPOSListener {
    private static final String TAG = "IndicatorFragmentActivity";

    public static final String EXTRA_TAB = "tab";
    public static final String EXTRA_QUIT = "extra.quit";

    protected int mCurrentTab = 0;
    protected int mLastTab = -1;

    //存放选项卡信息的列表
    protected ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

    //viewpager adapter
    protected MyAdapter myAdapter = null;

    //viewpager
    protected ViewPager mPager;

    //选项卡控件
    protected TitleIndicator mIndicator;

    public TitleIndicator getIndicator() {
        return mIndicator;
    }
    
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(getMainViewResId());
        initViews();

        //设置viewpager内部页面之间的间距
        mPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.page_margin_width));
        //设置viewpager内部页面间距的drawable
        mPager.setPageMarginDrawable(R.color.page_viewer_margin_color);
        
    }
    
    @Override
    protected void onDestroy() {
        mTabs.clear();
        mTabs = null;
        myAdapter.notifyDataSetChanged();
        myAdapter = null;
        mPager.setAdapter(null);
        mPager = null;
        mIndicator = null;

        super.onDestroy();
    }

    private final void initViews() {
        // 这里初始化界面
        mCurrentTab = supplyTabs(mTabs);
        Intent intent = getIntent();
        if (intent != null) {
            mCurrentTab = intent.getIntExtra(EXTRA_TAB, mCurrentTab);
        }
        Log.d(TAG, "mTabs.size() == " + mTabs.size() + ", cur: " + mCurrentTab);
        myAdapter = new MyAdapter(this, getSupportFragmentManager(), mTabs);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(myAdapter);
        mPager.setOnPageChangeListener(this);
        mPager.setOffscreenPageLimit(mTabs.size());

        mIndicator = (TitleIndicator) findViewById(R.id.pagerindicator);
        mIndicator.init(mCurrentTab, mTabs, mPager);

        mPager.setCurrentItem(mCurrentTab);
        mLastTab = mCurrentTab;
        
    }

    /**
     * 添加一个选项卡
     * @param tab
     */
    public void addTabInfo(TabInfo tab) {
        mTabs.add(tab);
        myAdapter.notifyDataSetChanged();
    }

    /**
     * 从列表添加选项卡
     * @param tabs
     */
    public void addTabInfos(ArrayList<TabInfo> tabs) {
        mTabs.addAll(tabs);
        myAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mIndicator.onScrolled((mPager.getWidth() + mPager.getPageMargin()) * position + positionOffsetPixels);
    }
    
    @Override
    public void onPageSelected(int position) {
//    	Log.i(TAG, "position at---->"+position);
        mIndicator.onSwitched(position);
        mCurrentTab = position;
        mPOSListener.transferMsg(position);
      
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            mLastTab = mCurrentTab;
        }
    }

    protected TabInfo getFragmentById(int tabId) {
        if (mTabs == null) return null;
        for (int index = 0, count = mTabs.size(); index < count; index++) {
            TabInfo tab = mTabs.get(index);
            if (tab.getId() == tabId) {
                return tab;
            }
        }
        return null;
    }
    /**
     * 跳转到任意选项卡
     * @param tabId 选项卡下标
     */
    public void navigate(int tabId) {
        for (int index = 0, count = mTabs.size(); index < count; index++) {
            if (mTabs.get(index).getId() == tabId) {
                mPager.setCurrentItem(index);
            }
        }
    }

    @Override
    public void onBackPressed() {
            finish();
    }

    /**
     * 返回layout id
     * @return layout id
     */
    protected int getMainViewResId() {
        return R.layout.titled_fragment_tab_activity;
    }

    /**
     * 在这里提供要显示的选项卡数据
     */
    protected abstract int supplyTabs(List<TabInfo> tabs);

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // for fix a known issue in support library
        // https://code.google.com/p/android/issues/detail?id=19917
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    private IPOSListener mPOSListener;
    @Override
    public void onAttachFragment(Fragment fragment) {
	    try {
	    	mPOSListener=(IPOSListener) fragment;
	    } catch (Exception e) {
    }
	    super.onAttachFragment(fragment);
    }
    
   /* @Override
    public void transferMsg(int flag_fragment) {
    // TODO Auto-generated method stub
    	 Log.i(TAG,"由Fragment传输过来的信息");
    }*/

}
