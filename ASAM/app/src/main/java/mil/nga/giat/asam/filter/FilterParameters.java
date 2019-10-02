package mil.nga.giat.asam.filter;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FilterParameters implements Parcelable {

    private static String FILTER_TYPE_KEY = "FILTER_TYPE";
    private static String FILTER_KEYWORD_KEY = "FILTER_KEYWORD";
    private static String FILTER_TIME_INTERVAL_KEY = "FILTER_TIME_INTERVAL";
    private static String FILTER_DATE_FROM_KEY = "FILTER_DATE_FROM";
    private static String FILTER_DATE_TO_KEY = "FILTER_DATE_TO";
    private static String FILTER_SUBREGION_IDS_KEY = "FILTER_SUBREGION_IDS";
    private static String FILTER_REFERENCE_NUMBER_KEY = "FILTER_REFERENCE_NUMBER";
    private static String FILTER_VICTIM_KEY = "FILTER_VICTIM";
    private static String FILTER_HOSTILITY_KEY = "FILTER_HOSTILITY";

    public enum Type {
        SIMPLE,
        ADVANCED
    }

    public Type mType;

    public String mKeyword;
    public Integer mTimeInterval;

    public String mDateFrom;
    public String mDateTo;
    public ArrayList<Integer> mSubregionIds = new ArrayList<>();
    public String mReferenceNumber;
    public String mVictim;
    public String mHostility;

    public static FilterParameters newInstance(SharedPreferences preferences) {
        FilterParameters filterParameters = new FilterParameters(Type.SIMPLE);

        String type = preferences.getString(FILTER_TYPE_KEY, null);
        if (type != null) {
            filterParameters.mType = Type.valueOf(type);
        }

        int timeInterval = preferences.getInt(FILTER_TIME_INTERVAL_KEY, -1);
        if (timeInterval != -1) {
            filterParameters.mTimeInterval = timeInterval;
        }

        filterParameters.mKeyword = preferences.getString(FILTER_KEYWORD_KEY, null);
        filterParameters.mTimeInterval = preferences.getInt(FILTER_TIME_INTERVAL_KEY, 0);
        filterParameters.mDateFrom = preferences.getString(FILTER_DATE_FROM_KEY, null);
        filterParameters.mDateTo = preferences.getString(FILTER_DATE_TO_KEY, null);
        filterParameters.mReferenceNumber = preferences.getString(FILTER_REFERENCE_NUMBER_KEY, null);
        filterParameters.mVictim = preferences.getString(FILTER_VICTIM_KEY, null);
        filterParameters.mHostility = preferences.getString(FILTER_HOSTILITY_KEY, null);

        Set<String> subregions = preferences.getStringSet(FILTER_SUBREGION_IDS_KEY, Collections.<String>emptySet());
        for (String subregion : subregions) {
            filterParameters.mSubregionIds.add(Integer.parseInt(subregion));
        }

        return filterParameters;
    }
    
    public static FilterParameters newInstance(FilterParameters parameters) {
        FilterParameters copy = new FilterParameters(parameters.mType);
        copy.mKeyword = parameters.mKeyword;
        copy.mTimeInterval = parameters.mTimeInterval;
        copy.mDateFrom = parameters.mDateFrom;
        copy.mDateTo = parameters.mDateTo;
        copy.mSubregionIds = parameters.mSubregionIds;
        copy.mReferenceNumber = parameters.mReferenceNumber;
        copy.mVictim = parameters.mVictim;
        copy.mHostility = parameters.mHostility;
        return copy;
    }

    public FilterParameters(Type type) {
        this.mType = type;
    }

    public void save(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(FILTER_TYPE_KEY, mType.name());
        editor.putString(FILTER_KEYWORD_KEY, mKeyword);
        editor.putString(FILTER_DATE_FROM_KEY, mDateFrom);
        editor.putString(FILTER_DATE_TO_KEY, mDateTo);
        editor.putString(FILTER_REFERENCE_NUMBER_KEY, mReferenceNumber);
        editor.putString(FILTER_VICTIM_KEY, mVictim);
        editor.putString(FILTER_HOSTILITY_KEY, mHostility);

        if (mTimeInterval != null) {
            editor.putInt(FILTER_TIME_INTERVAL_KEY, mTimeInterval);
        } else {
            editor.remove(FILTER_TIME_INTERVAL_KEY);
        }

        Set<String> subregions = new HashSet<>();
        for (Integer subregion : mSubregionIds) {
            subregions.add(subregion.toString());
        }
        editor.putStringSet(FILTER_SUBREGION_IDS_KEY, subregions);

        editor.apply();
    }
    
    public boolean isEmpty() {
        return StringUtils.isBlank(mKeyword) &&
                (mTimeInterval == null || mTimeInterval == 0) &&
                StringUtils.isBlank(mDateFrom) &&
                StringUtils.isBlank(mDateTo) &&
                mSubregionIds.isEmpty() &&
                StringUtils.isBlank(mReferenceNumber) &&
                StringUtils.isBlank(mVictim) &&
                StringUtils.isBlank(mHostility);
    }

    public Date getStartDateFromInterval() {
        if (mTimeInterval == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();

        switch (mTimeInterval) {
            case 60: {
                calendar.add(Calendar.DAY_OF_MONTH, mTimeInterval * -1);
                return calendar.getTime();
            }
            case 90: {
                calendar.add(Calendar.DAY_OF_MONTH, mTimeInterval * -1);
                return calendar.getTime();
            }
            case 180: {
                calendar.add(Calendar.DAY_OF_MONTH, mTimeInterval * -1);
                return calendar.getTime();
            }
            case 365: {
                calendar.add(Calendar.YEAR, -1);
                return calendar.getTime();
            }
            case 1300: {
                calendar.add(Calendar.YEAR, -5);
                return calendar.getTime();
            }
            default:
                return null;
        }
    }

    /**
     * Creator required for class implementing the parcelable interface.
     */
    public static final Parcelable.Creator<FilterParameters> CREATOR = new Creator<FilterParameters>() {

        @Override
        public FilterParameters createFromParcel(Parcel in) {
            return new FilterParameters(in);
        }

        @Override
        public FilterParameters[] newArray(int size) {
            return new FilterParameters[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mType.name());
        dest.writeValue(mTimeInterval);
        dest.writeString(mKeyword);
        dest.writeString(mDateFrom);
        dest.writeString(mDateTo);
        dest.writeSerializable(mSubregionIds);
        dest.writeString(mReferenceNumber);
        dest.writeString(mVictim);
        dest.writeString(mHostility);
    }

    private FilterParameters(Parcel in) {
        mType = Type.valueOf(in.readString());
        mTimeInterval = (Integer) in.readValue(Integer.class.getClassLoader());
        mKeyword = in.readString();
        mDateFrom = in.readString();
        mDateTo = in.readString();
        mSubregionIds = (ArrayList<Integer>) in.readSerializable();
        mReferenceNumber = in.readString();
        mVictim = in.readString();
        mHostility = in.readString();
    }
}
