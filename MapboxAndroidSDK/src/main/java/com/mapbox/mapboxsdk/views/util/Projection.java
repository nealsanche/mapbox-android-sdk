/**
 * A Projection serves to translate between the coordinate system of x/y on-screen pixel
 * coordinates and that of latitude/longitude points on the surface of the earth. You obtain a
 * Projection from MapView.getProjection(). You should not hold on to this object for more than
 * one draw, since the projection of the map could change. <br />
 * <br />
 * <I>Screen coordinates</I> are in the coordinate system of the screen's Canvas. The origin is
 * in the center of the plane. <I>Screen coordinates</I> are appropriate for using to draw to
 * the screen.<br />
 * <br />
 * <I>Map coordinates</I> are in the coordinate system of the standard Mercator projection. The
 * origin is in the upper-left corner of the plane. <I>Map coordinates</I> are appropriate for
 * use in the TileSystem class.<br />
 * <br />
 * <I>Intermediate coordinates</I> are used to cache the computationally heavy part of the
 * projection. They aren't suitable for use until translated into <I>screen coordinates</I> or
 * <I>map coordinates</I>.
 *
 * @author Nicolas Gramlich
 * @author Manuel Stahl
 */

package com.mapbox.mapboxsdk.views.util;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.GeoConstants;
import com.mapbox.mapboxsdk.tile.TileSystem;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.util.GeometryMath;
import com.mapbox.mapboxsdk.views.MapView;

public class Projection implements GeoConstants {
    private MapView mapView = null;

    private int viewWidth2;
    private int viewHeight2;
    private int worldSize2;
    private final int offsetX;
    private final int offsetY;

    private final BoundingBox mBoundingBoxProjection;
    private final float mZoomLevelProjection;
    private final Rect mScreenRectProjection;
    private final Rect mIntrinsicScreenRectProjection;
    private final float mMapOrientation;

    public Projection(final MapView mv) {
        super();
        this.mapView = mv;

        viewWidth2 = mapView.getWidth() >> 1;
        viewHeight2 = mapView.getHeight() >> 1;
        mZoomLevelProjection = mapView.getZoomLevel(false);
        worldSize2 = TileSystem.MapSize(mZoomLevelProjection) >> 1;

        offsetX = -worldSize2;
        offsetY = -worldSize2;

        mBoundingBoxProjection = mapView.getBoundingBox();
        mScreenRectProjection = mapView.getScreenRect(null);
        mIntrinsicScreenRectProjection = mapView.getIntrinsicScreenRect(null);
        mMapOrientation = mapView.getMapOrientation();
    }

    public float getZoomLevel() {
        return mZoomLevelProjection;
    }

    public int getHalfWorldSize() {
        return worldSize2;
    }

    public BoundingBox getBoundingBox() {
        return mBoundingBoxProjection;
    }

    public Rect getScreenRect() {
        return mScreenRectProjection;
    }

    public Rect getIntrinsicScreenRect() {
        return mIntrinsicScreenRectProjection;
    }

    public float getMapOrientation() {
        return mMapOrientation;
    }

    /**
     * Converts <I>screen coordinates</I> to the underlying LatLng.
     *
     * @param x
     * @param y
     * @return LatLng under x/y.
     */
    public ILatLng fromPixels(final float x, final float y) {
        final Rect screenRect = getIntrinsicScreenRect();
        return TileSystem.PixelXYToLatLong(screenRect.left + (int) x + worldSize2,
                screenRect.top + (int) y + worldSize2, mZoomLevelProjection);
    }

    /**
     * Converts <I>screen coordinates</I> to the underlying LatLng.
     *
     * @param x
     * @param y
     * @return LatLng under x/y.
     */
    public ILatLng fromPixels(final int x, final int y) {
        return fromPixels((float) x, (float) y);
    }

    /**
     * Converts from map pixels to a Point value. Optionally reuses an existing Point.
     *
     * @param x
     * @param y
     * @param reuse
     * @return
     */
    public Point fromMapPixels(final int x, final int y, final Point reuse) {
        final Point out;
        if (reuse != null) {
            out = reuse;
        } else {
            out = new Point();
        }
        out.set(x - viewWidth2, y - viewHeight2);
        out.offset(mapView.getScrollX(), mapView.getScrollY());
        return out;
    }

