package mil.nga.giat.asam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vividsolutions.jts.geom.Geometry;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

import mil.nga.giat.asam.map.OfflineMap;
import mil.nga.giat.asam.map.SingleAsamMapActivity;
import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamConstants;


public class AsamReportFragment extends Fragment implements Asam.OnOfflineFeaturesListener, OnMapReadyCallback {

    private AsamBean asam;
    private TextView occurrenceDate;
    private TextView aggressor;
    private TextView victim;
    private TextView subregion;
    private TextView navArea;
    private TextView referenceNumber;
    private TextView location;
    private TextView description;

    private MapView mapView;
    private GoogleMap map;
    private int mMapType;
    private OfflineMap offlineMap;
    private Collection<Geometry> offlineGeometries = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.asam_report_fragment, container, false);

        occurrenceDate = view.findViewById(R.id.asam_report_fragment_occurrence_date_ui);
        aggressor = view.findViewById(R.id.asam_report_fragment_hostility_ui);
        victim = view.findViewById(R.id.asam_report_fragment_victim_ui);
        subregion = view.findViewById(R.id.asam_report_fragment_subregion_ui);
        navArea = view.findViewById(R.id.asam_report_fragment_navarea_ui);
        referenceNumber = view.findViewById(R.id.asam_report_fragment_reference_number_ui);
        location = view.findViewById(R.id.asam_report_fragment_location_ui);
        description = view.findViewById(R.id.asam_report_fragment_description_ui);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        ((Asam) getActivity().getApplication()).unregisterOfflineMapListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void mapAsamLocation(View view) {
        Intent intent = new Intent(getActivity(), SingleAsamMapActivity.class);
        intent.putExtra(AsamConstants.ASAM_KEY, asam);
        startActivity(intent);
    }

    public void updateContent(AsamBean asam) {
        this.asam = asam;

        occurrenceDate.setText(AsamBean.OCCURRENCE_DATE_FORMAT.format(asam.getOccurrenceDate()));

        if (StringUtils.isBlank(asam.getAggressor())) {
            aggressor.setText("Unknown");
        } else {
            aggressor.setText(asam.getAggressor());
        }

        if (StringUtils.isBlank(asam.getVictim())) {
            victim.setText("Unknown");
        } else {
            victim.setText(asam.getVictim());
        }

        subregion.setText(StringUtils.isBlank(asam.getGeographicalSubregion()) ? " " : asam.getGeographicalSubregion());
        navArea.setText(StringUtils.isBlank(asam.getNavArea()) ? " " : asam.getNavArea());
        referenceNumber.setText(StringUtils.isBlank(asam.getReferenceNumber()) ? " " : asam.getReferenceNumber());
        location.setText(asam.formatLatitutdeDegMinSec() + ", " + asam.formatLongitudeDegMinSec());
        description.setText(StringUtils.isBlank(asam.getDescription()) ? " " : asam.getDescription());
    }

    @Override
    public void onOfflineFeaturesLoaded(Collection<Geometry> offlineGeometries) {
        this.offlineGeometries = offlineGeometries;

        if (offlineMap == null && mMapType == AsamConstants.MAP_TYPE_OFFLINE_110M) {
            if (offlineMap != null) offlineMap.clear();
            offlineMap = new OfflineMap(getActivity(), map, offlineGeometries);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        ((Asam) getActivity().getApplication()).registerOfflineMapListener(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int mapType = preferences.getInt(AsamConstants.MAP_TYPE_KEY, GoogleMap.MAP_TYPE_NORMAL);
        if (mMapType != mapType) setMapType(mapType);

        LatLng latLng = new LatLng(asam.getLatitude(), asam.getLongitude());
        map.addMarker(new MarkerOptions().position(latLng).icon(AsamConstants.PIRATE_MARKER).anchor(0.5f, 0.5f));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
    }

    private void setMapType(int mapType) {
        mMapType = mapType;

        // Change the map
        if (mapType == AsamConstants.MAP_TYPE_OFFLINE_110M) {
            if (offlineMap != null) offlineMap.clear();

            offlineMap = new OfflineMap(getActivity(), map, offlineGeometries);
        } else {
            if (offlineMap != null) {
                offlineMap.clear();
                offlineMap = null;
            }

            map.setMapType(mapType);
        }
    }
}
