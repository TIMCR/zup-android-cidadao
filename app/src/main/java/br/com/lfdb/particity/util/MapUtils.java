package br.com.lfdb.particity.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.annotation.ColorRes;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.lfdb.particity.R;
import br.com.lfdb.particity.ZupApplication;

public class MapUtils {

    public static Bitmap createMarker(Context context, int color, int number) {
        if (context == null) context = ZupApplication.getContext();
        ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.cluster_marker, null);
        GradientDrawable bgDrawable = (GradientDrawable) view.getBackground().mutate();
        bgDrawable.setColor(color);
        bgDrawable.invalidateSelf();
        ((TextView) view.getChildAt(0)).setText(String.valueOf(number));
        return ViewUtils.getBitmapFromView(view);
    }
}
