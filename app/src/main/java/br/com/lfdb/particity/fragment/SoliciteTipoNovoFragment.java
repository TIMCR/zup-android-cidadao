package br.com.lfdb.particity.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import br.com.lfdb.particity.R;
import br.com.lfdb.particity.SoliciteActivity;
import br.com.lfdb.particity.base.BaseFragment;
import br.com.lfdb.particity.domain.CategoriaRelato;
import br.com.lfdb.particity.domain.Solicitacao;
import br.com.lfdb.particity.service.CategoriaRelatoService;
import br.com.lfdb.particity.util.ImageUtils;
import butterknife.ButterKnife;
import butterknife.Bind;

public class SoliciteTipoNovoFragment extends BaseFragment {

    @Bind(R.id.categoriasContainer)
    LinearLayout categoriasContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_solicite_tipo_novo, container, false);
        ButterKnife.bind(this, view);

        ((SoliciteActivity) getActivity()).exibirBarraInferior(false);
        ((SoliciteActivity) getActivity()).setInfo(R.string.selecione_a_categoria_subcategoria);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        CategoriaRelato selecionada = null;
        if (getArguments() != null) {
            Solicitacao solicitacao = (Solicitacao) getArguments().getSerializable("solicitacao");
            if (solicitacao != null) {
                selecionada = solicitacao.getCategoria();
            }
        }

        if (selecionada == null) {
            selecionada = ((SoliciteActivity) getActivity()).getCategoria();
        }

        montarCategoriasRelatos(selecionada);
    }

    private void montarCategoriasRelatos(CategoriaRelato selecionada) {
        List<CategoriaRelato> categorias = new CategoriaRelatoService().getCategorias(getActivity());
        for (final CategoriaRelato categoria : categorias) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.item_expandable_category, categoriasContainer, false);
            view.setTag(categoria);
            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);

            ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);

            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);

            if (!categoria.equals(selecionada)) {
                imagem.setImageBitmap(ImageUtils.getScaledCustom(getActivity(), "reports", categoria.getIconeInativo(), 0.75f));
                nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                imagem.setImageBitmap(ImageUtils.getScaledCustom(getActivity(), "reports", categoria.getIconeAtivo(), 0.75f));
            }

            final TextView expander = ButterKnife.findById(view, R.id.expander);
            expander.setOnClickListener(v -> {
                if (expander.getTag() == null) {
                    expander.setTag(new Object());
                    subcategorias.setVisibility(View.VISIBLE);
                    expander.setText("Ocultar subcategorias");
                } else {
                    expander.setTag(null);
                    subcategorias.setVisibility(View.GONE);
                    expander.setText("Ver subcategorias");
                }
            });

            nomeCategoria.setText(categoria.getNome());
            nomeCategoria.setOnClickListener(v -> {
                if (expander.getTag() == null  && !categoria.getSubcategorias().isEmpty()) {
                    expander.setTag(new Object());
                    subcategorias.setVisibility(View.VISIBLE);
                    expander.setText("Ocultar subcategorias");
                } else {
                    desmarcarTudo(categoria.getSubcategorias().isEmpty());
                    ((SoliciteActivity) getActivity()).setCategoria(categoria);
                    nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
                    imagem.setImageBitmap(ImageUtils.getScaledCustom(getActivity(), "reports", categoria.getIconeAtivo(), 0.75f));
                }
            });

            if (categoria.getSubcategorias().isEmpty()) {
                expander.setVisibility(View.GONE);
            } else {
                for (CategoriaRelato sub : categoria.getSubcategorias()) {
                    View subView = getActivity().getLayoutInflater().inflate(R.layout.item_subcategoria, subcategorias, false);
                    final TextView nome = ButterKnife.findById(subView, R.id.nome);

                    if (sub.equals(selecionada)) {
                        expander.setTag(new Object());
                        subcategorias.setVisibility(View.VISIBLE);
                        expander.setText("Ocultar subcategorias");
                        expander.setTag(new Object());
                        nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
                        imagem.setImageBitmap(ImageUtils.getScaledCustom(getActivity(), "reports", sub.getIconeAtivo(), 0.75f));
                    }

                    nome.setText(sub.getNome());
                    subcategorias.addView(subView);
                    subView.setOnClickListener(v -> {
                        desmarcarTudo();
                        expander.setText("Ocultar subcategorias");
                        expander.setTag(new Object());
                        subcategorias.setVisibility(View.VISIBLE);
                        ((SoliciteActivity) getActivity()).setCategoria(sub);
                        nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
                        imagem.setImageBitmap(ImageUtils.getScaledCustom(getActivity(), "reports", sub.getIconeAtivo(), 0.75f));
                    });
                }
            }

            categoriasContainer.addView(view);
        }
    }

    private void desmarcarTudo() {
        desmarcarTudo(true);
    }

    private void desmarcarTudo(boolean collapse) {
        for (int i = 0; i < categoriasContainer.getChildCount(); i++) {
            View view = categoriasContainer.getChildAt(i);
            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);
            nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);
            imagem.setImageBitmap(ImageUtils.getScaledCustom(getActivity(), "reports", ((CategoriaRelato) view.getTag()).getIconeInativo(), 0.75f));

            ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);
            subcategorias.setVisibility(collapse ? View.GONE : View.VISIBLE);
            TextView expander = ButterKnife.findById(view, R.id.expander);
            if (collapse) expander.setText("Ver subcategorias");
            ButterKnife.findById(view, R.id.expander).setTag(null);
            for (int j = 0; j < subcategorias.getChildCount(); j++) {
                View v = subcategorias.getChildAt(j);
                TextView nome = ButterKnife.findById(v, R.id.nome);
                nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((SoliciteActivity) getActivity()).setInfo(R.string.selecione_a_categoria_subcategoria);
        }
    }

    @Override
    protected String getScreenName() {
        return "Seleção de Categoria (Novo Relato)";
    }
}
