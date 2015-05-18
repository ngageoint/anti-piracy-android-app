package mil.nga.giat.asam.filter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import mil.nga.giat.asam.R;
import mil.nga.giat.asam.map.AsamMapActivity;

public class FilterActivity extends AppCompatActivity implements Button.OnClickListener {

    private EditText keyword;
    private Spinner intervalSpinner;
    private FilterParameters queryParameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.advanced_filter).setOnClickListener(this);
        findViewById(R.id.apply).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);

        keyword = (EditText) findViewById(R.id.keyword);
        intervalSpinner = (Spinner) findViewById(R.id.interval_spinner);

        Intent intent = getIntent();
        queryParameters = intent.getParcelableExtra(AsamMapActivity.SEARCH_PARAMETERS);
        populateFields(queryParameters);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.reset:
                clearFields();
                return true;
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.filter_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.advanced_filter: {
                Intent intent = new Intent(this, FilterAdvancedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.apply: {
                Intent intent = new Intent();
                FilterParameters parameters = parseFields();
                intent.putExtra(AsamMapActivity.SEARCH_PARAMETERS, parameters);
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
            }
            case R.id.cancel: {
                finish();
                break;
            }
        }
    }

    private FilterParameters parseFields() {
        FilterParameters parameters = new FilterParameters(FilterParameters.Type.SIMPLE);

        parameters.mKeyword = keyword.getText().toString();
        parameters.mTimeInterval = getResources().getIntArray(R.array.filter_interval_values)[intervalSpinner.getSelectedItemPosition()];;

        return parameters;
    }

    private void populateFields(FilterParameters queryParameters) {
        if (queryParameters == null) return;

        keyword.setText(queryParameters.mKeyword);

        Integer timeInterval = queryParameters.mTimeInterval;
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
    }

    private void clearFields() {
        intervalSpinner.setSelection(0);
        keyword.setText("");
    }
}
