package br.com.lfdb.particity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import br.com.lfdb.particity.base.BaseActivity;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.core.ConstantesBase;
import br.com.lfdb.particity.domain.ItemInventario;
import br.com.lfdb.particity.domain.SolicitacaoListItem;
import br.com.lfdb.particity.fragment.InformacoesFragment;
import br.com.lfdb.particity.fragment.SolicitacoesFragment;
import br.com.lfdb.particity.service.LoginService;
import br.com.lfdb.particity.util.AuthHelper;
import br.com.lfdb.particity.util.FontUtils;
import br.com.lfdb.particity.widget.SolicitacaoListItemAdapter;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class DetalheMapaActivity extends BaseActivity implements View.OnClickListener {

    private InformacoesFragment infoFragment;
    private SolicitacoesFragment solFragment;

    private ArrayList<SolicitacaoListItem> relatos;
    private HashMap<String, HashMap<String, String>> inventoryData = new HashMap<>();

    private ItemInventario item;
    private TextView solicitacoes;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_mapa);

        setBarraNavegacaoVisivel(false);

        item = (ItemInventario) getIntent().getSerializableExtra("item");

        TextView titulo = (TextView) findViewById(R.id.titulo);
        titulo.setText(item.getCategoria().getNome());
        titulo.setTypeface(FontUtils.getLight(this));

        TextView voltar = (TextView) findViewById(R.id.botaoVoltar);
        voltar.setTypeface(FontUtils.getRegular(this));
        voltar.setOnClickListener(v -> finish());

        TextView informacoes = (TextView) findViewById(R.id.botaoInformacoes);
        informacoes.setTypeface(FontUtils.getLight(this));
        informacoes.setOnClickListener(this);

        solicitacoes = (TextView) findViewById(R.id.botaoSolicitacoes);
        solicitacoes.setTypeface(FontUtils.getLight(this));
        solicitacoes.setOnClickListener(this);

        infoFragment = new InformacoesFragment();
        solFragment = new SolicitacoesFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragments_place, solFragment)
                .add(R.id.fragments_place, infoFragment)
                .commit();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            new Tasker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new Tasker().execute();
        }
    }

    private void setBarraNavegacaoVisivel(boolean visible) {
        findViewById(R.id.barra_navegacao).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override public void onClick(View v) {
        ((TextView) findViewById(R.id.botaoInformacoes)).setTextColor(Color.rgb(0x2a, 0xb4, 0xdc));
        ((TextView) findViewById(R.id.botaoSolicitacoes)).setTextColor(Color.rgb(0x2a, 0xb4, 0xdc));

        switch (v.getId()) {
            case R.id.botaoInformacoes:
                getSupportFragmentManager().beginTransaction()
                        .show(infoFragment)
                        .hide(solFragment)
                        .commit();
                break;
            case R.id.botaoSolicitacoes:
                getSupportFragmentManager().beginTransaction()
                        .hide(infoFragment)
                        .show(solFragment)
                        .commit();
                break;
            default:
                return;
        }

        ((TextView) findViewById(v.getId())).setTextColor(Color.WHITE);
    }

    @Override protected String getScreenName() {
        return "Informações de um Item de Inventário";
    }

    private class Tasker extends AsyncTask<Void, Void, Boolean>
            implements DialogInterface.OnCancelListener {

        private ProgressDialog dialog;

        @Override protected void onPreExecute() {
            dialog = new ProgressDialog(DetalheMapaActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setIndeterminate(true);
            dialog.setMessage("Por favor, aguarde...");
            dialog.setOnCancelListener(this);
            dialog.show();
        }

        @Override protected Boolean doInBackground(Void... params) {
            try {
                Request request =
                        new Request.Builder().addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                                .addHeader("X-App-Token", new LoginService().getToken(DetalheMapaActivity.this))
                                .url(Constantes.REST_URL
                                        + "/reports/inventory/"
                                        + item.getId()
                                        + "/items"
                                        + ConstantesBase.getItemRelatoQuery(DetalheMapaActivity.this)
                                        + ",data.field.id,data.content")
                                .build();
                if (isCancelled()) return Boolean.FALSE;

                Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
                if (response.isSuccessful()) {
                    setRelatos(response.body().string());
                } else if (response.code() == 401) {
                    AuthHelper.redirectSessionExpired(getApplicationContext());
                    return Boolean.FALSE;
                }

                // Dados (campos dinâmicos)
                request = new Request.Builder().addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                        .addHeader("X-App-Token", new LoginService().getToken(DetalheMapaActivity.this))
                        .url(Constantes.REST_URL
                                + "/inventory/categories/"
                                + item.getCategoria().getId()
                                + ConstantesBase.getCategoriasInventarioQuery(DetalheMapaActivity.this)
                                + ",sections.title,sections.fields.label")
                        .build();

                if (isCancelled()) return Boolean.FALSE;

                response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
                String categoria = null;
                if (response.isSuccessful()) {
                    categoria = response.body().string();
                } else if (response.code() == 401) {
                    AuthHelper.redirectSessionExpired(getApplicationContext());
                    return Boolean.FALSE;
                }

                request = new Request.Builder().addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                        .addHeader("X-App-Token", new LoginService().getToken(DetalheMapaActivity.this))
                        .url(Constantes.REST_URL
                                + "/inventory/categories/"
                                + item.getCategoria().getId()
                                + "/items/"
                                + item.getId())
                        .build();

                if (isCancelled()) return Boolean.FALSE;

                response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
                String itemInventario = null;
                if (response.isSuccessful()) {
                    itemInventario = response.body().string();
                } else if (response.code() == 401) {
                    AuthHelper.redirectSessionExpired(getApplicationContext());
                    return Boolean.FALSE;
                }

                if (isCancelled()) return Boolean.FALSE;

                montarHashMap(categoria, itemInventario);
                return Boolean.TRUE;
            } catch (Exception e) {
                Log.e("ZUP", "Falha ao obter informações do item de inventário", e);
                return Boolean.FALSE;
            }
        }

        @Override protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            if (result) {
                if (relatos == null || relatos.isEmpty()) {
                    ocultarAbasAdicionais();
                } else {
                    prepararAbas();
                    solFragment.setRelatos(relatos);
                }

                for (String key : inventoryData.keySet()) {
                    infoFragment.setDados(key, inventoryData.get(key));
                }
                setBarraNavegacaoVisivel(true);
            } else {
                Toast.makeText(DetalheMapaActivity.this, "Não foi possível obter os dados do item",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }

        private void setRelatos(String json) throws Exception {
            JSONArray array = new JSONObject(json).getJSONArray("reports");
            relatos = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                relatos.add(
                        SolicitacaoListItemAdapter.adapt(DetalheMapaActivity.this, array.getJSONObject(i)));
            }
        }

        private void montarHashMap(String categoria, String itemInventario) throws Exception {
            List<JSONObject> dados = new ArrayList<>();

            JSONArray data = new JSONObject(itemInventario).getJSONObject("item").getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                dados.add(data.getJSONObject(i));
            }

            JSONArray sections =
                    new JSONObject(categoria).getJSONObject("category").getJSONArray("sections");
            for (int i = 0; i < sections.length(); i++) {
                List<JSONObject> campos = new ArrayList<>();
                String sectionName = sections.getJSONObject(i).getString("title");
                JSONArray fields = sections.getJSONObject(i).getJSONArray("fields");
                for (int j = 0; j < fields.length(); j++) {
                    campos.add(fields.getJSONObject(j));
                }

                HashMap<String, String> camposDinamicos = new HashMap<>();
                for (JSONObject campo : campos) {
                    for (JSONObject dado : dados) {
                        if (dado.getJSONObject("field").getLong("id") == campo.getLong("id")) {
                            if (!dado.getString("content").equals("null")) {
                                camposDinamicos.put(
                                        campo.getString("label").equals("null") ? campo.getString("title")
                                                : campo.getString("label"), dado.getString("content"));
                            }
                        }
                    }
                }

                inventoryData.put(sectionName, camposDinamicos);
            }
        }

        @Override public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
            cancel(true);
            finish();
        }
    }

    private void ocultarAbasAdicionais() {
        solicitacoes.setVisibility(View.GONE);

        ((TextView) findViewById(R.id.botaoInformacoes)).setTextColor(Color.WHITE);
        ((TextView) findViewById(R.id.botaoSolicitacoes)).setTextColor(Color.WHITE);
        getSupportFragmentManager().beginTransaction().hide(solFragment).show(infoFragment).commit();
    }

    private void prepararAbas() {
        getSupportFragmentManager().beginTransaction().show(solFragment).hide(infoFragment).commit();
    }
}
