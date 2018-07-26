package br.com.lfdb.particity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.gson.Gson;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.lfdb.particity.api.ZupApi;
import br.com.lfdb.particity.api.model.ReportItem;
import br.com.lfdb.particity.api.model.ReportItemRequest;
import br.com.lfdb.particity.base.BaseActivity;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.domain.CategoriaRelato;
import br.com.lfdb.particity.domain.Solicitacao;
import br.com.lfdb.particity.domain.SolicitacaoListItem;
import br.com.lfdb.particity.fragment.SoliciteDetalhesFragment;
import br.com.lfdb.particity.fragment.SoliciteFotosFragment;
import br.com.lfdb.particity.fragment.SoliciteLocalFragment_;
import br.com.lfdb.particity.fragment.SolicitePontoFragment;
import br.com.lfdb.particity.fragment.SoliciteTipoNovoFragment;
import br.com.lfdb.particity.service.FeatureService;
import br.com.lfdb.particity.service.UsuarioService;
import br.com.lfdb.particity.social.util.SocialUtils;
import br.com.lfdb.particity.util.AuthHelper;
import br.com.lfdb.particity.util.DateUtils;
import br.com.lfdb.particity.util.FontUtils;
import br.com.lfdb.particity.util.NetworkUtils;
import br.com.lfdb.particity.util.ViewUtils;
import retrofit.RetrofitError;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static br.com.lfdb.particity.util.ImageUtils.encodeBase64;

@EActivity(R.layout.activity_solicite) public class SoliciteActivity extends BaseActivity {

  @ViewById TextView titulo;
  @ViewById TextView instrucoes;
  @ViewById TextView botaoVoltar;
  @ViewById TextView botaoAvancar;
  @ViewById TextView botaoCancelar;
  @ViewById RelativeLayout barra_navegacao;

  @InstanceState Bundle savedInstanceState;

  public static final int LOGIN_REQUEST = 1578;

  private Passo atual = Passo.TIPO;
  private Solicitacao solicitacao = new Solicitacao();
  private SoliciteTipoNovoFragment tipoFragment;
  private SoliciteFotosFragment fotosFragment;
  private SoliciteLocalFragment_ localFragment;
  private SolicitePontoFragment pontoFragment;
  private SoliciteDetalhesFragment detalhesFragment;

  private enum Passo {
    TIPO, LOCAL, FOTOS, COMENTARIOS
  }

  @AfterViews void init(){
    if (savedInstanceState != null) {
      solicitacao = (Solicitacao) savedInstanceState.getSerializable("solicitacao");
      atual = new Gson().fromJson(savedInstanceState.getString("passo"), Passo.class);
      restoreFragmentsStates(savedInstanceState);
    } else {
      tipoFragment = new SoliciteTipoNovoFragment();
      getSupportFragmentManager().beginTransaction()
              .add(R.id.fragments_place, tipoFragment)
              .commit();
    }
    botaoAvancar.setTypeface(FontUtils.getRegular(this));
    botaoVoltar.setTypeface(FontUtils.getRegular(this));
    botaoCancelar.setTypeface(FontUtils.getRegular(this));
    titulo.setTypeface(FontUtils.getLight(this));
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSerializable("solicitacao", solicitacao);
    outState.putString("passo", new Gson().toJson(atual));
    if (fotosFragment != null) {
      outState.putString("imagemTemporaria", fotosFragment.getImagemTemporaria());
    }
    outState.putBoolean("tipo", tipoFragment != null);
    outState.putBoolean("fotos", fotosFragment != null);
    outState.putBoolean("local", localFragment != null);
    outState.putBoolean("ponto", pontoFragment != null);
    outState.putBoolean("detalhes", detalhesFragment != null);
  }

