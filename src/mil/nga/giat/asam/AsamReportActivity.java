package mil.nga.giat.asam;

import mil.nga.giat.asam.util.AsamConstants;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;


public class AsamReportActivity extends ActionBarActivity {

    private AsamReportFragment mAsamReportFragment;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asam_report);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        mAsamReportFragment = new AsamReportFragment();
        Bundle args = new Bundle();
        args.putSerializable(AsamConstants.ASAM_KEY, getIntent().getSerializableExtra(AsamConstants.ASAM_KEY));
        mAsamReportFragment.setArguments(args);
        
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.asam_report_asam_report_fragment, mAsamReportFragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
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
