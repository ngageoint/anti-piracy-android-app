package mil.nga.giat.asam.map;

import java.util.Collection;

import mil.nga.giat.asam.Asam;
import mil.nga.giat.asam.R;
import mil.nga.giat.asam.connectivity.OfflineBannerFragment;
import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamConstants;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vividsolutions.jts.geom.Geometry;


public class SingleAsamMapActivity extends ActionBarActivity implements Asam.OnOfflineFeaturesListener, OfflineBannerFragment.OnOfflineBannerClick {

    private GoogleMap mMapUI;
    private int mMapType;
    private SharedPreferences mSharedPreferences;
    private OfflineMap offlineMap;
    private MenuItem offlineMapMenuItem;
    private OfflineBannerFragment offlineAlertFragment;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_asam_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        AsamBean asam = null;
        CameraPosition initialPosition = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asam = (AsamBean)extras.getSerializable(AsamConstants.ASAM_KEY);
            initialPosition = (CameraPosition)extras.getParcelable(AsamConstants.INITIAL_MAP_POSITION_KEY);
        }
        mMapUI = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.single_asam_map_map_view_ui)).getMap();
        
        offlineAlertFragment = new OfflineBannerFragment();
        getSupportFragmentManager().beginTransaction()
            .add(android.R.id.content, offlineAlertFragment)
            .commit();

        LatLng markerPosition = new LatLng(asam.getLatitude(), asam.getLongitude());
        String title = asam.getVictim();
        String snippet = String.format(getResources().getString(R.string.single_asam_map_snippet_text), AsamBean.OCCURRENCE_DATE_FORMAT.format(asam.getOccurrenceDate()));
        mMapUI.addMarker(new MarkerOptions().position(markerPosition).title(title).snippet(snippet).icon(AsamConstants.PIRATE_MARKER).anchor(0.5f, 0.5f));
        
        float zoomLevel = AsamConstants.SINGLE_ASAM_ZOOM_LEVEL;
        if (initialPosition != null) {
            zoomLevel = initialPosition.zoom;
            mMapUI.moveCamera(CameraUpdateFactory.newCameraPosition(initialPosition));
            mMapUI.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(markerPosition).zoom(zoomLevel).build()));
        }
        else {
            mMapUI.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(markerPosition).zoom(zoomLevel).build()));
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        ((Asam) getApplication()).registerOfflineMapListener(this);
        
        supportInvalidateOptionsMenu();
        
        int mapType = mSharedPreferences.getInt(AsamConstants.MAP_TYPE_KEY, GoogleMap.MAP_TYPE_NORMAL);
        if (mapType != mMapType) onMapTypeChanged(mapType);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        offlineMap.clear();
        offlineMap = null;
        ((Asam) getApplication()).unregisterOfflineMapListener(this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.single_asam_map_menu, menu);        
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
            case android.R.id.home:
                finish();
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