  @Click void botaoAvancar() {
    botaoVoltar.setVisibility(View.VISIBLE);
    if (atual.equals(Passo.LOCAL)) {
      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      if (!getCategoria().getCategoriasInventario().isEmpty()) {
        ft.hide(pontoFragment);
        solicitacao.setIdItemInventario(pontoFragment.getCategoriaId());
        solicitacao.setLatitudeLongitude(pontoFragment.getLatitude(), pontoFragment.getLongitude());
      } else {
        if (!localFragment.validarEndereco()) return;
        ft.hide(localFragment);
      }
      if (fotosFragment == null) {
        fotosFragment = new SoliciteFotosFragment();
        ft.add(R.id.fragments_place, fotosFragment).commit();
      } else {
        ft.show(fotosFragment).commit();
      }
      atual = Passo.FOTOS;
    } else if (atual.equals(Passo.FOTOS)) {
      if (detalhesFragment == null) {
        detalhesFragment = new SoliciteDetalhesFragment();
        getSupportFragmentManager().beginTransaction()
                .hide(fotosFragment)
                .add(R.id.fragments_place, detalhesFragment)
                .commit();
      } else {
        getSupportFragmentManager().beginTransaction()
                .hide(fotosFragment)
                .show(detalhesFragment)
                .commit();
      }
      botaoAvancar.setText(R.string.publicar);
      atual = Passo.COMENTARIOS;
    } else if (atual.equals(Passo.COMENTARIOS)) {
      solicitar();
    } else if (atual.equals(Passo.TIPO)) {
      if (!solicitacao.getCategoria().getCategoriasInventario().isEmpty()) {
        if (pontoFragment == null) {
          pontoFragment = new SolicitePontoFragment();
          pontoFragment.setCategoria(solicitacao.getCategoria());
          FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
          if (localFragment != null) {
            ft.remove(localFragment).commit();
            ft = getSupportFragmentManager().beginTransaction();
            localFragment = null;
          }
          ft.add(R.id.fragments_place, pontoFragment).hide(tipoFragment).commitAllowingStateLoss();
        } else {
          getSupportFragmentManager().beginTransaction()
                  .hide(tipoFragment)
                  .show(pontoFragment)
                  .commitAllowingStateLoss();
          pontoFragment.setCategoria(solicitacao.getCategoria());
        }
      } else {
        if (localFragment == null) {
          localFragment = new SoliciteLocalFragment_();
          localFragment.setMarcador(solicitacao.getCategoria().getMarcador());
          FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
          if (pontoFragment != null) {
            ft.remove(pontoFragment).commit();
            ft = getSupportFragmentManager().beginTransaction();
            pontoFragment = null;
          }
          ft.add(R.id.fragments_place, localFragment).hide(tipoFragment).commitAllowingStateLoss();
        } else {
          getSupportFragmentManager().beginTransaction()
                  .hide(tipoFragment)
                  .show(localFragment)
                  .commitAllowingStateLoss();
          localFragment.setMarcador(solicitacao.getCategoria().getMarcador());
        }
      }
      atual = Passo.LOCAL;
    }
  }

  @Click void botaoVoltar() {
    botaoAvancar.setText(R.string.proximo);
    botaoAvancar.setVisibility(View.VISIBLE);
    switch (atual) {
      case COMENTARIOS:
        getSupportFragmentManager().beginTransaction()
                .hide(detalhesFragment)
                .show(fotosFragment)
                .commit();
        atual = Passo.FOTOS;
        break;
      case FOTOS:
        if (!getCategoria().getCategoriasInventario().isEmpty()) {
          getSupportFragmentManager().beginTransaction()
                  .hide(fotosFragment)
                  .show(pontoFragment)
                  .commit();
          atual = Passo.LOCAL;
        } else {
          getSupportFragmentManager().beginTransaction()
                  .hide(fotosFragment)
                  .show(localFragment)
                  .commit();
          atual = Passo.LOCAL;
        }
        break;
      case LOCAL:
        botaoVoltar.setVisibility(View.GONE);
        if (!getCategoria().getCategoriasInventario().isEmpty()) {
          getSupportFragmentManager().beginTransaction()
                  .hide(pontoFragment)
                  .show(tipoFragment)
                  .commit();
        } else {
          getSupportFragmentManager().beginTransaction()
                  .hide(localFragment)
                  .show(tipoFragment)
                  .commit();
        }
        atual = Passo.TIPO;
        break;
    }
  }

