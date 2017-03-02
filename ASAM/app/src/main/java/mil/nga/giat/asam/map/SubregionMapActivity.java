package mil.nga.giat.asam.map;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.nga.giat.asam.R;
import mil.nga.giat.asam.connectivity.NetworkChangeReceiver;
import mil.nga.giat.asam.connectivity.OfflineBannerFragment;
import mil.nga.giat.asam.filter.FilterActivity;
import mil.nga.giat.asam.model.SubregionBean;
import mil.nga.giat.asam.model.SubregionTextParser;
import mil.nga.giat.asam.permission.OnPermissionCallback;
import mil.nga.giat.asam.permission.PermissionHelper;
import mil.nga.giat.asam.util.CurrentSubregionHelper;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamLog;

import static mil.nga.giat.asam.util.AsamConstants.MAP_TYPE_OFFLINE;


public class SubregionMapActivity extends AppCompatActivity implements OnMapClickListener, View.OnClickListener, OfflineBannerFragment.OnOfflineBannerClick, NetworkChangeReceiver.ConnectivityEventListener, OnPermissionCallback, OnMapReadyCallback {

    private static final int INITIAL_ZOOM_LEVEL = 2;
    private static final float OUTLINE_WIDTH = 2.0f;

    private static final int OUTLINE_COLOR = 0xFF00BFA5;
    private static final int SELECTED_FILL_COLOR = 0x7F00BFA5;
    private static final int DEFAULT_COLOR = 0x000000;

    private GoogleMap mMapUI;
    private int mMapType;
    private List<SubregionBean> mSubregions;
    private MenuItem mResetMenuItemUI;
    private MenuItem mClearMenuItemUI;
    private MenuItem mSelectedSubregionMenuItemUI;
    private SharedPreferences mSharedPreferences;
    private OfflineMap offlineMap;
    private OfflineBannerFragment offlineAlertFragment;
    private Collection<Integer> initiallySelectedSubregionIds = null;
    private LatLng lastSearchedPosition;
    private ImageButton myLocationButton;

    final String LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    final String DIALOG_TITLE = "Access Fine Location";
    final String DIALOG_MESSAGE = "ASAM needs to access your Fine Location to get the Current Subregion";

    private CurrentSubregionHelper currentSubregionHelper;
    private Context mContext;

    // permissions
    PermissionHelper permissionHelper;
    private boolean hasFineLocationAccess = false; // fine location access permission

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        lastSearchedPosition = new LatLng(0.0, 0.0);

        AsamLog.i(SubregionMapActivity.class.getName() + ":onCreate");
        setContentView(R.layout.subregion_map);

        myLocationButton = (ImageButton) findViewById(R.id.location);
        myLocationButton.setOnClickListener(this);

