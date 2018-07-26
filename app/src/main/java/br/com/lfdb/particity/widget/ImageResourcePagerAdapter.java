package br.com.lfdb.particity.widget;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import br.com.lfdb.particity.R;
import br.com.lfdb.particity.fragment.ImageViewResourceFragment;

import com.viewpagerindicator.IconPagerAdapter;

public class ImageResourcePagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

	protected List<Integer> imagens = new ArrayList<Integer>();

	public ImageResourcePagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public ImageResourcePagerAdapter(FragmentManager fm, List<Integer> images) {
		super(fm);
		imagens = images;
	}

	@Override
	public Fragment getItem(int position) {
		return ImageViewResourceFragment.newInstance(imagens.get(position));
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