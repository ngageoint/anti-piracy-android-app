package mil.nga.giat.asam;

import mil.nga.giat.asam.util.AsamLog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;


public class InfoActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(InfoActivity.class.getName() + ":onCreate");
        setContentView(R.layout.info);
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
    
    public void legalRowClicked(View view) {
        Intent intent = new Intent(this, LegalActivity.class);
        startActivity(intent);
    }
    
    public void emailLinkClicked(View view) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        String[] recipients = { getString(R.string.info_fragment_email_address) };
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.info_fragment_email_subject_text));
        intent.setType("plain/text");
        startActivity(intent);
    }
}
