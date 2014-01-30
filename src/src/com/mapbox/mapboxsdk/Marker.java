package com.mapbox.mapboxsdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Overlay;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A Marker object is a visible representation of a point on a Map that has a geographical place.
 */
public class Marker extends Overlay {

    private Context context;
    private Tooltip tooltip;
    private MapView mapView;
    protected HotspotPlace mHotspotPlace;

    public static final int ITEM_STATE_FOCUSED_MASK = 4;
    public static final int ITEM_STATE_PRESSED_MASK = 1;
    public static final int ITEM_STATE_SELECTED_MASK = 2;

    protected Drawable mMarker;
    protected static final Point DEFAULT_MARKER_SIZE = new Point(26, 94);

    /**
     * Indicates a hotspot for an area. This is where the origin (0,0) of a point will be located
     * relative to the area. In otherwords this acts as an offset. NONE indicates that no adjustment
     * should be made.
     */
    public enum HotspotPlace {
        NONE, CENTER, BOTTOM_CENTER, TOP_CENTER, RIGHT_CENTER, LEFT_CENTER, UPPER_RIGHT_CORNER, LOWER_RIGHT_CORNER, UPPER_LEFT_CORNER, LOWER_LEFT_CORNER
    }


    /**
     * Initialize a new marker object
     *
     * @param center the location of the marker
     */
    public Marker(GeoPoint center) {
        super(center);
        fromMaki("markerstroked");
    }

    public Drawable getMarker(final int stateBitset) {
        // marker not specified
        if (mMarker == null) {
            return null;
        }
        // set marker state appropriately
        setState(mMarker, stateBitset);
        return mMarker;
    }

    public void setMarker(final Drawable marker) {
        this.mMarker = marker;
    }

    public void setMarkerHotspot(final HotspotPlace place) {
        this.mHotspotPlace = (place == null) ? HotspotPlace.BOTTOM_CENTER : place;
    }

    public HotspotPlace getMarkerHotspot() {
        return this.mHotspotPlace;
    }

    /**
     * Set this marker's icon to a marker from the Maki icon set.
     *
     * @param makiString the name of a Maki icon symbol
     */
    public void fromMaki(String makiString) {
        String urlString = makiString+"182x";
        int id = context.getResources().getIdentifier(urlString, "drawable", context.getPackageName());
        this.setMarker(context.getResources().getDrawable(id));
    }

    public void setTooltipVisible() {
        tooltip.setVisible(true);
        mapView.invalidate();
    }

    public void setTooltipInvisible() {
        tooltip.setVisible(false);
    }

    /*
	 * (copied from the Google API docs) Sets the state of a drawable to match a given state bitset.
	 * This is done by converting the state bitset bits into a state set of R.attr.state_pressed,
	 * R.attr.state_selected and R.attr.state_focused attributes, and then calling {@link
	 * Drawable.setState(int[])}.
	 */
    public static void setState(final Drawable drawable, final int stateBitset) {
        final int[] states = new int[3];
        int index = 0;
        if ((stateBitset & ITEM_STATE_PRESSED_MASK) > 0)
            states[index++] = android.R.attr.state_pressed;
        if ((stateBitset & ITEM_STATE_SELECTED_MASK) > 0)
            states[index++] = android.R.attr.state_selected;
        if ((stateBitset & ITEM_STATE_FOCUSED_MASK) > 0)
            states[index++] = android.R.attr.state_focused;

        drawable.setState(states);
    }

    public Drawable getDrawable() {
        return this.mMarker;
    }

    public int getWidth() {
        return this.mMarker.getIntrinsicWidth();
    }

    public int getHeight() {
        return this.mMarker.getIntrinsicHeight();
    }

    class BitmapLoader extends AsyncTask<String, Void,Bitmap> {

        @Override
        protected Bitmap doInBackground(String... src) {
            try {
                URL url = new URL(src[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(Bitmap bitmap){
            bitmap.setDensity(120);
            Marker.this.setMarker(new BitmapDrawable(bitmap));
        }
    }
}
