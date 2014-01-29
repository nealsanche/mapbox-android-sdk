package com.mapbox.mapboxsdk;

import android.graphics.Color;
import android.graphics.Paint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import org.osmdroid.views.overlay.*;
import com.mapbox.mapboxsdk.Marker;

/**
 * A GeoJSON parser.
 */
public class GeoJSON {

    /**
     * Parse a string of GeoJSON data, returning an array of Overlay objects.
     *
     * @param jsonString
     * @return
     * @throws JSONException
     */
    static ArrayList<Overlay> parseString(String jsonString) throws JSONException {
        return parse(new JSONObject(jsonString));
    }

    /**
     * Parse a GeoJSON object into an array of overlays.
     *
     * @param json
     * @return
     * @throws JSONException
     */
    static ArrayList<Overlay> parse(JSONObject json) throws JSONException {
        ArrayList<Overlay> overlays = new ArrayList<Overlay>();
        String type = json.optString("type");
        if (type.equals("FeatureCollection")) {
            return featureCollectionToLayers(json);
        } else if (type.equals("Feature")) {
            featureToLayer(json, overlays);
        }
        return overlays;
    }

    static ArrayList<Overlay> featureCollectionToLayers(JSONObject featureCollection) throws JSONException {
        JSONArray features = (JSONArray) featureCollection.get("features");
        ArrayList<Overlay> overlays = new ArrayList<Overlay>();
        for (int i = 0; i < features.length(); i++) {
            featureToLayer((JSONObject) features.get(i), overlays);
        }
        return overlays;
    }

    /**
     * Parse a GeoJSON feature object into some number of overlays, adding them to the overlays
     * array.
     *
     * @param feature
     * @param overlays
     * @throws JSONException
     */
    static void featureToLayer(JSONObject feature, ArrayList<Overlay> overlays) throws JSONException {

        JSONObject properties = (JSONObject) feature.get("properties");
        String title = "";
        title = properties.optString("title");

        JSONObject geometry = (JSONObject) feature.get("geometry");
        String type = geometry.optString("type");

        int j;

        /*
        if (type.equals("Point")) {
            JSONArray coordinates = (JSONArray) geometry.get("coordinates");
            double lon = (Double) coordinates.get(0);
            double lat = (Double) coordinates.get(1);
            return overlays.add(Marker(lat, lon, title, ""));
        } else if (type.equals("MultiPoint")) {
            JSONArray points = (JSONArray) geometry.get("coordinates");
            for (j = 0; j < points.length(); j++) {
                JSONArray coordinates = (JSONArray) points.get(j);
                double lon = (Double) coordinates.get(0);
                double lat = (Double) coordinates.get(1);
                overlays.add(Marker(lat, lon, title, ""));
            }
        } else
        */
        if (type.equals("LineString")) {
            PathOverlay path = new PathOverlay();
            JSONArray points = (JSONArray) geometry.get("coordinates");
            JSONArray coordinates;
            for (j = 0; j < points.length(); j++) {
                coordinates = (JSONArray) points.get(j);
                double lon = (Double) coordinates.get(0);
                double lat = (Double) coordinates.get(1);
                path.addPoint(new org.osmdroid.util.GeoPoint(lat, lon));
            }
            overlays.add(path);
        } else if (type.equals("MultiLineString")) {
            JSONArray lines = (JSONArray) geometry.get("coordinates");
            for (int k = 0; k < lines.length(); k++) {
                PathOverlay path = new PathOverlay();
                JSONArray points = (JSONArray) lines.get(k);
                JSONArray coordinates;
                for (j = 0; j < points.length(); j++) {
                    coordinates = (JSONArray) points.get(j);
                    double lon = (Double) coordinates.get(0);
                    double lat = (Double) coordinates.get(1);
                    path.addPoint(new org.osmdroid.util.GeoPoint(lat, lon));
                }
                overlays.add(path);
            }
        } else if (type.equals("Polygon")) {
            PathOverlay path = new PathOverlay();
            path.getPaint().setStyle(Paint.Style.FILL);
            JSONArray points = (JSONArray) geometry.get("coordinates");
            JSONArray outerRing = (JSONArray) points.get(0);
            JSONArray coordinates;
            for (j = 0; j < outerRing.length(); j++) {
                coordinates = (JSONArray) outerRing.get(j);
                double lon = (Double) coordinates.get(0);
                double lat = (Double) coordinates.get(1);
                path.addPoint(new org.osmdroid.util.GeoPoint(lat, lon));
            }
            overlays.add(path);
        }
    }
}
