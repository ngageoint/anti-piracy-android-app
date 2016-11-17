package mil.nga.giat.asam.map;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPolygonStyle;

import org.json.JSONException;

import java.io.IOException;

import mil.nga.giat.asam.R;

public class OfflineMap {

    private static int FILL_COLOR = 0xFFDDDDDD;

    private Context mContext;
    private GoogleMap map;
    private TileOverlay backgroundTileOverlay;
//    private List<TileOverlay> featureOverlays;
    private ProgressDialog progressDialog;
    private GeoPackageUtils geoPackageUtils;
    private GeoJsonLayer geoJsonLayer = null;

    public OfflineMap(Context context, GoogleMap map) {
        this.mContext = context;
        this.map = map;
//        featureOverlays = new ArrayList<>();
        geoPackageUtils = new GeoPackageUtils(context);
        loadOfflineMaps();
    }

    public void clear() {
        backgroundTileOverlay.clearTileCache();
        backgroundTileOverlay.remove();

        geoJsonLayer.removeLayerFromMap();

//        for (TileOverlay featureOverlay : featureOverlays) {
//            featureOverlay.clearTileCache();
//            featureOverlay.remove();
//        }

//        featureOverlays.clear();
    }

    private void loadOfflineMaps() {
        GeoJSONOfflineMapsTask task = new GeoJSONOfflineMapsTask();
//        OfflineMapsTask task = new OfflineMapsTask();
        task.execute();
    }

    private class GeoJSONOfflineMapsTask extends AsyncTask<Void, Void, GeoJsonLayer> {
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(mContext, mContext.getString(R.string.offline_map_progress_dialog_title_text), mContext.getString(R.string.offline_map_progress_dialog_content_text), true);

            map.setMapType(GoogleMap.MAP_TYPE_NONE);

            BackgroundTileProvider tileProvider = new BackgroundTileProvider(mContext);
            backgroundTileOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider).zIndex(1).visible(false));
            backgroundTileOverlay.setVisible(true);
        }

        @Override
        protected GeoJsonLayer doInBackground(Void... params) {
            try {
                return new GeoJsonLayer(map, R.raw.ne_110m_land, mContext);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(GeoJsonLayer layer) {
            updateLayerStyle(layer);
            addGeoJsonLayerToMap(layer);
            geoJsonLayer = layer;

            progressDialog.dismiss();
        }

        /**
         * Update the layer style
         * @param layer
         */
        private void updateLayerStyle(GeoJsonLayer layer) {
            GeoJsonPolygonStyle style = new GeoJsonPolygonStyle();
            style.setVisible(true);
            style.setZIndex(2);
            style.setFillColor(FILL_COLOR);
            style.setStrokeWidth(0);
            for (GeoJsonFeature feature : layer.getFeatures()) {
                feature.setPolygonStyle(style);
            }
        }

        private void addGeoJsonLayerToMap(GeoJsonLayer layer) {
            layer.addLayerToMap();
        }
    }



//    private class OfflineMapsTask extends AsyncTask<Void, Void, List<TileOverlayOptions>> {
//        @Override
//        protected void onPreExecute() {
//            progressDialog = ProgressDialog.show(mContext, mContext.getString(R.string.offline_map_progress_dialog_title_text), mContext.getString(R.string.offline_map_progress_dialog_content_text), true);
//
//            map.setMapType(GoogleMap.MAP_TYPE_NONE);
//
//            BackgroundTileProvider tileProvider = new BackgroundTileProvider(mContext);
//            backgroundTileOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider).zIndex(1).visible(false));
//            backgroundTileOverlay.setVisible(true);
//        }
//
//        @Override
//        protected List<TileOverlayOptions> doInBackground(Void... params) {
//
//            return geoPackageUtils.getOfflineMapOverlays(map);
//        }
//
//
//        @Override
//        protected void onPostExecute(List<TileOverlayOptions> newFeatureOverlays) {
//
//            for (TileOverlay featureOverlay : featureOverlays) {
//                featureOverlay.clearTileCache();
//                featureOverlay.remove();
//            }
//
//            featureOverlays.clear();
//
//            for(TileOverlayOptions overlayOptions : newFeatureOverlays) {
//                featureOverlays.add(map.addTileOverlay(overlayOptions));
//            }
//
//            progressDialog.dismiss();
//        }
//    }
}
