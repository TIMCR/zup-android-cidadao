package br.com.lfdb.particity.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.todddavies.components.progressbar.ProgressWheel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.com.lfdb.particity.FiltroEstatisticasNovoActivity;
import br.com.lfdb.particity.R;
import br.com.lfdb.particity.base.BaseFragment;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.core.ConstantesBase;
import br.com.lfdb.particity.domain.BuscaEstatisticas;
import br.com.lfdb.particity.domain.Estatistica;
import br.com.lfdb.particity.service.LoginService;
import br.com.lfdb.particity.util.FontUtils;
import br.com.lfdb.particity.util.ImageUtils;

public class EstatisticasFragment extends BaseFragment {

    public static final int REQUEST_FILTRO = 1478;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estatisticas, container, false);

        ((TextView) view.findViewById(R.id.titulo)).setTypeface(FontUtils.getLight(getActivity()));

        TextView botaoFiltrar = (TextView) view.findViewById(R.id.botaoFiltrar);
        botaoFiltrar.setTypeface(FontUtils.getRegular(getActivity()));
        botaoFiltrar.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getActivity().startActivityForResult(
                        new Intent(getActivity(), FiltroEstatisticasNovoActivity.class), REQUEST_FILTRO);
            }
        });

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            new Tasker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new Tasker().execute();
        }

        return view;
    }

    @Override public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                new Tasker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                new Tasker().execute();
            }
        }
    }

    public void aplicarFiltro(BuscaEstatisticas busca) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            new Tasker(busca).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new Tasker(busca).execute();
        }
    }

    @Override protected String getScreenName() {
        return "Estatísticas";
    }

    public class Tasker extends AsyncTask<Void, Void, String> {

        private ProgressDialog dialog;
        private String data = null;
        private Long id = null;

        public Tasker() {
        }

        public Tasker(BuscaEstatisticas busca) {
            this.id = busca.getCategoria();
            this.data = busca.getPeriodo().getDateString();
        }

        @Override protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setIndeterminate(true);
            dialog.setMessage("Por favor, aguarde...");
            dialog.show();
        }

        @Override protected String doInBackground(Void... params) {
            try {

                String url = Constantes.REST_URL + "/reports/stats";
                if (id != null) {
                    url += "?category_id=" + id;
                }
                if (data != null) {
                    if (id != null) {
                        url += "&begin_date=" + data;
                    } else {
                        url += "?begin_date=" + data;
                    }
                }
                LoginService loginService = new LoginService();
                Request request = new Request.Builder().url(url)
                        .addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                        .addHeader("X-App-Token", loginService.getToken(getActivity()))
                        .build();
                Crashlytics.setLong("User", loginService.getUserId(getActivity()));
                Crashlytics.setLong("CategoryId", id);
                Crashlytics.setString("Date", data);
                Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
                if (response.isSuccessful()) return response.body().string();
            } catch (Exception e) {
                Crashlytics.logException(e);
                Log.e("ZUP", e.getMessage(), e);
            }
            return null;
        }

        @Override protected void onPostExecute(String result) {
            dialog.dismiss();
            if (result != null) {
                try {
                    JSONArray array = new JSONObject(result).getJSONArray("stats");
                    List<Estatistica> estatisticas = new ArrayList<Estatistica>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONArray statuses = array.getJSONObject(i).getJSONArray("statuses");
                        for (int j = 0; j < statuses.length(); j++) {
                            JSONObject status = statuses.getJSONObject(j);
                            Estatistica estatistica = new Estatistica(status.getInt("status_id"),
                                    status.isNull("color") ? Color.BLACK
                                            : Color.parseColor(status.getString("color")), status.getInt("count"),
                                    status.getString("title"));
                            if (estatisticas.contains(estatistica)) {
                                for (Estatistica e : estatisticas) {
                                    if (e.equals(estatistica)) {
                                        e.setQuantidade(e.getQuantidade() + estatistica.getQuantidade());
                                        break;
                                    }
                                }
                            } else {
                                estatisticas.add(estatistica);
                            }
                        }
                    }

                    int total = 0;
                    for (Estatistica estatistica : estatisticas) {
                        total += estatistica.getQuantidade();
                    }

                    for (Estatistica estatistica : estatisticas) {
                        estatistica.setPorcentagem((int) (estatistica.getQuantidade() * 100.0 / total));
                    }

                    montarGraficos(estatisticas);
                } catch (Exception e) {
                    Crashlytics.logException(e);
                    Log.e("ZUP", e.getMessage(), e);
                    Toast.makeText(getActivity(), "Não foi possível obter as estatísticas", Toast.LENGTH_LONG)
                            .show();
                }
            } else {
                Toast.makeText(getActivity(), "Não foi possível obter as estatísticas", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private void montarGraficos(List<Estatistica> estatisticas) {
        for (int i = 1; i <= 5; i++) {
            ((LinearLayout) getView().findViewById(getActivity().getResources()
                    .getIdentifier("line_" + i, "id", getActivity().getPackageName()))).removeAllViews();
        }

        int linha = 0;
        boolean margin = false;
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (int i = 0; i < estatisticas.size(); i++) {
            if (i % 2 == 0) linha++;
            LinearLayout container = (LinearLayout) getView().findViewById(getActivity().getResources()
                    .getIdentifier("line_" + linha, "id", getActivity().getPackageName()));
            View view = inflater.inflate(R.layout.estatistica_item, container, false);
            ProgressWheel wheel = (ProgressWheel) view.findViewById(R.id.spinner);
            wheel.setText(estatisticas.get(i).getPorcentagem() + "%");
            wheel.setBarColor(estatisticas.get(i).getCor());
            wheel.setProgress((int) (360.0 / 100.0 * estatisticas.get(i).getPorcentagem()));
            TextView qtd = (TextView) view.findViewById(R.id.txtQtd);
            qtd.setText(String.valueOf(estatisticas.get(i).getQuantidade()));
            qtd.setTypeface(FontUtils.getExtraBold(getActivity()));
            qtd.setTextColor(estatisticas.get(i).getCor());
            TextView label = (TextView) view.findViewById(R.id.txtLabel);
            label.setText(estatisticas.get(i).getTexto());
            label.setTypeface(FontUtils.getRegular(getActivity()));

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            if (margin) {
                params.setMargins(0, 0, (int) ImageUtils.dpToPx(getActivity(), 35), 0);
            } else {
                params.setMargins((int) ImageUtils.dpToPx(getActivity(), 35), 0, 0, 0);
            }
            view.setLayoutParams(params);
            margin = !margin;
            container.addView(view);
        }
    }
}
