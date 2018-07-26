package br.com.lfdb.particity.track;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import br.com.lfdb.particity.R;

public class GoogleAnalyticsTracker {

    private Tracker t;

    private static GoogleAnalyticsTracker INSTANCE;

    public GoogleAnalyticsTracker(final Context context) {
        new Thread(() -> {
            final GoogleAnalytics instance = GoogleAnalytics.getInstance(context);
            instance.getLogger().setLogLevel(Logger.LogLevel.WARNING);
            instance.setLocalDispatchPeriod(5);
            t = instance.newTracker(R.xml.analytics);
            t.enableExceptionReporting(true);
            t.enableAdvertisingIdCollection(false);
        }).start();
    }

    public void track(String screen) {
        t.setScreenName(screen);
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    public static void init(Context context) {
        INSTANCE = new GoogleAnalyticsTracker(context);
    }

    public static GoogleAnalyticsTracker getInstance() {
        return INSTANCE;
    }
}
