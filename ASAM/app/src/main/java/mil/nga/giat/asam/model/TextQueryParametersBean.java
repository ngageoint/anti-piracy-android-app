package mil.nga.giat.asam.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

import mil.nga.giat.asam.util.AsamUtils;

public class TextQueryParametersBean implements Parcelable {

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
    
    public static TextQueryParametersBean newInstance(TextQueryParametersBean parameters) {
        TextQueryParametersBean copy = new TextQueryParametersBean(parameters.mType);
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

    public TextQueryParametersBean(Type type) {
        this.mType = type;
    }
    
    public boolean isEmpty() {
        return AsamUtils.isEmpty(mKeyword) &&
               mTimeInterval == null &&
               AsamUtils.isEmpty(mDateFrom) &&
               AsamUtils.isEmpty(mDateTo) &&
               AsamUtils.isEmpty(mSubregion) &&
               AsamUtils.isEmpty(mReferenceNumber) &&
               AsamUtils.isEmpty(mVictim) &&
               AsamUtils.isEmpty(mAggressor);
    }
    
    public String getParametersAsFormattedHtml() {
        StringBuilder html = new StringBuilder();
        if (!AsamUtils.isEmpty(mKeyword)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Text:</b> %s", mKeyword));
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

        if (!AsamUtils.isEmpty(mDateFrom) && !AsamUtils.isEmpty(mDateTo)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Date:</b> %s - %s", mDateFrom, mDateTo));
        } else if (!AsamUtils.isEmpty(mDateTo)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Date To:</b> %s", mDateTo));
        } else if (!AsamUtils.isEmpty(mDateFrom)){
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Date From:</b> %s", mDateFrom));
        }

        if (!AsamUtils.isEmpty(mSubregion)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Subregion:</b> %s", mSubregion));
        }

        if (!AsamUtils.isEmpty(mReferenceNumber)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Reference Number:</b> %s", mReferenceNumber));
        }
        if (!AsamUtils.isEmpty(mVictim)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Victim:</b> %s", mVictim));
        }
        if (!AsamUtils.isEmpty(mAggressor)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Aggressor:</b> %s", mAggressor));
        }
        return html.toString();
    }

    /**
     * Creator required for class implementing the parcelable interface.
     */
    public static final Parcelable.Creator<TextQueryParametersBean> CREATOR = new Creator<TextQueryParametersBean>() {

        @Override
        public TextQueryParametersBean createFromParcel(Parcel in) {
            return new TextQueryParametersBean(in);
        }

        @Override
        public TextQueryParametersBean[] newArray(int size) {
            return new TextQueryParametersBean[size];
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

    private TextQueryParametersBean(Parcel in) {
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
