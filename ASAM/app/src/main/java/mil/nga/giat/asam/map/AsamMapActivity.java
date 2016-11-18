package mil.nga.giat.asam.map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import mil.nga.giat.asam.AsamListActivity;
import mil.nga.giat.asam.model.AsamBeanClusterRenderer;
import mil.nga.giat.asam.model.AsamInputAdapter;
import mil.nga.giat.asam.settings.SettingsActivity;
import mil.nga.giat.asam.R;
import mil.nga.giat.asam.connectivity.NetworkChangeReceiver;
import mil.nga.giat.asam.connectivity.OfflineBannerFragment;
import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.filter.FilterActivity;
import mil.nga.giat.asam.filter.FilterParameters;
import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamListContainer;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.SyncTime;


public class AsamMapActivity extends AppCompatActivity implements CancelableCallback, OfflineBannerFragment.OnOfflineBannerClick, ClusterManager.OnClusterClickListener<AsamBean>, ClusterManager.OnClusterItemClickListener<AsamBean>, GoogleMap.OnCameraIdleListener, OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener {

    private static class QueryHandler extends Handler {

        WeakReference<AsamMapActivity> mAllAsamsMapTabletActivity;

        QueryHandler(AsamMapActivity asamMapActivity) {
            mAllAsamsMapTabletActivity = new WeakReference<AsamMapActivity>(asamMapActivity);
        }

        @Override
        public void handleMessage(Message message) {
            AsamMapActivity asamMapActivity = mAllAsamsMapTabletActivity.get();

            asamMapActivity.setFilterStatus(asamMapActivity.mDateStart, asamMapActivity.mDateSelected);

            asamMapActivity.mQueryProgressDialog.dismiss();
            if (asamMapActivity.mQueryError) {
                asamMapActivity.mQueryError = false;
                Toast.makeText(asamMapActivity, asamMapActivity.getString(R.string.all_asams_map_tablet_query_error_text), Toast.LENGTH_LONG).show();
            }

            // clear items
            asamMapActivity.getmClusterManager().clearItems();
            asamMapActivity.getmClusterManager().addItems(asamMapActivity.mAsams);
            asamMapActivity.getmClusterManager().cluster();
        }
    }

    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 0;
    private static final int LIST_ACTIVITY_REQUEST_CODE = 1;
    public static final String SEARCH_PARAMETERS = "SEARCH_PARAMETERS";
    public static final String MAP_LOCATION = "MAP_LOCATION";

    private static final int TOTAL_TIME_SLIDER_TICKS = 1000;
    private static final SimpleDateFormat DATE_RANGE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    private TextView mFilterStatus;

    private String mDateStart;
    private String mDateSelected;
    private long totalNumberOfAsams = 0;
    private final Object Mutex = new Object();
    private volatile boolean mQueryError;
    private List<AsamBean> mAsams;
    private GoogleMap map;
    private int mMapType;


    private TextView mDateStartTextViewUI;
    private TextView mDateSelectedTextViewUI;
    private SeekBar mTimeSliderUI;

    private Date mEarliestAsamDate;
    private ProgressDialog mQueryProgressDialog;
    private QueryHandler mQueryHandler;
    private FilterParameters mFilterParameters;
    private Date mTextQueryDateEarliest;
    private Date mTextQueryDateLatest;
    private SharedPreferences mSharedPreferences;
    private OfflineMap offlineMap;
    private OfflineBannerFragment offlineAlertFragment;
    private int selectedGraticuleMenuItem = R.id.grat_none;

    private ClusterManager<AsamBean> mClusterManager;
    private AsamInputAdapter asamIA;
    private GraticulesManager gratManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(AsamMapActivity.class.getName() + ":onCreate");
        setContentView(R.layout.map);

        asamIA = new AsamInputAdapter(getApplicationContext());

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mQueryError = false;
        mAsams = new ArrayList();

        mFilterStatus = (TextView) findViewById(R.id.all_asams_map_query_feedback_text_ui);

