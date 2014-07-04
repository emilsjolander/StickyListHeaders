package se.emilsjolander.stickylistheaders;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * @author lsjwzh
 */
public class ExpandableStickyListHeadersAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private final StickyListHeadersAdapter mInnerAdapter;
    DualHashMap<View,Long> mViewToItemMap = new DualHashMap<View, Long>();
    DistinctMultiHashMap<Integer,View> mGroupIdToViewMap = new DistinctMultiHashMap<Integer, View>();
    List<Long> mCollapseGroupIds = new ArrayList<Long>();

    public ExpandableStickyListHeadersAdapter(StickyListHeadersAdapter innerAdapter){
        this.mInnerAdapter = innerAdapter;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        return mInnerAdapter.getHeaderView(position,convertView,parent);
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
        View convertView = mInnerAdapter.getView(i,view,viewGroup);
        mViewToItemMap.put(convertView,getItemId(i));
        mGroupIdToViewMap.add((int) getHeaderId(i),convertView);
        if(mCollapseGroupIds.contains(getHeaderId(i))){
            convertView.setVisibility(View.GONE);
        }else {
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

    public List<View> getItemViewsByGroup(long groupId){
        return mGroupIdToViewMap.get((int) groupId);
    }

    public boolean isGroupCollapsed(long groupId){
        return mCollapseGroupIds.contains(groupId);
    }

    public void expand(long groupId) {
        if(isGroupCollapsed(groupId)){
            mCollapseGroupIds.remove((Object)groupId);
        }
    }

    public void collapse(long groupId) {
        if(!isGroupCollapsed(groupId)){
            mCollapseGroupIds.add(groupId);
        }
    }

    public View findViewByItemId(long itemId){
         return mViewToItemMap.getKey(itemId);
    }

    public long findItemIdByView(View view){
        return mViewToItemMap.get(view);
    }
}
