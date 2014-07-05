package mil.nga.giat.asam.map;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class OfflineMap {
    private static int FILL_COLOR = 0xFFDDDDDD;
    
    private Context mContext;
    private GoogleMap mMapUI;
    private Collection<Geometry> mOfflineFeatures;
    private boolean mLoaded = false;
    private boolean mVisible = false;
    private TileOverlay backgroundTileOverlay;
    private Collection<Polygon> offlinePolygons = null;

    public OfflineMap(Context context, GoogleMap map, Collection<Geometry> offlineFeatures) {
        this.mContext = context;
        this.mMapUI = map;
        this.mOfflineFeatures = offlineFeatures;
        loadOfflineMaps();
    }
    
    public void setVisible(boolean visible) {
        this.mVisible = visible;
        
        if (!mLoaded) return;
        
        backgroundTileOverlay.setVisible(visible);
        for (Polygon polygon : offlinePolygons) {
            polygon.setVisible(visible);
        }
        
        if (visible) mMapUI.setMapType(GoogleMap.MAP_TYPE_NONE);
    }
    
    public void clear() {
        backgroundTileOverlay.clearTileCache();
        backgroundTileOverlay.remove();
        for (Polygon polygon : offlinePolygons) {
            polygon.remove();
        }
        offlinePolygons.clear();
    }
    
    private void loadOfflineMaps() {
        BackgroundTileProvider tileProvider = new BackgroundTileProvider(mContext);           
        backgroundTileOverlay = mMapUI.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider).zIndex(1).visible(false));
        
        OfflineMapsTask task = new OfflineMapsTask();
        task.execute(mOfflineFeatures.toArray(new Geometry[mOfflineFeatures.size()]));
    }
       
    private class OfflineMapsTask extends AsyncTask<Geometry, Void, Collection<PolygonOptions>> {
   
        @Override
        protected Collection<PolygonOptions> doInBackground(Geometry... features) {
            Collection<PolygonOptions> polygons = new ArrayList<PolygonOptions>(features.length);
            for (Geometry feature : features) {
                // For now all offline map features are polygons
                if ("Polygon".equals(feature.getGeometryType())) {
                    PolygonOptions options = new PolygonOptions()
                        .zIndex(2)
                        .visible(false)
                        .fillColor(FILL_COLOR)
                        .strokeWidth(0);
                    
                    com.vividsolutions.jts.geom.Polygon polygon = (com.vividsolutions.jts.geom.Polygon) feature;
                    for (Coordinate coordinate : polygon.getExteriorRing().getCoordinates()) {
                        options.add(new LatLng(coordinate.y, coordinate.x));
                    }
                    
                    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                        Coordinate[] coordinates = polygon.getInteriorRingN(0).getCoordinates();
                        Collection<LatLng> hole = new ArrayList<LatLng>(coordinates.length);
                        for (Coordinate coordinate : coordinates) {
                            hole.add(new LatLng(coordinate.y, coordinate.x));
                        }                     
                        
                        options.addHole(hole);
                    }
                    
                    polygons.add(options);
                }
            }
            
            return polygons;
        }

        @Override
        protected void onPostExecute(Collection<PolygonOptions> polygons) {
            offlinePolygons = new ArrayList<Polygon>(polygons.size());
            if (mVisible) mMapUI.setMapType(GoogleMap.MAP_TYPE_NONE);

            backgroundTileOverlay.setVisible(mVisible);
            for (PolygonOptions polygon : polygons) {
                polygon.visible(mVisible);
                offlinePolygons.add(mMapUI.addPolygon(polygon));
            }
            
            mLoaded = true;
        }
    }
}
