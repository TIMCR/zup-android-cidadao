package br.com.lfdb.particity.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.com.lfdb.particity.R;
import br.com.lfdb.particity.domain.CategoriaRelato;
import br.com.lfdb.particity.service.CategoriaRelatoService;
import br.com.lfdb.particity.util.ImageUtils;
import butterknife.ButterKnife;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryPicker extends LinearLayout {

    private Adapter adapter;

    public void removeAll() {
        adapter.removeAll();
    }

    public void addAll() {
        adapter.addAll();
    }

    public List<Long> getSelectedCategories() {
        return adapter.getSelectedCategories();
    }

    public CategoryPicker(Context context) {
        super(context);
        init();
    }

    public CategoryPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public CategoryPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CategoryPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_category_picker, this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);
        adapter.addAll();

        findViewById(R.id.toggleAll).setOnClickListener(v -> {
            TextView text = (TextView) v.findViewById(R.id.textoTodos);
            ImageView icon = (ImageView) v.findViewById(R.id.iconeTodos);

            if (v.getTag() != null) {
                v.setTag(null);
                text.setText("Desativar todas as categorias");
                icon.setImageResource(R.drawable.filtros_check_todascategorias_desativar);
                adapter.addAll();
            } else {
                v.setTag(new Object());
                text.setText("Ativar todas as categorias");
                icon.setImageResource(R.drawable.filtros_check_todascategorias_ativar);
                adapter.removeAll();
            }
        });
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private final List<CategoriaRelato> categories;
        private final Set<Long> expanded = new HashSet<>();
        private final Set<CategoriaRelato> selecionadas = new HashSet<>();

        public Adapter() {
            categories = new CategoriaRelatoService().getCategorias(getContext());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expandable_category, parent, false));
        }

        public void addAll() {
            for (int i = 0; i < categories.size(); i++) {
                selecionadas.add(categories.get(i));
                selecionadas.addAll(categories.get(i).getSubcategorias());
            }
            notifyDataSetChanged();
        }

        public void removeAll() {
            selecionadas.clear();
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final CategoriaRelato categoria = categories.get(position);

            Bitmap bitmap = ImageUtils.getScaledCustom((Activity) getContext(), "reports", selecionadas.contains(categoria) ?
                    categoria.getIconeAtivo() : categoria.getIconeInativo(), 0.75f);

            if (bitmap == null) bitmap = ImageUtils.loadDefaultIcon((Activity) getContext(),
                    selecionadas.contains(categoria), 0.75f);
            holder.imagem.setImageBitmap(bitmap);
            holder.nomeCategoria.setText(categoria.getNome());
            holder.nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, selecionadas.contains(categoria) ?
                    R.drawable.filtros_check_categoria : 0, 0);
            holder.expander.setOnClickListener(v -> {
                switchState(categoria.getId());
                checkExpanded(categoria, holder);
            });

            holder.nomeCategoria.setOnClickListener(v -> {
                if (selecionadas.contains(categoria)) {
                    selecionadas.remove(categoria);
                    selecionadas.removeAll(categoria.getSubcategorias());
                } else {
                    selecionadas.add(categoria);
                    selecionadas.addAll(categoria.getSubcategorias());
                }
                checkExpanded(categoria, holder);
                holder.nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, selecionadas.contains(categoria) ?
                        R.drawable.filtros_check_categoria : 0, 0);
                Bitmap bitmap1 = ImageUtils.getScaledCustom((Activity) getContext(), "reports", selecionadas.contains(categoria) ?
                        categoria.getIconeAtivo() : categoria.getIconeInativo(), 0.75f);
                if (bitmap1 == null) bitmap1 = ImageUtils.loadDefaultIcon((Activity) getContext(),
                        selecionadas.contains(categoria), 0.75f);

                holder.imagem.setImageBitmap(bitmap1);
            });

            checkExpanded(categoria, holder);
        }

        protected void checkExpanded(CategoriaRelato categoria, ViewHolder holder) {
            if (holder.subcategorias.getChildCount() > 0) holder.subcategorias.removeAllViews();
            if (expanded.contains(categoria.getId())) {
                holder.expander.setText("Ocultar subcategorias");
                holder.subcategorias.setVisibility(View.VISIBLE);
                for (CategoriaRelato subcategory : categoria.getSubcategorias()) addSubcategoria(subcategory, holder.subcategorias);
            } else {
                holder.expander.setText("Ver subcategorias");
                holder.subcategorias.setVisibility(View.GONE);
            }
        }

        private void addSubcategoria(CategoriaRelato subcategory, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_subcategoria, parent, false);
            view.setTag(subcategory);
            final TextView nome = ButterKnife.findById(view, R.id.nome);
            nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, selecionadas.contains(subcategory) ? R.drawable.filtros_check_categoria : 0, 0);
            nome.setText(subcategory.getNome());

            view.setOnClickListener(v -> {
                if (selecionadas.contains(subcategory)) {
                    selecionadas.remove(subcategory);
                } else {
                    selecionadas.add(subcategory);
                }
                nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, selecionadas.contains(subcategory) ?
                        R.drawable.filtros_check_categoria : 0, 0);
            });

            parent.addView(view);
        }

        public List<Long> getSelectedCategories() {
            List<Long> values = new ArrayList<>();
            for (CategoriaRelato categoria : selecionadas) {
                values.add(categoria.getId());
            }
            return values;
        }

        protected void switchState(long id) {
            if (expanded.contains(id)) expanded.remove(id);
            else expanded.add(id);
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imagem;
            ViewGroup subcategorias;
            TextView nomeCategoria;
            TextView expander;

            public ViewHolder(View view) {
                super(view);

                imagem = ButterKnife.findById(view, R.id.imagemCategoria);
                subcategorias = ButterKnife.findById(view, R.id.subcategorias);
                nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);
                expander = ButterKnife.findById(view, R.id.expander);
            }
        }
    }
}
