package br.com.lfdb.particity.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import br.com.lfdb.particity.api.model.InventoryCategoriesResponse;
import br.com.lfdb.particity.api.model.InventoryCategory;
import br.com.lfdb.particity.api.model.ReportCategoriesResponse;
import br.com.lfdb.particity.api.model.ReportCategory;
import br.com.lfdb.particity.api.model.ReportCategoryStatus;
import br.com.lfdb.particity.base.Defaults;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.core.ConstantesBase;
import br.com.lfdb.particity.util.FileUtils;
import br.com.lfdb.particity.util.ImageUtils;
import com.google.common.io.CharStreams;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONObject;

public class Updater {

    public void update(Context context) throws Exception {
        long start = System.currentTimeMillis();
        try {
            Request request =
                    new Request.Builder().addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                            .url(Constantes.REST_URL
                                    + "/reports/categories"
                                    + ConstantesBase.getCategoriasRelatoQuery(context)
                                    + "&display_type=full")
                            .build();
            Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
            saveCategories(context, response.body().string(), "reports");
            setupStatuses(context);

            request = new Request.Builder().addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                    .url(Constantes.REST_URL
                            + "/inventory/categories"
                            + ConstantesBase.getCategoriasInventarioQuery(context))
                    .build();
            response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
            saveCategories(context, response.body().string(), "inventory");

            downloadDefaultImages(context);

            updateFeatureFlags(context);
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
            throw e;
        }
        Log.d("UPDATE", "Update took " + (System.currentTimeMillis() - start) + " milliseconds");
    }

    private void downloadDefaultImages(Context context) throws Exception {
        JSONObject json = new JSONObject(CharStreams.toString(new InputStreamReader(
                context.getResources().getAssets().open("defaults.json")))).getJSONObject("category");
        ReportCategory category = ConstantesBase.GSON.fromJson(json.toString(), ReportCategory.class);
        String density = ImageUtils.shouldDownloadRetinaIcon(context) ? "retina" : "default";
        Defaults.setup(category, density.equals("retina"));
        downloadImages(context, "default", density, category);
    }

    private void setupStatuses(Context context) throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String raw = prefs.getString("reports", "{\"categories\":[]}");
        List<ReportCategory> categories =
                ConstantesBase.GSON.fromJson(raw, ReportCategoriesResponse.class).getCategories();
        Set<ReportCategoryStatus> statuses = new HashSet<>();
        for (ReportCategory category : categories) {
            statuses.addAll(category.getStatuses());

            if (!category.getSubcategories().isEmpty()) {
                for (ReportCategory subcategory : category.getSubcategories()) {
                    statuses.addAll(subcategory.getStatuses());
                }
            }
        }
        prefs.edit()
                .putString("statuses", ConstantesBase.GSON.toJson(new ArrayList<>(statuses)))
                .apply();
    }

    private void updateFeatureFlags(Context context) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT).url(Constantes.REST_URL + "/feature_flags").build();
        Response response = client.newCall(request).execute();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("features", response.body().string()).apply();
    }

    private void saveCategories(Context context, String json, String type) throws Exception {
        String density = ImageUtils.shouldDownloadRetinaIcon(context) ? "retina" : "default";
        if (type.equals("reports")) {
            ReportCategoriesResponse response =
                    ConstantesBase.GSON.fromJson(json, ReportCategoriesResponse.class);
            if (response == null || response.getCategories() == null) return;
            for (ReportCategory category : response.getCategories()) {
                downloadImages(context, type, density, category);
                for (ReportCategory sub : category.getSubcategories())
                    downloadImages(context, type, density, sub);
            }
        } else {
            InventoryCategoriesResponse response =
                    ConstantesBase.GSON.fromJson(json, InventoryCategoriesResponse.class);
            if (response == null || response.getCategories() == null) return;
            for (InventoryCategory category : response.getCategories()) {
                if (density.equals("retina")) {
                    if (category.getPin() != null) {
                        FileUtils.downloadImage(context, type, category.getPin().getRetina().getMobile());
                    }
                    FileUtils.downloadImage(context, type, category.getMarker().getRetina().getMobile());
                    FileUtils.downloadImage(context, type,
                            category.getIcon().getRetina().getMobile().getActive());
                    FileUtils.downloadImage(context, type,
                            category.getIcon().getRetina().getMobile().getDisabled());
                } else {
                    if (category.getPin() != null) {
                        FileUtils.downloadImage(context, type, category.getPin().getCommon().getMobile());
                    }
                    FileUtils.downloadImage(context, type, category.getMarker().getCommon().getMobile());
                    FileUtils.downloadImage(context, type,
                            category.getIcon().getCommon().getMobile().getActive());
                    FileUtils.downloadImage(context, type,
                            category.getIcon().getCommon().getMobile().getDisabled());
                }
            }
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(type, json).apply();
    }

    private void downloadImages(Context context, String type, String density, ReportCategory category)
            throws Exception {
        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicBoolean error = new AtomicBoolean(false);
        if (density.equals("retina")) {
            new Thread(() -> {
                try {
                    FileUtils.downloadImage(context, type, category.getMarker().getRetina().getMobile());
                } catch (Exception e) {
                    Log.e("Error", "Failed to download", e);
                    error.set(true);
                }
                counter.addAndGet(1);
            }).start();
            new Thread(() -> {
                try {
                    FileUtils.downloadImage(context, type,
                            category.getIcon().getRetina().getMobile().getActive());
                } catch (Exception e) {
                    Log.e("Error", "Failed to download", e);
                    error.set(true);
                }
                counter.addAndGet(1);
            }).start();
            new Thread(() -> {
                try {
                    FileUtils.downloadImage(context, type,
                            category.getIcon().getRetina().getMobile().getDisabled());
                } catch (Exception e) {
                    Log.e("Error", "Failed to download", e);
                    error.set(true);
                }
                counter.addAndGet(1);
            }).start();
        } else {
            new Thread(() -> {
                try {
                    FileUtils.downloadImage(context, type, category.getMarker().getCommon().getMobile());
                } catch (Exception e) {
                    Log.e("Error", "Failed to download", e);
                    error.set(true);
                }
                counter.addAndGet(1);
            }).start();
            new Thread(() -> {
                try {
                    FileUtils.downloadImage(context, type,
                            category.getIcon().getCommon().getMobile().getActive());
                } catch (Exception e) {
                    Log.e("Error", "Failed to download", e);
                    error.set(true);
                }
                counter.addAndGet(1);
            }).start();
            new Thread(() -> {
                try {
                    FileUtils.downloadImage(context, type,
                            category.getIcon().getCommon().getMobile().getDisabled());
                } catch (Exception e) {
                    Log.e("Error", "Failed to download", e);
                    error.set(true);
                }
                counter.addAndGet(1);
            }).start();
        }
        while (counter.get() != 3 && !error.get()) ;
    }
}
