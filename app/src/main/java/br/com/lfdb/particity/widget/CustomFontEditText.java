package br.com.lfdb.particity.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;
import br.com.lfdb.particity.util.TypefaceHelper;

public class CustomFontEditText extends EditText {

  String font = "fonts/OpenSans-Regular.ttf";

  public CustomFontEditText(Context context) {
    super(context);
    init();
  }

  public CustomFontEditText(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    getFont();
    init();
  }

  public CustomFontEditText(Context context, AttributeSet attrs) {
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