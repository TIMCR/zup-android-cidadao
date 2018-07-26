package br.com.lfdb.particity.widget;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import br.com.lfdb.particity.R;
import br.com.lfdb.particity.fragment.ImageViewFragment;

import com.viewpagerindicator.IconPagerAdapter;

public class ImagePagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

	protected List<String> imagens = new ArrayList<String>();

	public ImagePagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public ImagePagerAdapter(FragmentManager fm, List<String> images) {
		super(fm);
		imagens = images;
	}

	@Override
	public Fragment getItem(int position) {
		return ImageViewFragment.newInstance(imagens.get(position));
	}

	@Override
	public int getCount() {
		return imagens.size();
	}

	@Override
	public int getIconResId(int index) {
		return R.drawable.selector;
	}
}