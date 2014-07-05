package mil.nga.giat.asam.jackson.deserializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.vividsolutions.jts.geom.Geometry;

public class FeatureDeserializer extends Deserializer {

	private GeometryDeserializer geometryDeserializer = new GeometryDeserializer();

	public Collection<Geometry> parseFeatures(InputStream is) throws JsonParseException, IOException {
	    Collection<Geometry> features = new ArrayList<Geometry>();
		JsonParser parser = factory.createParser(is);
		parser.nextToken();

		if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
			return features;
		}

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String name = parser.getCurrentName();
			if ("features".equals(name)) {
				parser.nextToken();
				while (parser.nextToken() != JsonToken.END_ARRAY) {
				    Geometry geometry = parseFeature(parser);
					features.add(geometry);
				}
			} else {
				parser.nextToken();
				parser.skipChildren();
			}
		}

		parser.close();
		return features;
	}

	private Geometry parseFeature(JsonParser parser) throws JsonParseException, IOException {
	    Geometry geometry = null;
		if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
			Collections.emptyList();
		}

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String name = parser.getCurrentName();
			if ("geometry".equals(name)) {
				parser.nextToken();
				geometry = geometryDeserializer.parseGeometry(parser);
			} else {
				parser.nextToken();
				parser.skipChildren();
			}
		}
		
		return geometry;
	}
}