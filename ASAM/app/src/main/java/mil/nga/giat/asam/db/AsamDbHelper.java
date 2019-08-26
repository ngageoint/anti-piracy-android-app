package mil.nga.giat.asam.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mil.nga.giat.asam.filter.FilterParameters;
import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.model.AsamJsonParser;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.SyncTime;


@SuppressLint("SdCardPath")
public class AsamDbHelper extends SQLiteOpenHelper {

    public static final SimpleDateFormat SQLITE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    public static final SimpleDateFormat TEXT_QUERY_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    private static final String DB_PATH = "/data/data/mil.nga.giat.asam/databases/";
    private static final String DB_NAME = "asams.db";
    private static final int DB_VERSION = 2;
    public static final String TABLE_NAME = "asams";
    public static final String ID = BaseColumns._ID;
    public static final String DATE_OF_OCCURRENCE = "date_of_occurrence";
    public static final String REFERENCE_NUMBER = "reference_number";
    public static final String NAV_AREA = "nav_area";
    public static final String SUBREGION = "subregion";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String HOSTILITY = "hostility";
    public static final String VICTIM = "victim";
    public static final String DESCRIPTION = "description";
    private Context mContext;
    
    public AsamDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        AsamLog.i(AsamDbHelper.class.getName() + ":onCreate");

        create(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AsamLog.i(AsamDbHelper.class.getName() + ":onUpgrade");

        destroy(db);
        create(db);
    }

    private void create(SQLiteDatabase db) {
        String table = "CREATE TABLE " + TABLE_NAME + " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DATE_OF_OCCURRENCE + " DATE, " +
                REFERENCE_NUMBER + " TEXT, " +
                SUBREGION + " TEXT, " +
                NAV_AREA + " TEXT, " +
                LATITUDE + " REAL, " +
                LONGITUDE + " REAL, " +
                HOSTILITY + " TEXT, " +
                VICTIM + " TEXT, " +
                DESCRIPTION + " TEXT)";

        db.execSQL(table);

        String dateIndex = "CREATE INDEX date_of_occurrence_IDX ON " + TABLE_NAME + "(" + DATE_OF_OCCURRENCE + ")";
        db.execSQL(dateIndex);

        String referenceIndex = "CREATE INDEX reference_number_IDX ON " + TABLE_NAME + "(" + REFERENCE_NUMBER + ")";
        db.execSQL(referenceIndex);
    }

    private void destroy(SQLiteDatabase db) {
        String drop = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(drop);

        SyncTime.removeSync(mContext);
    }

    private void seed(SQLiteDatabase db) {
        InputStream is = null;

        try {
            is = mContext.getAssets().open("asam_seed.json");
            List<AsamBean> asams = new AsamJsonParser().parseJson(is);
            insertAsams(db, asams);
        } catch (Exception e) {
            AsamLog.e("Error seeding database", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ignore) {
            }
        }

    }
    
