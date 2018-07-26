package br.com.lfdb.particity.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

public class ImageViewResourceFragment extends Fragment {

	private static final String KEY_CONTENT = "TestResourceFragment:Content";

	public static ImageViewResourceFragment newInstance(int content) {
		ImageViewResourceFragment fragment = new ImageViewResourceFragment();

		fragment.mContent = content;

		return fragment;
	}

	private int mContent = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
			mContent = savedInstanceState.getInt(KEY_CONTENT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ImageView imageView = new ImageView(getActivity());
		imageView.setImageResource(mContent);
		imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		return imageView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_CONTENT, mContent);
	}
}
