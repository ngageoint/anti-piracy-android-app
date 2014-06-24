package mil.nga.giat.asam;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.WindowManager;


public class InfoDialogFragment extends DialogFragment {

    public static InfoDialogFragment newInstance() {
        InfoDialogFragment dialogFragment = new InfoDialogFragment();
        return dialogFragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, 0);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.info_fragment, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setIcon(R.drawable.ic_launcher_pirate);
        builder.setTitle(getString(R.string.info_title_text));
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.info_dialog_fragment_close_button_text), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // no-op
            }
        });
        Dialog dialog = builder.create();
        dialog.show();

        WindowManager.LayoutParams layoutParameters = new WindowManager.LayoutParams();
        layoutParameters.copyFrom(dialog.getWindow().getAttributes());
        layoutParameters.width = (int)(500 * getResources().getDisplayMetrics().density);
        dialog.getWindow().setAttributes(layoutParameters);
        return dialog;
    }
}
