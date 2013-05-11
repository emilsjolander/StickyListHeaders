package com.emilsjolander.components.stickylistheaders;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.annotation.SuppressLint;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;

/**
 * @author Emil Sjölander
 */
public class StickyListHeadersListView extends ListView {

	public interface OnHeaderClickListener {
		public void onHeaderClick(StickyListHeadersListView l, View header,
				int itemPosition, long headerId, boolean currentlySticky);
	}

	private OnScrollListener mOnScrollListenerDelegate;
	private boolean mAreHeadersSticky = true;
	private int mHeaderBottomPosition;
	private View mHeader;
	private int mDividerHeight;
	private Drawable mDivider;
	private Boolean mClippingToPadding;
	private final Rect mClippingRect = new Rect();
	private Long mCurrentHeaderId = null;
	private AdapterWrapper mAdapter;
	private float mHeaderDownY = -1;
	private boolean mHeaderBeingPressed = false;
	private OnHeaderClickListener mOnHeaderClickListener;
	private int mHeaderPosition;
	private ViewConfiguration mViewConfig;
	private ArrayList<View> mFooterViews;
	private boolean mDrawingListUnderStickyHeader = false;
	private Rect mSelectorRect = new Rect();// for if reflection fails
	private Field mSelectorPositionField;

	private AdapterWrapper.OnHeaderClickListener mAdapterHeaderClickListener = new AdapterWrapper.OnHeaderClickListener() {

		@Override
		public void onHeaderClick(View header, int itemPosition, long headerId) {
			if (mOnHeaderClickListener != null) {
				mOnHeaderClickListener.onHeaderClick(
						StickyListHeadersListView.this, header, itemPosition,
						headerId, false);
			}
		}
	};

