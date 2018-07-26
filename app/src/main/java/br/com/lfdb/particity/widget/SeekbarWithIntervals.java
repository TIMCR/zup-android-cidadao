package br.com.lfdb.particity.widget;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import br.com.lfdb.particity.R;
import br.com.lfdb.particity.util.FontUtils;

public class SeekbarWithIntervals extends LinearLayout {
	private RelativeLayout relativeLayout = null;
	private SeekBar seekbar = null;
	private static int idGenerator = 1;

	private int widthMeasureSpec = 0;
	private int heightMeasureSpec = 0;

	public SeekbarWithIntervals(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		getActivity().getLayoutInflater().inflate(R.layout.seekbar_with_intervals, this);
		
		((TextView) findViewById(R.id.textView1)).setTypeface(FontUtils.getRegular(getContext()));
		((TextView) findViewById(R.id.textView2)).setTypeface(FontUtils.getRegular(getContext()));
		((TextView) findViewById(R.id.textView3)).setTypeface(FontUtils.getRegular(getContext()));
		((TextView) findViewById(R.id.textView4)).setTypeface(FontUtils.getRegular(getContext()));
		
		setIntervals(Arrays.asList("", "", "", ""));
	}

	private Activity getActivity() {
		return (Activity) getContext();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		if (changed) {
			alignIntervals();

			relativeLayout.measure(widthMeasureSpec, heightMeasureSpec);
			relativeLayout.layout(relativeLayout.getLeft(), relativeLayout.getTop(), relativeLayout.getRight(), relativeLayout.getBottom());
		}
	}

	private void alignIntervals() {
		int widthOfSeekbarThumb = getSeekbarThumbWidth();
		int thumbOffset = widthOfSeekbarThumb / 2;

		int widthOfSeekbar = getSeekbar().getWidth();
		int firstIntervalWidth = getRelativeLayout().getChildAt(0).getWidth();
		int remainingPaddableWidth = widthOfSeekbar - firstIntervalWidth - widthOfSeekbarThumb;

		int numberOfIntervals = getSeekbar().getMax();
		int maximumWidthOfEachInterval = remainingPaddableWidth / numberOfIntervals;

		alignFirstInterval(thumbOffset);
		alignIntervalsInBetween(maximumWidthOfEachInterval);
		alignLastInterval(thumbOffset, maximumWidthOfEachInterval);
	}

	private int getSeekbarThumbWidth() {
		return getResources().getDimensionPixelOffset(R.dimen.seekbar_thumb_width);
	}

	private void alignFirstInterval(int offset) {
		TextView firstInterval = (TextView) getRelativeLayout().getChildAt(0);
		firstInterval.setPadding(offset, 0, 0, 0);
	}

	private void alignIntervalsInBetween(int maximumWidthOfEachInterval) {
		int widthOfPreviousIntervalsText = 0;

		for (int index = 1; index < (getRelativeLayout().getChildCount() - 1); index++) {
			TextView textViewInterval = (TextView) getRelativeLayout().getChildAt(index);
			int widthOfText = textViewInterval.getWidth();

			int leftPadding = Math.round(maximumWidthOfEachInterval - (widthOfText / 2) - (widthOfPreviousIntervalsText / 2));
			textViewInterval.setPadding(leftPadding, 0, 0, 0);

			widthOfPreviousIntervalsText = widthOfText;
		}
	}

	private void alignLastInterval(int offset, int maximumWidthOfEachInterval) {
		int lastIndex = getRelativeLayout().getChildCount() - 1;

		TextView lastInterval = (TextView) getRelativeLayout().getChildAt(lastIndex);
		int widthOfText = lastInterval.getWidth();

		int leftPadding = Math.round(maximumWidthOfEachInterval - widthOfText - offset);
		lastInterval.setPadding(leftPadding, 0, 0, 0);
	}

	protected synchronized void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		this.widthMeasureSpec = widthMeasureSpec;
		this.heightMeasureSpec = heightMeasureSpec;

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public int getProgress() {
		return getSeekbar().getProgress();
	}

	public void setProgress(int progress) {
		getSeekbar().setProgress(progress);
	}

	public void setIntervals(List<String> intervals) {
		displayIntervals(intervals);
		getSeekbar().setMax(intervals.size() - 1);
	}

	private void displayIntervals(List<String> intervals) {
		int idOfPreviousInterval = 0;

		if (getRelativeLayout().getChildCount() == 0) {
			for (String interval : intervals) {
				TextView textViewInterval = createInterval(interval);
				alignTextViewToRightOfPreviousInterval(textViewInterval, idOfPreviousInterval);

				idOfPreviousInterval = textViewInterval.getId();

				getRelativeLayout().addView(textViewInterval);
			}
		}
	}

	private TextView createInterval(String interval) {
		View textBoxView = (View) LayoutInflater.from(getContext()).inflate(R.layout.seekbar_with_intervals_labels, null);

		TextView textView = (TextView) textBoxView.findViewById(R.id.textViewInterval);

		textView.setId(++idGenerator);
		textView.setText(interval);
		textView.setTypeface(FontUtils.getRegular(getContext()));
		
		return textView;
	}

	private void alignTextViewToRightOfPreviousInterval(TextView textView, int idOfPreviousInterval) {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		if (idOfPreviousInterval > 0) {
			params.addRule(RelativeLayout.RIGHT_OF, idOfPreviousInterval);
		}

		textView.setLayoutParams(params);
	}

	public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
		getSeekbar().setOnSeekBarChangeListener(onSeekBarChangeListener);
	}

	private RelativeLayout getRelativeLayout() {
		if (relativeLayout == null) {
			relativeLayout = (RelativeLayout) findViewById(R.id.intervals);
		}

		return relativeLayout;
	}

	private SeekBar getSeekbar() {
		if (seekbar == null) {
			seekbar = (SeekBar) findViewById(R.id.seekbar);
		}

		return seekbar;
	}
}