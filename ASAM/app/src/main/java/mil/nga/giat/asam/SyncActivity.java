package mil.nga.giat.asam;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.map.AsamMapActivity;
import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.model.AsamJsonParser;
import mil.nga.giat.asam.net.AsamWebService;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.SyncTime;

public class SyncActivity extends AppCompatActivity {

    private Executor backgroundExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sync);

        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                seedDatabase();
                sync();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        launch();
                    }
                });
            }
        });
    }

    private void launch() {
        startActivity(new Intent(this, AsamMapActivity.class));
        finish();
    }

    private void seedDatabase() {
        SQLiteDatabase db = null;
        InputStream is = null;
        try {
            AsamDbHelper dbHelper = new AsamDbHelper(getApplicationContext());
            db = dbHelper.getReadableDatabase();
            long count = dbHelper.count(db);
            if (count == 0) {
                is = getAssets().open("asam_seed.json");
                List<AsamBean> asams = new AsamJsonParser().parseJson(is);
                dbHelper.insertAsams(db, asams);
            }
        } catch (Exception e) {
            AsamLog.e("Error seeding database", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ignore) {
            }

            if (db != null) {
                db.close();
            }
        }
    }

    private void sync() {
        SQLiteDatabase db = null;

        try {
            AsamWebService webService = new AsamWebService(getApplicationContext());
            List<AsamBean> asams  = webService.query();
            if (asams.size() > 0) {
                // Do a diff of what the web service returned and what's currently in the db.
                AsamDbHelper dbHelper = new AsamDbHelper(getApplicationContext());
                db = dbHelper.getWritableDatabase();
                asams = dbHelper.removeDuplicates(db, asams);
                dbHelper.insertAsams(db, asams);
            }

            SyncTime.finishedSync(getApplicationContext());
        } catch (Exception caught) {
            AsamLog.e(AsamMapActivity.class.getName() + ":There was an error parsing ASAM feed", caught);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
