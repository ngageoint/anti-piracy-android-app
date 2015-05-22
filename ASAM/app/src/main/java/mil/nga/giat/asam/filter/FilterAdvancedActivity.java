package mil.nga.giat.asam.filter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import mil.nga.giat.asam.R;
import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.map.AsamMapActivity;
import mil.nga.giat.asam.model.SubregionTextParser;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.CurrentSubregionHelper;


public class FilterAdvancedActivity extends AppCompatActivity implements OnClickListener {

    private static final int SUBREGION_SPINNER_EMPTY_POSITION = 0;
    private static final int SUBREGION_SPINNER_CURRENT_LOCATION_POSITION = 1;

    private EditText mKeyword;
    private EditText mDateFromUI;
    private EditText mDateToUI;
    private Spinner mSubregionSpinnerUI;
    private EditText mReferenceNumberYearUI;
    private EditText mReferenceNumberIdUI;
    private EditText mVictimUI;
    private EditText mAggressorUI;
    private CurrentSubregionHelper mCurrentSubregionHelper;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(FilterAdvancedActivity.class.getName() + ":onCreate");
        setContentView(R.layout.filter_advanced);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.subregions, R.layout.subregion_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mSubregionSpinnerUI = (Spinner) findViewById(R.id.text_query_subregion_spinner_ui);
        mSubregionSpinnerUI.setAdapter(adapter);
        
        mReferenceNumberYearUI = (EditText) findViewById(R.id.text_query_reference_number_year_edit_text_ui);
        mReferenceNumberIdUI = (EditText) findViewById(R.id.text_query_reference_number_id_edit_text_ui);

        mKeyword = (EditText) findViewById(R.id.keyword);
        mDateFromUI = (EditText) findViewById(R.id.text_query_date_from_edit_text_ui);
        mDateToUI = (EditText) findViewById(R.id.text_query_date_to_edit_text_ui);
        mVictimUI = (EditText) findViewById(R.id.text_query_victim_edit_text_ui);
        mAggressorUI = (EditText) findViewById(R.id.text_query_aggressor_edit_text_ui);

        mDateFromUI.setOnClickListener(this);
        mDateToUI.setOnClickListener(this);

        findViewById(R.id.apply).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);

        mCurrentSubregionHelper = new CurrentSubregionHelper(this, new SubregionTextParser().parseSubregions(this));

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
        inflater.inflate(R.menu.filter_advanced, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

        //TODO subregion

        mAggressorUI.setText(filterParameters.mAggressor);
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

        if (mSubregionSpinnerUI.getSelectedItemPosition() == SUBREGION_SPINNER_CURRENT_LOCATION_POSITION) {
            parameters.mSubregion = mCurrentSubregionHelper.getCurrentSubregion() + "";
        } else if (mSubregionSpinnerUI.getSelectedItemPosition() != SUBREGION_SPINNER_EMPTY_POSITION) {
            parameters.mSubregion = ((String) mSubregionSpinnerUI.getSelectedItem()).split(" ")[1]; // Looks like "Subregion 32".
        }
        parameters.mAggressor = mAggressorUI.getText().toString();
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
        mSubregionSpinnerUI.setSelection(SUBREGION_SPINNER_EMPTY_POSITION);
        mKeyword.setText("");
        mDateFromUI.setText("");
        mDateToUI.setText("");
        mVictimUI.setText("");
        mAggressorUI.setText("");
        mReferenceNumberYearUI.setText("");
        mReferenceNumberIdUI.setText("");
    }
}
