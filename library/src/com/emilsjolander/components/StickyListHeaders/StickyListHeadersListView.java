package com.emilsjolander.components.StickyListHeaders;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
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
	private long oldHeaderId = -1;
	private boolean headerHasChanged = true;
	private boolean setupDone;
	private Rect clippingRect = new Rect();

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
		if(!setupDone){
			setupDone = true;
			super.setOnScrollListener(this);
			setDivider(getDivider());
			setDividerHeight(getDividerHeight());
			//null out divider, dividers are handled by adapter so they look good with headers
			super.setDivider(null);
			super.setDividerHeight(0);
			setVerticalFadingEdgeEnabled(false);
		}
	}
	
	private void reset()
	{
	    headerBottomPosition = 0;
	    headerHeight = -1;
	    header = null;
	    oldHeaderId = -1;
	    headerHasChanged = true;
	}
	
	@Override
	public void onRestoreInstanceState(Parcelable state) {
		headerHeight = ((Bundle)state).getInt(HEADER_HEIGHT);
		headerHasChanged = true;
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
	public boolean performItemClick(View view, int position, long id)
	{
		view = view.findViewById(R.id.__stickylistheaders_list_item_view);
	    return super.performItemClick(view, position, id);
	}
	
	/**
	 * can only be set to false if headers are sticky, not compatible with fading edges
	 */
	@Override
	public void setVerticalFadingEdgeEnabled(boolean verticalFadingEdgeEnabled) {
		if(areHeadersSticky){
			super.setVerticalFadingEdgeEnabled(false);
		}else{
			super.setVerticalFadingEdgeEnabled(verticalFadingEdgeEnabled);
		}
	}
	
	@Override
	public void setDivider(Drawable divider) {
		if(setupDone){
			this.divider = divider;
			if(getAdapter()!=null){
				((StickyListHeadersAdapter)getAdapter()).setDivider(divider);
			}
		}else{
			super.setDivider(divider);
		}
	}
	
	@Override
	public void setDividerHeight(int height) {
		if(setupDone){
			dividerHeight = height;
			if(getAdapter()!=null){
				((StickyListHeadersAdapter)getAdapter()).setDividerHeight(height);
			}
		}else{
			super.setDividerHeight(height);
		}
	}
	
	@Override
	public void setOnScrollListener(OnScrollListener l) {
		scrollListener = l;
	}
	
	public void setAreHeadersSticky(boolean areHeadersSticky) {
		if(areHeadersSticky){
			super.setVerticalFadingEdgeEnabled(false);
		}
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
		reset();
		super.setAdapter(adapter);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if(header != null && areHeadersSticky){
			if(headerHasChanged){
				int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
				int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
				header.measure(widthMeasureSpec, heightMeasureSpec);
				header.layout(getLeft()+getPaddingLeft(), 0, getRight()-getPaddingRight(), headerHeight);
				headerHasChanged = false;
			}
			int top = headerBottomPosition - headerHeight;
			clippingRect.left = getPaddingLeft();
			clippingRect.right = getWidth()-getPaddingRight();
			clippingRect.bottom = top+headerHeight;
			if(clippingToPadding){
				clippingRect.top = getPaddingTop();
			}else{
				clippingRect.top = 0;
			}
			
			canvas.save();
			canvas.clipRect(clippingRect);
			canvas.translate(getPaddingLeft(), top);
			header.draw(canvas);
			canvas.restore();
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
		if(getAdapter()==null || getAdapter().getCount() == 0) return;
		if(areHeadersSticky){
			if(getChildCount()!=0){
				
				View viewToWatch = super.getChildAt(0);
				for(int i = 1;i<getChildCount();i++){
					
					int firstChildDistance;
					if(clippingToPadding){
						firstChildDistance = Math.abs((viewToWatch.getTop() - getPaddingTop()));
					}else{
						firstChildDistance = Math.abs(viewToWatch.getTop());
					}
					
					int secondChildDistance;
					if(clippingToPadding){
						secondChildDistance = Math.abs((super.getChildAt(i).getTop() - getPaddingTop()) - headerHeight);
					}else{
						secondChildDistance = Math.abs(super.getChildAt(i).getTop() - headerHeight);
					}
					
					if(!(Boolean)viewToWatch.getTag() || ((Boolean)super.getChildAt(i).getTag() && secondChildDistance<firstChildDistance)){
						viewToWatch = super.getChildAt(i);
					}
				}
				
				if((Boolean)viewToWatch.getTag()){
					if(headerHeight<0) headerHeight=viewToWatch.findViewById(R.id.__stickylistheaders_header_view).getHeight();
					
					if(firstVisibleItem == 0 && super.getChildAt(0).getTop()>0 && !clippingToPadding){
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
			if(Build.VERSION.SDK_INT < 11){//work around to fix bug with firstVisibleItem being to high because listview does not take clipToPadding=false into account
				if(!clippingToPadding && getPaddingTop()>0){
					if(super.getChildAt(0).getTop() > 0){
						if(firstVisibleItem>0) firstVisibleItem -= 1;
					}
				}
			}
			if(oldHeaderId != ((StickyListHeadersAdapter)getAdapter()).getHeaderId(firstVisibleItem)){
				headerHasChanged = true;
				header = ((StickyListHeadersAdapter)getAdapter()).getHeaderView(firstVisibleItem, header);
				header.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, headerHeight));
			}
			oldHeaderId = ((StickyListHeadersAdapter)getAdapter()).getHeaderId(firstVisibleItem);
			for(int i = 0;i<getChildCount();i++){
				if((Boolean)super.getChildAt(i).getTag()){
					if(super.getChildAt(i).getTop()<(clippingToPadding ? getPaddingTop() : 0)){
						super.getChildAt(i).findViewById(R.id.__stickylistheaders_header_view).setVisibility(View.INVISIBLE);
					}else{
						super.getChildAt(i).findViewById(R.id.__stickylistheaders_header_view).setVisibility(View.VISIBLE);
					}
				}
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(scrollListener!=null){
			scrollListener.onScrollStateChanged(view, scrollState);
		}
	}

}
