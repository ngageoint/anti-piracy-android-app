package mil.nga.giat.asam.map;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import mil.nga.giat.asam.Asam;
import mil.nga.giat.asam.AsamListActivity;
import mil.nga.giat.asam.AsamReportActivity;
import mil.nga.giat.asam.R;
import mil.nga.giat.asam.connectivity.OfflineBannerFragment;
import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.model.AsamJsonParser;
import mil.nga.giat.asam.model.AsamMapClusterBean;
import mil.nga.giat.asam.model.TextQueryParametersBean;
import mil.nga.giat.asam.net.AsamWebService;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamListContainer;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.AsamUtils;
import mil.nga.giat.asam.util.SyncTime;
import mil.nga.giat.poffencluster.PoffenCluster;
import mil.nga.giat.poffencluster.PoffenClusterCalculator;
import mil.nga.giat.poffencluster.PoffenPoint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vividsolutions.jts.geom.Geometry;


public class AllAsamsMapActivity extends ActionBarActivity implements OnCameraChangeListener, OnMarkerClickListener, CancelableCallback, Asam.OnOfflineFeaturesListener, OfflineBannerFragment.OnOfflineBannerClick {
    
    private static class QueryHandler extends Handler {
        
        WeakReference<AllAsamsMapActivity> mAllAsamsMapActivity;

        QueryHandler(AllAsamsMapActivity allAsamsMapActivity) {
            mAllAsamsMapActivity = new WeakReference<AllAsamsMapActivity>(allAsamsMapActivity);
        }

