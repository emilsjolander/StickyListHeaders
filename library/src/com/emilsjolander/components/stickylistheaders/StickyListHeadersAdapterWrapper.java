package com.emilsjolander.components.stickylistheaders;

import java.util.WeakHashMap;

import android.content.Context;
import android.database.DataSetObserver;
import android.database.DataSetObservable;
import android.graphics.drawable.Drawable;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
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
class StickyListHeadersAdapterWrapper extends BaseAdapter implements
		StickyListHeadersAdapter {

	static final int VIEW_TYPE_DIVIDER_OFFSET = 1;
	static final int VIEW_TYPE_HEADER_OFFSET = 0;
	private static final int EXTRA_VIEW_TYPE_COUNT = 2;
	private static final int HEADER_POSITION = -1;
	private static final int DIVIDER_POSITION = -2;

	private final Context context;
	final StickyListHeadersAdapter delegate;
	private Drawable divider;
	private int dividerHeight;
	private WeakHashMap<View, Void> headers = new WeakHashMap<View, Void>();
	private SparseIntArray positionMapping = new SparseIntArray();
	int dividerViewType;
	int headerViewType;
	private int headerCount;
	private int dividerCount;
	private int cachedCount = -1;

	private DataSetObservable internalObservable = new DataSetObservable();
	private DataSetObservable regularObservable = new DataSetObservable();

	private DataSetObserver datasetObserver = new DataSetObserver() {
		public void onChanged() {
			cachedCount = -1;
			internalObservable.notifyChanged();
			regularObservable.notifyChanged();
		};

		public void onInvalidated() {
			cachedCount = -1;
			internalObservable.notifyInvalidated();
			regularObservable.notifyInvalidated();
		};
	};

	StickyListHeadersAdapterWrapper(Context context,
			StickyListHeadersAdapter delegate) {
		this.context = context;
		this.delegate = delegate;
		delegate.registerDataSetObserver(datasetObserver);
	}

	void setDivider(Drawable divider) {
		this.divider = divider;
	}

	void setDividerHeight(int dividerHeight) {
		this.dividerHeight = dividerHeight;
	}

	boolean isHeader(View v) {
		return headers.containsKey(v);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		int viewType = getItemViewType(position);
		if (viewType == headerViewType) {
			return true;
		} else if (viewType == dividerViewType) {
			return false;
		}
		position = translateListViewPosition(position);
		return delegate.areAllItemsEnabled() || delegate.isEnabled(position);
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		regularObservable.registerObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		regularObservable.unregisterObserver(observer);
	}

	void registerInternalDataSetObserver(DataSetObserver observer) {
		internalObservable.registerObserver(observer);
	}

	void unregisterInternalDataSetObserver(DataSetObserver observer) {
		internalObservable.unregisterObserver(observer);
	}

	@Override
	public int getCount() {
		// cache the count as it is expensive to count the headers
		if (cachedCount < 0) {
			positionMapping.clear();
			countHeadersAndUpdatePositionMapping();
			cachedCount = delegate.getCount() + headerCount + dividerCount;
		}
		return cachedCount;
	}

	private void countHeadersAndUpdatePositionMapping() {
		int headerCount = 0;
		int dividerCount = 0;
		int itemCount = delegate.getCount();
		if (itemCount > 0) {
			headerCount++;
			long lastHeaderId = delegate.getHeaderId(0);
			positionMapping.put(0, HEADER_POSITION);
			positionMapping.put(1, 0);
			for (int i = 1; i < itemCount; i++) {
				long headerId = delegate.getHeaderId(i);
				if (lastHeaderId != headerId) {
					lastHeaderId = headerId;
					positionMapping.put(i + headerCount + dividerCount,
							HEADER_POSITION);
					headerCount++;
				} else {
					positionMapping.put(i + headerCount + dividerCount,
							DIVIDER_POSITION);
					dividerCount++;
				}
				positionMapping.put(i + headerCount + dividerCount, i);
			}
		}
		this.dividerCount = dividerCount;
		this.headerCount = headerCount;
	}

	int getHeaderCount() {
		return headerCount;
	}

	@Override
	public Object getItem(int position) {
		int viewType = getItemViewType(position);
		if (viewType == headerViewType || viewType == dividerViewType) {
			return null;
		}
		position = translateListViewPosition(position);
		return delegate.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		if (getItemViewType(position) == headerViewType) {
			position = translateListViewPosition(position);
			return delegate.getHeaderId(position);
		}
		position = translateListViewPosition(position);
		return delegate.getItemId(position);
	}

	@Override
	public boolean hasStableIds() {
		return delegate.hasStableIds();
	}

	int translateAdapterPosition(int position) {
		return positionMapping.indexOfValue(position);
	}

	int translateListViewPosition(int position) {
		int viewType = getItemViewType(position);
		if (viewType == headerViewType) {
			return positionMapping.get(position + 1);
		} else if (viewType == dividerViewType) {
			return positionMapping.get(position - 1);
		} else {
			return positionMapping.get(position);
		}
	}

	@Override
	public int getItemViewType(int position) {

		position = positionMapping.get(position);

		if (position == HEADER_POSITION) {
			return headerViewType;
		}

		if (position == DIVIDER_POSITION) {
			return dividerViewType;
		}

		return delegate.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		headerViewType = delegate.getViewTypeCount() + VIEW_TYPE_HEADER_OFFSET;
		dividerViewType = delegate.getViewTypeCount()
				+ VIEW_TYPE_DIVIDER_OFFSET;
		return delegate.getViewTypeCount() + EXTRA_VIEW_TYPE_COUNT;
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int viewType = getItemViewType(position);

		if (viewType == headerViewType) {
			headers.remove(convertView);
			convertView = delegate.getHeaderView(
					translateListViewPosition(position), convertView, parent);
			headers.put(convertView, null);
		} else if (viewType == dividerViewType) {
			if (convertView == null) {
				convertView = makeDivider();
			}
			return convertView;
		} else {
			convertView = delegate.getView(translateListViewPosition(position),
					convertView, parent);
		}
		return convertView;
	}

	@SuppressWarnings("deprecation")
	private View makeDivider() {
		View v = new View(context);
		v.setBackgroundDrawable(divider);
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				AbsListView.LayoutParams.MATCH_PARENT, dividerHeight);
		v.setLayoutParams(params);
		return v;
	}

	@Override
	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		if (getItemViewType(position) == headerViewType) {
			return null;
		}
		position = translateListViewPosition(position);
		return ((BaseAdapter) delegate).getDropDownView(position, convertView,
				parent);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public void notifyDataSetChanged() {
		((BaseAdapter) delegate).notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetInvalidated() {
		((BaseAdapter) delegate).notifyDataSetInvalidated();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		return delegate.getHeaderView(translateListViewPosition(position),
				convertView, parent);
	}

	@Override
	public long getHeaderId(int position) {
		return delegate.getHeaderId(translateListViewPosition(position));
	}

	StickyListHeadersAdapter getDelegate() {
		return delegate;
	}

}
