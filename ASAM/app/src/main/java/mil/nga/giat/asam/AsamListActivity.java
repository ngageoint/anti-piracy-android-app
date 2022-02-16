package mil.nga.giat.asam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;

import mil.nga.giat.asam.map.AsamMapActivity;
import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamListContainer;

public class AsamListActivity extends AppCompatActivity implements AsamListFragment.OnAsamSelectedListener, SortAsamListDialogFragment.OnSortAsamListListener {

    public static final String ALWAYS_SHOW_LIST_KEY = "ALWAYS_SHOW_LIST";

    private static final int SHOW_ON_MAP_REQUEST_CODE = 0;

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
            listFragment.selectAsam(0);
        }

        Boolean alwaysShowList = getIntent().getBooleanExtra(ALWAYS_SHOW_LIST_KEY, false);
        if (reportFragment == null && alwaysShowList == false && AsamListContainer.mAsams.size() == 1) {
            Intent intent = new Intent(this, AsamReportActivity.class);
            intent.putExtra(AsamConstants.ASAM_KEY, AsamListContainer.mAsams.get(0));
            intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
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
        if (reportFragment == null) {
            Intent intent = new Intent(this, AsamReportActivity.class);
            intent.putExtra(AsamConstants.ASAM_KEY, asam);
            startActivityForResult(intent, SHOW_ON_MAP_REQUEST_CODE);
        } else {
            reportFragment.updateContent(asam);
        }
    }
    
    @Override
    public void onSortAsamList(int sortDirection, int sortPopupSpinnerSelection) {
        listFragment.onSort(sortDirection, sortPopupSpinnerSelection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (SHOW_ON_MAP_REQUEST_CODE): {
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(this, AsamMapActivity.class);
                    LatLng latLng = data.getParcelableExtra(AsamMapActivity.MAP_LOCATION);
                    intent.putExtra(AsamMapActivity.MAP_LOCATION, latLng);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
                break;
            }
        }
    }
}
