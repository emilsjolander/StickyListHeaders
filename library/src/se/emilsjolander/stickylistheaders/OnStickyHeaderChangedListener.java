package se.emilsjolander.stickylistheaders;

import android.view.View;

public interface OnStickyHeaderChangedListener {

    /**
     * @param l             The view parent
     * @param header        The new sticky header view.
     * @param itemPosition  The position of the item within the adapter's data set of
     *                      the item whose header is now sticky.
     * @param headerId      The id of the new sticky header.
     */
    public abstract void onStickyHeaderChanged(
            StickyListHeadersListViewAbstract l, View header, int itemPosition,
            long headerId);

}