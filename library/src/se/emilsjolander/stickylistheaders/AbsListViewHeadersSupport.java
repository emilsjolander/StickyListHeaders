package se.emilsjolander.stickylistheaders;

import android.view.View;

public interface AbsListViewHeadersSupport {
    public void addHeaderView(View v, Object data, boolean isSelectable);
    public void addHeaderView(View v);
    public int getHeaderViewsCount();
    public boolean removeHeaderView(View v);
    public void addFooterView(View v, Object data, boolean isSelectable);
    public void addFooterView(View v);
    public int getFooterViewsCount();
    public boolean removeFooterView(View v);
    
    public void smoothScrollByOffset(int offset);
    public void setSelectionAfterHeaderView();
    public void setSelectionFromTop(int position, int y);
}
