package mil.nga.giat.asam.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

import mil.nga.giat.asam.net.AsamWebService;
import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.SyncTime;

/**
 * AsamInputAdapter handles the retrieval, parsing, and storage of ASAMs
 */
public class AsamInputAdapter {

    private Context ctx;
    private AsamJsonParser parser;

    public AsamInputAdapter(Context ctx) {
        this.ctx = ctx;
        parser = new AsamJsonParser();
    }

    public Error run() {
        Error error = null;
        SQLiteDatabase db = null;
        try {
            String json = requestAsamData();
            if (StringUtils.isNotBlank(json)) {
                List<AsamBean> asams = parser.parseJson(json);
                if (asams.size() > 0) {

                    // Do a diff of what the web service returned and what's currently in the db.
                    AsamDbHelper dbHelper = new AsamDbHelper(ctx);
                    db = dbHelper.getWritableDatabase();
                    asams = dbHelper.removeDuplicates(db, asams);
                    dbHelper.insertAsams(db, asams);
                } else {
                    error = new Error("No ASAMs in response");
                }
            }
            SyncTime.finishedSync(ctx);
        } catch (Exception caught) {
            AsamLog.e("There was an error parsing ASAM feed", caught);
            error = new Error("Error retrieving ASAMs.");
        } finally {
            if (db != null) {
                db.close();
                db = null;
            }
        }
        return error;
    }

    /**
     *
     * @return
     * @throws IOException
     */
    private String requestAsamData() throws IOException {
        AsamWebService webService = new AsamWebService(ctx);
        return webService.query();
    }
}
