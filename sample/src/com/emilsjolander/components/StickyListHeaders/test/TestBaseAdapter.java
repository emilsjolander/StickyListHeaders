package com.emilsjolander.components.StickyListHeaders.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.emilsjolander.components.StickyListHeaders.R;
import com.emilsjolander.components.StickyListHeaders.StickyListHeadersBaseAdapter;
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
public class TestBaseAdapter extends StickyListHeadersBaseAdapter {
	
	private String[] countries;
	private LayoutInflater inflater;

	public TestBaseAdapter(Context context) {
		super(context);
		inflater = LayoutInflater.from(context);
		countries = context.getResources().getStringArray(R.array.countries);
	}

	@Override
	public int getCount() {
		return countries.length;
	}

	@Override
	public Object getItem(int position) {
		return countries[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	//remember that these have to be static, postion=1 should walys return the same Id that is.
	@Override
	public long getHeaderId(int position) {
		//return the first character of the country as ID because this is what headers are based upon
		return countries[position].subSequence(0, 1).charAt(0);
	}

	//works in the same way as getview()
	@Override
	public View getHeaderView(int position, View convertView) {
		HeaderViewHolder holder;
		
		if(convertView == null){
			holder = new HeaderViewHolder();
			convertView = inflater.inflate(R.layout.header, null);
			holder.text = (TextView) convertView.findViewById(R.id.text);
			convertView.setTag(holder);
		}else{
			holder = (HeaderViewHolder) convertView.getTag();
		}
		
		//set header text as first char in name
		holder.text.setText(countries[position].subSequence(0, 1));
		
		return convertView;
	}
	
	class HeaderViewHolder{
		TextView text;
	}

	//note that i do not ovveride getView(int position, View convertView, ViewGroup parent);
	//as this would break listheader functionality
	@Override
	protected View getView(int position, View convertView) {
		ViewHolder holder;
		
		if(convertView == null){
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.test_list_item_layout, null);
			holder.text = (TextView) convertView.findViewById(R.id.text);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.text.setText(countries[position]);
		
		return convertView;
	}
	
	class ViewHolder{
		TextView text;
	}

}
