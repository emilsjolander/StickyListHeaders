package com.emilsjolander.components.stickylistheaders.test;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;


/**
 * @author Emil Sjölander
 */
public class TestBaseAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer {

	private String[] countries;
	private int[] sectionIndices;
	private Character[] sectionsLetters;
	private LayoutInflater inflater;

	public TestBaseAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		countries = context.getResources().getStringArray(R.array.countries);
		sectionIndices = getSectionIndices();
		sectionsLetters = getStartingLetters();
	}

	private Character[] getStartingLetters() {
		Character[] letters = new Character[sectionIndices.length];
		for (int i = 0; i < sectionIndices.length; i++) {
			letters[i] = countries[sectionIndices[i]].charAt(0);
		}
		return letters;
	}

	private int[] getSectionIndices() {
		ArrayList<Integer> sectionIndices = new ArrayList<Integer>();
		char lastFirstChar = countries[0].charAt(0);
		sectionIndices.add(0);
		for (int i = 1; i < countries.length; i++) {
			if(countries[i].charAt(0) != lastFirstChar) {
				lastFirstChar = countries[i].charAt(0);
				sectionIndices.add(i);
			}
		}
		int[] sections = new int[sectionIndices.size()];
		for (int i = 0; i < sectionIndices.size(); i++) {
			sections[i] = sectionIndices.get(i);
		}
		return sections;
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

	@Override public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.test_list_item_layout, parent, false);
			holder.text = (TextView) convertView.findViewById(R.id.text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.text.setText(countries[position]);

		return convertView;
	}

	@Override public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder holder;
		if (convertView == null) {
			holder = new HeaderViewHolder();
			convertView = inflater.inflate(R.layout.header, parent, false);
			holder.text1 = (TextView) convertView.findViewById(R.id.text1);
			holder.text2 = (TextView) convertView.findViewById(R.id.text2);
			convertView.setTag(holder);
		} else {
			holder = (HeaderViewHolder) convertView.getTag();
		}
		//set header text as first char in name
		char headerChar = countries[position].subSequence(0, 1).charAt(0);
		String headerText;
		if(headerChar%2 == 0){
			headerText = headerChar + "\n" + headerChar + "\n" + headerChar;
		}else{
			headerText = headerChar + "\n" + headerChar;
		}
		holder.text1.setText(headerText);
		holder.text2.setText(headerText);
		return convertView;
	}

	//remember that these have to be static, postion=1 should walys return the same Id that is.
	@Override
	public long getHeaderId(int position) {
		//return the first character of the country as ID because this is what headers are based upon
		return countries[position].subSequence(0, 1).charAt(0);
	}

	class HeaderViewHolder {
		TextView text1;
		TextView text2;
	}

	class ViewHolder {
		TextView text;
	}

	@Override
	public int getPositionForSection(int section) {
		if(section >= sectionIndices.length) {
			section = sectionIndices.length-1;
		}else if(section < 0){
			section = 0;
		}
		return sectionIndices[section];
	}

	@Override
	public int getSectionForPosition(int position) {
		for (int i = 0; i < sectionIndices.length; i++) {
			if(position < sectionIndices[i]) {
				return i-1;
			}
		}
		return sectionIndices.length-1;
	}

	@Override
	public Object[] getSections() {
		return sectionsLetters;
	}
	
}
