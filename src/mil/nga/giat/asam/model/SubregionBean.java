package mil.nga.giat.asam.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;


@SuppressWarnings("serial")
public class SubregionBean implements Serializable {

    public static final List<Integer> MULTI_SUBREGION_IDS = Arrays.asList(new Integer[] { 16, 19, 76, 83, 96 });
    private int mSubregionId;
    private List<GeoPoint> mGeoPoints; // Needed because LatLng translates 180 to -180 which messes up map click calculations.
    private boolean mSelected;
    private List<LatLng> mMapCoordinates;
    private Polygon mMapPolygon;
    
    public SubregionBean(int subregionId, List<GeoPoint> geoPoints) {
        mSubregionId = subregionId;
        mGeoPoints = geoPoints;
        mMapCoordinates = new ArrayList<LatLng>();
        for (GeoPoint geoPoint : geoPoints) {
            mMapCoordinates.add(new LatLng(geoPoint.latitude, geoPoint.longitude));
        }
    }
    
    public int getSubregionId() {
        return mSubregionId;
    }
    
    public List<LatLng> getMapCoordinates() {
        return mMapCoordinates;
    }
    
    public boolean isSelected() {
        return mSelected;
    }
    
    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }
    
    public Polygon getMapPolygon() {
        return mMapPolygon;
    }
    
    public void setMapPolygon(Polygon mapPolygon) {
        mMapPolygon = mapPolygon;
    }
    
    public List<GeoPoint> getGeoPoints() {
        return mGeoPoints;
    }
    
    public static class GeoPoint {
        
        public double latitude;
        public double longitude;
        
        public static GeoPoint newInstance(double latitude, double longitude) {
            GeoPoint geoPoint = new GeoPoint();
            geoPoint.latitude = latitude;
            geoPoint.longitude = longitude;
            return geoPoint;
        }
    }
}