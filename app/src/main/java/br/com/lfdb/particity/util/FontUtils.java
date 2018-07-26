package br.com.lfdb.particity.util;

import android.content.Context;
import android.graphics.Typeface;

public class FontUtils {

	public static Typeface getLight(Context context) {
		return Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Light.ttf");
	}
	
	public static Typeface getExtraBold(Context context) {
		return Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-ExtraBold.ttf");
	}
	
	public static Typeface getBold(Context context) {
		return Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Bold.ttf");
	}
	
	public static Typeface getRegular(Context context) {
		return Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Regular.ttf");
	}
	
	public static Typeface getSemibold(Context context) {
		return Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Semibold.ttf");
	}
}
