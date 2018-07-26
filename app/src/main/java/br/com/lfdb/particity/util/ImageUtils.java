package br.com.lfdb.particity.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import java.io.File;
import java.io.FileInputStream;

import br.com.lfdb.particity.ZupApplication;
import br.com.lfdb.particity.base.Defaults;

public class ImageUtils {

    public static Bitmap toGrayscale(Bitmap srcBitmap) {
        int height = srcBitmap.getHeight();
        int width = srcBitmap.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        paint.setAlpha(100);
        c.drawBitmap(srcBitmap, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap loadFromFile(Context context, String file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(new File(FileUtils.getImagesFolder(context) + File.separator + file).toString(), options);
    }

    public static Bitmap loadFromFile(Context context, String subfolder, String file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(new File(FileUtils.getImagesFolder(context, subfolder) + File.separator + file).toString(), options);
    }

    public static Bitmap getScaled(Activity activity, String filename) {
        float scale = ImageUtils.shouldDownloadRetinaIcon(activity) ? 0.5f : 1f;
        return getScaled(activity, scale, ImageUtils.loadFromFile(activity, filename));
    }

    public static Bitmap getScaled(Activity activity, String subfolder, String filename) {
        float scale = ImageUtils.shouldDownloadRetinaIcon(activity) ? 0.5f : 1f;
        return getScaled(activity, scale, ImageUtils.loadFromFile(activity, subfolder, filename));
    }

    public static Bitmap getHalfScaled(Activity activity, String subfolder, String filename) {
        float scale = ImageUtils.shouldDownloadRetinaIcon(activity) ? 0.25f : 0.5f;
        return getScaled(activity, scale, ImageUtils.loadFromFile(activity, subfolder, filename));
    }

    public static Bitmap getScaledCustom(Activity activity, String subfolder, String filename, float scale) {
        float divider = ImageUtils.shouldDownloadRetinaIcon(activity) ? 2f : 1f;
        return getScaled(activity, scale / divider, ImageUtils.loadFromFile(activity, subfolder, filename));
    }

    public static Bitmap getScaled(Activity activity, float scale, Bitmap bitmapOrg) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        try {
            int width = bitmapOrg.getWidth();
            int height = bitmapOrg.getHeight();

            float scaleWidth = metrics.scaledDensity * scale;
            float scaleHeight = metrics.scaledDensity * scale;

            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);

            return Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static StateListDrawable getStateListDrawable(Activity activity, String filename) {
        float scale = ImageUtils.shouldDownloadRetinaIcon(activity) ? 0.5f : 1f;
        Bitmap original = ImageUtils.getScaled(activity, scale, ImageUtils.loadFromFile(activity, filename));
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, new BitmapDrawable(activity.getResources(), original));
        states.addState(new int[]{}, new BitmapDrawable(activity.getResources(), ImageUtils.toGrayscale(original)));
        return states;
    }

    public static StateListDrawable getReportStateListDrawable(Activity activity, String activeFilename, String disabledFilename) {
        Bitmap active = ImageUtils.getScaled(activity, 1, ImageUtils.loadFromFile(activity, "reports", activeFilename));
        Bitmap disabled = ImageUtils.getScaled(activity, 1, ImageUtils.loadFromFile(activity, "reports", disabledFilename));
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, new BitmapDrawable(activity.getResources(), active));
        states.addState(new int[]{}, new BitmapDrawable(activity.getResources(), disabled));
        return states;
    }

    public static StateListDrawable getInventoryStateListDrawable(Activity activity, String activeFilename, String disabledFilename) {
        Bitmap active = ImageUtils.getScaled(activity, 1, ImageUtils.loadFromFile(activity, "inventory", activeFilename));
        Bitmap disabled = ImageUtils.getScaled(activity, 1, ImageUtils.loadFromFile(activity, "inventory", disabledFilename));
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, new BitmapDrawable(activity.getResources(), active));
        states.addState(new int[]{}, new BitmapDrawable(activity.getResources(), disabled));
        return states;
    }

    public static Bitmap adjustedContrast(Bitmap src, double value) {
        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        Canvas c = new Canvas();
        c.setBitmap(bmOut);

        c.drawBitmap(src, 0, 0, new Paint(Color.BLACK));

        int a, r, g, b;
        int pixel;
        double contrast = Math.pow((100 + value) / 100, 2);

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                pixel = src.getPixel(x, y);
                a = Color.alpha(pixel);
                r = Color.red(pixel);
                r = (int) (((((r / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (r < 0) {
                    r = 0;
                } else if (r > 255) {
                    r = 255;
                }

                g = Color.green(pixel);
                g = (int) (((((g / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (g < 0) {
                    g = 0;
                } else if (g > 255) {
                    g = 255;
                }

                b = Color.blue(pixel);
                b = (int) (((((b / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (b < 0) {
                    b = 0;
                } else if (b > 255) {
                    b = 255;
                }

                bmOut.setPixel(x, y, Color.argb(a, r, g, b));
            }
        }
        return bmOut;
    }

    public static float dpToPx(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static Drawable getStatusBackground(Context context, int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setStroke(0, Color.WHITE);
        float tenDp = ImageUtils.dpToPx(context, 15);
        gd.setCornerRadii(new float[]{tenDp, tenDp,
                tenDp, tenDp, tenDp, tenDp, tenDp, tenDp});
        return gd;
    }

    public static boolean shouldDownloadRetinaIcon(Context context) {
        if (context == null) context = ZupApplication.getContext();
        return context.getResources().getDisplayMetrics().density >= 1.5f;
    }

    public static String encodeBase64(String path) {
        try {
            File file = new File(path);
            FileInputStream imageInFile = new FileInputStream(file);
            byte imageData[] = new byte[(int) file.length()];
            imageInFile.read(imageData);
            imageInFile.close();

            return Base64.encodeToString(imageData, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("MakeMe", e.getMessage() != null ? e.getMessage() : "NullPointerException", e);
            return null;
        }
    }

    public static Bitmap loadDefaultIcon(Activity activity, boolean active, float scale) {
        float divider = ImageUtils.shouldDownloadRetinaIcon(activity) ? 2f : 1f;
        return getScaled(activity, scale / divider, ImageUtils.loadFromFile(activity, "default", active ?
                Defaults.getDefaultActiveIcon() : Defaults.getDefaultInactiveIcon()));
    }

    public static Bitmap loadDefaultMarker(Activity activity, float scale) {
        float divider = ImageUtils.shouldDownloadRetinaIcon(activity) ? 2f : 1f;
        return getScaled(activity, scale / divider, ImageUtils.loadFromFile(activity, "default", Defaults.getDefaultMarker()));
    }
}
