package mil.nga.giat.asam.map;

import android.animation.LayoutTransition;
import android.app.Activity;
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
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
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

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import mil.nga.giat.asam.Asam;
import mil.nga.giat.asam.AsamListActivity;
import mil.nga.giat.asam.InfoActivity;
import mil.nga.giat.asam.InfoDialogFragment;
import mil.nga.giat.asam.LegalTabletActivity;
import mil.nga.giat.asam.PreferencesActivity;
import mil.nga.giat.asam.PreferencesDialogFragment;
import mil.nga.giat.asam.PreferencesDialogFragment.OnPreferencesDialogDismissedListener;
import mil.nga.giat.asam.R;
import mil.nga.giat.asam.connectivity.OfflineBannerFragment;
import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.filter.FilterActivity;
import mil.nga.giat.asam.filter.FilterAdvancedActivity;
import mil.nga.giat.asam.filter.FilterParameters;
import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.model.AsamJsonParser;
import mil.nga.giat.asam.model.AsamMapClusterBean;
import mil.nga.giat.asam.net.AsamWebService;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamListContainer;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.AsamUtils;
import mil.nga.giat.asam.util.SyncTime;
import mil.nga.giat.poffencluster.PoffenCluster;
import mil.nga.giat.poffencluster.PoffenClusterCalculator;
import mil.nga.giat.poffencluster.PoffenPoint;


public class AsamMapActivity extends AppCompatActivity implements OnCameraChangeListener, OnMarkerClickListener, CancelableCallback, OnPreferencesDialogDismissedListener, Asam.OnOfflineFeaturesListener, OfflineBannerFragment.OnOfflineBannerClick {

    private static class QueryHandler extends Handler {

        WeakReference<AsamMapActivity> mAllAsamsMapTabletActivity;

        QueryHandler(AsamMapActivity asamMapActivity) {
            mAllAsamsMapTabletActivity = new WeakReference<AsamMapActivity>(asamMapActivity);
        }

