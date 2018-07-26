package br.com.lfdb.particity.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import br.com.lfdb.particity.domain.CategoriaInventario;
import br.com.lfdb.particity.util.ImageUtils;

public class CategoriaInventarioService {

    public CategoriaInventario getById(Context context, long id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String raw = prefs.getString("inventory", "");
        if (raw.isEmpty()) {
            return null;
        }

        try {
            JSONArray array = new JSONObject(raw).getJSONArray("categories");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.getLong("id") == id) {
                    return extract(context, obj);
                }
            }
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
        }

        return null;
    }

    public List<CategoriaInventario> getCategorias(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String raw = prefs.getString("inventory", "");
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<CategoriaInventario> categorias = new ArrayList<>();
            JSONArray array = new JSONObject(raw).getJSONArray("categories");
            for (int i = 0; i < array.length(); i++) {
                CategoriaInventario category = extract(context, array.getJSONObject(i));
                if (category.isPrivada()) continue;
                categorias.add(category);
            }
            return categorias;
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private CategoriaInventario extract(Context context, JSONObject obj) throws Exception {
        String density = ImageUtils.shouldDownloadRetinaIcon(context) ? "retina" : "default";

        CategoriaInventario categoria = new CategoriaInventario();
        categoria.setShowMarker(obj.getString("plot_format").equals("marker"));
        JSONObject icon = obj.getJSONObject("icon").getJSONObject(density).getJSONObject("mobile");
        String[] file = icon.getString("active").split("/");
        categoria.setIconeAtivo(file[file.length - 1]);
        file = icon.getString("disabled").split("/");
        categoria.setIconeInativo(file[file.length - 1]);
        categoria.setId(obj.getLong("id"));
        categoria.setCor(obj.getString("color"));
        categoria.setPrivada(obj.optBoolean("private", false));
        file = obj.getJSONObject("marker").getJSONObject(density).getString("mobile").split("/");
        categoria.setMarcador(file[file.length - 1]);
        if (obj.has("pin")) {
            file = obj.getJSONObject("pin").getJSONObject(density).getString("mobile").split("/");
            categoria.setPin(file[file.length - 1]);
        }
        categoria.setNome(obj.getString("title"));
        return categoria;
    }
}
