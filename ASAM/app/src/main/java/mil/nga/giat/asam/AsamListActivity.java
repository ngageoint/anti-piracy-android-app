package mil.nga.giat.asam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;

import java.util.Collections;

import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamListContainer;
import mil.nga.giat.asam.util.AsamLog;

public class AsamListActivity extends ActionBarActivity implements AsamListFragment.OnAsamSelectedListener, SortAsamListDialogFragment.OnSortAsamListListener {

    public static final String ALWAYS_SHOW_LIST_KEY = "ALWAYS_SHOW_LIST";

    private AsamListFragment listFragment = null;
    private AsamReportFragment reportFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asam_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Pick the first item in the list.
        if (reportFragment != null && AsamListContainer.mAsams.size() > 0) {
            Collections.sort(AsamListContainer.mAsams, new AsamBean.DescendingOccurrenceDateComparator());
            reportFragment.updateContent(AsamListContainer.mAsams.get(0));
        }

        Boolean alwaysShowList = getIntent().getBooleanExtra(ALWAYS_SHOW_LIST_KEY, false);
        if (reportFragment == null && alwaysShowList == false && AsamListContainer.mAsams.size() == 1) {
            Intent intent = new Intent(this, AsamReportActivity.class);
            intent.putExtra(AsamConstants.ASAM_KEY, AsamListContainer.mAsams.get(0));
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof  AsamReportFragment) {
            reportFragment = (AsamReportFragment) fragment;
        } else if (fragment instanceof  AsamListFragment) {
            listFragment = (AsamListFragment) fragment;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAsamSelected(AsamBean asam) {
        AsamLog.i(AsamListActivity.class.getName() + ":onAsamSelected");
        if (reportFragment == null) {
            Intent intent = new Intent(this, AsamReportActivity.class);
            intent.putExtra(AsamConstants.ASAM_KEY, asam);
            startActivity(intent);
        } else {
            reportFragment.updateContent(asam);
        }
    }
    
    @Override
    public void onSortAsamList(int sortDirection, int sortPopupSpinnerSelection) {
        AsamLog.i(AsamListActivity.class.getName() + ":onSortAsamList");
        listFragment.onSortAsamList(sortDirection, sortPopupSpinnerSelection);
    }
    
    public void mapAsamLocation(View view) {
        reportFragment.mapAsamLocation(view);
    }
}