	private DataSetObserver mDataSetChangedObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			reset();
		}

		@Override
		public void onInvalidated() {
			reset();
		}
	};

	private OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (mOnScrollListenerDelegate != null) {
				mOnScrollListenerDelegate.onScrollStateChanged(view,
						scrollState);
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (mOnScrollListenerDelegate != null) {
				mOnScrollListenerDelegate.onScroll(view, firstVisibleItem,
						visibleItemCount, totalItemCount);
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
				scrollChanged(firstVisibleItem);
			}
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

		super.setOnScrollListener(mOnScrollListener);
		// null out divider, dividers are handled by adapter so they look good
		// with headers
		super.setDivider(null);
		super.setDividerHeight(0);
		mViewConfig = ViewConfiguration.get(context);
		if (mClippingToPadding == null) {
			mClippingToPadding = true;
		}

		try {
			Field selectorRectField = AbsListView.class
					.getDeclaredField("mSelectorRect");
			selectorRectField.setAccessible(true);
			mSelectorRect = (Rect) selectorRectField.get(this);

			mSelectorPositionField = AbsListView.class
					.getDeclaredField("mSelectorPosition");
			mSelectorPositionField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			reset();
			scrollChanged(getFirstVisiblePosition());
		}
	}

	private void reset() {
		mHeader = null;
		mCurrentHeaderId = null;
		mHeaderBottomPosition = -1;
	}

	@Override
	public boolean performItemClick(View view, int position, long id) {
		if (view instanceof WrapperView) {
			view = ((WrapperView) view).mItem;
		}
		return super.performItemClick(view, position, id);
	}

	@Override
	public void setDivider(Drawable divider) {
		this.mDivider = divider;
		if (divider != null) {
			int dividerDrawableHeight = divider.getIntrinsicHeight();
			if (dividerDrawableHeight >= 0) {
				setDividerHeight(dividerDrawableHeight);
			}
		}
		if (mAdapter != null) {
			mAdapter.setDivider(divider);
			requestLayout();
			invalidate();
		}
	}

	@Override
	public void setDividerHeight(int height) {
		mDividerHeight = height;
		if (mAdapter != null) {
			mAdapter.setDividerHeight(height);
			requestLayout();
			invalidate();
		}
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mOnScrollListenerDelegate = l;
	}

	public void setAreHeadersSticky(boolean areHeadersSticky) {
		if (this.mAreHeadersSticky != areHeadersSticky) {
			this.mAreHeadersSticky = areHeadersSticky;
			requestLayout();
		}
	}

	public boolean getAreHeadersSticky() {
		return mAreHeadersSticky;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (this.isInEditMode()) {
			super.setAdapter(adapter);
			return;
		}
		if (adapter == null) {
			mAdapter = null;
			reset();
			super.setAdapter(null);
			return;
		}
		if (!(adapter instanceof StickyListHeadersAdapter)) {
			throw new IllegalArgumentException(
					"Adapter must implement StickyListHeadersAdapter");
		}
		mAdapter = wrapAdapter(adapter);
		reset();
		super.setAdapter(this.mAdapter);
	}

	private AdapterWrapper wrapAdapter(ListAdapter adapter) {
		AdapterWrapper wrapper;
		if (adapter instanceof SectionIndexer) {
			wrapper = new SectionIndexerAdapterWrapper(getContext(),
					(StickyListHeadersAdapter) adapter);
		} else {
			wrapper = new AdapterWrapper(getContext(),
					(StickyListHeadersAdapter) adapter);
		}
		wrapper.setDivider(mDivider);
		wrapper.setDividerHeight(mDividerHeight);
		wrapper.registerDataSetObserver(mDataSetChangedObserver);
		wrapper.setOnHeaderClickListener(mAdapterHeaderClickListener);
		return wrapper;
	}

	public StickyListHeadersAdapter getWrappedAdapter() {
		return mAdapter == null ? null : mAdapter.mDelegate;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			scrollChanged(getFirstVisiblePosition());
		}
		positionSelectorRect();
		if (!mAreHeadersSticky || mHeader == null) {
			super.dispatchDraw(canvas);
			return;
		}

		if (!mDrawingListUnderStickyHeader) {
			mClippingRect
					.set(0, mHeaderBottomPosition, getWidth(), getHeight());
			canvas.save();
			canvas.clipRect(mClippingRect);
		}

		super.dispatchDraw(canvas);

		if (!mDrawingListUnderStickyHeader) {
			canvas.restore();
		}

		drawStickyHeader(canvas);
	}

	private void positionSelectorRect() {
		if (!mSelectorRect.isEmpty()) {
			int selectorPosition = getSelectorPosition();
			if (selectorPosition >= 0) {
				int firstVisibleItem = fixedFirstVisibleItem(getFirstVisiblePosition());
				View v = getChildAt(selectorPosition - firstVisibleItem);
				if (v instanceof WrapperView) {
					WrapperView wrapper = ((WrapperView) v);
					mSelectorRect.top = wrapper.getTop() + wrapper.mItemTop;
				}
			}
		}
	}

	private int getSelectorPosition() {
		if (mSelectorPositionField == null) { //not all supported andorid version have this variable
			for (int i = 0; i < getChildCount(); i++) {
				if (getChildAt(i).getBottom() == mSelectorRect.bottom) {
					return i + fixedFirstVisibleItem(getFirstVisiblePosition());
				}
			}
		} else {
			try {
				return mSelectorPositionField.getInt(this);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	private void drawStickyHeader(Canvas canvas) {
		int headerHeight = getHeaderHeight();
		int top = mHeaderBottomPosition - headerHeight;
		// clip the headers drawing region
		mClippingRect.left = getPaddingLeft();
		mClippingRect.right = getWidth() - getPaddingRight();
		mClippingRect.bottom = top + headerHeight;
		mClippingRect.top = mClippingToPadding ? getPaddingTop() : 0;

		canvas.save();
		canvas.clipRect(mClippingRect);
		canvas.translate(getPaddingLeft(), top);
		mHeader.draw(canvas);
		canvas.restore();
	}

	private void measureHeader() {
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(),
				MeasureSpec.EXACTLY);
		int heightMeasureSpec = 0;

		ViewGroup.LayoutParams params = mHeader.getLayoutParams();
		if (params != null && params.height > 0) {
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(params.height,
					MeasureSpec.EXACTLY);
		} else {
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		mHeader.measure(widthMeasureSpec, heightMeasureSpec);
		mHeader.layout(getLeft() + getPaddingLeft(), 0, getRight()
				- getPaddingRight(), mHeader.getMeasuredHeight());
	}

	private int getHeaderHeight() {
		return mHeader == null ? 0 : mHeader.getMeasuredHeight();
	}

	@Override
	public void setClipToPadding(boolean clipToPadding) {
		super.setClipToPadding(clipToPadding);
		mClippingToPadding = clipToPadding;
	}

	private void scrollChanged(int reportedFirstVisibleItem) {

		int adapterCount = mAdapter == null ? 0 : mAdapter.getCount();
		if (adapterCount == 0 || !mAreHeadersSticky) {
			return;
		}

		final int listViewHeaderCount = getHeaderViewsCount();
		final int firstVisibleItem = fixedFirstVisibleItem(reportedFirstVisibleItem)
				- listViewHeaderCount;

		if (firstVisibleItem < 0 || firstVisibleItem > adapterCount - 1) {
			reset();
			updateHeaderVisibilities();
			invalidate();
			return;
		}

		long newHeaderId = mAdapter.getHeaderId(firstVisibleItem);
		if (mCurrentHeaderId == null || mCurrentHeaderId != newHeaderId) {
			mHeaderPosition = firstVisibleItem;
			mCurrentHeaderId = newHeaderId;
			mHeader = mAdapter.getHeaderView(mHeaderPosition, mHeader, this);
			measureHeader();
		}

		int childCount = getChildCount();
		if (childCount != 0) {
			View viewToWatch = null;
			int watchingChildDistance = Integer.MAX_VALUE;
			boolean viewToWatchIsFooter = false;

			for (int i = 0; i < childCount; i++) {
				final View child = super.getChildAt(i);
				final boolean childIsFooter = mFooterViews != null
						&& mFooterViews.contains(child);

				final int childDistance = child.getTop()
						- (mClippingToPadding ? getPaddingTop() : 0);
				if (childDistance < 0) {
					continue;
				}

				if (viewToWatch == null
						|| (!viewToWatchIsFooter && !((WrapperView) viewToWatch)
								.hasHeader())
						|| ((childIsFooter || ((WrapperView) child).hasHeader()) && childDistance < watchingChildDistance)) {
					viewToWatch = child;
					viewToWatchIsFooter = childIsFooter;
					watchingChildDistance = childDistance;
				}
			}

			final int headerHeight = getHeaderHeight();
			if (viewToWatch != null
					&& (viewToWatchIsFooter || ((WrapperView) viewToWatch)
							.hasHeader())) {
				if (firstVisibleItem == listViewHeaderCount
						&& super.getChildAt(0).getTop() > 0
						&& !mClippingToPadding) {
					mHeaderBottomPosition = 0;
				} else {
					final int paddingTop = mClippingToPadding ? getPaddingTop()
							: 0;
					mHeaderBottomPosition = Math.min(viewToWatch.getTop(),
							headerHeight + paddingTop);
					mHeaderBottomPosition = mHeaderBottomPosition < paddingTop ? headerHeight
							+ paddingTop
							: mHeaderBottomPosition;
				}
			} else {
				mHeaderBottomPosition = headerHeight
						+ (mClippingToPadding ? getPaddingTop() : 0);
			}
		}
		updateHeaderVisibilities();
		invalidate();
	}

	@Override
	public void addFooterView(View v) {
		super.addFooterView(v);
		if (mFooterViews == null) {
			mFooterViews = new ArrayList<View>();
		}
		mFooterViews.add(v);
	}

	@Override
	public boolean removeFooterView(View v) {
		if (super.removeFooterView(v)) {
			mFooterViews.remove(v);
			return true;
		}
		return false;
	}

	private void updateHeaderVisibilities() {
		int top = mClippingToPadding ? getPaddingTop() : 0;
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = super.getChildAt(i);
			if (child instanceof WrapperView) {
				WrapperView wrapperViewChild = (WrapperView) child;
				if (wrapperViewChild.hasHeader()) {
					View childHeader = wrapperViewChild.mHeader;
					if (wrapperViewChild.getTop() < top) {
						childHeader.setVisibility(View.INVISIBLE);
					} else {
						childHeader.setVisibility(View.VISIBLE);
					}
				}
			}
		}
	}

	private int fixedFirstVisibleItem(int firstVisibleItem) {
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
		if (!mClippingToPadding && getPaddingTop() > 0) {
			if (super.getChildAt(0).getTop() > 0) {
				if (firstVisibleItem > 0) {
					firstVisibleItem -= 1;
				}
			}
		}
		return firstVisibleItem;
	}

	@Override
	public void setSelectionFromTop(int position, int y) {
		if (mAreHeadersSticky) {
			y += getHeaderHeight();
		}
		super.setSelectionFromTop(position, y);
	}

	@SuppressLint("NewApi")
	@Override
	public void smoothScrollToPositionFromTop(int position, int offset) {
		if (mAreHeadersSticky) {
			offset += getHeaderHeight();
		}
		super.smoothScrollToPositionFromTop(position, offset);
	}

	@SuppressLint("NewApi")
	@Override
	public void smoothScrollToPositionFromTop(int position, int offset,
			int duration) {
		if (mAreHeadersSticky) {
			offset += getHeaderHeight();
		}
		super.smoothScrollToPositionFromTop(position, offset, duration);
	}

	public void setOnHeaderClickListener(
			OnHeaderClickListener onHeaderClickListener) {
		this.mOnHeaderClickListener = onHeaderClickListener;
	}

	public void setDrawingListUnderStickyHeader(
			boolean drawingListUnderStickyHeader) {
		mDrawingListUnderStickyHeader = drawingListUnderStickyHeader;
	}

	public boolean isDrawingListUnderStickyHeader() {
		return mDrawingListUnderStickyHeader;
	}

	// TODO handle touches better, multitouch etc.
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		if (action == MotionEvent.ACTION_DOWN
				&& ev.getY() <= mHeaderBottomPosition) {
			mHeaderDownY = ev.getY();
			mHeaderBeingPressed = true;
			mHeader.setPressed(true);
			mHeader.invalidate();
			invalidate(0, 0, getWidth(), mHeaderBottomPosition);
			return true;
		}
		if (mHeaderBeingPressed) {
			if (Math.abs(ev.getY() - mHeaderDownY) < mViewConfig
					.getScaledTouchSlop()) {
				if (action == MotionEvent.ACTION_UP
						|| action == MotionEvent.ACTION_CANCEL) {
					mHeaderDownY = -1;
					mHeaderBeingPressed = false;
					mHeader.setPressed(false);
					mHeader.invalidate();
					invalidate(0, 0, getWidth(), mHeaderBottomPosition);
					if (mOnHeaderClickListener != null) {
						mOnHeaderClickListener.onHeaderClick(this, mHeader,
								mHeaderPosition, mCurrentHeaderId, true);
					}
				}
				return true;
			} else {
				mHeaderDownY = -1;
				mHeaderBeingPressed = false;
				mHeader.setPressed(false);
				mHeader.invalidate();
				invalidate(0, 0, getWidth(), mHeaderBottomPosition);
			}
		}
		return super.onTouchEvent(ev);
	}

}
