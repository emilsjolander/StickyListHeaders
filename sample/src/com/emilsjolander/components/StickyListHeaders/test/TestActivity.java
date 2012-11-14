package com.emilsjolander.components.StickyListHeaders.test;

import android.app.Activity;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.emilsjolander.components.StickyListHeaders.R;
import com.emilsjolander.components.StickyListHeaders.StickyListHeadersListView;
/**
 * 
 * @author Emil Sj�lander
 * 
 * 
Copyright 2012 Emil Sj�lander

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 *
 */
public class TestActivity extends Activity implements OnScrollListener {

	private static final String KEY_LIST_POSITION = "KEY_LIST_POSITION";
	private StickyListHeadersListView stickyList;
	private int firstVisible;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		stickyList = (StickyListHeadersListView) findViewById(R.id.list);
		stickyList.setOnScrollListener(this);
		
		if (savedInstanceState != null) {
			firstVisible = savedInstanceState.getInt(KEY_LIST_POSITION);
		}

		stickyList.setAdapter(new TestBaseAdapter(this));
		stickyList.setSelection(firstVisible );

		//        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		//        stickyList.setAdapter(new TestCursorAdapter(this,cursor));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_LIST_POSITION, firstVisible);
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.firstVisible = firstVisibleItem;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

}