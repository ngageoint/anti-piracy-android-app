package mil.nga.giat.asam.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.util.ArrayList;
import java.util.List;

import mil.nga.giat.asam.R;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/**
 * Manages graticules
 */
public class GraticulesManager {

    private Context ctx;
    private GoogleMap mMapUI;
    private ArrayList<Marker> graticuleNumberMarkers = new ArrayList<>();
    private LatLng previousAdjustedCenter;
    private GeoPackageUtils geoPackageUtils;
    private List<TileOverlay> gratTileOverlays;
    private String selectedGraticuleGeoPackage = "";


    /**
     * Constructor
     * @param ctx
     * @param mMapUI
     */
    public GraticulesManager(Context ctx, GoogleMap mMapUI) {
        this.ctx = ctx;
        this.mMapUI = mMapUI;
        gratTileOverlays = new ArrayList<>();
        geoPackageUtils = new GeoPackageUtils(ctx);
    }

    /**
     * Clear out graticules
     */
    public void clear() {
        for (Marker marker : graticuleNumberMarkers){
            marker.remove();
        }
        graticuleNumberMarkers.clear();
    }

    /**
     * Refreshes graticules
     */
    public void refreshGraticules() {
        addGraticulesToMap(selectedGraticuleGeoPackage);
    }

    /**
     * Add graticules to map
     * @param geopackage
     */
    public void addGraticulesToMap(String geopackage) {

        selectedGraticuleGeoPackage = geopackage;

        List<TileOverlayOptions> overlayOptions = geoPackageUtils.addGraticuleOverlaysToMap(geopackage, mMapUI);

        removeGraticulesFromMap();

        for (TileOverlayOptions overlayOption : overlayOptions) {
            gratTileOverlays.add(mMapUI.addTileOverlay(overlayOption));
        }

        if (!this.ctx.getResources().getString(R.string.grat_none).equals(geopackage)) {
            addGraticuleNumbersToMap(geopackage);
        }

    }

    /**
     * removes graticules from the map
     */
    public void removeGraticulesFromMap() {
        for (TileOverlay gratTileOverlay : gratTileOverlays) {
            gratTileOverlay.clearTileCache();
            gratTileOverlay.remove();
        }

        clear();

        if (gratTileOverlays != null)
            gratTileOverlays.clear();
    }


    /**
     * Adds graticule numbers onto the map for a given geoPackage
     * @param geopackage
     */
    private void addGraticuleNumbersToMap(String geopackage) {

        int textColor = Color.BLACK;

        if (mMapUI.getMapType() == GoogleMap.MAP_TYPE_HYBRID || mMapUI.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            textColor = Color.WHITE;
        }

        // determine bounds of visible region
        LatLngBounds bounds = mMapUI.getProjection().getVisibleRegion().latLngBounds;

        int modValue = 30;
        if (this.ctx.getString(R.string.grat_10_geopackage).equals(geopackage)) {
            modValue = 10;
        } else if (this.ctx.getString(R.string.grat_15_geopackage).equals(geopackage)) {
            modValue = 15;
        } else if (this.ctx.getString(R.string.grat_20_geopackage).equals(geopackage)) {
            modValue = 20;
        }

        double centerLat = Math.round(bounds.getCenter().latitude);
        centerLat = centerLat - centerLat % modValue;
        double centerLon = Math.round(bounds.getCenter().longitude);
        centerLon = centerLon - centerLon % modValue;

        previousAdjustedCenter = new LatLng(centerLat, centerLon);

        // determine which latitudinal lines are visible
        int[] latitudes = getLatitudes(geopackage);

        // determine location of lines
        for(Integer lat : latitudes) {
            if (lat == centerLat){
                continue;
            }
            LatLng latitudeLine = new LatLng(lat.doubleValue(), centerLon);

            String strText = lat.toString() + "°";
            Bitmap bmpText = textAsBitmap(strText, (14.0f * this.ctx.getResources().getDisplayMetrics().density), textColor);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latitudeLine)
                    .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
                    .anchor(0.5f, 0.5f);

            graticuleNumberMarkers.add(mMapUI.addMarker(markerOptions));

        }

