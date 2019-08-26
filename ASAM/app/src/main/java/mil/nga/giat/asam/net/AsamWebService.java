package mil.nga.giat.asam.net;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.model.AsamJsonParser;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.ManifestMetaData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AsamWebService {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.US);

    public final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(AsamConstants.QUERY_TIMEOUT_THRESHOLD_IN_SECONDS, TimeUnit.SECONDS)
            .readTimeout(AsamConstants.QUERY_TIMEOUT_THRESHOLD_IN_SECONDS, TimeUnit.SECONDS)
            .build();

    private Context mContext;
    
    public AsamWebService(Context context) {
        mContext = context;
    }

    public List<AsamBean> query() throws IOException {
        List<AsamBean> asams = Collections.emptyList();
        Date maxDate;
        AsamDbHelper dbHelper = new AsamDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        maxDate = dbHelper.getMaxOccurrenceDate(db);
        db.close();

        String url = String.format(ManifestMetaData.getString(mContext, "web_service_url"), DATE_FORMAT.format(maxDate), DATE_FORMAT.format(new Date()));
        AsamLog.i(AsamWebService.class.getName() + ":" + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response  = client.newCall(request).execute();
        if (response.isSuccessful()) {
            ResponseBody body = response.body();

            if (body != null) {
                InputStream is = body.byteStream();
                asams = new AsamJsonParser().parseJson(is);
                is.close();
            }
        }

        return asams;
    }
}