  @Click void botaoCancelar() {
    setResult(Activity.RESULT_CANCELED);
    finish();
  }

  @UiThread public void exibirBarraInferior(boolean exibir) {
    barra_navegacao.setVisibility(exibir ? View.VISIBLE : View.GONE);
  }

  public ArrayList<String> getFotos() {
    return solicitacao.getFotos();
  }

  @UiThread public void enableNextButton(boolean enabled) {
    botaoAvancar.setVisibility(enabled ? View.VISIBLE : View.GONE);
  }

  public CategoriaRelato getCategoria() {
    return solicitacao.getCategoria();
  }

  @UiThread public void setCategoria(CategoriaRelato categoria) {
    solicitacao.setCategoria(categoria);
    if (new UsuarioService().getUsuarioAtivo(this) == null) {
      startActivityForResult(new Intent(this, WarningActivity.class), LOGIN_REQUEST);
      return;
    }
    exibirBarraInferior(true);
  }

  @UiThread public void setInfo(int string) {
    instrucoes.setText(string);
    instrucoes.setTypeface(FontUtils.getBold(this));
    instrucoes.setText(instrucoes.getText().toString().toUpperCase(Locale.US));
  }

  @UiThread public void adicionarFoto(String foto) {
    solicitacao.adicionarFoto(foto);
  }

  @UiThread public void removerFoto(String foto) {
    solicitacao.removerFoto(foto);
  }

  @UiThread public void solicitar() {
    solicitacao.setComentario(detalhesFragment.getComentario());
    if (solicitacao.getComentario().length() > 800) {
      alertarTamanhoComentario();
      return;
    }
    if (solicitacao.getCategoria().getCategoriasInventario().isEmpty()) {
      solicitacao.setLatitudeLongitude(localFragment.getLatitudeAtual(),
              localFragment.getLongitudeAtual());
    } else {
      if (solicitacao.getIdItemInventario() == null) {
        alertarItemInventario();
        return;
      }
      solicitacao.setIdItemInventario(pontoFragment.getCategoriaId());
    }
    enviarSolicitacao();
  }

  @UiThread void alertarItemInventario() {
    new AlertDialog.Builder(this).setMessage("O local do relato não foi selecionado corretamente")
            .setNeutralButton("OK", (dialog, which) -> dialog.dismiss())
            .show();
  }

  @UiThread void alertarTamanhoComentario() {
    new AlertDialog.Builder(this).setMessage("O comentário deve ter menos de 800 caracteres")
            .setNeutralButton("OK", (dialog, which) -> dialog.dismiss())
            .show();
  }

