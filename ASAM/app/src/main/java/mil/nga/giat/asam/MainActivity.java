package mil.nga.giat.asam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.IOException;

import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.map.AsamMapActivity;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamLog;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    
    private GoogleMap mMapUI;
    private boolean mApplicationLaunching;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mApplicationLaunching = false;
        setUpMapIfNeeded();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMapUI == null) {
            SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(mil.nga.giat.asam.R.id.main_map_view_ui);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMapUI = googleMap;
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(mil.nga.giat.asam.R.id.main_map_view_ui);
        getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
        launchApplication();
    }
    
    private void launchApplication() {
        
        if (mApplicationLaunching) {
            return;
        }
        mApplicationLaunching = true;
        
        // Make a pre-initialized database if necessary.
        AsamDbHelper dbHelper = new AsamDbHelper(this);
        if (!dbHelper.doesSeededAsamDbExist()) {
            try {
                dbHelper.initializeSeededAsamDb();
            }
            catch (IOException caught) {
                AsamLog.e("Error creating DB", caught);
            }
        }
        
        // Launch to the correct screen.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hideDisclaimer = preferences.getBoolean(AsamConstants.HIDE_DISCLAIMER_KEY, false);
        Intent intent = hideDisclaimer ?
                new Intent(this, AsamMapActivity.class) :
                new Intent(this, DisclaimerActivity.class);

        startActivity(intent);
        finish();
    }
}
