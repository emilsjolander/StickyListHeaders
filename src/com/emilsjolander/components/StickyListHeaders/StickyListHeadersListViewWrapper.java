package com.emilsjolander.components.StickyListHeaders;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;

public class StickyListHeadersListViewWrapper extends FrameLayout implements OnScrollListener {
	
	private static final String HEADER_HEIGHT = "headerHeight";
	private static final String SUPER_INSTANCE_STATE = "superInstanceState";
	
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
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		headerHeight = ((Bundle)state).getInt(HEADER_HEIGHT);
		super.onRestoreInstanceState(((Bundle)state).getParcelable(SUPER_INSTANCE_STATE));
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle instanceState = new Bundle();
		instanceState.putInt(HEADER_HEIGHT, headerHeight);
		instanceState.putParcelable(SUPER_INSTANCE_STATE, super.onSaveInstanceState());
		return instanceState;
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
		list.setId(R.id.list_view);
		addView(list);
	}
	
	public ListView getWrappedList(){
		return list;
	}
	
	public void setWrappedList(ListView list){
		list.setOnScrollListener(this);
		this.list = list;
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
				if(headerHeight<0) headerHeight=viewToWatch.findViewById(R.id.header_view).getHeight();
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
			params.gravity = 0;
			header.setLayoutParams(params);
			header.setVisibility(View.VISIBLE);
		}else{
			if(header != null){
				header.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(onScrollListener!=null){
			onScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

}
