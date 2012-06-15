package com.emilsjolander.components.StickyListHeaders.test;

import android.app.Activity;
import android.os.Bundle;

import com.emilsjolander.components.StickyListHeaders.R;
import com.emilsjolander.components.StickyListHeaders.StickyListHeadersListView;
/**
 * 
 * @author Emil Sjšlander
 * 
 * 
Copyright 2012 Emil Sjšlander

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
public class TestActivity extends Activity {
	
	private StickyListHeadersListView stickyList;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        stickyList = (StickyListHeadersListView) findViewById(R.id.list);
        
        stickyList.setAdapter(new TestBaseAdapter(this));

//        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
//        stickyList.setAdapter(new TestCursorAdapter(this,cursor));
    }
    
}