package mil.nga.giat.asam;

import java.lang.ref.WeakReference;
import java.util.List;

import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.model.AsamJsonParser;
import mil.nga.giat.asam.net.AsamWebService;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.AsamUtils;
import mil.nga.giat.asam.util.SyncTime;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class PreferencesActivity extends ActionBarActivity {

    private static class QueryHandler extends Handler {
        
        WeakReference<PreferencesActivity> mPreferencesActivity;

        QueryHandler(PreferencesActivity preferencesActivity) {
            mPreferencesActivity = new WeakReference<PreferencesActivity>(preferencesActivity);
        }

        @Override
        public void handleMessage(Message message) {
            PreferencesActivity preferencesActivity = mPreferencesActivity.get();
            preferencesActivity.mQueryProgressDialog.dismiss();
            if (preferencesActivity.mQueryError) {
                preferencesActivity.mQueryError = false;
                preferencesActivity.showSimpleDialog(preferencesActivity.getString(R.string.preferences_query_error_text), preferencesActivity.getString(R.string.preferences_query_error_text));
                return;
            }
            SyncTime.finishedSync(preferencesActivity);
            TextView lastSyncTimeUI = (TextView)preferencesActivity.findViewById(R.id.preferences_last_sync_time_ui);
            lastSyncTimeUI.setText(SyncTime.getLastSyncTimeAsText(preferencesActivity));
            preferencesActivity.showSimpleDialog(preferencesActivity.getString(R.string.preferences_sync_complete_title_text), preferencesActivity.getString(R.string.preferences_sync_complete_description_text));
        }
    }
    
    private CheckBox mDisclaimerCheckBoxUI;
    private volatile boolean mQueryError;
    private QueryHandler mQueryHandler;
    private ProgressDialog mQueryProgressDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(PreferencesActivity.class.getName() + ":onCreate");
        setContentView(R.layout.preferences);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mDisclaimerCheckBoxUI = (CheckBox)findViewById(R.id.preferences_hide_disclaimer_checkbox_ui);
        mDisclaimerCheckBoxUI.setChecked(preferences.getBoolean(AsamConstants.HIDE_DISCLAIMER_KEY, false));
        
        // Called to handle the UI after the query has run.
        mQueryHandler = new QueryHandler(this);
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
    
    public void syncButtonClicked(View view) {
        if (SyncTime.isSynched(this)) {
            showSimpleDialog(getString(R.string.preferences_no_sync_title_text), getString(R.string.preferences_no_sync_description_text));
            return;
        }
        mQueryProgressDialog = ProgressDialog.show(this, getString(R.string.preferences_query_progress_dialog_title_text), getString(R.string.preferences_query_progress_dialog_content_text), true);
        new QueryThread().start();
    }
    
    public void disclaimerButtonClicked(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AsamConstants.HIDE_DISCLAIMER_KEY, mDisclaimerCheckBoxUI.isChecked());
        editor.commit();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        AsamLog.i(PreferencesActivity.class.getName() + ":onResume");
        TextView lastSyncTimeUI = (TextView)findViewById(R.id.preferences_last_sync_time_ui);
        lastSyncTimeUI.setText(SyncTime.getLastSyncTimeAsText(this));
    }
    
    private void showSimpleDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.preferences_ok_button_text), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    
    private class QueryThread extends Thread {
        
        @Override
        public void run() {
            mQueryError = false;
            Context context = PreferencesActivity.this;
            String json = null;
            SQLiteDatabase db = null;
            try {
                AsamWebService webService = new AsamWebService(context);
                json = webService.query();
                if (!AsamUtils.isEmpty(json)) {
                    AsamJsonParser parser = new AsamJsonParser();
                    List<AsamBean> asams = parser.parseJson(json);
                    if (asams.size() > 0) {
                        
                        // Do a diff of what the web service returned and what's currently in the db.
                        AsamDbHelper dbHelper = new AsamDbHelper(context);
                        db = dbHelper.getWritableDatabase();
                        asams = dbHelper.removeDuplicates(db, asams);
                        dbHelper.insertAsams(db, asams);
                    }
                }
                SyncTime.finishedSync(context);
            }
            catch (Exception caught) {
                AsamLog.e("There was an error parsing ASAM feed", caught);
                mQueryError = true;
            }
            finally {
                if (db != null) {
                    db.close();
                    db = null;
                }
            }
            mQueryHandler.sendEmptyMessage(0);
        }
    }
}
