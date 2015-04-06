package mil.nga.giat.asam.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("serial")
public class AsamBean implements Comparable<AsamBean>, Parcelable {

    public static final SimpleDateFormat OCCURRENCE_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    private Integer mId;
    private Double mLatitude;
    private Double mLongitude;
    private Date mOccurrenceDate;
    private String mReferenceNumber;
    private String mGeographicalSubregion;
    private String mAggressor;
    private String mVictim;
    private String mDescription;
    private String mLatitudeDegMinSec;
    private String mLongitudeDegMinSec;

    public AsamBean() {

    }

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        this.mId = id;
    }

    public Double getLatitude() {
        if (mLatitude == null) {
            return 0.0;
        }
        else if (mLatitude < -90.0) {
            mLatitude = -90.0;
        }
        else if (mLatitude > 90.0) {
            mLatitude = 90.0;
        }
        return mLatitude;
    }

    public void setLatitude(Double latitude) {
        this.mLatitude = latitude;
    }

    public Double getLongitude() {
        if (mLongitude == null) {
            return 0.0;
        }
        else if (mLongitude < -180.0) {
            mLongitude = -180.0;
        }
        else if (mLongitude > 180.0) {
            mLongitude = 180.0;
        }
        return mLongitude;
    }

    public void setLongitude(Double longitude) {
        this.mLongitude = longitude;
    }

    public Date getOccurrenceDate() {
        return mOccurrenceDate;
    }

    public void setOccurrenceDate(Date occurrenceDate) {
        this.mOccurrenceDate = occurrenceDate;
    }

    public String getReferenceNumber() {
        return mReferenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.mReferenceNumber = referenceNumber;
    }

    public String getGeographicalSubregion() {
        return mGeographicalSubregion;
    }

    public void setGeographicalSubregion(String geographicalSubregion) {
        this.mGeographicalSubregion = geographicalSubregion;
    }

    public String getAggressor() {
        return mAggressor;
    }

    public void setAggressor(String aggressor) {
        this.mAggressor = aggressor;
    }

    public String getVictim() {
        return mVictim;
    }

    public void setVictim(String victim) {
        this.mVictim = victim;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String formatLatitutdeDegMinSec() {
        if (mLatitudeDegMinSec == null && mLatitude != null) {
            String hemisphere = "N";
            double degrees = mLatitude.doubleValue();
            if (degrees < 0) {
                hemisphere = "S";
                degrees = Math.abs(degrees);
            }
            double minutes = (degrees - (int)degrees) * 60.0;
            long seconds = Math.round((minutes - (int)minutes) * 60.0);
            if (seconds >= 60) {
                seconds = 0;
                minutes++;
            }
            if (minutes >= 60) {
                minutes = 0;
                degrees++;
            }
            mLatitudeDegMinSec = String.format("%02d\u00b0 %02d' %02d\" %s", (int)degrees, (int)minutes, seconds, hemisphere);
        }
        return mLatitudeDegMinSec == null ? "" : mLatitudeDegMinSec;
    }

    public String formatLongitudeDegMinSec() {
        if (mLongitudeDegMinSec == null && mLongitude != null) {
            String hemisphere = "E";
            double degrees = mLongitude.doubleValue();
            if (degrees < 0) {
                hemisphere = "W";
                degrees = Math.abs(degrees);
            }
            double minutes = (degrees - (int)degrees) * 60.0;
            long seconds = Math.round((minutes - (int)minutes) * 60.0);
            if (seconds >= 60) {
                seconds = 0;
                minutes++;
            }
            if (minutes >= 60) {
                minutes = 0;
                degrees++;
            }
            mLongitudeDegMinSec = String.format("%03d\u00b0 %02d' %02d\" %s", (int)degrees, (int)minutes, seconds, hemisphere);
        }
        return mLongitudeDegMinSec == null ? "" : mLongitudeDegMinSec;
    }

    @Override
    public String toString() {
        return "Victim: " + mVictim + ", Lat: " + mLatitude + ", Lon: " + mLongitude + ", Date: " + mOccurrenceDate;
    }

    @Override
    public int compareTo(AsamBean another) {
        if (mOccurrenceDate == null) {
            return -1;
        }
        if (another != null && another.mOccurrenceDate != null) {
            return -mOccurrenceDate.compareTo(another.mOccurrenceDate);
        }
        return 1;
    }

    public static class AscendingOccurrenceDateComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.mOccurrenceDate == null) {
                return -1;
            }
            return asam1.mOccurrenceDate.compareTo(asam2.mOccurrenceDate);
        }
    }

    public static class DescendingOccurrenceDateComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.mOccurrenceDate == null) {
                return 1;
            }
            return -asam1.mOccurrenceDate.compareTo(asam2.mOccurrenceDate);
        }
    }

    public static class AscendingReferenceNumberComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.mReferenceNumber == null) {
                return -1;
            }
            return asam1.mReferenceNumber.toUpperCase(Locale.US).compareTo(asam2.mReferenceNumber == null ? null : asam2.mReferenceNumber.toUpperCase(Locale.US));
        }
    }

    public static class DescendingReferenceNumberComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.mReferenceNumber == null) {
                return 1;
            }
            return -asam1.mReferenceNumber.toUpperCase(Locale.US).compareTo(asam2.mReferenceNumber == null ? null : asam2.mReferenceNumber.toUpperCase(Locale.US));
        }
    }

    public static class AscendingVictimComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.mVictim == null) {
                return -1;
            }
            return asam1.mVictim.toUpperCase(Locale.US).compareTo(asam2.mVictim == null ? null : asam2.mVictim.toUpperCase(Locale.US));
        }
    }

    public static class DescendingVictimComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.mVictim == null) {
                return 1;
            }
            return -asam1.mVictim.toUpperCase(Locale.US).compareTo(asam2.mVictim == null ? null : asam2.mVictim.toUpperCase(Locale.US));
        }
    }

    public static class AscendingSubregionComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.mGeographicalSubregion == null) {
                return -1;
            }
            return asam1.mGeographicalSubregion.compareTo(asam2.mGeographicalSubregion);
        }
    }

    public static class DescendingSubregionComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.mGeographicalSubregion == null) {
                return 1;
            }
            return -asam1.mGeographicalSubregion.compareTo(asam2.mGeographicalSubregion);
        }
    }

    public static class AscendingAggressorComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.mAggressor == null) {
                return -1;
            }
            return asam1.mAggressor.toUpperCase(Locale.US).compareTo(asam2.mAggressor == null ? null : asam2.mAggressor.toUpperCase(Locale.US));
        }
    }

    public static class DescendingAggressorComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.mAggressor == null) {
                return 1;
            }
            return -asam1.mAggressor.toUpperCase(Locale.US).compareTo(asam2.mAggressor == null ? null : asam2.mAggressor.toUpperCase(Locale.US));
        }
    }

    private AsamBean(Parcel in) {
        mId = in.readInt();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mOccurrenceDate = new Date(in.readLong());
        mReferenceNumber = in.readString();
        mGeographicalSubregion = in.readString();
        mAggressor = in.readString();
        mVictim = in.readString();
        mDescription = in.readString();
        mLatitudeDegMinSec = in.readString();
        mLongitudeDegMinSec = in.readString();
    }

    public static final Parcelable.Creator<AsamBean> CREATOR = new Parcelable.Creator<AsamBean>() {
        public AsamBean createFromParcel(Parcel in) {
            return new AsamBean(in);
        }

        public AsamBean[] newArray(int size) {
            return new AsamBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeLong(mOccurrenceDate.getTime());
        dest.writeString(mReferenceNumber);
        dest.writeString(mGeographicalSubregion);
        dest.writeString(mAggressor);
        dest.writeString(mVictim);
        dest.writeString(mDescription);
        dest.writeString(mLatitudeDegMinSec);
        dest.writeString(mLongitudeDegMinSec);
    }
}