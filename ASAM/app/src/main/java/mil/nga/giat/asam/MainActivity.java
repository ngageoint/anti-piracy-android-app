package mil.nga.giat.asam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;

import mil.nga.giat.asam.util.AsamConstants;


public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean hideDisclaimer = preferences.getBoolean(AsamConstants.HIDE_DISCLAIMER_KEY, false);
         Intent intent = hideDisclaimer ?
                 new Intent(this, SyncActivity.class) :
                 new Intent(this, DisclaimerActivity.class);

         startActivity(intent);
         finish();
    }
}
