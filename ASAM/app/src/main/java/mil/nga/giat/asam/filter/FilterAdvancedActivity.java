package mil.nga.giat.asam.filter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import mil.nga.giat.asam.R;
import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.map.AsamMapActivity;
import mil.nga.giat.asam.map.SubregionMapActivity;


public class FilterAdvancedActivity extends AppCompatActivity implements OnClickListener {

    public static final String SUBREGIONS_EXTRA = "SUBREGIONS_EXTRA";

    private static final int PICK_SUBREGIONS_REQUEST = 1;

    private ArrayList<Integer> mSubregionIds = new ArrayList<>();

    private EditText mKeyword;
    private EditText mDateFromUI;
    private EditText mDateToUI;
    private EditText mSubregionsText;
    private EditText mReferenceNumberYearUI;
    private EditText mReferenceNumberIdUI;
    private EditText mVictimUI;
    private EditText mHostility;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.filter_advanced);
        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.subregions, R.layout.subregion_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        mReferenceNumberYearUI = findViewById(R.id.text_query_reference_number_year_edit_text_ui);
        mReferenceNumberIdUI = findViewById(R.id.text_query_reference_number_id_edit_text_ui);

        mKeyword = findViewById(R.id.keyword);
        mDateFromUI = findViewById(R.id.text_query_date_from_edit_text_ui);
        mDateToUI = findViewById(R.id.text_query_date_to_edit_text_ui);
        mSubregionsText = findViewById(R.id.subregions);
        mVictimUI = findViewById(R.id.text_query_victim_edit_text_ui);
        mHostility = findViewById(R.id.text_query_hostility_edit_text_ui);

        mDateFromUI.setOnClickListener(this);
        mDateToUI.setOnClickListener(this);

        findViewById(R.id.subregions).setOnClickListener(this);

        Intent intent = getIntent();
        FilterParameters filterParameters = intent.getParcelableExtra(AsamMapActivity.SEARCH_PARAMETERS);
        populateFields(filterParameters);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.clear: {
                clearFields();
                return true;
            }
            case R.id.apply:
                Intent intent = new Intent();
                FilterParameters parameters = parseFields();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                parameters.save(preferences);
                intent.putExtra(AsamMapActivity.SEARCH_PARAMETERS, parameters);
                setResult(Activity.RESULT_OK, intent);
                finish();
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
        inflater.inflate(R.menu.filter_advanced, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_SUBREGIONS_REQUEST) {
            if (resultCode == RESULT_OK) {
                populateSubregions(data.getIntegerArrayListExtra(SUBREGIONS_EXTRA));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.subregions: {
                Intent intent = new Intent(this, SubregionMapActivity.class);
                intent.putIntegerArrayListExtra(SUBREGIONS_EXTRA, mSubregionIds);
                startActivityForResult(intent, PICK_SUBREGIONS_REQUEST);
                break;
            }
            case R.id.text_query_date_from_edit_text_ui: {
                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        GregorianCalendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                        mDateFromUI.setText(AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(calendar.getTime()));
                    }
                };
                Calendar calendar = Calendar.getInstance();
                if (mDateFromUI != null && StringUtils.isNotBlank(mDateFromUI.getText().toString())) {
                    try {
                        Date date = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.parse(mDateFromUI.getText().toString());
                        calendar.setTime(date);
                    }
                    catch (ParseException caught) {} // ignore.
                }
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(FilterAdvancedActivity.this, listener, year, month, day);
                dialog.show();
                break;
            }
            case R.id.text_query_date_to_edit_text_ui: {
                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        GregorianCalendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                        mDateToUI.setText(AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(calendar.getTime()));
                    }
                };
                Calendar calendar = Calendar.getInstance();
                if (mDateToUI != null && StringUtils.isNotBlank(mDateToUI.getText().toString())) {
                    try {
                        Date date = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.parse(mDateToUI.getText().toString());
                        calendar.setTime(date);
                    }
                    catch (ParseException caught) {} // ignore.
                }
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(FilterAdvancedActivity.this, listener, year, month, day);
                dialog.show();
                break;
            }
        }
    }

    private void populateSubregions(ArrayList<Integer> subregionIds) {
        mSubregionIds = subregionIds;
        mSubregionsText.setText(StringUtils.join(subregionIds, ","));
    }

    private void populateFields(FilterParameters filterParameters) {
        if (filterParameters == null) return;

        mKeyword.setText(filterParameters.mKeyword);

        Date startDate  = filterParameters.getStartDateFromInterval();
        if (startDate != null) {
            mDateFromUI.setText(AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(startDate));
        }

        if (StringUtils.isNotBlank(filterParameters.mDateFrom)) {
            mDateFromUI.setText(filterParameters.mDateFrom);
        }

        if (StringUtils.isNotBlank(filterParameters.mDateTo)) {
            mDateToUI.setText(filterParameters.mDateTo);
        }

        populateSubregions(filterParameters.mSubregionIds);

        mHostility.setText(filterParameters.mHostility);
        mVictimUI.setText(filterParameters.mVictim);

        String[] referenceNumber = StringUtils.split(filterParameters.mReferenceNumber, "-");
        if (referenceNumber != null && referenceNumber.length == 2) {
            mReferenceNumberYearUI.setText(referenceNumber[0]);
            mReferenceNumberIdUI.setText(referenceNumber[1]);
        }
    }

    private FilterParameters parseFields() {
        FilterParameters parameters = new FilterParameters(FilterParameters.Type.ADVANCED);

        parameters.mKeyword = mKeyword.getText().toString();

        String dateFrom = mDateFromUI.getText().toString();
        parameters.mDateFrom = StringUtils.isNotBlank(dateFrom) ? dateFrom : null;

        String dateTo = mDateToUI.getText().toString();
        parameters.mDateTo = StringUtils.isNotBlank(dateTo) ? dateTo : null;

        parameters.mSubregionIds = mSubregionIds;

        parameters.mHostility = mHostility.getText().toString();
        parameters.mVictim = mVictimUI.getText().toString();
        if (StringUtils.isNotBlank(mReferenceNumberYearUI.getText().toString()) && StringUtils.isNotBlank(mReferenceNumberIdUI.getText().toString())) {
            parameters.mReferenceNumber = mReferenceNumberYearUI.getText().toString() + "-" + mReferenceNumberIdUI.getText().toString();
        }

        if (parameters.isEmpty()) {
            parameters.mType = FilterParameters.Type.SIMPLE;
        }

        return parameters;
    }

    private void clearFields() {
        mSubregionIds.clear();
        mSubregionsText.setText("");
        mKeyword.setText("");
        mDateFromUI.setText("");
        mDateToUI.setText("");
        mVictimUI.setText("");
        mHostility.setText("");
        mReferenceNumberYearUI.setText("");
        mReferenceNumberIdUI.setText("");
    }
}
