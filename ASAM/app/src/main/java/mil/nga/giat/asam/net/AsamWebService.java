package mil.nga.giat.asam.net;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.AsamUtils;
import mil.nga.giat.asam.util.ManifestMetaData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AsamWebService {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.US);
    
    private Context mContext;
    
    public AsamWebService(Context context) {
        mContext = context;
    }

    private static OkHttpClient httpClient;

    static {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(AsamConstants.QUERY_TIMEOUT_THRESHOLD_IN_MILLISECONDS, TimeUnit.MILLISECONDS)
                .readTimeout(AsamConstants.QUERY_TIMEOUT_THRESHOLD_IN_MILLISECONDS, TimeUnit.MILLISECONDS)
                .build();
    }
    
    public String query() throws IOException {
        String results = null;
        AsamDbHelper dbHelper = new AsamDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Date maxDate = dbHelper.getMaxOccurrenceDate(db);
        db.close();
        
        String url = String.format(ManifestMetaData.getString(mContext, "web_service_url"), DATE_FORMAT.format(maxDate), DATE_FORMAT.format(new Date()));
        AsamLog.i(AsamWebService.class.getName() + ":" + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = httpClient.newCall(request).execute();

        if (response != null && response.isSuccessful()) {
            InputStream in = response.body().byteStream();
            results = AsamUtils.readStream(in);
            in.close();
        }

        return results;
    }
}
