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

    private Integer id;
    private Double latitude;
    private Double longitude;
    private Date occurrenceDate;
    private String referenceNumber;
    private String geographicalSubregion;
    private String navArea;
    private String aggressor;
    private String victim;
    private String description;
    private String latitudeDegMinSec;
    private String longitudeDegMinSec;

    public AsamBean() { }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getLatitude() {
        if (latitude == null) {
            return 0.0;
        }
        else if (latitude < -90.0) {
            latitude = -90.0;
        }
        else if (latitude > 90.0) {
            latitude = 90.0;
        }
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        if (longitude == null) {
            return 0.0;
        }
        else if (longitude < -180.0) {
            longitude = -180.0;
        }
        else if (longitude > 180.0) {
            longitude = 180.0;
        }
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Date getOccurrenceDate() {
        return occurrenceDate;
    }

    public void setOccurrenceDate(Date occurrenceDate) {
        this.occurrenceDate = occurrenceDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getGeographicalSubregion() {
        return geographicalSubregion;
    }

    public void setGeographicalSubregion(String geographicalSubregion) {
        this.geographicalSubregion = geographicalSubregion;
    }

    public String getNavArea() {
        return navArea;
    }

    public void setNavArea(String navArea) {
        this.navArea = navArea;
    }

    public String getAggressor() {
        return aggressor;
    }

    public void setAggressor(String hostility) {
        this.aggressor = hostility;
    }

    public String getVictim() {
        return victim;
    }

    public void setVictim(String victim) {
        this.victim = victim;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String formatLatitutdeDegMinSec() {
        if (latitudeDegMinSec == null && latitude != null) {
            String hemisphere = "N";
            double degrees = latitude.doubleValue();
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
            latitudeDegMinSec = String.format("%02d\u00b0 %02d' %02d\" %s", (int)degrees, (int)minutes, seconds, hemisphere);
        }
        return latitudeDegMinSec == null ? "" : latitudeDegMinSec;
    }

    public String formatLongitudeDegMinSec() {
        if (longitudeDegMinSec == null && longitude != null) {
            String hemisphere = "E";
            double degrees = longitude.doubleValue();
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
            longitudeDegMinSec = String.format("%03d\u00b0 %02d' %02d\" %s", (int)degrees, (int)minutes, seconds, hemisphere);
        }
        return longitudeDegMinSec == null ? "" : longitudeDegMinSec;
    }

    @Override
    public String toString() {
        return "Victim: " + victim + ", Lat: " + latitude + ", Lon: " + longitude + ", Date: " + occurrenceDate + ", Nav Area: " + navArea;
    }

    @Override
    public int compareTo(AsamBean another) {
        if (occurrenceDate == null) {
            return -1;
        }
        if (another != null && another.occurrenceDate != null) {
            return -occurrenceDate.compareTo(another.occurrenceDate);
        }
        return 1;
    }

    public static class AscendingOccurrenceDateComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.occurrenceDate == null) {
                return -1;
            }
            return asam1.occurrenceDate.compareTo(asam2.occurrenceDate);
        }
    }

    public static class DescendingOccurrenceDateComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.occurrenceDate == null) {
                return 1;
            }
            return -asam1.occurrenceDate.compareTo(asam2.occurrenceDate);
        }
    }

    public static class AscendingReferenceNumberComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.referenceNumber == null) {
                return -1;
            }
            return asam1.referenceNumber.toUpperCase(Locale.US).compareTo(asam2.referenceNumber == null ? null : asam2.referenceNumber.toUpperCase(Locale.US));
        }
    }

    public static class DescendingReferenceNumberComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.referenceNumber == null) {
                return 1;
            }
            return -asam1.referenceNumber.toUpperCase(Locale.US).compareTo(asam2.referenceNumber == null ? null : asam2.referenceNumber.toUpperCase(Locale.US));
        }
    }

    public static class AscendingVictimComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.victim == null) {
                return -1;
            }
            return asam1.victim.toUpperCase(Locale.US).compareTo(asam2.victim == null ? null : asam2.victim.toUpperCase(Locale.US));
        }
    }

    public static class DescendingVictimComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.victim == null) {
                return 1;
            }
            return -asam1.victim.toUpperCase(Locale.US).compareTo(asam2.victim == null ? null : asam2.victim.toUpperCase(Locale.US));
        }
    }

    public static class AscendingSubregionComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.geographicalSubregion == null) {
                return -1;
            }
            return asam1.geographicalSubregion.compareTo(asam2.geographicalSubregion);
        }
    }

    public static class DescendingSubregionComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.geographicalSubregion == null) {
                return 1;
            }
            return -asam1.geographicalSubregion.compareTo(asam2.geographicalSubregion);
        }
    }

    public static class AscendingHostilityComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.aggressor == null) {
                return -1;
            }
            return asam1.aggressor.toUpperCase(Locale.US).compareTo(asam2.aggressor == null ? null : asam2.aggressor.toUpperCase(Locale.US));
        }
    }

    public static class DescendingHostilityComparator implements Comparator<AsamBean> {

        @Override
        public int compare(AsamBean asam1, AsamBean asam2) {
            if (asam1.aggressor == null) {
                return 1;
            }
            return -asam1.aggressor.toUpperCase(Locale.US).compareTo(asam2.aggressor == null ? null : asam2.aggressor.toUpperCase(Locale.US));
        }
    }

    private AsamBean(Parcel in) {
        id = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        occurrenceDate = new Date(in.readLong());
        referenceNumber = in.readString();
        geographicalSubregion = in.readString();
        navArea = in.readString();
        aggressor = in.readString();
        victim = in.readString();
        description = in.readString();
        latitudeDegMinSec = in.readString();
        longitudeDegMinSec = in.readString();
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
        dest.writeInt(id);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeLong(occurrenceDate.getTime());
        dest.writeString(referenceNumber);
        dest.writeString(geographicalSubregion);
        dest.writeString(navArea);
        dest.writeString(aggressor);
        dest.writeString(victim);
        dest.writeString(description);
        dest.writeString(latitudeDegMinSec);
        dest.writeString(longitudeDegMinSec);
    }
}