        findViewById(R.id.apply).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);

        ImageButton btn = (ImageButton) findViewById(R.id.map_overlay_menu);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMapTypeMenu(v);
            }
        });

        initiallySelectedSubregionIds = getIntent().getIntegerArrayListExtra(FilterActivity.SUBREGIONS_EXTRA);

        offlineAlertFragment = new OfflineBannerFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.subregion_connectivity_fragment_container, offlineAlertFragment)
                .commit();

        permissionHelper = PermissionHelper.getInstance(this);

        permissionHelper.setForceAccepting(false).request(LOCATION_PERMISSION);

        checkFineLocationAccess();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.subregion_map_map_view_ui)).getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMapUI = googleMap;
        mMapUI.setOnMapClickListener(this);

        // Initialize subregions and place on map.
        mSubregions = new SubregionTextParser().parseSubregions(this);
        for (SubregionBean subregion : mSubregions) {
            //PolygonOptions polygonOptions = new PolygonOptions().zIndex(100);
            PolygonOptions polygonOptions = new PolygonOptions().zIndex(100);
            polygonOptions.addAll(subregion.getMapCoordinates()).strokeColor(OUTLINE_COLOR).strokeWidth(OUTLINE_WIDTH);

            if (initiallySelectedSubregionIds.contains(subregion.getSubregionId())) {
                polygonOptions.fillColor(SELECTED_FILL_COLOR);
                subregion.setSelected(true);
            }

            Polygon polygon = mMapUI.addPolygon(polygonOptions);
            subregion.setMapPolygon(polygon);
        }

        CameraPosition cameraPosition = new CameraPosition.Builder().target(lastSearchedPosition).zoom(INITIAL_ZOOM_LEVEL).build();
        mMapUI.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        int mapType = mSharedPreferences.getInt(AsamConstants.MAP_TYPE_KEY, GoogleMap.MAP_TYPE_NORMAL);
        if (mapType != mMapType) onMapTypeChanged(mapType);
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    /**
     * Handle search intents
     * @param intent
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            highlightRegionAndCenterMap(intent.getStringExtra(SearchManager.QUERY));
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            highlightRegionAndCenterMap(intent.getDataString());
        }
    }

    /**
     * Highlight region queried and center map around it
     * @param regionQuery
     */
    private void highlightRegionAndCenterMap(String regionQuery) {
        boolean found = false;
        Integer regionId = Integer.parseInt(regionQuery);
        for (SubregionBean subregion : mSubregions) {
            if (subregion.getSubregionId() == regionId) { // found a matching region
                if (!subregion.isSelected()) {
                    subregion.setSelected(true);
                    subregion.getMapPolygon().setFillColor(SELECTED_FILL_COLOR);

                    // Some subregions are made up of more than one geometry.
                    if (SubregionBean.MULTI_SUBREGION_IDS.contains(regionId)) {
                        for (SubregionBean multiSubregion : mSubregions) {
                            if (multiSubregion.equals(subregion))
                                continue;
                            if (multiSubregion.getSubregionId() == regionId) {
                                multiSubregion.setSelected(true);
                                multiSubregion.getMapPolygon().setFillColor(SELECTED_FILL_COLOR);
                            }
                        }
                    }
                }

                lastSearchedPosition = subregion.getCenterPoint();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(lastSearchedPosition)
                        .build();

                mMapUI.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);

                found = true;

                break;
            }
        }

        if (!found) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No Regions with ID of " + regionQuery)
                    .setTitle(R.string.invalid_subregion_query_id);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void centerMapOnRegion(String regionQuery) {
        boolean found = false;
        Integer regionId = Integer.parseInt(regionQuery);
        for (SubregionBean subregion : mSubregions) {
            if (subregion.getSubregionId() == regionId) { // found a matching region
                lastSearchedPosition = subregion.getCenterPoint();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(lastSearchedPosition)
                        .build();

                mMapUI.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);

                found = true;

                break;
            }
        }

        if (!found) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No Regions with ID of " + regionQuery)
                    .setTitle(R.string.invalid_subregion_query_id);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        AsamLog.i(SubregionMapActivity.class.getName() + ":onResume");

        NetworkChangeReceiver.getInstance().addListener(this);
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(NetworkChangeReceiver.getInstance(), filter);
        showOfflineAlertFragment(!NetworkChangeReceiver.getInstance().hasInternetConnectivity(getApplicationContext()));

        supportInvalidateOptionsMenu();

        if (mMapUI != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(lastSearchedPosition).zoom(INITIAL_ZOOM_LEVEL).build();
            mMapUI.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            int mapType = mSharedPreferences.getInt(AsamConstants.MAP_TYPE_KEY, GoogleMap.MAP_TYPE_NORMAL);
            if (mapType != mMapType) onMapTypeChanged(mapType);
        }

        checkFineLocationAccess();
    }

    private void checkFineLocationAccess() {
        hasFineLocationAccess = permissionHelper.isPermissionGranted(LOCATION_PERMISSION);
        if (hasFineLocationAccess) {
            fineLocationAccessGranted();
        } else {
            fineLocationAccessDenied();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        NetworkChangeReceiver.getInstance().removeListener(this);
        try {
            unregisterReceiver(NetworkChangeReceiver.getInstance());
        } catch (IllegalArgumentException e) {
            Log.e("SubregionMapActivity", "NetworkChangeReceiver was never registered");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.subregion_map_menu, menu);
        mResetMenuItemUI = menu.findItem(R.id.subregion_map_menu_reset_ui);
        mClearMenuItemUI = menu.findItem(R.id.subregion_map_menu_clear_ui);
        mSelectedSubregionMenuItemUI = menu.findItem(R.id.subregion_map_menu_selected_subregions_ui);


        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.subregion_map_menu_query_ui).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setInputType(InputType.TYPE_CLASS_NUMBER);

        // get edit text from searchview and apply a filter to limit input length to 2 numbers
        ((EditText)searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null)))
                .setFilters(new InputFilter[] { new InputFilter.LengthFilter(2) });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        setMenuState();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.subregion_map_menu_reset_ui:
                resetMenuClicked();
                return true;
            case R.id.subregion_map_menu_clear_ui:
                clearMenuClicked();
                return true;
            case R.id.subregion_map_menu_selected_subregions_ui:
                selectedSubregionsMenuClicked();
                return true;
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.apply:
                Intent intent = new Intent();
                intent.putIntegerArrayListExtra(FilterActivity.SUBREGIONS_EXTRA, getSelectedSubregionIds());
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.location:
                centerMapOnRegion(String.valueOf(currentSubregionHelper.getCurrentSubregion()));
                break;
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        for (SubregionBean subregion : mSubregions) {
            if (isSubregionTapped(subregion, point)) {
                subregion.setSelected(!subregion.isSelected());
                if (subregion.isSelected()) {
                    subregion.getMapPolygon().setFillColor(SELECTED_FILL_COLOR);
                } else {
                    subregion.getMapPolygon().setFillColor(DEFAULT_COLOR);
                }

                int tappedSubregionId = subregion.getSubregionId();

                // Some subregions are made up of more than one geometry.
                if (SubregionBean.MULTI_SUBREGION_IDS.contains(tappedSubregionId)) {
                    boolean selectedStatus = subregion.isSelected();
                    for (SubregionBean multiSubregion : mSubregions) {
                        if (multiSubregion.equals(subregion))
                            continue;
                        if (multiSubregion.getSubregionId() == tappedSubregionId) {
                            multiSubregion.setSelected(selectedStatus);
                            if (selectedStatus) {
                                multiSubregion.getMapPolygon().setFillColor(SELECTED_FILL_COLOR);
                            } else {
                                multiSubregion.getMapPolygon().setFillColor(DEFAULT_COLOR);
                            }
                        }
                    }
                }
            }
        }

        setMenuState();
    }

    @Override
    public void onOfflineBannerClick() {
        onMapTypeChanged(AsamConstants.MAP_TYPE_OFFLINE);
        supportInvalidateOptionsMenu();
    }

    public void onMapTypeChanged(int mapType) {
        boolean typeChanged = mMapType != mapType;

        mMapType = mapType;

        // show if there is no internet connectivity and the map type is not offline map
        showOfflineAlertFragment(!(NetworkChangeReceiver.getInstance().hasInternetConnectivity(getApplicationContext()) || mMapType == MAP_TYPE_OFFLINE));

        if (typeChanged) {
            // Change the map
            if (mMapType == AsamConstants.MAP_TYPE_OFFLINE) {
                if (offlineMap != null) offlineMap.clear();

                offlineMap = new OfflineMap(this, mMapUI);
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
    }

    private void clearMenuClicked() {
        clearMap();
    }

    private void resetMenuClicked() {
        resetMap();
    }

    private void selectedSubregionsMenuClicked() {
        launchSelectedSubregionDialog();
    }

    private void launchSelectedSubregionDialog() {

        StringBuilder sb = new StringBuilder();
        for(Integer regionId : getSelectedSubregionIds()) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(regionId);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(sb.toString())
                .setTitle(R.string.selected_subregion_dialog_title);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Clears subregion map of any selected regions
     */
    private void clearMap() {
        for (SubregionBean subregion : mSubregions) {
            subregion.setSelected(false);
            subregion.getMapPolygon().setFillColor(Color.TRANSPARENT);
        }

        setMenuState();
    }

    /**
     * Resets subregion map to initial state
     */
    private void resetMap() {
        clearMap();
        setInitialRegions();
        setMenuState();
    }

    /**
     * Set the initial subregions on the map
     */
    private void setInitialRegions() {
        for (SubregionBean subregion : mSubregions) {
            int subregionId = subregion.getSubregionId();
            if (initiallySelectedSubregionIds.contains(subregionId)) {
                subregion.getMapPolygon().setFillColor(SELECTED_FILL_COLOR);
                subregion.setSelected(true);
            }

        }
    }

    private void setMenuState() {
        ArrayList<Integer> selectedSubregions = getSelectedSubregionIds();
        if (selectedSubregions.size() > 0) {
            mClearMenuItemUI.setEnabled(true);
            mSelectedSubregionMenuItemUI.setEnabled(true);
        } else {
            mClearMenuItemUI.setEnabled(false);
            mSelectedSubregionMenuItemUI.setEnabled(false);
        }

        if (selectedSubregions.containsAll(initiallySelectedSubregionIds) && initiallySelectedSubregionIds.containsAll(selectedSubregions)) {
            mResetMenuItemUI.setEnabled(false);
        } else {
            mResetMenuItemUI.setEnabled(true);
        }
    }

    /**
     * Return a list of the selected subregions
     * @return
     */
    private ArrayList<Integer> getSelectedSubregionIds() {
        ArrayList<Integer> selectedSubregionIds = new ArrayList<Integer>();
        for (SubregionBean subregion : mSubregions) {
            if (subregion.isSelected()) {
                selectedSubregionIds.add(subregion.getSubregionId());
            }
        }
        Set<Integer> duplicatesRemoved = new HashSet<Integer>(selectedSubregionIds);
        selectedSubregionIds = new ArrayList<Integer>(duplicatesRemoved);
        Collections.sort(selectedSubregionIds);
        return selectedSubregionIds;
    }


    private boolean isSubregionTapped(SubregionBean subregion, LatLng point) {
        List<SubregionBean.GeoPoint> geoPoints = subregion.getGeoPoints();
        boolean contains = false;
        for (int i = 0, j = geoPoints.size() - 1; i < geoPoints.size() - 0; j = i++) {
            if (((geoPoints.get(i).latitude > point.latitude) != (geoPoints.get(j).latitude > point.latitude)) && (point.longitude < (geoPoints.get(j).longitude - geoPoints.get(i).longitude) * (point.latitude - geoPoints.get(i).latitude) / (geoPoints.get(j).latitude - geoPoints.get(i).latitude) + geoPoints.get(i).longitude)) {
                contains = !contains;
            }
        }

        return contains;
    }

    @Override
    public void onPermissionPreGranted(String permissionsName) {
        fineLocationAccessGranted();
    }

    @Override
    public void onPermissionGranted(String[] permissionName) {
        fineLocationAccessGranted();
    }

    @Override
    public void onPermissionDeclined(String[] permissionName) {
        fineLocationAccessDenied();
    }

    @Override
    public void onPermissionNeedExplanation(String permissionName) {
        showAlertDialog(DIALOG_TITLE, DIALOG_MESSAGE, LOCATION_PERMISSION);
    }

    @Override
    public void onPermissionReallyDeclined(String permissionName) {
        fineLocationAccessDenied();
    }

    @Override
    public void onNoPermissionNeeded() {
        fineLocationAccessGranted();
    }

    /**
     * Access was granted
     */
    private void fineLocationAccessGranted() {
        hasFineLocationAccess = true;
        if (currentSubregionHelper == null) {
            currentSubregionHelper = new CurrentSubregionHelper(mContext, new SubregionTextParser().parseSubregions(mContext));
        }
        myLocationButton.setVisibility(View.VISIBLE);
    }

    /**
     * Access was denied
     */
    private void fineLocationAccessDenied() {
        hasFineLocationAccess = false;
        currentSubregionHelper = null;
        myLocationButton.setVisibility(View.INVISIBLE);
    }

    private void showAlertDialog(String title, String message, final String permission) {

        android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        permissionHelper.requestAfterExplanation(permission);
                    }
                }).create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                fineLocationAccessDenied();
            }
        });
        dialog.show();
    }

    /**
     * Show or Hides the fragment
     * @param show
     */
    private void showOfflineAlertFragment(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .show(offlineAlertFragment)
                            .commit();
                    offlineAlertFragment.show();
                } else {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .hide(offlineAlertFragment)
                            .commit();
                    offlineAlertFragment.hide();
                }
            }
        });
    }


    @Override
    public void onAllDisconnected() {
        showOfflineAlertFragment(mMapType != MAP_TYPE_OFFLINE);
    }

    @Override
    public void onAnyConnected() {
        showOfflineAlertFragment(false);
    }
}