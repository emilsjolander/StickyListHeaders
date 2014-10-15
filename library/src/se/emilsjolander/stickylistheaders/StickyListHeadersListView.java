package se.emilsjolander.stickylistheaders;

import android.content.Context;
import android.util.AttributeSet;

public class StickyListHeadersListView extends
        StickyListHeadersListViewAbstract<WrapperViewList> {

    public StickyListHeadersListView(Context context) {
        super(context);
    }
    
    public StickyListHeadersListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StickyListHeadersListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected WrapperViewList createListView(Context context) {
        // TODO Auto-generated method stub
        return new WrapperViewList(context);
    }

}
