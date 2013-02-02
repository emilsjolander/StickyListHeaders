package com.emilsjolander.components.stickylistheaders;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.ListAdapter;

/**
 * A {@link ListAdapter} which wraps a {@link StickyListHeadersAdapter} and
 * automatically handles wrapping the result of
 * {@link StickyListHeadersAdapter#getView(int, android.view.View, android.view.ViewGroup)}
 * and
 * {@link StickyListHeadersAdapter#getHeaderView(int, android.view.View, android.view.ViewGroup)}
 * appropriately.
 *
 * @author Jake Wharton (jakewharton@gmail.com)
 */
final class StickyListHeadersAdapterWrapper extends BaseAdapter {

	public interface OnHeaderClickListener{
		public void onHeaderClick(View header, int itemPosition, long headerId);
	}

	private final List<View> headerCache = new ArrayList<View>();
	private final Context context;
	final StickyListHeadersAdapter delegate;
	private Drawable divider;
	private int dividerHeight;
	private DataSetObserver dataSetObserver = new DataSetObserver() {

		@Override
		public void onInvalidated() {
			headerCache.clear();
		}
	};
	private OnHeaderClickListener onHeaderClickListener;

	StickyListHeadersAdapterWrapper(Context context,
			StickyListHeadersAdapter delegate) {
		this.context = context;
		this.delegate = delegate;
		delegate.registerDataSetObserver(dataSetObserver);
	}

	void setDivider(Drawable divider) {
		this.divider = divider;
	}

	void setDividerHeight(int dividerHeight) {
		this.dividerHeight = dividerHeight;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return delegate.areAllItemsEnabled();
	}

	@Override
	public boolean isEnabled(int position) {
		return delegate.isEnabled(position);
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		delegate.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		delegate.unregisterDataSetObserver(observer);
	}

	@Override
	public int getCount() {
		return delegate.getCount();
	}

	@Override
	public Object getItem(int position) {
		return delegate.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return delegate.getItemId(position);
	}

	@Override
	public boolean hasStableIds() {
		return delegate.hasStableIds();
	}

	@Override
	public int getItemViewType(int position) {
		return delegate.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return delegate.getViewTypeCount();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	/**
	 * Will recycle header from {@link WrapperView} if it exists
	 */
	private void recycleHeaderIfExists(WrapperView wv) {
		View header = wv.header;
		if (header != null) {
			headerCache.add(header);
		}
	}

	/**
	 * Get a header view. This optionally pulls a header from the supplied
	 * {@link WrapperView} and will also recycle the divider if it exists.
	 */
	private View configureHeader(WrapperView wv, final int position) {
		View header = wv.header;
		header = delegate.getHeaderView(position, header, wv);
		if (header == null) {
			throw new NullPointerException("Header view must not be null.");
		}
		//if the header isn't clickable, the listselector will be drawn on top of the header
		header.setClickable(true);
		header.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(onHeaderClickListener != null){
					long headerId = delegate.getHeaderId(position);
					onHeaderClickListener.onHeaderClick(v, position, headerId);
				}
			}
		});
		return header;
	}

	/** Returns {@code true} if the previous position has the same header ID. */
	private boolean previousPositionHasSameHeader(int position) {
		return position != 0
				&& delegate.getHeaderId(position) == delegate
						.getHeaderId(position - 1);
	}

	@Override
	public WrapperView getView(int position, View convertView, ViewGroup parent) {
		WrapperView wv = (convertView == null) ? new WrapperView(context) : (WrapperView) convertView;
		View item = delegate.getView(position, wv.item, wv);
		View header = null;
		if (previousPositionHasSameHeader(position)) {
			recycleHeaderIfExists(wv);
		} else {
			header = configureHeader(wv, position);
		}
		if((item instanceof Checkable) && !(wv instanceof CheckableWrapperView)) {
			// Need to create Checkable subclass of WrapperView for ListView to work correctly
			wv = new CheckableWrapperView(context);
		} else if(!(item instanceof Checkable) && (wv instanceof CheckableWrapperView)) {
			wv = new WrapperView(context);
		}
		wv.update(item, header, divider, dividerHeight);
		return wv;
	}

	public void setOnHeaderClickListener(OnHeaderClickListener onHeaderClickListener){
		this.onHeaderClickListener = onHeaderClickListener;
	}

}
