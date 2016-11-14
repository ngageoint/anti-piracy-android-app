package mil.nga.giat.asam.map;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.extension.link.FeatureTileTableLinker;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.features.MapFeatureTiles;
import mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile;
import mil.nga.geopackage.tiles.overlay.FeatureOverlay;
import mil.nga.geopackage.tiles.overlay.GeoPackageOverlayFactory;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.giat.asam.R;

/**
 * Utility class for accessing tiles from the database
 */
public class GeoPackageUtils {

    private Context ctx;
    private GeoPackageManager manager;
    private int gratColorLight;
    private int gratColorDark;
    private int offlineMapFillColor;
    private int offlineMapLineColor;

    public GeoPackageUtils(Context ctx) {
        this.ctx = ctx;
        this.manager = GeoPackageFactory.getManager(ctx);
        gratColorLight = ContextCompat.getColor(ctx, R.color.graticules_line_color_light);
        gratColorDark = ContextCompat.getColor(ctx, R.color.graticules_line_color_dark);
        offlineMapFillColor = ContextCompat.getColor(ctx, R.color.offline_map_fill_color);
        offlineMapLineColor = ContextCompat.getColor(ctx, R.color.offline_map_line_color);
    }


    /**
     * Adds Graticule Overlays to map
     * @param geopackage
     * @param map
     * @return List of overlays added to map
     */
    public List<TileOverlayOptions> addGraticuleOverlaysToMap(String geopackage, GoogleMap map) {

        int mapType = map.getMapType();

        int lineColor = gratColorLight;
        String tileTableName = ctx.getString(R.string.grat_tiles_light_table_name);

        if (mapType == GoogleMap.MAP_TYPE_NORMAL) {
            lineColor = gratColorDark;
            tileTableName = ctx.getString(R.string.grat_tiles_dark_table_name);
        }

        List<TileOverlayOptions> overlays = new ArrayList<>();

        // Open database
        GeoPackage geoPackage = manager.open(geopackage);

        // Feature and tile tables
        List<String> features = geoPackage.getFeatureTables();

        // Query Features
        FeatureDao featureDao = geoPackage.getFeatureDao(features.get(0));

        // Feature Tile Provider (dynamically draw tiles from features)
        FeatureTiles featureTiles = new MapFeatureTiles(ctx, featureDao);
        featureTiles.setMaxFeaturesTileDraw(new NumberFeaturesTile(ctx)); // Draw feature count tiles when max features passed
        featureTiles.getLinePaint().setColor(lineColor);
        featureTiles.getLinePaint().setStrokeWidth(0);
        featureTiles.calculateDrawOverlap();

        FeatureOverlay featureOverlay = new FeatureOverlay(featureTiles);
        featureOverlay.setMinZoom(featureDao.getZoomLevel()); // Set zoom level to start showing tiles

        TileOverlayOptions featureOverlayOptions = new TileOverlayOptions();
        featureOverlayOptions.tileProvider(featureOverlay);
        featureOverlayOptions.zIndex(5); // Draw the feature tiles behind map markers

        FeatureTileTableLinker linker = new FeatureTileTableLinker(geoPackage);
        List<TileDao> tileDaos = linker.getTileDaosForFeatureTable(featureDao.getTableName());
        featureOverlay.ignoreTileDaos(tileDaos);

        // Tile Provider (GeoPackage or Google API)
        TileProvider overlay = GeoPackageOverlayFactory
                .getTileProvider(geoPackage.getTileDao(tileTableName));
        TileOverlayOptions overlayOptions = new TileOverlayOptions();
        overlayOptions.tileProvider(overlay);
        overlayOptions.zIndex(4);

        overlays.add(featureOverlayOptions);
        overlays.add(overlayOptions);

        return overlays;

    }

    public List<TileOverlayOptions> getOfflineMapOverlays(GoogleMap map) {
        List<TileOverlayOptions> overlays = new ArrayList<>();

        // Open database
        GeoPackage geoPackage = manager.open(ctx.getString(R.string.offline_map_geopackage));

        // Feature and tile tables
        List<String> features = geoPackage.getFeatureTables();
        List<String> tiles = geoPackage.getTileTables();

        // Query Features
        FeatureDao featureDao = geoPackage.getFeatureDao(features.get(0));

        // Feature Tile Provider (dynamically draw tiles from features)
        FeatureTiles featureTiles = new MapFeatureTiles(ctx, featureDao);
        featureTiles.setMaxFeaturesTileDraw(new NumberFeaturesTile(ctx)); // Draw feature count tiles when max features passed
        featureTiles.setFillPolygon(true);
        featureTiles.getPolygonFillPaint().setColor(offlineMapFillColor);
        featureTiles.getLinePaint().setColor(offlineMapLineColor);
        featureTiles.setMaxFeaturesPerTile(5000); // Set max features to draw per tile
        featureTiles.calculateDrawOverlap();

        FeatureOverlay featureOverlay = new FeatureOverlay(featureTiles);
        featureOverlay.setMinZoom(featureDao.getZoomLevel()); // Set zoom level to start showing tiles

        FeatureTileTableLinker linker = new FeatureTileTableLinker(geoPackage);
        List<TileDao> tileDaos = linker.getTileDaosForFeatureTable(featureDao.getTableName());
        featureOverlay.ignoreTileDaos(tileDaos);


        TileOverlayOptions featureOverlayOptions = new TileOverlayOptions();
        featureOverlayOptions.tileProvider(featureOverlay);
        featureOverlayOptions.zIndex(3); // Draw the feature tiles behind map markers

        // Tile Provider (GeoPackage or Google API)
        TileProvider overlay = GeoPackageOverlayFactory
                .getTileProvider(geoPackage.getTileDao(tiles.get(0)));
        TileOverlayOptions overlayOptions = new TileOverlayOptions();
        overlayOptions.tileProvider(overlay);
        overlayOptions.zIndex(2);

        overlays.add(featureOverlayOptions);
        overlays.add(overlayOptions);

        return overlays;
    }

    public int getGratColorLight() {
        return gratColorLight;
    }

    public int getGratColorDark() {
        return gratColorDark;
    }
}
