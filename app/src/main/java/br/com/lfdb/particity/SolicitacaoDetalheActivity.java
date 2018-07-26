package br.com.lfdb.particity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import br.com.lfdb.particity.base.BaseActivity;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.core.ConstantesBase;
import br.com.lfdb.particity.domain.ComentarioRelato;
import br.com.lfdb.particity.domain.SolicitacaoListItem;
import br.com.lfdb.particity.fragment.ExploreFragment_;
import br.com.lfdb.particity.service.LoginService;
import br.com.lfdb.particity.util.AuthHelper;
import br.com.lfdb.particity.util.DateUtils;
import br.com.lfdb.particity.util.FontUtils;
import br.com.lfdb.particity.util.ImageUtils;
import br.com.lfdb.particity.widget.ImagePagerAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.PageIndicator;
import in.uncod.android.bypass.Bypass;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SolicitacaoDetalheActivity extends BaseActivity implements OnMapReadyCallback {

    @Bind(R.id.categoryIcon) ImageView categoryIcon;
    @Bind(R.id.categoryName) TextView categoryName;
    @Bind(R.id.subcategoryName) TextView subcategoryName;

    @Bind(R.id.comment_container) ViewGroup commentContainer;

    SolicitacaoListItem solicitacao;
    Bypass mBypass;

    @SuppressWarnings("deprecation") @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitacao_detalhe);
        ButterKnife.bind(this);

        solicitacao = (SolicitacaoListItem) getIntent().getExtras().getSerializable("solicitacao");
        boolean alterarLabel = getIntent().getExtras().getBoolean("alterar_botao", false);
        mBypass = new Bypass();

        TextView protocolo = (TextView) findViewById(R.id.protocolo);
        if (solicitacao.getProtocolo() == null || solicitacao.getProtocolo().equalsIgnoreCase("null")) {
            protocolo.setVisibility(View.GONE);
        } else {
            protocolo.setText(getString(R.string.protocolo) + " " + solicitacao.getProtocolo());
            protocolo.setTypeface(FontUtils.getBold(this));
        }

        TextView endereco = (TextView) findViewById(R.id.endereco);
        endereco.setText(solicitacao.getEndereco());
        endereco.setTypeface(FontUtils.getLight(this));

        TextView referencia = (TextView) findViewById(R.id.referencia);
        referencia.setText(solicitacao.getReferencia());
        referencia.setTypeface(FontUtils.getLight(this));

        TextView data = (TextView) findViewById(R.id.data);
        data.setText(getString(R.string.enviada) + " " + solicitacao.getData());
        data.setTypeface(FontUtils.getBold(this));

        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        if (solicitacao.getFotos().isEmpty()) {
            mPager.setVisibility(View.GONE);
            findViewById(R.id.indicator).setVisibility(View.GONE);

            if (checkPlayServices()) {
                ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
            } else {
                Toast.makeText(this,
                        "Necessitamos saber da sua localização. Por favor, autorize nas configurações do seu aparelho.",
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            findViewById(R.id.map).setVisibility(View.GONE);
            ImagePagerAdapter mAdapter =
                    new ImagePagerAdapter(getSupportFragmentManager(), solicitacao.getFotos());
            mPager.setAdapter(mAdapter);
            PageIndicator mIndicator = (IconPageIndicator) findViewById(R.id.indicator);
            mIndicator.setViewPager(mPager);
        }

        TextView comentario = (TextView) findViewById(R.id.comentario);
        comentario.setTypeface(FontUtils.getRegular(this));
        comentario.setText(solicitacao.getComentario());

        TextView botaoVoltar = (TextView) findViewById(R.id.botaoVoltar);
        if (alterarLabel) {
            botaoVoltar.setText(R.string.solicitaces_maiusculo);
        }
        botaoVoltar.setTypeface(FontUtils.getRegular(this));
        botaoVoltar.setOnClickListener(v -> finish());

        TextView indicadorStatus = (TextView) findViewById(R.id.indicadorStatus);
        indicadorStatus.setTypeface(FontUtils.getBold(this));
        int fiveDp = (int) ImageUtils.dpToPx(this, 5);
        int tenDp = (int) ImageUtils.dpToPx(this, 10);
        indicadorStatus.setPadding(tenDp, fiveDp, tenDp, fiveDp);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            indicadorStatus.setBackgroundDrawable(
                    ImageUtils.getStatusBackground(this, solicitacao.getStatus().getCor()));
        } else {
            indicadorStatus.setBackground(
                    ImageUtils.getStatusBackground(this, solicitacao.getStatus().getCor()));
        }
        indicadorStatus.setText(solicitacao.getStatus().getNome());

        if (solicitacao.getCategoria().getCategoriaMae() != null) {
            categoryIcon.setImageBitmap(ImageUtils.getScaledCustom(this, "reports",
                    solicitacao.getCategoria().getCategoriaMae().getIconeAtivo(), 0.75f));
            categoryName.setText(solicitacao.getCategoria().getNome());
            subcategoryName.setText(solicitacao.getCategoria().getCategoriaMae().getNome());
        } else {
            categoryIcon.setImageBitmap(
                    ImageUtils.getScaledCustom(this, "reports", solicitacao.getCategoria().getIconeAtivo(),
                            0.75f));
            categoryName.setText(solicitacao.getCategoria().getNome());
            subcategoryName.setVisibility(View.GONE);
        }

        loadComments();
    }

    private boolean checkPlayServices() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
           /* ActivityCompat.requestPermissions(this,
                    new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    ExploreFragment_.MY_PERMISSIONS_REQUEST_READ_CONTACTS);*/
            return false;
        }
        return true;
    }

    @Override protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void loadComments() {
        new Thread(() -> {
            try {
                Request request =
                        new Request.Builder().addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT).addHeader("X-App-Token", new LoginService().getToken(this))
                                .url(Constantes.REST_URL + String.format("/reports/%d/comments",
                                        solicitacao.getId()))
                                .build();
                Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
                if (response.isSuccessful()) {
                    String raw = response.body().string();
                    fillData(new JSONObject(raw).getJSONArray("comments"));
                } else if (response.code() == 401) {
                    AuthHelper.redirectSessionExpired(getApplicationContext());
                }
            } catch (Exception e) {
                Log.e("ZUP", "Couldn't retrieve comments", e);
                if (!BuildConfig.DEBUG) Crashlytics.logException(e);
            }
        }).start();
    }

    private void fillData(JSONArray list) throws Exception {
        final List<ComentarioRelato> comentarios = new ArrayList<>();
        for (int i = 0; i < list.length(); i++) {
            JSONObject json = list.getJSONObject(i);
            if (json.getInt("visibility") == 0 || (json.getInt("visibility") == 1
                    && new LoginService().getUserId(this) == solicitacao.getCreatorId())) {
                ComentarioRelato comentario = new ComentarioRelato();
                comentario.setMensagem(json.getString("message"));
                comentario.setCriacao(new DateTime(json.getString("created_at")).toDate());
                comentarios.add(comentario);
            }
        }

        runOnUiThread(() -> buildUi(comentarios));
    }

    private void buildUi(List<ComentarioRelato> comentarios) {
        for (ComentarioRelato comentario : comentarios) {
            View view =
                    getLayoutInflater().inflate(R.layout.item_comentario_relato, commentContainer, false);
            TextView message = ButterKnife.findById(view, R.id.message);

            CharSequence commentText =
                    mBypass.markdownToSpannable(transformLinksIntoMarkdownLinks(comentario.getMensagem()));
            message.setText(commentText);
            message.setMovementMethod(LinkMovementMethod.getInstance());

            TextView autor = ButterKnife.findById(view, R.id.author);
            autor.setText(String.format("Resposta do município enviada: %s",
                    DateUtils.formatDate(this, comentario.getCriacao())));
            commentContainer.addView(view);
        }
    }

    private String transformLinksIntoMarkdownLinks(String message) {
        String formattedMessage = message;
        Pattern urlPattern = Pattern.compile(
                "(https?:\\/\\/|)(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)($|\\s)",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

        Matcher matcher = urlPattern.matcher(message);
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            String url = message.substring(matchStart, matchEnd).trim();
            String formattedUrl = "[" + url + "](" + url + ")";
            formattedMessage = formattedMessage.replace(url, formattedUrl);
        }

        return formattedMessage;
    }

    @Override protected String getScreenName() {
        return "Detalhes de um Relato";
    }

    @Override public void onMapReady(GoogleMap map) {

        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);

        if (solicitacao == null) {
            return;
        }
        CameraPosition p = new CameraPosition.Builder().target(
                new LatLng(solicitacao.getLatitude(), solicitacao.getLongitude())).zoom(15).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
        map.moveCamera(update);
        try {
            MarkerOptions marker = new MarkerOptions();
            LatLng latLong = new LatLng(solicitacao.getLatitude(), solicitacao.getLongitude());
            marker.position(latLong);
            map.addMarker(marker);
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
