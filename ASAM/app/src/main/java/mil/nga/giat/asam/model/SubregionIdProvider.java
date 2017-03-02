package mil.nga.giat.asam.model;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.List;

public class SubregionIdProvider extends ContentProvider {

    // use hashset to prevent duplicates
    HashSet<String> subregionIds;

    @Override
    public boolean onCreate() {
        subregionIds = new HashSet<>();

        // Initialize subregions and add subregions polygons on the map.
        List<SubregionBean> mSubregions = new SubregionTextParser().parseSubregions(getContext());
        for (SubregionBean subregion : mSubregions) {
            subregionIds.add(String.valueOf(subregion.getSubregionId()));
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        MatrixCursor cursor = new MatrixCursor(
                new String[] {
                        BaseColumns._ID,
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA

                }
        );
        if (subregionIds != null) {
            int count = 0;
            String query = uri.getLastPathSegment();
            for (String subRegion : subregionIds) {
                if (subRegion.contains(query)){
                    cursor.addRow(new Object[]{ count, subRegion, subRegion });
                }
                count++;
            }
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
