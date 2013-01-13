package com.emilsjolander.components.stickylistheaders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * 
 * @author Emil Sjölander
 * 
 * 
 *         Copyright 2012 Emil Sjölander
 * 
 *         Licensed under the Apache License, Version 2.0 (the "License"); you
 *         may not use this file except in compliance with the License. You may
 *         obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied. See the License for the specific language governing
 *         permissions and limitations under the License.
 * 
 */
class WrapperView extends ViewGroup {

	View item;
	Drawable divider;
	int dividerHeight;
	View header;

	public WrapperView(Context c) {
		super(c);
	}

	void update(View item, View header, Drawable divider, int dividerHeight) {
		if (item == null) {
			throw new NullPointerException("List view item must not be null.");
		}

		if (this.item != item) {
			removeView(this.item);
			this.item = item;
			final ViewParent parent = item.getParent();
			if(parent != null && parent != this) {
				if(parent instanceof ViewGroup) {
					((ViewGroup) parent).removeView(item);
				}
			}
			addView(item);
		}

		if (this.header != header) {
			if (this.header != null) {
				removeView(this.header);
			}
			this.header = header;
			if (header != null) {
				addView(header);
			}
		}

		if (this.divider != divider) {
			this.divider = divider;
			this.dividerHeight = dividerHeight;
			invalidate();
		}
	}

	boolean hasHeader() {
		return header != null;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth,
				MeasureSpec.EXACTLY);
		int measuredHeight = 0;
		if (header != null) {
			ViewGroup.LayoutParams params = header.getLayoutParams();
			if (params != null && params.height > 0) {
				header.measure(childWidthMeasureSpec,
						MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY));
			} else {
				header.measure(childWidthMeasureSpec,
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			}
			measuredHeight += header.getMeasuredHeight();
		} else if (divider != null) {
			measuredHeight += dividerHeight;
		}
		ViewGroup.LayoutParams params = item.getLayoutParams();
		if (params != null && params.height > 0) {
			item.measure(childWidthMeasureSpec,
					MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY));
		} else {
			item.measure(childWidthMeasureSpec,
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		}
		measuredHeight += item.getMeasuredHeight();

		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		// don't really know why these values aren't what i want them to be from
		// the start
		l = 0;
		t = 0;
		r = getWidth();
		b = getHeight();

		if (header != null) {
			header.layout(l, t, r, header.getMeasuredHeight());
			item.layout(l, header.getMeasuredHeight(), r, b);
		} else if (divider != null) {
			divider.setBounds(l, t, r, dividerHeight);
			item.layout(l, dividerHeight, r, b);
		} else {
			item.layout(l, t, r, b);
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (header == null && divider != null) {
			// Drawable.setBounds() does not seem to work pre-honeycomb. So have
			// to do this instead
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				canvas.clipRect(0, 0, getWidth(), dividerHeight);
			}
			divider.draw(canvas);
		}
	}
}
