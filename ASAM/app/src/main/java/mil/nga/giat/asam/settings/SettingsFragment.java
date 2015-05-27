package mil.nga.giat.asam.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mil.nga.giat.asam.R;


public class SettingsFragment extends Fragment {

    private View aboutRow;

    public interface InfoClickListener {
        void onAboutClick();
        void onDisclaimerClick();
        void onPrivacyPolicyClick();
        void onOpenSourceClick();
        void onSyncClick();
    }

    private InfoClickListener infoClickListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_info_fragment, container, false);

        aboutRow = view.findViewById(R.id.about_row);
        aboutRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoClickListener.onAboutClick();
            }
        });

        view.findViewById(R.id.disclaimer_row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoClickListener.onDisclaimerClick();
            }
        });

        view.findViewById(R.id.sync_row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoClickListener.onSyncClick();
            }
        });

        view.findViewById(R.id.privacy_policy_row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoClickListener.onPrivacyPolicyClick();
            }
        });

        view.findViewById(R.id.open_source_row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoClickListener.onOpenSourceClick();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            infoClickListener = (InfoClickListener) activity;
        } catch (ClassCastException caught) {
            throw new ClassCastException(activity.toString() + " must implement InfoClickListener");
        }
    }

    public void selectFirstRow() {
        aboutRow.performClick();
    }
}
