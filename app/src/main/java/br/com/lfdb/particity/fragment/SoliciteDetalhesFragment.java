package br.com.lfdb.particity.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import br.com.lfdb.particity.R;
import br.com.lfdb.particity.RedesSociaisCadastroActivity;
import br.com.lfdb.particity.SoliciteActivity;
import br.com.lfdb.particity.TermosDeUsoActivity;
import br.com.lfdb.particity.base.BaseFragment;
import br.com.lfdb.particity.domain.Solicitacao;
import br.com.lfdb.particity.service.FeatureService;
import br.com.lfdb.particity.social.SocialConstants;
import br.com.lfdb.particity.util.FontUtils;
import butterknife.ButterKnife;

public class SoliciteDetalhesFragment extends BaseFragment implements View.OnClickListener {

    private boolean publicar = false;

    private TextView sigioso;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((SoliciteActivity) getActivity()).setInfo(R.string.concluir_solicitacao);
            sigioso.setVisibility(((SoliciteActivity) getActivity()).getCategoria().isConfidencial() ?
                    View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((SoliciteActivity) getActivity()).setInfo(R.string.concluir_solicitacao);

        View view = inflater.inflate(R.layout.fragment_solicite_detalhes, container, false);
        view.findViewById(R.id.seletor_postagem).setOnClickListener(this);

        sigioso = (TextView) view.findViewById(R.id.sigiloso);
        sigioso.setVisibility(((SoliciteActivity) getActivity()).getCategoria().isConfidencial() ?
                View.VISIBLE : View.GONE);

        EditText comentario = (EditText) view.findViewById(R.id.comentario);
        comentario.setTypeface(FontUtils.getRegular(getActivity()));

        comentario.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ((SoliciteActivity) getActivity()).solicitar();
                handled = true;
            }
            return handled;
        });

        TextView redeSocial = (TextView) view.findViewById(R.id.redeSocial);
        redeSocial.setTypeface(FontUtils.getLight(getActivity()));

        TextView loginSocial = (TextView) view.findViewById(R.id.loginSocial);
        loginSocial.setText(R.string.login_social);
        loginSocial.setTypeface(FontUtils.getLight(getActivity()));
        loginSocial.setOnClickListener(v -> startActivity(new Intent(getActivity(), RedesSociaisCadastroActivity.class)));

        TextView termos = (TextView) view.findViewById(R.id.termos);
        termos.setText(Html.fromHtml(getString(R.string.termos_de_uso_relato)));
        termos.setTypeface(FontUtils.getLight(getActivity()));
        termos.setOnClickListener(v -> startActivity(new Intent(getActivity(), TermosDeUsoActivity.class)));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            Solicitacao solicitacao = (Solicitacao) getArguments().getSerializable("solicitacao");
            TextView comentario = (TextView) getView().findViewById(R.id.comentario);
            comentario.setText(solicitacao.getComentario());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkSocial();
    }

    private void checkSocial() {
        boolean socialEnabled = new FeatureService().isAnySocialEnabled();
        if (!socialEnabled) {
            ButterKnife.findById(getView(), R.id.postSocial).setVisibility(View.GONE);
            ButterKnife.findById(getView(), R.id.loginSocial).setVisibility(View.GONE);
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String social = prefs.getString(SocialConstants.PREF_LOGGED_SOCIAL, "");
        if (social.equals("")) {
            getView().findViewById(R.id.postSocial).setVisibility(View.GONE);
            getView().findViewById(R.id.loginSocial).setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(R.id.loginSocial).setVisibility(View.GONE);
            getView().findViewById(R.id.postSocial).setVisibility(View.VISIBLE);

            if (social.startsWith("google")) social += "+";

            TextView redeSocial = (TextView) getView().findViewById(R.id.redeSocial);
            redeSocial.setText(getString(R.string.compartilhar_rede_social, StringUtils.capitalize(social)));
        }
    }

    @Override
    public void onClick(View v) {
        publicar = !publicar;

        if (publicar) {
            ((ImageView) v).setImageResource(R.drawable.switch_on);
        } else {
            ((ImageView) v).setImageResource(R.drawable.switch_off);
        }
    }

    public boolean getPublicar() {
        return publicar;
    }

    public String getComentario() {
        return ((TextView) getView().findViewById(R.id.comentario)).getText().toString();
    }

    @Override
    protected String getScreenName() {
        return "Comentário do Relato (Novo Relato)";
    }
}
