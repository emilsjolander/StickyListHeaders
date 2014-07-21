package se.emilsjolander.stickylistheaders.sample;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * @author Emil Sjölander
 */
public class TestActivity extends ActionBarActivity implements
        AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
        StickyListHeadersListView.OnStickyHeaderOffsetChangedListener,
        StickyListHeadersListView.OnStickyHeaderChangedListener {

    private TestBaseAdapter mAdapter;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean fadeHeader = true;

    private StickyListHeadersListView stickyList;
    private SwipeRefreshLayout refreshLayout;

    private Button restoreButton;
    private Button updateButton;
    private Button clearButton;

    private CheckBox stickyCheckBox;
    private CheckBox fadeCheckBox;
    private CheckBox drawBehindCheckBox;
    private CheckBox fastScrollCheckBox;
    private Button openExpandableListButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        mAdapter = new TestBaseAdapter(this);

        stickyList = (StickyListHeadersListView) findViewById(R.id.list);
        stickyList.setOnItemClickListener(this);
        stickyList.setOnHeaderClickListener(this);
        stickyList.setOnStickyHeaderChangedListener(this);
        stickyList.setOnStickyHeaderOffsetChangedListener(this);
        stickyList.addHeaderView(getLayoutInflater().inflate(R.layout.list_header, null));
        stickyList.addFooterView(getLayoutInflater().inflate(R.layout.list_footer, null));
        stickyList.setEmptyView(findViewById(R.id.empty));
        stickyList.setDrawingListUnderStickyHeader(true);
        stickyList.setAreHeadersSticky(true);
        stickyList.setAdapter(mAdapter);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        );

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        restoreButton = (Button) findViewById(R.id.restore_button);
        restoreButton.setOnClickListener(buttonListener);
        openExpandableListButton = (Button) findViewById(R.id.open_expandable_list_button);
        openExpandableListButton.setOnClickListener(buttonListener);
        updateButton = (Button) findViewById(R.id.update_button);
        updateButton.setOnClickListener(buttonListener);
        clearButton = (Button) findViewById(R.id.clear_button);
        clearButton.setOnClickListener(buttonListener);

        stickyCheckBox = (CheckBox) findViewById(R.id.sticky_checkBox);
        stickyCheckBox.setOnCheckedChangeListener(checkBoxListener);
        fadeCheckBox = (CheckBox) findViewById(R.id.fade_checkBox);
        fadeCheckBox.setOnCheckedChangeListener(checkBoxListener);
        drawBehindCheckBox = (CheckBox) findViewById(R.id.draw_behind_checkBox);
        drawBehindCheckBox.setOnCheckedChangeListener(checkBoxListener);
        fastScrollCheckBox = (CheckBox) findViewById(R.id.fast_scroll_checkBox);
        fastScrollCheckBox.setOnCheckedChangeListener(checkBoxListener);

        stickyList.setStickyHeaderTopOffset(-20);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    CompoundButton.OnCheckedChangeListener checkBoxListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.sticky_checkBox:
                    stickyList.setAreHeadersSticky(isChecked);
                    break;
                case R.id.fade_checkBox:
                    fadeHeader = isChecked;
                    break;
                case R.id.draw_behind_checkBox:
                    stickyList.setDrawingListUnderStickyHeader(isChecked);
                    break;
                case R.id.fast_scroll_checkBox:
                    stickyList.setFastScrollEnabled(isChecked);
                    stickyList.setFastScrollAlwaysVisible(isChecked);
                    break;
            }
        }
    };

    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.restore_button:
                    mAdapter.restore();
                    break;
                case R.id.update_button:
                    mAdapter.notifyDataSetChanged();
                    break;
                case R.id.clear_button:
                    mAdapter.clear();
                    break;
                case R.id.open_expandable_list_button:
                    Intent intent = new Intent(TestActivity.this,ExpandableListTestActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, "Item " + position + " clicked!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {
        Toast.makeText(this, "Header " + headerId + " currentlySticky ? " + currentlySticky, Toast.LENGTH_SHORT).show();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onStickyHeaderOffsetChanged(StickyListHeadersListView l, View header, int offset) {
        if (fadeHeader && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            header.setAlpha(1 - (offset / (float) header.getMeasuredHeight()));
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onStickyHeaderChanged(StickyListHeadersListView l, View header, int itemPosition, long headerId) {
        header.setAlpha(1);
    }

}