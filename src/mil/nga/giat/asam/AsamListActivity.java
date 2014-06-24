package mil.nga.giat.asam;

import java.util.List;

import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamListContainer;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.widget.AsamArrayAdapter;
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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;


public class AsamListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    
    private List<AsamBean> mAsams;
    private ListView mAsamListViewUI;
    private AsamArrayAdapter mAsamArrayAdapter;
    private Spinner mAsamListSortPopupSpinnerUI;
    private RadioButton mSortAscendingRadioButtonUI;
    private RadioButton mSortDescendingRadioButtonUI;
    private int mSortDirection;
    private int mSortPopupSpinnerSelection;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(AsamListActivity.class.getName() + ":onCreate");
        setContentView(R.layout.asam_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        mAsams = AsamListContainer.mAsams;
        mAsamListViewUI = (ListView)findViewById(R.id.asam_list_list_view_ui);
        mAsamArrayAdapter = new AsamArrayAdapter(this, R.layout.asam_list_row, mAsams);
        mSortDirection = AsamConstants.SORT_DESCENDING;
        mSortPopupSpinnerSelection = AsamConstants.OCURRENCE_DATE_SORT;
        mAsamArrayAdapter.sort(new AsamBean.DescendingOccurrenceDateComparator());
        mAsamListViewUI.setAdapter(mAsamArrayAdapter);
        mAsamListViewUI.setOnItemClickListener(this);
        
        TextView emptyListViewUI = new TextView(this);
        emptyListViewUI.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        emptyListViewUI.setText(getResources().getString(R.string.asam_list_empty_list_text));
        emptyListViewUI.setVisibility(View.GONE);
        ((ViewGroup)mAsamListViewUI.getParent()).addView(emptyListViewUI);
        mAsamListViewUI.setEmptyView(emptyListViewUI);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.asam_list_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else if (itemId == R.id.asam_list_menu_sort_ui) {
            sortListView();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, AsamReportActivity.class);
        intent.putExtra(AsamConstants.ASAM_KEY, mAsams.get(position));
        startActivity(intent);
    }
    
    private void sortListView() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.asam_list_sort_popup_title_text));
        View popupView = getLayoutInflater().inflate(R.layout.asam_list_sort_popup, null);
        mAsamListSortPopupSpinnerUI = (Spinner)popupView.findViewById(R.id.asam_list_sort_popup_sort_spinner_ui);
        mSortAscendingRadioButtonUI = (RadioButton)popupView.findViewById(R.id.asam_list_sort_popup_sort_ascending_radio_button_ui);
        mSortDescendingRadioButtonUI = (RadioButton)popupView.findViewById(R.id.asam_list_sort_popup_sort_descending_radio_button_ui);
        builder.setView(popupView);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sort_types, R.layout.asam_list_sort_popup_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mAsamListSortPopupSpinnerUI.setAdapter(adapter);
        mAsamListSortPopupSpinnerUI.setSelection(mSortPopupSpinnerSelection);
        mSortAscendingRadioButtonUI.setChecked(mSortDirection == AsamConstants.SORT_ASCENDING);
        mSortDescendingRadioButtonUI.setChecked(mSortDirection == AsamConstants.SORT_DESCENDING);
        builder.setPositiveButton(getString(R.string.asam_list_sort_popup_sort_button_text), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSortPopupSpinnerSelection = mAsamListSortPopupSpinnerUI.getSelectedItemPosition();
                mSortDirection = mSortAscendingRadioButtonUI.isChecked() ? AsamConstants.SORT_ASCENDING : AsamConstants.SORT_DESCENDING;
                switch (mSortPopupSpinnerSelection) {
                    case AsamConstants.AGGRESSOR_SORT:
                        if (mSortDirection == AsamConstants.SORT_ASCENDING) {
                            mAsamArrayAdapter.sort(new AsamBean.AscendingAggressorComparator());
                        }
                        else {
                            mAsamArrayAdapter.sort(new AsamBean.DescendingAggressorComparator());
                        }
                        break;
                        
                    case AsamConstants.OCURRENCE_DATE_SORT:
                        if (mSortDirection == AsamConstants.SORT_ASCENDING) {
                            mAsamArrayAdapter.sort(new AsamBean.AscendingOccurrenceDateComparator());
                        }
                        else {
                            mAsamArrayAdapter.sort(new AsamBean.DescendingOccurrenceDateComparator());
                        }
                        break;
                        
                    case AsamConstants.REFERENCE_NUMBER_SORT:
                        if (mSortDirection == AsamConstants.SORT_ASCENDING) {
                            mAsamArrayAdapter.sort(new AsamBean.AscendingReferenceNumberComparator());
                        }
                        else {
                            mAsamArrayAdapter.sort(new AsamBean.DescendingReferenceNumberComparator());
                        }
                        break;
                        
                    case AsamConstants.SUBREGION_SORT:
                        if (mSortDirection == AsamConstants.SORT_ASCENDING) {
                            mAsamArrayAdapter.sort(new AsamBean.AscendingSubregionComparator());
                        }
                        else {
                            mAsamArrayAdapter.sort(new AsamBean.DescendingSubregionComparator());
                        }
                        break;
                        
                    case AsamConstants.VICTIM_SORT:
                        if (mSortDirection == AsamConstants.SORT_ASCENDING) {
                            mAsamArrayAdapter.sort(new AsamBean.AscendingVictimComparator());
                        }
                        else {
                            mAsamArrayAdapter.sort(new AsamBean.DescendingVictimComparator());
                        }
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.asam_list_sort_popup_cancel_button_text), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }
}
