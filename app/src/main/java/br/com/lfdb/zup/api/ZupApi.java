package br.com.lfdb.zup.api;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.service.LoginService;

public class ZupApi {

    public static boolean validateCityBoundary(Context context, double latitude, double longitude)
            throws Exception {
        String call = String.format(Constantes.REST_URL +
                "/utils/city-boundary/validate?latitude=%f&longitude=%f",
                latitude, longitude);
        String header = new LoginService().getToken(context);

        Request request = new Request.Builder()
                .url(call)
                .header("X-App-Token", header)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        String value = response.body().string();
        JSONObject json = new JSONObject(value);
        return !json.has("error");
    }
}