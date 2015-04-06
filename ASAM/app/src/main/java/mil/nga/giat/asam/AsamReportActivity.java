package mil.nga.giat.asam;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;

import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamConstants;


public class AsamReportActivity extends ActionBarActivity {

    private AsamReportFragment mAsamReportFragment;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asam_report);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AsamBean asam = (AsamBean) getIntent().getParcelableExtra(AsamConstants.ASAM_KEY);
        AsamReportFragment asamReportFragment = (AsamReportFragment) getSupportFragmentManager().findFragmentById(R.id.asam_list_report_tablet_asam_report_fragment);
        asamReportFragment.updateContent(asam);
    }
    
    public void mapAsamLocation(View view) {
        mAsamReportFragment.mapAsamLocation(view);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
