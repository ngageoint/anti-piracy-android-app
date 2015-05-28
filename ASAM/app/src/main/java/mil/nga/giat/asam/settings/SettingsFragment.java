package mil.nga.giat.asam.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import mil.nga.giat.asam.PullAsamsTask;
import mil.nga.giat.asam.R;
import mil.nga.giat.asam.util.SyncTime;


public class SettingsFragment extends Fragment implements AdapterView.OnItemClickListener, PullAsamsTask.OnSyncCompletedListener {

    private ListView listView;
    private ListAdapter listAdapter;

    public interface SettingClickListener {
        void onAboutClick();
        void onDisclaimerClick();
        void onPrivacyPolicyClick();
        void onOpenSourceClick();
    }

    private SettingClickListener settingClickListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_info_fragment, container, false);

        listView = (ListView) view.findViewById(R.id.list_view);
        listAdapter = new SettingsAdapter(view.getContext());
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            settingClickListener = (SettingClickListener) activity;
        } catch (ClassCastException caught) {
            throw new ClassCastException(activity.toString() + " must implement InfoClickListener");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                settingClickListener.onAboutClick();
                break;
            case 1:
                settingClickListener.onDisclaimerClick();
                break;
            case 2:
                settingClickListener.onPrivacyPolicyClick();
                break;
            case 3:
                settingClickListener.onOpenSourceClick();
                break;
            case 4:
                onSyncClick();
                break;
        }
    }

    public void selectSetting(int position) {
        listView.performItemClick(listView, position, listView.getItemIdAtPosition(position));
    }

    @Override
    public void onSyncCompleted() {
        View v = listView.getChildAt(4);
        TextView date = (TextView) v.findViewById(R.id.title);
        date.setText(String.format(getActivity().getString(R.string.preferences_last_sync_time_label_text), SyncTime.getLastSyncTimeAsText(getActivity())));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getString(R.string.preferences_sync_complete_title_text));
        builder.setMessage(getString(R.string.preferences_sync_complete_description_text));
        builder.setPositiveButton(getString(R.string.preferences_ok_button_text), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void onSyncClick() {
        if (SyncTime.isSynched(getActivity())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getString(R.string.preferences_no_sync_title_text));
            builder.setMessage(getString(R.string.preferences_no_sync_description_text));
            builder.setPositiveButton(getString(R.string.preferences_ok_button_text), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            builder.show();
            return;
        }

        new PullAsamsTask(getActivity(), this).execute();
    }
}
