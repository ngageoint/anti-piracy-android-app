package mil.nga.giat.asam.net;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.AsamUtils;
import mil.nga.giat.asam.util.ManifestMetaData;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


public class AsamWebService {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.US);
    
    private Context mContext;
    
    public AsamWebService(Context context) {
        mContext = context;
    }
    
    public String query() throws IOException {
        String results = null;
        Date maxDate = null;
        AsamDbHelper dbHelper = new AsamDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        maxDate = dbHelper.getMaxOccurrenceDate(db);
        db.close();
        
        String url = String.format(ManifestMetaData.getString(mContext, "web_service_url"), DATE_FORMAT.format(maxDate), DATE_FORMAT.format(new Date()));
        AsamLog.i(AsamWebService.class.getName() + ":" + url);
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, AsamConstants.QUERY_TIMEOUT_THRESHOLD_IN_MILLISECONDS);
        HttpConnectionParams.setSoTimeout(httpParameters, AsamConstants.QUERY_TIMEOUT_THRESHOLD_IN_MILLISECONDS);
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        HttpResponse response = httpClient.execute(new HttpGet(url));
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream in = entity.getContent();
            results = AsamUtils.readStream(in);
            in.close();
        }
        return results;
    }
}