        @Override
        public void handleMessage(Message message) {
            AllAsamsMapActivity allAsamsMapActivity = mAllAsamsMapActivity.get();
            allAsamsMapActivity.mQueryProgressDialog.dismiss();
            if (allAsamsMapActivity.mQueryError) {
                allAsamsMapActivity.mQueryError = false;
                Toast.makeText(allAsamsMapActivity, allAsamsMapActivity.getString(R.string.all_asams_map_query_error_text), Toast.LENGTH_LONG).show();
            }
            
            allAsamsMapActivity.clearAsamMarkers();
            
            if (allAsamsMapActivity.mAsams.size() == 1) {
                allAsamsMapActivity.mPerformMapClustering = true;
                AsamBean asam = allAsamsMapActivity.mAsams.get(0);
                allAsamsMapActivity.mMapUI.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(asam.getLatitude(), asam.getLongitude())).zoom(AsamConstants.SINGLE_ASAM_ZOOM_LEVEL).build()));
            }
            else if (allAsamsMapActivity.mAsams.size() > 1) {
                allAsamsMapActivity.mPerformMapClustering = true;
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                for (AsamBean asam : allAsamsMapActivity.mAsams) {
                    boundsBuilder = boundsBuilder.include(new LatLng(asam.getLatitude(), asam.getLongitude()));
                }
                allAsamsMapActivity.mMapUI.moveCamera(CameraUpdateFactory.scrollBy(0.5f, 0.0f)); // Needed in case camera doesn't move the view.
                allAsamsMapActivity.mMapUI.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 0), allAsamsMapActivity);
            }
            else {
                Toast.makeText(allAsamsMapActivity, allAsamsMapActivity.getString(R.string.all_asams_map_no_asams_text), Toast.LENGTH_LONG).show();
            }
                        
            // Now set the feedback title.
            StringBuilder feedbackText = new StringBuilder("");
            if (allAsamsMapActivity.mQueryType == AsamConstants.ALL_ASAMS_QUERY) {
                if (allAsamsMapActivity.mAsams.size() == 1) {
                    feedbackText.append(String.format(allAsamsMapActivity.getString(R.string.all_asams_map_1_asam_text_with_timespan), allAsamsMapActivity.mTimeSpanTitleText));
                }
                else {
                    feedbackText.append(String.format(allAsamsMapActivity.getString(R.string.all_asams_map_multiple_asams_text_with_timespan), allAsamsMapActivity.mAsams.size(), allAsamsMapActivity.mTimeSpanTitleText));
                }
            }
            else if (allAsamsMapActivity.mQueryType == AsamConstants.SUBREGION_QUERY) {
                if (allAsamsMapActivity.mAsams.size() == 1) {
                    feedbackText.append(String.format(allAsamsMapActivity.getString(R.string.all_asams_map_1_asam_text_with_timespan_with_subregions), allAsamsMapActivity.mTimeSpanTitleText, allAsamsMapActivity.mSubregionsTitleText));
                }
                else {
                    feedbackText.append(String.format(allAsamsMapActivity.getString(R.string.all_asams_map_multiple_asams_text_with_timespan_with_subregions), allAsamsMapActivity.mAsams.size(), allAsamsMapActivity.mTimeSpanTitleText, allAsamsMapActivity.mSubregionsTitleText));
                }
            }
            else if (allAsamsMapActivity.mQueryType == AsamConstants.TEXT_QUERY) {
                if (allAsamsMapActivity.mAsams.size() == 1) {
                    feedbackText.append(allAsamsMapActivity.getString(R.string.all_asams_map_1_asam_text));
                }
                else {
                    feedbackText.append(String.format(allAsamsMapActivity.getString(R.string.all_asams_map_multiple_asams_text), allAsamsMapActivity.mAsams.size()));
                }
            }
            allAsamsMapActivity.mQueryFeedbackUI.setText(feedbackText.toString());
        }
    }

    private final Object Mutex = new Object();
    private volatile boolean mQueryError;
    private volatile boolean mPerformMapClustering;
    private List<AsamBean> mAsams;
    private List<AsamMapClusterBean> mMapClusters;
    private TextView mQueryFeedbackUI;
    private GoogleMap mMapUI;
    private int mMapType;
    private ProgressDialog mQueryProgressDialog;
    private QueryHandler mQueryHandler;
    private String mTimeSpanTitleText;
    private String mSubregionsTitleText;
    private int mQueryType;
    private int mPreviousZoomLevel;
    private List<AsamMapClusterBean> mVisibleClusters;
    private SharedPreferences mSharedPreferences;  
    private OfflineMap offlineMap;
    private MenuItem offlineMapMenuItem;
    private OfflineBannerFragment offlineAlertFragment;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(AllAsamsMapActivity.class.getName() + ":onCreate");
        setContentView(R.layout.all_asams_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        mQueryError = false;
        mPerformMapClustering = false;
        mAsams = new ArrayList<AsamBean>();
        mMapClusters = new ArrayList<AsamMapClusterBean>();
        mVisibleClusters = new ArrayList<AsamMapClusterBean>();

        mQueryFeedbackUI = (TextView)findViewById(R.id.all_asams_map_query_feedback_text_ui);
        mMapUI = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.all_asams_map_map_view_ui)).getMap();
        mMapUI.setOnCameraChangeListener(this);
        mMapUI.setOnMarkerClickListener(this);
        mPreviousZoomLevel = -1;
        
        offlineAlertFragment = new OfflineBannerFragment();
        getSupportFragmentManager().beginTransaction()
            .add(android.R.id.content, offlineAlertFragment)
            .commit();
        
        // Called to handle the UI after the query has run.
        mQueryHandler = new QueryHandler(this);
        mQueryProgressDialog = ProgressDialog.show(this, getString(R.string.all_asams_map_query_progress_dialog_title_text), getString(R.string.all_asams_map_query_progress_dialog_content_text), true);
        Intent intent = getIntent();
        mQueryType = intent.getIntExtra(AsamConstants.QUERY_TYPE_KEY, AsamConstants.ALL_ASAMS_QUERY);
        switch (mQueryType) {
            
            case AsamConstants.SUBREGION_QUERY:
                int queryTimeSpan = intent.getIntExtra(AsamConstants.SUBREGION_QUERY_TIME_SPAN_KEY, -1);
                List<Integer> selectedSubregionIds = intent.getIntegerArrayListExtra(AsamConstants.SUBREGION_QUERY_SUBREGIONS_LIST_KEY);
                if (selectedSubregionIds.size() == 1) {
                    mSubregionsTitleText = getString(R.string.all_asams_map_1_subregion_text);
                }
                else {
                    mSubregionsTitleText = String.format(getString(R.string.all_asams_map_multiple_subregions_text), selectedSubregionIds.size());
                }
                if (queryTimeSpan == AsamConstants.TIME_SPAN_60_DAYS) {
                    mTimeSpanTitleText = getString(R.string.query_60_days_text);
                }
                else if (queryTimeSpan == AsamConstants.TIME_SPAN_90_DAYS) {
                    mTimeSpanTitleText = getString(R.string.query_90_days_text);
                }
                else if (queryTimeSpan == AsamConstants.TIME_SPAN_180_DAYS) {
                    mTimeSpanTitleText = getString(R.string.query_180_days_text);
                }
                else if (queryTimeSpan == AsamConstants.TIME_SPAN_1_YEAR) {
                    mTimeSpanTitleText = getString(R.string.query_1_year_text);
                }
                else if (queryTimeSpan == AsamConstants.TIME_SPAN_5_YEARS) {
                    mTimeSpanTitleText = getString(R.string.query_5_years_text);
                }
                else if (queryTimeSpan == AsamConstants.TIME_SPAN_ALL) {
                    mTimeSpanTitleText = getString(R.string.query_all_text);
                }
                new QueryThread(queryTimeSpan, selectedSubregionIds).start();
                break;
                
            case AsamConstants.ALL_ASAMS_QUERY:
                mTimeSpanTitleText = getString(R.string.query_1_year_text); // Start off with this.
                new QueryThread(AsamConstants.TIME_SPAN_1_YEAR).start();
                break;
                
            case AsamConstants.TEXT_QUERY:
                TextQueryParametersBean parameters = (TextQueryParametersBean)intent.getSerializableExtra(AsamConstants.TEXT_QUERY_PARAMETERS_KEY);
                new QueryThread(parameters).start();
                break;
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        if (offlineMap == null) {
            ((Asam) getApplication()).registerOfflineMapListener(this);
        }
        
        supportInvalidateOptionsMenu();
        
        int mapType = mSharedPreferences.getInt(AsamConstants.MAP_TYPE_KEY, GoogleMap.MAP_TYPE_NORMAL);
        if (mapType != mMapType) onMapTypeChanged(mapType);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        ((Asam) getApplication()).unregisterOfflineMapListener(this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.all_asams_map_menu, menu);        
        if (mQueryType == AsamConstants.SUBREGION_QUERY || mQueryType == AsamConstants.TEXT_QUERY) {
            MenuItem queryUI = menu.findItem(R.id.all_asams_map_menu_query_ui);
            queryUI.setVisible(false);
        }
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int mapType = mSharedPreferences.getInt(AsamConstants.MAP_TYPE_KEY, 1);
        switch (mapType) {
            case GoogleMap.MAP_TYPE_SATELLITE:
                menu.findItem(R.id.map_type_satellite).setChecked(true);
                break;
            case GoogleMap.MAP_TYPE_HYBRID:
                menu.findItem(R.id.map_type_hybrid).setChecked(true);
                break;
            case AsamConstants.MAP_TYPE_OFFLINE:
                menu.findItem(R.id.map_type_offline).setChecked(true);
                break;
            default:
                menu.findItem(R.id.map_type_normal).setChecked(true);
        }
        
        offlineMapMenuItem = menu.findItem(R.id.map_type_offline);
        if (offlineMap != null) offlineMapMenuItem.setVisible(true);
        
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.map_type_normal:
                item.setChecked(!item.isChecked());
                onMapTypeChanged(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.map_type_satellite:
                item.setChecked(!item.isChecked());
                onMapTypeChanged(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.map_type_hybrid:
                item.setChecked(!item.isChecked());
                onMapTypeChanged(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.map_type_offline:
                item.setChecked(!item.isChecked());
                onMapTypeChanged(AsamConstants.MAP_TYPE_OFFLINE);
                return true;
            case R.id.all_asams_map_menu_zoom_ui:
                zoomToOriginalExtent();
                return true;
            case R.id.all_asams_map_menu_list_view_ui:
                AsamListContainer.mAsams = mAsams;
                Intent intent = new Intent(this, AsamListActivity.class);
                startActivity(intent);
                return true;
            case R.id.all_asams_map_menu_60_days_ui:
                runQuery(getString(R.string.query_60_days_text), AsamConstants.TIME_SPAN_60_DAYS);
                return true;
            case R.id.all_asams_map_menu_90_days_ui:
                runQuery(getString(R.string.query_90_days_text), AsamConstants.TIME_SPAN_90_DAYS);
                return true;
            case R.id.all_asams_map_menu_180_days_ui:
                runQuery(getString(R.string.query_180_days_text), AsamConstants.TIME_SPAN_180_DAYS);
                return true;
            case R.id.all_asams_map_menu_1_year_ui:
                runQuery(getString(R.string.query_1_year_text), AsamConstants.TIME_SPAN_1_YEAR);
                return true;
            case R.id.all_asams_map_menu_5_years_ui:
                runQuery(getString(R.string.query_5_years_text), AsamConstants.TIME_SPAN_5_YEARS);
                return true;
            case R.id.all_asams_map_menu_all_ui:
                runQuery(getString(R.string.query_all_text), AsamConstants.TIME_SPAN_ALL);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
    
    @Override
    public boolean onMarkerClick(Marker marker) {
        synchronized (Mutex) {
            for (AsamMapClusterBean mapCluster : mVisibleClusters) {
                if (marker.equals(mapCluster.getMapMarker())) {
                    Intent intent = null;
                    if (mapCluster.getNumPointsInCluster() == 1) {
                        intent = new Intent(this, AsamReportActivity.class);
                        intent.putExtra(AsamConstants.ASAM_KEY, mapCluster.getAsams().get(0));
                    }
                    else {
                        intent = new Intent(this, AsamListActivity.class);
                        AsamListContainer.mAsams = mapCluster.getAsams();
                    }
                    startActivity(intent);
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        if (mPerformMapClustering) {
            mPerformMapClustering = false;
            mPreviousZoomLevel = Math.round(mMapUI.getCameraPosition().zoom);
            
            // Use the PoffenCluster library to calculate the clusters.
            int zoomLevel = mPreviousZoomLevel;
            int numLatitudeCells = (int)(Math.round(Math.pow(2, zoomLevel)));
            int numLongitudeCells = (int)(Math.round(Math.pow(2, zoomLevel)));
            PoffenClusterCalculator<AsamBean> calculator = new PoffenClusterCalculator.Builder<AsamBean>(numLatitudeCells, numLongitudeCells).mergeLargeClusters(false).build();
            for (AsamBean asam : mAsams) {
                calculator.add(asam, new PoffenPoint(asam.getLatitude(), asam.getLongitude()));
            }
            
            mMapClusters = Collections.synchronizedList(new ArrayList<AsamMapClusterBean>());
            mVisibleClusters = new ArrayList<AsamMapClusterBean>();
            List<PoffenCluster<AsamBean>> poffenClusters = calculator.getPoffenClusters();
            for (PoffenCluster<AsamBean> poffenCluster : poffenClusters) {
                PoffenPoint poffenPoint = poffenCluster.getClusterCoordinateClosestToMean();
                AsamMapClusterBean cluster = new AsamMapClusterBean(poffenCluster.getClusterItems(), new LatLng(poffenPoint.getLatitude(), poffenPoint.getLongitude()));
                mMapClusters.add(cluster);
                mVisibleClusters.add(cluster);
                
                // Now draw it on the map.
                Marker marker;
                if (poffenCluster.getClusterItems().size() == 1) {
                    marker = mMapUI.addMarker(new MarkerOptions().position(cluster.getClusteredMapPosition()).icon(AsamConstants.PIRATE_MARKER).anchor(0.5f, 0.5f));
                }
                else {
                    BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(AsamUtils.drawNumberOnClusterMarker(this, poffenCluster.getClusterItems().size()));
                    marker = mMapUI.addMarker(new MarkerOptions().position(cluster.getClusteredMapPosition()).icon(bitmapDescriptor).anchor(0.5f, 0.5f));
                }
                cluster.setMapMarker(marker);
            }
        }
        else {
            if (mPreviousZoomLevel == -1) {
                mPreviousZoomLevel = Math.round(position.zoom);
            }
            else if (mPreviousZoomLevel != Math.round(position.zoom)) {
                mPreviousZoomLevel = Math.round(position.zoom);
                new RecalculateAndRedrawClustersBasedOnZoomLevelAsyncTask().execute(Math.round(mMapUI.getCameraPosition().zoom));
            }
            else {
                redrawMarkersOnMapBasedOnVisibleRegion();
            }
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        Thread redrawThread = new Thread() {
            
            @Override
            public void run() {
                try {
                    Thread.sleep(250);
                }
                catch (Exception ignore) {}
                runOnUiThread(new Thread() {
                    
                    @Override
                    public void run() {
                        redrawMarkersOnMapBasedOnVisibleRegion();
                    }
                });
            }
        };
        redrawThread.start();
    }
    
    @Override
    public void onCancel() {
    }

    @Override
    public void onFinish() {
    }
    
    private void redrawMarkersOnMapBasedOnVisibleRegion() {
        LatLngBounds bounds = mMapUI.getProjection().getVisibleRegion().latLngBounds;
        int zoomLevel = Math.round(mMapUI.getCameraPosition().zoom);
        final List<AsamMapClusterBean> clustersToAddToMap = new ArrayList<AsamMapClusterBean>();
        final List<AsamMapClusterBean> clustersToRemoveFromMap = new ArrayList<AsamMapClusterBean>();
        for (AsamMapClusterBean mapCluster : mMapClusters) {
            if (bounds.contains(mapCluster.getClusteredMapPosition()) || zoomLevel <= AsamConstants.ZOOM_LEVEL_TO_DRAW_ALL_CLUSTERS) {
                clustersToAddToMap.add(mapCluster);
            }
        }
        synchronized (Mutex) {
            for (AsamMapClusterBean mapCluster : mVisibleClusters) {
                if (!bounds.contains(mapCluster.getClusteredMapPosition()) && zoomLevel > AsamConstants.ZOOM_LEVEL_TO_DRAW_ALL_CLUSTERS) {
                    clustersToRemoveFromMap.add(mapCluster);
                }
            }
            for (AsamMapClusterBean mapCluster : clustersToRemoveFromMap) {
                mapCluster.getMapMarker().remove(); // Remove from map.
                mVisibleClusters.remove(mapCluster); // Remove from visible marker list.
            }
            for (AsamMapClusterBean mapCluster : clustersToAddToMap) {

                // Only add it if not already visible.
                if (!mVisibleClusters.contains(mapCluster)) {
                    Marker marker;
                    if (mapCluster.getAsams().size() == 1) {
                        marker = mMapUI.addMarker(new MarkerOptions().position(mapCluster.getClusteredMapPosition()).icon(AsamConstants.PIRATE_MARKER).anchor(0.5f, 0.5f));
                    }
                    else {
                        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(AsamUtils.drawNumberOnClusterMarker(AllAsamsMapActivity.this, mapCluster.getAsams().size()));
                        marker = mMapUI.addMarker(new MarkerOptions().position(mapCluster.getClusteredMapPosition()).icon(bitmapDescriptor).anchor(0.5f, 0.5f));
                    }
                    mapCluster.setMapMarker(marker);
                    mVisibleClusters.add(mapCluster);
                }
            }
        }
    }
    
    private void runQuery(String timeSpanText, int timeSpan) {
        mTimeSpanTitleText = timeSpanText;
        mQueryProgressDialog = ProgressDialog.show(this, getString(R.string.all_asams_map_query_progress_dialog_title_text), getString(R.string.all_asams_map_query_progress_dialog_content_text), true);
        new QueryThread(timeSpan).start();
    }
    
    private void zoomToOriginalExtent() {
        synchronized (Mutex) {
            if (mMapClusters.size() > 0) {
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                for (AsamMapClusterBean mapCluster : mMapClusters) {
                    boundsBuilder = boundsBuilder.include(mapCluster.getClusteredMapPosition());
                }
                mMapUI.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 0));
            }
        }
    }
    
    private class RecalculateAndRedrawClustersBasedOnZoomLevelAsyncTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            clearAsamMarkers();
        }
        
        @Override
        protected Void doInBackground(Integer... zoomLevel) {
            mMapClusters.clear();
            if (mAsams.size() < AsamConstants.MAX_NUM_ASAMS_FOR_NO_CLUSTERING_WITH_ZOOM_LEVEL && zoomLevel[0] > AsamConstants.MAX_ZOOM_LEVEL_FOR_CLUSTERING) {
                
                // Turn off clustering. Zoomed in enough.
                for (AsamBean asam : mAsams) {
                    List<AsamBean> asams = new ArrayList<AsamBean>();
                    asams.add(asam);
                    AsamMapClusterBean cluster = new AsamMapClusterBean(asams, new LatLng(asam.getLatitude(), asam.getLongitude()));
                    mMapClusters.add(cluster);
                }
            }
            else {
                
                // Use the PoffenCluster library to calculate the clusters.
                int numLatitudeCells = (int)(Math.round(Math.pow(2, zoomLevel[0])));
                int numLongitudeCells = (int)(Math.round(Math.pow(2, zoomLevel[0])));
                PoffenClusterCalculator<AsamBean> calculator = new PoffenClusterCalculator.Builder<AsamBean>(numLatitudeCells, numLongitudeCells).mergeLargeClusters(false).build();
                for (AsamBean asam : mAsams) {
                    calculator.add(asam, new PoffenPoint(asam.getLatitude(), asam.getLongitude()));
                }
                
                List<PoffenCluster<AsamBean>> poffenClusters = calculator.getPoffenClusters();
                for (PoffenCluster<AsamBean> poffenCluster : poffenClusters) {
                    PoffenPoint poffenPoint = poffenCluster.getClusterCoordinateClosestToMean();
                    AsamMapClusterBean cluster = new AsamMapClusterBean(poffenCluster.getClusterItems(), new LatLng(poffenPoint.getLatitude(), poffenPoint.getLongitude()));
                    mMapClusters.add(cluster);
                }
            }
            synchronized (Mutex) {
                mVisibleClusters.clear();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void empty) {
            redrawMarkersOnMapBasedOnVisibleRegion();
        }
    }
    
    private class QueryThread extends Thread {
        
        private int mTimeSpan;
        List<Integer> mSelectedSubregions;
        TextQueryParametersBean mTextQueryParameters;
        
        QueryThread(int timeSpan) {
            mTimeSpan = timeSpan;
        }
        
        QueryThread(int timeSpan, List<Integer> selectedSubregions) {
            mTimeSpan = timeSpan;
            mSelectedSubregions = selectedSubregions;
        }
        
        QueryThread(TextQueryParametersBean textQueryParameters) {
            mTextQueryParameters = textQueryParameters;
        }
        
        @Override
        public void run() {
            Context context = AllAsamsMapActivity.this;
            String json = null;
            SQLiteDatabase db = null;
            if (!SyncTime.isSynched(context)) {
                try {
                    AsamWebService webService = new AsamWebService(context);
                    json = webService.query();
                    if (!AsamUtils.isEmpty(json)) {
                        AsamJsonParser parser = new AsamJsonParser();
                        List<AsamBean> asams = parser.parseJson(json);
                        if (asams.size() > 0) {
                        
                            // Do a diff of what the web service returned and what's currently in the db.
                            AsamDbHelper dbHelper = new AsamDbHelper(context);
                            db = dbHelper.getWritableDatabase();
                            asams = dbHelper.removeDuplicates(db, asams);
                            dbHelper.insertAsams(db, asams);
                        }
                    }
                    SyncTime.finishedSync(context);
                }
                catch (Exception caught) {
                    AsamLog.e(AllAsamsMapActivity.class.getName() + ":There was an error parsing ASAM feed", caught);
                    mQueryError = true;
                }
                finally {
                    if (db != null) {
                        db.close();
                        db = null;
                    }
                }
            }
            
            try {
                
                // Query for the time period.
                Calendar timePeriod = new GregorianCalendar();
                if (mTimeSpan == AsamConstants.TIME_SPAN_1_YEAR) {
                    timePeriod.add(Calendar.YEAR, -1);
                }
                else if (mTimeSpan == AsamConstants.TIME_SPAN_5_YEARS) {
                    timePeriod.add(Calendar.YEAR, -5);
                }
                else {
                    timePeriod.add(Calendar.DAY_OF_YEAR, -mTimeSpan);
                }
                synchronized (Mutex) {
                    mAsams.clear();
                    AsamDbHelper dbHelper = new AsamDbHelper(context);
                    db = dbHelper.getReadableDatabase();
                    if (mSelectedSubregions != null) {
                        mAsams.addAll(dbHelper.queryByTimeAndSubregions(db, timePeriod, mSelectedSubregions));
                    }
                    else if (mTextQueryParameters != null) {
                        mAsams.addAll(dbHelper.queryByText(db, mTextQueryParameters));
                    }
                    else {
                        mAsams.addAll(dbHelper.queryByTime(db, timePeriod));
                    }
                }
            }
            finally {
                if (db != null) {
                    db.close();
                }
            }
            mQueryHandler.sendEmptyMessage(0);
        }
    }
    
    private void clearAsamMarkers() {        
        for (AsamMapClusterBean mapCluster : mVisibleClusters) {
            mapCluster.getMapMarker().remove(); // Remove from map.
        }
    }

    public void onMapTypeChanged(int mapType) {
        mMapType = mapType;
        
        // Show/hide the offline alert fragement based on map type
        if (mMapType == AsamConstants.MAP_TYPE_OFFLINE) {
            getSupportFragmentManager()
                .beginTransaction()
                .hide(offlineAlertFragment)
                .commit();
        } else {
            getSupportFragmentManager()
                .beginTransaction()
                .show(offlineAlertFragment)
                .commit(); 
        }

        
        // Change the map
        if (mMapType == AsamConstants.MAP_TYPE_OFFLINE) {
            if (offlineMap != null) {
                offlineMap.setVisible(true);
            }
        } else {
            mMapUI.setMapType(mMapType);
            if (offlineMap != null) {
                offlineMap.setVisible(false);
            }
        }
        
        // Update shared preferences
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(AsamConstants.MAP_TYPE_KEY, mMapType);
        editor.commit();   
    }

    @Override
    public void onOfflineFeaturesLoaded(Collection<Geometry> offlineFeatures) {
        offlineMap = new OfflineMap(getApplicationContext(), mMapUI, offlineFeatures);
        
        if (mMapType == AsamConstants.MAP_TYPE_OFFLINE) {
            offlineMap.setVisible(true);
        }
        
        if (offlineMapMenuItem != null) offlineMapMenuItem.setVisible(true);
    }

    @Override
    public void onOfflineBannerClick() {
        onMapTypeChanged(AsamConstants.MAP_TYPE_OFFLINE);
        supportInvalidateOptionsMenu();
    }
}
