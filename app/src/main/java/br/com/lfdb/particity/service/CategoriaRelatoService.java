package br.com.lfdb.particity.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.lfdb.particity.ZupApplication;
import br.com.lfdb.particity.api.model.ReportCategoryStatus;
import br.com.lfdb.particity.core.ConstantesBase;
import br.com.lfdb.particity.domain.CategoriaInventario;
import br.com.lfdb.particity.domain.CategoriaRelato;
import br.com.lfdb.particity.util.ImageUtils;

public class CategoriaRelatoService {

    public List<CategoriaRelato.Status> getStatus(Context context, long categoriaId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String raw = prefs.getString("reports", "");
        List<CategoriaRelato.Status> status = new ArrayList<>();

        if (raw.isEmpty()) {
            return status;
        }

        try {
            JSONArray array = new JSONObject(raw).getJSONArray("categories");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.getLong("id") == categoriaId) {
                    JSONArray statuses = obj.getJSONArray("statuses");
                    for (int j = 0; j < statuses.length(); j++) {
                        status.add(new CategoriaRelato.Status(statuses.getJSONObject(j).getLong("id"),
                                statuses.getJSONObject(j).getString("title"),
                                statuses.getJSONObject(j).getString("color")));
                    }

                    for (int j = 0; j < obj.getJSONArray("subcategories").length(); j++) {
                        JSONObject sub = obj.getJSONArray("subcategories").getJSONObject(j);
                        JSONArray s = sub.getJSONArray("statuses");
                        for (int k = 0; k < s.length(); k++) {
                            status.add(new CategoriaRelato.Status(s.getJSONObject(k).getLong("id"),
                                    s.getJSONObject(k).getString("title"),
                                    s.getJSONObject(k).getString("color")));
                        }
                    }
                } else {
                    for (int j = 0; j < obj.getJSONArray("subcategories").length(); j++) {
                        JSONObject sub = obj.getJSONArray("subcategories").getJSONObject(j);
                        if (sub.getLong("id") == categoriaId) {
                            JSONArray s = sub.getJSONArray("statuses");
                            for (int k = 0; k < s.length(); k++) {
                                status.add(new CategoriaRelato.Status(s.getJSONObject(k).getLong("id"),
                                        s.getJSONObject(k).getString("title"),
                                        s.getJSONObject(k).getString("color")));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
        }

        return status;
    }

    public CategoriaRelato.Status getStatusById(Context context, long categoriaId, long statusId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String raw = prefs.getString("statuses", "[]");
        List<ReportCategoryStatus> statuses = ConstantesBase.GSON.fromJson(raw, new TypeToken<List<ReportCategoryStatus>>() {
        }.getType());
        for (ReportCategoryStatus status : statuses) {
            if (status.getId() == statusId) return status.compat();
        }
        return null;
    }

    public CategoriaRelato getById(Context context, long id) {
        if (context == null) context = ZupApplication.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String raw = prefs.getString("reports", "");
        if (raw.isEmpty()) {
            return null;
        }

        try {
            JSONArray array = new JSONObject(raw).getJSONArray("categories");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.getLong("id") == id) {
                    CategoriaRelato categoria = extrairDoJson(context, obj);

                    for (int j = 0; j < obj.getJSONArray("subcategories").length(); j++) {
                        CategoriaRelato filha = extrairDoJson(context, obj.getJSONArray("subcategories").getJSONObject(j));

                        filha.setCategoriaMae(categoria);

                        categoria.addSubcategoria(filha);
                    }

                    return categoria;
                } else {
                    for (int j = 0; j < obj.getJSONArray("subcategories").length(); j++) {
                        JSONObject subJson = obj.getJSONArray("subcategories").getJSONObject(j);

                        if (subJson.getLong("id") == id) {
                            CategoriaRelato filha = extrairDoJson(context, subJson);

                            CategoriaRelato categoria = extrairDoJson(context, obj);
                            for (int k = 0; k < obj.getJSONArray("subcategories").length(); k++) {
                                CategoriaRelato f = extrairDoJson(context, obj.getJSONArray("subcategories").getJSONObject(k));
                                f.setCategoriaMae(categoria);
                                categoria.addSubcategoria(f);
                            }
                            filha.setCategoriaMae(categoria);
                            return filha;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
        }

        Log.e("ZUP", "Could not find category with id " + id);
        return null;
    }

    public List<CategoriaRelato> getCategorias(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String raw = prefs.getString("reports", "");
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<CategoriaRelato> categorias = new ArrayList<>();
            JSONArray array = new JSONObject(raw).getJSONArray("categories");
            for (int i = 0; i < array.length(); i++) {
                CategoriaRelato categoria = extrairDoJson(context, array.getJSONObject(i));
                if (categoria.isPrivada()) continue;

                for (int j = 0; j < array.getJSONObject(i).getJSONArray("subcategories").length(); j++) {
                    CategoriaRelato filha = extrairDoJson(context, array.getJSONObject(i).getJSONArray("subcategories").getJSONObject(j));
                    if (filha.isPrivada()) continue;
                    filha.setCategoriaMae(categoria);
                    categoria.addSubcategoria(filha);
                }

                categorias.add(categoria);
            }
            return categorias;
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private ArrayList<CategoriaInventario> extrairCategoriasInventario(Context context, JSONArray array) throws JSONException {
        ArrayList<CategoriaInventario> categorias = new ArrayList<>();

        CategoriaInventarioService service = new CategoriaInventarioService();
        for (int i = 0; i < array.length(); i++) {
            categorias.add(service.getById(context, array.getJSONObject(i).getLong("id")));
        }

        return categorias;
    }

    private ArrayList<CategoriaRelato.Status> extrairStatus(JSONArray lista) throws JSONException {
        ArrayList<CategoriaRelato.Status> status = new ArrayList<>();
        for (int i = 0; i < lista.length(); i++) {
            JSONObject obj = lista.getJSONObject(i);
            CategoriaRelato.Status s = new CategoriaRelato.Status();
            s.setCor(Color.parseColor(obj.getString("color")));
            s.setId(obj.getLong("id"));
            s.setNome(obj.getString("title"));
            status.add(s);
        }
        return status;
    }

    private CategoriaRelato extrairDoJson(Context context, JSONObject json) throws JSONException {
        String density = ImageUtils.shouldDownloadRetinaIcon(context) ? "retina" : "default";
        CategoriaRelato categoria = new CategoriaRelato();
        JSONObject icon = json.getJSONObject("icon").getJSONObject(density).getJSONObject("mobile");
        String[] file = icon.getString("active").split("/");
        categoria.setIconeAtivo(file[file.length - 1]);
        file = icon.getString("disabled").split("/");
        categoria.setIconeInativo(file[file.length - 1]);
        categoria.setId(json.getLong("id"));
        categoria.setPrivada(json.optBoolean("private", false));
        categoria.setCor(json.getString("color"));
        categoria.setPosicaoLivre(json.optBoolean("allows_arbitrary_position", false));
        categoria.setTempoResolucao(json.optLong("resolution_time"));
        categoria.setTempoResposta(json.optLong("user_response_time"));
        categoria.setConfidencial(json.optBoolean("confidential", false));
        categoria.setTempoResolucaoAtivado(json.getBoolean("resolution_time_enabled"));
        categoria.setTempoResolucaoPrivado(json.getBoolean("private_resolution_time"));
        file = json.getJSONObject("marker").getJSONObject(density).getString("mobile").split("/");
        categoria.setMarcador(file[file.length - 1]);
        categoria.setNome(json.getString("title"));
        categoria.setStatus(extrairStatus(json.getJSONArray("statuses")));
        categoria.setCategoriasInventario(extrairCategoriasInventario(context, json.getJSONArray("inventory_categories")));

        return categoria;
    }
}
