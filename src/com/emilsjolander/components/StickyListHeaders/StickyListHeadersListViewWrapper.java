package com.emilsjolander.components.StickyListHeaders;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;

public class StickyListHeadersListViewWrapper extends FrameLayout implements OnScrollListener {
	
	private boolean areHeadersSticky;
	private int headerBottomPosition;
	private int headerHeight = -1;
	private View header;
	private ListView list;
	private OnScrollListener onScrollListener;

	public StickyListHeadersListViewWrapper(Context context) {
		super(context);
		list = new ListView(context);
		setAreHeadersSticky(true);
		setup();
	}

	public StickyListHeadersListViewWrapper(Context context, AttributeSet attrs) {
		super(context, attrs);
		list = new ListView(context, attrs);
		TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.StickyListHeadersListView);
		setAreHeadersSticky(a.getBoolean(0, true));
		a.recycle();
		setup();
	}

	public StickyListHeadersListViewWrapper(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		list = new ListView(context, attrs, defStyle);
		TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.StickyListHeadersListView);
		setAreHeadersSticky(a.getBoolean(0, true));
		a.recycle();
		setup();
	}

	private void setup() {
		list.setOnScrollListener(this);
		addView(list);
	}
	
	public ListView getWrappedList(){
		return list;
	}
	
	public void setWrappedList(ListView list){
		this.list = list;
		list.setOnScrollListener(this);
	}
	
	/**
	 * Use this method instead of getWrappedList().setOnScrollListener(OnScrollListener onScrollListener), using that will break this class.
	 */
	public void setOnScrollListener(OnScrollListener onScrollListener){
		this.onScrollListener = onScrollListener;
	}

	public void setAreHeadersSticky(boolean areHeadersSticky) {
		this.areHeadersSticky = areHeadersSticky;
	}

	public boolean areHeadersSticky() {
		return areHeadersSticky;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if(onScrollListener!=null){
			onScrollListener.onScroll(view,firstVisibleItem,visibleItemCount,totalItemCount);
		}
		if(list.getAdapter()==null) return;
		if(!(list.getAdapter() instanceof StickyListHeadersAdapter)){
			throw new IllegalArgumentException("Adapter must be a subclass of StickyListHeadersAdapter");
		}
		StickyListHeadersAdapter stickyListHeadersAdapter = (StickyListHeadersAdapter) list.getAdapter();
		if(areHeadersSticky){
			View viewToWatch = stickyListHeadersAdapter.getCurrentlyVissibleHeaderViews().get(firstVisibleItem);
			if(viewToWatch==null){
				viewToWatch = stickyListHeadersAdapter.getCurrentlyVissibleHeaderViews().get(firstVisibleItem+1);
			}
			if(viewToWatch!=null){
				if(headerHeight<0) headerHeight=viewToWatch.findViewById(StickyListHeadersAdapter.HEADER_ID).getHeight();
				headerBottomPosition = Math.min(viewToWatch.getTop(), headerHeight);
				headerBottomPosition = headerBottomPosition<0 ? headerHeight : headerBottomPosition;
			}else{
				headerBottomPosition = headerHeight;
			}

			header = stickyListHeadersAdapter.getHeaderView(firstVisibleItem, header);
			if(getChildCount()>1){
				removeViewAt(1);
			}
			addView(header);
			LayoutParams params = (LayoutParams) header.getLayoutParams();
			params.height=headerHeight;
			params.topMargin = headerBottomPosition - headerHeight;
			header.setLayoutParams(params);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(onScrollListener!=null){
			onScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

}
