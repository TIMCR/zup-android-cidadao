package br.com.lfdb.particity.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class FeatureService {

    private static final FeatureService INSTANCE = new FeatureService();
    private static Context staticContext;

    public static FeatureService getInstance(Context context) {
        staticContext = context;
        return INSTANCE;
    }

    private JSONArray getPrefs() {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(staticContext);
            return new JSONObject(prefs.getString("features", "")).getJSONArray("flags");
        } catch (Exception e) {
            Log.e("ZUP", "Falha ao carregar configurações", e);
            return null;
        }
    }

    private boolean checkFeature(String featureName) {
        try {
            JSONArray json = getPrefs();
            for (int i = 0; i < json.length(); i++) {
                if (json.getJSONObject(i).getString("name").equals(featureName)) {
                    return json.getJSONObject(i).getString("status").equals("enabled");
                }
            }
        } catch (Exception e) {
            Log.e("ZUP", "Falha ao carregar configurações", e);
        }

        return false;
    }

    public boolean isExploreEnabled() {
        return checkFeature("explore");
    }

    public boolean isCreateReportsClients() {
        return checkFeature("create_report_clients");
    }

    public boolean isStatsEnabled() {
        return checkFeature("stats");
    }

    public boolean isSocialNetworkGPlusEnabled() {
        return checkFeature("social_networks_gplus");
    }

    public boolean isSocialNetworkTwitterEnabled() {
        return checkFeature("social_networks_twitter");
    }

    public boolean isShowResolutionTimeToClientsEnabled() {
        return checkFeature("show_resolution_time_to_clients");
    }

    public boolean isAllowPhotoAlbumAccessEnabled() {
        return checkFeature("allow_photo_album_access");
    }

    public boolean isReportsEnabled() {
        return checkFeature("reports");
    }

    public boolean isCasesEnabled() {
        return checkFeature("cases");
    }

    public boolean isCreateReportPanelEnabled() {
        return checkFeature("create_report_panel");
    }

    public boolean isInventoryEnabled() {
        return checkFeature("inventory");
    }

    public boolean isSocialNetworkFacebookEnabled() {
        return checkFeature("social_networks_facebook");
    }

    public boolean isAnySocialEnabled() {
        return checkFeature("social_networks_facebook") || checkFeature("social_networks_gplus") ||
                checkFeature("social_networks_twitter");
    }
}
