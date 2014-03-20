package com.mapbox.mapboxsdk.tileprovider.tilesource;

import java.io.InputStream;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.util.LowMemoryException;

import android.graphics.drawable.Drawable;

public interface ITileLayer {

    /**
     * Get a rendered Drawable from the specified InputStream.
     *
     * @param aTileInputStream an InputStream
     * @return the rendered Drawable
     */
    Drawable getDrawable(InputStream aTileInputStream) throws LowMemoryException;

    /**
     * Set the current tile url template used in this layer
     *
     * @return the tile layer
     */
    public TileLayer setURL(final String aUrl);

    /**
     * Get the current tile url template used in this layer
     *
     * @return tile url string as a template string
     */
    public String getTileURL(final MapTile aTile, boolean hdpi);

    /**
     * Get the minimum zoom level this tile source can provide.
     *
     * @return the minimum zoom level
     */
    public float getMinimumZoomLevel();

    /**
     * Get the maximum zoom level this tile source can provide.
     *
     * @return the maximum zoom level
     */
    public float getMaximumZoomLevel();

    /**
     * Get the tile size in pixels this tile source provides.
     *
     * @return the tile size in pixels
     */
    public int getTileSizePixels();
}
