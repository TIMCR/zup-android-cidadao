package br.com.lfdb.particity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStreamReader;

import br.com.lfdb.particity.base.BaseActivity;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TermosDeUsoActivity extends BaseActivity {

    private float downX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termos_de_uso);

        WebView container = (WebView) findViewById(R.id.container);
        container.setPadding(0, 0, 0, 0);
        container.getSettings().setLoadWithOverviewMode(true);
        container.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        try {
            container.loadDataWithBaseURL("", CharStreams.toString(new InputStreamReader(
                            getResources().getAssets().open("termo-de-uso.html"))),
                    "text/html", "UTF-8", "");
        } catch (IOException e) {
            Log.e("ZUP", "Failed to open terms", e);
        }

        container.setHorizontalScrollBarEnabled(false);
        container.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                }
                break;

                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP: {
                    event.setLocation(downX, event.getY());
                }
                break;

            }

            return false;
        });

        findViewById(R.id.botaoVoltar).setOnClickListener(v -> finish());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected String getScreenName() {
        return "Termos de Uso";
    }
}
