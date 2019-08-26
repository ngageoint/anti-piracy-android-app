package mil.nga.giat.asam.filter;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class FilterParameters implements Parcelable {

    public enum Type {
        SIMPLE,
        ADVANCED;
    }

    public Type mType;

    public String mKeyword;
    public Integer mTimeInterval;

    public String mDateFrom;
    public String mDateTo;
    public ArrayList<Integer> mSubregionIds = new ArrayList<Integer>();
    public String mReferenceNumber;
    public String mVictim;
    public String mHostility;
    
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
