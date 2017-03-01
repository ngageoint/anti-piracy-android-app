package mil.nga.giat.asam;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import mil.nga.giat.asam.model.AsamInputAdapter;

/**
 * Created by wnewman on 5/26/15.
 */
public class PullAsamsTask extends AsyncTask<Void, Void, Error> {

    public interface OnSyncResultListener {
        void onSyncCompleted();
        void onSyncFailed();
    }

    private Context context;
    private ProgressDialog progressDialog;
    private OnSyncResultListener onSyncCompletedListener;
    private AsamInputAdapter asamIA;


    public PullAsamsTask(Context context, OnSyncResultListener onSyncCompletedListener) {
        this.context = context;
        this.onSyncCompletedListener = onSyncCompletedListener;
        this.asamIA = new AsamInputAdapter(this.context);
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
    protected Error doInBackground(Void... params) {
        return asamIA.run();
    }

    @Override
    protected void onPostExecute(Error error) {
        progressDialog.dismiss();
        if (error != null) {
            Log.e("PullAsamsTask", error.getMessage());
            onSyncCompletedListener.onSyncFailed();
        } else {
            onSyncCompletedListener.onSyncCompleted();
        }
    }

}
