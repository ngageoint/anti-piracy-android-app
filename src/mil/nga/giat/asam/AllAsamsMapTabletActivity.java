package mil.nga.giat.asam;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import mil.nga.giat.asam.DisclaimerDialogFragment.OnDisclaimerDialogDismissedListener;
import mil.nga.giat.asam.PreferencesDialogFragment.OnPreferencesDialogDismissedListener;
import mil.nga.giat.asam.TextQueryDialogFragment.OnTextQueryListener;
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
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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


public class AllAsamsMapTabletActivity extends ActionBarActivity implements OnCameraChangeListener, OnMarkerClickListener, CancelableCallback, OnTextQueryListener, OnDisclaimerDialogDismissedListener, OnPreferencesDialogDismissedListener {

    private static class QueryHandler extends Handler {
        
        WeakReference<AllAsamsMapTabletActivity> mAllAsamsMapTabletActivity;

        QueryHandler(AllAsamsMapTabletActivity allAsamsMapTabletActivity) {
            mAllAsamsMapTabletActivity = new WeakReference<AllAsamsMapTabletActivity>(allAsamsMapTabletActivity);
        }

        @Override
        public void handleMessage(Message message) {
            AllAsamsMapTabletActivity allAsamsMapTabletActivity = mAllAsamsMapTabletActivity.get();
            allAsamsMapTabletActivity.mDateRangeTextViewUI.setText(allAsamsMapTabletActivity.mDateRangeText);
            allAsamsMapTabletActivity.mTotalAsamsTextViewUI.setText(allAsamsMapTabletActivity.mTotalAsamsText);
            allAsamsMapTabletActivity.mQueryProgressDialog.dismiss();
            if (allAsamsMapTabletActivity.mQueryError) {
                allAsamsMapTabletActivity.mQueryError = false;
                Toast.makeText(allAsamsMapTabletActivity, allAsamsMapTabletActivity.getString(R.string.all_asams_map_tablet_query_error_text), Toast.LENGTH_LONG).show();
            }
            
            allAsamsMapTabletActivity.mMapUI.clear();
            if (allAsamsMapTabletActivity.mAsams.size() == 1) {
                
                // Camera position changing so redraw will be triggered in onCameraChange.
                if (allAsamsMapTabletActivity.mPerformBoundsAdjustmentWithQuery) {
                    allAsamsMapTabletActivity.mPerformMapClustering = true;
                    AsamBean asam = allAsamsMapTabletActivity.mAsams.get(0);
                    allAsamsMapTabletActivity.mMapUI.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(asam.getLatitude(), asam.getLongitude())).zoom(AsamConstants.SINGLE_ASAM_ZOOM_LEVEL).build()));
                }
            }
            else if (allAsamsMapTabletActivity.mAsams.size() > 1) {
                
                // Camera position changing so redraw will be triggered in onCameraChange.
                if (allAsamsMapTabletActivity.mPerformBoundsAdjustmentWithQuery) {
                    allAsamsMapTabletActivity.mPerformMapClustering = true;
                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                    for (AsamBean asam : allAsamsMapTabletActivity.mAsams) {
                        boundsBuilder = boundsBuilder.include(new LatLng(asam.getLatitude(), asam.getLongitude()));
                    }
                    allAsamsMapTabletActivity.mMapUI.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 0), allAsamsMapTabletActivity);
                }
            }
            else {
                Toast.makeText(allAsamsMapTabletActivity, allAsamsMapTabletActivity.getString(R.string.all_asams_map_no_asams_text), Toast.LENGTH_LONG).show();
            }
            
            // Camera position not changing so redraw won't be triggered in onCameraChange.
            if (!allAsamsMapTabletActivity.mPerformBoundsAdjustmentWithQuery && allAsamsMapTabletActivity.mAsams.size() > 0) {
                
                // Use the PoffenCluster library to calculate the clusters.
                int zoomLevel = Math.round(allAsamsMapTabletActivity.mMapUI.getCameraPosition().zoom);
                LatLngBounds bounds = allAsamsMapTabletActivity.mMapUI.getProjection().getVisibleRegion().latLngBounds;
                int numLatitudeCells = (int)(Math.round(Math.pow(2, zoomLevel)));
                int numLongitudeCells = (int)(Math.round(Math.pow(2, zoomLevel)));
                PoffenClusterCalculator<AsamBean> calculator = new PoffenClusterCalculator.Builder<AsamBean>(numLatitudeCells, numLongitudeCells).mergeLargeClusters(false).build();
                for (AsamBean asam : allAsamsMapTabletActivity.mAsams) {
                    calculator.add(asam, new PoffenPoint(asam.getLatitude(), asam.getLongitude()));
                }
                
                allAsamsMapTabletActivity.mMapClusters = Collections.synchronizedList(new ArrayList<AsamMapClusterBean>());
                synchronized (allAsamsMapTabletActivity.Mutex) {
                    allAsamsMapTabletActivity.mVisibleClusters = new ArrayList<AsamMapClusterBean>();
                }
                List<PoffenCluster<AsamBean>> poffenClusters = calculator.getPoffenClusters();
                synchronized (allAsamsMapTabletActivity.Mutex) {
                    for (PoffenCluster<AsamBean> poffenCluster : poffenClusters) {
                        PoffenPoint poffenPoint = poffenCluster.getClusterCoordinateClosestToMean();
                        AsamMapClusterBean cluster = new AsamMapClusterBean(poffenCluster.getClusterItems(), new LatLng(poffenPoint.getLatitude(), poffenPoint.getLongitude()));
                        allAsamsMapTabletActivity.mMapClusters.add(cluster);
                        
                        if (bounds.contains(cluster.getClusteredMapPosition()) || zoomLevel <= AsamConstants.ZOOM_LEVEL_TO_DRAW_ALL_CLUSTERS) {
                            allAsamsMapTabletActivity.mVisibleClusters.add(cluster);
                    
                            // Now draw it on the map.
                            Marker marker;
                            if (poffenCluster.getClusterItems().size() == 1) {
                                marker = allAsamsMapTabletActivity.mMapUI.addMarker(new MarkerOptions().position(cluster.getClusteredMapPosition()).icon(AsamConstants.PIRATE_MARKER).anchor(0.5f, 0.5f));
                            }
                            else {
                                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(AsamUtils.drawNumberOnClusterMarker(allAsamsMapTabletActivity, poffenCluster.getClusterItems().size()));
                                marker = allAsamsMapTabletActivity.mMapUI.addMarker(new MarkerOptions().position(cluster.getClusteredMapPosition()).icon(bitmapDescriptor).anchor(0.5f, 0.5f));
                            }
                            cluster.setMapMarker(marker);
                        }
                    }
                }
            }
        }
    }
    
    private static final int ALL_QUERY_MODE = 0;
    private static final int SUBREGION_QUERY_MODE = 1;
    private static final int TEXT_QUERY_MODE = 2;
    private static final int SUBREGION_MAP_TABLET_ACTIVITY_REQUEST_CODE = 0;
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
    private TextView mDateRangeTextViewUI;
    private TextView mTotalAsamsTextViewUI;
    private TextView mQueryModeMessageTextViewUI;
    private LinearLayout mQueryModeMessageContainerUI;
    private SeekBar mTimeSliderUI;
    private MenuItem mResetMenuItemUI;
    private MenuItem mListViewMenuItemUI;
    private MenuItem mSubregionsMenuItemUI;
    private MenuItem mSettingsMenuItemUI;
    private MenuItem mTextQueryMenuItemUI;
    private MenuItem mInfoMenuItemUI;
    private Date mEarliestAsamDate;
    private ProgressDialog mQueryProgressDialog;
    private QueryHandler mQueryHandler;
    private List<Integer> mSelectedSubregionIds;
    private TextQueryParametersBean mTextQueryParametersBean;
    private Date mTextQueryDateEarliest;
    private Date mTextQueryDateLatest;
    private int mQueryMode;
    private int mPreviousZoomLevel;
    private List<AsamMapClusterBean> mVisibleClusters;
    private boolean mDisclaimerPopupShowing;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(AllAsamsMapTabletActivity.class.getName() + ":onCreate");
        setContentView(R.layout.all_asams_map_tablet);
        
        mQueryError = false;
        mPerformMapClustering = false;
        mAsams = new ArrayList<AsamBean>();
        mMapClusters = Collections.synchronizedList(new ArrayList<AsamMapClusterBean>());
        mVisibleClusters = new ArrayList<AsamMapClusterBean>();
        
        mMapUI = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.all_asams_map_tablet_map_view_ui)).getMap();
        mMapUI.setOnCameraChangeListener(this);
        mMapUI.setOnMarkerClickListener(this);
        mPreviousZoomLevel = -1;
        mDateRangeTextViewUI = (TextView)findViewById(R.id.all_asams_map_tablet_date_range_text_view_ui);
        mTimeSliderUI = (SeekBar)findViewById(R.id.all_asams_map_tablet_time_slider_ui);
        mTotalAsamsTextViewUI = (TextView)findViewById(R.id.all_asams_map_tablet_total_asams_text_view_ui);
        mQueryModeMessageTextViewUI = (TextView)findViewById(R.id.all_asams_map_tablet_query_mode_message_text_view_ui);
        mQueryModeMessageContainerUI = (LinearLayout)findViewById(R.id.all_asams_map_tablet_query_mode_message_container_ui);
        
        mTimeSliderUI.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mQueryMode == TEXT_QUERY_MODE) {
                    if (progress == TOTAL_TIME_SLIDER_TICKS - 1) {
                        mDateRangeTextViewUI.setText(String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(mTextQueryDateLatest), DATE_RANGE_FORMAT.format(mTextQueryDateEarliest)));
                    }
                    else if (progress == 0) {
                        mDateRangeTextViewUI.setText(String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(mTextQueryDateLatest), DATE_RANGE_FORMAT.format(mTextQueryDateLatest)));
                    }
                    else {
                        mDateRangeTextViewUI.setText(String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(mTextQueryDateLatest), DATE_RANGE_FORMAT.format(calculateTextQueryDateFromTimeSlider(progress))));
                    }
                }
                else {
                    if (progress == TOTAL_TIME_SLIDER_TICKS - 1) {
                        mDateRangeTextViewUI.setText(String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(new Date()), DATE_RANGE_FORMAT.format(initAndGetEarliestAsamDate())));
                    }
                    else if (progress == 0) {
                        mDateRangeTextViewUI.setText(String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(new Date()), DATE_RANGE_FORMAT.format(new Date())));
                    }
                    else {
                        mDateRangeTextViewUI.setText(String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(new Date()), DATE_RANGE_FORMAT.format(calculateQueryDateFromTimeSlider(progress))));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mQueryProgressDialog = ProgressDialog.show(AllAsamsMapTabletActivity.this, getString(R.string.all_asams_map_tablet_query_progress_dialog_title_text), getString(R.string.all_asams_map_tablet_query_progress_dialog_content_text), true);
                new QueryThread(seekBar.getProgress()).start();
            }
            
        });
        
        mQueryMode = ALL_QUERY_MODE;
        
        // Called to handle the UI after the query has run.
        mQueryHandler = new QueryHandler(this);
        
        // Show the disclaimer popup if necessary.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hideDisclaimer = preferences.getBoolean(AsamConstants.HIDE_DISCLAIMER_KEY, false);
        boolean isTabletLaunching = getIntent().getBooleanExtra(AsamConstants.TABLET_IS_LAUNCHING_KEY, false);
        if (isTabletLaunching && !hideDisclaimer) {
            mDisclaimerPopupShowing = true;
            mTimeSliderUI.setEnabled(false);
            DialogFragment dialogFragment = DisclaimerDialogFragment.newInstance();
            dialogFragment.show(getSupportFragmentManager(), AsamConstants.DISCLAIMER_DIALOG_TAG);
        }
        else {
            new QueryThread(getInitialTimePeriodQuery()).start();
            mQueryProgressDialog = ProgressDialog.show(this, getString(R.string.all_asams_map_tablet_query_progress_dialog_title_text), getString(R.string.all_asams_map_tablet_query_progress_dialog_content_text), true);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.all_asams_map_tablet_menu, menu);
        mResetMenuItemUI = menu.findItem(R.id.all_asams_map_tablet_menu_reset_ui);
        mListViewMenuItemUI = menu.findItem(R.id.all_asams_map_tablet_menu_list_view_ui);
        mSubregionsMenuItemUI = menu.findItem(R.id.all_asams_map_tablet_menu_subregions_ui);
        mSettingsMenuItemUI = menu.findItem(R.id.all_asams_map_tablet_menu_settings_ui);
        mTextQueryMenuItemUI = menu.findItem(R.id.all_asams_map_tablet_menu_text_query_ui);
        mInfoMenuItemUI = menu.findItem(R.id.all_asams_map_tablet_menu_info_ui);
        setMenuItemsState(!mDisclaimerPopupShowing);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        int itemId = item.getItemId();
        if (itemId == R.id.all_asams_map_tablet_menu_list_view_ui) {
            AsamListContainer.mAsams = mAsams;
            intent = new Intent(this, AsamListReportTabletActivity.class);
            startActivity(intent);
            return true;
        }
        else if (itemId == R.id.all_asams_map_tablet_menu_subregions_ui) {
            
            // Jump to the subregions map.
            intent = new Intent(this, SubregionMapActivity.class);
            intent.putExtra(AsamConstants.SUBREGION_MAP_EXPECTING_RESULT_CODE_KEY, true);
            startActivityForResult(intent, SUBREGION_MAP_TABLET_ACTIVITY_REQUEST_CODE);
            return true;
        }
        else if (itemId == R.id.all_asams_map_tablet_menu_text_query_ui) {
            DialogFragment dialogFragment = TextQueryDialogFragment.newInstance();
            dialogFragment.show(getSupportFragmentManager(), AsamConstants.TEXT_QUERY_DIALOG_TAG);
            return true;
        }
        else if (itemId == R.id.all_asams_map_tablet_menu_settings_ui) {
            DialogFragment dialogFragment = PreferencesDialogFragment.newInstance();
            dialogFragment.show(getSupportFragmentManager(), AsamConstants.PREFERENCES_DIALOG_TAG);
            return true;
        }
        else if (itemId == R.id.all_asams_map_tablet_menu_info_ui) {
            DialogFragment dialogFragment = InfoDialogFragment.newInstance();
            dialogFragment.show(getSupportFragmentManager(), AsamConstants.INFO_DIALOG_TAG);
        }
        else if (itemId == R.id.all_asams_map_tablet_menu_reset_ui) {
            mQueryModeMessageContainerUI.setVisibility(View.INVISIBLE);
            mResetMenuItemUI.setVisible(false);
            mQueryMode = ALL_QUERY_MODE;
            mTextQueryDateEarliest = null;
            mTextQueryDateLatest = null;
            mQueryProgressDialog = ProgressDialog.show(this, getString(R.string.all_asams_map_tablet_query_progress_dialog_title_text), getString(R.string.all_asams_map_tablet_query_progress_dialog_content_text), true);
            new QueryThread(getInitialTimePeriodQuery()).start();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onMarkerClick(Marker marker) {
        synchronized (Mutex) {
            for (AsamMapClusterBean mapCluster : mVisibleClusters) {
                if (marker.equals(mapCluster.getMapMarker())) {
                    AsamListContainer.mAsams = mapCluster.getAsams();
                    Intent intent = new Intent(this, AsamListReportTabletActivity.class);
                    startActivity(intent);
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        AsamLog.i(AllAsamsMapTabletActivity.class.getName() + ":onCameraChange");
        if (mDisclaimerPopupShowing) {
            return;
        }
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
            case SUBREGION_MAP_TABLET_ACTIVITY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    mQueryModeMessageContainerUI.setVisibility(View.VISIBLE);
                    mQueryMode = SUBREGION_QUERY_MODE;
                    mSelectedSubregionIds = data.getIntegerArrayListExtra(AsamConstants.SUBREGION_QUERY_SUBREGIONS_LIST_KEY);
                    mQueryModeMessageTextViewUI.setText(Html.fromHtml(String.format(getResources().getString(R.string.all_asams_map_tablet_subregions_query_mode_message_text), AsamUtils.getCommaSeparatedStringFromIntegerList(mSelectedSubregionIds))));
                    
                    Calendar timePeriod = new GregorianCalendar();
                    int timeSpan = data.getIntExtra(AsamConstants.SUBREGION_QUERY_TIME_SPAN_KEY, AsamConstants.TIME_SPAN_60_DAYS);
                    if (timeSpan == AsamConstants.TIME_SPAN_1_YEAR) {
                        timePeriod.add(Calendar.YEAR, -1);
                    }
                    else if (timeSpan == AsamConstants.TIME_SPAN_5_YEARS) {
                        timePeriod.add(Calendar.YEAR, -5);
                    }
                    else if (timeSpan == AsamConstants.TIME_SPAN_ALL) {
                        timePeriod.setTime(initAndGetEarliestAsamDate());
                    }
                    else {
                        timePeriod.add(Calendar.DAY_OF_YEAR, -timeSpan);
                    }
                    mTimeSliderUI.setProgress(calculateTimeSliderTicksFromDate(timePeriod.getTime()));
                    mResetMenuItemUI.setVisible(true);
                    mQueryProgressDialog = ProgressDialog.show(this, getString(R.string.all_asams_map_tablet_query_progress_dialog_title_text), getString(R.string.all_asams_map_tablet_query_progress_dialog_content_text), true);
                    new QueryThread(timePeriod).start();
                }
                break;
        }
    }
    
    @Override
    public void onTextQuery(TextQueryParametersBean textQueryParameters) {
        DialogFragment dialogFragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(AsamConstants.TEXT_QUERY_DIALOG_TAG);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        mTextQueryParametersBean = textQueryParameters;
        mQueryMode = TEXT_QUERY_MODE;
        
        // Populate the from and to dates for the text query.
        if (!AsamUtils.isEmpty(textQueryParameters.mDateFrom)) {
            try {
                mTextQueryDateEarliest = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.parse(textQueryParameters.mDateFrom);
            }
            catch (ParseException caught) {
                mTextQueryDateEarliest = initAndGetEarliestAsamDate();
            }
        }
        else {
            mTextQueryDateEarliest = initAndGetEarliestAsamDate();
        }
        if (!AsamUtils.isEmpty(textQueryParameters.mDateTo)) {
            try {
                mTextQueryDateLatest = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.parse(textQueryParameters.mDateTo);
            }
            catch (ParseException caught) {
                mTextQueryDateLatest = new Date();
            }
        }
        else {
            mTextQueryDateLatest = new Date();
        }
        mQueryModeMessageContainerUI.setVisibility(View.VISIBLE);
        mQueryModeMessageTextViewUI.setText(Html.fromHtml(String.format(getResources().getString(R.string.all_asams_map_tablet_text_query_mode_message_text), textQueryParameters.getParametersAsFormattedHtml())));
        mResetMenuItemUI.setVisible(true);
        mTimeSliderUI.setProgress(TOTAL_TIME_SLIDER_TICKS - 1); // All of the time will be shown.
        mQueryProgressDialog = ProgressDialog.show(this, getString(R.string.all_asams_map_tablet_query_progress_dialog_title_text), getString(R.string.all_asams_map_tablet_query_progress_dialog_content_text), true);
        new QueryThread().start();
    }
    
    public void onDisclaimerDialogDismissed(boolean exitApplication) {
//        DialogFragment dialogFragment = (DialogFragment)getSupportFragmentManager().findFragmentByTag(AsamConstants.DISCLAIMER_DIALOG_TAG);
//        if (dialogFragment != null) {
//            dialogFragment.dismiss();
//        }
        if (exitApplication) {
            finish();
        }
        else {
            mDisclaimerPopupShowing = false;
            setMenuItemsState(true);
            mTimeSliderUI.setEnabled(true);
            new QueryThread(getInitialTimePeriodQuery()).start();
            mQueryProgressDialog = ProgressDialog.show(this, getString(R.string.all_asams_map_tablet_query_progress_dialog_title_text), getString(R.string.all_asams_map_tablet_query_progress_dialog_content_text), true);
        }
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
    
    public void disclaimerButtonClicked(View view) {
        // no-op. Taken care of by onDisclaimerDialogDismissed and onPreferencesDialogDismissed.
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
    
    private void setMenuItemsState(boolean enabled) {
        mListViewMenuItemUI.setEnabled(enabled);
        mSubregionsMenuItemUI.setEnabled(enabled);
        mSettingsMenuItemUI.setEnabled(enabled);
        mTextQueryMenuItemUI.setEnabled(enabled);
        mInfoMenuItemUI.setEnabled(enabled);
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
                        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(AsamUtils.drawNumberOnClusterMarker(AllAsamsMapTabletActivity.this, mapCluster.getAsams().size()));
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
    
    private Calendar getInitialTimePeriodQuery() {
        Calendar timePeriod = new GregorianCalendar();
        timePeriod.add(Calendar.YEAR, -1);
        mTimeSliderUI.setProgress(calculateTimeSliderTicksFromDate(timePeriod.getTime()));
        return timePeriod;
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
            mMapUI.clear();
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
            Context context = AllAsamsMapTabletActivity.this;
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
                    AsamLog.e(AllAsamsMapTabletActivity.class.getName() + ":There was an error parsing ASAM feed", caught);
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
                            Toast.makeText(AllAsamsMapTabletActivity.this, getString(R.string.preferences_sync_complete_description_text), Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(AllAsamsMapTabletActivity.this, getString(R.string.preferences_error_sync_description_text), Toast.LENGTH_LONG).show();
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
                    Calendar timePeriod = null;
                    if (mQueryType == TIME_SLIDER_QUERY) {
                        timePeriod = new GregorianCalendar();
                        if (mTimeSliderTick == TOTAL_TIME_SLIDER_TICKS - 1) {
                            timePeriod.setTime(initAndGetEarliestAsamDate());
                        }
                        else {
                            timePeriod.setTime(calculateQueryDateFromTimeSlider(mTimeSliderTick));
                        }
                    }
                    else if (mQueryType == TIME_PERIOD_QUERY) {
                        timePeriod = mTimePeriod;
                    }
                    if (mQueryMode == ALL_QUERY_MODE) {
                        mAsams.addAll(dbHelper.queryByTime(db, timePeriod));
                    }
                    else if (mQueryMode == SUBREGION_QUERY_MODE) {
                        mAsams.addAll(dbHelper.queryByTimeAndSubregions(db, timePeriod, mSelectedSubregionIds));
                    }
                    else if (mQueryMode == TEXT_QUERY_MODE) {
                        TextQueryParametersBean parameters = TextQueryParametersBean.newInstance(mTextQueryParametersBean);
                        parameters.mDateTo = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(mTextQueryDateLatest);
                        if (mQueryType == STANDARD_QUERY) {
                            parameters.mDateFrom = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(mTextQueryDateEarliest);
                        }
                        else if (mQueryType == TIME_SLIDER_QUERY) {
                            if (mTimeSliderTick == 0) {
                                parameters.mDateFrom = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(mTextQueryDateLatest);
                            }
                            else if (mTimeSliderTick == TOTAL_TIME_SLIDER_TICKS - 1) {
                                parameters.mDateFrom = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(mTextQueryDateEarliest);
                            }
                            else {
                                parameters.mDateFrom = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(calculateTextQueryDateFromTimeSlider(mTimeSliderTick));
                            }
                        }
                        mAsams.addAll(dbHelper.queryByText(db, parameters));
                    }
                    if (mQueryMode == TEXT_QUERY_MODE) {
                        if (mQueryType == STANDARD_QUERY) {
                            mDateRangeText = String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(mTextQueryDateLatest), DATE_RANGE_FORMAT.format(mTextQueryDateEarliest));
                        }
                        else if (mQueryType == TIME_SLIDER_QUERY) {
                            if (mTimeSliderTick == 0) {
                                mDateRangeText = String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(mTextQueryDateLatest), DATE_RANGE_FORMAT.format(mTextQueryDateLatest));
                            }
                            else if (mTimeSliderTick == TOTAL_TIME_SLIDER_TICKS - 1) {
                                mDateRangeText = String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(mTextQueryDateLatest), DATE_RANGE_FORMAT.format(mTextQueryDateEarliest));
                            }
                            else {
                                mDateRangeText = String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(mTextQueryDateLatest), DATE_RANGE_FORMAT.format(calculateTextQueryDateFromTimeSlider(mTimeSliderTick)));
                            }
                        }
                    }
                    else {
                        mDateRangeText = String.format(DATE_RANGE_PATTERN, DATE_RANGE_FORMAT.format(new Date()), DATE_RANGE_FORMAT.format(timePeriod.getTime()));
                    }
                    mTotalAsamsText = String.format(TOTAL_ASAMS_PATTERN, mAsams.size(), totalNumberOfAsams);
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
}
