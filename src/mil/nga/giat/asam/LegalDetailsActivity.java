package mil.nga.giat.asam;

import mil.nga.giat.asam.util.AsamConstants;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;


public class LegalDetailsActivity extends ActionBarActivity {

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legal_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int titleId = extras.getInt(AsamConstants.LEGAL_DETAILS_TITLE_KEY);
            getSupportActionBar().setTitle(titleId);
        }
        
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
