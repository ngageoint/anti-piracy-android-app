package mil.nga.giat.asam.connectivity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import mil.nga.giat.asam.R;

public class OfflineBannerFragment extends Fragment implements View.OnClickListener {

    public interface OnOfflineBannerClick {
        void onOfflineBannerClick();
    }

    private Button alertBannerButton;
    private OnOfflineBannerClick onOfflineBannerClickListener;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alert_banner, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onOfflineBannerClickListener = (OnOfflineBannerClick) context;
        } catch (ClassCastException caught) {
            throw new ClassCastException(context.toString() + " must implement OnOfflineBannerClick");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onOfflineBannerClickListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        alertBannerButton = (Button) getActivity().findViewById(R.id.alert_banner_button);
        alertBannerButton.setOnClickListener(this);
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Shows fragment
     */
    public void show() {
        getView().setVisibility(View.VISIBLE);
    }

    /**
     * Hides fragment
     */
    public void hide() {
        getView().setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        onOfflineBannerClickListener.onOfflineBannerClick();
    }

}
