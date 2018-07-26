package br.com.lfdb.particity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import br.com.lfdb.particity.core.Crashlytics;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.com.lfdb.particity.base.BaseActivity;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.core.ConstantesBase;
import br.com.lfdb.particity.service.LoginService;
import br.com.lfdb.particity.util.FontUtils;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private TextView botaoEntrar;
    private EditText campoSenha;
    private EditText campoEmail;
    private TextView linkEsqueciSenha;

    LoginService loginService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ((TextView) findViewById(R.id.login)).setTypeface(FontUtils.getLight(this));

        linkEsqueciSenha = (TextView) findViewById(R.id.linkEsqueciSenha);
        linkEsqueciSenha.setTypeface(FontUtils.getBold(this));
        linkEsqueciSenha.setOnClickListener(this);

        TextView botaoCancelar = (TextView) findViewById(R.id.botaoCancelar);
        botaoCancelar.setTypeface(FontUtils.getRegular(this));
        botaoCancelar.setOnClickListener(v -> finish());
        botaoEntrar = (TextView) findViewById(R.id.botaoEntrar);
        botaoEntrar.setTypeface(FontUtils.getRegular(this));
        botaoEntrar.setOnClickListener(this);

        campoSenha = (EditText) findViewById(R.id.campoSenha);
        campoSenha.setTypeface(FontUtils.getLight(this));
        campoSenha.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_GO) {
                new Tasker().execute();
                handled = true;
            }
            return handled;
        });

        campoEmail = (EditText) findViewById(R.id.campoEmail);
        campoEmail.setTypeface(FontUtils.getLight(this));

        loginService = new LoginService();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == linkEsqueciSenha.getId()) {
            startActivity(new Intent(this, RecuperarSenhaActivity.class));
        } else if (v.getId() == botaoEntrar.getId()) {
            new Tasker().execute();
        }
    }

    @Override
    protected String getScreenName() {
        return "Login";
    }

    public class Tasker extends AsyncTask<Void, Void, String> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setIndeterminate(true);
            dialog.setMessage("Por favor, aguarde...");
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(LoginActivity.this);

                try {
                    prefs.edit().putString("gcm", gcm.register(LoginActivity.this.getString(R.string.gcm_project))).apply();
                } catch (Exception e) {
                    Log.e("ZUP", e.getMessage(), e);
                }

                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                        String.format("{\"email\":\"%s\",\"password\":\"%s\",\"device_type\":\"android\",\"device_token\":\"%s\"}",
                                campoEmail.getText().toString().trim(),
                                campoSenha.getText().toString(),
                                PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).getString("gcm", "")));
                Request request = new Request.Builder()
                        .url(Constantes.REST_URL + "/authenticate")
                        .post(body)
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

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    loginService.registrarLogin(LoginActivity.this,
                            json.getJSONObject("user"),
                            json.getString("token"));
                    Crashlytics.setUserIdentifier(
                        String.valueOf(loginService.getUserId(LoginActivity.this)));
                    Crashlytics.setUserEmail(campoEmail.getText().toString().trim());
                } catch (JSONException e) {
                    Log.e("ZUP", e.getMessage(), e);
                }
                Toast.makeText(LoginActivity.this, "Login realizado com sucesso", Toast.LENGTH_LONG).show();
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Falha no login", Toast.LENGTH_LONG).show();
            }
        }
    }
}
