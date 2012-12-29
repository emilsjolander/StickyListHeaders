package com.emilsjolander.components.stickylistheaders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;

/**
 * 
 * @author Emil Sj�lander
 * 
 * 
 *         Copyright 2012 Emil Sj�lander
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
final class WrapperView extends ViewGroup implements Checkable {

	View item;
	Drawable divider;
	int dividerHeight;
	View header;
	boolean checkable;

	public WrapperView(Context c) {
		super(c);
	}

	void update(View item, View header, Drawable divider, int dividerHeight) {
		if (item == null) {
			throw new NullPointerException("List view item must not be null.");
		}
		
		if(this.item != item){
			removeView(this.item);
			this.item = item;
			// keep track of whether or not the wrapped item is Checkable 
			// to know if we can delegate Checkable methods to it.
			checkable = item instanceof Checkable;
			addView(item);
		}
		
		if(this.header != header){
			if(this.header != null){
				removeView(this.header);
			}
			this.header = header;
			if (header != null) {
				addView(header);
			}
		}
		
		if(this.divider != divider){
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
		int measuredHeight = 0;
		if (header != null) {
			header.measure(MeasureSpec.makeMeasureSpec(measuredWidth,
					MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED));
			measuredHeight += header.getMeasuredHeight();
		} else if (divider != null) {
			measuredHeight += dividerHeight;
		}
		item.measure(
				MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
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

	@Override
	public boolean isChecked() {
		return checkable ? ((Checkable) item).isChecked() : false;
	}

	@Override
	public void setChecked(boolean checked) {
		if (checkable) {
			// delegate checked state to wrapped view if it implements Checkable
			// interface to support listviews that indicate choices (single,
			// multiple, multiple modal).
			((Checkable) item).setChecked(checked);
		}
	}

	@Override
	public void toggle() {
		setChecked(!isChecked());
	}
}
