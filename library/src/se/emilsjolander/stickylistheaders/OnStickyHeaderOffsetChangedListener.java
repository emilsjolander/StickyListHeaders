package se.emilsjolander.stickylistheaders;

import android.view.View;

public interface OnStickyHeaderOffsetChangedListener {

    /**
     * @param l      The view parent
     * @param header The currently sticky header being offset.
     *               This header is not guaranteed to have it's measurements set.
     *               It is however guaranteed that this view has been measured,
     *               therefor you should user getMeasured* methods instead of
     *               get* methods for determining the view's size.
     * @param offset The amount the sticky header is offset by towards to top of the screen.
     */
    public abstract void onStickyHeaderOffsetChanged(
            StickyListHeadersListViewAbstract l, View header, int offset);

}