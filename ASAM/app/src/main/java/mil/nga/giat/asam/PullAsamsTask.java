package mil.nga.giat.asam;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.model.AsamJsonParser;
import mil.nga.giat.asam.net.AsamWebService;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.SyncTime;

/**
 * Created by wnewman on 5/26/15.
 */
public class PullAsamsTask extends AsyncTask<Void, Void, Void> {

    public interface OnSyncCompletedListener {
        void onSyncCompleted();
    }

    private Context context;
    private ProgressDialog progressDialog;
    private boolean error = false;
    private OnSyncCompletedListener onSyncCompletedListener;

    public PullAsamsTask(Context context, OnSyncCompletedListener onSyncCompletedListener) {
        this.context = context;
        this.onSyncCompletedListener = onSyncCompletedListener;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context, R.style.AppCompatAlertDialogStyle);
        progressDialog.setTitle(context.getString(R.string.preferences_query_progress_dialog_title_text));
        progressDialog.setMessage(context.getString(R.string.preferences_query_progress_dialog_content_text));
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        String json = null;
        SQLiteDatabase db = null;
        try {
            AsamWebService webService = new AsamWebService(context);
            json = webService.query();
            if (StringUtils.isNotBlank(json)) {
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
        } catch (Exception caught) {
            AsamLog.e("There was an error parsing ASAM feed", caught);
            error = true;
        } finally {
            if (db != null) {
                db.close();
                db = null;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        progressDialog.dismiss();

        if (error) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(context.getString(R.string.preferences_query_error_text));
            builder.setMessage(context.getString(R.string.preferences_query_error_text));
            builder.setPositiveButton(context.getString(R.string.preferences_ok_button_text), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            builder.show();
        }

        onSyncCompletedListener.onSyncCompleted();
    }

}