    /**
     * Converts a LatLng to its <I>screen coordinates</I>.
     *
     * @param in    the LatLng you want the <I>screen coordinates</I> of
     * @param reuse just pass null if you do not have a Point to be 'recycled'.
     * @return the Point containing the <I>screen coordinates</I> of the LatLng passed.
     */
    public PointF toMapPixels(final ILatLng in, final PointF reuse) {
        return toMapPixels(in.getLatitude(), in.getLongitude(), reuse);
    }

    public PointF toMapPixels(final double latitude, final double longitude, final PointF reuse) {
        final PointF out;
        if (reuse != null) {
            out = reuse;
        } else {
            out = new PointF();
        }
        final float zoom = getZoomLevel();
        final int mapSize = TileSystem.MapSize(zoom);
        final float scrollX = mapView.getScrollX();
        final float scrollY = mapView.getScrollY();
        TileSystem.LatLongToPixelXY(
                latitude,
                longitude,
                zoom, out);
        out.offset(offsetX, offsetY);
        if (Math.abs(out.x - scrollX)
                > Math.abs(out.x - mapSize - scrollX)) {
            out.x -= mapSize;
        }
        if (Math.abs(out.x - scrollX)
                > Math.abs(out.x + mapSize - scrollX)) {
            out.x += mapSize;
        }
        if (Math.abs(out.y - scrollY)
                > Math.abs(out.y - mapSize - scrollY)) {
            out.y -= mapSize;
        }
        if (Math.abs(out.y - scrollY)
                > Math.abs(out.y + mapSize - scrollY)) {
            out.y += mapSize;
        }
        return out;
    }

    /**
     * Performs only the first computationally heavy part of the projection. Call
     * toMapPixelsTranslated to get the final position.
     *
     * @param latitude  the latitude of the point
     * @param longitude the longitude of the point
     * @param reuse     just pass null if you do not have a Point to be 'recycled'.
     * @return intermediate value to be stored and passed to toMapPixelsTranslated.
     */
    public PointF toMapPixelsProjected(final double latitude, final double longitude,
                                       final PointF reuse) {
        final PointF out;
        if (reuse != null) {
            out = reuse;
        } else {
            out = new PointF();
        }
        TileSystem
                .LatLongToPixelXY(latitude, longitude, TileLayerConstants.MAXIMUM_ZOOMLEVEL, out);
        return out;
    }

    /**
     * Performs the second computationally light part of the projection. Returns results in
     * <I>screen coordinates</I>.
     *
     * @param in    the Point calculated by the toMapPixelsProjected
     * @param reuse just pass null if you do not have a Point to be 'recycled'.
     * @return the Point containing the <I>Screen coordinates</I> of the initial LatLng passed
     * to the toMapPixelsProjected.
     */
    public PointF toMapPixelsTranslated(final PointF in, final PointF reuse) {
        final PointF out;
        if (reuse != null) {
            out = reuse;
        } else {
            out = new PointF();
        }

        final float zoomDifference = TileLayerConstants.MAXIMUM_ZOOMLEVEL - getZoomLevel();
        out.set((int) (GeometryMath.rightShift(in.x, zoomDifference) + offsetX),
                (int) (GeometryMath.rightShift(in.y, zoomDifference) + offsetY));
        return out;
    }

    /**
     * Translates a rectangle from <I>screen coordinates</I> to <I>intermediate coordinates</I>.
     *
     * @param in the rectangle in <I>screen coordinates</I>
     * @return a rectangle in </I>intermediate coordindates</I>.
     */
    public Rect fromPixelsToProjected(final Rect in) {
        final Rect result = new Rect();

        final float zoomDifference = TileLayerConstants.MAXIMUM_ZOOMLEVEL - getZoomLevel();

        final int x0 = (int) GeometryMath.leftShift(in.left - offsetX, zoomDifference);
        final int x1 = (int) GeometryMath.leftShift(in.right - offsetX, zoomDifference);
        final int y0 = (int) GeometryMath.leftShift(in.bottom - offsetY, zoomDifference);
        final int y1 = (int) GeometryMath.leftShift(in.top - offsetY, zoomDifference);

        result.set(Math.min(x0, x1), Math.min(y0, y1), Math.max(x0, x1), Math.max(y0, y1));
        return result;
    }

    private static final String TAG = "Projection";
}
