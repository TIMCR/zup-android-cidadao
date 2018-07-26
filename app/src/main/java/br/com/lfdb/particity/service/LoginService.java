package br.com.lfdb.particity.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONObject;

public class LoginService {

    public void atualizarUsuario(Context context, JSONObject usuario) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("raw_user", usuario.toString()).apply();
    }

    public long getUserId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return new JSONObject(prefs.getString("raw_user", "")).getLong("id");
        } catch (Exception e) {
            Log.e("ZUP", "Failed to retrieve user id", e);
            return -1;
        }
    }

    public void registrarLogin(Context context, JSONObject usuario, String token) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("raw_user", usuario.toString()).putString("token", token).apply();
    }

    public void registrarLogout(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove("raw_user").remove("token").apply();
    }

    public boolean usuarioLogado(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.contains("raw_user") && prefs.contains("token");
    }

    public String getToken(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("token", "");
    }
}
