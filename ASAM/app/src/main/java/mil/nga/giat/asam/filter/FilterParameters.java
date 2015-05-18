package mil.nga.giat.asam.filter;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;

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
               mTimeInterval == null &&
                StringUtils.isBlank(mDateFrom) &&
                StringUtils.isBlank(mDateTo) &&
                StringUtils.isBlank(mSubregion) &&
                StringUtils.isBlank(mReferenceNumber) &&
                StringUtils.isBlank(mVictim) &&
                StringUtils.isBlank(mAggressor);
    }
    
    public String getParametersAsFormattedHtml() {
        StringBuilder html = new StringBuilder();
        if (StringUtils.isNotBlank(mKeyword)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Keyword:</b> %s", mKeyword));
        }
        if (mTimeInterval != null) {
            Calendar calendar = Calendar.getInstance();
            switch (mTimeInterval) {
                case 60:
                case 90:
                case 180:
                    html.append(String.format("<br/>&nbsp;&nbsp;- <b>Last:</b> %s days", mTimeInterval));
                    calendar.add(Calendar.DAY_OF_MONTH, mTimeInterval * -1);
                    break;
                case 365:
                    html.append("<br/>&nbsp;&nbsp;- <b>Date</b>: last year");
                    calendar.add(Calendar.YEAR, -1);
                    break;
                case 1300:
                    html.append("<br/>&nbsp;&nbsp;- <b>Date</b>: last 5 years");
                    calendar.add(Calendar.YEAR, -5);
                    break;
            }
        }

        if (StringUtils.isNotBlank(mDateFrom) && StringUtils.isNotBlank(mDateTo)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Date:</b> %s - %s", mDateFrom, mDateTo));
        } else if (StringUtils.isNotBlank(mDateTo)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Date To:</b> %s", mDateTo));
        } else if (StringUtils.isNotBlank(mDateFrom)){
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Date From:</b> %s", mDateFrom));
        }

        if (StringUtils.isNotBlank(mSubregion)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Subregion:</b> %s", mSubregion));
        }

        if (StringUtils.isNotBlank(mReferenceNumber)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Reference Number:</b> %s", mReferenceNumber));
        }
        if (StringUtils.isNotBlank(mVictim)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Victim:</b> %s", mVictim));
        }
        if (StringUtils.isNotBlank(mAggressor)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Aggressor:</b> %s", mAggressor));
        }
        return html.toString();
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
