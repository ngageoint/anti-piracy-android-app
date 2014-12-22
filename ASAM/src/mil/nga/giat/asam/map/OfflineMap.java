package mil.nga.giat.asam.map;

import java.util.ArrayList;
import java.util.Collection;

import mil.nga.giat.asam.R;

import android.app.ProgressDialog;
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
    private TileOverlay backgroundTileOverlay;
    private Collection<Polygon> offlinePolygons = null;
    private ProgressDialog progressDialog;

    public OfflineMap(Context context, GoogleMap map, Collection<Geometry> offlineFeatures) {
        this.mContext = context;
        this.mMapUI = map;
        this.mOfflineFeatures = offlineFeatures;
        
        loadOfflineMaps();
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
        OfflineMapsTask task = new OfflineMapsTask();
        task.execute(mOfflineFeatures.toArray(new Geometry[mOfflineFeatures.size()]));
    }
       
    private class OfflineMapsTask extends AsyncTask<Geometry, Void, Collection<PolygonOptions>> {
        @Override
        protected void onPreExecute() {
        	progressDialog = ProgressDialog.show(mContext, mContext.getString(R.string.offline_map_progress_dialog_title_text), mContext.getString(R.string.offline_map_progress_dialog_content_text), true);

            mMapUI.setMapType(GoogleMap.MAP_TYPE_NONE);
            
            BackgroundTileProvider tileProvider = new BackgroundTileProvider(mContext);           
            backgroundTileOverlay = mMapUI.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider).zIndex(1).visible(false));
            backgroundTileOverlay.setVisible(true);
        }
    	
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
                		                		
                		//nested MultiPolygons are ignored for now.  Recursive solution has performance implications.                		
                	}                	
                }
            }    
            
            return polygons;
        }

		@Override
        protected void onPostExecute(Collection<PolygonOptions> polygons) {
    		offlinePolygons = new ArrayList<Polygon>(polygons.size());
        	for (PolygonOptions polygon : polygons) {
                offlinePolygons.add(mMapUI.addPolygon(polygon));  
        	}
        	
        	progressDialog.dismiss();
        }
		     
        /**
         * Utility method for generating PolygonOptions from a Polygon.
         * @param pPolygon A Polygon to generate.
         * @return A fully constructed PolygonOptions object complete with style and 
         *         z-index positioning.
         */
        private PolygonOptions generatePolygon(com.vividsolutions.jts.geom.Polygon pPolygon) {
        	
        	PolygonOptions options = new PolygonOptions()
        		.visible(true)
                .zIndex(2)
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
