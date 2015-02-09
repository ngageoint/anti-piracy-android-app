package mil.nga.giat.asam;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;


public class DisclaimerDialogFragment extends DialogFragment {
    
    public interface OnDisclaimerDialogDismissedListener {
        public void onDisclaimerDialogDismissed(boolean exitApplication);
    }

    public static DisclaimerDialogFragment newInstance() {
        DisclaimerDialogFragment dialogFragment = new DisclaimerDialogFragment();
        return dialogFragment;
    }
    
    private OnDisclaimerDialogDismissedListener mOnDisclaimerDialogDismissedListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, 0);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.disclaimer_fragment, null);
        view.setMinimumWidth((int)(500 * getResources().getDisplayMetrics().density));
        view.setMinimumHeight((int)(400 * getResources().getDisplayMetrics().density));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setIcon(R.drawable.ic_launcher_pirate);
        builder.setTitle(getString(R.string.disclaimer_title_text));
        builder.setPositiveButton(getString(R.string.disclaimer_agree_button_text), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            	if (mOnDisclaimerDialogDismissedListener != null) {
                    mOnDisclaimerDialogDismissedListener.onDisclaimerDialogDismissed(false);
            	}
            	
            	mOnDisclaimerDialogDismissedListener = null;
            }
        });
        builder.setNegativeButton(getString(R.string.disclaimer_exit_button_text), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            	if (mOnDisclaimerDialogDismissedListener != null) {
                    mOnDisclaimerDialogDismissedListener.onDisclaimerDialogDismissed(true);
            	}
            	
            	mOnDisclaimerDialogDismissedListener = null;
            }
        });
        return builder.create();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnDisclaimerDialogDismissedListener = (OnDisclaimerDialogDismissedListener)activity;
        }
        catch (ClassCastException caught) {
            throw new ClassCastException(activity.toString() + " must implement OnDisclaimerDialogDismissedListener");
        }
    }
}
