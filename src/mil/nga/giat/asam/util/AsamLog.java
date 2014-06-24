package mil.nga.giat.asam.util;

import android.util.Log;


public class AsamLog {

    private static final boolean LOGGING = false;
    public static final String TAG = "ASAM:";
    
    public static void i(String message) {
        if (LOGGING) {
            Log.i(TAG, message);
        }
    }
    
    public static void d(String message) {
        if (LOGGING) {
            Log.d(TAG, message);
        }
    }
    
    public static void v(String message) {
        if (LOGGING) {
            Log.v(TAG, message);
        }
    }
    
    public static void e(String message, Exception exception) {
        if (LOGGING) {
            Log.e(TAG, message, exception);
        }
    }
}
