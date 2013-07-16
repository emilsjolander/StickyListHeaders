package com.emilsjolander.components.stickylistheaders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * 
 * the view that wrapps a divider header and a normal list item. The listview sees this as 1 item
 * 
 * @author Emil Sjölander
 */
class WrapperView extends ViewGroup {

	View mWrapped;
	Drawable mDivider;
	int mDividerHeight;
	View mHeader;
	int mItemTop;

	public WrapperView(Context c) {
		super(c);
	}

	void update(View wrapped, View header, Drawable divider, int dividerHeight) {
		
		//every wrapperview must have a list item
		if (wrapped == null) {
			throw new IllegalArgumentException("Wrapped view must be non-null.");
		}
		
		//only remove the current item if it is not the same as the new item. this can happen if wrapping a recycled view
		if (mWrapped != wrapped) {
			removeView(mWrapped);
			mWrapped = wrapped;
			ViewParent parent = wrapped.getParent();
			if(parent != this && parent instanceof ViewGroup) {
				((ViewGroup) parent).removeView(wrapped);
			}
			addView(wrapped);
		}

		//same logic as above but for the header
		if (mHeader != header) {
			if (mHeader != null) {
				removeView(mHeader);
			}
			mHeader = header;
			if (header != null) {
				addView(header);
			}
		}

		if (mDivider != divider) {
			mDivider = divider;
			mDividerHeight = dividerHeight;
			invalidate();
		}
	}

	boolean hasHeader() {
		return (mHeader != null);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth,
				MeasureSpec.EXACTLY);
		int measuredHeight = 0;
		
		//measure header or divider. when there is a header visible it acts as the divider
		if (mHeader != null) {
			ViewGroup.LayoutParams params = mHeader.getLayoutParams();
			if (params != null && params.height > 0) {
				mHeader.measure(childWidthMeasureSpec,
						MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY));
			} else {
				mHeader.measure(childWidthMeasureSpec,
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			}
			measuredHeight += mHeader.getMeasuredHeight();
		} else if (mDivider != null) {
			measuredHeight += mDividerHeight;
		}
		
		//measure item
		ViewGroup.LayoutParams params = mWrapped.getLayoutParams();
		if (params != null && params.height > 0) {
			mWrapped.measure(childWidthMeasureSpec,
					MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY));
		} else {
			mWrapped.measure(childWidthMeasureSpec,
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		}
		measuredHeight += mWrapped.getMeasuredHeight();

		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		l = 0;
		t = 0;
		r = getWidth();
		b = getHeight();

		if (mHeader != null) {
			int headerHeight = mHeader.getMeasuredHeight();
			mHeader.layout(l, t, r, headerHeight);
			mItemTop = headerHeight;
			mWrapped.layout(l, headerHeight, r, b);
		} else if (mDivider != null) {
			mDivider.setBounds(l, t, r, mDividerHeight);
			mItemTop = mDividerHeight;
			mWrapped.layout(l, mDividerHeight, r, b);
		} else {
			mItemTop = t;
			mWrapped.layout(l, t, r, b);
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mHeader == null && mDivider != null) {
			// Drawable.setBounds() does not seem to work pre-honeycomb. So have
			// to do this instead
			if (!Ver.honeycomb()) {
				canvas.clipRect(0, 0, getWidth(), mDividerHeight);
			}
			mDivider.draw(canvas);
		}
	}
}
