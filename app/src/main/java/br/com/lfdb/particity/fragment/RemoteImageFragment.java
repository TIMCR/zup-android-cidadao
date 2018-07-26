package br.com.lfdb.particity.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import br.com.lfdb.particity.R;
import butterknife.ButterKnife;
import com.squareup.picasso.Picasso;

public class RemoteImageFragment extends Fragment {

    private static final String KEY_CONTENT = "TestResourceFragment:Content";

    public static RemoteImageFragment newInstance(String content) {
        RemoteImageFragment fragment = new RemoteImageFragment();

        fragment.mContent = content;

        return fragment;
    }

    private String mContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remote_image, container, false);

        ImageView imageView = ButterKnife.findById(view, R.id.background);
        Picasso.with(getActivity()).load(mContent).into(imageView);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }
}
