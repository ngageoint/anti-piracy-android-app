package mil.nga.giat.asam;

import mil.nga.giat.asam.util.AsamConstants;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;


public class LegalActivity extends ActionBarActivity {

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    
    public void ngaDisclaimerRowClicked(View view) {
        Intent intent = new Intent(this, LegalDetailsActivity.class);
        intent.putExtra(AsamConstants.LEGAL_DETAILS_TITLE_KEY, R.string.legal_details_nga_disclaimer_title_text);
        intent.putExtra(AsamConstants.LEGAL_DETAILS_TEXT_KEY, R.string.legal_details_fragment_nga_disclaimer_license_text);
        startActivity(intent);
    }
    
    public void ngaPrivacyPolicyRowClicked(View view) {
        Intent intent = new Intent(this, LegalDetailsActivity.class);
        intent.putExtra(AsamConstants.LEGAL_DETAILS_TITLE_KEY, R.string.legal_details_nga_privacy_policy_title_text);
        intent.putExtra(AsamConstants.LEGAL_DETAILS_TEXT_KEY, R.string.legal_details_fragment_nga_privacy_policy_license_text);
        startActivity(intent);
    }
    
    public void openSourceAttributionRowClicked(View view) {
        Intent intent = new Intent(this, OpenSourceDetailsActivity.class);
        startActivity(intent);
    }
}
