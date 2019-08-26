package mil.nga.giat.asam.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;
import java.util.Locale;

import mil.nga.giat.asam.model.SubregionBean;


public class CurrentSubregionHelper implements LocationListener {

    private Location mCurrentLocation;
    private List<SubregionBean> mSubregions;
    private Integer mCurrentSubregion;
    private LocationManager mLocationManager;
    
    public CurrentSubregionHelper(Context context, List<SubregionBean> subregions) {
        mLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            mCurrentLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            AsamLog.e("Cannot request location", e);
        }

        mSubregions = subregions;
    }
    
    public synchronized int getCurrentSubregion() {
        if (mCurrentSubregion == null) {
            for (SubregionBean subregion : mSubregions) {
                if (subregionContainsCurrentLocation(subregion.getGeoPoints())) {
                    mCurrentSubregion = subregion.getSubregionId();
                    AsamLog.i(String.format(Locale.US, CurrentSubregionHelper.class.getName() + ":Found subregion %d for lat:%f lon:%f", mCurrentSubregion, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                    break;
                }
            }

            mLocationManager.removeUpdates(this);
            return mCurrentSubregion == null ? 11 : mCurrentSubregion;
        }

        return mCurrentSubregion;
    }

    @Override
    public synchronized void onLocationChanged(Location location) {
        AsamLog.i(CurrentSubregionHelper.class.getName() + ":Got a better geolocation fix");
        mCurrentLocation = location;
        mLocationManager.removeUpdates(this);
        for (SubregionBean subregion : mSubregions) {
            if (subregionContainsCurrentLocation(subregion.getGeoPoints())) {
                mCurrentSubregion = subregion.getSubregionId();
                AsamLog.i(String.format(Locale.US, CurrentSubregionHelper.class.getName() + ":Found better subregion %d for lat:%f lon:%f", mCurrentSubregion, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                break;
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    
    private boolean subregionContainsCurrentLocation(List<SubregionBean.GeoPoint> points) {
        if (mCurrentLocation == null) {
            return false;
        }
        boolean contains = false;
        for (int i = 0, j = points.size() - 1; i < points.size() - 0; j = i++) {
            if (((points.get(i).latitude > mCurrentLocation.getLatitude()) != (points.get(j).latitude > mCurrentLocation.getLatitude())) && (mCurrentLocation.getLongitude() < (points.get(j).longitude - points.get(i).longitude) * (mCurrentLocation.getLatitude() - points.get(i).latitude) / (points.get(j).latitude - points.get(i).latitude) + points.get(i).longitude)) {
                contains = !contains;
            }
        }
        return contains;
    }
}
