package br.com.lfdb.particity.util;

import android.app.Activity;
import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

public class BitmapUtils {

    private static Map<String, Bitmap> reportBitmaps = new HashMap<>();
    private static Map<String, Bitmap> inventoryBitmaps = new HashMap<>();

    public static Bitmap getReportMarker(Activity activity, String filename) {
        if (reportBitmaps.containsKey(filename)) {
            return reportBitmaps.get(filename);
        }

        Bitmap bitmap = ImageUtils.getScaled(activity, "reports", filename);
        reportBitmaps.put(filename, bitmap);
        return bitmap;
    }

    public static Bitmap getInventoryMarker(Activity activity, String filename) {
        if (inventoryBitmaps.containsKey(filename)) {
            return inventoryBitmaps.get(filename);
        }

        Bitmap bitmap = ImageUtils.getScaled(activity, "inventory", filename);
        inventoryBitmaps.put(filename, bitmap);
        return bitmap;
    }
}
