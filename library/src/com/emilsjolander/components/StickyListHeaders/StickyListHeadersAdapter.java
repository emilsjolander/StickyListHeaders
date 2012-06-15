package com.emilsjolander.components.StickyListHeaders;

import android.graphics.drawable.Drawable;
import android.view.View;

public interface StickyListHeadersAdapter {
	public abstract View getHeaderView(int position, View convertView);
	public abstract long getHeaderId(int position);
	
	public abstract void setDivider(Drawable divider, int dividerHeight);
	public abstract void setDivider(Drawable divider);
	public abstract void setDividerHeight(int dividerHeight);
}
