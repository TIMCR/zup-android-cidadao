package br.com.lfdb.particity.core;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.DateTime;

import br.com.lfdb.particity.api.converter.DateTimeConverter;
import br.com.lfdb.particity.util.ImageUtils;

public class ConstantesBase {

    public static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(DateTime.class, new DateTimeConverter())
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public static String getItemInventarioQuery(Context context) {
        return new StringBuilder("?return_fields=")
                .append("id,")
                .append("position.latitude,")
                .append("position.longitude,")
                .append("inventory_category_id")
                .toString();
    }

    public static String getItemRelatoQuery(Context context) {
        String imageSize = ImageUtils.shouldDownloadRetinaIcon(context) ? "high" : "low";
        return new StringBuilder("?return_fields=")
                .append("id,")
                .append("description,")
                .append("protocol,")
                .append("address,")
                .append("number,")
                .append("postal_code,")
                .append("district,")
                .append("created_at,")
                .append("category.id,")
                .append("position.latitude,")
                .append("position.longitude,")
                .append("inventory_item_id,")
                .append("status_id,")
                .append("reference,")
                .append("images,")
                .append("user.id")
                .toString();
    }

    public static String getCategoriasInventarioQuery(Context context) {
        String imageSize = ImageUtils.shouldDownloadRetinaIcon(context) ? "retina" : "default";
        return new StringBuilder("?return_fields=")
                .append("plot_format,")
                .append("icon,")
                .append("id,")
                .append("color,")
                .append("marker,")
                .append("pin,")
                .append("title")
                .toString();
    }

    public static String getCategoriasRelatoQuery(Context context) {
        String imageSize = ImageUtils.shouldDownloadRetinaIcon(context) ? "retina" : "default";
        return new StringBuilder("?return_fields=")
                .append("icon,")
                .append("marker,")
                .append("color,")
                .append("id,")
                .append("title,")
                .append("resolution_time,")
                .append("user_response_time,")
                .append("confidential,")
                .append("allows_arbitrary_position,")
                .append("resolution_time_enabled,")
                .append("private_resolution_time,")
                .append("statuses.id,")
                .append("statuses.title,")
                .append("statuses.color,")
                .append("inventory_categories.id,")
                        // subcategories
                .append("subcategories.").append("icon,")
                .append("subcategories.").append("marker,")
                .append("subcategories.").append("id,")
                .append("subcategories.").append("title,")
                .append("subcategories.").append("color,")
                .append("subcategories.").append("resolution_time,")
                .append("subcategories.").append("user_response_time,")
                .append("subcategories.").append("confidential,")
                .append("subcategories.").append("allows_arbitrary_position,")
                .append("subcategories.").append("resolution_time_enabled,")
                .append("subcategories.").append("private_resolution_time,")
                .append("subcategories.").append("statuses.id,")
                .append("subcategories.").append("statuses.title,")
                .append("subcategories.").append("statuses.color,")
                .append("subcategories.").append("inventory_categories.id")
                .toString();
    }
}
