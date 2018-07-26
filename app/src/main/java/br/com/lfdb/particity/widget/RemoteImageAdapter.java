package br.com.lfdb.particity.widget;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import br.com.lfdb.particity.fragment.RemoteImageFragment;

public class RemoteImageAdapter extends FragmentPagerAdapter {

    protected List<String> items;

    public RemoteImageAdapter(FragmentManager fm, List<String> items) {
        super(fm);
        this.items = items;
    }

    @Override
    public Fragment getItem(int position) {
        return RemoteImageFragment.newInstance(items.get(position));
    }

    @Override
    public int getCount() {
        return items.size();
    }
}