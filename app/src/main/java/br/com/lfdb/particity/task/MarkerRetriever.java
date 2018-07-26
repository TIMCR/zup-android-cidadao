package br.com.lfdb.particity.task;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import br.com.lfdb.particity.api.model.Cluster;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.core.ConstantesBase;
import br.com.lfdb.particity.domain.BuscaExplore;
import br.com.lfdb.particity.domain.ItemInventario;
import br.com.lfdb.particity.domain.ItemRelato;
import br.com.lfdb.particity.domain.RequestModel;
import br.com.lfdb.particity.fragment.ExploreFragment;
import br.com.lfdb.particity.service.CategoriaInventarioService;
import br.com.lfdb.particity.service.CategoriaRelatoService;
import br.com.lfdb.particity.service.LoginService;
import br.com.lfdb.particity.util.AuthHelper;
import br.com.lfdb.particity.util.DateUtils;
import br.com.lfdb.particity.util.ViewUtils;
import com.google.android.gms.maps.model.Marker;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

public class MarkerRetriever extends AsyncTask<Void, Object, Void> {

  private List<ItemInventario> itensInventario = new CopyOnWriteArrayList<>();
  private List<ItemRelato> itensRelato = new CopyOnWriteArrayList<>();
  private List<Cluster> clusters = new CopyOnWriteArrayList<>();
  private static final int MAX_ITEMS_PER_REQUEST = 50;
  private BuscaExplore search;
  private Context context;
  private RequestModel request;
  private ProgressBar loading;
  private Map<Marker, Object> marcadores = new HashMap<>();
  private ExploreFragment fragment;

  public MarkerRetriever(RequestModel request, ProgressBar loading, BuscaExplore search,
                         Context context, ExploreFragment fragment) {
    this.request = request;
    this.loading = loading;
    this.search = search;
    this.context = context;
    this.fragment = fragment;
  }

  @Override protected void onPreExecute() {
    loading.post(() -> loading.setVisibility(View.VISIBLE));
  }

  @Override protected void onPostExecute(Void aVoid) {
    loading.post(() -> loading.setVisibility(View.GONE));
  }

  @Override protected void onCancelled() {
    loading.post(() -> loading.setVisibility(View.GONE));
  }

  @Override protected Void doInBackground(Void... voids) {
    try {
      com.squareup.okhttp.Request requisicao;
      if (!search.getIdsCategoriaInventario().isEmpty()) {
        String query;
        if (search.getIdsCategoriaInventario().size() == 1) {
          query = "&inventory_categories_ids=" + search.getIdsCategoriaInventario().get(0);
        } else {
          StringBuilder builder = new StringBuilder("&inventory_categories_ids=");
          for (Long id : search.getIdsCategoriaInventario()) {
            builder.append(id).append(",");
          }
          query = builder.toString();
          query = query.substring(0, query.length() - 1);
        }
        requisicao = new Request.Builder().url(
                Constantes.REST_URL
                        + "/search/inventory/items"
                        + ConstantesBase.getItemInventarioQuery(context)
                        + "&position[latitude]="
                        + request.getLatitude()
                        + "&position[longitude]="
                        + request.getLongitude()
                        + "&position[distance]="
                        + request.getRaio()
                        + "&max_items="
                        + MAX_ITEMS_PER_REQUEST
                        + query
                        + getClusterQuery())
                .addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                .addHeader("X-App-Token", new LoginService().getToken(context))
                .build();
        if (isCancelled()) return null;
        Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(requisicao).execute();
        if (response.isSuccessful()) {
          if (isCancelled()) return null;
          extrairItensInventario(response.body().string());
        } else if (response.code() == 401) {
          AuthHelper.redirectSessionExpired(context);
          return null;
        }
      }
      if (!search.getIdsCategoriaRelato().isEmpty()) {

        String categories;
        if (search.getIdsCategoriaRelato().size() == 1) {
          categories = "&reports_categories_ids=" + search.getIdsCategoriaRelato().get(0);
        } else {
          StringBuilder builder = new StringBuilder("&reports_categories_ids=");
          for (Long id : search.getIdsCategoriaRelato()) {
            builder.append(id).append(",");
          }
          categories = builder.toString();
          categories = categories.substring(0, categories.length() - 1);
        }
        String query =
                Constantes.REST_URL
                        + "/search/reports/items"
                        + ConstantesBase.getItemRelatoQuery(context)
                        + "&position[latitude]="
                        + request.getLatitude()
                        + "&position[longitude]="
                        + request.getLongitude()
                        + "&position[distance]="
                        + request.getRaio()
                        + "&max_items="
                        + MAX_ITEMS_PER_REQUEST
                        + "&begin_date="
                        + search.getPeriodo().getDateString()
                        + "&end_date="
                        + DateTime.now()
                        + "&display_type=full"
                        + categories
                        + getClusterQuery();
        if (search.getStatus() != null) {
          query += "&statuses=" + search.getStatus().getId();
        }
        Log.d("REQUEST", query);
        String token = new LoginService().getToken(context);
        requisicao = new Request.Builder().url(query)
                .addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                .addHeader("X-App-Token", token)
                .build();
        if (isCancelled()) return null;

        Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(requisicao).execute();
        if (response.isSuccessful()) {
          if (isCancelled()) return null;
          extrairItensRelato(response.body().string());
        } else if (response.code() == 401) {
          AuthHelper.redirectSessionExpired(context);
          return null;
        }
      }
    } catch (Exception e) {
      if (!(e instanceof InterruptedIOException)) {
        Log.e("ZUP", "Erro ao buscar inventário", e);
      }
      return null;
    }

    for (ItemInventario item : itensInventario) {
      if (!marcadores.containsValue(item)) {
        publishProgress(item);
      }
    }

    for (ItemRelato item : itensRelato) {
      if (!marcadores.containsValue(item)) {
        publishProgress(item);
      }
    }

    for (Cluster item : clusters) {
      if (!marcadores.containsValue(item)) {
        publishProgress(item);
      }
    }

    return null;
  }

