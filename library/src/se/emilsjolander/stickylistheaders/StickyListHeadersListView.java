package se.emilsjolander.stickylistheaders;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;

import se.emilsjolander.stickylistheaders.WrapperViewList.LifeCycleListener;

/**
 * Even though this is a FrameLayout subclass we it is called a ListView. This
 * is because of 2 reasons. 1. It acts like as ListView 2. It used to be a
 * ListView subclass and i did not was to change to name causing compatibility
 * errors.
 *
 * @author Emil Sjölander
 */
public class StickyListHeadersListView extends FrameLayout {

    public interface OnHeaderClickListener {
        public void onHeaderClick(StickyListHeadersListView l, View header,
                                  int itemPosition, long headerId, boolean currentlySticky);
    }

    /**
     * Notifies the listener when the sticky headers top offset has changed.
     */
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
        public void onStickyHeaderOffsetChanged(StickyListHeadersListView l, View header, int offset);
    }

    /**
     * Notifies the listener when the sticky header has been updated
     */
    public interface OnStickyHeaderChangedListener {
        /**
         * @param l             The view parent
         * @param header        The new sticky header view.
         * @param itemPosition  The position of the item within the adapter's data set of
         *                      the item whose header is now sticky.
         * @param headerId      The id of the new sticky header.
         */
        public void onStickyHeaderChanged(StickyListHeadersListView l, View header,
                                          int itemPosition, long headerId);

    }

    /* --- Children --- */
    private WrapperViewList mList;
    private View mHeader;

    /* --- Header state --- */
    private Long mHeaderId;
    // used to not have to call getHeaderId() all the time
    private Integer mHeaderPosition;
    private Integer mHeaderOffset;

    /* --- Delegates --- */
    private OnScrollListener mOnScrollListenerDelegate;
    private AdapterWrapper mAdapter;

    /* --- Settings --- */
    private boolean mAreHeadersSticky = true;
    private boolean mClippingToPadding = true;
    private boolean mIsDrawingListUnderStickyHeader = true;
    private int mPaddingLeft = 0;
    private int mPaddingTop = 0;
    private int mPaddingRight = 0;
    private int mPaddingBottom = 0;

    /* --- Other --- */
    private OnHeaderClickListener mOnHeaderClickListener;
    private OnStickyHeaderOffsetChangedListener mOnStickyHeaderOffsetChangedListener;
    private OnStickyHeaderChangedListener mOnStickyHeaderChangedListener;
    private AdapterWrapperDataSetObserver mDataSetObserver;
    private Drawable mDivider;
    private int mDividerHeight;

    public StickyListHeadersListView(Context context) {
        this(context, null);
    }

    public StickyListHeadersListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public StickyListHeadersListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Initialize the wrapped list
        mList = new WrapperViewList(context);

        // null out divider, dividers are handled by adapter so they look good with headers
        mDivider = mList.getDivider();
        mDividerHeight = mList.getDividerHeight();
        mList.setDivider(null);
        mList.setDividerHeight(0);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs,R.styleable.StickyListHeadersListView, 0, 0);

            try {
                // -- View attributes --
                int padding = a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_padding, 0);
                mPaddingLeft = a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_paddingLeft, padding);
                mPaddingTop = a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_paddingTop, padding);
                mPaddingRight = a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_paddingRight, padding);
                mPaddingBottom = a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_paddingBottom, padding);

                setPadding(mPaddingLeft, mPaddingTop, mPaddingRight,
                        mPaddingBottom);

                // Set clip to padding on the list and reset value to default on
                // wrapper
                mClippingToPadding = a.getBoolean(R.styleable.StickyListHeadersListView_android_clipToPadding, true);
                super.setClipToPadding(true);
                mList.setClipToPadding(mClippingToPadding);

                // scrollbars
                final int scrollBars = a.getInt(R.styleable.StickyListHeadersListView_android_scrollbars, 0x00000200);
                mList.setVerticalScrollBarEnabled((scrollBars & 0x00000200) != 0);
                mList.setHorizontalScrollBarEnabled((scrollBars & 0x00000100) != 0);

                // overscroll
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    mList.setOverScrollMode(a.getInt(R.styleable.StickyListHeadersListView_android_overScrollMode, 0));
                }

                // -- ListView attributes --
                mList.setFadingEdgeLength(a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_fadingEdgeLength,
                        mList.getVerticalFadingEdgeLength()));
                final int fadingEdge = a.getInt(R.styleable.StickyListHeadersListView_android_requiresFadingEdge, 0);
                if (fadingEdge == 0x00001000) {
                    mList.setVerticalFadingEdgeEnabled(false);
                    mList.setHorizontalFadingEdgeEnabled(true);
                } else if (fadingEdge == 0x00002000) {
                    mList.setVerticalFadingEdgeEnabled(true);
                    mList.setHorizontalFadingEdgeEnabled(false);
                } else {
                    mList.setVerticalFadingEdgeEnabled(false);
                    mList.setHorizontalFadingEdgeEnabled(false);
                }
                mList.setCacheColorHint(a
                        .getColor(R.styleable.StickyListHeadersListView_android_cacheColorHint, mList.getCacheColorHint()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mList.setChoiceMode(a.getInt(R.styleable.StickyListHeadersListView_android_choiceMode,
                            mList.getChoiceMode()));
                }
                mList.setDrawSelectorOnTop(a.getBoolean(R.styleable.StickyListHeadersListView_android_drawSelectorOnTop, false));
                mList.setFastScrollEnabled(a.getBoolean(R.styleable.StickyListHeadersListView_android_fastScrollEnabled,
                        mList.isFastScrollEnabled()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mList.setFastScrollAlwaysVisible(a.getBoolean(
                            R.styleable.StickyListHeadersListView_android_fastScrollAlwaysVisible,
                            mList.isFastScrollAlwaysVisible()));
                }

                mList.setScrollBarStyle(a.getInt(R.styleable.StickyListHeadersListView_android_scrollbarStyle, 0));

                if (a.hasValue(R.styleable.StickyListHeadersListView_android_listSelector)) {
                    mList.setSelector(a.getDrawable(R.styleable.StickyListHeadersListView_android_listSelector));
                }

                mList.setScrollingCacheEnabled(a.getBoolean(R.styleable.StickyListHeadersListView_android_scrollingCache,
                        mList.isScrollingCacheEnabled()));

                if (a.hasValue(R.styleable.StickyListHeadersListView_android_divider)) {
                    mDivider = a.getDrawable(R.styleable.StickyListHeadersListView_android_divider);
                }

                mDividerHeight = a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_dividerHeight,
                        mDividerHeight);

                // -- StickyListHeaders attributes --
                mAreHeadersSticky = a.getBoolean(R.styleable.StickyListHeadersListView_hasStickyHeaders, true);
                mIsDrawingListUnderStickyHeader = a.getBoolean(
                        R.styleable.StickyListHeadersListView_isDrawingListUnderStickyHeader,
                        true);
            } finally {
                a.recycle();
            }
        }

        // attach some listeners to the wrapped list
        mList.setLifeCycleListener(new WrapperViewListLifeCycleListener());
        mList.setOnScrollListener(new WrapperListScrollListener());

        addView(mList);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureHeader(mHeader);
    }

    private void ensureHeaderHasCorrectLayoutParams(View header) {
        ViewGroup.LayoutParams lp = header.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        } else if (lp.height == LayoutParams.MATCH_PARENT) {
            lp.height = LayoutParams.WRAP_CONTENT;
        }
        header.setLayoutParams(lp);
    }

    private void measureHeader(View header) {
        if (header != null) {
            final int width = getMeasuredWidth() - mPaddingLeft - mPaddingRight;
            final int parentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    width, MeasureSpec.EXACTLY);
            final int parentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
            measureChild(header, parentWidthMeasureSpec,
                    parentHeightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        mList.layout(0, 0, mList.getMeasuredWidth(), getHeight());
        if (mHeader != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mHeader
                    .getLayoutParams();
            int headerTop = lp.topMargin
                    + (mClippingToPadding ? mPaddingTop : 0);
            // The left parameter must for some reason be set to 0.
            // I think it should be set to mPaddingLeft but apparently not
            mHeader.layout(mPaddingLeft, headerTop, mHeader.getMeasuredWidth()
                    + mPaddingLeft, headerTop + mHeader.getMeasuredHeight());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // Only draw the list here.
        // The header should be drawn right after the lists children are drawn.
        // This is done so that the header is above the list items
        // but below the list decorators (scroll bars etc).
        drawChild(canvas, mList, 0);
    }

    // Reset values tied the header. also remove header form layout
    // This is called in response to the data set or the adapter changing
    private void clearHeader() {
        if (mHeader != null) {
            removeView(mHeader);
            mHeader = null;
            mHeaderId = null;
            mHeaderPosition = null;
            mHeaderOffset = null;

            // reset the top clipping length
            mList.setTopClippingLength(0);
            updateHeaderVisibilities();
        }
    }

    private void updateOrClearHeader(int firstVisiblePosition) {
        final int adapterCount = mAdapter == null ? 0 : mAdapter.getCount();
        if (adapterCount == 0 || !mAreHeadersSticky) {
            return;
        }

        final int headerViewCount = mList.getHeaderViewsCount();
        final int realFirstVisibleItem = firstVisiblePosition - headerViewCount;

        // It is not a mistake to call getFirstVisiblePosition() here.
        // Most of the time getFixedFirstVisibleItem() should be called
        // but that does not work great together with getChildAt()
        final boolean doesListHaveChildren = mList.getChildCount() != 0;
        final boolean isFirstViewBelowTop = doesListHaveChildren && mList
                .getFirstVisiblePosition() == 0
                && mList.getChildAt(0).getTop() > (mClippingToPadding ? mPaddingTop : 0);
        final boolean isFirstVisibleItemOutsideAdapterRange = realFirstVisibleItem > adapterCount - 1
                || realFirstVisibleItem < 0;
        if (!doesListHaveChildren || isFirstVisibleItemOutsideAdapterRange
                || isFirstViewBelowTop) {
            clearHeader();
            return;
        }

        updateHeader(realFirstVisibleItem);
    }

    private void updateHeader(int firstVisiblePosition) {

        // check if there is a new header should be sticky
        if (mHeaderPosition == null || mHeaderPosition != firstVisiblePosition) {
            mHeaderPosition = firstVisiblePosition;
            final long headerId = mAdapter.getHeaderId(firstVisiblePosition);
            if (mHeaderId == null || mHeaderId != headerId) {
                mHeaderId = headerId;
                final View header = mAdapter.getHeaderView(mHeaderPosition,
                        mHeader, this);
                if (mHeader != header) {
                    if (header == null) {
                        throw new NullPointerException("header may not be null");
                    }
                    swapHeader(header);
                }
                ensureHeaderHasCorrectLayoutParams(mHeader);
                measureHeader(mHeader);
                if(mOnStickyHeaderChangedListener != null) {
                    mOnStickyHeaderChangedListener.onStickyHeaderChanged(this,
                            mHeader, firstVisiblePosition, mHeaderId);
                }
                // Reset mHeaderOffset to null ensuring
                // that it will be set on the header and
                // not skipped for performance reasons.
                mHeaderOffset = null;
            }
        }

        int headerOffset = 0;

        // Calculate new header offset
        // Skip looking at the first view. it never matters because it always
        // results in a headerOffset = 0
        int headerBottom = mHeader.getMeasuredHeight()
                + (mClippingToPadding ? mPaddingTop : 0);
        for (int i = 0; i < mList.getChildCount(); i++) {
            final View child = mList.getChildAt(i);
            final boolean doesChildHaveHeader = child instanceof WrapperView
                    && ((WrapperView) child).hasHeader();
            final boolean isChildFooter = mList.containsFooterView(child);
            if (child.getTop() >= (mClippingToPadding ? mPaddingTop : 0)
                    && (doesChildHaveHeader || isChildFooter)) {
                headerOffset = Math.min(child.getTop() - headerBottom, 0);
                break;
            }
        }

        setHeaderOffet(headerOffset);

        if (!mIsDrawingListUnderStickyHeader) {
            mList.setTopClippingLength(mHeader.getMeasuredHeight()
                    + mHeaderOffset);
        }

        updateHeaderVisibilities();
    }

    private void swapHeader(View newHeader) {
        if (mHeader != null) {
            removeView(mHeader);
        }
        mHeader = newHeader;
        addView(mHeader);
        mHeader.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnHeaderClickListener != null) {
                    mOnHeaderClickListener.onHeaderClick(
                            StickyListHeadersListView.this, mHeader,
                            mHeaderPosition, mHeaderId, true);
                }
            }

        });
    }

    // hides the headers in the list under the sticky header.
    // Makes sure the other ones are showing
    private void updateHeaderVisibilities() {
        int top;
        if (mHeader != null) {
            top = mHeader.getMeasuredHeight()
                    + (mHeaderOffset != null ? mHeaderOffset : 0);
        } else {
            top = mClippingToPadding ? mPaddingTop : 0;
        }
        int childCount = mList.getChildCount();
        for (int i = 0; i < childCount; i++) {

            // ensure child is a wrapper view
            View child = mList.getChildAt(i);
            if (!(child instanceof WrapperView)) {
                continue;
            }

            // ensure wrapper view child has a header
            WrapperView wrapperViewChild = (WrapperView) child;
            if (!wrapperViewChild.hasHeader()) {
                continue;
            }

            // update header views visibility
            View childHeader = wrapperViewChild.mHeader;
            if (wrapperViewChild.getTop() < top) {
                if (childHeader.getVisibility() != View.INVISIBLE) {
                    childHeader.setVisibility(View.INVISIBLE);
                }
            } else {
                if (childHeader.getVisibility() != View.VISIBLE) {
                    childHeader.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // Wrapper around setting the header offset in different ways depending on
    // the API version
    @SuppressLint("NewApi")
    private void setHeaderOffet(int offset) {
        if (mHeaderOffset == null || mHeaderOffset != offset) {
            mHeaderOffset = offset;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mHeader.setTranslationY(mHeaderOffset);
            } else {
                MarginLayoutParams params = (MarginLayoutParams) mHeader
                        .getLayoutParams();
                params.topMargin = mHeaderOffset;
                mHeader.setLayoutParams(params);
            }
            if (mOnStickyHeaderOffsetChangedListener != null) {
                mOnStickyHeaderOffsetChangedListener
                        .onStickyHeaderOffsetChanged(this, mHeader, -mHeaderOffset);
            }
        }
    }

    private class AdapterWrapperDataSetObserver extends DataSetObserver {

        @Override
        public void onChanged() {
            clearHeader();
        }

        @Override
        public void onInvalidated() {
            clearHeader();
        }

    }

    private class WrapperListScrollListener implements OnScrollListener {

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (mOnScrollListenerDelegate != null) {
                mOnScrollListenerDelegate.onScroll(view, firstVisibleItem,
                        visibleItemCount, totalItemCount);
            }
            updateOrClearHeader(mList.getFixedFirstVisibleItem());
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mOnScrollListenerDelegate != null) {
                mOnScrollListenerDelegate.onScrollStateChanged(view,
                        scrollState);
            }
        }

    }

    private class WrapperViewListLifeCycleListener implements LifeCycleListener {

        @Override
        public void onDispatchDrawOccurred(Canvas canvas) {
            // onScroll is not called often at all before froyo
            // therefor we need to update the header here as well.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                updateOrClearHeader(mList.getFixedFirstVisibleItem());
            }
            if (mHeader != null) {
                if (mClippingToPadding) {
                    canvas.save();
                    canvas.clipRect(0, mPaddingTop, getRight(), getBottom());
                    drawChild(canvas, mHeader, 0);
                    canvas.restore();
                } else {
                    drawChild(canvas, mHeader, 0);
                }
            }
        }

    }

    private class AdapterWrapperHeaderClickHandler implements
            AdapterWrapper.OnHeaderClickListener {

        @Override
        public void onHeaderClick(View header, int itemPosition, long headerId) {
            mOnHeaderClickListener.onHeaderClick(
                    StickyListHeadersListView.this, header, itemPosition,
                    headerId, false);
        }

    }

    private boolean isStartOfSection(int position) {
        return position == 0
                || mAdapter.getHeaderId(position) != mAdapter
                .getHeaderId(position - 1);
    }

    private int getHeaderOverlap(int position) {
        boolean isStartOfSection = isStartOfSection(position);
        if (!isStartOfSection) {
            View header = mAdapter.getHeaderView(position, null, mList);
            if (header == null) {
                throw new NullPointerException("header may not be null");
            }
            ensureHeaderHasCorrectLayoutParams(header);
            measureHeader(header);
            return header.getMeasuredHeight();
        }
        return 0;
    }

	/* ---------- StickyListHeaders specific API ---------- */

    public void setAreHeadersSticky(boolean areHeadersSticky) {
        mAreHeadersSticky = areHeadersSticky;
        if (!areHeadersSticky) {
            clearHeader();
        } else {
            updateOrClearHeader(mList.getFixedFirstVisibleItem());
        }
        // invalidating the list will trigger dispatchDraw()
        mList.invalidate();
    }

    public boolean areHeadersSticky() {
        return mAreHeadersSticky;
    }

    /**
     * Use areHeadersSticky() method instead
     */
    @Deprecated
    public boolean getAreHeadersSticky() {
        return areHeadersSticky();
    }

    public void setDrawingListUnderStickyHeader(
            boolean drawingListUnderStickyHeader) {
        mIsDrawingListUnderStickyHeader = drawingListUnderStickyHeader;
        // reset the top clipping length
        mList.setTopClippingLength(0);
    }

    public boolean isDrawingListUnderStickyHeader() {
        return mIsDrawingListUnderStickyHeader;
    }

    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        mOnHeaderClickListener = listener;
        if (mAdapter != null) {
            if (mOnHeaderClickListener != null) {
                mAdapter.setOnHeaderClickListener(new AdapterWrapperHeaderClickHandler());
            } else {
                mAdapter.setOnHeaderClickListener(null);
            }
        }
    }

    public void setOnStickyHeaderOffsetChangedListener(OnStickyHeaderOffsetChangedListener listener) {
        mOnStickyHeaderOffsetChangedListener = listener;
    }

    public void setOnStickyHeaderChangedListener(OnStickyHeaderChangedListener listener) {
        mOnStickyHeaderChangedListener = listener;
    }

    public View getListChildAt(int index) {
        return mList.getChildAt(index);
    }

    public int getListChildCount() {
        return mList.getChildCount();
    }

    /**
     * Use the method with extreme caution!! Changing any values on the
     * underlying ListView might break everything.
     *
     * @return the ListView backing this view.
     */
    public ListView getWrappedList() {
        return mList;
    }

	/* ---------- ListView delegate methods ---------- */

    public void setAdapter(StickyListHeadersAdapter adapter) {
        if (adapter == null) {
            mList.setAdapter(null);
            clearHeader();
            return;
        }
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        if (adapter instanceof SectionIndexer) {
            mAdapter = new SectionIndexerAdapterWrapper(getContext(), adapter);
        } else {
            mAdapter = new AdapterWrapper(getContext(), adapter);
        }
        mDataSetObserver = new AdapterWrapperDataSetObserver();
        mAdapter.registerDataSetObserver(mDataSetObserver);

        if (mOnHeaderClickListener != null) {
            mAdapter.setOnHeaderClickListener(new AdapterWrapperHeaderClickHandler());
        } else {
            mAdapter.setOnHeaderClickListener(null);
        }

        mAdapter.setDivider(mDivider, mDividerHeight);

        mList.setAdapter(mAdapter);
        clearHeader();
    }

    public StickyListHeadersAdapter getAdapter() {
        return mAdapter == null ? null : mAdapter.mDelegate;
    }

    public void setDivider(Drawable divider) {
        mDivider = divider;
        if (mAdapter != null) {
            mAdapter.setDivider(mDivider, mDividerHeight);
        }
    }

    public void setDividerHeight(int dividerHeight) {
        mDividerHeight = dividerHeight;
        if (mAdapter != null) {
            mAdapter.setDivider(mDivider, mDividerHeight);
        }
    }

    public Drawable getDivider() {
        return mDivider;
    }

    public int getDividerHeight() {
        return mDividerHeight;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListenerDelegate = onScrollListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mList.setOnItemClickListener(listener);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mList.setOnItemLongClickListener(listener);
    }

    public void addHeaderView(View v, Object data, boolean isSelectable) {
        mList.addHeaderView(v, data, isSelectable);
    }

    public void addHeaderView(View v) {
        mList.addHeaderView(v);
    }

    public void removeHeaderView(View v) {
        mList.removeHeaderView(v);
    }

    public int getHeaderViewsCount() {
        return mList.getHeaderViewsCount();
    }

    public void addFooterView(View v) {
        mList.addFooterView(v);
    }

    public void removeFooterView(View v) {
        mList.removeFooterView(v);
    }

    public int getFooterViewsCount() {
        return mList.getFooterViewsCount();
    }

    public void setEmptyView(View v) {
        mList.setEmptyView(v);
    }

    public View getEmptyView() {
        return mList.getEmptyView();
    }

    @Override
    public boolean isVerticalScrollBarEnabled() {
        return mList.isVerticalScrollBarEnabled();
    }

    @Override
    public boolean isHorizontalScrollBarEnabled() {
        return mList.isHorizontalScrollBarEnabled();
    }

    @Override
    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {
        mList.setVerticalScrollBarEnabled(verticalScrollBarEnabled);
    }

    @Override
    public void setHorizontalScrollBarEnabled(boolean horizontalScrollBarEnabled) {
        mList.setHorizontalScrollBarEnabled(horizontalScrollBarEnabled);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public int getOverScrollMode() {
        requireSdkVersion(Build.VERSION_CODES.GINGERBREAD);
        return mList.getOverScrollMode();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void setOverScrollMode(int mode) {
        requireSdkVersion(Build.VERSION_CODES.GINGERBREAD);
        if (mList != null) {
            mList.setOverScrollMode(mode);
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public void smoothScrollBy(int distance, int duration) {
        requireSdkVersion(Build.VERSION_CODES.FROYO);
        mList.smoothScrollBy(distance, duration);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void smoothScrollByOffset(int offset) {
        requireSdkVersion(Build.VERSION_CODES.HONEYCOMB);
        mList.smoothScrollByOffset(offset);
    }

    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.FROYO)
    public void smoothScrollToPosition(int position) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            mList.smoothScrollToPosition(position);
        } else {
            int offset = mAdapter == null ? 0 : getHeaderOverlap(position);
            offset -= mClippingToPadding ? 0 : mPaddingTop;
            mList.smoothScrollToPositionFromTop(position, offset);
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public void smoothScrollToPosition(int position, int boundPosition) {
        requireSdkVersion(Build.VERSION_CODES.FROYO);
        mList.smoothScrollToPosition(position, boundPosition);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void smoothScrollToPositionFromTop(int position, int offset) {
        requireSdkVersion(Build.VERSION_CODES.HONEYCOMB);
        offset += mAdapter == null ? 0 : getHeaderOverlap(position);
        offset -= mClippingToPadding ? 0 : mPaddingTop;
        mList.smoothScrollToPositionFromTop(position, offset);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void smoothScrollToPositionFromTop(int position, int offset,
                                              int duration) {
        requireSdkVersion(Build.VERSION_CODES.HONEYCOMB);
        offset += mAdapter == null ? 0 : getHeaderOverlap(position);
        offset -= mClippingToPadding ? 0 : mPaddingTop;
        mList.smoothScrollToPositionFromTop(position, offset, duration);
    }

    public void setSelection(int position) {
        setSelectionFromTop(position, 0);
    }

    public void setSelectionAfterHeaderView() {
        mList.setSelectionAfterHeaderView();
    }

    public void setSelectionFromTop(int position, int y) {
        y += mAdapter == null ? 0 : getHeaderOverlap(position);
        y -= mClippingToPadding ? 0 : mPaddingTop;
        mList.setSelectionFromTop(position, y);
    }

    public void setSelector(Drawable sel) {
        mList.setSelector(sel);
    }

    public void setSelector(int resID) {
        mList.setSelector(resID);
    }

    public int getFirstVisiblePosition() {
        return mList.getFirstVisiblePosition();
    }

    public int getLastVisiblePosition() {
        return mList.getLastVisiblePosition();
    }

    public void setChoiceMode(int choiceMode) {
        mList.setChoiceMode(choiceMode);
    }

    public void setItemChecked(int position, boolean value) {
        mList.setItemChecked(position, value);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public int getCheckedItemCount() {
        requireSdkVersion(Build.VERSION_CODES.HONEYCOMB);
        return mList.getCheckedItemCount();
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public long[] getCheckedItemIds() {
        requireSdkVersion(Build.VERSION_CODES.FROYO);
        return mList.getCheckedItemIds();
    }

    public int getCheckedItemPosition() {
        return mList.getCheckedItemPosition();
    }

    public SparseBooleanArray getCheckedItemPositions() {
        return mList.getCheckedItemPositions();
    }

    public int getCount() {
        return mList.getCount();
    }

    public Object getItemAtPosition(int position) {
        return mList.getItemAtPosition(position);
    }

    public long getItemIdAtPosition(int position) {
        return mList.getItemIdAtPosition(position);
    }

    @Override
    public void setOnCreateContextMenuListener(OnCreateContextMenuListener l) {
        mList.setOnCreateContextMenuListener(l);
    }

    @Override
    public boolean showContextMenu() {
        return mList.showContextMenu();
    }

    public void invalidateViews() {
        mList.invalidateViews();
    }

    @Override
    public void setClipToPadding(boolean clipToPadding) {
        if (mList != null) {
            mList.setClipToPadding(clipToPadding);
        }
        mClippingToPadding = clipToPadding;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        mPaddingLeft = left;
        mPaddingTop = top;
        mPaddingRight = right;
        mPaddingBottom = bottom;

        if (mList != null) {
            mList.setPadding(left, top, right, bottom);
        }
        super.setPadding(0, 0, 0, 0);
        requestLayout();
    }

    /*
     * Overrides an @hide method in View
     */
    protected void recomputePadding() {
        setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
    }

    @Override
    public int getPaddingLeft() {
        return mPaddingLeft;
    }

    @Override
    public int getPaddingTop() {
        return mPaddingTop;
    }

    @Override
    public int getPaddingRight() {
        return mPaddingRight;
    }

    @Override
    public int getPaddingBottom() {
        return mPaddingBottom;
    }

    public void setFastScrollEnabled(boolean fastScrollEnabled) {
        mList.setFastScrollEnabled(fastScrollEnabled);
    }

    /**
     * @throws ApiLevelTooLowException on pre-Honeycomb device.
     * @see android.widget.AbsListView#setFastScrollAlwaysVisible(boolean)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setFastScrollAlwaysVisible(boolean alwaysVisible) {
        requireSdkVersion(Build.VERSION_CODES.HONEYCOMB);
        mList.setFastScrollAlwaysVisible(alwaysVisible);
    }

    /**
     * @return true if the fast scroller will always show. False on pre-Honeycomb devices.
     * @see android.widget.AbsListView#isFastScrollAlwaysVisible()
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean isFastScrollAlwaysVisible() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return false;
        }
        return mList.isFastScrollAlwaysVisible();
    }

    public void setScrollBarStyle(int style) {
        mList.setScrollBarStyle(style);
    }

    public int getScrollBarStyle() {
        return mList.getScrollBarStyle();
    }

    private void requireSdkVersion(int versionCode) {
        if (Build.VERSION.SDK_INT < versionCode) {
            throw new ApiLevelTooLowException(versionCode);
        }
    }

    public int getPositionForView(View view) {
        return mList.getPositionForView(view);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
        requireSdkVersion(Build.VERSION_CODES.HONEYCOMB);
        mList.setMultiChoiceModeListener(listener);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (superState != BaseSavedState.EMPTY_STATE) {
          throw new IllegalStateException("Handling non empty state of parent class is not implemented");
        }
        return mList.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE);
        mList.onRestoreInstanceState(state);
    }
}
