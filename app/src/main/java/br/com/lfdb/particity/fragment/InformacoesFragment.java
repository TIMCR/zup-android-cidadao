package br.com.lfdb.particity.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.lfdb.particity.R;
import br.com.lfdb.particity.util.FontUtils;
import br.com.lfdb.particity.util.ImageUtils;
import br.com.lfdb.particity.util.JsonUtils;
import br.com.lfdb.particity.widget.RemoteImageAdapter;
import butterknife.ButterKnife;

public class InformacoesFragment extends Fragment {

    private LinearLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_informacoes, container, false);

        layout = (LinearLayout) view.findViewById(R.id.conteudo);

        ViewPager mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setVisibility(View.GONE);

        return view;
    }

    @SuppressLint("DefaultLocale")
    private void addView(String label, String content) {
        final float scale = getActivity().getResources().getDisplayMetrics().density;

        TextView tvLabel = new TextView(getActivity());
        tvLabel.setText(label.toUpperCase(Locale.US));
        tvLabel.setTextColor(Color.rgb(0x33, 0x33, 0x33));
        tvLabel.setTypeface(FontUtils.getBold(getActivity()));
        tvLabel.setPadding((int) (15 * scale + 0.5f), (int) (5 * scale + 0.5f), 0, 0);
        layout.addView(tvLabel);

        if (isImageContent(content)) {
            setUpViewPager(content, layout);
        } else {
            TextView tvContent = new TextView(getActivity());
            tvContent.setTypeface(FontUtils.getLight(getActivity()));
            tvContent.setTextColor(Color.rgb(0x33, 0x33, 0x33));

            if (JsonUtils.isJsonArray(content)) {
                try {
                    JSONArray array = new JSONArray(content);

                    List<String> values = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        values.add(array.getString(i));
                    }
                    tvContent.setText(TextUtils.join(", ", values));

                } catch (JSONException e) {
                    Log.wtf("ZUP", "Isso não deveria acontecer...", e);
                }
            } else {
                tvContent.setText(content);
            }
            tvContent.setPadding((int) (15 * scale + 0.5f), 0, (int) (15 * scale + 0.5f), (int) (15 * scale + 0.5f));
            layout.addView(tvContent);
        }

    }

    private void setUpViewPager(String content, ViewGroup container) {
        try {
            View view = getActivity().getLayoutInflater().inflate(R.layout.view_pager, container, false);
            ViewPager pager = ButterKnife.findById(view, R.id.imageSlider);
            CirclePageIndicator indicator = ButterKnife.findById(view, R.id.indicator);
            pager.setAdapter(new RemoteImageAdapter(getFragmentManager(), getImages(content)));
            indicator.setViewPager(pager);
            container.addView(view);
        } catch (Exception e) {
            Log.w("ZUP", "Falha ao criar ViewPager", e);
        }
    }

    private List<String> getImages(String content) {
        try {
            List<String> images = new ArrayList<>();
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < array.length(); i++) {
                JSONObject image = array.getJSONObject(i);
                images.add(image.getJSONObject("versions").getString(ImageUtils.shouldDownloadRetinaIcon(getActivity()) ? "high" : "low"));
            }
            return images;
        } catch (Exception e) {
            Log.w("ZUP", "Falha ao realizar parse de lista de imagens", e);
            return Collections.emptyList();
        }
    }

    private boolean isImageContent(String content) {
        if (!JsonUtils.isJsonArray(content)) return false;

        try {
            JSONArray array = new JSONArray(content);

            if (array.length() == 0) return false;

            if (!JsonUtils.isJsonObject(array.get(0).toString())) return false;
        } catch (JSONException e) {
            Log.wtf("ZUP", "Isso não deveria acontecer também...", e);
            return false;
        }

        return true;
    }

    public void setDados(String sectionTitle, Map<String, String> camposDinamicos) {
        if (!camposDinamicos.isEmpty()) {

            if (sectionTitle != null && !sectionTitle.trim().isEmpty())
                adicionarSubtitulo(sectionTitle);

            for (String key : camposDinamicos.keySet()) {
                addView(key, camposDinamicos.get(key));
            }
        }
    }

    private void adicionarSubtitulo(String subtitulo) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.inventory_section_title, null);
        ((TextView) view.findViewById(R.id.title)).setText(subtitulo);
        layout.addView(view);
    }
}
