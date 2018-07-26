package br.com.lfdb.particity.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class MapRelativeLayout extends RelativeLayout {
	private GestureDetector gestureDetector;

	public MapRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		gestureDetector = new GestureDetector(context, new GestureListener());
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		gestureDetector.onTouchEvent(ev);
		return false;
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			// do whatever you want here
			return false;
		}
	}
}
