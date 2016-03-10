package com.zy.pos.activity;

import java.util.List;

import android.app.Fragment;
import android.os.Bundle;

import com.zy.indicatorfragment.R;
import com.zy.pos.fragment.FragmentOne;
import com.zy.pos.fragment.FragmentThree;
import com.zy.pos.fragment.FragmentTwo;
import com.zy.pos.ui.IndicatorFragmentActivity;
import com.zy.pos.ui.TabInfo;

public class MainActivity extends IndicatorFragmentActivity {

    public static final int FRAGMENT_ONE = 0;
    public static final int FRAGMENT_TWO = 1;
    public static final int FRAGMENT_THREE = 2;
    public static final int FRAGMENT_FOUR = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int supplyTabs(List<TabInfo> tabs) {
        tabs.add(new TabInfo(FRAGMENT_ONE, getString(R.string.fragment_one),
                FragmentOne.class));
        tabs.add(new TabInfo(FRAGMENT_TWO, getString(R.string.fragment_two),
                FragmentTwo.class));
        tabs.add(new TabInfo(FRAGMENT_THREE, getString(R.string.fragment_three),
                FragmentThree.class));
        tabs.add(new TabInfo(FRAGMENT_FOUR, getString(R.string.fragment_four),
        		FragmentThree.class));

        return FRAGMENT_ONE;
    }

	@Override
	public void transferMsg(int flag_fragment) {
		// TODO Auto-generated method stub
		
	}

  
    
}
