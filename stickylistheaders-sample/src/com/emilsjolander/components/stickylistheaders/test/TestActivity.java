package com.emilsjolander.components.stickylistheaders.test;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

/**
 * @author Emil Sjölander
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TestActivity extends FragmentActivity implements OnPageChangeListener {

	private ViewPager mPager;
	private TabListener tabChangeListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setOnPageChangeListener(this);
		mPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			
			tabChangeListener = new TabListener() {

				@Override
				public void onTabReselected(Tab tab, FragmentTransaction ft) {}

				@Override
				public void onTabSelected(Tab tab, FragmentTransaction ft) {
					mPager.setCurrentItem(tab.getPosition(), true);
				}

				@Override
				public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
			};
			
		    ActionBar actionBar = getActionBar();
		    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		    actionBar.addTab(getActionBar().newTab().setText("1").setTabListener(tabChangeListener));
		    actionBar.addTab(getActionBar().newTab().setText("2").setTabListener(tabChangeListener));
		    actionBar.addTab(getActionBar().newTab().setText("3").setTabListener(tabChangeListener));
		    actionBar.addTab(getActionBar().newTab().setText("4").setTabListener(tabChangeListener));
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int position) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setSelectedNavigationItem(position);
		}
	}

}