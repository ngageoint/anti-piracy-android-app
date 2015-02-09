package mil.nga.giat.asam.util;

import java.text.ParseException;
import java.util.Date;

import mil.nga.giat.asam.db.AsamDbHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class SyncTime {

    public static boolean isSynched(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastSyncTime = preferences.getString(AsamConstants.LAST_SYNC_TIME, "");
        if (AsamUtils.isEmpty(lastSyncTime)) {
            return false;
        }
        Date today = truncateDate(new Date());
        Date sync = null;
        try {
            sync = AsamDbHelper.SQLITE_DATE_FORMAT.parse(lastSyncTime);
        }
        catch (ParseException caught) {
            AsamLog.e("Error parsing date: " + sync, caught);
            return false;
        }
        sync = truncateDate(sync);
        if (sync.before(today)) {
            return false;
        }
        return true;
    }
    
    public static void finishedSync(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AsamConstants.LAST_SYNC_TIME, AsamDbHelper.SQLITE_DATE_FORMAT.format(truncateDate(new Date())));
        editor.commit();
    }
    
    public static String getLastSyncTimeAsText(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(AsamConstants.LAST_SYNC_TIME, "N/A");
    }
    
    private static Date truncateDate(Date date) {
        try {
            return AsamDbHelper.SQLITE_DATE_FORMAT.parse(AsamDbHelper.SQLITE_DATE_FORMAT.format(date));
        }
        catch (ParseException caught) {
            AsamLog.e("Error parsing date: " + date, caught);
        }
        return date;
    }
}
