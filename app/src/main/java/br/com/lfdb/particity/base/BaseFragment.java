package br.com.lfdb.particity.base;

import android.support.v4.app.Fragment;

import br.com.lfdb.particity.track.GoogleAnalyticsTracker;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalyticsTracker.getInstance().track(getScreenName());
    }

    protected abstract String getScreenName();
}
