package br.com.lfdb.particity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import br.com.lfdb.particity.base.BaseActivity;
import br.com.lfdb.particity.domain.BuscaEstatisticas;
import br.com.lfdb.particity.domain.CategoriaRelato;
import br.com.lfdb.particity.domain.Periodo;
import br.com.lfdb.particity.service.CategoriaRelatoService;
import br.com.lfdb.particity.util.ImageUtils;
import br.com.lfdb.particity.widget.SeekbarWithIntervals;
import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FiltroEstatisticasNovoActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

    @Bind(R.id.seekBar)
    SeekbarWithIntervals seekBar;
    @Bind(R.id.categoriasContainer)
    LinearLayout categoriasContainer;
    @Bind(R.id.iconeTodos)
    ImageView iconeTodos;
    @Bind(R.id.textoTodos)
    TextView textoTodos;

    private BuscaEstatisticas busca = new BuscaEstatisticas();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro_estatisticas_novo);
        ButterKnife.bind(this);

        seekBar.setOnSeekBarChangeListener(this);

        iconeTodos.setImageResource(R.drawable.filtros_check_todascategorias_ativar);
        textoTodos.setText("Ativar todas as categorias");
        preencherCategorias();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @OnClick(R.id.botaoConcluido)
    public void concluir() {
        Intent i = new Intent();
        i.putExtra("busca", busca);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    private void preencherCategorias() {
        List<CategoriaRelato> categorias = new CategoriaRelatoService().getCategorias(this);
        for (final CategoriaRelato categoria : categorias) {
            View view = getLayoutInflater().inflate(R.layout.item_expandable_category, categoriasContainer, false);
            view.setTag(categoria);
            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);

            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);

            if (busca.getCategoria() != null && !busca.getCategoria().equals(categoria.getId())) {
                imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", categoria.getIconeInativo(), 0.75f));
                nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", categoria.getIconeAtivo(), 0.75f));
            }

            nomeCategoria.setText(categoria.getNome());
            nomeCategoria.setOnClickListener(v -> {
                desmarcarTodas();
                busca.setCategoria(categoria.getId());
                nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
                imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", categoria.getIconeAtivo(), 0.75f));
            });

            ButterKnife.findById(view, R.id.expander).setVisibility(View.GONE);

            categoriasContainer.addView(view);
        }
    }

    @OnClick(R.id.toggleAll)
    public void selecionarTodos() {
        busca.setCategoria(null);

        for (int i = 0; i < categoriasContainer.getChildCount(); i++) {
            View view = categoriasContainer.getChildAt(i);
            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);
            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);
            imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", ((CategoriaRelato) view.getTag()).getIconeAtivo(), 0.75f));

            nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);

            ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);
            for (int j = 0; j < subcategorias.getChildCount(); j++) {
                View v = subcategorias.getChildAt(j);
                TextView nome = ButterKnife.findById(v, R.id.nome);
                nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
            }
        }
    }

    private void desmarcarTodas() {
        for (int i = 0; i < categoriasContainer.getChildCount(); i++) {
            View view = categoriasContainer.getChildAt(i);
            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);
            nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);
            imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", ((CategoriaRelato) view.getTag()).getIconeInativo(), 0.75f));

            ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);
            for (int j = 0; j < subcategorias.getChildCount(); j++) {
                View v = subcategorias.getChildAt(j);
                TextView nome = ButterKnife.findById(v, R.id.nome);
                nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }
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
        return "Filtro de Estatísticas";
    }
}