  @UiThread void enviarSolicitacao() {
    if (!NetworkUtils.isInternetPresent(this)) {
      new AlertDialog.Builder(this).setMessage(getString(R.string.no_connection))
              .setNeutralButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss())
              .show();
      return;
    }
    ProgressDialog dialog = new ProgressDialog(this);
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    dialog.setIndeterminate(true);
    dialog.setMessage(getString(R.string.sending_request));
    dialog.setCancelable(false);
    dialog.show();
    tasker(dialog);
  }

  public String getReferencia() {
    return solicitacao.getReferencia() != null ? solicitacao.getReferencia() : "";
  }

  public void setReferencia(String referencia) {
    solicitacao.setReferencia(referencia);
  }

  @UiThread public void assertFragmentVisibility() {
    getSupportFragmentManager().beginTransaction()
            .hide(localFragment != null ? localFragment : pontoFragment)
            .show(fotosFragment)
            .commitAllowingStateLoss();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (data == null){
      return;
    }
    if (requestCode == LOGIN_REQUEST && resultCode == Activity.RESULT_OK) {
      setCategoria(solicitacao.getCategoria());
    }
  }

  private SolicitacaoListItem getSolicitacao(String retorno) throws Exception {
    SolicitacaoListItem item = new SolicitacaoListItem();
    JSONObject json = new JSONObject(retorno).getJSONObject("report");
    item.setComentario(solicitacao.getComentario());
    item.setData(DateUtils.getIntervaloTempo(new Date()));
    item.setFotos(new ArrayList<>());
    JSONArray fotos = json.getJSONArray("images");
    for (int j = 0; j < fotos.length(); j++) {
      item.getFotos()
              .add(ViewUtils.isMdpiOrLdpi(this) ? fotos.getJSONObject(j).getString("low")
                      : fotos.getJSONObject(j).getString("high"));
    }
    item.setProtocolo(json.optString("protocol", null));
    item.setStatus(new SolicitacaoListItem.Status(json.getJSONObject("status").getString("title"),
            json.getJSONObject("status").getString("color")));
    item.setTitulo(json.getJSONObject("category").getString("title"));
    item.setCategoria(solicitacao.getCategoria());
    item.setLatitude(solicitacao.getLatitude());
    item.setLongitude(solicitacao.getLongitude());
    item.setEndereco(solicitacao.getEndereco());
    item.setReferencia(solicitacao.getReferencia());
    item.setId(json.optLong("id"));
    return item;
  }

  @UiThread void restoreFragmentsStates(Bundle bundle) {
    Bundle params = new Bundle();
    params.putSerializable("solicitacao", solicitacao);
    params.putString("imagemTemporaria", bundle.getString("imagemTemporaria"));

    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

    if (bundle.getBoolean("tipo")) {
      tipoFragment = new SoliciteTipoNovoFragment();
      tipoFragment.setArguments(params);
      ft.add(R.id.fragments_place, tipoFragment);

      if (atual != Passo.TIPO) {
        ft.hide(tipoFragment);
      }
    }

    if (bundle.getBoolean("fotos")) {
      fotosFragment = new SoliciteFotosFragment();
      fotosFragment.setArguments(params);
      ft.add(R.id.fragments_place, fotosFragment);

      if (atual != Passo.FOTOS) {
        ft.hide(fotosFragment);
      }
    }

    if (bundle.getBoolean("local")) {
      localFragment = new SoliciteLocalFragment_();
      localFragment.setArguments(params);
      ft.add(R.id.fragments_place, localFragment);

      if (atual != Passo.LOCAL) {
        ft.hide(localFragment);
      }
    }

    if (bundle.getBoolean("detalhes")) {
      detalhesFragment = new SoliciteDetalhesFragment();
      detalhesFragment.setArguments(params);
      ft.add(R.id.fragments_place, detalhesFragment);

      if (atual != Passo.COMENTARIOS) {
        ft.hide(detalhesFragment);
      }
    }

    if (bundle.getBoolean("ponto")) {
      pontoFragment = new SolicitePontoFragment();
      pontoFragment.setArguments(params);
      pontoFragment.setCategoria(solicitacao.getCategoria());
      ft.add(R.id.fragments_place, pontoFragment);

      if (atual != Passo.LOCAL) {
        ft.hide(pontoFragment);
      }
    }

    ft.commitAllowingStateLoss();
  }

  @Override protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
  }

  private void setAddress(ReportItemRequest item, Address address, String street, String number) {
    item.setAddress(Strings.emptyToNull(street));
    item.setNumber(Strings.emptyToNull(number));
    item.setDistrict(Strings.emptyToNull(address.getSubLocality()));
    item.setCity(Strings.emptyToNull(getCity(address)));
    item.setState(Strings.emptyToNull(address.getAdminArea()));
    item.setCountry(Strings.emptyToNull(address.getCountryName()));
    item.setPostalCode(Strings.emptyToNull(address.getPostalCode()));
  }

  private void setAddress(ReportItemRequest item, Address address) {
    setAddress(item, address, address.getThoroughfare(), address.getFeatureName());
  }

  private String getCity(Address address) {
    return address.getSubAdminArea() != null ? address.getSubAdminArea() : address.getLocality();
  }

  @Override protected String getScreenName() {
    return getString(R.string.create_report);
  }

  @Background void tasker(ProgressDialog dialog) {
    try {
      Thread.sleep(2000);
      ReportItemRequest item = new ReportItemRequest();
      ReportItem response = null;

      item.setDescription(solicitacao.getComentario().trim());
      if (solicitacao.getCategoria().getCategoriasInventario().isEmpty()) {
        item.setLatitude(String.valueOf(solicitacao.getLatitude()));
        item.setLongitude(String.valueOf(solicitacao.getLongitude()));
        if (solicitacao.getReferencia() != null && !solicitacao.getReferencia().trim().isEmpty()) {
          item.setReference(solicitacao.getReferencia());
        }
        setAddress(item, localFragment.getRawAddress(), localFragment.getStreet(),
                localFragment.getNumber());
      } else {
        item.setInventoryItemId(solicitacao.getIdItemInventario());
        setAddress(item, pontoFragment.getAddress());
        solicitacao.setLatitudeLongitude(pontoFragment.getLatitude(), pontoFragment.getLongitude());
      }
      item.setCategoryId(solicitacao.getCategoria().getId());
      item.setResolutionTime(solicitacao.getCategoria().getTempoResolucao());
      List<String> images = new ArrayList<>();
      for (String foto : solicitacao.getFotos()) {
        images.add(encodeBase64(foto));
      }
      item.setImages(images);
      response = ZupApi.get(this).createReport(item.getCategoryId(), item).getReport();
      if (detalhesFragment.getPublicar()) {
        SocialUtils.post(SoliciteActivity.this,
                "Estou colaborando com a minha cidade, reportando problemas e solicitações.\n" +
                        Constantes.WEBSITE_URL + "/" + item.getId() + "\n#ZeladoriaUrbana");
      }
      dialog.dismiss();
      if (item == null) {
        toast("Falha no envio da solicitação");
      }
      showAlertDialog(item, response);
    } catch (Throwable error) {
      Log.e("ZUP", error.toString());
      if (error instanceof RetrofitError) {
        RetrofitError retrofitError = (RetrofitError) error;
        if (retrofitError.getResponse().getStatus() == 401) {
          AuthHelper.redirectSessionExpired(getApplicationContext());
        } else {
 /*         CreateReportError createReportError =
                  (CreateReportError) retrofitError.getBodyAs(CreateReportError.class);
          runOnUiThread(new Runnable() {
            @Override public void run() {
              Toast.makeText(SoliciteActivity.this, createReportError.getError(), Toast.LENGTH_SHORT).show();
              dialog.dismiss();
            }
          });
*/

        }
      }
    }
  }

  @UiThread void toast(String msg) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
  }

  @UiThread void showAlertDialog(ReportItemRequest item, ReportItem response) {
    StringBuilder message = new StringBuilder();
    message.append(getString(R.string.dialog_info));
    message.append(getString(R.string.line_separator));
    message.append(getString(R.string.note_protocol));
    message.append(response.getProtocol());
    message.append(getSolutionDue(item));
    new AlertDialog.Builder(this).setTitle(getString(R.string.request_sent))
            .setMessage(message)
            .setNeutralButton(getString(R.string.ok), (dialog1, which) -> {
              Intent i = new Intent(this, SolicitacaoDetalheActivity.class);
              i.putExtra("solicitacao", response.compat(this));
              startActivity(i);
              setResult(Activity.RESULT_OK);
              finish();
            })
            .setCancelable(false)
            .show();
  }

  private String getSolutionDue(ReportItemRequest item) {
    StringBuilder message = new StringBuilder();
    if (FeatureService.getInstance(this).isShowResolutionTimeToClientsEnabled()
            && solicitacao.getCategoria().isTempoResolucaoAtivado()
            && !solicitacao.getCategoria().isTempoResolucaoPrivado()) {
      message.append(getString(R.string.line_separator));
      message.append(getString(R.string.solution_due));
      message.append(DateUtils.getString(item.getResolutionTime()));
      return message.toString();
    }
    return "";
  }
}