        @Override
        public void handleMessage(Message message) {
            AsamMapActivity asamMapActivity = mAllAsamsMapTabletActivity.get();

            asamMapActivity.setFilterStatus(asamMapActivity.mDateRangeText, asamMapActivity.mTotalAsamsText);

            asamMapActivity.mQueryProgressDialog.dismiss();
            if (asamMapActivity.mQueryError) {
                asamMapActivity.mQueryError = false;
                Toast.makeText(asamMapActivity, asamMapActivity.getString(R.string.all_asams_map_tablet_query_error_text), Toast.LENGTH_LONG).show();
            }

            asamMapActivity.clearAsamMarkers();
            if (asamMapActivity.mAsams.size() == 1) {

                // Camera position changing so redraw will be triggered in onCameraChange.
                if (asamMapActivity.mPerformBoundsAdjustmentWithQuery) {
                    asamMapActivity.mPerformMapClustering = true;
                    AsamBean asam = asamMapActivity.mAsams.get(0);
                    asamMapActivity.mMapUI.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(asam.getLatitude(), asam.getLongitude())).zoom(AsamConstants.SINGLE_ASAM_ZOOM_LEVEL).build()));
                }
            }
            else if (asamMapActivity.mAsams.size() > 1) {

                // Camera position changing so redraw will be triggered in onCameraChange.
                if (asamMapActivity.mPerformBoundsAdjustmentWithQuery) {
                    asamMapActivity.mPerformMapClustering = true;
                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                    for (AsamBean asam : asamMapActivity.mAsams) {
                        boundsBuilder = boundsBuilder.include(new LatLng(asam.getLatitude(), asam.getLongitude()));
                    }
                    asamMapActivity.mMapUI.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 0), asamMapActivity);
                }
            }
            else {
                Toast.makeText(asamMapActivity, asamMapActivity.getString(R.string.all_asams_map_no_asams_text), Toast.LENGTH_LONG).show();
            }

            // Camera position not changing so redraw won't be triggered in onCameraChange.
            if (!asamMapActivity.mPerformBoundsAdjustmentWithQuery && asamMapActivity.mAsams.size() > 0) {

                // Use the PoffenCluster library to calculate the clusters.
                int zoomLevel = Math.round(asamMapActivity.mMapUI.getCameraPosition().zoom);
                LatLngBounds bounds = asamMapActivity.mMapUI.getProjection().getVisibleRegion().latLngBounds;
                int numLatitudeCells = (int)(Math.round(Math.pow(2, zoomLevel)));
                int numLongitudeCells = (int)(Math.round(Math.pow(2, zoomLevel)));
                PoffenClusterCalculator<AsamBean> calculator = new PoffenClusterCalculator.Builder<AsamBean>(numLatitudeCells, numLongitudeCells).mergeLargeClusters(false).build();
                for (AsamBean asam : asamMapActivity.mAsams) {
                    calculator.add(asam, new PoffenPoint(asam.getLatitude(), asam.getLongitude()));
                }

                asamMapActivity.mMapClusters = Collections.synchronizedList(new ArrayList<AsamMapClusterBean>());
                synchronized (asamMapActivity.Mutex) {
                    asamMapActivity.mVisibleClusters = new ArrayList<AsamMapClusterBean>();
                }
                List<PoffenCluster<AsamBean>> poffenClusters = calculator.getPoffenClusters();
                synchronized (asamMapActivity.Mutex) {
                    for (PoffenCluster<AsamBean> poffenCluster : poffenClusters) {
                        PoffenPoint poffenPoint = poffenCluster.getClusterCoordinateClosestToMean();
                        AsamMapClusterBean cluster = new AsamMapClusterBean(poffenCluster.getClusterItems(), new LatLng(poffenPoint.getLatitude(), poffenPoint.getLongitude()));
                        asamMapActivity.mMapClusters.add(cluster);

                        if (bounds.contains(cluster.getClusteredMapPosition()) || zoomLevel <= AsamConstants.ZOOM_LEVEL_TO_DRAW_ALL_CLUSTERS) {
                            asamMapActivity.mVisibleClusters.add(cluster);

                            // Now draw it on the map.
                            Marker marker;
                            if (poffenCluster.getClusterItems().size() == 1) {
                                marker = asamMapActivity.mMapUI.addMarker(new MarkerOptions().position(cluster.getClusteredMapPosition()).icon(AsamConstants.PIRATE_MARKER).anchor(0.5f, 0.5f));
                            }
                            else {
                                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(AsamUtils.drawNumberOnClusterMarker(asamMapActivity, poffenCluster.getClusterItems().size()));
                                marker = asamMapActivity.mMapUI.addMarker(new MarkerOptions().position(cluster.getClusteredMapPosition()).icon(bitmapDescriptor).anchor(0.5f, 0.5f));
                            }
                            cluster.setMapMarker(marker);
                        }
                    }
                }
            }
        }
    }

    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 0;
    private static final int LIST_ACTIVITY_REQUEST_CODE = 1;
    public static final String SEARCH_PARAMETERS = "SEARCH_PARAMETERS";
    public static final String MAP_LOCATION = "MAP_LOCATION";

    private static final int TOTAL_TIME_SLIDER_TICKS = 1000;
    private static final SimpleDateFormat DATE_RANGE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    private static final String DATE_RANGE_PATTERN = "%s to %s";
    private static final String TOTAL_ASAMS_PATTERN = "%5d of %d ASAMs";


    private String mDateRangeText;
    private String mTotalAsamsText;
    private final Object Mutex = new Object();
    private volatile boolean mQueryError;
    private volatile boolean mPerformMapClustering;
    private volatile boolean mPerformBoundsAdjustmentWithQuery;
    private List<AsamBean> mAsams;
    private List<AsamMapClusterBean> mMapClusters;
    private GoogleMap mMapUI;
    private int mMapType;
    private Collection<Geometry> offlineGeometries = null;

    private TextView mDateRangeTextViewUI;
    private TextView mTotalAsamsTextViewUI;
    private TextView mQueryModeMessageTextViewUI;
    private LinearLayout mQueryModeMessageContainerUI;
    private TextView mFilterStatus;
    private SeekBar mTimeSliderUI;

    private Date mEarliestAsamDate;
    private ProgressDialog mQueryProgressDialog;
    private QueryHandler mQueryHandler;
    private FilterParameters mFilterParameters;
    private Date mTextQueryDateEarliest;
    private Date mTextQueryDateLatest;
    private int mPreviousZoomLevel;
    private List<AsamMapClusterBean> mVisibleClusters;
    private SharedPreferences mSharedPreferences;
    private OfflineMap offlineMap;
    private MenuItem offlineMap110mMenuItem;
    private OfflineBannerFragment offlineAlertFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(AsamMapActivity.class.getName() + ":onCreate");
        setContentView(R.layout.map);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mQueryError = false;
        mPerformMapClustering = false;
        mAsams = new ArrayList<AsamBean>();
        mMapClusters = Collections.synchronizedList(new ArrayList<AsamMapClusterBean>());
        mVisibleClusters = new ArrayList<AsamMapClusterBean>();

        mMapUI = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.all_asams_map_tablet_map_view_ui)).getMap();
        mMapUI.setOnCameraChangeListener(this);
        mMapUI.setOnMarkerClickListener(this);

        offlineAlertFragment = new OfflineBannerFragment();
        getSupportFragmentManager().beginTransaction()
            .add(android.R.id.content, offlineAlertFragment)
            .commit();

        mPreviousZoomLevel = -1;

        mQueryModeMessageTextViewUI = (TextView)findViewById(R.id.all_asams_map_tablet_query_mode_message_text_view_ui);
        mQueryModeMessageContainerUI = (LinearLayout)findViewById(R.id.all_asams_map_tablet_query_mode_message_container_ui);
        final ImageView filterResultsToggle = (ImageView) findViewById(R.id.filter_results_toggle);
        mQueryModeMessageContainerUI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQueryModeMessageTextViewUI.getVisibility() == View.VISIBLE) {
                    mQueryModeMessageTextViewUI.setVisibility(View.GONE);
                } else {
                    mQueryModeMessageTextViewUI.setVisibility(View.VISIBLE);
                }
            }
        });
        mQueryModeMessageContainerUI.getLayoutTransition().addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
            }

            @Override
            public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                if (transitionType == LayoutTransition.APPEARING) {
                    filterResultsToggle.setImageResource(R.drawable.ic_keyboard_arrow_up_white);
                } else if (transitionType == LayoutTransition.CHANGE_DISAPPEARING) {
                    filterResultsToggle.setImageResource(R.drawable.ic_keyboard_arrow_down_white);
                }
            }
        });

        Calendar timePeriod = new GregorianCalendar();
        timePeriod.add(Calendar.YEAR, -1);
        View dateRangeView = findViewById(R.id.all_asams_map_tablet_date_range);
        if (dateRangeView != null) {
            setupDateRangeView(dateRangeView);
            mTimeSliderUI.setProgress(calculateTimeSliderTicksFromDate(timePeriod.getTime()));
        }

        mFilterStatus = (TextView)findViewById(R.id.all_asams_map_query_feedback_text_ui);

        mQueryHandler = new QueryHandler(this);

        mFilterParameters = new FilterParameters(FilterParameters.Type.SIMPLE);
        mFilterParameters.mTimeInterval = 365;
        onFilter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.all_asams_map_tablet_menu, menu);

        int mapType = mSharedPreferences.getInt(AsamConstants.MAP_TYPE_KEY, 1);
        switch (mapType) {
            case GoogleMap.MAP_TYPE_SATELLITE:
                menu.findItem(R.id.map_type_satellite).setChecked(true);
                break;
            case GoogleMap.MAP_TYPE_HYBRID:
                menu.findItem(R.id.map_type_hybrid).setChecked(true);
                break;
            case AsamConstants.MAP_TYPE_OFFLINE_110M:
                menu.findItem(R.id.map_type_offline_110m).setChecked(true);
                break;
            default:
                menu.findItem(R.id.map_type_normal).setChecked(true);

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
            case AsamConstants.MAP_TYPE_OFFLINE_110M:
                menu.findItem(R.id.map_type_offline_110m).setChecked(true);
                break;
            default:
                menu.findItem(R.id.map_type_normal).setChecked(true);
        }

        offlineMap110mMenuItem = menu.findItem(R.id.map_type_offline_110m);

        if (offlineGeometries != null) offlineMap110mMenuItem.setVisible(true);

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onResume() {
        super.onResume();

        ((Asam) getApplication()).registerOfflineMapListener(this);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
            case R.id.map_type_offline_110m:
                item.setChecked(!item.isChecked());
                onMapTypeChanged(AsamConstants.MAP_TYPE_OFFLINE_110M);
                return true;
            case R.id.all_asams_map_menu_list_view_ui: {
                AsamListContainer.mAsams = mAsams;
                Intent intent = new Intent(this, AsamListActivity.class);
                intent.putExtra(AsamListActivity.ALWAYS_SHOW_LIST_KEY, true);
                startActivityForResult(intent, LIST_ACTIVITY_REQUEST_CODE);
                return true;
            }
            case R.id.all_asams_map_tablet_menu_settings_ui: {
                DialogFragment dialogFragment = PreferencesDialogFragment.newInstance();
                dialogFragment.show(getSupportFragmentManager(), AsamConstants.PREFERENCES_DIALOG_TAG);
                return true;
            }
            case R.id.all_asams_map_tablet_menu_info_ui: {
                DialogFragment dialogFragment = InfoDialogFragment.newInstance();
                dialogFragment.show(getSupportFragmentManager(), AsamConstants.INFO_DIALOG_TAG);
                return true;
            }
            case R.id.asam_about: {
                //TODO should launch same info thing as tablet, use different layout
                Intent intent = new Intent(this, InfoActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.asam_settings: {
                //TODO should launch same preference as tablet, use different layout
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.all_asams_map_menu_search_ui: {
                Intent intent = launchAdvancedFilter() ?
                    new Intent(this, FilterAdvancedActivity.class) :
                    new Intent(this, FilterActivity.class);

                intent.putExtra(SEARCH_PARAMETERS, mFilterParameters);
                startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        synchronized (Mutex) {
            for (AsamMapClusterBean mapCluster : mVisibleClusters) {
                if (marker.equals(mapCluster.getMapMarker())) {
                    AsamListContainer.mAsams = mapCluster.getAsams();
                    Intent intent = new Intent(this, AsamListActivity.class);
                    startActivity(intent);
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        AsamLog.i(AsamMapActivity.class.getName() + ":onCameraChange");

        if (mPerformMapClustering) {
            mPerformMapClustering = false;
            mPreviousZoomLevel = Math.round(mMapUI.getCameraPosition().zoom);

            // Use the PoffenCluster library to calculate the clusters.
            int zoomLevel = mPreviousZoomLevel;
            int numLatitudeCells = (int)(Math.round(Math.pow(2, zoomLevel)));
            int numLongitudeCells = (int)(Math.round(Math.pow(2, zoomLevel)));
            LatLngBounds bounds = mMapUI.getProjection().getVisibleRegion().latLngBounds;
            PoffenClusterCalculator<AsamBean> calculator = new PoffenClusterCalculator.Builder<AsamBean>(numLatitudeCells, numLongitudeCells).mergeLargeClusters(false).build();
            for (AsamBean asam : mAsams) {
                calculator.add(asam, new PoffenPoint(asam.getLatitude(), asam.getLongitude()));
            }

            mMapClusters = Collections.synchronizedList(new ArrayList<AsamMapClusterBean>());
            synchronized (Mutex) {
                mVisibleClusters = new ArrayList<AsamMapClusterBean>();
            }
            List<PoffenCluster<AsamBean>> poffenClusters = calculator.getPoffenClusters();
            synchronized (Mutex) {
                for (PoffenCluster<AsamBean> poffenCluster : poffenClusters) {
                    PoffenPoint poffenPoint = poffenCluster.getClusterCoordinateClosestToMean();
                    AsamMapClusterBean cluster = new AsamMapClusterBean(poffenCluster.getClusterItems(), new LatLng(poffenPoint.getLatitude(), poffenPoint.getLongitude()));
                    mMapClusters.add(cluster);
                    if (bounds.contains(cluster.getClusteredMapPosition()) || zoomLevel <= AsamConstants.ZOOM_LEVEL_TO_DRAW_ALL_CLUSTERS) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (SEARCH_ACTIVITY_REQUEST_CODE): {
                if (resultCode == Activity.RESULT_OK) {
                    mFilterParameters = data.getParcelableExtra(SEARCH_PARAMETERS);
                    onFilter();
                }
                break;
            }
            case (LIST_ACTIVITY_REQUEST_CODE): {
                if (resultCode == Activity.RESULT_OK) {
                    LatLng latLng = data.getParcelableExtra(MAP_LOCATION);
                    mMapUI.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                }
                break;
            }
        }
    }

    public void onFilter() {
        DialogFragment dialogFragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(AsamConstants.TEXT_QUERY_DIALOG_TAG);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }

        mTextQueryDateEarliest = initAndGetEarliestAsamDate();
        mTextQueryDateLatest = new Date();

        if (mFilterParameters.mType == FilterParameters.Type.SIMPLE) {
            if (mFilterParameters.mTimeInterval != null) {
                mTextQueryDateEarliest = mFilterParameters.getStartDateFromInterval();
                mTextQueryDateLatest = new Date();
            }
        } else {
            // Populate the from and to dates for the text query.
            if (StringUtils.isNotBlank(mFilterParameters.mDateFrom)) {
                try {
                    mTextQueryDateEarliest = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.parse(mFilterParameters.mDateFrom);
                } catch (ParseException caught) {
                    mTextQueryDateEarliest = initAndGetEarliestAsamDate();
                }
            } else {
                mTextQueryDateEarliest = initAndGetEarliestAsamDate();
            }

            if (StringUtils.isNotBlank(mFilterParameters.mDateTo)) {
                try {
                    mTextQueryDateLatest = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.parse(mFilterParameters.mDateTo);
                } catch (ParseException caught) {
                    mTextQueryDateLatest = new Date();
                }
            } else {
                mTextQueryDateLatest = new Date();
            }
        }

        if (mQueryModeMessageContainerUI != null) {
            mQueryModeMessageContainerUI.setVisibility(View.VISIBLE);
        }

        if (mQueryModeMessageContainerUI != null) {
            if (mFilterParameters.isEmpty()) {
                mQueryModeMessageContainerUI.setVisibility(View.INVISIBLE);
            } else {
                mQueryModeMessageContainerUI.setVisibility(View.VISIBLE);
                mQueryModeMessageTextViewUI.setText(Html.fromHtml(String.format(getResources().getString(R.string.all_asams_map_tablet_text_query_mode_message_text), mFilterParameters.getParametersAsFormattedHtml())));
            }
        }

        setTimeSlider(null);
        mQueryProgressDialog = ProgressDialog.show(this, getString(R.string.all_asams_map_tablet_query_progress_dialog_title_text), getString(R.string.all_asams_map_tablet_query_progress_dialog_content_text), true);
        new QueryThread().start();
    }

    public void onPreferencesDialogDismissed(boolean hideDisclaimer) {
        DialogFragment dialogFragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(AsamConstants.PREFERENCES_DIALOG_TAG);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AsamConstants.HIDE_DISCLAIMER_KEY, hideDisclaimer);
        editor.commit();
    }

    public void syncButtonClicked(View view) {
        PreferencesDialogFragment dialogFragment = (PreferencesDialogFragment)getSupportFragmentManager().findFragmentByTag(AsamConstants.PREFERENCES_DIALOG_TAG);
        if (dialogFragment != null) {
            if (SyncTime.isSynched(this)) {
                Toast.makeText(this, getString(R.string.preferences_no_sync_description_text), Toast.LENGTH_LONG).show();
            }
            else {
                mQueryProgressDialog = ProgressDialog.show(this, getString(R.string.all_asams_map_tablet_query_progress_dialog_title_text), getString(R.string.all_asams_map_tablet_query_progress_dialog_content_text), true);
                new QueryThread(true).start();
            }
        }
    }

    public void legalRowClicked(View view) {
        DialogFragment dialogFragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(AsamConstants.INFO_DIALOG_TAG);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        Intent intent = new Intent(this, LegalTabletActivity.class);
        startActivity(intent);
    }

    public void emailLinkClicked(View view) {
        DialogFragment dialogFragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(AsamConstants.INFO_DIALOG_TAG);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        String[] recipients = { getString(R.string.info_fragment_email_address) };
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.info_fragment_email_subject_text));
        intent.setType("plain/text");
        startActivity(intent);
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
                    } else {
                        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(AsamUtils.drawNumberOnClusterMarker(AsamMapActivity.this, mapCluster.getAsams().size()));
                        marker = mMapUI.addMarker(new MarkerOptions().position(mapCluster.getClusteredMapPosition()).icon(bitmapDescriptor).anchor(0.5f, 0.5f));
                    }
                    mapCluster.setMapMarker(marker);
                    mVisibleClusters.add(mapCluster);
                }
            }
        }
    }

    private Date initAndGetEarliestAsamDate() {
        if (mEarliestAsamDate == null) {
            SQLiteDatabase db = null;
            try {
                AsamDbHelper dbHelper = new AsamDbHelper(this);
                db = dbHelper.getReadableDatabase();
                if (mEarliestAsamDate == null) {
                    mEarliestAsamDate = dbHelper.getMinOccurrenceDate(db);
                }
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MONTH, Calendar.MAY);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.YEAR, 1978);
                Date earliestDate = calendar.getTime();
                if (mEarliestAsamDate.before(earliestDate)) {
                    mEarliestAsamDate = earliestDate;
                }
            }
            finally {
                if (db != null) {
                    db.close();
                }
            }
        }
        return mEarliestAsamDate;
    }

    private Date calculateQueryDateFromTimeSlider(int timeSliderTick) {
        Date currentDate = new Date();
        long totalDateRangeInMilliseconds = currentDate.getTime() - initAndGetEarliestAsamDate().getTime();
        long millisecondsFromLatestAsamDate = Math.round(((double)totalDateRangeInMilliseconds / TOTAL_TIME_SLIDER_TICKS) * timeSliderTick);
        return new Date(currentDate.getTime() - millisecondsFromLatestAsamDate);
    }

    private Date calculateTextQueryDateFromTimeSlider(int timeSliderTick) {
        long totalDateRangeInMilliseconds = mTextQueryDateLatest.getTime() - mTextQueryDateEarliest.getTime();
        long millisecondsFromTextQueryDateFrom = Math.round(((double)totalDateRangeInMilliseconds / TOTAL_TIME_SLIDER_TICKS) * timeSliderTick);
        return new Date(mTextQueryDateLatest.getTime() - millisecondsFromTextQueryDateFrom);
    }

    private int calculateTimeSliderTicksFromDate(Date date) {
        Date currentDate = new Date();
        long totalDateRangeInMilliseconds = currentDate.getTime() - initAndGetEarliestAsamDate().getTime();
        double percentage = (currentDate.getTime() - date.getTime()) / (double)totalDateRangeInMilliseconds;
        return (int)Math.round(TOTAL_TIME_SLIDER_TICKS * percentage);
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
            } else {

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

        private static final int TIME_SLIDER_QUERY = 0;
        private static final int TIME_PERIOD_QUERY = 1;
        private static final int STANDARD_QUERY = 2;
        private int mQueryType;
        private int mTimeSliderTick;
        private Calendar mTimePeriod;
        private boolean mSynchronizationQuery;

        QueryThread() {
            mQueryType = STANDARD_QUERY;
            mPerformBoundsAdjustmentWithQuery = true;
            mSynchronizationQuery = false;
        }

        QueryThread(boolean synchronizationQuery) {
            mSynchronizationQuery = synchronizationQuery;
        }

        QueryThread(int timeSliderTick) {
            mQueryType = TIME_SLIDER_QUERY;
            mTimeSliderTick = timeSliderTick;
            mPerformBoundsAdjustmentWithQuery = false;
            mSynchronizationQuery = false;
        }

        QueryThread(Calendar timePeriod) {
            mQueryType = TIME_PERIOD_QUERY;
            mTimePeriod = timePeriod;
            mPerformBoundsAdjustmentWithQuery = true;
            mSynchronizationQuery = false;
        }

        @Override
        public void run() {
            Context context = AsamMapActivity.this;
            String json = null;
            SQLiteDatabase db = null;
            if (!SyncTime.isSynched(context)) {
                try {
                    AsamWebService webService = new AsamWebService(context);
                    json = webService.query();
                    if (StringUtils.isNotBlank(json)) {
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
                    AsamLog.e(AsamMapActivity.class.getName() + ":There was an error parsing ASAM feed", caught);
                    mQueryError = true;
                }
                finally {
                    if (db != null) {
                        db.close();
                        db = null;
                    }
                }
            }

            if (mSynchronizationQuery) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mQueryProgressDialog.dismiss();
                        if (!mQueryError) {
                            Toast.makeText(AsamMapActivity.this, getString(R.string.preferences_sync_complete_description_text), Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(AsamMapActivity.this, getString(R.string.preferences_error_sync_description_text), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                return;
            }

            try {
                // Query for the time period based on the slider.
                synchronized (Mutex) {
                    mAsams.clear();
                    AsamDbHelper dbHelper = new AsamDbHelper(context);
                    db = dbHelper.getReadableDatabase();
                    long totalNumberOfAsams = dbHelper.getTotalNumberOfAsams(db);

                    FilterParameters parameters = FilterParameters.newInstance(mFilterParameters);

                    if (mTextQueryDateLatest != null) {
                        parameters.mDateTo = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(mTextQueryDateLatest);
                    }

                    if (mQueryType == STANDARD_QUERY ) {
                        if (mTextQueryDateEarliest != null) {
                            parameters.mDateFrom = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(mTextQueryDateEarliest);
                        }
                    } else if (mQueryType == TIME_SLIDER_QUERY) {
                        if (mTimeSliderTick == 0) {
                            parameters.mDateFrom = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(mTextQueryDateLatest);
                        } else if (mTimeSliderTick == TOTAL_TIME_SLIDER_TICKS - 1) {
                            parameters.mDateFrom = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(mTextQueryDateEarliest);
                        } else {
                            Date dateFromSlider = mTextQueryDateEarliest != null && mTextQueryDateLatest != null ? calculateTextQueryDateFromTimeSlider(mTimeSliderTick) : calculateQueryDateFromTimeSlider(mTimeSliderTick);
                            parameters.mDateFrom = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(dateFromSlider);
                        }
                    }
                    mAsams.addAll(dbHelper.queryWithFilters(db, parameters));

                    // TODO tablet specific, needs to move to a fragment
                    if (mDateRangeTextViewUI != null) {
                        Date earliest = mTextQueryDateEarliest != null ? mTextQueryDateEarliest : initAndGetEarliestAsamDate();
                        Date latest = mTextQueryDateLatest != null ? mTextQueryDateLatest : new Date();
                        if (mQueryType == STANDARD_QUERY) {
                            mDateRangeText = String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(latest), DATE_RANGE_FORMAT.format(earliest));
                        } else if (mQueryType == TIME_SLIDER_QUERY) {
                            if (mTimeSliderTick == 0) {
                                mDateRangeText = String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(latest), DATE_RANGE_FORMAT.format(latest));
                            } else if (mTimeSliderTick == TOTAL_TIME_SLIDER_TICKS - 1) {
                                mDateRangeText = String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(latest), DATE_RANGE_FORMAT.format(earliest));
                            } else {
                                Date dateFromSlider = mTextQueryDateEarliest != null && mTextQueryDateLatest != null ? calculateTextQueryDateFromTimeSlider(mTimeSliderTick) : calculateQueryDateFromTimeSlider(mTimeSliderTick);
                                mDateRangeText = String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(latest), DATE_RANGE_FORMAT.format(dateFromSlider));
                            }
                        }

                        mTotalAsamsText = String.format(TOTAL_ASAMS_PATTERN, mAsams.size(), totalNumberOfAsams);
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

        // Show/hide the offline alert fragment based on map type
        if (mMapType == AsamConstants.MAP_TYPE_OFFLINE_110M) {
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
        if (mMapType == AsamConstants.MAP_TYPE_OFFLINE_110M) {
        	if (offlineMap != null) offlineMap.clear();

        	offlineMap = new OfflineMap(this, mMapUI, offlineGeometries);
        } else {
        	if (offlineMap != null) {
        		offlineMap.clear();
        		offlineMap = null;
        	}

            mMapUI.setMapType(mMapType);
        }

        // Update shared preferences
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(AsamConstants.MAP_TYPE_KEY, mMapType);
        editor.commit();
    }

    @Override
    public void onOfflineFeaturesLoaded(Collection<Geometry> offlineGeometries) {
    	this.offlineGeometries = offlineGeometries;

        if (offlineMap110mMenuItem != null) offlineMap110mMenuItem.setVisible(true);

    	if (offlineMap == null && mMapType == AsamConstants.MAP_TYPE_OFFLINE_110M) {
    		if (offlineMap != null) offlineMap.clear();
    		offlineMap = new OfflineMap(this, mMapUI, offlineGeometries);
    	}
    }

    @Override
    public void onOfflineBannerClick() {
        onMapTypeChanged(AsamConstants.MAP_TYPE_OFFLINE_110M);
        supportInvalidateOptionsMenu();
    }

    private boolean launchAdvancedFilter() {
        return mFilterParameters != null && mFilterParameters.mType == FilterParameters.Type.ADVANCED;
    }

    private void setupDateRangeView(View dateRangeView) {
        mDateRangeTextViewUI = (TextView)dateRangeView.findViewById(R.id.all_asams_map_tablet_date_range_text_view_ui);
        mTotalAsamsTextViewUI = (TextView)findViewById(R.id.all_asams_map_tablet_total_asams_text_view_ui);
        mTimeSliderUI = (SeekBar)dateRangeView.findViewById(R.id.all_asams_map_tablet_time_slider_ui);
        mTimeSliderUI.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Date earliest = mTextQueryDateEarliest != null ? mTextQueryDateEarliest : initAndGetEarliestAsamDate();
                Date latest = mTextQueryDateLatest != null ? mTextQueryDateLatest : new Date();

                if (progress == TOTAL_TIME_SLIDER_TICKS - 1) {
                    mDateRangeTextViewUI.setText(String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(latest), DATE_RANGE_FORMAT.format(earliest)));
                } else if (progress == 0) {
                    mDateRangeTextViewUI.setText(String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(latest), DATE_RANGE_FORMAT.format(latest)));
                } else {
                    Date dateFromSlider = mTextQueryDateEarliest != null && mTextQueryDateLatest != null ? calculateTextQueryDateFromTimeSlider(progress) : calculateQueryDateFromTimeSlider(progress);
                    mDateRangeTextViewUI.setText(String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(latest), DATE_RANGE_FORMAT.format(dateFromSlider)));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mQueryProgressDialog = ProgressDialog.show(AsamMapActivity.this, getString(R.string.all_asams_map_tablet_query_progress_dialog_title_text), getString(R.string.all_asams_map_tablet_query_progress_dialog_content_text), true);
                new QueryThread(seekBar.getProgress()).start();
            }

        });
    }

    private void setTimeSlider(Date time) {
        if (mTimeSliderUI != null) {
            if (time == null) {
                mTimeSliderUI.setProgress(TOTAL_TIME_SLIDER_TICKS - 1); // All of the time will be shown.
            } else {
                mTimeSliderUI.setProgress(calculateTimeSliderTicksFromDate(time));
            }
        }
    }

    private void setFilterStatus(String dateRangeText, String totalAsamsText) {
        if (mDateRangeTextViewUI != null) {
            mDateRangeTextViewUI.setText(dateRangeText);
        }

        if (mTotalAsamsTextViewUI != null) {
            mTotalAsamsTextViewUI.setText(totalAsamsText);
        }

        // Now set the feedback title.
        StringBuilder feedbackText = new StringBuilder("");
        if (mAsams.size() == 1) {
            feedbackText.append(getString(R.string.all_asams_map_1_asam_text_with_timespan));
        } else {
            feedbackText.append(String.format(getString(R.string.all_asams_map_multiple_asams_text_with_timespan), mAsams.size()));
        }

        mFilterStatus.setText(feedbackText.toString());
    }

}
