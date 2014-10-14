package se.emilsjolander.stickylistheaders;

import android.view.View;

public interface OnHeaderClickListener {

    public abstract void onHeaderClick(StickyListHeadersListViewAbstract l,
            View header, int itemPosition, long headerId,
            boolean currentlySticky);

}