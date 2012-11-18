package com.emilsjolander.components.stickylistheaders;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
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
public class WrapperView extends LinearLayout {

	private View item;
	private View divider;
	private View header;

	public WrapperView(Context c) {
		super(c, null);
		setOrientation(VERTICAL);
	}

	public View getItem() {
		return item;
	}

	public View getDivider() {
		return divider;
	}

	public View getHeader() {
		return header;
	}

	public boolean hasHeader() {
		return header != null;
	}

	public WrapperView wrapViews(View item, View divider, View header){
		removeAllViews();
		this.item = item;
		this.divider = divider;
		this.header = header;
		if (divider != null) addView(divider);
		if (header != null) addView(header);
		addView(item);
		return this;
	}

}
