package br.com.lfdb.particity.fragment;

import br.com.lfdb.particity.R;
import br.com.lfdb.particity.core.Constantes;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

import com.squareup.picasso.Picasso;

public class ImageViewFragment extends Fragment {

	private static final String KEY_CONTENT = "TestFragment:Content";

	public static ImageViewFragment newInstance(String content) {
		ImageViewFragment fragment = new ImageViewFragment();

		fragment.mContent = content;

		return fragment;
	}

	private String mContent = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
			mContent = savedInstanceState.getString(KEY_CONTENT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ImageView imageView = new ImageView(getActivity());
		//imageView.setImageBitmap(ImageUtils.loadFromFile(mContent));
        if (!mContent.startsWith("http")) mContent = Constantes.REST_URL + (mContent.startsWith("/") ? mContent : ("/" + mContent));
        Picasso.with(getActivity()).load(Uri.parse(mContent)).placeholder(R.drawable.loading).into(imageView);
        imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		return imageView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_CONTENT, mContent);
	}
}
