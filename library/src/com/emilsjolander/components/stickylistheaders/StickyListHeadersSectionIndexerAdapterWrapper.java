package com.emilsjolander.components.stickylistheaders;

import android.content.Context;
import android.widget.SectionIndexer;

public class StickyListHeadersSectionIndexerAdapterWrapper extends StickyListHeadersAdapterWrapper implements SectionIndexer {
	
	private final SectionIndexer delegate;

	StickyListHeadersSectionIndexerAdapterWrapper(Context context,
			StickyListHeadersAdapter delegate) {
		super(context, delegate);
		this.delegate = (SectionIndexer) delegate;
	}

	@Override
	public int getPositionForSection(int section) {
		int position = delegate.getPositionForSection(section);
		position = translateAdapterPosition(position);
		return position;
	}

	@Override
	public int getSectionForPosition(int position) {
		position = translateListViewPosition(position);
		return delegate.getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		return delegate.getSections();
	}

}
