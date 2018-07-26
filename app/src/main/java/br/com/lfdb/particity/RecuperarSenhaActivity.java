package br.com.lfdb.particity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import br.com.lfdb.particity.base.BaseActivity;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.core.ConstantesBase;
import br.com.lfdb.particity.util.FontUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class RecuperarSenhaActivity extends BaseActivity implements View.OnClickListener {

    private EditText campoEmail;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);

        ((TextView) findViewById(R.id.esqueceuSenha)).setTypeface(FontUtils.getLight(this));

        TextView botaoVoltar = (TextView) findViewById(R.id.botaoVoltar);
        botaoVoltar.setTypeface(FontUtils.getRegular(this));
        botaoVoltar.setOnClickListener(v -> finish());

        TextView botaoEnviar = (TextView) findViewById(R.id.botaoEnviar);
        botaoEnviar.setTypeface(FontUtils.getRegular(this));
        botaoEnviar.setOnClickListener(this);

        campoEmail = (EditText) findViewById(R.id.campoEmail);
        campoEmail.setTypeface(FontUtils.getLight(this));
    }

    @Override public void onClick(View v) {
        new Tasker().execute();
    }

    @Override protected String getScreenName() {
        return "Recuperar Senha";
    }

    public class Tasker extends AsyncTask<Void, Void, String> {

        private ProgressDialog dialog;

        @Override protected void onPreExecute() {
            dialog = new ProgressDialog(RecuperarSenhaActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setIndeterminate(true);
            dialog.setMessage("Por favor, aguarde...");
            dialog.show();
        }

        @Override protected String doInBackground(Void... params) {
            try {
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                        String.format("{\"email\":\"%s\"", campoEmail.getText().toString().trim()));
                Request request =
                        new Request.Builder().addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                                .url(Constantes.REST_URL + "/recover_password")
                                .put(body)
                                .build();
                Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                }
            } catch (Exception e) {
                Log.e("ZUP", e.getMessage(), e);
            }
            return null;
        }

        @Override protected void onPostExecute(String result) {
            dialog.dismiss();
            if (result != null) {
                Toast.makeText(RecuperarSenhaActivity.this,
                        "Solicitação enviada com sucesso. Verifique seu e-mail", Toast.LENGTH_LONG).show();
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                Toast.makeText(RecuperarSenhaActivity.this, "Falha no envio da solicitação",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
