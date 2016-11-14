package mil.nga.giat.asam.connectivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import mil.nga.giat.asam.R;

public class OfflineBannerFragment extends Fragment implements View.OnClickListener, NetworkChangeReceiver.ConnectivityEventListener {

    public interface OnOfflineBannerClick {
        void onOfflineBannerClick();
    }

    private View alertBanner;
    private Button alertBannerButton;
    private OnOfflineBannerClick onOfflineBannerClickListener;

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        getContext().registerReceiver(NetworkChangeReceiver.getInstance(), filter);
        NetworkChangeReceiver.getInstance().addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(NetworkChangeReceiver.getInstance());
        NetworkChangeReceiver.getInstance().removeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alert_banner, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onOfflineBannerClickListener = (OnOfflineBannerClick) activity;
        }
        catch (ClassCastException caught) {
            throw new ClassCastException(activity.toString() + " must implement OnOfflineBannerClick");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        alertBanner = getActivity().findViewById(R.id.alert_banner);
        alertBannerButton = (Button) getActivity().findViewById(R.id.alert_banner_button);
        alertBannerButton.setOnClickListener(this);

        if (isOnline(getActivity().getApplicationContext())) {
            alertBanner.setVisibility(View.GONE);
        } else {
            alertBanner.setVisibility(View.VISIBLE);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        onOfflineBannerClickListener.onOfflineBannerClick();
    }

    @Override
    public void onAllDisconnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertBanner.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onAnyConnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertBanner.setVisibility(View.GONE);
            }
        });
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
