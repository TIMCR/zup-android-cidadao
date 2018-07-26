package br.com.lfdb.particity.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import br.com.lfdb.particity.OpeningActivity;
import br.com.lfdb.particity.service.LoginService;

public class AuthHelper {

    public static void redirectSessionExpired(Context context) {
        new LoginService().registrarLogout(context);
        Intent intent = new Intent(context, OpeningActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.runOnUiThread(() -> Toast.makeText(activity, "Sessão expirada, por favor realize seu login novamente", Toast.LENGTH_LONG).show());
            activity.finish();
        }
    }
}
