package mil.nga.giat.asam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;

import mil.nga.giat.asam.map.AsamMapActivity;
import mil.nga.giat.asam.util.AsamConstants;


public class DisclaimerActivity extends FragmentActivity {

    private SharedPreferences preferences;
    private CheckBox showDisclaimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.disclaimer);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        showDisclaimer = (CheckBox) findViewById(R.id.show_disclaimer);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hideDisclaimer = preferences.getBoolean(AsamConstants.HIDE_DISCLAIMER_KEY, false);
        showDisclaimer.setChecked(!hideDisclaimer);
    }
    
    public void disclaimerExitButtonClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    public void disclaimerAgreeButtonClicked(View view) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AsamConstants.HIDE_DISCLAIMER_KEY, !showDisclaimer.isChecked());
        editor.apply();

        Intent intent = new Intent(this, AsamMapActivity.class);
        startActivity(intent);
        finish();
    }
}
