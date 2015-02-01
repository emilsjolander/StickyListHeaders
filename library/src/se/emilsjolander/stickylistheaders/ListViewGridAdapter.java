package se.emilsjolander.stickylistheaders;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;



public class ListViewGridAdapter
        extends BaseAdapter
        implements SectionIndexer, StickyListHeadersAdapter
//        implements View.OnClickListener,
//        View.OnLongClickListener 
        {
    private final static String LAYOUT_TAG = "ListViewGridAdapter_LinearLayout_TAG";
    private interface PoolObjectFactory<T extends View> {
        public T createObject();
    }
    
    private class LinearLayoutPoolObjectFactory implements PoolObjectFactory<LinearLayout>{

        private final Context context;

        public LinearLayoutPoolObjectFactory(final Context context) {
            this.context = context;
        }

        @Override
        public LinearLayout createObject() {
            LinearLayout result = new LinearLayout(context);
            result.setTag(LAYOUT_TAG);
            return result;
        }
    }
    
    static private class ViewPool<T extends View> implements Parcelable {

        Stack<T> stack = new Stack<>();
        PoolObjectFactory<T> factory = null;

        ViewPool(PoolObjectFactory<T> factory) {
            this.factory = factory;
        }

        T get() {
            if (stack.size() > 0) {
                return stack.pop();
            }
            T object = factory != null ? factory.createObject() : null;
            return object;
        }

        void put(T object) {
            if (object != null) {
                stack.push(object);
            }
        }

        void clear() {
            stack.clear();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {

        }
    }

    private AbsListView listView;
    
    final BaseAdapter mRealAdapter;
    
    
    protected int requestedHorizontalSpacing;
    protected int requestedColumnWidth;
    protected int requestedColumnCount;
    
    private int actualNumColumns = 0;
    private int actualTotalCount = 0;
    private int actualRowCount = 0;
    private final boolean mIsSectionAdapter;
    private List<Integer> rowsStartIndexes = null;
    private final ViewPool<LinearLayout> linearLayoutPool;
    Stack<View> realAdapterConvertViewPool = new Stack<>();

    
    private DataSetObserver mDataSetObserver = new DataSetObserver() {

        @Override
        public void onInvalidated() {
            linearLayoutPool.clear();
            realAdapterConvertViewPool.clear();
            ListViewGridAdapter.super.notifyDataSetInvalidated();
        }
        
        @Override
        public void onChanged() {
            ListViewGridAdapter.super.notifyDataSetChanged();
        }
    };


    public ListViewGridAdapter(final AbsListView listView,
                                     BaseAdapter realAdapter) {

        this.linearLayoutPool = new ViewPool<>(new LinearLayoutPoolObjectFactory(listView.getContext()));
        this.listView = listView;
//        listView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
//            
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right,
//                    int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                if (oldRight - oldLeft != right - left && requestedColumnWidth > 0) {
//                    notifyDataSetChanged();
//                }
//                
//            }
//        });
        
        this.mRealAdapter = realAdapter;
        mIsSectionAdapter = (mRealAdapter instanceof SectionIndexer);
        if (mIsSectionAdapter) {
            //only needed for the SectionIndexer
            rowsStartIndexes = new ArrayList<>();
        }
        realAdapter.registerDataSetObserver(mDataSetObserver);
    }
    
    public void setColumnWidth(final int width) {
        if (width != requestedColumnWidth) {
            requestedColumnCount = -1 ;
            requestedColumnWidth = width;
            notifyDataSetChanged();
        }
        
    }

    public void setNumColumns(int requestedColumnCount) {
        if (this.requestedColumnCount != requestedColumnCount) {
            requestedColumnWidth = 0;
            this.requestedColumnCount = requestedColumnCount;
            notifyDataSetChanged();
        }
    }

    public int getRequestedHorizontalSpacing() {
        return requestedHorizontalSpacing;
    }

    public void setHorizontalSpacing(int spacing) {
        requestedHorizontalSpacing = spacing;
    }
    
    public int getNumColumns() {
        return requestedColumnCount;
    }

    public int getColumnWidth() {
        return requestedColumnWidth;
    }

    public int getAvailableSpace() {
        return this.listView.getMeasuredWidth() - this.listView.getPaddingLeft() - this.listView.getPaddingRight();
    }
    
    public int determineColumns() {
        int numColumns = 0;
        final int availableSpace = getAvailableSpace();

        if (requestedColumnWidth > 0) {
            numColumns = (availableSpace + requestedHorizontalSpacing) /
                    (requestedColumnWidth + requestedHorizontalSpacing);
        } else if (requestedColumnCount > 0) {
            numColumns = requestedColumnCount;
        }

        this.actualNumColumns = numColumns;

        return numColumns;
    }

    
    
    @Override
    public Object getItem(int position) {
        return mRealAdapter.getItem(getSectionIndex(position));
    }

    @Override
    public long getItemId(int position) {
        return mRealAdapter.getItemId(getSectionIndex(position));
    }
    
    @Override
    public int getItemViewType(int position) {
        return mRealAdapter.getItemViewType(getSectionIndex(position));
    }
    
    @Override
    public boolean areAllItemsEnabled() {
        return mRealAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return mRealAdapter.isEnabled(getSectionIndex(position));
    }

    @Override
    public boolean hasStableIds() {
        return mRealAdapter.hasStableIds();
    }


    @Override
    public int getViewTypeCount() {
        return mRealAdapter.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return mRealAdapter.isEmpty();
    }
    
    
    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        //position is the row position
        int startIndex = 0;
        int stopIndex = actualTotalCount;
        LinearLayout layout = findOrInitializeLayout(convertView);
        if (mIsSectionAdapter) {
            startIndex = rowsStartIndexes.get(position);
            if (position < rowsStartIndexes.size() -1) {
                stopIndex = rowsStartIndexes.get(position + 1);
            }
        }
        else {
            startIndex = position * actualNumColumns;
        }
        
        for (int i = 0; i < actualNumColumns; i++) {
            final LinearLayout childLayout = findOrInitializeChildLayout(layout, i);
            View childConvertView = childLayout.getChildAt(0);
            
            if (i + startIndex < stopIndex) {
                if (childConvertView == null && realAdapterConvertViewPool.size() > 0) {
                    childConvertView = realAdapterConvertViewPool.pop();
                    childLayout.addView(childConvertView);
                }
                final View v = mRealAdapter.getView(i + startIndex, childConvertView, parent);
                if (v != childConvertView) {
                    childLayout.addView(v);
                    if (childConvertView != null) {
                        realAdapterConvertViewPool.add(childConvertView);
                        childLayout.removeView(childConvertView);
                    }
                    childConvertView = v;
                }
                if (childConvertView != null) { 
                    childLayout.setGravity(Gravity.CENTER);
                    if (requestedColumnWidth > 0) {
                        childConvertView.getLayoutParams().width = requestedColumnWidth;
                    } else if (requestedColumnCount > 0) {
                        childConvertView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                }
                
            } else if (childConvertView != null) {
                realAdapterConvertViewPool.add(childConvertView);
                childLayout.removeView(childConvertView);
            }

        }
        
        return layout;
    }




    private LinearLayout findOrInitializeLayout(final View convertView) {
        LinearLayout layout;

        if (convertView == null || !(convertView instanceof LinearLayout)) {
            layout = new LinearLayout(listView.getContext(), null);

            layout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

            layout.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                    AbsListView.LayoutParams.WRAP_CONTENT));
        } else
            layout = (LinearLayout) convertView;
            int currentCount = layout.getChildCount();
            if (currentCount > actualNumColumns) {
                for (int i = currentCount - 1; i >= actualNumColumns; i--) {
                    LinearLayout tempChild = (LinearLayout) layout.getChildAt(i);
                    linearLayoutPool.put(tempChild);
                    layout.removeView(tempChild);
                }
            }

        return layout;
    }

    private LinearLayout findOrInitializeChildLayout(final LinearLayout parentLayout, final int childIndex) {
        LinearLayout childLayout = (LinearLayout) parentLayout.getChildAt(childIndex);

        if (childLayout == null) {
            childLayout = linearLayoutPool.get();
            childLayout.setLayoutParams(new LinearLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                    AbsListView.LayoutParams.WRAP_CONTENT, 1));
            parentLayout.addView(childLayout);
        }

        return childLayout;
    }
    
    

    @Override
    public int getCount() {
        return actualRowCount;
    }

    public int getRowCount() {
        if (mIsSectionAdapter) {
            return rowsStartIndexes.size();
        }
        return (int) Math.ceil(actualTotalCount / (float)actualNumColumns);
    }

    public void recalculateItemsPerRow() {
        rowsStartIndexes.clear();
        calculateItemsPerRow();
    }

    private void calculateItemsPerRow() {
            Object [] sections = ((SectionIndexer)mRealAdapter).getSections();
            int sectionsCount = sections.length;
            int startSection = 0;
            int endSection = 0;
            for (int i = 0; i < sectionsCount; i++) {
                startSection = ((SectionIndexer)mRealAdapter).getPositionForSection(i);
                endSection = (i < (sectionsCount - 1))?((SectionIndexer)mRealAdapter).getPositionForSection(i+1):actualTotalCount;
                int sectionCount = endSection - startSection;
                int sectionRowCount = (int) Math.ceil(sectionCount / (float)actualNumColumns);
                for (int j = 0; j < sectionRowCount; j++) {
                    rowsStartIndexes.add(startSection + j*actualNumColumns);
                }
            }

    }

    @Override
    public Object[] getSections() {
        if (mIsSectionAdapter) {
            return ((SectionIndexer)mRealAdapter).getSections();
        }
        return null;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (mIsSectionAdapter) {
            return ((SectionIndexer)mRealAdapter).getPositionForSection(sectionIndex);
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        if (mIsSectionAdapter) {
            return ((SectionIndexer)mRealAdapter).getSectionForPosition(position);
        }
        return 0;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        return ((StickyListHeadersAdapter)mRealAdapter).getHeaderView(getSectionIndex(position), convertView, parent);
    }
    
    private int getSectionIndex(int position) {
        if (!mIsSectionAdapter) {
            return position * actualNumColumns;
        }
        try {
            return rowsStartIndexes.get(position);
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }

    @Override
    public long getHeaderId(int position) {
        if (mIsSectionAdapter) {
            return ((StickyListHeadersAdapter)mRealAdapter).getHeaderId(getSectionIndex(position));
        } else {
            return ((StickyListHeadersAdapter)mRealAdapter).getHeaderId(position*actualNumColumns);
        }
    }
    
    public boolean updateNumColumns() {
        if (listView.getMeasuredWidth() != 0 && requestedColumnCount > 0 || 
                requestedColumnWidth > 0) {
            int oldNum = actualNumColumns;
            if (determineColumns() != oldNum) {
                if (mIsSectionAdapter) {
                    recalculateItemsPerRow();
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void notifyDataSetChanged() {
        actualTotalCount = mRealAdapter.getCount();
        actualRowCount = 0;
        updateNumColumns();
        if (actualNumColumns > 0) {
            actualRowCount = getRowCount();
            mRealAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetInvalidated() {
        mRealAdapter.notifyDataSetInvalidated();
    }

}