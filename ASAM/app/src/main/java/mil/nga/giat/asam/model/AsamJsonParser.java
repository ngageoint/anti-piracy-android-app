package mil.nga.giat.asam.model;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mil.nga.giat.asam.util.AsamLog;


public class AsamJsonParser {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy MM dd hh:mm:ss", Locale.US);
    private static JsonFactory factory = new JsonFactory();

    public List<AsamBean> parseJson(InputStream is) throws JsonParseException, IOException {
        List<AsamBean> asams = new ArrayList<>();

        JsonParser parser = factory.createParser(is);
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            return asams;
        }

        if (parser.nextToken() != JsonToken.VALUE_STRING && !"asam".equals(parser.getCurrentName())) {
            return asams;
        }

        if (parser.nextToken() != JsonToken.START_ARRAY) {
            return asams;
        }

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            AsamBean asam = parseAsam(parser);
            if (asam != null) {
                asams.add(asam);
            }
        }

        return asams;
    }

    public AsamBean parseAsam(JsonParser parser) throws JsonParseException, IOException {
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            return null;
        }

        AsamBean asam = new AsamBean();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String name = parser.getCurrentName();
            if ("latitude".equals(name)) {
                parser.nextToken();
                asam.setLatitude(parseDouble(name, parser));
            } else if ("longitude".equals(name)) {
                parser.nextToken();
                asam.setLongitude(parseDouble(name, parser));
            } else if ("date".equals(name)) {
                parser.nextToken();
                asam.setOccurrenceDate(parseDate(name, parser));
            } else if ("reference".equals(name)) {
                parser.nextToken();
                asam.setReferenceNumber(parseString(name, parser));
            } else if ("subreg".equals(name)) {
                parser.nextToken();
                asam.setGeographicalSubregion(parseString(name, parser));
            } else if ("navArea".equals(name)) {
                parser.nextToken();
                asam.setNavArea(parseString(name, parser));
            } else if ("victim".equals(name)) {
                parser.nextToken();
                asam.setVictim(parseString(name, parser));
            } else if ("hostility".equals(name)) {
                parser.nextToken();
                asam.setHostility(parseString(name, parser));
            } else if ("description".equals(name)) {
                parser.nextToken();
                asam.setDescription(parseString(name, parser));
            } else {
                AsamLog.i(AsamJsonParser.class.getName() + ": Skipping key " + parser.getCurrentName());
                parser.nextToken();
                parser.skipChildren();
            }
        }

        return asam;
    }
    
    private String parseString(String key, JsonParser parser) {
        String value = null;
        try {
            value = parser.getText().trim().replaceAll("\\+s", " ");
        } catch (IOException e) {
            AsamLog.e(AsamJsonParser.class.getName() + ":Error extracting " + key, e);
        }

        return value;
    }
    
    private Double parseDouble(String key, JsonParser parser) {
        Double value = null;
        try {
            value = parser.getDoubleValue();
        } catch (IOException e) {
            AsamLog.e(AsamJsonParser.class.getName() + ":Error extracting " + key, e);
        }

        return value;
    }
    
    private Date parseDate(String key, JsonParser parser) {
        Date date = null;
        try {
            date = DATE_FORMAT.parse(parser.getText());
        } catch (Exception e) {
            AsamLog.e(AsamJsonParser.class.getName() + ":Error extracting " + key, e);
        }

        return date;
    }
}
