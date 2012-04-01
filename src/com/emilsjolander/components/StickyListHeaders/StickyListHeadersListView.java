package com.emilsjolander.components.StickyListHeaders;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
/**
 * 
 * @author Emil Sjšlander
 * 
 * 
Copyright 2012 Emil Sjšlander

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 *
 */
public class StickyListHeadersListView extends ListView implements OnScrollListener {
	
	private static final String HEADER_HEIGHT = "headerHeight";
	private static final String SUPER_INSTANCE_STATE = "superInstanceState";
	
	private OnScrollListener scrollListener;
	private boolean areHeadersSticky;
	private int headerBottomPosition;
	private int headerHeight = -1;
	private View header;
	private int dividerHeight;
	private Drawable divider;
	private boolean clippingToPadding;
	private boolean clipToPaddingHasBeenSet;

	public StickyListHeadersListView(Context context) {
		super(context);
		setup();
	}

	public StickyListHeadersListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.StickyListHeadersListView);
		setAreHeadersSticky(a.getBoolean(0, true));
		a.recycle();
		setup();
	}

	public StickyListHeadersListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.StickyListHeadersListView);
		setAreHeadersSticky(a.getBoolean(0, true));
		a.recycle();
		setup();
	}
	
	private void setup() {
		super.setOnScrollListener(this);
		setDivider(getDivider());
		setDividerHeight(getDividerHeight());
		super.setDivider(null);
		super.setDividerHeight(0);
	}
	
	@Override
	public void onRestoreInstanceState(Parcelable state) {
		headerHeight = ((Bundle)state).getInt(HEADER_HEIGHT);
		super.onRestoreInstanceState(((Bundle)state).getParcelable(SUPER_INSTANCE_STATE));
	}
	
	@Override
	public Parcelable onSaveInstanceState() {
		Bundle instanceState = new Bundle();
		instanceState.putInt(HEADER_HEIGHT, headerHeight);
		instanceState.putParcelable(SUPER_INSTANCE_STATE, super.onSaveInstanceState());
		return instanceState;
	}
	
	@Override
	public void setDivider(Drawable divider) {
		this.divider = divider;
		if(getAdapter()!=null){
			((StickyListHeadersAdapter)getAdapter()).setDivider(divider);
		}
	}
	
	@Override
	public void setDividerHeight(int height) {
		dividerHeight = height;
		if(getAdapter()!=null){
			((StickyListHeadersAdapter)getAdapter()).setDividerHeight(height);
		}
	}
	
	@Override
	public void setOnScrollListener(OnScrollListener l) {
		scrollListener = l;
	}
	
	public void setAreHeadersSticky(boolean areHeadersSticky) {
		this.areHeadersSticky = areHeadersSticky;
	}

	public boolean areHeadersSticky() {
		return areHeadersSticky;
	}
	
	@Override
	public void setAdapter(ListAdapter adapter) {
		if(!clipToPaddingHasBeenSet){
			clippingToPadding = true;
		}
		
		if(!(adapter instanceof StickyListHeadersAdapter)) throw new IllegalArgumentException("Adapter must be a subclass of StickyListHeadersAdapter");
		((StickyListHeadersAdapter)adapter).setDivider(divider);
		((StickyListHeadersAdapter)adapter).setDividerHeight(dividerHeight);
		super.setAdapter(adapter);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if(header != null && areHeadersSticky){
			header.setDrawingCacheEnabled(true);  
			
			int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST);
			int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			header.measure(widthMeasureSpec, heightMeasureSpec);
			header.layout(getLeft()+getPaddingLeft(), 0, getRight()-getPaddingRight(), headerHeight);
			
			header.buildDrawingCache();
			Bitmap drawingCache = header.getDrawingCache();
			if(drawingCache != null){
				int top = headerBottomPosition - headerHeight;
				if(clippingToPadding && getPaddingTop()>0){
					int rowsToDelete = -1*(top-getPaddingTop());
					drawingCache.setPixels(new int[rowsToDelete * drawingCache.getWidth()], 0, drawingCache.getWidth(), 0, 0, drawingCache.getWidth(), rowsToDelete);
				}
				canvas.drawBitmap(drawingCache, getPaddingLeft(), top, null);
			}
			header.setDrawingCacheEnabled(false); 
		}
	}
	
	@Override
	public void setClipToPadding(boolean clipToPadding) {
		super.setClipToPadding(clipToPadding);
		clippingToPadding  = clipToPadding;
		clipToPaddingHasBeenSet = true;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if(scrollListener!=null){
			scrollListener.onScroll(view,firstVisibleItem,visibleItemCount,totalItemCount);
		}
		if(getAdapter()==null) return;
		if(areHeadersSticky){
			if(getChildCount()!=0){
				View viewToWatch = getChildAt(0);
				if(!((Boolean)viewToWatch.getTag()) && getChildCount()>1){
					viewToWatch = getChildAt(1);
				}
				if((Boolean)viewToWatch.getTag()){
					if(headerHeight<0) headerHeight=viewToWatch.findViewById(R.id.header_view).getHeight();
					
					if(firstVisibleItem == 0 && viewToWatch.getTop()>0 && !clippingToPadding){
						headerBottomPosition = 0;
					}else{
						if(clippingToPadding){
							headerBottomPosition = Math.min(viewToWatch.getTop(), headerHeight+getPaddingTop());
							headerBottomPosition = headerBottomPosition<getPaddingTop() ? headerHeight+getPaddingTop() : headerBottomPosition;
						}else{
							headerBottomPosition = Math.min(viewToWatch.getTop(), headerHeight);
							headerBottomPosition = headerBottomPosition<0 ? headerHeight : headerBottomPosition;
						}
					}
				}else{
					headerBottomPosition = headerHeight;
					if(clippingToPadding){
						headerBottomPosition += getPaddingTop();
					}
				}
			}

			header = ((StickyListHeadersAdapter)getAdapter()).getHeaderView(firstVisibleItem, header);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(scrollListener!=null){
			scrollListener.onScrollStateChanged(view, scrollState);
		}
	}

}
