package br.com.lfdb.particity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import br.com.lfdb.particity.base.BaseActivity;
import br.com.lfdb.particity.domain.BuscaEstatisticas;
import br.com.lfdb.particity.domain.BuscaExplore;
import br.com.lfdb.particity.fragment.EstatisticasFragment;
import br.com.lfdb.particity.fragment.ExploreFragment_;
import br.com.lfdb.particity.fragment.MinhaContaFragment;
import br.com.lfdb.particity.service.CategoriaRelatoService;
import br.com.lfdb.particity.service.FeatureService;
import br.com.lfdb.particity.service.LoginService;
import br.com.lfdb.particity.util.FontUtils;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int SOLICITACAO_REQUEST_CODE = 1010;
    public static final int FILTRO_CODE = 1007;

    private TextView current;

    private EstatisticasFragment estatisticasFragment;
    private ExploreFragment_ exploreFragment;
    private MinhaContaFragment minhaContaFragment;

    private Fragment atual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView exploreButton = (TextView) findViewById(R.id.exploreButton);
        exploreButton.setOnClickListener(this);
        exploreButton.setTypeface(FontUtils.getRegular(this));

        TextView soliciteButton = (TextView) findViewById(R.id.soliciteButton);
        soliciteButton.setOnClickListener(this);
        soliciteButton.setTypeface(FontUtils.getRegular(this));
        if (!FeatureService.getInstance(this).isCreateReportsClients()) {
            soliciteButton.setVisibility(View.GONE);
        }

        TextView minhaContaButton = (TextView) findViewById(R.id.minhaContaButton);
        minhaContaButton.setOnClickListener(this);
        minhaContaButton.setTypeface(FontUtils.getRegular(this));

 /*       TextView estatisticasButton = (TextView) findViewById(R.id.estatisticasButton);
        estatisticasButton.setOnClickListener(this);
        estatisticasButton.setTypeface(FontUtils.getRegular(this));
        if (!FeatureService.getInstance(this).isStatsEnabled()) {
            estatisticasButton.setVisibility(View.GONE);
        }*/

        if (new CategoriaRelatoService().getCategorias(this).isEmpty()) {
            findViewById(R.id.footer).setVisibility(View.GONE);
        }

        if (FeatureService.getInstance(this).isExploreEnabled()) {
            current = exploreButton;
            exploreFragment = new ExploreFragment_();
            setFragment(exploreFragment);
        } else {
            exploreButton.setVisibility(View.GONE);
            if (!FeatureService.getInstance(this).isStatsEnabled() &&
                    !FeatureService.getInstance(this).isCreateReportsClients()) {
                findViewById(R.id.footer).setVisibility(View.GONE);
            }
            selectCurrent(R.id.minhaContaButton);
            current = minhaContaButton;
            minhaContaFragment = new MinhaContaFragment();
            setFragment(minhaContaFragment);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == current.getId()) {
            return;
        }

        if (!new LoginService().usuarioLogado(this) && v.getId() == R.id.minhaContaButton) {
            startActivity(new Intent(this, WarningActivity.class));
            return;
        }

        if (v.getId() == R.id.soliciteButton) {
            startActivityForResult(new Intent(this, SoliciteActivity_.class), SOLICITACAO_REQUEST_CODE);
            return;
        }

        unselectCurrent();
        selectCurrent(v.getId());
    }

    private void selectCurrent(int id) {
        TextView view = (TextView) findViewById(id);
        view.setTextColor(Color.WHITE);
        switch (id) {
            case R.id.exploreButton:
                view.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.tabbar_icon_explore_active), null, null);
                if (exploreFragment == null) {
                    exploreFragment = new ExploreFragment_();
                    setFragment(exploreFragment);
                } else {
                    activeFragment(exploreFragment);
                }
                break;
            case R.id.minhaContaButton:
                view.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.tabbar_icon_conta_active), null, null);
                if (minhaContaFragment == null) {
                    minhaContaFragment = new MinhaContaFragment();
                    setFragment(minhaContaFragment);
                } else {
                    activeFragment(minhaContaFragment);
                }
                break;
        }
        current = view;
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, fragment);
        if (atual != null) {
            fm.hide(atual);
            fm.hide(atual);
        }
        fm.commit();
        atual = fragment;
    }

    private void activeFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().hide(atual).show(fragment).commit();
        atual = fragment;
    }

    private void unselectCurrent() {
        current.setTextColor(getResources().getColorStateList(R.color.tabbar_text_color));
        switch (current.getId()) {
            case R.id.exploreButton:
                current.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.tabbar_icon_explore), null, null);
                break;
            case R.id.minhaContaButton:
                current.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.tabbar_icon_conta), null, null);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SOLICITACAO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            exploreFragment.refresh();
            if (minhaContaFragment != null) minhaContaFragment.refresh();
        } else if (requestCode == FILTRO_CODE && resultCode == Activity.RESULT_OK) {
            exploreFragment.applyFilter((BuscaExplore) data.getSerializableExtra("busca"));
        } else if (requestCode == EstatisticasFragment.REQUEST_FILTRO && resultCode == Activity.RESULT_OK) {
            estatisticasFragment.aplicarFiltro((BuscaEstatisticas) data.getSerializableExtra("busca"));
        }
    }

    @Override
    protected String getScreenName() {
        return "Tela Principal";
    }
}
