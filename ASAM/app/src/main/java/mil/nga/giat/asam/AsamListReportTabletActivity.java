package mil.nga.giat.asam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;

import java.util.Collections;

import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamListContainer;
import mil.nga.giat.asam.util.AsamLog;

public class AsamListReportTabletActivity extends ActionBarActivity implements AsamListFragment.OnAsamSelectedListener, SortAsamListDialogFragment.OnSortAsamListListener {    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(AsamListReportTabletActivity.class.getName() + ":onCreate");
        setContentView(R.layout.asam_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Pick the first item in the list.
        AsamReportFragment asamReportFragment = (AsamReportFragment) getSupportFragmentManager().findFragmentById(R.id.asam_list_report_tablet_asam_report_fragment);
        if (asamReportFragment != null && AsamListContainer.mAsams.size() > 0) {
            Collections.sort(AsamListContainer.mAsams, new AsamBean.DescendingOccurrenceDateComparator());
            asamReportFragment.updateContent(AsamListContainer.mAsams.get(0));
            //TODO select first item in list
//            AsamReportFragment asamReportFragment = new AsamReportFragment();
//            Bundle args = new Bundle();
//            args.putSerializable(AsamConstants.ASAM_KEY, AsamListContainer.mAsams.get(0));
//            asamReportFragment.setArguments(args);
//
//            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.replace(R.id.asam_list_report_tablet_asam_report_fragment, asamReportFragment);
//            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//            fragmentTransaction.commit();
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
        AsamLog.i(AsamListReportTabletActivity.class.getName() + ":onAsamSelected");
        AsamReportFragment asamReportFragment = (AsamReportFragment) getSupportFragmentManager().findFragmentById(R.id.asam_list_report_tablet_asam_report_fragment);
        if (asamReportFragment == null) {
            Intent intent = new Intent(this, AsamReportActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(AsamConstants.ASAM_KEY, asam);
            intent.putExtras(bundle);
            startActivity(intent);
        } else {
            asamReportFragment.updateContent(asam);
        }

//        Bundle args = new Bundle();
//        args.putSerializable(AsamConstants.ASAM_KEY, asam);
//        asamReportFragment.setArguments(args);
//
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.asam_list_report_tablet_asam_report_fragment, asamReportFragment);
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//        fragmentTransaction.commit();
    }
    
    @Override
    public void onSortAsamList(int sortDirection, int sortPopupSpinnerSelection) {
        AsamLog.i(AsamListReportTabletActivity.class.getName() + ":onSortAsamList");
        AsamListFragment fragment = (AsamListFragment)getSupportFragmentManager().findFragmentById(R.id.asam_list_report_tablet_asam_list_fragment);
        fragment.onSortAsamList(sortDirection, sortPopupSpinnerSelection);
    }
    
    public void mapAsamLocation(View view) {
        AsamReportFragment fragment = (AsamReportFragment)getSupportFragmentManager().findFragmentById(R.id.asam_list_report_tablet_asam_report_fragment);
        fragment.mapAsamLocation(view);
    }
}