    public void insertAsams(SQLiteDatabase db, List<AsamBean> asams) {
        AsamLog.i(AsamDbHelper.class.getName() + ":Entering insertAsams");
        db.beginTransaction();
        try {
            String columns = StringUtils.join(new String[] {DATE_OF_OCCURRENCE, REFERENCE_NUMBER, SUBREGION, NAV_AREA, LATITUDE, LONGITUDE, HOSTILITY, VICTIM, DESCRIPTION}, ", ");
            StringBuilder sql = new StringBuilder("INSERT INTO ").append(TABLE_NAME)
                .append("(").append(columns).append(")")
                .append(" VALUES ").append("(?, ?, ?, ?, ?, ?, ?, ?, ?)");

            SQLiteStatement pstmt = db.compileStatement(sql.toString());
            for (AsamBean asam : asams) {
                bindString(pstmt, 1, (asam.getOccurrenceDate() == null) ? null : SQLITE_DATE_FORMAT.format(asam.getOccurrenceDate()));
                bindString(pstmt, 2, asam.getReferenceNumber());
                bindString(pstmt, 3, asam.getGeographicalSubregion());
                bindString(pstmt, 4, asam.getNavArea());
                pstmt.bindDouble(5, asam.getLatitude());
                pstmt.bindDouble(6, asam.getLongitude());
                bindString(pstmt, 7, asam.getHostility());
                bindString(pstmt, 8, asam.getVictim());
                bindString(pstmt, 9, asam.getDescription());

                String foo = pstmt.toString();
                pstmt.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        AsamLog.i(AsamDbHelper.class.getName() + ":Exiting insertAsams");
    }
    
    public List<AsamBean> removeDuplicates(SQLiteDatabase db, List<AsamBean> asams) {
        AsamLog.i(AsamDbHelper.class.getName() + ":Entering removeDuplicates");
        List<AsamBean> asamsNotInDB = new ArrayList<AsamBean>();
        db.beginTransaction();
        try {
            String sql = "SELECT COUNT(*)" +
                         " FROM " +
                         TABLE_NAME +
                         " WHERE " +
                         REFERENCE_NUMBER + " = ?";
            AsamLog.i(sql);
            SQLiteStatement pstmt = db.compileStatement(sql);
            for (AsamBean asam : asams) {
                bindString(pstmt, 1, asam.getReferenceNumber());
                long result = pstmt.simpleQueryForLong();
                if (result == 0) {
                    asamsNotInDB.add(asam);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        AsamLog.i(AsamDbHelper.class.getName() + ":Exiting removeDuplicates");
        return asamsNotInDB;
    }

    public long count(SQLiteDatabase db) {
        SQLiteStatement statement = db.compileStatement( "SELECT count(*) FROM " + TABLE_NAME );
        return statement.simpleQueryForLong();
    }

    public List<AsamBean> queryWithFilters(SQLiteDatabase db, FilterParameters filterParameters) {
        return filterParameters.mType == FilterParameters.Type.SIMPLE ?
            simpleFilter(db, filterParameters) :
            advancedFilter(db, filterParameters);
    }

    private List<AsamBean> simpleFilter(SQLiteDatabase db, FilterParameters filterParameters) {
        Collection<String> clauses = new ArrayList<>();
        if (StringUtils.isNotBlank(filterParameters.mKeyword)) {
            List<String> textClauses = new ArrayList<>();
            textClauses.add("LOWER(" + AsamDbHelper.VICTIM + ") LIKE '%" + filterParameters.mKeyword.toLowerCase(Locale.US) + "%'");
            textClauses.add("LOWER(" + AsamDbHelper.HOSTILITY + ") LIKE '%" + filterParameters.mKeyword.toLowerCase(Locale.US) + "%'");
            if (StringUtils.isNumeric(filterParameters.mKeyword)) {
                textClauses.add(AsamDbHelper.SUBREGION + " == " + filterParameters.mKeyword);
            }
            textClauses.add(AsamDbHelper.REFERENCE_NUMBER + " == '" + filterParameters.mKeyword + "'");
            textClauses.add("LOWER(" + AsamDbHelper.DESCRIPTION + ") LIKE '%" + filterParameters.mKeyword.toLowerCase(Locale.US) + "%'");


            clauses.add("(" + StringUtils.join(textClauses, " OR ") + ")");
        }

        // From and to dates.
        List<String> dateClauses = new ArrayList<>();
        if (StringUtils.isNotBlank(filterParameters.mDateFrom)) {
            try {
                dateClauses.add(AsamDbHelper.DATE_OF_OCCURRENCE + " >= '" + AsamDbHelper.SQLITE_DATE_FORMAT.format(TEXT_QUERY_DATE_FORMAT.parse(filterParameters.mDateFrom).getTime()) + "'");
            } catch (ParseException caught) {
                AsamLog.e(AsamDbHelper.class.getName() + ":" + caught.getMessage(), caught);
            }
        }
        if (StringUtils.isNotBlank(filterParameters.mDateTo)) {
            try {
                dateClauses.add(AsamDbHelper.DATE_OF_OCCURRENCE + " <= '" + AsamDbHelper.SQLITE_DATE_FORMAT.format(TEXT_QUERY_DATE_FORMAT.parse(filterParameters.mDateTo).getTime()) + "'");
            } catch (ParseException caught) {
                AsamLog.e(AsamDbHelper.class.getName() + ":" + caught.getMessage(), caught);
            }
        }

        if (!dateClauses.isEmpty()) {
            clauses.add("(" + StringUtils.join(dateClauses, " AND ") + ")");
        }

        Collection<String> subregionPlaceholders = new ArrayList<String>(filterParameters.mSubregionIds.size());
        Collection<String> subregionParameters = new ArrayList<String>(filterParameters.mSubregionIds.size());
        for (Integer subregionId : filterParameters.mSubregionIds) {
            subregionPlaceholders.add("?");
            subregionParameters.add(subregionId.toString());
        }

        if (!subregionParameters.isEmpty()) {
            StringBuilder subregionQuery = new StringBuilder();
            subregionQuery.append(SUBREGION).append(" IN ( ").append(StringUtils.join(subregionPlaceholders, ",")).append(" )");
            clauses.add(subregionQuery.toString());
        }

        String columns = StringUtils.join(new String[] {ID, DATE_OF_OCCURRENCE, REFERENCE_NUMBER, NAV_AREA, SUBREGION, LATITUDE, LONGITUDE, HOSTILITY, VICTIM, DESCRIPTION}, ", ");
        StringBuilder sql = new StringBuilder("SELECT ").append(columns).append(" FROM ").append(TABLE_NAME);
        if (!clauses.isEmpty()) {
            sql.append(" WHERE ").append(StringUtils.join(clauses, " AND "));
        }

        try {
            db.beginTransaction();
            AsamLog.i(AsamDbHelper.class.getName() + ":" + sql);
            Cursor cursor = db.rawQuery(sql.toString(), new String[] {});
            List<AsamBean> asams = translate(cursor);
            cursor.close();
            db.setTransactionSuccessful();
            return asams;
        } finally {
            db.endTransaction();
        }
    }

    private List<AsamBean> advancedFilter(SQLiteDatabase db, FilterParameters filterParameters) {
        Collection<String> clauses = new ArrayList<String>();
        if (StringUtils.isNotBlank(filterParameters.mKeyword)) {
            List<String> textClauses = new ArrayList<String>();
            textClauses.add("LOWER(" + AsamDbHelper.VICTIM + ") LIKE '%" + filterParameters.mKeyword.toLowerCase(Locale.US) + "%'");
            textClauses.add("LOWER(" + AsamDbHelper.HOSTILITY + ") LIKE '%" + filterParameters.mKeyword.toLowerCase(Locale.US) + "%'");
            if (StringUtils.isNumeric(filterParameters.mKeyword)) {
                textClauses.add(AsamDbHelper.SUBREGION + " == " + filterParameters.mKeyword);
            }
            textClauses.add(AsamDbHelper.REFERENCE_NUMBER + " == '" + filterParameters.mKeyword + "'");
            textClauses.add("LOWER(" + AsamDbHelper.DESCRIPTION + ") LIKE '%" + filterParameters.mKeyword.toLowerCase(Locale.US) + "%'");


            clauses.add("(" + StringUtils.join(textClauses, " OR ") + ")");
        }

        List<String> whereClauses = new ArrayList<String>();
        // From and to dates.
        if (StringUtils.isNotBlank(filterParameters.mDateFrom)) {
            try {
                whereClauses.add(AsamDbHelper.DATE_OF_OCCURRENCE + " >= '" + AsamDbHelper.SQLITE_DATE_FORMAT.format(TEXT_QUERY_DATE_FORMAT.parse(filterParameters.mDateFrom).getTime()) + "'");
            } catch (ParseException caught) {
                AsamLog.e(AsamDbHelper.class.getName() + ":" + caught.getMessage(), caught);
            }
        }
        if (StringUtils.isNotBlank(filterParameters.mDateTo)) {
            try {
                whereClauses.add(AsamDbHelper.DATE_OF_OCCURRENCE + " <= '" + AsamDbHelper.SQLITE_DATE_FORMAT.format(TEXT_QUERY_DATE_FORMAT.parse(filterParameters.mDateTo).getTime()) + "'");
            } catch (ParseException caught) {
                AsamLog.e(AsamDbHelper.class.getName() + ":" + caught.getMessage(), caught);
            }
        }
        
        if (StringUtils.isNotBlank(filterParameters.mVictim)) {
            whereClauses.add("LOWER(" + AsamDbHelper.VICTIM + ") LIKE '%" + filterParameters.mVictim.toLowerCase(Locale.US) + "%'");
        }
        
        if (StringUtils.isNotBlank(filterParameters.mHostility)) {
            whereClauses.add("LOWER(" + AsamDbHelper.HOSTILITY + ") LIKE '%" + filterParameters.mHostility.toLowerCase(Locale.US) + "%'");
        }

        Collection<String> subregionPlaceholders = new ArrayList<String>(filterParameters.mSubregionIds.size());
        Collection<String> subregionParameters = new ArrayList<String>(filterParameters.mSubregionIds.size());
        for (Integer subregionId : filterParameters.mSubregionIds) {
            subregionPlaceholders.add("?");
            subregionParameters.add(subregionId.toString());
        }

        if (!subregionParameters.isEmpty()) {
            StringBuilder subregionQuery = new StringBuilder();
            subregionQuery.append(SUBREGION).append(" IN ( ").append(StringUtils.join(subregionPlaceholders, ",")).append(" )");
            whereClauses.add(subregionQuery.toString());
        }
        
        if (StringUtils.isNotBlank(filterParameters.mReferenceNumber)) {
            whereClauses.add(AsamDbHelper.REFERENCE_NUMBER + " == '" + filterParameters.mReferenceNumber + "'");
        }

        String columns = StringUtils.join(new String[] {ID, DATE_OF_OCCURRENCE, REFERENCE_NUMBER, NAV_AREA, SUBREGION, LATITUDE, LONGITUDE, HOSTILITY, VICTIM, DESCRIPTION}, ", ");
        StringBuilder sql = new StringBuilder("SELECT ").append(columns).append(" FROM ").append(TABLE_NAME);

        if (whereClauses.size() > 0) {
            clauses.add("(" + StringUtils.join(whereClauses, " AND ") + ")");
        }

        if (!clauses.isEmpty()) {
            sql.append(" WHERE ").append(StringUtils.join(clauses, " AND "));
        }
        
        try {
            db.beginTransaction();
            AsamLog.i(AsamDbHelper.class.getName() + ":" + sql);
            Cursor cursor = db.rawQuery(sql.toString(), subregionParameters.toArray(new String[subregionParameters.size()]));
            List<AsamBean> asams = translate(cursor);
            cursor.close();
            db.setTransactionSuccessful();
            return asams;
        } finally {
            db.endTransaction();
        }
    }

    public Date getMaxOccurrenceDate(SQLiteDatabase db) {
        Date maxDate = new Date();
        db.beginTransaction();
        try {
            String sql = "SELECT " +
                         "MAX(" + DATE_OF_OCCURRENCE + ")" +
                         " FROM " +
                         TABLE_NAME;
            AsamLog.i(AsamDbHelper.class.getName() + ":" + sql);
            SQLiteStatement pstmt = db.compileStatement(sql);
            String dateOfOccurrence = pstmt.simpleQueryForString();
            if (StringUtils.isNotBlank(dateOfOccurrence)) {
                try {
                    maxDate = SQLITE_DATE_FORMAT.parse(dateOfOccurrence);
                } catch (ParseException caught) {
                    AsamLog.e(AsamDbHelper.class.getName() + ":Error finding max date", caught);
                }
            }
            db.setTransactionSuccessful();
            return maxDate;
        } finally {
            db.endTransaction();
        }
    }
    
    public Date getMinOccurrenceDate(SQLiteDatabase db) {
        Date maxDate = new Date();
        db.beginTransaction();
        try {
            String sql = "SELECT " +
                         "MIN(" + DATE_OF_OCCURRENCE + ")" +
                         " FROM " +
                         TABLE_NAME;
            AsamLog.i(AsamDbHelper.class.getName() + ":" + sql);
            SQLiteStatement pstmt = db.compileStatement(sql);
            String dateOfOccurrence = pstmt.simpleQueryForString();
            if (StringUtils.isNotBlank(dateOfOccurrence)) {
                try {
                    maxDate = SQLITE_DATE_FORMAT.parse(dateOfOccurrence);
                }
                catch (ParseException caught) {
                    AsamLog.e(AsamDbHelper.class.getName() + ":Error finding min date", caught);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return maxDate;
    }
    
    public long getTotalNumberOfAsams(SQLiteDatabase db) {
        long totalNumberOfAsams = 0;
        db.beginTransaction();
        try {
            String sql = "SELECT " +
                         "COUNT(*)" +
                         " FROM " +
                         TABLE_NAME;
            AsamLog.i(AsamDbHelper.class.getName() + ":" + sql);
            SQLiteStatement pstmt = db.compileStatement(sql);
            totalNumberOfAsams = pstmt.simpleQueryForLong();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return totalNumberOfAsams;
    }
    
    private void bindString(SQLiteStatement pstmt, int index, String value) {
        if (value == null) {
            pstmt.bindNull(index);
        } else {
            pstmt.bindString(index, value);
        }
    }

    private List<AsamBean> translate(Cursor cursor) {
        List<AsamBean> asams = new ArrayList<>();

        while (cursor.moveToNext()) {
            AsamBean asam = new AsamBean();
            try {
                asam.setId(cursor.getInt(cursor.getColumnIndex(AsamDbHelper.ID)));
                asam.setOccurrenceDate(AsamDbHelper.SQLITE_DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(AsamDbHelper.DATE_OF_OCCURRENCE))));
                asam.setReferenceNumber(cursor.getString(cursor.getColumnIndex(AsamDbHelper.REFERENCE_NUMBER)));
                asam.setNavArea(cursor.getString(cursor.getColumnIndex(AsamDbHelper.NAV_AREA)));
                asam.setGeographicalSubregion(cursor.getString(cursor.getColumnIndex(AsamDbHelper.SUBREGION)));
                asam.setLatitude(cursor.getDouble(cursor.getColumnIndex(AsamDbHelper.LATITUDE)));
                asam.setLongitude(cursor.getDouble(cursor.getColumnIndex(AsamDbHelper.LONGITUDE)));
                asam.setHostility(cursor.getString(cursor.getColumnIndex(AsamDbHelper.HOSTILITY)));
                asam.setVictim(cursor.getString(cursor.getColumnIndex(AsamDbHelper.VICTIM)));
                asam.setDescription(cursor.getString(cursor.getColumnIndex(AsamDbHelper.DESCRIPTION)));
                asams.add(asam);
            }
            catch (Exception caught) {
                AsamLog.e(AsamDbHelper.class.getName() + ":Error querying ASAMs", caught);
            }
        }

        return asams;
    }
}
