package mil.nga.giat.asam.connectivity;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver  {

    public interface ConnectivityEventListener {

        public void onAllDisconnected();
        
        public void onAnyConnected();
    }

    
	/**
	 * Singleton.
	 */
	private static NetworkChangeReceiver mNetworkChangeReceiver;

	/**
	 * Do not use!
	 */
	public NetworkChangeReceiver() {
		
	}
	
	public static NetworkChangeReceiver getInstance() {
		if (mNetworkChangeReceiver == null) {
			mNetworkChangeReceiver = new NetworkChangeReceiver();
		}
		return mNetworkChangeReceiver;
	}	
	
	private static final int sleepDelay = 10; // in seconds
	
	private static final String LOG_NAME = NetworkChangeReceiver.class.getName();

	private static Collection<ConnectivityEventListener> listeners = new CopyOnWriteArrayList<ConnectivityEventListener>();

	private static ScheduledExecutorService connectionFutureWorker = Executors.newSingleThreadScheduledExecutor();
	private static ScheduledFuture<?> connectionDataFuture = null;
	private static Boolean oldConnectionAvailabilityState = null;	
	
	@Override
	public void onReceive(final Context context, final Intent intent) {
		final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		final boolean newConnectionAvailabilityState = wifi.isConnected() || mobile.isConnected();
		
		// set the old state if it's the first time through!
		if (oldConnectionAvailabilityState == null) {
			oldConnectionAvailabilityState = !newConnectionAvailabilityState;
		}

		// was there a change in general connectivity?
		if (oldConnectionAvailabilityState ^ newConnectionAvailabilityState) {
			// is mobile data now on?
			if (newConnectionAvailabilityState) {
				Runnable task = new Runnable() {
					public void run() {
						Log.d(LOG_NAME, "CONNECTIVITY IS ON");
						for (ConnectivityEventListener listener : listeners) {
							listener.onAnyConnected();
						}
					}
				};
				connectionDataFuture = connectionFutureWorker.schedule(task, sleepDelay, TimeUnit.SECONDS);	
			} else {
				if (connectionDataFuture != null) {
					connectionDataFuture.cancel(false);
					connectionDataFuture = null;
				}
				Log.d(LOG_NAME, "CONNECTIVITY IS OFF");
				for (ConnectivityEventListener listener : listeners) {
					listener.onAllDisconnected();
				}
			}
		}
		
		// set the old states!
		oldConnectionAvailabilityState = newConnectionAvailabilityState;
	}

	public boolean addListener(ConnectivityEventListener listener) {
		return listeners.add(listener);
	}

	public boolean removeListener(ConnectivityEventListener listener) {
		return listeners.remove(listener);
	}
}