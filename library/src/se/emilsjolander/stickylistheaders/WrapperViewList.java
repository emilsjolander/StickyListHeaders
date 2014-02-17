package se.emilsjolander.stickylistheaders;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

class WrapperViewList extends ListView {

	interface LifeCycleListener {
		void onDispatchDrawOccurred(Canvas canvas);
	}

	private LifeCycleListener mLifeCycleListener;
	private List<WrapperView> mHeaderWrapperViews;
	private List<WrapperView> mFooterWrapperViews;
	private int mTopClippingLength;
	private Rect mSelectorRect = new Rect();// for if reflection fails
	private Field mSelectorPositionField;
	private boolean mClippingToPadding = true;
	private Drawable mDivider;
	private int mDividerHeight;
	private Rect mDividerTempRect = new Rect();
	private boolean mHeaderDividersEnabled = true;
	private boolean mFooterDividersEnabled = true;


	public WrapperViewList(Context context) {
		super(context);

		// Use reflection to be able to change the size/position of the list
		// selector so it does not come under/over the header
		try {
			Field selectorRectField = AbsListView.class.getDeclaredField("mSelectorRect");
			selectorRectField.setAccessible(true);
			mSelectorRect = (Rect) selectorRectField.get(this);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				mSelectorPositionField = AbsListView.class.getDeclaredField("mSelectorPosition");
				mSelectorPositionField.setAccessible(true);
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean performItemClick(View view, int position, long id) {
		if (view instanceof WrapperView) {
			view = ((WrapperView) view).mItem;
		}
		return super.performItemClick(view, position, id);
	}

	private void positionSelectorRect() {
		if (!mSelectorRect.isEmpty()) {
			int selectorPosition = getSelectorPosition();
			if (selectorPosition >= 0) {
				int firstVisibleItem = getFixedFirstVisibleItem();
				View v = getChildAt(selectorPosition - firstVisibleItem);
				if (v instanceof WrapperView) {
					WrapperView wrapper = ((WrapperView) v);
					mSelectorRect.top = wrapper.getTop() + wrapper.mItemTop;
				}
			}
		}
	}

	private int getSelectorPosition() {
		if (mSelectorPositionField == null) { // not all supported andorid
												// version have this variable
			for (int i = 0; i < getChildCount(); i++) {
				if (getChildAt(i).getBottom() == mSelectorRect.bottom) {
					return i + getFixedFirstVisibleItem();
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

	@Override
	protected void dispatchDraw(Canvas canvas) {
		positionSelectorRect();
		if (mTopClippingLength != 0) {
			canvas.save();
			Rect clipping = canvas.getClipBounds();
			clipping.top = mTopClippingLength;
			canvas.clipRect(clipping);
			super.dispatchDraw(canvas);
			canvas.restore();
		} else {
			super.dispatchDraw(canvas);
		}

		// bottom divider
		if (mFooterDividersEnabled && mDivider != null && getChildCount() > 0) {
			int t = getChildAt(getChildCount() - 1).getBottom();

			mDividerTempRect.set(mDivider.getBounds());

			mDivider.setBounds(getPaddingLeft(), t, getRight() - getLeft() - getPaddingRight(), t + mDividerHeight);
			mDivider.draw(canvas);
			mDivider.setBounds(mDividerTempRect); // restore bounds
		}
		mLifeCycleListener.onDispatchDrawOccurred(canvas);
	}

	void setLifeCycleListener(LifeCycleListener lifeCycleListener) {
		mLifeCycleListener = lifeCycleListener;
	}

	void setListDivider(Drawable divider, int dividerHeight) {
		this.mDivider = divider;
		this.mDividerHeight = dividerHeight;
	}

	@Override
	public void setHeaderDividersEnabled(boolean headerDividersEnabled) {
		super.setHeaderDividersEnabled(headerDividersEnabled);
		this.mHeaderDividersEnabled = headerDividersEnabled;

		updateHeaderViews();
	}

	@Override
	public void setFooterDividersEnabled(boolean footerDividersEnabled) {
		super.setFooterDividersEnabled(footerDividersEnabled);
		this.mFooterDividersEnabled = footerDividersEnabled;

		updateFooterViews();
	}

	@Override
	public void addHeaderView(View v, Object data, boolean isSelectable) {
		if (mHeaderWrapperViews == null) {
			mHeaderWrapperViews = new ArrayList<WrapperView>();
		}

		WrapperView wv = new WrapperView(getContext(), v);
		super.addHeaderView(wv, data, isSelectable);
		mHeaderWrapperViews.add(wv);

		updateHeaderViews();
	}

	@Override
	public boolean removeHeaderView(View v) {
		WrapperView wv = getWrapperViewByItem(mHeaderWrapperViews, v);
		if (wv != null) {
			super.removeHeaderView(wv);
			mHeaderWrapperViews.remove(wv);
			updateHeaderViews();
			return true;
		}
		return false;
	}

	private void updateHeaderViews() {
		for (int i = 0; i < getHeaderViewsCount(); i++) {
			WrapperView wv = mHeaderWrapperViews.get(i);
			if (i == 0 || !mHeaderDividersEnabled) {
				wv.update(wv.getItem(), null, null, 0);
			} else {
				wv.update(wv.getItem(), null, mDivider, mDividerHeight);
			}
		}
	}

	@Override
	public void addFooterView(View v, Object data, boolean isSelectable) {
		if (mFooterWrapperViews == null) {
			mFooterWrapperViews = new ArrayList<WrapperView>();
		}

		WrapperView wv = new WrapperView(getContext(), v);
		super.addFooterView(wv, data, isSelectable);
		mFooterWrapperViews.add(wv);

        updateFooterViews();
	}

	@Override
	public boolean removeFooterView(View v) {
		WrapperView wv = getWrapperViewByItem(mFooterWrapperViews, v);
		if (wv != null) {
			super.removeFooterView(wv);
			mFooterWrapperViews.remove(wv);
			// no need to update dividers
			return true;
		}
		return false;
	}

	private void updateFooterViews() {
		for (int i = 0; i < getFooterViewsCount(); i++) {
			WrapperView wv = mFooterWrapperViews.get(i);
			if (!mFooterDividersEnabled) {
				wv.update(wv.getItem(), null, null, 0);
			} else {
				wv.update(wv.getItem(), null, mDivider, mDividerHeight);
			}
		}
	}

	static WrapperView getWrapperViewByItem(List<WrapperView> wrapperViewList, View item) {
		for (WrapperView wrapperView : wrapperViewList) {
			if (wrapperView.getItem() == item) {
				return wrapperView;
			}
		}
		return null;
	}

	boolean containsFooterView(View v) {
		if (mFooterWrapperViews == null) {
			return false;
		}

		return getWrapperViewByItem(mFooterWrapperViews, v) != null;
	}

	void setTopClippingLength(int topClipping) {
		mTopClippingLength = topClipping;
	}

	int getFixedFirstVisibleItem() {
		int firstVisibleItem = getFirstVisiblePosition();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return firstVisibleItem;
		}

		// first getFirstVisiblePosition() reports items
		// outside the view sometimes on old versions of android
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildAt(i).getBottom() >= 0) {
				firstVisibleItem += i;
				break;
			}
		}

		// work around to fix bug with firstVisibleItem being to high
		// because list view does not take clipToPadding=false into account
		// on old versions of android
		if (!mClippingToPadding && getPaddingTop() > 0 && firstVisibleItem > 0) {
			if (getChildAt(0).getTop() > 0) {
				firstVisibleItem -= 1;
			}
		}

		return firstVisibleItem;
	}

	@Override
	public void setClipToPadding(boolean clipToPadding) {
		mClippingToPadding = clipToPadding;
		super.setClipToPadding(clipToPadding);
	}

}
