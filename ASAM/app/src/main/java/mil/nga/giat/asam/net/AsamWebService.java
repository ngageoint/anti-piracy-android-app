package mil.nga.giat.asam.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.AsamUtils;
import mil.nga.giat.asam.util.ManifestMetaData;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class AsamWebService {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.US);
    
    private Context mContext;
    
    public AsamWebService(Context context) {
        mContext = context;
    }

    public String query() throws IOException {
        AsamLog.i(AsamWebService.class.getName() + ":AsamWebService->query");
        String results = null;
        Date maxDate = null;
        AsamDbHelper dbHelper = new AsamDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        maxDate = dbHelper.getMaxOccurrenceDate(db);
        db.close();

        Calendar c = Calendar.getInstance();
        c.setTime(maxDate);
        c.add(Calendar.DAY_OF_YEAR, -60);

        URL url = new URL(String.format(ManifestMetaData.getString(mContext, "web_service_url"), DATE_FORMAT.format(c.getTime()), DATE_FORMAT.format(new Date())));
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(AsamConstants.QUERY_TIMEOUT_THRESHOLD_IN_MILLISECONDS);


        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            results = AsamUtils.readStream(in);
        } catch (IOException e) {
            InputStream err = urlConnection.getErrorStream();
            results = AsamUtils.readStream(err);
            throw new IOException(results);
        } finally {

            if (urlConnection != null) {
                Log.d("query", "response code: " + urlConnection.getResponseCode());
                urlConnection.disconnect();
            }
        }

        return results;
    }
}
