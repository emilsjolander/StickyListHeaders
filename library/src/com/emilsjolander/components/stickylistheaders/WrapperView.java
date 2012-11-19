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
final class WrapperView extends LinearLayout {
	// TODO Draw divider as Drawable rather than using a whole view
	// TODO Custom ViewGroup that is much more simple and faster than LinearLayout

	View item;
	View divider;
	View header;

	WrapperView(Context c) {
		super(c);
		setOrientation(VERTICAL);
	}

	void update(View item, View header, View divider) {
		if (item == null) {
			throw new NullPointerException("List view item must not be null.");
		}
		removeAllViews();
		this.item = item;
		this.header = header;
		this.divider = divider;
		if (header != null) addView(header);
		if (divider != null) addView(divider);
		addView(item);
	}

	boolean hasHeader() {
		return header != null;
	}
}