        ImageButton btn = (ImageButton) findViewById(R.id.map_overlay_menu);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMapTypeMenu(v);
            }
        });

        ImageButton gratButton = (ImageButton) findViewById(R.id.graticules_overlay_menu);
        gratButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGraticulesMenu(v);
            }
        });

        offlineAlertFragment = new OfflineBannerFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.connectivity_fragment_container, offlineAlertFragment)
                .commit();

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.all_asams_map_tablet_map_view_ui)).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {

        this.map = map;



        gratManager = new GraticulesManager(getApplicationContext(), this.map);

        // initialize the cluster manager
        mClusterManager = new ClusterManager(this, this.map);
        mClusterManager.setRenderer(new AsamBeanClusterRenderer(this, this.map, mClusterManager));
        this.map.setOnCameraIdleListener(this);
        this.map.setOnCameraMoveStartedListener(this);
        this.map.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);

        int mapType = mSharedPreferences.getInt(AsamConstants.MAP_TYPE_KEY, GoogleMap.MAP_TYPE_NORMAL);
        if (mapType != mMapType) onMapTypeChanged(mapType);

        Calendar timePeriod = new GregorianCalendar();
        timePeriod.add(Calendar.YEAR, -1);
        View dateRangeView = findViewById(R.id.all_asams_map_tablet_date_range);
        if (dateRangeView != null) {
            setupDateRangeView(dateRangeView);
            mTimeSliderUI.setProgress(calculateTimeSliderTicksFromDate(timePeriod.getTime()));
        }

        mQueryHandler = new QueryHandler(this);

        mFilterParameters = new FilterParameters();
        mFilterParameters.mTimeInterval = 365;
        onFilter();
    }

    public void showGraticulesMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        popup.inflate(R.menu.graticule_overlay_menu);

        popup.getMenu().findItem(selectedGraticuleMenuItem).setChecked(true);

        popup.show();
    }

    public void showMapTypeMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        popup.inflate(R.menu.map_overlay_menu);

        int mapType = mSharedPreferences.getInt(AsamConstants.MAP_TYPE_KEY, 1);
        switch (mapType) {
            case GoogleMap.MAP_TYPE_SATELLITE:
                popup.getMenu().findItem(R.id.map_type_satellite).setChecked(true);
                break;
            case GoogleMap.MAP_TYPE_HYBRID:
                popup.getMenu().findItem(R.id.map_type_hybrid).setChecked(true);
                break;
            case AsamConstants.MAP_TYPE_OFFLINE:
                popup.getMenu().findItem(R.id.map_type_offline).setChecked(true);
                break;
            default:
                popup.getMenu().findItem(R.id.map_type_normal).setChecked(true);

        }

        popup.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.all_asams_map_tablet_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
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
            case R.id.map_type_offline:
                item.setChecked(!item.isChecked());
                onMapTypeChanged(AsamConstants.MAP_TYPE_OFFLINE);
                return true;
            case R.id.all_asams_map_menu_list_view_ui: {
                AsamListContainer.mAsams = mAsams;
                Intent intent = new Intent(this, AsamListActivity.class);
                intent.putExtra(AsamListActivity.ALWAYS_SHOW_LIST_KEY, true);
                startActivityForResult(intent, LIST_ACTIVITY_REQUEST_CODE);
                return true;
            }
            case R.id.about: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.all_asams_map_menu_search_ui: {

                Intent intent = new Intent(this, FilterActivity.class);
                intent.putExtra(SEARCH_PARAMETERS, mFilterParameters);
                startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
                return true;
            }
            case R.id.grat_none:
                selectedGraticuleMenuItem = item.getItemId();
                gratManager.removeGraticulesFromMap();
                item.setChecked(!item.isChecked());
                return true;
            case R.id.grat_degree_10:
                selectedGraticuleMenuItem = item.getItemId();
                gratManager.addGraticulesToMap(getApplicationContext().getString(R.string.grat_10_geopackage));
                item.setChecked(!item.isChecked());
                return true;
            case R.id.grat_degree_15:
                selectedGraticuleMenuItem = item.getItemId();
                gratManager.addGraticulesToMap(getApplicationContext().getString(R.string.grat_15_geopackage));
                item.setChecked(!item.isChecked());
                return true;
            case R.id.grat_degree_20:
                selectedGraticuleMenuItem = item.getItemId();
                gratManager.addGraticulesToMap(getApplicationContext().getString(R.string.grat_20_geopackage));
                item.setChecked(!item.isChecked());
                return true;
            case R.id.grat_degree_30:
                selectedGraticuleMenuItem = item.getItemId();
                gratManager.addGraticulesToMap(getApplicationContext().getString(R.string.grat_30_geopackage));
                item.setChecked(!item.isChecked());
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
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
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                }
                break;
            }
        }
    }

    public void onFilter() {
        AsamLog.i(AsamMapActivity.class.getName() + ":onFilter");
        DialogFragment dialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(AsamConstants.TEXT_QUERY_DIALOG_TAG);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }

        mTextQueryDateEarliest = initAndGetEarliestAsamDate();
        mTextQueryDateLatest = new Date();

        // Populate the from and to dates for the text query.
        if (mFilterParameters.getStartDateFromInterval() != null) {
            mTextQueryDateEarliest = mFilterParameters.getStartDateFromInterval();
        } else {
            if (StringUtils.isNotBlank(mFilterParameters.mDateFrom)) {
                try {
                    mTextQueryDateEarliest = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.parse(mFilterParameters.mDateFrom);
                } catch (ParseException caught) {
                    mTextQueryDateEarliest = initAndGetEarliestAsamDate();
                }
            } else {
                mTextQueryDateEarliest = initAndGetEarliestAsamDate();
            }
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

        setTimeSlider(null);

        mQueryProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        mQueryProgressDialog.setTitle(getString(R.string.all_asams_map_tablet_query_progress_dialog_title_text));
        mQueryProgressDialog.setMessage(getString(R.string.all_asams_map_tablet_query_progress_dialog_content_text));
        mQueryProgressDialog.setIndeterminate(true);
        mQueryProgressDialog.show();
        new QueryThread().start();
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
            } finally {
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
        long millisecondsFromLatestAsamDate = Math.round(((double) totalDateRangeInMilliseconds / TOTAL_TIME_SLIDER_TICKS) * timeSliderTick);
        return new Date(currentDate.getTime() - millisecondsFromLatestAsamDate);
    }

    private Date calculateTextQueryDateFromTimeSlider(int timeSliderTick) {
        long totalDateRangeInMilliseconds = mTextQueryDateLatest.getTime() - mTextQueryDateEarliest.getTime();
        long millisecondsFromTextQueryDateFrom = Math.round(((double) totalDateRangeInMilliseconds / TOTAL_TIME_SLIDER_TICKS) * timeSliderTick);
        return new Date(mTextQueryDateLatest.getTime() - millisecondsFromTextQueryDateFrom);
    }

    private int calculateTimeSliderTicksFromDate(Date date) {
        Date currentDate = new Date();
        long totalDateRangeInMilliseconds = currentDate.getTime() - initAndGetEarliestAsamDate().getTime();
        double percentage = (currentDate.getTime() - date.getTime()) / (double) totalDateRangeInMilliseconds;
        return (int) Math.round(TOTAL_TIME_SLIDER_TICKS * percentage);
    }

    private class QueryThread extends Thread {

        private static final int TIME_SLIDER_QUERY = 0;
        private static final int TIME_PERIOD_QUERY = 1;
        private static final int STANDARD_QUERY = 2;
        private int mQueryType;
        private int mTimeSliderTick;
        private Calendar mTimePeriod;

        QueryThread() {
            mQueryType = STANDARD_QUERY;
        }

        QueryThread(int timeSliderTick) {
            mQueryType = TIME_SLIDER_QUERY;
            mTimeSliderTick = timeSliderTick;
        }

        @Override
        public void run() {
            Context context = AsamMapActivity.this;
            SQLiteDatabase db = null;
            if (!SyncTime.isSynched(context)) {
                asamIA.run();
            }

            try {
                // Query for the time period based on the slider.
                synchronized (Mutex) {
                    mAsams.clear();
                    AsamDbHelper dbHelper = new AsamDbHelper(context);
                    db = dbHelper.getReadableDatabase();

                    totalNumberOfAsams = dbHelper.getTotalNumberOfAsams(db);

                    FilterParameters parameters = FilterParameters.newInstance(mFilterParameters);

                    if (mTextQueryDateLatest != null) {
                        parameters.mDateTo = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(mTextQueryDateLatest);
                    }

                    if (mQueryType == STANDARD_QUERY) {
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
                    if (mDateStartTextViewUI != null) {
                        Date earliest = mTextQueryDateEarliest != null ? mTextQueryDateEarliest : initAndGetEarliestAsamDate();
                        Date latest = mTextQueryDateLatest != null ? mTextQueryDateLatest : new Date();
                        if (mQueryType == STANDARD_QUERY) {
                            mDateStart = DATE_RANGE_FORMAT.format(latest);
                            mDateSelected = DATE_RANGE_FORMAT.format(earliest);
                        } else if (mQueryType == TIME_SLIDER_QUERY) {
                            if (mTimeSliderTick == TOTAL_TIME_SLIDER_TICKS - 1) {
                                mDateStart = DATE_RANGE_FORMAT.format(latest);
                                mDateSelected = DATE_RANGE_FORMAT.format(earliest);
                            } else if (mTimeSliderTick == 0) {
                                mDateStart = DATE_RANGE_FORMAT.format(latest);
                                mDateSelected = DATE_RANGE_FORMAT.format(latest);
                            } else {
                                Date dateFromSlider = mTextQueryDateEarliest != null && mTextQueryDateLatest != null ? calculateTextQueryDateFromTimeSlider(mTimeSliderTick) : calculateQueryDateFromTimeSlider(mTimeSliderTick);
                                mDateStart = DATE_RANGE_FORMAT.format(latest);
                                mDateSelected = DATE_RANGE_FORMAT.format(dateFromSlider);
                            }
                        }
                    }

                }
            } finally {
                if (db != null) {
                    db.close();
                }
            }
            mQueryHandler.sendEmptyMessage(0);
        }
    }

    public void onMapTypeChanged(int mapType) {
        boolean typeChanged = mMapType != mapType;

        mMapType = mapType;

        // Show/hide the offline alert fragment based on map type
        if (mMapType == AsamConstants.MAP_TYPE_OFFLINE) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(offlineAlertFragment)
                    .commit();
        } else if (!NetworkChangeReceiver.getInstance().hasInternetConnectivity(getApplicationContext())) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .show(offlineAlertFragment)
                    .commit();
        }

        if (typeChanged) {

            // Change the map
            if (mMapType == AsamConstants.MAP_TYPE_OFFLINE) {
                if (offlineMap != null) offlineMap.clear();

                offlineMap = new OfflineMap(this, map);
            } else {
                if (offlineMap != null) {
                    offlineMap.clear();
                    offlineMap = null;
                }

                map.setMapType(mMapType);
            }

            // update graticules
            if (selectedGraticuleMenuItem != R.id.grat_none)
                gratManager.refreshGraticules();
            else
                gratManager.removeGraticulesFromMap();

            // Update shared preferences
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt(AsamConstants.MAP_TYPE_KEY, mMapType);
            editor.commit();
        }
    }

    @Override
    public void onOfflineBannerClick() {
        onMapTypeChanged(AsamConstants.MAP_TYPE_OFFLINE);
        supportInvalidateOptionsMenu();
    }

    private void setupDateRangeView(View dateRangeView) {
        mDateStartTextViewUI = (TextView) dateRangeView.findViewById(R.id.all_asams_map_tablet_date_start_text_view_ui);
        mDateSelectedTextViewUI = (TextView) dateRangeView.findViewById(R.id.all_asams_map_tablet_date_selected_text_view_ui);
        mTimeSliderUI = (SeekBar) dateRangeView.findViewById(R.id.all_asams_map_tablet_time_slider_ui);
        mTimeSliderUI.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Date earliest = mTextQueryDateEarliest != null ? mTextQueryDateEarliest : initAndGetEarliestAsamDate();
                Date latest = mTextQueryDateLatest != null ? mTextQueryDateLatest : new Date();

                if (progress == TOTAL_TIME_SLIDER_TICKS - 1) {
                    mDateStart = DATE_RANGE_FORMAT.format(latest);
                    mDateSelected = DATE_RANGE_FORMAT.format(earliest);
                } else if (progress == 0) {
                    mDateStart = DATE_RANGE_FORMAT.format(latest);
                    mDateSelected = DATE_RANGE_FORMAT.format(latest);
                } else {
                    Date dateFromSlider = mTextQueryDateEarliest != null && mTextQueryDateLatest != null ? calculateTextQueryDateFromTimeSlider(progress) : calculateQueryDateFromTimeSlider(progress);
                    mDateStart = DATE_RANGE_FORMAT.format(latest);
                    mDateSelected = DATE_RANGE_FORMAT.format(dateFromSlider);
                }

                setFilterStatus(mDateStart, mDateSelected);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mQueryProgressDialog = new ProgressDialog(AsamMapActivity.this, R.style.AppCompatAlertDialogStyle);
                mQueryProgressDialog.setTitle(getString(R.string.all_asams_map_tablet_query_progress_dialog_title_text));
                mQueryProgressDialog.setMessage(getString(R.string.all_asams_map_tablet_query_progress_dialog_content_text));
                mQueryProgressDialog.setIndeterminate(true);
                mQueryProgressDialog.show();
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

    private void setFilterStatus(String startDate, String selectedDate) {
        if (mDateStartTextViewUI != null) {
            mDateStartTextViewUI.setText(startDate);
        }

        if (mDateSelectedTextViewUI != null) {
            mDateSelectedTextViewUI.setText(selectedDate);
        }

        // Now set the feedback title.
        String feedbackText = String.format(getString(R.string.all_asams_map_multiple_asams_text_with_timespan), mAsams.size(), totalNumberOfAsams);
        mFilterStatus.setText(feedbackText);
    }

    @Override
    public boolean onClusterClick(Cluster<AsamBean> cluster) {
        AsamListContainer.mAsams = new ArrayList(cluster.getItems());
        Intent intent = new Intent(this, AsamListActivity.class);
        startActivityForResult(intent, LIST_ACTIVITY_REQUEST_CODE);
        return true;
    }

    @Override
    public boolean onClusterItemClick(AsamBean asamBean) {
        AsamListContainer.mAsams = Arrays.asList(asamBean);
        Intent intent = new Intent(this, AsamListActivity.class);
        startActivityForResult(intent, LIST_ACTIVITY_REQUEST_CODE);
        return true;

    }

    @Override
    public void onCameraIdle() {
        gratManager.mapUpdate();
        mClusterManager.onCameraIdle();
    }

    @Override
    public void onCameraMoveStarted(int i) {
        gratManager.clearNumbers();
    }

    public ClusterManager<AsamBean> getmClusterManager() {
        return mClusterManager;
    }

}
