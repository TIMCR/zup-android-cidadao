package br.com.ntxdev.zup.util;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;

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
		c.drawBitmap(srcBitmap, 0, 0, paint);
		return bmpGrayscale;
	}

	public static Bitmap loadFromFile(String file) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeFile(new File(FileUtils.getImagesFolder() + File.separator + file).toString(), options);
	}

	public static Bitmap getScaled(Activity activity, String filename) {
		return getScaled(activity, ImageUtils.loadFromFile(filename));
	}
	
	public static Bitmap getScaled(Activity activity, Bitmap bitmapOrg) {
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();

		float scaleWidth = metrics.scaledDensity;
		float scaleHeight = metrics.scaledDensity;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		return Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
	}

	public static StateListDrawable getStateListDrawable(Activity activity, String filename) {
		Bitmap original = ImageUtils.getScaled(activity, ImageUtils.loadFromFile(filename));
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[] { android.R.attr.state_pressed }, new BitmapDrawable(activity.getResources(), original));
		states.addState(new int[] {}, new BitmapDrawable(activity.getResources(), ImageUtils.toGrayscale(original)));
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
}