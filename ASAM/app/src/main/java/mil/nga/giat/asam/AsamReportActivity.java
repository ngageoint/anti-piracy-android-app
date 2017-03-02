package mil.nga.giat.asam;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamUtils;

public class AsamReportActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asam_report);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AsamBean asam = (AsamBean) getIntent().getParcelableExtra(AsamConstants.ASAM_KEY);
        AsamReportFragment asamReportFragment = (AsamReportFragment) getSupportFragmentManager().findFragmentById(R.id.asam_list_report_tablet_asam_report_fragment);
        asamReportFragment.updateContent(asam);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && AsamUtils.isTablet(getApplicationContext())) {
            finish();
        }
    }
}
