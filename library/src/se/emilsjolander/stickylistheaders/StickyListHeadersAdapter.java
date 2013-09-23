package se.emilsjolander.stickylistheaders;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public interface StickyListHeadersAdapter extends ListAdapter {
	/**
	 * Get a View that displays the header data at the specified position in the
	 * set. You can either create a View manually or inflate it from an XML layout
	 * file.
	 *
	 * @param position
	 * The position of the item within the adapter's data set of the item whose
	 * header view we want.
	 * @param convertView
	 * The old view to reuse, if possible. Note: You should check that this view is
	 * non-null and of an appropriate type before using. If it is not possible to
	 * convert this view to display the correct data, this method can create a new
	 * view.
	 * @param parent
	 * The parent that this view will eventually be attached to.
	 * @return
	 * A View corresponding to the data at the specified position.
	 */
	View getHeaderView(int position, View convertView, ViewGroup parent);

	/**
	 * Get the header id associated with the specified position in the list.
	 *
	 * @param position
	 * The position of the item within the adapter's data set whose header id we
	 * want.
	 * @return
	 * The id of the header at the specified position.
	 */
	long getHeaderId(int position);
}
