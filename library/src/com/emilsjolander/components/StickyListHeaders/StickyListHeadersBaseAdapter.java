package com.emilsjolander.components.StickyListHeaders;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

/**
 * 
 * @author Emil Sj�lander
 * 
 * 
Copyright 2012 Emil Sj�lander

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
public abstract class StickyListHeadersBaseAdapter extends BaseAdapter implements StickyListHeadersAdapter{
	
	private ArrayList<View> headerCache;
	private ArrayList<WrapperView> wrapperCache;
	private Context context;
	private Drawable divider;
	private int dividerHeight;
	private ArrayList<View> dividerCache;
	
	public StickyListHeadersBaseAdapter(Context context) {
		headerCache = new ArrayList<View>();
		dividerCache = new ArrayList<View>();
		wrapperCache = new ArrayList<WrapperView>();
		this.context = context;
	}
	
	/**
	 * 
	 * @param position
	 * list item's position in list, NOT the index of the header
	 * @param convertView
	 * a reused view, use this if not null
	 * @return
	 * the header for list item at position
	 */
	public abstract View getHeaderView(int position, View convertView);
	
	/**
	 * 
	 * @param position
	 * the list position
	 * @return
	 * an identifier for this header, a header for a position must always have a constant ID
	 */
	public abstract long getHeaderId(int position);
	
	/**
	 * 
	 * @param position
	 * list item's position in list.
	 * @param convertView
	 * a reused view, use this if not null
	 * @return
	 * the list item at position
	 */
	protected abstract View getView(int position, View convertView);
	
	//returns a header for position. will pass a header from cache if one exists
	private View getHeaderWithForPosition(int position){
		View header = null;
		if(headerCache.size()>0){
			header = headerCache.remove(0);
		}
		header = getHeaderView(position,header);
		header.setId(R.id.__stickylistheaders_header_view);
		return header;
	}
	
	//attaches a header to a list item
	private View attachHeaderToListItem(View header, View listItem){
		listItem.setId(R.id.__stickylistheaders_list_item_view);
		WrapperView wrapper = null;
		if(wrapperCache.size()>0){
			wrapper = wrapperCache.remove(0);
		}
		if(wrapper == null){
			wrapper = new WrapperView(context);
		}
		//this does so touches on header are not counted as listitem clicks
		header.setClickable(true);
		header.setFocusable(false);
		return wrapper.wrapViews(header,listItem);
	}

	//attaches a divider to list item
	private View attachDividerToListItem(View listItem) {
		listItem.setId(R.id.__stickylistheaders_list_item_view);
		WrapperView wrapper = null;
		if(wrapperCache.size()>0){
			wrapper = wrapperCache.remove(0);
		}
		if(wrapper == null){
			wrapper = new WrapperView(context);
		}
		View divider = null;
		if(dividerCache.size()>0){
			divider = dividerCache.remove(0);
		}
		if(divider == null){
			divider = new View(context);
			divider.setId(R.id.__stickylistheaders_divider_view);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dividerHeight);
			divider.setLayoutParams(params);
		}
		divider.setBackgroundDrawable(this.divider);
		return wrapper.wrapViews(divider,listItem);
	}
	
	//puts header into headerCache, wrapper into wrapperCache and returns listItem
	//if convertView is null, returns null
	private View axtractHeaderAndListItemFromConvertView(View convertView){
		if(convertView == null) return null;
		ViewGroup vg = (ViewGroup) convertView;
		
		View header = vg.findViewById(R.id.__stickylistheaders_header_view);
		if(header!=null){
			header.setVisibility(View.VISIBLE);
			headerCache.add(header);
		}
		
		View divider = vg.findViewById(R.id.__stickylistheaders_divider_view);
		if(divider!=null){
			dividerCache.add(divider);
		}
		
		View listItem = vg.findViewById(R.id.__stickylistheaders_list_item_view);
		vg.removeAllViews();
		wrapperCache.add(new WrapperView(convertView));
		
		return listItem;
	}
	
	/**
	 * 
	 * !!!DO NOT OVERRIDE THIS METHOD!!!
	 * !!!DO NOT OVERRIDE THIS METHOD!!!
	 * !!!DO NOT OVERRIDE THIS METHOD!!!
	 * 
	 * Override getView(int position,View convertView) instead!
	 * 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = getView(position,axtractHeaderAndListItemFromConvertView(convertView));
		if(position == 0 || getHeaderId(position)!=getHeaderId(position-1)){
			v = attachHeaderToListItem(getHeaderWithForPosition(position),v);
			v.setTag(true);
		}else{
			v = attachDividerToListItem(v);
			v.setTag(false);
		}
		return v;
	}

	public Context getContext() {
		return context;
	}

	/**
	 * @internal
	 * used by the StickyListHeadersListView, set the divider and divider height via listview instead!
	 */
	public void setDivider(Drawable divider, int dividerHeight) {
		this.divider = divider;
		this.dividerHeight = dividerHeight;
	}

	/**
	 * @internal
	 * used by the StickyListHeadersListView, set the divider and divider height via listview instead!
	 */
	public void setDivider(Drawable divider) {
		this.divider = divider;
	}

	/**
	 * @internal
	 * used by the StickyListHeadersListView, set the divider and divider height via listview instead!
	 */
	public void setDividerHeight(int dividerHeight) {
		this.dividerHeight = dividerHeight;
	}
	
	@Override
	public void notifyDataSetChanged() {
		wrapperCache.clear();
		headerCache.clear();
		dividerCache.clear();
		super.notifyDataSetChanged();
	}

}
