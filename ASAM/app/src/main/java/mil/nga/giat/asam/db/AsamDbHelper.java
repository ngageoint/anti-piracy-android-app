package mil.nga.giat.asam.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import org.apache.commons.lang3.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mil.nga.giat.asam.filter.FilterParameters;
import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamLog;


@SuppressLint("SdCardPath")
public class AsamDbHelper extends SQLiteOpenHelper {

    public static final SimpleDateFormat SQLITE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    public static final SimpleDateFormat TEXT_QUERY_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    private static final String DB_PATH = "/data/data/mil.nga.giat.asam/databases/";
    private static final String DB_NAME = "asams.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "asams";
    public static final String ID = BaseColumns._ID;
    public static final String DATE_OF_OCCURRENCE = "date_of_occurrence";
    public static final String REFERENCE_NUMBER = "reference_number";
    public static final String SUBREGION = "subregion";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String AGGRESSOR = "aggressor";
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AsamLog.i(AsamDbHelper.class.getName() + ":onUpgrade");
    }
    
    public void initializeSeededAsamDb() throws IOException {
        
        // Create the DB directory.
        SQLiteDatabase db = getReadableDatabase();
        db.close();

        // Copy the database from the assets folder.
        InputStream in = null;
        OutputStream out = null;
        try {
            out = new FileOutputStream(DB_PATH + DB_NAME);
            byte[] buffer = new byte[1024];
            int bytesRead;
            String[] seededDbFiles = new String[] { "seeded_db_aa", "seeded_db_ab", "seeded_db_ac", "seeded_db_ad" };
            for (String fileName : seededDbFiles) {
                in = mContext.getAssets().open(fileName);
                while ((bytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
                in.close();
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public boolean doesSeededAsamDbExist() {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception caught) {

        }

        if (db != null) {
            db.close();
        }
        return db != null ? true : false;
    }
    
    public void insertAsams(SQLiteDatabase db, List<AsamBean> asams) {
        AsamLog.i(AsamDbHelper.class.getName() + ":Entering insertAsams");
        db.beginTransaction();
        try {
            String columns = StringUtils.join(new String[] {DATE_OF_OCCURRENCE, REFERENCE_NUMBER, SUBREGION, LATITUDE, LONGITUDE, AGGRESSOR, VICTIM, DESCRIPTION}, ", ");
            StringBuilder sql = new StringBuilder("INSERT INTO ").append(TABLE_NAME)
                .append("(").append(columns).append(")")
                .append(" VALUES ").append("(?, ?, ?, ?, ?, ?, ?, ?)");

            SQLiteStatement pstmt = db.compileStatement(sql.toString());
            for (AsamBean asam : asams) {
                bindString(pstmt, 1, (asam.getOccurrenceDate() == null) ? null : SQLITE_DATE_FORMAT.format(asam.getOccurrenceDate()));
                bindString(pstmt, 2, asam.getReferenceNumber());
                bindString(pstmt, 3, asam.getGeographicalSubregion());
                pstmt.bindDouble(4, asam.getLatitude().doubleValue());
                pstmt.bindDouble(5, asam.getLongitude().doubleValue());
                bindString(pstmt, 6, asam.getAggressor());
                bindString(pstmt, 7, asam.getVictim());
                bindString(pstmt, 8, asam.getDescription());
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

    public List<AsamBean> queryAll(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            String columns = StringUtils.join(new String[] {ID, DATE_OF_OCCURRENCE, REFERENCE_NUMBER, SUBREGION, LATITUDE, LONGITUDE, AGGRESSOR, VICTIM, DESCRIPTION}, ", ");
            StringBuilder sql = new StringBuilder("SELECT ").append(columns).append(" FROM ").append(TABLE_NAME);

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
    
    public List<AsamBean> queryByTime(SQLiteDatabase db, Calendar timePeriod) {
        db.beginTransaction();
        try {
            String columns = StringUtils.join(new String[] {ID, DATE_OF_OCCURRENCE, REFERENCE_NUMBER, SUBREGION, LATITUDE, LONGITUDE, AGGRESSOR, VICTIM, DESCRIPTION}, ", ");
            StringBuilder sql = new StringBuilder("SELECT ")
                    .append(columns).append(" FROM ").append(TABLE_NAME)
                    .append(" WHERE ").append(DATE_OF_OCCURRENCE).append(" >= '").append(SQLITE_DATE_FORMAT.format(timePeriod.getTime())).append("'");

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

    public List<AsamBean> queryWithFilters(SQLiteDatabase db, FilterParameters filterParameters) {
        return filterParameters.mType == FilterParameters.Type.SIMPLE ?
            simpleFilter(db, filterParameters) :
            advancedFilter(db, filterParameters);
    }

    private List<AsamBean> simpleFilter(SQLiteDatabase db, FilterParameters filterParameters) {
        Collection<String> clauses = new ArrayList<String>();
        String textClause = null;
        if (StringUtils.isNotBlank(filterParameters.mKeyword)) {
            List<String> textClauses = new ArrayList<String>();
            textClauses.add("LOWER(" + AsamDbHelper.VICTIM + ") LIKE '%" + filterParameters.mKeyword.toLowerCase(Locale.US) + "%'");
            textClauses.add("LOWER(" + AsamDbHelper.AGGRESSOR + ") LIKE '%" + filterParameters.mKeyword.toLowerCase(Locale.US) + "%'");
            if (StringUtils.isNumeric(filterParameters.mKeyword)) {
                textClauses.add(AsamDbHelper.SUBREGION + " == " + filterParameters.mKeyword);
            }
            textClauses.add(AsamDbHelper.REFERENCE_NUMBER + " == '" + filterParameters.mKeyword + "'");
            textClauses.add("LOWER(" + AsamDbHelper.DESCRIPTION + ") LIKE '%" + filterParameters.mKeyword.toLowerCase(Locale.US) + "%'");


            clauses.add("(" + StringUtils.join(textClauses, " OR ") + ")");
        }

        // From and to dates.
        List<String> dateClauses = new ArrayList<String>();
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

        String columns = StringUtils.join(new String[] {ID, DATE_OF_OCCURRENCE, REFERENCE_NUMBER, SUBREGION, LATITUDE, LONGITUDE, AGGRESSOR, VICTIM, DESCRIPTION}, ", ");
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

        String keywordClause = null;
        if (StringUtils.isNotBlank(filterParameters.mKeyword)) {
            List<String> textClauses = new ArrayList<String>();
            textClauses.add("LOWER(" + AsamDbHelper.VICTIM + ") LIKE '%" + filterParameters.mKeyword.toLowerCase(Locale.US) + "%'");
            textClauses.add("LOWER(" + AsamDbHelper.AGGRESSOR + ") LIKE '%" + filterParameters.mKeyword.toLowerCase(Locale.US) + "%'");
            if (StringUtils.isNumeric(filterParameters.mKeyword)) {
                textClauses.add(AsamDbHelper.SUBREGION + " == " + filterParameters.mKeyword);
            }
            textClauses.add(AsamDbHelper.REFERENCE_NUMBER + " == '" + filterParameters.mKeyword + "'");
            textClauses.add("LOWER(" + AsamDbHelper.DESCRIPTION + ") LIKE '%" + filterParameters.mKeyword.toLowerCase(Locale.US) + "%'");


           keywordClause = ("(" + StringUtils.join(textClauses, " OR ") + ")";
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
        
        if (StringUtils.isNotBlank(filterParameters.mAggressor)) {
            whereClauses.add("LOWER(" + AsamDbHelper.AGGRESSOR + ") LIKE '%" + filterParameters.mAggressor.toLowerCase(Locale.US) + "%'");
        }
        
        if (StringUtils.isNotBlank(filterParameters.mSubregion)) {
            whereClauses.add(AsamDbHelper.SUBREGION + " == " + filterParameters.mSubregion);
        }
        
        if (StringUtils.isNotBlank(filterParameters.mReferenceNumber)) {
            whereClauses.add(AsamDbHelper.REFERENCE_NUMBER + " == '" + filterParameters.mReferenceNumber + "'");
        }

        String columns = StringUtils.join(new String[] {ID, DATE_OF_OCCURRENCE, REFERENCE_NUMBER, SUBREGION, LATITUDE, LONGITUDE, AGGRESSOR, VICTIM, DESCRIPTION}, ", ");
        StringBuilder sql = new StringBuilder("SELECT ").append(columns).append(" FROM ").append(TABLE_NAME);
        if (whereClauses.size() > 0) {
            sql.append(" WHERE ").append(StringUtils.join(whereClauses, " AND "));
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
    
    public List<AsamBean> queryByTimeAndSubregions(SQLiteDatabase db, Calendar timePeriod, List<Integer> subregionIds) {
        db.beginTransaction();
        try {
            StringBuilder questionMarks = new StringBuilder("");
            String[] queryParameters = new String[subregionIds.size()];
            for (int i = 0; i < subregionIds.size(); i++) {
                queryParameters[i] = subregionIds.get(i).toString();
                questionMarks.append("?");
                if (i != subregionIds.size() - 1) {
                    questionMarks.append(", ");
                }
            }

            String columns = StringUtils.join(new String[] {ID, DATE_OF_OCCURRENCE, REFERENCE_NUMBER, SUBREGION, LATITUDE, LONGITUDE, AGGRESSOR, VICTIM, DESCRIPTION}, ", ");
            StringBuilder sql = new StringBuilder("SELECT ").append(columns).append(" FROM ").append(TABLE_NAME)
                    .append(" WHERE ")
                    .append(DATE_OF_OCCURRENCE).append(" >= '").append(SQLITE_DATE_FORMAT.format(timePeriod.getTime())).append("'")
                    .append(" AND ")
                    .append(SUBREGION).append(" IN ( ").append(questionMarks.toString()).append(" )");

            AsamLog.i(AsamDbHelper.class.getName() + ":" + sql);
            Cursor cursor = db.rawQuery(sql.toString(), queryParameters);
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
    
    public long getTotalNumberOfAsamsForSubregions(SQLiteDatabase db, List<Integer> subregionIds) {
        long totalNumberOfAsams = 0;
        db.beginTransaction();
        try {
            StringBuilder queryParameters = new StringBuilder("");
            for (int i = 0; i < subregionIds.size(); i++) {
                queryParameters.append(subregionIds.get(i) + "");
                if (i != subregionIds.size() - 1) {
                    queryParameters.append(", ");
                }
            }
            String sql = "SELECT " +
                         "COUNT(*)" +
                         " FROM " +
                         TABLE_NAME +
                         " WHERE " +
                         SUBREGION + " IN ( " + queryParameters.toString() + " )";
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
        List<AsamBean> asams = new ArrayList<AsamBean>();

        while (cursor.moveToNext()) {
            AsamBean asam = new AsamBean();
            try {
                asam.setId(cursor.getInt(cursor.getColumnIndex(AsamDbHelper.ID)));
                asam.setOccurrenceDate(AsamDbHelper.SQLITE_DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(AsamDbHelper.DATE_OF_OCCURRENCE))));
                asam.setReferenceNumber(cursor.getString(cursor.getColumnIndex(AsamDbHelper.REFERENCE_NUMBER)));
                asam.setGeographicalSubregion(cursor.getString(cursor.getColumnIndex(AsamDbHelper.SUBREGION)));
                asam.setLatitude(cursor.getDouble(cursor.getColumnIndex(AsamDbHelper.LATITUDE)));
                asam.setLongitude(cursor.getDouble(cursor.getColumnIndex(AsamDbHelper.LONGITUDE)));
                asam.setAggressor(cursor.getString(cursor.getColumnIndex(AsamDbHelper.AGGRESSOR)));
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
