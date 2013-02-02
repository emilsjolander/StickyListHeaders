package com.emilsjolander.components.stickylistheaders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class StickyListHeadersListViewWrapper extends FrameLayout {

	private View header = null;
	private int headerBottomPosition = -1;

	public StickyListHeadersListViewWrapper(Context context) {
		this(context, null, 0);
	}

	public StickyListHeadersListViewWrapper(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StickyListHeadersListViewWrapper(Context context,
			AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	void setHeader(View header) {
		if (this.header != null) {
			throw new IllegalStateException(
					"You must first remove the old header first");
		}
		this.header = header;
		if (header != null) {
			View list = getChildAt(0);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.leftMargin = list.getPaddingLeft();
			params.rightMargin = list.getPaddingRight();
			header.setLayoutParams(params);
			setHeaderBottomPosition(headerBottomPosition);
			addView(header);
		}
	}

	View removeHeader() {
		if (this.header != null) {
			removeView(this.header);
		}
		View header = this.header;
		this.header = null;
		return header;
	}

	boolean hasHeader() {
		return header != null;
	}

	boolean isHeader(View v) {
		return header == v;
	}

	int getHeaderHeight() {
		if (header == null) {
			return 0;
		}
		int parentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
		int parentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY);
		measureChild(header, parentWidthMeasureSpec, parentHeightMeasureSpec);
		return header.getMeasuredHeight();
	}

	@SuppressLint("NewApi")
	void setHeaderBottomPosition(int headerBottomPosition) {
		this.headerBottomPosition = headerBottomPosition;
		if (header != null) {
			header.setTranslationY(this.headerBottomPosition
					- header.getMeasuredHeight());
		}
	}

}
