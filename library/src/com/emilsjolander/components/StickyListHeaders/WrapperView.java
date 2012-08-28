package com.emilsjolander.components.StickyListHeaders;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
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
public class WrapperView {
	
	private LinearLayout v;
	
	public WrapperView(Context c) {
		v = new LinearLayout(c);
		v.setId(R.id.__stickylistheaders_wrapper_view);
		v.setOrientation(LinearLayout.VERTICAL);
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
