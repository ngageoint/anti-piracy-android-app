package mil.nga.giat.asam.settings.disclaimer;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mil.nga.giat.asam.R;


public class DisclaimerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_disclaimer_fragment, container, false);
        return view;
    }
}
