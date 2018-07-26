package br.com.lfdb.particity.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import br.com.lfdb.particity.domain.BuscaEstatisticas;
import br.com.lfdb.particity.domain.BuscaExplore;

import com.google.gson.Gson;

public class PreferenceUtils {

	public static void salvarBusca(Context context, BuscaExplore busca) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString("busca", new Gson().toJson(busca)).apply();
	}
	
	public static BuscaExplore obterBuscaExplore(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return new Gson().fromJson(prefs.getString("busca", ""), BuscaExplore.class);
	}
	
	public static void salvarBusca(Context context, BuscaEstatisticas busca) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString("busca", new Gson().toJson(busca)).apply();
	}
	
	public static BuscaEstatisticas obterBuscaEstatisticas(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return new Gson().fromJson(prefs.getString("busca", ""), BuscaEstatisticas.class);
	}
}
