package br.com.lfdb.particity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.lfdb.particity.base.BaseActivity;
import br.com.lfdb.particity.domain.BuscaExplore;
import br.com.lfdb.particity.domain.CategoriaInventario;
import br.com.lfdb.particity.domain.CategoriaRelato;
import br.com.lfdb.particity.domain.Periodo;
import br.com.lfdb.particity.service.CategoriaInventarioService;
import br.com.lfdb.particity.service.CategoriaRelatoService;
import br.com.lfdb.particity.util.FontUtils;
import br.com.lfdb.particity.util.ImageUtils;
import br.com.lfdb.particity.view.CategoryPicker;
import br.com.lfdb.particity.widget.SeekbarWithIntervals;
import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FiltroExploreNovoActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

    @Bind(R.id.formCategorias)
    CategoryPicker formCategorias;
    @Bind(R.id.formInventario)
    View formInventario;
    @Bind(R.id.formPeriodo)
    View formPeriodo;

    @Bind(R.id.seletorInventario)
    TextView seletorInventario;
    @Bind(R.id.seletorPeriodo)
    TextView seletorPeriodo;
    @Bind(R.id.seletorCategorias)
    TextView seletorCategorias;

    @Bind(R.id.inventarioContainer)
    LinearLayout inventarioContainer;

    @Bind(R.id.opcoes)
    LinearLayout opcoes;
    @Bind(R.id.status)
    TextView status;

    @Bind(R.id.seekBar)
    SeekbarWithIntervals seekBar;

    private BuscaExplore busca;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro_explore_novo);
        ButterKnife.bind(this);

        busca = (BuscaExplore) getIntent().getSerializableExtra("busca");
        if (busca == null) busca = new BuscaExplore();

        seekBar.setOnSeekBarChangeListener(this);

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Aguarde...");
        dialog.show();

        new Thread(() -> {
            preencherInventario();
            aplicarFiltroInicial();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            popularListStatus();

            runOnUiThread(dialog::dismiss);
        }).start();
    }

    private void preencherInventario() {
        List<CategoriaInventario> categorias = new CategoriaInventarioService().getCategorias(this);
        for (final CategoriaInventario categoria : categorias) {
            View view = getLayoutInflater().inflate(R.layout.item_expandable_category, inventarioContainer, false);
            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);
            view.setTag(categoria);

            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);

            Bitmap bitmap;
            if (!busca.getIdsCategoriaInventario().contains(categoria.getId())) {
                bitmap = ImageUtils.getScaledCustom(this, "inventory", categoria.getIconeInativo(), 0.75f);
                if (bitmap == null) bitmap = ImageUtils.loadDefaultIcon(this, false, 0.75f);
                imagem.setImageBitmap(bitmap);
                nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                bitmap = ImageUtils.getScaledCustom(this, "inventory", categoria.getIconeAtivo(), 0.75f);
                if (bitmap == null) bitmap = ImageUtils.loadDefaultIcon(this, true, 0.75f);
                imagem.setImageBitmap(bitmap);
            }

            nomeCategoria.setText(categoria.getNome());
            nomeCategoria.setOnClickListener(v -> {
                desmarcarCategoriasInventario();
                Bitmap bitmap1;
                if (busca.getIdsCategoriaInventario().contains(categoria.getId())) {
                    busca.getIdsCategoriaInventario().remove(categoria.getId());
                    nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    bitmap1 = ImageUtils.getScaledCustom(this, "inventory", categoria.getIconeInativo(), 0.75f);
                    if (bitmap1 == null) bitmap1 = ImageUtils.loadDefaultIcon(this, false, 0.75f);
                    imagem.setImageBitmap(bitmap1);
                } else {
                    busca.getIdsCategoriaInventario().add(categoria.getId());
                    nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
                    bitmap1 = ImageUtils.getScaledCustom(this, "inventory", categoria.getIconeAtivo(), 0.75f);
                    if (bitmap1 == null) bitmap1 = ImageUtils.loadDefaultIcon(this, true, 0.75f);
                    imagem.setImageBitmap(bitmap1);
                }

                desmarcarCategoriasRelato();
            });

            ButterKnife.findById(view, R.id.expander).setVisibility(View.GONE);
            ButterKnife.findById(view, R.id.subcategorias).setVisibility(View.GONE);

            inventarioContainer.addView(view);
        }
    }

    private void desmarcarCategoriasRelato() {
        formCategorias.removeAll();
        popularListStatus();
    }

    private void desmarcarCategoriasInventario() {
        busca.getIdsCategoriaInventario().clear();

        for (int i = 0; i < inventarioContainer.getChildCount(); i++) {
            View view = inventarioContainer.getChildAt(i);
            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);
            nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);
            imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "inventory", ((CategoriaInventario) view.getTag()).getIconeInativo(), 0.75f));

            ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);
            for (int j = 0; j < subcategorias.getChildCount(); j++) {
                View v = subcategorias.getChildAt(j);
                TextView nome = ButterKnife.findById(v, R.id.nome);
                nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }
    }

    private void popularListStatus() {
        CategoriaRelatoService service = new CategoriaRelatoService();
        List<CategoriaRelato.Status> status = new ArrayList<>();
        for (long id : formCategorias.getSelectedCategories()) {
            status.addAll(service.getStatus(this, id));
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        opcoes.removeAllViews();

        TextView tv = (TextView) inflater.inflate(R.layout.status_textview, opcoes, false);
        tv.setTypeface(FontUtils.getLight(this));
        tv.setText("Todos os status");
        tv.setOnClickListener(cliqueOpcao);
        opcoes.addView(tv);

        // Verificando se o status atualmente selecionado não foi removido
        if (busca.getStatus() != null && !status.contains(busca.getStatus())) {
            this.status.setText("Todos os status");
            this.status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.seta_expandir, 0);
            opcoes.setVisibility(View.GONE);
            busca.setStatus(null);
        }

        Set<Long> ids = new HashSet<>();
        for (CategoriaRelato.Status s : status) {
            if (!ids.contains(s.getId())) {
                tv = (TextView) inflater.inflate(R.layout.status_textview, opcoes, false);
                tv.setTypeface(FontUtils.getLight(this));
                tv.setTag(s);
                tv.setText(s.getNome());
                tv.setOnClickListener(cliqueOpcao);
                if (s.equals(busca.getStatus())) {
                    tv.setTextColor(Color.parseColor("#2ab4dc"));
                }
                opcoes.addView(tv);
                ids.add(s.getId());
            }
        }

        if (busca.getStatus() == null) {
            ((TextView) opcoes.getChildAt(0)).setTextColor(Color.parseColor("#2ab4dc"));
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @OnClick(R.id.seletorCategorias)
    public void selecinarCategorias() {
        formCategorias.setVisibility(View.VISIBLE);
        formInventario.setVisibility(View.GONE);
        formPeriodo.setVisibility(View.GONE);

        seletorInventario.setTextColor(Color.parseColor("#808080"));
        seletorPeriodo.setTextColor(Color.parseColor("#808080"));
        seletorCategorias.setTextColor(Color.WHITE);
    }

    @OnClick(R.id.seletorPeriodo)
    public void selecionarPeriodo() {
        formCategorias.setVisibility(View.GONE);
        formInventario.setVisibility(View.GONE);
        formPeriodo.setVisibility(View.VISIBLE);

        seletorInventario.setTextColor(Color.parseColor("#808080"));
        seletorPeriodo.setTextColor(Color.WHITE);
        seletorCategorias.setTextColor(Color.parseColor("#808080"));
    }

    @OnClick(R.id.seletorInventario)
    public void selecionarInventario() {
        formCategorias.setVisibility(View.GONE);
        formInventario.setVisibility(View.VISIBLE);
        formPeriodo.setVisibility(View.GONE);

        seletorInventario.setTextColor(Color.WHITE);
        seletorPeriodo.setTextColor(Color.parseColor("#808080"));
        seletorCategorias.setTextColor(Color.parseColor("#808080"));
    }

    @OnClick(R.id.status)
    public void toggleStatus() {
        if (!busca.getIdsCategoriaRelato().isEmpty()) {
            if (opcoes.getVisibility() == View.GONE) {
                opcoes.setVisibility(View.VISIBLE);
                status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.seta_contrair, 0);
            } else {
                opcoes.setVisibility(View.GONE);
                status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.seta_expandir, 0);
            }
        }
    }

    private View.OnClickListener cliqueOpcao = v -> {

        for (int i = 0; i < opcoes.getChildCount(); i++) {
            ((TextView) opcoes.getChildAt(i)).setTextColor(getResources().getColorStateList(R.color.text_option_color));
        }

        TextView selecionado = (TextView) v;
        status.setText(selecionado.getText());
        selecionado.setTextColor(Color.rgb(0x2a, 0xb4, 0xdc));
        opcoes.setVisibility(View.GONE);
        status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.seta_expandir, 0);

        CategoriaRelato.Status status = (CategoriaRelato.Status) v.getTag();
        busca.setStatus(status);
    };

    private void aplicarFiltroInicial() {

        switch (busca.getPeriodo()) {
            case ULTIMOS_6_MESES:
                seekBar.setProgress(0);
                break;
            case ULTIMOS_3_MESES:
                seekBar.setProgress(1);
                break;
            case ULTIMO_MES:
                seekBar.setProgress(2);
                break;
            case ULTIMA_SEMANA:
                seekBar.setProgress(3);
                break;
        }
    }

    @OnClick(R.id.botaoConcluido)
    public void concluir() {
        Intent i = new Intent();
        busca.setIdsCategoriaRelato((ArrayList<Long>) formCategorias.getSelectedCategories());
        i.putExtra("busca", busca);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (progress) {
            case 0:
                busca.setPeriodo(Periodo.ULTIMOS_6_MESES);
                break;
            case 1:
                busca.setPeriodo(Periodo.ULTIMOS_3_MESES);
                break;
            case 2:
                busca.setPeriodo(Periodo.ULTIMO_MES);
                break;
            case 3:
                busca.setPeriodo(Periodo.ULTIMA_SEMANA);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    protected String getScreenName() {
        return "Filtro do Explore";
    }
}
