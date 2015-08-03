package mil.nga.giat.asam.map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.nga.giat.asam.Asam;
import mil.nga.giat.asam.R;
import mil.nga.giat.asam.filter.FilterAdvancedActivity;
import mil.nga.giat.asam.model.SubregionBean;
import mil.nga.giat.asam.model.SubregionTextParser;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamLog;


public class SubregionMapActivity extends AppCompatActivity implements OnMapClickListener, Asam.OnOfflineFeaturesListener, View.OnClickListener {

    private static final int INITIAL_ZOOM_LEVEL = 2;
    private static final float OUTLINE_WIDTH = 2.0f;

    private static final int OUTLINE_COLOR = 0xFF00BFA5;
    private static final int SELECTED_FILL_COLOR = 0x7F00BFA5;

    private GoogleMap mMapUI;
    private int mMapType;
    private List<SubregionBean> mSubregions;
    private MenuItem mResetMenuItemUI;
    private SharedPreferences mSharedPreferences;
    private OfflineMap offlineMap;
    private Collection<Geometry> offlineGeometries = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        AsamLog.i(SubregionMapActivity.class.getName() + ":onCreate");
        setContentView(R.layout.subregion_map);
        
        mMapUI = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.subregion_map_map_view_ui)).getMap();
        mMapUI.setOnMapClickListener(this);

        findViewById(R.id.apply).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);

        Collection<Integer> selectedSubregionIds = getIntent().getIntegerArrayListExtra(FilterAdvancedActivity.SUBREGIONS_EXTRA);
        
        // Initialize subregions and place on map.
        mSubregions = new SubregionTextParser().parseSubregions(this);
        for (SubregionBean subregion : mSubregions) {
            PolygonOptions polygonOptions = new PolygonOptions().zIndex(100);
            polygonOptions.addAll(subregion.getMapCoordinates()).strokeColor(OUTLINE_COLOR).strokeWidth(OUTLINE_WIDTH);

            if (selectedSubregionIds.contains(subregion.getSubregionId())) {
                polygonOptions.fillColor(SELECTED_FILL_COLOR);
                subregion.setSelected(true);
            }

            Polygon polygon = mMapUI.addPolygon(polygonOptions);
            subregion.setMapPolygon(polygon);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        AsamLog.i(SubregionMapActivity.class.getName() + ":onResume");
        LatLng position = new LatLng(0.0, 0.0);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(INITIAL_ZOOM_LEVEL).build();
        mMapUI.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        ((Asam) getApplication()).registerOfflineMapListener(this);

        supportInvalidateOptionsMenu();
        
        int mapType = mSharedPreferences.getInt(AsamConstants.MAP_TYPE_KEY, GoogleMap.MAP_TYPE_NORMAL);
        if (mapType != mMapType) setMapType(mapType);
    }

    @Override
    public void onPause() {
        super.onPause();

        ((Asam) getApplication()).unregisterOfflineMapListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.subregion_map_menu, menu);
        mResetMenuItemUI = menu.findItem(R.id.subregion_map_menu_reset_ui);

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
            case R.id.subregion_map_menu_reset_ui:
                clearMenuClicked();
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
                intent.putIntegerArrayListExtra(FilterAdvancedActivity.SUBREGIONS_EXTRA, getSelectedSubregionIds());
                setResult(RESULT_OK, intent);
                finish();
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
                    subregion.getMapPolygon().setFillColor(Color.TRANSPARENT);
                }

                int tappedSubregionId = subregion.getSubregionId();
                
                // Some subregions are made up of more than one geometry.
                if (SubregionBean.MULTI_SUBREGION_IDS.contains(tappedSubregionId)) {
                    boolean selectedStatus = subregion.isSelected();
                    for (SubregionBean multiSubregion : mSubregions) {
                        if (multiSubregion.getSubregionId() == tappedSubregionId) {
                            multiSubregion.setSelected(selectedStatus);
                            if (multiSubregion.isSelected()) {
                                multiSubregion.getMapPolygon().setFillColor(SELECTED_FILL_COLOR);
                            } else {
                                subregion.getMapPolygon().setFillColor(Color.TRANSPARENT);
                            }
                        }
                    }
                }
            }
        }

        setMenuState();
    }


    @Override
    public void onOfflineFeaturesLoaded(Collection<Geometry> offlineGeometries) {
        this.offlineGeometries = offlineGeometries;

        if (offlineMap == null && mMapType == AsamConstants.MAP_TYPE_OFFLINE_110M) {
            if (offlineMap != null) offlineMap.clear();
            offlineMap = new OfflineMap(this, mMapUI, offlineGeometries);
        }
    }
    
    private void clearMenuClicked() {
        clearMap();
    }
    
    private void clearMap() {
        for (SubregionBean subregion : mSubregions) {
            subregion.setSelected(false);
            subregion.getMapPolygon().setFillColor(Color.TRANSPARENT);
        }

        setMenuState();
    }
    
    private void setMenuState() {
        if (getSelectedSubregionIds().size() > 0) {
            mResetMenuItemUI.setEnabled(true);
        } else {
            mResetMenuItemUI.setEnabled(false);
        }
    }
    
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
    
    private void setMapType(int mapType) {
        mMapType = mapType;

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
    }
}