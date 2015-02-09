package mil.nga.giat.asam;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import mil.nga.giat.asam.db.AsamDbHelper;
import mil.nga.giat.asam.model.SubregionTextParser;
import mil.nga.giat.asam.model.TextQueryParametersBean;
import mil.nga.giat.asam.util.AsamUtils;
import mil.nga.giat.asam.util.CurrentSubregionHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class TextQueryDialogFragment extends DialogFragment {
    
    public interface OnTextQueryListener {
        public void onTextQuery(TextQueryParametersBean textQueryParameters);
    }

    public static TextQueryDialogFragment newInstance() {
        TextQueryDialogFragment dialogFragment = new TextQueryDialogFragment();
        return dialogFragment;
    }
    
    private static final int SUBREGION_SPINNER_EMPTY_POSITION = 0;
    private static final int SUBREGION_SPINNER_CURRENT_LOCATION_POSITION = 1;
    private Spinner mSubregionSpinnerUI;
    private EditText mReferenceNumberYearEditTextUI;
    private EditText mReferenceNumberIdEditTextUI;
    private EditText mDateFromEditTextUI;
    private EditText mDateToEditTextUI;
    private EditText mVictimEditTextUI;
    private EditText mAggressorEditTextUI;
    private Button mCancelButtonUI;
    private Button mQueryButtonUI;
    private Button mClearButtonUI;
    private OnTextQueryListener mOnTextQueryListener;
    private CurrentSubregionHelper mCurrentSubregionHelper;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, 0);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.text_query_dialog_fragment, null);
        mReferenceNumberYearEditTextUI = (EditText)view.findViewById(R.id.text_query_dialog_fragment_reference_number_year_edit_text_ui);
        mReferenceNumberIdEditTextUI = (EditText)view.findViewById(R.id.text_query_dialog_fragment_reference_number_id_edit_text_ui);
        mDateFromEditTextUI = (EditText)view.findViewById(R.id.text_query_dialog_fragment_date_from_edit_text_ui);
        mDateToEditTextUI = (EditText)view.findViewById(R.id.text_query_dialog_fragment_date_to_edit_text_ui);
        mVictimEditTextUI = (EditText)view.findViewById(R.id.text_query_dialog_fragment_victim_edit_text_ui);
        mAggressorEditTextUI = (EditText)view.findViewById(R.id.text_query_dialog_fragment_aggressor_edit_text_ui);
        mCancelButtonUI = (Button)view.findViewById(R.id.text_query_dialog_fragment_cancel_button_ui);
        mQueryButtonUI = (Button)view.findViewById(R.id.text_query_dialog_fragment_query_button_ui);
        mClearButtonUI = (Button)view.findViewById(R.id.text_query_dialog_fragment_clear_button_ui);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.subregions, R.layout.subregion_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mSubregionSpinnerUI = (Spinner)view.findViewById(R.id.text_query_dialog_fragment_subregion_spinner_ui);
        mSubregionSpinnerUI.setAdapter(adapter);
        
        mDateFromEditTextUI.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener listener;
                listener = new DatePickerDialog.OnDateSetListener() {
                    
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        GregorianCalendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                        mDateFromEditTextUI.setText(AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(calendar.getTime()));
                    }
                };
                Calendar calendar = Calendar.getInstance();
                if (mDateFromEditTextUI != null && !AsamUtils.isEmpty(mDateFromEditTextUI.getText().toString())) {
                    try {
                        Date date = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.parse(mDateFromEditTextUI.getText().toString());
                        calendar.setTime(date);
                    }
                    catch (ParseException caught) {} // ignore.
                }
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog mDateFromDialogUI = new DatePickerDialog(getActivity(),  listener,  year, month, day);
                mDateFromDialogUI.show();
            }
        });
        
        mDateToEditTextUI.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener listener;
                listener = new DatePickerDialog.OnDateSetListener() {
                    
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        GregorianCalendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                        mDateToEditTextUI.setText(AsamDbHelper.TEXT_QUERY_DATE_FORMAT.format(calendar.getTime()));
                    }
                };
                Calendar calendar = Calendar.getInstance();
                if (mDateToEditTextUI != null && !AsamUtils.isEmpty(mDateToEditTextUI.getText().toString())) {
                    try {
                        Date date = AsamDbHelper.TEXT_QUERY_DATE_FORMAT.parse(mDateToEditTextUI.getText().toString());
                        calendar.setTime(date);
                    }
                    catch (ParseException caught) {} // ignore.
                }
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog mDateToDialogUI = new DatePickerDialog(getActivity(),  listener,  year, month, day);
                mDateToDialogUI.show();
            }
        });
        
        mCancelButtonUI.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        
        mQueryButtonUI.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View view) {
                TextQueryParametersBean parameters = new TextQueryParametersBean();
                parameters.mDateFrom = mDateFromEditTextUI.getText().toString();
                parameters.mDateTo = mDateToEditTextUI.getText().toString();
                if (mSubregionSpinnerUI.getSelectedItemPosition() == SUBREGION_SPINNER_CURRENT_LOCATION_POSITION) {
                    parameters.mSubregion = mCurrentSubregionHelper.getCurrentSubregion() + "";
                }
                else if (mSubregionSpinnerUI.getSelectedItemPosition() != SUBREGION_SPINNER_EMPTY_POSITION) {
                    parameters.mSubregion = ((String)mSubregionSpinnerUI.getSelectedItem()).split(" ")[1]; // Looks like "Subregion 32".
                }
                parameters.mAggressor = mAggressorEditTextUI.getText().toString().trim();
                parameters.mVictim = mVictimEditTextUI.getText().toString().trim();
                String referenceNumberYear = mReferenceNumberYearEditTextUI.getText().toString();
                String referenceNumberId = mReferenceNumberIdEditTextUI.getText().toString();
                if (!AsamUtils.isEmpty(referenceNumberYear) && !AsamUtils.isEmpty(referenceNumberId)) {
                    parameters.mReferenceNumber = referenceNumberYear.trim() + "-" + referenceNumberId.trim();
                }
                if (!AsamUtils.isEmpty(referenceNumberYear) && AsamUtils.isEmpty(referenceNumberId) || AsamUtils.isEmpty(referenceNumberYear) && !AsamUtils.isEmpty(referenceNumberId)) {
                    Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.text_query_dialog_fragment_malformed_reference_number_warning_text), referenceNumberYear, referenceNumberId), Toast.LENGTH_LONG).show();
                    getDialog().dismiss();
                }
                else if (parameters.isEmpty()) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.text_query_dialog_fragment_empty_query_warning_text), Toast.LENGTH_LONG).show();
                    getDialog().dismiss();
                }
                else {
                    getDialog().dismiss();
                    mOnTextQueryListener.onTextQuery(parameters);
                }
            }
        });
        
        mClearButtonUI.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View view) {
                mSubregionSpinnerUI.setSelection(SUBREGION_SPINNER_EMPTY_POSITION);
                mReferenceNumberYearEditTextUI.setText("");
                mReferenceNumberIdEditTextUI.setText("");
                mDateFromEditTextUI.setText("");
                mDateToEditTextUI.setText("");
                mVictimEditTextUI.setText("");
                mAggressorEditTextUI.setText("");
            }
        });
        mCurrentSubregionHelper = new CurrentSubregionHelper(getActivity(), new SubregionTextParser().parseSubregions(getActivity()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setIcon(R.drawable.ic_launcher_pirate);
        builder.setTitle(getString(R.string.text_query_dialog_fragment_title_text));
        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnTextQueryListener = (OnTextQueryListener)activity;
        }
        catch (ClassCastException caught) {
            throw new ClassCastException(activity.toString() + " must implement OnTextQueryListener");
        }
    }
}
