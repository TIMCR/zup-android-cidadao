package br.com.lfdb.particity.widget;

import android.content.Context;

import android.util.Log;
import br.com.lfdb.particity.core.Crashlytics;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import br.com.lfdb.particity.domain.CategoriaRelato;
import br.com.lfdb.particity.domain.SolicitacaoListItem;
import br.com.lfdb.particity.service.CategoriaRelatoService;
import br.com.lfdb.particity.util.DateUtils;
import br.com.lfdb.particity.util.ViewUtils;

public class SolicitacaoListItemAdapter {

    public static SolicitacaoListItem adapt(Context context, JSONObject json) throws Exception {
        try {
            CategoriaRelatoService service = new CategoriaRelatoService();
            SolicitacaoListItem item = new SolicitacaoListItem();
            item.setId(json.getLong("id"));
            item.setComentario(json.getString("description"));
            item.setData(DateUtils.getDefault(DateUtils.parseRFC3339Date(json.getString("created_at"))));
            item.setEndereco(json.getString("address") +
                (json.has("number") && !json.isNull("number") ? ", " + json.getString("number") : "") +
                (json.has("postal_code") && !json.isNull("postal_code") ? ", " + json.getString(
                    "postal_code") : "") +
                (json.has("district") && !json.isNull("district") ? ", " + json.getString("district") : ""));
            item.setReferencia(json.optString("reference"));
            item.setFotos(new ArrayList<>());

            JSONArray array = json.getJSONArray("images");
            for (int j = 0; j < array.length(); j++) {
                item.getFotos()
                    .add(ViewUtils.isMdpiOrLdpi(context) ? array.getJSONObject(j).getString("low")
                        : array.getJSONObject(j).getString("high"));
            }

            CategoriaRelato categoria;
            if (json.has("category_id")) {
                categoria = service.getById(context, json.getLong("category_id"));
            } else {
                categoria = service.getById(context, json.getJSONObject("category").getLong("id"));
            }
            if (categoria != null) {
                if (categoria.getNome() != null) {
                    item.setTitulo(categoria.getNome());
                } else {
                    item.setTitulo("");
                }
                item.setCategoria(categoria);

                if (json.has("status_id")) {
                    CategoriaRelato.Status status =
                        service.getStatusById(context, categoria.getId(), json.getLong("status_id"));
                    item.setStatus(new SolicitacaoListItem.Status(status.getNome(), status.getCor()));
                } else {
                    item.setStatus(new SolicitacaoListItem.Status(json.getJSONObject("status").getString("title"),
                        json.getJSONObject("status").getString("color")));
                }
            }
            item.setProtocolo(json.optString("protocol", null));
            item.setCreatorId(json.getJSONObject("user").getLong("id"));
            item.setLatitude(json.getJSONObject("position").getDouble("latitude"));
            item.setLongitude(json.getJSONObject("position").getDouble("longitude"));
            return item;
        } catch(Exception e){
            Log.e("ZUP", e.getMessage(), e);
            Crashlytics.logException(e);
            return null;
        }
    }
}
