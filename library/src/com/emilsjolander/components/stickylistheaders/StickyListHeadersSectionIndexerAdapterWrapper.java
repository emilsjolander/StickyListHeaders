package com.emilsjolander.components.stickylistheaders;

import android.content.Context;
import android.widget.SectionIndexer;

class StickyListHeadersSectionIndexerAdapterWrapper extends
		StickyListHeadersAdapterWrapper implements SectionIndexer {
	
	final SectionIndexer mSectionIndexerDelegate;

	StickyListHeadersSectionIndexerAdapterWrapper(Context context,
			StickyListHeadersAdapter delegate) {
		super(context, delegate);
		mSectionIndexerDelegate = (SectionIndexer) delegate;
	}

	@Override
	public int getPositionForSection(int section) {
		return mSectionIndexerDelegate.getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		return mSectionIndexerDelegate.getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		return mSectionIndexerDelegate.getSections();
	}

}
