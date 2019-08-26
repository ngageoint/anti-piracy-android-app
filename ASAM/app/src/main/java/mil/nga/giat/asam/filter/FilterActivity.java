package mil.nga.giat.asam.filter;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import mil.nga.giat.asam.R;
import mil.nga.giat.asam.map.AsamMapActivity;
import mil.nga.giat.asam.model.SubregionTextParser;
import mil.nga.giat.asam.util.CurrentSubregionHelper;

public class FilterActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 100;

    private EditText keyword;
    private Spinner intervalSpinner;
    private CheckBox currentSubregion;

    private CurrentSubregionHelper currentSubregionHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.advanced).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAdvancedFilter();
            }
        });

        keyword = findViewById(R.id.keyword);
        intervalSpinner = findViewById(R.id.interval_spinner);
        currentSubregion = findViewById(R.id.current_subregion);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            currentSubregionHelper = new CurrentSubregionHelper(this, new SubregionTextParser().parseSubregions(this));
            populateFields();
        } else {
            ActivityCompat.requestPermissions(FilterActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.clear:
                clearFields();
                return true;
            case R.id.apply: {
                Intent intent = new Intent();
                FilterParameters parameters = parseFields();
                intent.putExtra(AsamMapActivity.SEARCH_PARAMETERS, parameters);
                setResult(Activity.RESULT_OK, intent);
                finish();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.filter, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            currentSubregionHelper = new CurrentSubregionHelper(this, new SubregionTextParser().parseSubregions(this));
        }

        populateFields();
    }

    private void onAdvancedFilter() {
        Intent intent = new Intent(this, FilterAdvancedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        FilterParameters parameters = parseFields();
        intent.putExtra(AsamMapActivity.SEARCH_PARAMETERS, parameters);
        startActivity(intent);
        finish();
    }

    private FilterParameters parseFields() {
        FilterParameters parameters = new FilterParameters(FilterParameters.Type.SIMPLE);

        parameters.mKeyword = keyword.getText().toString();
        parameters.mTimeInterval = getResources().getIntArray(R.array.filter_interval_values)[intervalSpinner.getSelectedItemPosition()];

        if (currentSubregion.isChecked()) {
            parameters.mSubregionIds.add(currentSubregionHelper.getCurrentSubregion());
        }

        return parameters;
    }

    private void populateFields() {
        Intent intent = getIntent();
        FilterParameters filterParameters = intent.getParcelableExtra(AsamMapActivity.SEARCH_PARAMETERS);

        if (filterParameters == null) return;

        keyword.setText(filterParameters.mKeyword);

        Integer timeInterval = filterParameters.mTimeInterval;
        if (timeInterval != null) {
            int index = 0;
            int[] intervalValues = getResources().getIntArray(R.array.filter_interval_values);
            for (; index < intervalValues.length; index++) {
                if (timeInterval == intervalValues[index]) {
                    break;
                }
            }
            intervalSpinner.setSelection(index);
        }


        if (currentSubregionHelper != null) {
            currentSubregion.setVisibility(View.VISIBLE);

            int currentSubregionId = currentSubregionHelper.getCurrentSubregion();
            if (filterParameters.mSubregionIds.size() == 1 && filterParameters.mSubregionIds.get(0) == currentSubregionId) {
                currentSubregion.setChecked(true);
            }
        }
    }

    private void clearFields() {
        intervalSpinner.setSelection(0);
        keyword.setText("");
        currentSubregion.setChecked(false);
    }
}
