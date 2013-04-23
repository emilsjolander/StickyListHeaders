package com.emilsjolander.components.stickylistheaders;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;

/**
 * @author Emil Sjölander
 */
@SuppressLint("NewApi")
public class StickyListHeadersListView extends ListView implements
		OnScrollListener, OnClickListener {

	public interface OnHeaderClickListener {
		public void onHeaderClick(StickyListHeadersListView l, View header,
				int itemPosition, long headerId, boolean currentlySticky);
	}

	private OnScrollListener scrollListener;
	private boolean areHeadersSticky = true;
	private int dividerHeight;
	private Drawable divider;
	private boolean clippingToPadding;
	private boolean clipToPaddingHasBeenSet;
	private Long currentHeaderId = null;
	private StickyListHeadersAdapterWrapper adapter;
	private OnHeaderClickListener onHeaderClickListener;
	private int headerPosition;
	private ArrayList<View> footerViews;
	private StickyListHeadersListViewWrapper frame;
	private boolean drawingListUnderStickyHeader = true;
	private boolean dataChanged = false;
	private boolean drawSelectorOnTop;
	private OnItemLongClickListener onItemLongClickListenerDelegate;
	private MultiChoiceModeListener multiChoiceModeListenerDelegate;
	private int positionToSetWhenAdapterIsReady = 0;
	private int offsetToSetWhenAdapterIsReady = 0;

	private DataSetObserver dataSetChangedObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			dataChanged = true;
			currentHeaderId = null;
		}

		@Override
		public void onInvalidated() {
			currentHeaderId = null;
			frame.removeHeader();
		}
	};
	private OnItemLongClickListener onItemLongClickListenerWrapper = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> l, View v, int position,
				long id) {
			if (onItemLongClickListenerDelegate != null) {
				return onItemLongClickListenerDelegate.onItemLongClick(l, v,
						adapter.translateListViewPosition(position), id);
			}
			return false;
		}

	};
	private MultiChoiceModeListener multiChoiceModeListenerWrapper;

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

		int[] attrsArray = new int[] { android.R.attr.drawSelectorOnTop };

		TypedArray a = context.obtainStyledAttributes(attrs, attrsArray,
				defStyle, 0);
		drawSelectorOnTop = a.getBoolean(0, false);
		a.recycle();
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			setMultiChoiceModeListenerWrapper();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (frame == null) {
			ViewGroup parent = ((ViewGroup) getParent());
			int listIndex = parent.indexOfChild(this);
			parent.removeView(this);

			int visibility = getVisibility();
			setVisibility(View.VISIBLE);
			
			frame = new StickyListHeadersListViewWrapper(getContext());
			frame.setSelector(getSelector());
			frame.setDrawSelectorOnTop(drawSelectorOnTop);
			frame.setVisibility(visibility);

			ViewGroup.MarginLayoutParams p = (MarginLayoutParams) getLayoutParams();
			if (clippingToPadding) {
				frame.setPadding(0, getPaddingTop(), 0, getPaddingBottom());
				setPadding(getPaddingLeft(), 0, getPaddingRight(), 0);
			}

			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			setLayoutParams(params);
			
			frame.addView(this);
			frame.setBackgroundDrawable(getBackground());
			super.setBackgroundDrawable(null);

			frame.setLayoutParams(p);
			parent.addView(frame, listIndex);
		}
	}

	@Override
	@Deprecated
	public void setBackgroundDrawable(Drawable background) {
		if (frame != null) {
			frame.setBackgroundDrawable(background);
		} else {
			super.setBackgroundDrawable(background);
		}
	}

	@Override
	public void setDrawSelectorOnTop(boolean onTop) {
		super.setDrawSelectorOnTop(onTop);
		drawSelectorOnTop = onTop;
		if (frame != null) {
			frame.setDrawSelectorOnTop(drawSelectorOnTop);
		}
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
		if (divider != null) {
			int dividerDrawableHeight = divider.getIntrinsicHeight();
			if (dividerDrawableHeight >= 0) {
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
        if (this.isInEditMode()) {
            super.setAdapter(adapter);
            return;
        }

		if (!clipToPaddingHasBeenSet) {
			clippingToPadding = true;
		}
		if (adapter != null && !(adapter instanceof StickyListHeadersAdapter)) {
			throw new IllegalArgumentException(
					"Adapter must implement StickyListHeadersAdapter");
		}

		if (this.adapter != null) {
			this.adapter.unregisterInternalDataSetObserver(dataSetChangedObserver);
			this.adapter = null;
		}

		if (adapter != null) {
			if (adapter instanceof SectionIndexer) {
				this.adapter = new StickyListHeadersSectionIndexerAdapterWrapper(
						getContext(), (StickyListHeadersAdapter) adapter);
			} else {
				this.adapter = new StickyListHeadersAdapterWrapper(
						getContext(), (StickyListHeadersAdapter) adapter);
			}
			this.adapter.setDivider(divider);
			this.adapter.setDividerHeight(dividerHeight);
			this.adapter.registerInternalDataSetObserver(dataSetChangedObserver);
			
			setSelectionFromTop(positionToSetWhenAdapterIsReady,offsetToSetWhenAdapterIsReady);
		}

		currentHeaderId = null;
		if(frame != null){
			frame.removeHeader();
		}
		updateHeaderVisibilities();
		invalidate();
		
		super.setAdapter(this.adapter);
	}
	
	@Override
	public void setVisibility(int visibility) {
		if(frame != null){
			frame.setVisibility(visibility);
		}
		super.setVisibility(visibility);
	}

	public StickyListHeadersAdapter getWrappedAdapter() {
		if (adapter != null) {
			return adapter.getDelegate();
		}
		return null;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			post(new Runnable() {

				@Override
				public void run() {
					scrollChanged(StickyListHeadersListView.super.getFirstVisiblePosition());
				}
			});
		}
		if (!drawingListUnderStickyHeader) {
			canvas.clipRect(0, Math.max(frame.getHeaderBottomPosition(), 0),
					canvas.getWidth(), canvas.getHeight());
		}
		super.dispatchDraw(canvas);
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
		if (adapter == null || frame == null) {
			return;
		}

		int adapterCount = adapter.getCount();
		if (adapterCount == 0 || !areHeadersSticky) {
			frame.removeHeader();
			return;
		}

		final int listViewHeaderCount = getHeaderViewsCount();
		firstVisibleItem = getFixedFirstVisibleItem(firstVisibleItem)
				- listViewHeaderCount;

		if (firstVisibleItem < 0 || firstVisibleItem > adapterCount - 1) {
			if (currentHeaderId != null || dataChanged) {
				currentHeaderId = null;
				frame.removeHeader();
				updateHeaderVisibilities();
				invalidate();
				dataChanged = false;
			}
			return;
		}

		boolean headerHasChanged = false;
		long newHeaderId = adapter.getHeaderId(firstVisibleItem);
		if (currentHeaderId == null || currentHeaderId != newHeaderId) {
			headerPosition = firstVisibleItem;
			View header = adapter.getHeaderView(headerPosition,
					frame.removeHeader(), frame);
			header.setOnClickListener(this);
			frame.setHeader(header);
			headerHasChanged = true;
		}
		currentHeaderId = newHeaderId;

		int childCount = getChildCount();

		if (childCount > 0) {
			View viewToWatch = null;
			int watchingChildDistance = Integer.MAX_VALUE;
			boolean viewToWatchIsFooter = false;

			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				boolean childIsFooter = footerViews != null
						&& footerViews.contains(child);

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
						|| (!viewToWatchIsFooter && !adapter
								.isHeader(viewToWatch))
						|| ((childIsFooter || adapter.isHeader(child)) && childDistance < watchingChildDistance)) {
					viewToWatch = child;
					viewToWatchIsFooter = childIsFooter;
					watchingChildDistance = childDistance;
				}
			}

			int headerHeight = frame.getHeaderHeight();
			int headerBottomPosition = 0;
			if (viewToWatch != null
					&& (viewToWatchIsFooter || adapter.isHeader(viewToWatch))) {

				if (firstVisibleItem == listViewHeaderCount
						&& getChildAt(0).getTop() > 0 && !clippingToPadding) {
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
			if (frame.getHeaderBottomPosition() != headerBottomPosition
					|| headerHasChanged) {
				frame.setHeaderBottomPosition(headerBottomPosition);
			}
			updateHeaderVisibilities();
		}
	}

	@Override
	public void setSelector(Drawable sel) {
		super.setSelector(sel);
		if (frame != null) {
			frame.setSelector(sel);
		}
	}

	@Override
	public void addFooterView(View v) {
		super.addFooterView(v);
		if (footerViews == null) {
			footerViews = new ArrayList<View>();
		}
		footerViews.add(v);
	}

	@Override
	public boolean removeFooterView(View v) {
		boolean removed = super.removeFooterView(v);
		if (removed) {
			footerViews.remove(v);
		}
		return removed;
	}

	private void updateHeaderVisibilities() {
		int top = clippingToPadding ? getPaddingTop() : 0;
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (adapter.isHeader(child)) {
				if (child.getTop() < top) {
					if (child.getVisibility() != View.INVISIBLE) {
						child.setVisibility(View.INVISIBLE);
					}
				} else {
					if (child.getVisibility() != View.VISIBLE) {
						child.setVisibility(View.VISIBLE);
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

	public void setOnHeaderClickListener(
			OnHeaderClickListener onHeaderClickListener) {
		this.onHeaderClickListener = onHeaderClickListener;
	}

	@Override
	public void onClick(View v) {
		if (frame.isHeader(v)) {
			if (onHeaderClickListener != null) {
				onHeaderClickListener.onHeaderClick(this, v, headerPosition,
						currentHeaderId, true);
			}
		}
	}

	public boolean isDrawingListUnderStickyHeader() {
		return drawingListUnderStickyHeader;
	}

	public void setDrawingListUnderStickyHeader(
			boolean drawingListUnderStickyHeader) {
		this.drawingListUnderStickyHeader = drawingListUnderStickyHeader;
	}
	
	
	/* METHODS THAT NEED POSITION TRANSLATING! */
	


	private void setMultiChoiceModeListenerWrapper() {
		multiChoiceModeListenerWrapper = new MultiChoiceModeListener() {

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				if (multiChoiceModeListenerDelegate != null) {
					return multiChoiceModeListenerDelegate.onPrepareActionMode(
							mode, menu);
				}
				return false;
			}
	
			@Override
			public void onDestroyActionMode(ActionMode mode) {
				if (multiChoiceModeListenerDelegate != null) {
					multiChoiceModeListenerDelegate.onDestroyActionMode(mode);
				}
			}
	
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				if (multiChoiceModeListenerDelegate != null) {
					return multiChoiceModeListenerDelegate.onCreateActionMode(mode,
							menu);
				}
				return false;
			}
	
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				if (multiChoiceModeListenerDelegate != null) {
					return multiChoiceModeListenerDelegate.onActionItemClicked(
							mode, item);
				}
				return false;
			}
	
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked) {
				if (multiChoiceModeListenerDelegate != null) {
					position = adapter.translateListViewPosition(position);
					multiChoiceModeListenerDelegate.onItemCheckedStateChanged(mode,
							position, id, checked);
				}
			}
		};
	}
	@Override
	public boolean performItemClick(View view, int position, long id) {
		OnItemClickListener listener = getOnItemClickListener();
		int headerViewsCount = getHeaderViewsCount();
		final int viewType = adapter.getItemViewType(position
				- headerViewsCount);
		if (viewType == adapter.headerViewType) {
			if (onHeaderClickListener != null) {
				position = adapter.translateListViewPosition(position
						- headerViewsCount);
				onHeaderClickListener.onHeaderClick(this, view, position, id,
						false);
				return true;
			}
			return false;
		} else if (viewType == adapter.dividerViewType) {
			return false;
		} else {
			if (listener != null) {
				if (position >= adapter.getCount()) {
					position -= adapter.getHeaderCount();
				} else if (!(position < headerViewsCount)) {
					position = adapter.translateListViewPosition(position
							- headerViewsCount)
							+ headerViewsCount;
				}
				listener.onItemClick(this, view, position, id);
				return true;
			}
			return false;
		}
	}

	@Override
	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		onItemLongClickListenerDelegate = listener;
		if (listener == null) {
			super.setOnItemLongClickListener(null);
		} else {
			super.setOnItemLongClickListener(onItemLongClickListenerWrapper);
		}
	}

	@Override
	public Object getItemAtPosition(int position) {
		if(isCalledFromSuper()){
			return super.getItemAtPosition(position);
		}else{
			return (adapter == null || position < 0) ? null : adapter.delegate
					.getItem(position);
		}
	}

	@Override
	public long getItemIdAtPosition(int position) {
		if(isCalledFromSuper()){
			return super.getItemIdAtPosition(position);
		}else{
			return (adapter == null || position < 0) ? ListView.INVALID_ROW_ID
					: adapter.delegate.getItemId(position);
		}
	}
	
	@Override
	protected ContextMenuInfo getContextMenuInfo() {
		AdapterContextMenuInfo info = (android.widget.AdapterView.AdapterContextMenuInfo) super.getContextMenuInfo();
		info.position = adapter.translateListViewPosition(info.position - getHeaderViewsCount());
		info.position += getHeaderViewsCount();
		return info;
	}

	private boolean isCalledFromSuper() {
		// i feel dirty...
	    // could not think if better way, need to translate positions when not
	    // called from super
	    StackTraceElement callingFrame = Thread.currentThread().getStackTrace()[5];
	    return callingFrame.getClassName().contains("android.widget.AbsListView") || 
	           callingFrame.getClassName().contains("android.widget.ListView") ||
	           callingFrame.getClassName().contains("android.widget.FastScroller");
	}

	@Override
	public void setItemChecked(int position, boolean value) {
		if (!isCalledFromSuper()) {
			position = adapter.translateAdapterPosition(position);
		}
		// only real items are checkable
		int viewtype = adapter.getItemViewType(position);
		if (viewtype != adapter.dividerViewType
				&& viewtype != adapter.headerViewType) {
			super.setItemChecked(position, value);
		}
	}

	@Override
	public boolean isItemChecked(int position) {
		if (!isCalledFromSuper()) {
			position = adapter.translateAdapterPosition(position);
		}
		return super.isItemChecked(position);
	}

	@Override
	public void setSelectionFromTop(int position, int offset) {
		if (!isCalledFromSuper()) {
			if(adapter == null){
				positionToSetWhenAdapterIsReady = position;
				offsetToSetWhenAdapterIsReady = offset;
				return;
			}
			if (areHeadersSticky) {
				if (frame != null && frame.hasHeader()) {
					offset += frame.getHeaderHeight();
				}
			}
			position = adapter.translateAdapterPosition(position);
		}
		super.setSelectionFromTop(position, offset);
	}
	
	@Override
	public void setSelection(int position) {
		setSelectionFromTop(position, 0);
	}
	
	@Override
	public void smoothScrollToPosition(int position) {
		smoothScrollToPositionFromTop(position, 0);
	}
	
	@Override
	public void smoothScrollToPosition(int position, int boundPosition) {
		//skipping bound position for now as is does not allow an offset
		smoothScrollToPositionFromTop(position, 0);
	}
	
	@Override
	public void smoothScrollToPositionFromTop(int position, int offset) {
		smoothScrollToPositionFromTop(position, offset, 500);
	}
	
	@Override
	public void smoothScrollToPositionFromTop(int position, int offset,
			int duration) {
		if (!isCalledFromSuper()) {
			if(adapter == null){
				positionToSetWhenAdapterIsReady = position;
				offsetToSetWhenAdapterIsReady = offset;
				return;
			}
			if (areHeadersSticky) {
				if (frame != null && frame.hasHeader()) {
					offset += frame.getHeaderHeight();
				}
			}
			position = adapter.translateAdapterPosition(position);
		}
		super.smoothScrollToPositionFromTop(position, offset, duration);
	}
	
	@Override
	public int getFirstVisiblePosition() {
		if (adapter != null && !isCalledFromSuper()) {
			return adapter.translateAdapterPosition(super.getFirstVisiblePosition());
		}
		return super.getFirstVisiblePosition();
	}
	
	@Override
	public int getLastVisiblePosition() {
		if (adapter != null && !isCalledFromSuper()) {
			return adapter.translateAdapterPosition(super.getLastVisiblePosition());
		}
		return super.getLastVisiblePosition();
	}

	@Override
	public int getCheckedItemPosition() {
		int position = super.getCheckedItemPosition();
		if (adapter != null && !isCalledFromSuper() && position != ListView.INVALID_POSITION) {
			position = adapter.translateAdapterPosition(position);
		}
		return position;
	}

	@Override
	public SparseBooleanArray getCheckedItemPositions() {
		SparseBooleanArray superCheckeditems = super.getCheckedItemPositions();
		if (adapter != null && !isCalledFromSuper() && superCheckeditems != null) {
			SparseBooleanArray checkeditems = new SparseBooleanArray(superCheckeditems.size());
			for(int i = 0 ; i<superCheckeditems.size() ; i++){
				int key = adapter.translateListViewPosition(superCheckeditems.keyAt(i));
				boolean value = superCheckeditems.valueAt(i);
				checkeditems.put(key, value);
			}
			return checkeditems;
		}
		return superCheckeditems;
	}

	@Override
	public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
		multiChoiceModeListenerDelegate = listener;
		if (listener == null) {
			super.setMultiChoiceModeListener(null);
		} else {
			super.setMultiChoiceModeListener(multiChoiceModeListenerWrapper);
		}
	}
	

}
