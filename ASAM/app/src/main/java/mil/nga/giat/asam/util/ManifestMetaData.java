package mil.nga.giat.asam.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;


public class ManifestMetaData {

    private static Object getMetadata(Context context, String key) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = info.metaData;
            Object value = bundle.get(key);
            return value;
        }
        catch (NameNotFoundException caught) {
            caught.printStackTrace();
            AsamLog.e(caught.getMessage(), caught);
        }
        return null;
    }

    public static String getString(Context context, String key) {
        return (String)getMetadata(context, key);
    }

    public static int getInt(Context context, String key) {
        return (Integer)getMetadata(context, key);
    }

    public static Boolean getBoolean(Context context, String key) {
        return (Boolean)getMetadata(context, key);
    }

    public static Object get(Context context, String key) {
        return getMetadata(context, key);
    }
}
