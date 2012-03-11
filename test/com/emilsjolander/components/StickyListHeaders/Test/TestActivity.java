package com.emilsjolander.components.StickyListHeaders.Test;

import android.app.Activity;
import android.os.Bundle;

import com.emilsjolander.components.StickyListHeaders.R;
import com.emilsjolander.components.StickyListHeaders.StickyListHeadersListViewWrapper;

public class TestActivity extends Activity {

	private StickyListHeadersListViewWrapper stickyList;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        stickyList = (StickyListHeadersListViewWrapper) findViewById(R.id.list);
        stickyList.getWrappedList().setAdapter(new TestAdapter(this));
    }
}