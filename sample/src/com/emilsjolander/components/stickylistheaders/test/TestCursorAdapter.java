package com.emilsjolander.components.stickylistheaders.test;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersCursorAdapter;

public class TestCursorAdapter extends StickyListHeadersCursorAdapter {

	private LayoutInflater inflater;

	public TestCursorAdapter(Context context, Cursor c) {
		super(context, c, false);
		inflater = LayoutInflater.from(context);
	}

	@Override
	protected View newHeaderView(Context context, Cursor cursor) {
		HeaderViewHolder holder = new HeaderViewHolder();
		View v = inflater.inflate(R.layout.header, null);
		holder.text = (TextView) v.findViewById(R.id.text);
		v.setTag(holder);
		return v;
	}

	class HeaderViewHolder{
		TextView text;
	}

	@Override
	protected void bindHeaderView(View view, Context context, Cursor cursor) {
		String headerText = ""+cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)).charAt(0);
		((HeaderViewHolder)view.getTag()).text.setText(headerText);
	}

	@Override
	protected long getHeaderId(Context context, Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		return name.charAt(0);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		((ViewHolder)view.getTag()).text.setText(name);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		ViewHolder holder = new ViewHolder();
		View v = inflater.inflate(R.layout.test_list_item_layout, null);
		holder.text = (TextView) v.findViewById(R.id.text);
		v.setTag(holder);
		return v;
	}

	class ViewHolder{
		TextView text;
	}

}
