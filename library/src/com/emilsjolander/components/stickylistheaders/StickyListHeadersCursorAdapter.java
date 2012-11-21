package com.emilsjolander.components.stickylistheaders;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

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
public abstract class StickyListHeadersCursorAdapter extends CursorAdapter implements StickyListHeadersAdapter{

	private final ArrayList<View> headerCache = new ArrayList<View>();
	private final ArrayList<View> dividerCache = new ArrayList<View>();
	private final ArrayList<WrapperView> wrapperCache = new ArrayList<WrapperView>();
	private final Context context;
	private Drawable divider;
	private int dividerHeight;

	@Deprecated
	public StickyListHeadersCursorAdapter(Context context, Cursor c) {
		super(context,c);
		this.context = context;
	}

	public StickyListHeadersCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context,c,autoRequery);
		this.context = context;
	}

	/**
	 * WARNING! This constructor requires API 11 or newer.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public StickyListHeadersCursorAdapter(Context context, Cursor c, int flags) {
		super(context,c,flags);
		this.context = context;
	}

	public View getHeaderView(int position, View convertView){
		if (!getCursor().moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position " + position);
		}
		View v;
		if (convertView == null) {
			v = newHeaderView(context, getCursor());
		} else {
			v = convertView;
		}
		bindHeaderView(v, context, getCursor());
		return v;
	}

	/**
	 * Makes a new header to hold the data pointed to by cursor.
   *
	 * @param context
   * Interface to application's global information.
	 * @param cursor
   * The cursor from which to get the data. The cursor is already moved to the
   * correct position.
	 * @return
	 * The newly created header.
	 */
	protected abstract View newHeaderView(Context context, Cursor cursor);

	/**
	 * Bind an existing header to the data pointed to by cursor.
   *
	 * @param view
   * Existing view, returned earlier by
   * {@link #newHeaderView(android.content.Context, android.database.Cursor)}
	 * @param context
   * Interface to application's global information.
	 * @param cursor
   * The cursor from which to get the data. The cursor is already moved to the
   * correct position.
	 */
	protected abstract void bindHeaderView(View view, Context context, Cursor cursor);

	public long getHeaderId(int position){
		if (!getCursor().moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position " + position);
		}
		return getHeaderId(context,getCursor());
	}

	/**
	 *
	 * @param context
	 * Interface to application's global information
	 * @param cursor
	 * cursor The cursor from which to get the data. The cursor is already moved to the correct position.
	 * @return
	 * an identifier for this header, a header for a position must always have a constant positive ID
	 */
	protected abstract long getHeaderId(Context context, Cursor cursor);

	//returns a header for position. will pass a header from cache if one exists
	private View getHeaderForPosition(int position){
		View header = null;
		if(headerCache.size()>0){
			header = headerCache.remove(0);
		}
		header = getHeaderView(position,header);
		return header;
	}

	//attaches a header to a list item
	private View attachHeaderToListItem(View header, View listItem){
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
		return wrapper.wrapViews(listItem, null, header);
	}

	//attaches a divider to list item
	private View attachDividerToListItem(View listItem) {
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
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, dividerHeight);
			divider.setLayoutParams(params);
		}
		divider.setBackgroundDrawable(this.divider);
		return wrapper.wrapViews(listItem, divider, null);
	}

	//puts header into headerCache, wrapper into wrapperCache and returns listItem
	//if convertView is null, returns null
	private View extractHeaderAndListItemFromConvertView(View convertView){
		if(convertView == null) return null;
		WrapperView wv = (WrapperView) convertView;

		View header = wv.getHeader();
		if(header!=null){
			header.setVisibility(View.VISIBLE);
			headerCache.add(header);
		}

		View divider = wv.getDivider();
		if(divider!=null){
			dividerCache.add(divider);
		}

		View listItem = wv.getItem();
		wv.removeAllViews();
		wrapperCache.add(wv);

		return listItem;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position,extractHeaderAndListItemFromConvertView(convertView),parent);
		if(position == 0 || getHeaderId(position)!=getHeaderId(position-1)){
			v = attachHeaderToListItem(getHeaderForPosition(position),v);
		}else{
			v = attachDividerToListItem(v);
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
	@Override
	public void setDivider(Drawable divider) {
		this.divider = divider;
	}

	/**
	 * @internal
	 * used by the StickyListHeadersListView, set the divider and divider height via listview instead!
	 */
	@Override
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
