package br.com.lfdb.particity.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;
import br.com.lfdb.particity.util.TypefaceHelper;

public class CustomFontTextView extends TextView {

  String font = "fonts/OpenSans-Regular.ttf";

  public CustomFontTextView(Context context) {
    super(context);
    init();
  }

  public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    getFont();
    init();
  }

  public CustomFontTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    getFont();
    init();
  }

  public void getFont() {
    font = (String) getTag();
    if (font == null) {
      font = "fonts/OpenSans-Regular.ttf";
    }
  }

  public void init() {
    if (!isInEditMode()) {
      Typeface tf = TypefaceHelper.get(getContext(), "fonts/" + font + ".ttf");
      setTypeface(tf);
    }
  }
}