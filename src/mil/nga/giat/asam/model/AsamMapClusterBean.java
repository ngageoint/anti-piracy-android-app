package mil.nga.giat.asam.model;

import java.io.Serializable;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


@SuppressWarnings("serial")
public class AsamMapClusterBean implements Serializable {

    private List<AsamBean> mAsams;
    private LatLng mClusteredMapPosition;
    private Marker mMapMarker;
    
    public AsamMapClusterBean(List<AsamBean> asams, LatLng clusteredMapPosition) {
        mAsams = asams;
        mClusteredMapPosition = clusteredMapPosition;
    }
    
    public List<AsamBean> getAsams() {
        return mAsams;
    }
    
    public LatLng getClusteredMapPosition() {
        return mClusteredMapPosition;
    }
    
    public void setMapMarker(Marker mapMarker) {
        mMapMarker = mapMarker;
    }
    
    public Marker getMapMarker() {
        return mMapMarker;
    }
    
    public int getNumPointsInCluster() {
        return mAsams.size();
    }
}
