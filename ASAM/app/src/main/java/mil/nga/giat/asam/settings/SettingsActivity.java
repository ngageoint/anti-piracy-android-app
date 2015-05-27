package mil.nga.giat.asam.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import mil.nga.giat.asam.PullAsamsTask;
import mil.nga.giat.asam.R;
import mil.nga.giat.asam.settings.about.AboutActivity;
import mil.nga.giat.asam.settings.about.AboutFragment;
import mil.nga.giat.asam.settings.disclaimer.DisclaimerActivity;
import mil.nga.giat.asam.settings.disclaimer.DisclaimerFragment;
import mil.nga.giat.asam.settings.opensource.OpenSourceActivity;
import mil.nga.giat.asam.settings.opensource.OpenSourceFragment;
import mil.nga.giat.asam.settings.privacy.PrivacyActivity;
import mil.nga.giat.asam.settings.privacy.PrivacyFragment;
import mil.nga.giat.asam.util.SyncTime;


public class SettingsActivity extends AppCompatActivity implements SettingsFragment.InfoClickListener, PullAsamsTask.OnSyncCompletedListener {

    private SettingsFragment settingsFragment = null;
    private View detailContainer = null;
    private Fragment detailFragment = null;
    private TextView syncDate = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        syncDate = (TextView) findViewById(R.id.sync_date);
        syncDate.setText(SyncTime.getLastSyncTimeAsText(this));

        detailContainer = findViewById(R.id.detail_container);
        if (detailContainer != null) {
            settingsFragment.selectFirstRow();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof SettingsFragment) {
            settingsFragment = (SettingsFragment) fragment;
        } else {
            detailFragment = fragment;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAboutClick() {
        if (detailContainer == null) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        } else {
            detailFragment = new AboutFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(detailContainer.getId(), detailFragment)
                    .commit();
        }
    }

    @Override
    public void onDisclaimerClick() {
        if (detailContainer == null) {
            Intent intent = new Intent(this, DisclaimerActivity.class);
            startActivity(intent);
        } else {
            detailFragment = new DisclaimerFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(detailContainer.getId(), detailFragment)
                    .commit();
        }
    }

    @Override
    public void onPrivacyPolicyClick() {
        if (detailContainer == null) {
            Intent intent = new Intent(this, PrivacyActivity.class);
            startActivity(intent);
        } else {
            detailFragment = new PrivacyFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(detailContainer.getId(), detailFragment)
                    .commit();
        }
    }

    @Override
    public void onOpenSourceClick() {
        if (detailContainer == null) {
            Intent intent = new Intent(this, OpenSourceActivity.class);
            startActivity(intent);
        } else {
            detailFragment = new OpenSourceFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(detailContainer.getId(), detailFragment)
                    .commit();
        }
    }

    @Override
    public void onSyncClick() {
        if (SyncTime.isSynched(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getString(R.string.preferences_no_sync_title_text));
            builder.setMessage(getString(R.string.preferences_no_sync_description_text));
            builder.setPositiveButton(getString(R.string.preferences_ok_button_text), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            builder.show();
            return;
        }

        new PullAsamsTask(this, this).execute();
    }

    @Override
    public void onSyncCompleted() {
        syncDate.setText(SyncTime.getLastSyncTimeAsText(this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getString(R.string.preferences_sync_complete_title_text));
        builder.setMessage(getString(R.string.preferences_sync_complete_description_text));
        builder.setPositiveButton(getString(R.string.preferences_ok_button_text), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
