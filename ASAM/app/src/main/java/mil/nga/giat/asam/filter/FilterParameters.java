package mil.nga.giat.asam.filter;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
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
    public String mSubregion;
    public String mReferenceNumber;
    public String mVictim;
    public String mAggressor;
    
    public static FilterParameters newInstance(FilterParameters parameters) {
        FilterParameters copy = new FilterParameters(parameters.mType);
        copy.mKeyword = parameters.mKeyword;
        copy.mTimeInterval = parameters.mTimeInterval;
        copy.mDateFrom = parameters.mDateFrom;
        copy.mDateTo = parameters.mDateTo;
        copy.mSubregion = parameters.mSubregion;
        copy.mReferenceNumber = parameters.mReferenceNumber;
        copy.mVictim = parameters.mVictim;
        copy.mAggressor = parameters.mAggressor;
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
                StringUtils.isBlank(mSubregion) &&
                StringUtils.isBlank(mReferenceNumber) &&
                StringUtils.isBlank(mVictim) &&
                StringUtils.isBlank(mAggressor);
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

    public String getParametersAsFormattedHtml() {
        Collection<String> partials = new ArrayList<String>();

        StringBuilder html = new StringBuilder();
        if (StringUtils.isNotBlank(mKeyword)) {
            partials.add(String.format("&nbsp;<b>Keyword:</b> %s", mKeyword));
        }
        if (mTimeInterval != null) {
            switch (mTimeInterval) {
                case 60:
                case 90:
                case 180:
                    partials.add(String.format("&nbsp;<b>Last:</b> %s days", mTimeInterval));
                    break;
                case 365:
                    partials.add("&nbsp;<b>Date</b>: last year");
                    break;
                case 1300:
                    partials.add("&nbsp;<b>Date</b>: last 5 years");
                    break;
            }
        }

        if (StringUtils.isNotBlank(mDateFrom) && StringUtils.isNotBlank(mDateTo)) {
            partials.add(String.format("&nbsp;<b>Date:</b> %s - %s", mDateFrom, mDateTo));
        } else if (StringUtils.isNotBlank(mDateTo)) {
            partials.add(String.format("&nbsp;<b>Date To:</b> %s", mDateTo));
        } else if (StringUtils.isNotBlank(mDateFrom)){
            partials.add(String.format("&nbsp;<b>Date From:</b> %s", mDateFrom));
        }

        if (StringUtils.isNotBlank(mSubregion)) {
            partials.add(String.format("&nbsp;<b>Subregion:</b> %s", mSubregion));
        }

        if (StringUtils.isNotBlank(mReferenceNumber)) {
            partials.add(String.format("&nbsp;<b>Reference Number:</b> %s", mReferenceNumber));
        }
        if (StringUtils.isNotBlank(mVictim)) {
            partials.add(String.format("&nbsp;<b>Victim:</b> %s", mVictim));
        }
        if (StringUtils.isNotBlank(mAggressor)) {
            partials.add(String.format("&nbsp;<b>Aggressor:</b> %s", mAggressor));
        }

        return "<br>" + StringUtils.join(partials, "<br>");
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
        dest.writeString(mSubregion);
        dest.writeString(mReferenceNumber);
        dest.writeString(mVictim);
        dest.writeString(mAggressor);
    }

    private FilterParameters(Parcel in) {
        mType = Type.valueOf(in.readString());
        mTimeInterval = (Integer) in.readValue(Integer.class.getClassLoader());
        mKeyword = in.readString();
        mDateFrom = in.readString();
        mDateTo = in.readString();
        mSubregion = in.readString();
        mReferenceNumber = in.readString();
        mVictim = in.readString();
        mAggressor = in.readString();
    }
}
