package se.emilsjolander.stickylistheaders;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author lsjwzh
 */
class ExpandableStickyListHeadersAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private final StickyListHeadersAdapter mInnerAdapter;
    DualHashMap<View, Long> mViewToItemIdMap = new DualHashMap<View, Long>();
    DistinctMultiHashMap<Long, View> mHeaderIdToViewMap = new DistinctMultiHashMap<Long, View>();
    Map<Long, Boolean> mCollapseHeaderIds = new HashMap<Long, Boolean>();
    private boolean mStartCollapsed;

    ExpandableStickyListHeadersAdapter(StickyListHeadersAdapter innerAdapter, boolean startCollapsed) {
        this.mInnerAdapter = innerAdapter;
        this.mStartCollapsed = startCollapsed;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        return mInnerAdapter.getHeaderView(position, convertView, parent);
    }

    @Override
    public long getHeaderId(int position) {
        return mInnerAdapter.getHeaderId(position);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mInnerAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int i) {
        return mInnerAdapter.isEnabled(i);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        mInnerAdapter.registerDataSetObserver(dataSetObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        mInnerAdapter.unregisterDataSetObserver(dataSetObserver);
    }

    @Override
    public int getCount() {
        return mInnerAdapter.getCount();
    }

    @Override
    public Object getItem(int i) {
        return mInnerAdapter.getItem(i);
    }

    @Override
    public long getItemId(int i) {
        return mInnerAdapter.getItemId(i);
    }

    @Override
    public boolean hasStableIds() {
        return mInnerAdapter.hasStableIds();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View convertView = mInnerAdapter.getView(i, view, viewGroup);
        mViewToItemIdMap.put(convertView, getItemId(i));
        final long headerId = getHeaderId(i);
        mHeaderIdToViewMap.add(headerId, convertView);
        if (!mCollapseHeaderIds.containsKey(headerId)) {
            mCollapseHeaderIds.put(headerId, mStartCollapsed);
        }
        if (mCollapseHeaderIds.get(headerId)) {
            convertView.setVisibility(View.GONE);
        } else {
            convertView.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int i) {
        return mInnerAdapter.getItemViewType(i);
    }

    @Override
    public int getViewTypeCount() {
        return mInnerAdapter.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return mInnerAdapter.isEmpty();
    }

    public List<View> getItemViewsByHeaderId(long headerId) {
        return mHeaderIdToViewMap.get(headerId);
    }

    public boolean isHeaderCollapsed(long headerId) {
        return mCollapseHeaderIds.containsKey(headerId) && mCollapseHeaderIds.get(headerId);
    }

    public void expand(long headerId) {
        if (isHeaderCollapsed(headerId)) {
            mCollapseHeaderIds.put(headerId, false);
        }
    }

    public void collapse(long headerId) {
        if (!isHeaderCollapsed(headerId)) {
            mCollapseHeaderIds.put(headerId, true);
        }
    }

    public View findViewByItemId(long itemId) {
        return mViewToItemIdMap.getKey(itemId);
    }

    public long findItemIdByView(View view) {
        return mViewToItemIdMap.get(view);
    }
}
