package com.emilsjolander.components.stickylistheaders.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;

/**
 * @author Emil Sjölander
 */
public class TestBaseAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private final Context mContext;
    private String[] mCountries;
    private LayoutInflater mInflater;

    public TestBaseAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mCountries = context.getResources().getStringArray(R.array.countries);
    }

    @Override
    public int getCount() {
        return mCountries.length;
    }

    @Override
    public Object getItem(int position) {
        return mCountries[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.test_list_item_layout, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(mCountries[position]);

        return convertView;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        // set header text as first char in name
        char headerChar = mCountries[position].subSequence(0, 1).charAt(0);
        String headerText;
        if (headerChar % 2 == 0) {
            headerText = headerChar + "\n" + headerChar + "\n" + headerChar;
        } else {
            headerText = headerChar + "\n" + headerChar;
        }
        holder.text.setText(headerText);

        return convertView;
    }

    /**
     * Remember that these have to be static, postion=1 should always return
     * the same Id that is.
     */
    @Override
    public long getHeaderId(int position) {
        // return the first character of the country as ID because this is what
        // headers are based upon
        return mCountries[position].subSequence(0, 1).charAt(0);
    }

    public void clear() {
        mCountries = new String[0];
        notifyDataSetChanged();
    }

    public void restore() {
        mCountries = mContext.getResources().getStringArray(R.array.countries);
        notifyDataSetChanged();
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        TextView text;
    }

}
