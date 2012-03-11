package com.emilsjolander.components.StickyListHeaders;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class StickyListHeadersAdapter extends BaseAdapter {
	
	public static final int LIST_ITEM_ID = 1337;
	public static final int HEADER_ID = 2674;
	
	private ArrayList<View> headerCache;
	private ArrayList<WrapperView> wrapperCache;
	private Context context;
	private HashMap<Integer, View> currentlyVissibleHeaderViews;
	
	public StickyListHeadersAdapter(Context context) {
		headerCache = new ArrayList<View>();
		wrapperCache = new ArrayList<WrapperView>();
		currentlyVissibleHeaderViews = new HashMap<Integer, View>();
		this.context = context;
	}
	
	public abstract View getHeaderView(int position, View convertView);
	
	/**
	 * 
	 * @param position
	 * the list position
	 * @return
	 * an identifier for this header, a header for a position must always have a constant ID
	 */
	public abstract long getHeaderId(int position);
	protected abstract View getView(int position, View convertView);
	
	private View getHeaderWithForPosition(int position){
		View header = null;
		if(headerCache.size()>0){
			header = headerCache.remove(0);
		}
		header = getHeaderView(position,header);
		header.setId(HEADER_ID);
		return header;
	}
	
	private View attachHeaderToListItem(View header, View listItem){
		listItem.setId(LIST_ITEM_ID);
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

	private View wrapListItem(View listItem) {
		listItem.setId(LIST_ITEM_ID);
		WrapperView wrapper = null;
		if(wrapperCache.size()>0){
			wrapper = wrapperCache.remove(0);
		}
		if(wrapper == null){
			wrapper = new WrapperView(context);
		}
		return wrapper.wrapViews(listItem);
	}
	
	/**
	 * puts header into headerCache, wrapper into wrapperCache and returns listItem
	 * if convertView is null, returns null
	 */
	private View axtractHeaderAndListItemFromConvertView(View convertView){
		if(convertView == null) return null;
		if(currentlyVissibleHeaderViews.containsValue(convertView)){
			currentlyVissibleHeaderViews.remove(convertView.getTag());
		}
		ViewGroup vg = (ViewGroup) convertView;
		
		View header = vg.findViewById(HEADER_ID);
		if(header!=null){
			headerCache.add(header);
		}
		
		View listItem = vg.findViewById(LIST_ITEM_ID);
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
			currentlyVissibleHeaderViews.put(position, v);
		}else{
			v = wrapListItem(v);
		}
		v.setTag(position);
		return v;
	}

	public Context getContext() {
		return context;
	}
	
	public HashMap<Integer, View> getCurrentlyVissibleHeaderViews() {
		return currentlyVissibleHeaderViews;
	}

}
