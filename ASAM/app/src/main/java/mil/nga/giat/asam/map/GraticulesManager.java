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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.giat.asam.R;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/**
 * Manages graticules
 */
public class GraticulesManager {

    private Context ctx;
    private GoogleMap mMapUI;
    private ArrayList<Marker> graticuleNumberMarkers = new ArrayList<>();
    private GeoPackageUtils geoPackageUtils;
    private List<TileOverlay> gratTileOverlays;
    private String selectedGraticuleGeoPackage = "";
    private Map<Integer, Bitmap> darkBitmaps;
    private Map<Integer, Bitmap> lightBitmaps;

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

        initializeBitmaps();
    }

    private void initializeBitmaps() {
        darkBitmaps = new HashMap<>();
        lightBitmaps = new HashMap<>();
        for (int i = -180; i < 180; i+=5) {
            String strText = i + "°";
            darkBitmaps.put(i, textAsBitmap(strText, (14.0f * this.ctx.getResources().getDisplayMetrics().density), Color.BLACK));
            lightBitmaps.put(i, textAsBitmap(strText, (14.0f * this.ctx.getResources().getDisplayMetrics().density), Color.WHITE));
        }
    }

    /**
     * Clear out graticules
     */
    public void clearNumbers() {
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

        clearNumbers();

        if (gratTileOverlays != null)
            gratTileOverlays.clear();
    }


    /**
     * Adds graticule numbers onto the map for a given geoPackage
     * @param geopackage
     */
    private void addGraticuleNumbersToMap(String geopackage) {

        Map<Integer, Bitmap> bitmaps = darkBitmaps;

        if (mMapUI.getMapType() == GoogleMap.MAP_TYPE_HYBRID || mMapUI.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            bitmaps = lightBitmaps;
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

        double southerLat = bounds.southwest.latitude + 1;
        double easternLon = bounds.northeast.longitude - 1;

        // determine which latitudinal lines are visible
        int[] latitudes = getLatitudes(geopackage);

        // determine location of lines
        for(Integer lat : latitudes) {
            if (Math.abs(lat - southerLat) < (modValue / 2)){
                continue;
            }

            LatLng latitudeLine = new LatLng(lat.doubleValue(), easternLon);

            Bitmap bmpText = bitmaps.get(lat);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latitudeLine)
                    .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
                    .zIndex(100)
                    .anchor(1.0f, 0.5f);

            graticuleNumberMarkers.add(mMapUI.addMarker(markerOptions));

        }

        // determine which longitudinal lines are visible
        int[] longitudes = getLongitudes(geopackage);

        // determine location of lines
        for(Integer lon : longitudes) {
            LatLng longitudeLine = new LatLng(southerLat, lon.doubleValue());

            Bitmap bmpText = bitmaps.get(lon);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(longitudeLine)
                    .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
                    .zIndex(100)
                    .anchor(0.5f, 1.0f);

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
        if (!gratTileOverlays.isEmpty()) {
            // remove markers
            for (Marker marker : graticuleNumberMarkers){
                marker.remove();
            }
            graticuleNumberMarkers.clear();
            // add markers
            addGraticuleNumbersToMap(selectedGraticuleGeoPackage);
        }
    }

    /**
     * Creates a bitmap for text
     * @param text
     * @param textSize
     * @param textColor
     * @return
     */
    private Bitmap textAsBitmap(String text, float textSize, int textColor) {
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
