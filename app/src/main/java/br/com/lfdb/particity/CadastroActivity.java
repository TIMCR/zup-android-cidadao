package br.com.lfdb.particity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import br.com.lfdb.particity.base.BaseActivity;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.core.ConstantesBase;
import br.com.lfdb.particity.domain.Usuario;
import br.com.lfdb.particity.service.FeatureService;
import br.com.lfdb.particity.service.UsuarioService;
import br.com.lfdb.particity.util.FontUtils;
import br.com.lfdb.particity.util.ToastHelper;
import br.com.lfdb.particity.validador.CpfValidador;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

@EActivity(R.layout.activity_cadastro) public class CadastroActivity extends BaseActivity {

  private static final int REQUEST_SOCIAL = 9876;

  @ViewById EditText nameField;
  @ViewById EditText passField;
  @ViewById EditText emailField;
  @ViewById EditText confirmPassField;
  @ViewById EditText documentField;
  @ViewById EditText phoneField;
  @ViewById EditText addressField;
  @ViewById EditText complField;
  @ViewById EditText cepField;
  @ViewById EditText neighborhoodField;
  @ViewById EditText cityField;
  @ViewById TextView addAccount;
  @ViewById TextView btnCancel;
  @ViewById TextView btnCreate;
  @ViewById TextView terms;

  List<EditText> listToValidate;
  ToastHelper toast;
  List<Integer> campos;

