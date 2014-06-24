package mil.nga.giat.asam;

import mil.nga.giat.asam.util.AsamConstants;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;


public class SortAsamListDialogFragment extends DialogFragment {

    public interface OnSortAsamListListener {
        public void onSortAsamList(int sortDirection, int sortPopupSpinnerSelection);
    }
    
    public static SortAsamListDialogFragment newInstance(int sortDirection, int sortPopupSpinnerSelection) {
        SortAsamListDialogFragment dialogFragment = new SortAsamListDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(AsamConstants.SORT_DIRECTION_KEY, sortDirection);
        arguments.putInt(AsamConstants.SPINNER_SELECTION_KEY, sortPopupSpinnerSelection);
        dialogFragment.setArguments(arguments);
        return dialogFragment;
    }
    
    private Spinner mSortSpinnerUI;
    private RadioButton mSortAscendingRadioButtonUI;
    private RadioButton mSortDescendingRadioButtonUI;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.sort_asam_list_dialog_fragment_title_text));
        View popupView = getActivity().getLayoutInflater().inflate(R.layout.sort_asam_list_dialog_fragment, null);
        mSortSpinnerUI = (Spinner)popupView.findViewById(R.id.sort_asam_list_dialog_fragment_sort_spinner_ui);
        mSortAscendingRadioButtonUI = (RadioButton)popupView.findViewById(R.id.sort_asam_list_dialog_fragment_sort_ascending_radio_button_ui);
        mSortDescendingRadioButtonUI = (RadioButton)popupView.findViewById(R.id.sort_asam_list_dialog_fragment_sort_descending_radio_button_ui);
        builder.setView(popupView);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sort_types, R.layout.sort_asam_list_dialog_fragment_spinner_item);
        adapter.setDropDownViewResource(R.layout.sort_asam_list_dialog_fragment_spinner_dropdown_item);
        mSortSpinnerUI.setAdapter(adapter);
        mSortSpinnerUI.setSelection(getArguments().getInt(AsamConstants.SPINNER_SELECTION_KEY));
        mSortAscendingRadioButtonUI.setChecked(getArguments().getInt(AsamConstants.SORT_DIRECTION_KEY) == AsamConstants.SORT_ASCENDING);
        mSortDescendingRadioButtonUI.setChecked(getArguments().getInt(AsamConstants.SORT_DIRECTION_KEY) == AsamConstants.SORT_DESCENDING);
        builder.setPositiveButton(getString(R.string.sort_asam_list_dialog_fragment_sort_button_text), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int sortPopupSpinnerSelection = mSortSpinnerUI.getSelectedItemPosition();
                int sortDirection = mSortAscendingRadioButtonUI.isChecked() ? AsamConstants.SORT_ASCENDING : AsamConstants.SORT_DESCENDING;
                ((OnSortAsamListListener)getActivity()).onSortAsamList(sortDirection, sortPopupSpinnerSelection);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.sort_asam_list_dialog_fragment_cancel_button_text), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
}
