package se.emilsjolander.stickylistheaders;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

/**
 * @author Emil Sjölander
 */
public class TestActivity extends FragmentActivity implements
        AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener {

    private TestBaseAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mAdapter = new TestBaseAdapter(this);

        StickyListHeadersListView stickyList = (StickyListHeadersListView) findViewById(R.id.list);
        stickyList.setOnItemClickListener(this);
        stickyList.setOnHeaderClickListener(this);
        stickyList.setOnStickyHeaderOffsetChangedListener(this);

//		mStickyList.addHeaderView(inflater.inflate(R.layout.list_header, null));
//		mStickyList.addFooterView(inflater.inflate(R.layout.list_footer, null));
        stickyList.setEmptyView(findViewById(R.id.empty));

        stickyList.setDrawingListUnderStickyHeader(true);
        stickyList.setAreHeadersSticky(true);

        stickyList.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.restore:
                mAdapter.restore();
                return true;
            case R.id.update:
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.clear:
                mAdapter.clear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Toast.makeText(this, "Item " + position + " clicked!",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onHeaderClick(StickyListHeadersListView l, View header,
                              int itemPosition, long headerId, boolean currentlySticky) {
        Toast.makeText(this, "Header " + headerId + " currentlySticky ? " + currentlySticky,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStickyHeaderOffsetChanged(StickyListHeadersListView l, View header, int offset) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            header.setAlpha(1-(offset/(float)header.getMeasuredHeight()));
        }
    }
}