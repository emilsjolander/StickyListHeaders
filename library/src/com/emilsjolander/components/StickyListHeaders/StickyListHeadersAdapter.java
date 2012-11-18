package com.emilsjolander.components.stickylistheaders;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ListAdapter;

public interface StickyListHeadersAdapter extends ListAdapter {
	View getHeaderView(int position, View convertView);
	long getHeaderId(int position);

	void setDivider(Drawable divider, int dividerHeight);
	void setDivider(Drawable divider);
	void setDividerHeight(int dividerHeight);
}
