package com.emilsjolander.components.StickyListHeaders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class WrapperView {
	
	private LinearLayout v;
	private int layoutId = R.layout.wrapper;
	
	public WrapperView(Context c) {
		v = (LinearLayout) LayoutInflater.from(c).inflate(layoutId, null);
	}
	
	public WrapperView(View v) {
		this.v = (LinearLayout) v;
	}

	public View wrapViews(View... views){
		v.removeAllViews();
		for(View child : views){
			v.addView(child);
		}
		return v;
	}

}
