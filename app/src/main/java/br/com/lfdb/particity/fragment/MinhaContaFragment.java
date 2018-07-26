package br.com.lfdb.particity.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import br.com.lfdb.particity.EditarContaActivity;
import br.com.lfdb.particity.OpeningActivity;
import br.com.lfdb.particity.R;
import br.com.lfdb.particity.SolicitacaoDetalheActivity;
import br.com.lfdb.particity.base.BaseFragment;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.core.ConstantesBase;
import br.com.lfdb.particity.domain.SolicitacaoListItem;
import br.com.lfdb.particity.domain.Usuario;
import br.com.lfdb.particity.service.LoginService;
import br.com.lfdb.particity.service.UsuarioService;
import br.com.lfdb.particity.util.AuthHelper;
import br.com.lfdb.particity.util.FontUtils;
import br.com.lfdb.particity.util.ImageUtils;
import br.com.lfdb.particity.widget.SolicitacaoListItemAdapter;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MinhaContaFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private static final int REQUEST_EDIT_USER = 1099;

    private TextView nomeUsuario;

    private boolean isLoading = false;
    private Tasker task;
    private SolicitacaoAdapter adapter;
    private int lastPageLoaded = 0;
    private boolean shouldContinueLoading = true;
    private String lastResult = "";

    List<SolicitacaoListItem> listaSolicitacoes = new ArrayList<>();

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_minha_conta, container, false);

        TextView botaoSair = (TextView) view.findViewById(R.id.botaoSair);
        botaoSair.setTypeface(FontUtils.getRegular(getActivity()));
        botaoSair.setOnClickListener(v -> new AlertDialog.Builder(getActivity()).setMessage(
                R.string.deseja_realmente_sair_da_sua_conta)
                .setPositiveButton(R.string.sim, (dialog, which) -> {
                    final ProgressDialog dialog1 = new ProgressDialog(getActivity());
                    dialog1.setMessage("Saindo...");
                    dialog1.setCancelable(false);
                    dialog1.show();

                    new Thread(() -> {
                        try {
                            GoogleCloudMessaging.getInstance(getActivity()).unregister();
                            OkHttpClient client = new OkHttpClient();
                            Request request =
                                    new Request.Builder().addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                                            .addHeader("X-App-Token", new LoginService().getToken(getActivity()))
                                            .url(Constantes.REST_URL + "/sign_out?token=" + Uri.encode(
                                                    new LoginService().getToken(getActivity())))
                                            .delete()
                                            .build();
                            Response response = client.newCall(request).execute();
                            if (response.isSuccessful()) {
                                dialog1.dismiss();
                                PreferenceManager.getDefaultSharedPreferences(getActivity())
                                        .edit()
                                        .remove("gcm")
                                        .apply();
                                startActivity(new Intent(getActivity(), OpeningActivity.class));
                                new LoginService().registrarLogout(getActivity());
                                getActivity().finish();
                            } else if (response.code() == 401) {
                                AuthHelper.redirectSessionExpired(getActivity());
                            } else {
                                dialog1.dismiss();
                                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(),
                                        "Ops... Não foi possível realizar seu logout...", Toast.LENGTH_SHORT).show());
                            }
                        } catch (IOException e) {
                            dialog1.dismiss();
                            e.printStackTrace();
                        }
                    }).start();
                })
                .setNegativeButton(R.string.nao, (dialog, which) -> dialog.dismiss())
                .show());

        TextView botaoEditar = (TextView) view.findViewById(R.id.botaoEditar);
        botaoEditar.setTypeface(FontUtils.getRegular(getActivity()));
        botaoEditar.setOnClickListener(
                v -> startActivityForResult(new Intent(getActivity(), EditarContaActivity.class),
                        REQUEST_EDIT_USER));

        ((TextView) view.findViewById(R.id.instrucoes)).setTypeface(FontUtils.getBold(getActivity()));

        List<SolicitacaoListItem> items = new ArrayList<>();

        ((TextView) view.findViewById(R.id.minhaConta)).setTypeface(FontUtils.getLight(getActivity()));
        nomeUsuario = (TextView) view.findViewById(R.id.nomeUsuario);
        nomeUsuario.setTypeface(FontUtils.getLight(getActivity()));

        Usuario usuario = new UsuarioService().getUsuarioAtivo(getActivity());
        if (usuario != null) {
            nomeUsuario.setText(usuario.getNome() != null ? usuario.getNome() : usuario.getEmail());
        }

        TextView solicitacoes = (TextView) view.findViewById(R.id.solicitacoes);
        solicitacoes.setTypeface(FontUtils.getBold(getActivity()));
        solicitacoes.setText(items.size() + " " +
                (items.size() == 1 ? getString(R.string.solicitacao) : getString(R.string.solicitacoes)));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            new Tasker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new Tasker().execute();
        }

        ListView list = (ListView) view.findViewById(R.id.listaSolicitacoes);
        list.setOnItemClickListener(this);
        adapter = new SolicitacaoAdapter(getActivity(), listaSolicitacoes);
        list.setAdapter(adapter);
        //list.setOnScrollListener(this);

        return view;
    }

    @Override public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            lastPageLoaded = 0;
            shouldContinueLoading = true;
            lastResult = "";
            adapter.clear();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                new Tasker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                new Tasker().execute();
            }
        }
    }

    private void preencherLista(List<SolicitacaoListItem> itens) {
        listaSolicitacoes.addAll(itens);
        adapter.notifyDataSetChanged();
    }

    /*
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }*/

    /*
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int loadedItems = firstVisibleItem + visibleItemCount;
        if ((loadedItems == totalItemCount) && !isLoading) {
            shouldContinueLoading = true;
            task = new Tasker();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }
        }
    }*/

    public void refresh() {
        lastPageLoaded = 0;
        shouldContinueLoading = true;
        lastResult = "";
        adapter.clear();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            new Tasker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new Tasker().execute();
        }
    }

    @Override protected String getScreenName() {
        return "Minha Conta";
    }

    public class SolicitacaoAdapter extends ArrayAdapter<SolicitacaoListItem> {

        private List<SolicitacaoListItem> items;

        public SolicitacaoAdapter(Context context, List<SolicitacaoListItem> objects) {
            super(context, R.layout.solicitacao_row, objects);
            items = objects;
        }

        @SuppressLint("NewApi") @SuppressWarnings("deprecation") @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = getActivity().getLayoutInflater().inflate(R.layout.solicitacao_row, parent, false);
            SolicitacaoListItem item = items.get(position);

            TextView titulo = (TextView) row.findViewById(R.id.titulo);

            TextView subcategoria = (TextView) row.findViewById(R.id.subcategoria);
            if (item.getCategoria() != null && item.getCategoria().getCategoriaMae() != null) {
                titulo.setText(item.getCategoria().getNome());
                titulo.setTypeface(FontUtils.getLight(getContext()));

                subcategoria.setText(item.getCategoria().getCategoriaMae().getNome());
                subcategoria.setTypeface(FontUtils.getLight(getContext()));
            } else {
                titulo.setText(item.getTitulo());
                titulo.setTypeface(FontUtils.getLight(getContext()));
                subcategoria.setVisibility(View.GONE);
            }

            TextView data = (TextView) row.findViewById(R.id.data);
            data.setText(item.getData());
            data.setTypeface(FontUtils.getBold(getContext()));

            TextView protocolo = (TextView) row.findViewById(R.id.protocolo);
            if (item.getProtocolo() != null) {
                protocolo.setText(getString(R.string.protocolo) + " " + item.getProtocolo());
                protocolo.setTypeface(FontUtils.getRegular(getContext()));
            } else {
                protocolo.setVisibility(View.GONE);
            }
            TextView indicadorStatus = (TextView) row.findViewById(R.id.indicadorStatus);
            indicadorStatus.setTypeface(FontUtils.getBold(getContext()));
            if (item.getStatus() != null) {
                row.findViewById(R.id.bg).setBackgroundColor(item.getStatus().getCor());
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    indicadorStatus.setBackgroundDrawable(
                            ImageUtils.getStatusBackground(getActivity(), item.getStatus().getCor()));
                } else {
                    indicadorStatus.setBackground(
                            ImageUtils.getStatusBackground(getActivity(), item.getStatus().getCor()));
                }
                indicadorStatus.setText(item.getStatus().getNome());
                int fiveDp = (int) ImageUtils.dpToPx(getActivity(), 5);
                int tenDp = (int) ImageUtils.dpToPx(getActivity(), 10);
                indicadorStatus.setPadding(tenDp, fiveDp, tenDp, fiveDp);
            }
            return row;
        }
    }

    @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SolicitacaoListItem item = (SolicitacaoListItem) parent.getItemAtPosition(position);
        Intent intent = new Intent(getActivity(), SolicitacaoDetalheActivity.class);
        intent.putExtra("solicitacao", item);
        startActivity(intent);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_USER && resultCode == Activity.RESULT_OK) {
            Usuario usuario = new UsuarioService().getUsuarioAtivo(getActivity());
            if (usuario != null) {
                nomeUsuario.setText(usuario.getNome() != null ? usuario.getNome() : usuario.getEmail());
            }
        }
    }

    public class Tasker extends AsyncTask<Void, Void, String> {

        private ProgressDialog dialog;

        @Override protected void onPreExecute() {
            if (shouldContinueLoading) {
                dialog = new ProgressDialog(getActivity());
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setIndeterminate(true);
                dialog.setMessage("Por favor, aguarde...");
                dialog.show();
            }
        }

        @Override protected String doInBackground(Void... params) {
            if (shouldContinueLoading) {
                try {
                    isLoading = true;
                    Request request = new Request.Builder().url(
                            Constantes.REST_URL + "/reports/users/me/items?per_page=10&page=" + (lastPageLoaded
                                    + 1) + "&sort=created_at&order=DESC")
                            .addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                            .addHeader("X-App-Token", new LoginService().getToken(getActivity()))
                            .build();
                    Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String json = response.body().string();

                        if (lastResult.equals(json)) {
                            shouldContinueLoading = false;
                            return null;
                        } else {
                            lastResult = json;
                        }

                        return json;
                    } else if (response.code() == 401) {
                        AuthHelper.redirectSessionExpired(getActivity());
                        return null;
                    }
                } catch (Exception e) {
                    Log.e("ZUP", e.getMessage(), e);
                }
            }
            return null;
        }

        @Override protected void onPostExecute(String result) {
            isLoading = false;
            if (dialog != null) dialog.dismiss();
            if (shouldContinueLoading) {
                if (result != null) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        setReportCount(obj.getInt("total_reports_by_user"));
                        JSONArray array = obj.getJSONArray("reports");
                        List<SolicitacaoListItem> itens = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            try {
                                itens.add(SolicitacaoListItemAdapter.adapt(getActivity(), array.getJSONObject(i)));
                            } catch (Exception e) {
                                Log.e("ZUP", "não foi possível parsear o relato", e);
                            }
                        }

                        if (itens.isEmpty() || itens.size() < 10) shouldContinueLoading = false;
                        lastPageLoaded++;
                        preencherLista(itens);
                    } catch (Exception e) {
                        Log.e("ZUP", e.getMessage(), e);
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), "Não foi possível obter sua lista de relatos",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), "Não foi possível obter sua lista de relatos",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    private void setReportCount(int reportCount) {
        TextView solicitacoes = (TextView) getView().findViewById(R.id.solicitacoes);
        solicitacoes.setTypeface(FontUtils.getBold(getActivity()));
        solicitacoes.setText(reportCount + " " +
                (reportCount == 1 ? getString(R.string.solicitacao) : getString(R.string.solicitacoes)));
    }
}
