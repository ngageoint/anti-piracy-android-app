package mil.nga.giat.asam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import mil.nga.giat.asam.map.AllAsamsMapTabletActivity;


public class DisclaimerActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disclaimer);
    }
    
    public void disclaimerExitButtonClicked(View view) {
        finish();
    }
    
    public void disclaimerAgreeButtonClicked(View view) {
        Intent intent = new Intent(this, AllAsamsMapTabletActivity.class);
        startActivity(intent);
        finish();
    }
}
