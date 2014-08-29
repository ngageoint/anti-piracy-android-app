package mil.nga.giat.asam.map;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

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
                if (feature instanceof com.vividsolutions.jts.geom.Polygon) {                	                	
                	polygons.add(generatePolygon((com.vividsolutions.jts.geom.Polygon)feature));                    
                }
                else if(feature instanceof com.vividsolutions.jts.geom.MultiPolygon) {                	
                	MultiPolygon multiPolygon = (MultiPolygon)feature;
                	for(int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                		Geometry geometry = multiPolygon.getGeometryN(i);
                		if(geometry instanceof com.vividsolutions.jts.geom.Polygon) {
                			polygons.add(generatePolygon((com.vividsolutions.jts.geom.Polygon)geometry));
                		}
                		//nested MultiPolygons are ignored for now.  Recursive solution has performance
                		//implications.                		
                	}                	
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
        
        /**
         * Utility method for generating PolygonOptions from a Polygon.
         * @param pPolygon A Polygon to generate.
         * @return A fully constructed PolygonOptions object complete with style and 
         *         z-index positioning.
         */
        private PolygonOptions generatePolygon(com.vividsolutions.jts.geom.Polygon pPolygon) {
        	
        	PolygonOptions options = new PolygonOptions()
                .zIndex(2)
                .visible(false)
                .fillColor(FILL_COLOR)
                .strokeWidth(0);
        	
        	//Exterior Polygon
        	for (Coordinate coordinate : pPolygon.getExteriorRing().getCoordinates()) {
                options.add(new LatLng(coordinate.y, coordinate.x));
            }
        	
        	//Interior Polygons
        	for (int i = 0; i < pPolygon.getNumInteriorRing(); i++) {
                Coordinate[] coordinates = pPolygon.getInteriorRingN(0).getCoordinates();
                Collection<LatLng> hole = new ArrayList<LatLng>(coordinates.length);
                for (Coordinate coordinate : coordinates) {
                    hole.add(new LatLng(coordinate.y, coordinate.x));
                }                                     
                options.addHole(hole);
            }        	
        	
        	return options;
        }
        
    }
    
}
