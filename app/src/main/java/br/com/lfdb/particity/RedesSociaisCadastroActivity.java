package br.com.lfdb.particity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import br.com.lfdb.particity.base.BaseActivity;
import br.com.lfdb.particity.service.FeatureService;
import br.com.lfdb.particity.social.auth.FacebookAuth;
import br.com.lfdb.particity.social.auth.GooglePlusAuth;
import br.com.lfdb.particity.social.auth.TwitterAuth;
import br.com.lfdb.particity.util.FontUtils;

public class RedesSociaisCadastroActivity extends BaseActivity implements View.OnClickListener {

	private static final int REQUEST_CODE = 9999;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_redes_sociais_cadastro);

		((TextView) findViewById(R.id.compartilhamento)).setTypeface(FontUtils.getLight(this));
		((TextView) findViewById(R.id.textView1)).setTypeface(FontUtils.getLight(this));

		TextView botaoVoltar = (TextView) findViewById(R.id.botaoVoltar);
		botaoVoltar.setTypeface(FontUtils.getRegular(this));
		botaoVoltar.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

		TextView linkPular = (TextView) findViewById(R.id.linkPular);
		linkPular.setTypeface(FontUtils.getBold(this));
		linkPular.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK);
            finish();
        });

		ImageButton botaoFacebook = (ImageButton) findViewById(R.id.botao_logar_facebook);
		botaoFacebook.setOnClickListener(this);
		ImageButton botaoTwitter = (ImageButton) findViewById(R.id.botao_logar_twitter);
		botaoTwitter.setOnClickListener(this);
		ImageButton botaoGooglePlus = (ImageButton) findViewById(R.id.botao_logar_google);
		botaoGooglePlus.setOnClickListener(this);

        if (!FeatureService.getInstance(this).isSocialNetworkFacebookEnabled()) {
            botaoFacebook.setVisibility(View.GONE);
        }
        if (!FeatureService.getInstance(this).isSocialNetworkTwitterEnabled()) {
            botaoTwitter.setVisibility(View.GONE);
        }
        if (!FeatureService.getInstance(this).isSocialNetworkGPlusEnabled()) {
            botaoGooglePlus.setVisibility(View.GONE);
        }
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.botao_logar_twitter:
			startActivityForResult(new Intent(this, TwitterAuth.class), REQUEST_CODE);
			break;
		case R.id.botao_logar_facebook:
			startActivityForResult(new Intent(this, FacebookAuth.class), REQUEST_CODE);
			break;
		case R.id.botao_logar_google:
			startActivityForResult(new Intent(this, GooglePlusAuth.class), REQUEST_CODE);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				setResult(Activity.RESULT_OK);
				finish();
			} else {
				Toast.makeText(this, getString(R.string.failed_social_auth), Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	protected String getScreenName() {
		return "Seleção de Redes Sociais (cadastro)";
	}
}
