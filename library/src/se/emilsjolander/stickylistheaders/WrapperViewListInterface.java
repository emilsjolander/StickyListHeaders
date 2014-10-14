package se.emilsjolander.stickylistheaders;

import android.view.View;

public interface WrapperViewListInterface {
    void setLifeCycleListener(WrapperListViewLifeCycleListener lifeCycleListener);
    void setTopClippingLength(int topClipping);
    boolean containsFooterView(View v);
    int getFixedFirstVisibleItem();
    public void setBlockLayoutChildren(boolean block);
}
