package mil.nga.giat.asam.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import mil.nga.giat.asam.R;
import mil.nga.giat.asam.model.SubregionBean.GeoPoint;
import mil.nga.giat.asam.util.AsamLog;
import android.content.Context;


public class SubregionTextParser {

    public List<SubregionBean> parseSubregions(Context context) {
        BufferedReader in = null;
        List<SubregionBean> subregions = new ArrayList<SubregionBean>();
        try {
            in = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.subregions)));
            String line = null;
            int subregionId = -1;
            List<SubregionBean.GeoPoint> geoPoints;
            while ((line = in.readLine()) != null) {
                String[] entries = line.split(",");
                int tmpId = Integer.parseInt(entries[0]);
                subregionId = tmpId;
                geoPoints = new ArrayList<SubregionBean.GeoPoint>();
                for (int i = 1; i < entries.length; i += 2) {
                    double latitude = Double.parseDouble(entries[i]);
                    double longitude = Double.parseDouble(entries[i + 1]);
                    latitude = (latitude < 0) ? Math.max(-90.0, latitude) : Math.min(90.0, latitude);
                    longitude = (longitude < 0) ? Math.max(-180.0, longitude) : Math.min(180.0, longitude);
                    geoPoints.add(GeoPoint.newInstance(latitude, longitude));
                }
                subregions.add(new SubregionBean(subregionId, geoPoints));
            }
        }
        catch (Exception caught) {
            caught.printStackTrace();
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception ignore) {}
            }
        }
        AsamLog.v(SubregionTextParser.class.getName() + ":subregions size: " + subregions.size());
        return subregions;
    }
}
