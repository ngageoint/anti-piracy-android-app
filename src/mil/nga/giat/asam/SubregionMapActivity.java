package mil.nga.giat.asam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.nga.giat.asam.model.SubregionBean;
import mil.nga.giat.asam.model.SubregionTextParser;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamLog;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;


public class SubregionMapActivity extends ActionBarActivity implements OnMapClickListener {

    private static final int INITIAL_ZOOM_LEVEL = 2;
    private static final float OUTLINE_WIDTH = 1.0f;
    private static final int OUTLINE_COLOR = 0xffff8200; // ARGB 255, 255, 130, 0.
    private static final int SELECTED_FILL_COLOR = 0x50ff9600; // ARGB 128, 255, 150, 0.
    private static final int UNSELECTED_FILL_COLOR = 0x5000ff00; // ARGB 128, 0, 255, 0.
    private GoogleMap mMapUI;
    private List<SubregionBean> mSubregions;
    private MenuItem mResetMenuItemUI;
    private MenuItem mQueryMenuItemUI;
    private MenuItem mSelectedSubregionsMenuItemUI;
    private boolean mLaunchedExpectingResultCode;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(SubregionMapActivity.class.getName() + ":onCreate");
        setContentView(R.layout.subregion_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        Bundle extras = getIntent().getExtras();
        mLaunchedExpectingResultCode = false;
        if (extras != null) {
            mLaunchedExpectingResultCode = extras.getBoolean(AsamConstants.SUBREGION_MAP_EXPECTING_RESULT_CODE_KEY, false);
        }
        
        mMapUI = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.subregion_map_map_view_ui)).getMap();
        mMapUI.setOnMapClickListener(this);
        
        // Initialize subregions and place on map.
        mSubregions = new SubregionTextParser().parseSubregions(this);
        for (SubregionBean subregion : mSubregions) {
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.addAll(subregion.getMapCoordinates()).strokeColor(OUTLINE_COLOR).strokeWidth(OUTLINE_WIDTH).fillColor(UNSELECTED_FILL_COLOR);
            Polygon polygon = mMapUI.addPolygon(polygonOptions);
            subregion.setMapPolygon(polygon);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.subregion_map_menu, menu);
        mResetMenuItemUI = menu.findItem(R.id.subregion_map_menu_reset_ui);
        mQueryMenuItemUI = menu.findItem(R.id.subregion_map_menu_query_ui);
        mSelectedSubregionsMenuItemUI = menu.findItem(R.id.subregion_map_menu_selected_subregions_ui);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (mLaunchedExpectingResultCode) {
                Intent intent = getIntent();
                setResult(Activity.RESULT_CANCELED, intent);
            }
            finish();
            return true;
        }
        else if (itemId == R.id.subregion_map_menu_reset_ui) {
            resetMenuClicked();
            return true;
        }
        else if (itemId == R.id.subregion_map_menu_selected_subregions_ui) {
            showSelectedSubregionsPopup();
            return true;
        }
        else if (itemId == R.id.subregion_map_menu_60_days_ui) {
            showQueryOnMap(AsamConstants.TIME_SPAN_60_DAYS);
            return true;
        }
        else if (itemId == R.id.subregion_map_menu_90_days_ui) {
            showQueryOnMap(AsamConstants.TIME_SPAN_90_DAYS);
            return true;
        }
        else if (itemId == R.id.subregion_map_menu_180_days_ui) {
            showQueryOnMap(AsamConstants.TIME_SPAN_180_DAYS);
            return true;
        }
        else if (itemId == R.id.subregion_map_menu_1_year_ui) {
            showQueryOnMap(AsamConstants.TIME_SPAN_1_YEAR);
            return true;
        }
        else if (itemId == R.id.subregion_map_menu_5_years_ui) {
            showQueryOnMap(AsamConstants.TIME_SPAN_5_YEARS);
            return true;
        }
        else if (itemId == R.id.subregion_map_menu_all_ui) {
            showQueryOnMap(AsamConstants.TIME_SPAN_ALL);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        AsamLog.i(SubregionMapActivity.class.getName() + ":onResume");
        LatLng position = new LatLng(0.0, 0.0);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(INITIAL_ZOOM_LEVEL).build();
        mMapUI.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    
    @Override
    public void onMapClick(LatLng point) {
        AsamLog.v(SubregionMapActivity.class.getName() + ":onMapClick");
        for (SubregionBean subregion : mSubregions) {
            if (isSubregionTapped(subregion, point)) {
                subregion.setSelected(!subregion.isSelected());
                if (subregion.isSelected()) {
                    subregion.getMapPolygon().setFillColor(SELECTED_FILL_COLOR);
                }
                else {
                    subregion.getMapPolygon().setFillColor(UNSELECTED_FILL_COLOR);
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
                            }
                            else {
                                multiSubregion.getMapPolygon().setFillColor(UNSELECTED_FILL_COLOR);
                            }
                        }
                    }
                }
            }
        }
        setMenuState();
    }
    
    private void resetMenuClicked() {
        clearMap();
    }
    
    private void clearMap() {
        for (SubregionBean subregion : mSubregions) {
            subregion.setSelected(false);
            subregion.getMapPolygon().setFillColor(UNSELECTED_FILL_COLOR);
        }
        setMenuState();
    }
    
    private void setMenuState() {
        if (getSelectedSubregionIds().size() > 0) {
            mResetMenuItemUI.setEnabled(true);
            mQueryMenuItemUI.setEnabled(true);
            mSelectedSubregionsMenuItemUI.setEnabled(true);
        }
        else {
            mResetMenuItemUI.setEnabled(false);
            mQueryMenuItemUI.setEnabled(false);
            mSelectedSubregionsMenuItemUI.setEnabled(false);
        }
    }
    
    private void showSelectedSubregionsPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.selected_subregions_popup_title_text));
        View popupView = getLayoutInflater().inflate(R.layout.selected_subregions_popup, null);
        builder.setView(popupView);
        
        List<Integer> selectedSubregionIds = getSelectedSubregionIds();
        TextView subregionsTextUI = (TextView)popupView.findViewById(R.id.selected_subregions_popup_subregions_text_ui);
        StringBuilder subregionsText = new StringBuilder("");
        for (int i = 0; i < selectedSubregionIds.size(); i++) {
            subregionsText.append(selectedSubregionIds.get(i));
            if (i < selectedSubregionIds.size() - 1) {
                subregionsText.append(", ");
            }
        }
        subregionsTextUI.setText(subregionsText.toString());
        
        builder.setNegativeButton(getString(R.string.selected_subregions_popup_cancel_text), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        Dialog dialog = builder.create();
        dialog.show();
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
    
    private void showQueryOnMap(int timeSpan) {
        Intent intent = null;
        if (mLaunchedExpectingResultCode) {
            intent = getIntent();
        }
        else {
            intent = new Intent(this, AllAsamsMapActivity.class);
        }
        intent.putExtra(AsamConstants.QUERY_TYPE_KEY, AsamConstants.SUBREGION_QUERY);
        intent.putExtra(AsamConstants.SUBREGION_QUERY_TIME_SPAN_KEY, timeSpan);
        intent.putIntegerArrayListExtra(AsamConstants.SUBREGION_QUERY_SUBREGIONS_LIST_KEY, getSelectedSubregionIds());
        if (mLaunchedExpectingResultCode) {
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        else {
            startActivity(intent);
            clearMap(); // Reset everything to the initial state.
        }
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
}