        // determine which longitudinal lines are visible
        int[] longitudes = getLongitudes(geopackage);

        // determine location of lines
        for(Integer lon : longitudes) {
            if (lon == centerLon) {
                continue;
            }
            LatLng longitudeLine = new LatLng(centerLat, lon.doubleValue());

            String strText = lon.toString() + "°";
            Bitmap bmpText = textAsBitmap(strText, (14.0f * this.ctx.getResources().getDisplayMetrics().density), textColor);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(longitudeLine)
                    .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
                    .anchor(0.5f, 0.5f);

            graticuleNumberMarkers.add(mMapUI.addMarker(markerOptions));

        }

    }

    /**
     * Get Latitudes for a given geopackage
     * @param geopackage
     * @return
     */
    private int[] getLatitudes(String geopackage) {
        int[] result = this.ctx.getResources().getIntArray(R.array.grat_30_latitudes);
        if (this.ctx.getResources().getString(R.string.grat_10_geopackage).equals(geopackage)) {
            result = this.ctx.getResources().getIntArray(R.array.grat_10_latitudes);
        } else if (this.ctx.getResources().getString(R.string.grat_15_geopackage).equals(geopackage)) {
            result = this.ctx.getResources().getIntArray(R.array.grat_15_latitudes);
        } else if (this.ctx.getResources().getString(R.string.grat_20_geopackage).equals(geopackage)) {
            result = this.ctx.getResources().getIntArray(R.array.grat_20_latitudes);
        }
        return result;
    }


    /**
     * Get Longitudes for a given geopackage
     * @param geopackage
     * @return
     */
    private int[] getLongitudes(String geopackage) {
        int[] result = this.ctx.getResources().getIntArray(R.array.grat_30_longitudes);
        if (this.ctx.getResources().getString(R.string.grat_10_geopackage).equals(geopackage)) {
            result = this.ctx.getResources().getIntArray(R.array.grat_10_longitudes);
        } else if (this.ctx.getResources().getString(R.string.grat_15_geopackage).equals(geopackage)) {
            result = this.ctx.getResources().getIntArray(R.array.grat_15_longitudes);
        } else if (this.ctx.getResources().getString(R.string.grat_20_geopackage).equals(geopackage)) {
            result = this.ctx.getResources().getIntArray(R.array.grat_20_longitudes);
        }
        return result;
    }

    /**
     * Updates graticules should be called when map events occur such as camera changes
     */
    public void mapUpdate() {
        if (!graticuleNumberMarkers.isEmpty()) {
            // determine bounds of visible region
            LatLngBounds bounds = mMapUI.getProjection().getVisibleRegion().latLngBounds;

            int modValue = 30;
            if (this.ctx.getString(R.string.grat_10_geopackage).equals(selectedGraticuleGeoPackage)) {
                modValue = 10;
            } else if (this.ctx.getString(R.string.grat_15_geopackage).equals(selectedGraticuleGeoPackage)) {
                modValue = 15;
            } else if (this.ctx.getString(R.string.grat_20_geopackage).equals(selectedGraticuleGeoPackage)) {
                modValue = 20;
            }

            double centerLat = Math.round(bounds.getCenter().latitude);
            centerLat = centerLat - centerLat % modValue;
            double centerLon = Math.round(bounds.getCenter().longitude);
            centerLon = centerLon - centerLon % modValue;

            if (previousAdjustedCenter != null) {
                if (centerLat != previousAdjustedCenter.latitude ||
                        centerLon != previousAdjustedCenter.longitude) {
                    // remove markers
                    for (Marker marker : graticuleNumberMarkers){
                        marker.remove();
                    }
                    graticuleNumberMarkers.clear();

                    // add markers
                    addGraticuleNumbersToMap(selectedGraticuleGeoPackage);
                }
            }
        }
    }

    /**
     * Creates a bitmap for text
     * @param text
     * @param textSize
     * @param textColor
     * @return
     */
    public Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

}
