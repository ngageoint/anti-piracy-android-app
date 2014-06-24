package mil.nga.giat.asam;

import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamLog;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;


public class LegalTabletActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(LegalTabletActivity.class.getName() + ":onCreate");
        setContentView(R.layout.legal_tablet);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    
    public void ngaDisclaimerRowClicked(View view) {
        replaceFragment(R.string.legal_details_fragment_nga_disclaimer_license_text);
    }
    
    public void ngaPrivacyPolicyRowClicked(View view) {
        replaceFragment(R.string.legal_details_fragment_nga_privacy_policy_license_text);
    }
    
    private void replaceFragment(int textId) {
        LegalDetailsFragment fragment = LegalDetailsFragment.newInstance();
        Bundle args = new Bundle();
        args.putInt(AsamConstants.LEGAL_DETAILS_TEXT_KEY, textId);
        fragment.setArguments(args);
        
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.legal_tablet_legal_details_fragment, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }
}