  @Override protected void onProgressUpdate(Object... values) {
    if (values[0] instanceof ItemInventario) {
      fragment.addMarkerInventory((ItemInventario) values[0]);
    } else if (values[0] instanceof ItemRelato) {
      fragment.addMarkerReport((ItemRelato) values[0]);
    } else if (values[0] instanceof Cluster) {
      fragment.addMarkerCluster((Cluster) values[0]);
    }
  }

  private void extrairItensInventario(String raw) throws Exception {
    CategoriaInventarioService service = new CategoriaInventarioService();
    JSONObject root = new JSONObject(raw);
    JSONArray array = root.getJSONArray("items");
    for (int i = 0; i < array.length(); i++) {
      if (isCancelled()) return;
      JSONObject json = array.getJSONObject(i);
      ItemInventario item = new ItemInventario();
      item.setCategoria(service.getById(context, json.getLong("inventory_category_id")));
      item.setId(json.getLong("id"));
      item.setLatitude(json.getJSONObject("position").getDouble("latitude"));
      item.setLongitude(json.getJSONObject("position").getDouble("longitude"));
      itensInventario.add(item);
    }

    addClusters(root.optJSONArray("clusters"), false);
  }

  private void extrairItensRelato(String raw) throws Exception {
    CategoriaRelatoService service = new CategoriaRelatoService();
    JSONObject root = new JSONObject(raw);
    JSONArray array = root.getJSONArray("reports");
    for (int i = 0; i < array.length(); i++) {
      if (isCancelled()) return;
      JSONObject json = array.getJSONObject(i);
      ItemRelato item = new ItemRelato();
      item.setId(json.getLong("id"));
      item.setDescricao(json.getString("description"));
      item.setProtocolo(json.optString("protocol", null));
      item.setEndereco(json.getString("address") +
              (json.has("number") && !json.isNull("number") ? ", " + json.getString("number") : "") +
              (json.has("postal_code") && !json.isNull("postal_code") ? ", " + json.getString(
                      "postal_code") : "") +
              (json.has("district") && !json.isNull("district") ? ", " + json.getString("district")
                      : ""));
      item.setData(
              DateUtils.getIntervaloTempo(DateUtils.parseRFC3339Date(json.getString("created_at"))));
      item.setCategoria(service.getById(context, json.getJSONObject("category").getLong("id")));
      item.setLatitude(json.getJSONObject("position").getDouble("latitude"));
      item.setLongitude(json.getJSONObject("position").getDouble("longitude"));
      item.setIdItemInventario(json.optLong("inventory_item_id", -1));
      item.setIdStatus(json.optLong("status_id", -1));
      item.setReferencia(json.optString("reference", ""));

      JSONArray fotos = json.getJSONArray("images");
      for (int j = 0; j < fotos.length(); j++) {
        item.getFotos()
                .add(ViewUtils.isMdpiOrLdpi(context) ? fotos.getJSONObject(j).getString("low")
                        : fotos.getJSONObject(j).getString("high"));
      }

      itensRelato.add(item);
    }

    addClusters(root.optJSONArray("clusters"), true);
  }

  private void addClusters(JSONArray array, boolean isReport) throws Exception {
    if (array == null) return;

    List<Long> ids = new ArrayList<>();
    List<Cluster> clusters = new ArrayList<>();
    for (int i = 0; i < array.length(); i++) {
      if (isCancelled()) return;
      Cluster cluster =
              ConstantesBase.GSON.fromJson(array.get(i).toString(), Cluster.class).setReport(isReport);
      ids.addAll(cluster.getItemsIds());
      clusters.add(cluster);
    }

    ((Activity) context).runOnUiThread(() -> {
      Iterator<Marker> it = marcadores.keySet().iterator();
      while (it.hasNext()) {
        Marker marker = it.next();
        if (marcadores.get(marker) instanceof Cluster) {
          marker.remove();
          it.remove();
          marcadores.remove(marker);
        } else if (marcadores.get(marker) instanceof ItemRelato) {
          ItemRelato item = (ItemRelato) marcadores.get(marker);
          if (ids.contains(item.getId())) {
            marker.remove();
            it.remove();
            marcadores.remove(marker);
            Log.d("TAG", "true " + item.getId());
          } else {
            Log.d("TAG", "false");
          }
        } else if (marcadores.get(marker) instanceof ItemInventario) {
          ItemInventario item = (ItemInventario) marcadores.get(marker);
          if (ids.contains(item.getId())) {
            marker.remove();
            it.remove();
            marcadores.remove(marker);
            Log.d("TAG", "true " + item.getId());
          } else {
            Log.d("TAG", "false");
          }
        }
      }
    });
    this.clusters = clusters;
  }

  private String getClusterQuery() {
    return "&clusterize=true&zoom=" + (int) fragment.zoom;
  }
}