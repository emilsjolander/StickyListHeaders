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
import android.view.Menu;

/**
 * @author Emil Sjölander
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TestActivity extends FragmentActivity implements TabListener, OnPageChangeListener {

	private ViewPager mPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setOnPageChangeListener(this);
		mPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));

	    ActionBar actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.addTab(getActionBar().newTab().setText("1").setTabListener(this));
	    actionBar.addTab(getActionBar().newTab().setText("2").setTabListener(this));
	    actionBar.addTab(getActionBar().newTab().setText("3").setTabListener(this));
	    actionBar.addTab(getActionBar().newTab().setText("4").setTabListener(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int position) {
		getActionBar().setSelectedNavigationItem(position);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mPager.setCurrentItem(tab.getPosition(), true);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

}