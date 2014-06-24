package mil.nga.giat.asam;

import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamConstants;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class SingleAsamMapActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_asam_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        AsamBean asam = null;
        CameraPosition initialPosition = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asam = (AsamBean)extras.getSerializable(AsamConstants.ASAM_KEY);
            initialPosition = (CameraPosition)extras.getParcelable(AsamConstants.INITIAL_MAP_POSITION_KEY);
        }
        GoogleMap mapUI = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.single_asam_map_map_view_ui)).getMap();

        LatLng markerPosition = new LatLng(asam.getLatitude(), asam.getLongitude());
        String title = asam.getVictim();
        String snippet = String.format(getResources().getString(R.string.single_asam_map_snippet_text), AsamBean.OCCURRENCE_DATE_FORMAT.format(asam.getOccurrenceDate()));
        mapUI.addMarker(new MarkerOptions().position(markerPosition).title(title).snippet(snippet).icon(AsamConstants.PIRATE_MARKER).anchor(0.5f, 0.5f));
        
        float zoomLevel = AsamConstants.SINGLE_ASAM_ZOOM_LEVEL;
        if (initialPosition != null) {
            zoomLevel = initialPosition.zoom;
            mapUI.moveCamera(CameraUpdateFactory.newCameraPosition(initialPosition));
            mapUI.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(markerPosition).zoom(zoomLevel).build()));
        }
        else {
            mapUI.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(markerPosition).zoom(zoomLevel).build()));
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
