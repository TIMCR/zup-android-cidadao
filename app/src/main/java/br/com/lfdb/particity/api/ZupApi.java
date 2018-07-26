package br.com.lfdb.particity.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import br.com.lfdb.particity.BuildConfig;
import br.com.lfdb.particity.core.Constantes;
import br.com.lfdb.particity.core.ConstantesBase;
import br.com.lfdb.particity.service.LoginService;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.json.JSONObject;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class ZupApi {

    private static ZupService service;

    public static boolean validateCityBoundary(Context context, double latitude, double longitude)
            throws Exception {
        String call = Constantes.REST_URL + "/utils/city-boundary/validate?latitude=" + latitude +
                "&longitude=" + longitude;
        String header = new LoginService().getToken(context);

        Request request =
                new Request.Builder().addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT)
                        .url(call)
                        .header("X-App-Token", header)
                        .build();
        Response response = new OkHttpClient().newCall(request).execute();
        if (!response.isSuccessful()) return false;
        JSONObject json = new JSONObject(response.body().string());
        Log.e("Retorno Perimetro", json.toString());
        return json.optBoolean("inside_boundaries", true);
    }

    @NonNull public static ZupService get(final Context context) {
        if (service == null) {
            RestAdapter restAdapter = new RestAdapter.Builder().setRequestInterceptor(request -> {
                request.addHeader("X-App-Token", new LoginService().getToken(context));
                request.addHeader("X-App-Namespace", Constantes.NAMESPACE_DEFAULT);
            })
                    .setEndpoint(Constantes.REST_URL)
                    .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                    .setConverter(new GsonConverter(ConstantesBase.GSON))
                    .build();
            service = restAdapter.create(ZupService.class);
        }

        return service;
    }
}
