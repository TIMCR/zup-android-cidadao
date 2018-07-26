package br.com.lfdb.particity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.core.ConstantesBase;
import br.com.lfdb.particity.domain.SolicitacaoListItem;
import br.com.lfdb.particity.service.LoginService;
import br.com.lfdb.particity.task.Updater;
import br.com.lfdb.particity.util.NetworkUtils;
import br.com.lfdb.particity.widget.SolicitacaoListItemAdapter;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.json.JSONObject;

public class SplashActivity extends Activity {
    boolean jumpToMainActivity = false;

    private SolicitacaoListItem item = null;

    @SuppressLint("NewApi") @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        setContentView(R.layout.activity_splash);
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("jump")) {
            jumpToMainActivity = extras.getBoolean("jump");
        }
        new CategoriaUpdater().execute();
    }

    @Override public void onBackPressed() {
    }

    private class CategoriaUpdater extends AsyncTask<Void, Void, Boolean> {

        long start, finish;
        boolean error = false;

        @Override protected void onPreExecute() {
            start = System.currentTimeMillis();
        }

        @Override protected Boolean doInBackground(Void... params) {
            if (!NetworkUtils.isInternetPresent(SplashActivity.this)) return Boolean.FALSE;

            try {
                Context context = SplashActivity.this;
                new Updater().update(context);

                long reportId = getIntent().getLongExtra("report_id", -1);
                if (reportId != -1 && new LoginService().usuarioLogado(context)) {
                    OkHttpClient client = new OkHttpClient();
                    Request request =
                            new Request.Builder().addHeader("X-App-Token", new LoginService().getToken(context))
                                    .addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                                    .url(Constantes.REST_URL
                                            + "/reports/items/"
                                            + reportId
                                            + ConstantesBase.getItemRelatoQuery(context))
                                    .get()
                                    .build();
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) throw new Exception();

                    item = SolicitacaoListItemAdapter.adapt(context,
                            new JSONObject(response.body().string()).getJSONObject("report"));
                }
            } catch (Exception e) {
                error = true;
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override protected void onPostExecute(Boolean result) {
            if (result) {
                finish = System.currentTimeMillis();
                if (finish - start < 3500) {
                    try {
                        Thread.sleep(finish - start);
                    } catch (Exception e) {
                        Log.w("ZUP", e.getMessage());
                    }
                }
                Intent intent;
                if (new LoginService().usuarioLogado(SplashActivity.this) || jumpToMainActivity) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else if (item != null) {
                    intent = new Intent(SplashActivity.this, SolicitacaoDetalheActivity.class);
                    intent.putExtra("solicitacao", item);
                } else {
                    intent = new Intent(SplashActivity.this, OpeningActivity.class);
                }
                startActivity(intent);
                finish();
            } else {
                if (error) {
                    new AlertDialog.Builder(SplashActivity.this).setTitle("Falha na sincronização")
                            .setMessage(
                                    "Não foi possível realizar o sincronismo de dados. Deseja tentar novamente?")
                            .setPositiveButton("Sim", (dialog, which) -> {
                                dialog.dismiss();
                                new CategoriaUpdater().execute();
                            })
                            .setNegativeButton("Não", (dialog, which) -> {
                                dialog.dismiss();
                                finish();
                            })
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "Conexão com a Internet indisponível",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}
