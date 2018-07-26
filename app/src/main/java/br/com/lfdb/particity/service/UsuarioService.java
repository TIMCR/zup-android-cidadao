package br.com.lfdb.particity.service;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import br.com.lfdb.particity.domain.Usuario;

public class UsuarioService {

    public Usuario getUsuarioAtivo(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            String json = prefs.getString("raw_user", "");
            if ("".equals(json)) return null;

            return extrairDoJSON(new JSONObject(json));
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
            return null;
        }
    }

    public Usuario extrairDoJSON(JSONObject j) throws JSONException {
        Usuario u = new Usuario();
        u.setBairro(j.optString("district"));
        u.setCep(j.optString("postal_code"));
        u.setComplemento(j.optString("address_additional"));
        u.setCpf(j.optString("document"));
        u.setEmail(j.getString("email"));
        u.setEndereco(j.optString("address"));
        u.setNome(j.getString("name"));
        u.setTelefone(j.getString("phone"));
        u.setId(j.getLong("id"));
        u.setCidade(j.optString("city"));

        return u;
    }

    public JSONObject converterParaJSON(Usuario usuario) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("district", usuario.getBairro());
        json.put("postal_code", usuario.getCep());
        json.put("address_additional", usuario.getComplemento());
        json.put("document", usuario.getCpf());
        json.put("email", usuario.getEmail());
        json.put("address", usuario.getEndereco());
        json.put("name", usuario.getNome());
        json.put("phone", usuario.getTelefone());
        json.put("current_password", usuario.getSenhaAntiga());
        json.put("password", usuario.getSenha());
        json.put("password_confirmation", usuario.getConfirmacaoSenha());
        json.put("id", usuario.getId());
        json.put("city", usuario.getCidade());

        return json;
    }

    public String loginData(String email, String senha, String gcm) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("email", email);
        json.put("password", senha);
        json.put("device_type", "android");
        json.put("device_token", gcm);
        return json.toString();
    }
}
