package mil.nga.giat.asam;

import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.SyncTime;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;


public class PreferencesDialogFragment extends DialogFragment {

    public interface OnPreferencesDialogDismissedListener {
        public void onPreferencesDialogDismissed(boolean hideDisclaimer);
    }
    
    public static PreferencesDialogFragment newInstance() {
        PreferencesDialogFragment dialogFragment = new PreferencesDialogFragment();
        return dialogFragment;
    }
    
    private OnPreferencesDialogDismissedListener mOnPreferencesDialogDismissedListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, 0);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.preferences, null);
        TextView syncTimeUI = (TextView)view.findViewById(R.id.preferences_last_sync_time_ui);
        syncTimeUI.setText(SyncTime.getLastSyncTimeAsText(getActivity()));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        CheckBox disclaimerCheckBoxUI = (CheckBox)view.findViewById(R.id.preferences_hide_disclaimer_checkbox_ui);
        disclaimerCheckBoxUI.setChecked(preferences.getBoolean(AsamConstants.HIDE_DISCLAIMER_KEY, false));
        view.setMinimumWidth((int)(300 * getResources().getDisplayMetrics().density));
        view.setMinimumHeight((int)(200 * getResources().getDisplayMetrics().density));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setIcon(R.drawable.ic_launcher_pirate);
        builder.setTitle(getString(R.string.preferences_title_text));
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.preferences_dialog_close_button_text), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // no-op
            }
        });
        return builder.create();
    }
    
    @Override
    public void onDismiss(DialogInterface dialog) {
        boolean hideDisclaimer = ((CheckBox)getDialog().findViewById(R.id.preferences_hide_disclaimer_checkbox_ui)).isChecked();
        mOnPreferencesDialogDismissedListener.onPreferencesDialogDismissed(hideDisclaimer);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnPreferencesDialogDismissedListener = (OnPreferencesDialogDismissedListener)activity;
        }
        catch (ClassCastException caught) {
            throw new ClassCastException(activity.toString() + " must implement OnPreferencesDialogDismissedListener");
        }
    }
}
