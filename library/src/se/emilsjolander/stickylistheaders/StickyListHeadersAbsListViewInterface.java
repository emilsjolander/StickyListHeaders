package se.emilsjolander.stickylistheaders;

import android.view.View;

public interface StickyListHeadersAbsListViewInterface {
    public void setOnStickyHeaderOffsetChangedListener(OnStickyHeaderOffsetChangedListener listener);
    public void setOnStickyHeaderChangedListener(OnStickyHeaderChangedListener listener);
    public View getListChildAt(int index);
    public int getListChildCount();
}
