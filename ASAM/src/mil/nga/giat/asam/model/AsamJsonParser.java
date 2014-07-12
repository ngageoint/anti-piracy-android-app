package mil.nga.giat.asam.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mil.nga.giat.asam.util.AsamLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AsamJsonParser {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    
    public List<AsamBean> parseJson(String json) throws Exception {
        List<AsamBean> asams = new ArrayList<AsamBean>();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            AsamBean asam = new AsamBean();
            asam.setReferenceNumber(extractString("Reference", object));
            asam.setAggressor(extractString("Aggressor", object));
            asam.setVictim(extractString("Victim", object));
            asam.setDescription(extractString("Description", object));
            asam.setGeographicalSubregion(extractString("Subregion", object));
            asam.setLatitude(extractDouble("lat", object));
            asam.setLongitude(extractDouble("lng", object));
            asam.setOccurrenceDate(extractDate("Date", object));
            asams.add(asam);
        }
        return asams;
    }
    
    private String extractString(String key, JSONObject object) {
        String value = null;
        if (object.has(key) && !object.isNull(key)) {
            try {
                value = object.getString(key).trim().replaceAll("\\+s", " ");
            }
            catch (JSONException caught) {
                AsamLog.e(AsamJsonParser.class.getName() + ":Error extracting " + key, caught);
            }
        }
        return value;
    }
    
    private Double extractDouble(String key, JSONObject object) {
        Double value = null;
        if (object.has(key) && !object.isNull(key)) {
            try {
                value = object.getDouble(key);
            }
            catch (JSONException caught) {
                AsamLog.e(AsamJsonParser.class.getName() + ":Error extracting " + key, caught);
            }
        }
        return value;
    }
    
    private Date extractDate(String key, JSONObject object) {
        Date value = null;
        if (object.has(key) && !object.isNull(key)) {
            try {
                value = DATE_FORMAT.parse(object.getString(key));
            }
            catch (Exception caught) {
                AsamLog.e(AsamJsonParser.class.getName() + ":Error extracting " + key, caught);
            }
        }
        return value;
    }
}
