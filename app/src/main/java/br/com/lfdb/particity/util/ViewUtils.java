package br.com.lfdb.particity.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class ViewUtils {

    public static void hideKeyboard(Context context, TextView v) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void hideKeyboard(Context context, IBinder windowToken) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static boolean isMdpiOrLdpi(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM || metrics.densityDpi == DisplayMetrics.DENSITY_LOW;
    }

    public static Bitmap getBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);
        return bitmap;
    }
}