  @AfterViews void init() {
    toast = new ToastHelper();
    loadTypeface();
    loadTerms();
    cityField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_GO) {
          registerIsValid();
          handled = true;
        }
        return handled;
      }
    });
    loadListToValidate();
  }

  @UiThread void loadListToValidate() {
    listToValidate = new ArrayList<>();
    listToValidate.add(nameField);
    listToValidate.add(emailField);
    listToValidate.add(documentField);
    listToValidate.add(phoneField);
    listToValidate.add(addressField);
    listToValidate.add(cepField);
    listToValidate.add(neighborhoodField);
    listToValidate.add(passField);
    listToValidate.add(confirmPassField);
    listToValidate.add(cityField);
  }

  @Click void btnCancel() {
    finish();
  }

  @Click void btnCreate() {
    registerIsValid();
  }

  @Click void terms() {
    startActivity(new Intent(this, TermosDeUsoActivity.class));
  }

  @UiThread void loadTypeface() {
    btnCancel.setTypeface(FontUtils.getRegular(this));
    btnCreate.setTypeface(FontUtils.getRegular(this));
    addAccount.setTypeface(FontUtils.getLight(this));
    terms.setTypeface(FontUtils.getLight(this));
  }

  @UiThread void loadTerms() {
    terms.setText(Html.fromHtml(getString(R.string.termos_de_uso_cadastro)));
  }

  @UiThread void registerIsValid() {
    clear();
    isValid();
    if (campos != null && !campos.isEmpty()) {
      focusLabels(campos);
      return;
    }
    if (FeatureService.getInstance(this).isAnySocialEnabled()) {
      startActivityForResult(new Intent(this, RedesSociaisCadastroActivity.class), REQUEST_SOCIAL);
      return;
    }
    register();
  }

  void isValid() {
    campos = new ArrayList<>();
    String pass = passField.getText().toString().trim();
    String confirmPass = confirmPassField.getText().toString().trim();
    if (pass.isEmpty() || confirmPass.isEmpty() || !pass.equals(confirmPass)) {
      campos.add(passField.getId());
      campos.add(confirmPassField.getId());
    } else if (pass.length() < 6) {
      campos.add(passField.getId());
      toast.show(this, getString(R.string.pass_length_message), Toast.LENGTH_LONG);
    }
    add();
    String document = documentField.getText().toString().trim();
    if (!CpfValidador.isValid(document.replace("-", "").replace(".", ""))) {
      campos.add(documentField.getId());
    }
  }

  @UiThread void verifyFields(List<Integer> campos) {
    for (EditText et : listToValidate) {
      if (et.getText().toString().trim().isEmpty()) {
        campos.add(et.getId());
      }
    }
  }

  List<Integer> getFields() {
    return Arrays.asList(R.id.nameField, R.id.emailField, R.id.documentField, R.id.phoneField,
            R.id.addressField, R.id.cepField, R.id.neighborhoodField, R.id.cityField);
  }

  void add() {
    for (Integer id : getFields()) {
      if (((TextView) findViewById(id)).getText().toString().trim().isEmpty()) {
        campos.add(id);
      }
    }
  }

  @UiThread void register() {
    Usuario usuario = new Usuario();
    usuario.setBairro(neighborhoodField.getText().toString());
    usuario.setCep(cepField.getText().toString());
    usuario.setComplemento(complField.getText().toString());
    usuario.setCpf(documentField.getText().toString());
    usuario.setEmail(emailField.getText().toString());
    usuario.setEndereco(addressField.getText().toString());
    usuario.setNome(nameField.getText().toString());
    usuario.setTelefone(phoneField.getText().toString());
    usuario.setSenha(passField.getText().toString());
    usuario.setConfirmacaoSenha(confirmPassField.getText().toString());
    usuario.setCidade(cityField.getText().toString().trim());
    if (isFinishing()) {
      return;
    }
    ProgressDialog dialog = new ProgressDialog(CadastroActivity.this);
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    dialog.setIndeterminate(true);
    dialog.setMessage(getString(R.string.waiting_message));
    dialog.show();
    tasker(dialog, usuario);
  }

  @UiThread void focusLabels(List<Integer> campos) {
    for (Integer id : campos) {
      findViewById(id).setBackgroundResource(R.drawable.textbox_red);
    }
    toast.show(this, getString(R.string.review_fields_message), Toast.LENGTH_LONG);
  }

  void clear() {
    for (Integer id : getFields()) {
      findViewById(id).setBackgroundResource(R.drawable.textbox_bg);
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_SOCIAL && resultCode == Activity.RESULT_OK) {
      register();
    } else if (requestCode == OpeningActivity.LOGIN_REQUEST) {
      setResult(resultCode);
      finish();
    }
  }

  @Override protected String getScreenName() {
    return getString(R.string.cadastro);
  }

  @Background void tasker(ProgressDialog dialog, Usuario usuario) {
    String result = null;
    try {
      SharedPreferences prefs =
              PreferenceManager.getDefaultSharedPreferences(CadastroActivity.this);
      GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(CadastroActivity.this);
      prefs.edit()
              .putString("gcm", gcm.register(CadastroActivity.this.getString(R.string.gcm_project)))
              .apply();

      RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
              new UsuarioService().converterParaJSON(usuario).toString());
      Request request =
              new Request.Builder().addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                      .url(Constantes.REST_URL + "/users")
                      .post(body)
                      .build();
      Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
      if (response.isSuccessful()) {
        body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                new UsuarioService().loginData(usuario.getEmail(), usuario.getSenha(), PreferenceManager
                        .getDefaultSharedPreferences(CadastroActivity.this)
                        .getString("gcm", "")));
        request = new Request.Builder().addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                .url(Constantes.REST_URL + "/authenticate")
                .post(body)
                .build();
        response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
        if (response.isSuccessful()) {
          result = response.body().string();
        } else {
          Log.e("Error!", response.body().toString());
        }
      } else {
        JSONObject jsonObject = new JSONObject(response.body().string());
        jsonObject = jsonObject.getJSONObject("error");
        List<Integer> fields = new ArrayList<>();
        if (jsonObject.has("document")) {
          fields.add(R.id.documentField);
        }
        if (jsonObject.has("email")) {
          fields.add(R.id.emailField);
        }
        if (jsonObject.has("district")) {
          fields.add(R.id.documentField);
        }
        if (jsonObject.has("postal_code")) {
          fields.add(R.id.cepField);
        }
        if (jsonObject.has("address")) {
          fields.add(R.id.addressField);
        }
        if (jsonObject.has("phone")) {
          fields.add(R.id.phoneField);
        }
        if (jsonObject.has("city")) {
          fields.add(R.id.cityField);
        }
        focusLabels(fields);
      }
      dialog.dismiss();
    } catch (Exception e) {
      Log.e("ZUP Register error", e.getMessage());
      Crashlytics.logException(e);
    } finally {
      final String finalResult = result;
      runOnUiThread(new Runnable() {
        @Override public void run() {
          if (finalResult != null) {
            Toast.makeText(CadastroActivity.this, R.string.register_success, Toast.LENGTH_LONG)
                    .show();
            Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
            intent.putExtra("login", emailField.getText().toString().trim());
            intent.putExtra("pass", passField.getText().toString());
            startActivityForResult(intent, OpeningActivity.LOGIN_REQUEST);
          }
        }
      });
    }
  }
}
