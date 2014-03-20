/**
 * @author Brad Leege <bleege@gmail.com>
 * Created on 3/9/14 at 2:50 PM
 */

package com.mapbox.mapboxsdk.util;

import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import java.lang.reflect.Field;

public class BitmapUtils {
    public static BitmapFactory.Options getBitmapOptions(DisplayMetrics mDisplayMetrics) {
        try {
            // TODO I think this can all be done without reflection now because all these properties are SDK 4
            final Field density = DisplayMetrics.class.getDeclaredField("DENSITY_DEFAULT");
            final Field inDensity = BitmapFactory.Options.class.getDeclaredField("inDensity");
            final Field inTargetDensity = BitmapFactory.Options.class.getDeclaredField("inTargetDensity");
            final Field targetDensity = DisplayMetrics.class.getDeclaredField("densityDpi");
            final BitmapFactory.Options options = new BitmapFactory.Options();
            inDensity.setInt(options, density.getInt(null));
            inTargetDensity.setInt(options, targetDensity.getInt(mDisplayMetrics));
            return options;
        } catch (final IllegalAccessException ex) {
            // ignore
        } catch (final NoSuchFieldException ex) {
            // ignore
        }
        return null;
    }
}
