package com.emilsjolander.components.stickylistheaders.test;

import static android.widget.Toast.LENGTH_SHORT;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView.OnHeaderClickListener;

public class TestFragment extends Fragment implements
AdapterView.OnItemClickListener, OnHeaderClickListener {

	private TestBaseAdapter mAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_test, container, false);
		StickyListHeadersListView stickyList = (StickyListHeadersListView) v.findViewById(R.id.list);
		stickyList.setOnItemClickListener(this);
		stickyList.setOnHeaderClickListener(this);

		stickyList.addHeaderView(inflater.inflate(
				R.layout.list_header, null));
		stickyList.addFooterView(inflater.inflate(
				R.layout.list_footer, null));
		mAdapter = new TestBaseAdapter(getActivity());
		stickyList.setEmptyView(v.findViewById(R.id.empty));
		
		stickyList.setAdapter(mAdapter);
		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Toast.makeText(getActivity(), "Item " + position + " clicked!", LENGTH_SHORT)
				.show();
	}

	@Override
	public void onHeaderClick(StickyListHeadersListView l, View header,
			int itemPosition, long headerId, boolean currentlySticky) {
		Toast.makeText(getActivity(), "header " + headerId, Toast.LENGTH_SHORT).show();
	}

}
