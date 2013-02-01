package com.emilsjolander.components.stickylistheaders;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * @author Emil Sj��lander
 * 
 * 
 *         Copyright 2012 Emil Sj��lander
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
 */
public class StickyListHeadersListView extends ListView implements
		OnScrollListener {

	public interface OnHeaderClickListener {
		public void onHeaderClick(StickyListHeadersListView l, View header,
				int itemPosition, long headerId, boolean currentlySticky);
	}
	
	private OnScrollListener scrollListener;
	private boolean areHeadersSticky = true;
	private int headerBottomPosition;
	private View header;
	private int dividerHeight;
	private Drawable divider;
	private boolean clippingToPadding;
	private boolean clipToPaddingHasBeenSet;
	private final Rect clippingRect = new Rect();
	private Long currentHeaderId = null;
	private StickyListHeadersAdapterWrapper adapter;
	private float headerDownY = -1;
	private boolean headerBeingPressed = false;
	private OnHeaderClickListener onHeaderClickListener;
	private int headerPosition;
	private ViewConfiguration viewConfig;
	private ArrayList<View> footerViews;

	private StickyListHeadersAdapterWrapper.OnHeaderClickListener addapterHeaderClickListener = new StickyListHeadersAdapterWrapper.OnHeaderClickListener() {

		@Override
		public void onHeaderClick(View header, int itemPosition, long headerId) {
			if (onHeaderClickListener != null) {
				onHeaderClickListener.onHeaderClick(
						StickyListHeadersListView.this, header, itemPosition,
						headerId, false);
			}
		}
	};

	private DataSetObserver dataSetChangedObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			reset();
		}

		@Override
		public void onInvalidated() {
			reset();
		}
	};

	public StickyListHeadersListView(Context context) {
		this(context, null);
	}

	public StickyListHeadersListView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public StickyListHeadersListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

		super.setOnScrollListener(this);
		// null out divider, dividers are handled by adapter so they look good
		// with headers
		super.setDivider(null);
		super.setDividerHeight(0);
		setVerticalFadingEdgeEnabled(false);
		viewConfig = ViewConfiguration.get(context);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(changed){
			reset();
		}
	}

	private void reset() {
		headerBottomPosition = 0;
		header = null;
		currentHeaderId = null;
	}

	@Override
	public boolean performItemClick(View view, int position, long id) {
		if(view instanceof WrapperView){
			view = ((WrapperView) view).item;
		}
		return super.performItemClick(view, position, id);
	}

	/**
	 * can only be set to false if headers are sticky, not compatible with
	 * fading edges
	 */
	@Override
	public void setVerticalFadingEdgeEnabled(boolean verticalFadingEdgeEnabled) {
		if (areHeadersSticky) {
			super.setVerticalFadingEdgeEnabled(false);
		} else {
			super.setVerticalFadingEdgeEnabled(verticalFadingEdgeEnabled);
		}
	}

	@Override
	public void setDivider(Drawable divider) {
		this.divider = divider;
		if(divider != null){
			int dividerDrawableHeight = divider.getIntrinsicHeight();
			if(dividerDrawableHeight>=0){
				setDividerHeight(dividerDrawableHeight);
			}
		}
		if (adapter != null) {
			adapter.setDivider(divider);
			requestLayout();
			invalidate();
		}
	}

	@Override
	public void setDividerHeight(int height) {
		dividerHeight = height;
		if (adapter != null) {
			adapter.setDividerHeight(height);
			requestLayout();
			invalidate();
		}
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		scrollListener = l;
	}

	public void setAreHeadersSticky(boolean areHeadersSticky) {
		if (this.areHeadersSticky != areHeadersSticky) {
			if (areHeadersSticky) {
				super.setVerticalFadingEdgeEnabled(false);
			}
			requestLayout();
			this.areHeadersSticky = areHeadersSticky;
		}
	}

	public boolean getAreHeadersSticky() {
		return areHeadersSticky;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (!clipToPaddingHasBeenSet) {
			clippingToPadding = true;
		}
		if (!(adapter instanceof StickyListHeadersAdapter)) {
			throw new IllegalArgumentException(
					"Adapter must implement StickyListHeadersAdapter");
		}
		this.adapter = new StickyListHeadersAdapterWrapper(getContext(),
				(StickyListHeadersAdapter) adapter);
		this.adapter.setDivider(divider);
		this.adapter.setDividerHeight(dividerHeight);
		this.adapter.registerDataSetObserver(dataSetChangedObserver);
		this.adapter.setOnHeaderClickListener(addapterHeaderClickListener);
		reset();
		super.setAdapter(this.adapter);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			scrollChanged(getFirstVisiblePosition());
		}
		super.dispatchDraw(canvas);
		if (header == null || !areHeadersSticky) {
			return;
		}

		int headerHeight = getHeaderHeight();
		int top = headerBottomPosition - headerHeight;
		clippingRect.left = getPaddingLeft();
		clippingRect.right = getWidth() - getPaddingRight();
		clippingRect.bottom = top + headerHeight;
		if (clippingToPadding) {
			clippingRect.top = getPaddingTop();
		} else {
			clippingRect.top = 0;
		}

		canvas.save();
		canvas.clipRect(clippingRect);
		canvas.translate(getPaddingLeft(), top);
		header.draw(canvas);
		canvas.restore();
	}

	private void measureHeader() {
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(),
				MeasureSpec.EXACTLY);
		int heightMeasureSpec = 0;

		ViewGroup.LayoutParams params = header.getLayoutParams();
		if (params != null && params.height > 0) {
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY);
		} else {
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		header.measure(widthMeasureSpec, heightMeasureSpec);
		header.layout(getLeft() + getPaddingLeft(), 0, getRight()
				- getPaddingRight(), header.getMeasuredHeight());
	}

	private int getHeaderHeight() {
		if (header != null) {
			return header.getMeasuredHeight();
		}
		return 0;
	}

	@Override
	public void setClipToPadding(boolean clipToPadding) {
		super.setClipToPadding(clipToPadding);
		clippingToPadding = clipToPadding;
		clipToPaddingHasBeenSet = true;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (scrollListener != null) {
			scrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			scrollChanged(firstVisibleItem);
		}
	}

	private void scrollChanged(int firstVisibleItem) {
		if (adapter == null){
			return;
		}
		
		int adapterCount = adapter.getCount();
		if(adapterCount == 0 || !areHeadersSticky){
			
		}

		final int listViewHeaderCount = getHeaderViewsCount();
		firstVisibleItem = getFixedFirstVisibleItem(firstVisibleItem) - listViewHeaderCount;
		
		if(firstVisibleItem < 0 || firstVisibleItem > adapterCount-1){
			header = null;
			currentHeaderId = null;
			headerBottomPosition = -1;
			updateHeaderVisibilities();
			invalidate();
			return;
		}

		long newHeaderId = adapter.getHeaderId(firstVisibleItem);
		if (currentHeaderId == null || currentHeaderId != newHeaderId) {
			headerPosition = firstVisibleItem;
			header = adapter.getHeaderView(headerPosition, header,
					this);
			measureHeader();
		}
		currentHeaderId = newHeaderId;

		int childCount = getChildCount();
		
		if (childCount != 0) {
			View viewToWatch = null;
			int watchingChildDistance = 99999;
			boolean viewToWatchIsFooter = false;

			for (int i = 0; i < childCount; i++) {
				View child = super.getChildAt(i);
				boolean childIsFooter = footerViews != null && footerViews.contains(child);
				
				int childDistance;
				if (clippingToPadding) {
					childDistance = child.getTop() - getPaddingTop();
				} else {
					childDistance = child.getTop();
				}

				if (childDistance < 0) {
					continue;
				}

				if (viewToWatch == null
						|| (!viewToWatchIsFooter && !((WrapperView)viewToWatch).hasHeader())
						|| ((childIsFooter || ((WrapperView)child).hasHeader()) && childDistance < watchingChildDistance)) {
					viewToWatch = child;
					viewToWatchIsFooter = childIsFooter;
					watchingChildDistance = childDistance;
				}
			}

			int headerHeight = getHeaderHeight();

			if (viewToWatch != null && (viewToWatchIsFooter || ((WrapperView)viewToWatch).hasHeader())) {

				if (firstVisibleItem == listViewHeaderCount && super.getChildAt(0).getTop() > 0
						&& !clippingToPadding) {
					headerBottomPosition = 0;
				} else {
					if (clippingToPadding) {
						headerBottomPosition = Math.min(viewToWatch.getTop(),
								headerHeight + getPaddingTop());
						headerBottomPosition = headerBottomPosition < getPaddingTop() ? headerHeight
								+ getPaddingTop()
								: headerBottomPosition;
					} else {
						headerBottomPosition = Math.min(viewToWatch.getTop(),
								headerHeight);
						headerBottomPosition = headerBottomPosition < 0 ? headerHeight
								: headerBottomPosition;
					}
				}
			} else {
				headerBottomPosition = headerHeight;
				if (clippingToPadding) {
					headerBottomPosition += getPaddingTop();
				}
			}
		}
		
		updateHeaderVisibilities();
		invalidate();
	}
	
	@Override
	public void addFooterView(View v) {
		super.addFooterView(v);
		if(footerViews == null){
			footerViews = new ArrayList<View>();
		}
		footerViews.add(v);
	}
	
	@Override
	public boolean removeFooterView(View v) {
		boolean removed = super.removeFooterView(v);
		if(removed){
			footerViews.remove(v);
		}
		return removed;
	}
	
	private void updateHeaderVisibilities(){
		int top = clippingToPadding ? getPaddingTop() : 0;
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = super.getChildAt(i);
			if(child instanceof WrapperView){
				WrapperView wrapperViewChild = (WrapperView) child;
				if (wrapperViewChild.hasHeader()) {
					View childHeader = wrapperViewChild.header;
					if (wrapperViewChild.getTop() < top) {
						childHeader.setVisibility(View.INVISIBLE);
					} else {
						childHeader.setVisibility(View.VISIBLE);
					}
				}
			}
		}
	}

	private int getFixedFirstVisibleItem(int firstVisibleItem) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return firstVisibleItem;
		}

		for (int i = 0; i < getChildCount(); i++) {
			if (getChildAt(i).getBottom() >= 0) {
				firstVisibleItem += i;
				break;
			}
		}

		// work around to fix bug with firstVisibleItem being to high because
		// listview does not take clipToPadding=false into account
		if (!clippingToPadding && getPaddingTop() > 0) {
			if (super.getChildAt(0).getTop() > 0) {
				if (firstVisibleItem > 0) {
					firstVisibleItem -= 1;
				}
			}
		}
		return firstVisibleItem;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollListener != null) {
			scrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void setSelectionFromTop(int position, int y) {
		if (areHeadersSticky) {
			y += getHeaderHeight();
		}
		super.setSelectionFromTop(position, y);
	}

	public void setOnHeaderClickListener(
			OnHeaderClickListener onHeaderClickListener) {
		this.onHeaderClickListener = onHeaderClickListener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		if (action == MotionEvent.ACTION_DOWN
				&& ev.getY() <= headerBottomPosition) {
			headerDownY = ev.getY();
			headerBeingPressed = true;
			header.setPressed(true);
			header.invalidate();
			invalidate(0, 0, getWidth(), headerBottomPosition);
			return true;
		}
		if (headerBeingPressed) {
			if (Math.abs(ev.getY() - headerDownY) < viewConfig
					.getScaledTouchSlop()) {
				if (action == MotionEvent.ACTION_UP
						|| action == MotionEvent.ACTION_CANCEL) {
					headerDownY = -1;
					headerBeingPressed = false;
					header.setPressed(false);
					header.invalidate();
					invalidate(0, 0, getWidth(), headerBottomPosition);
					if (onHeaderClickListener != null) {
						onHeaderClickListener.onHeaderClick(this, header,
								headerPosition, currentHeaderId, true);
					}
				}
				return true;
			} else {
				headerDownY = -1;
				headerBeingPressed = false;
				header.setPressed(false);
				header.invalidate();
				invalidate(0, 0, getWidth(), headerBottomPosition);
			}
		}
		return super.onTouchEvent(ev);
	}

}
