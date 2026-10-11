package com.sinreels;

import android.content.Context;
import android.content.SharedPreferences;

/** Ajustes guardados de la app. */
final class Prefs {
    private Prefs() {}

    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences("sinreels", Context.MODE_PRIVATE);
    }

    static boolean diagnostico(Context c) {
        return p(c).getBoolean("diagnostico", false);
    }

    static void setDiagnostico(Context c, boolean on) {
        p(c).edit().putBoolean("diagnostico", on).apply();
    }

    static String idsExtra(Context c) {
        return p(c).getString("ids_extra", "");
    }

    static void setIdsExtra(Context c, String ids) {
        p(c).edit().putString("ids_extra", ids).apply();
    }

    static int bloqueos(Context c) {
        return p(c).getInt("bloqueos", 0);
    }

    static void sumarBloqueo(Context c) {
        p(c).edit().putInt("bloqueos", bloqueos(c) + 1).apply();
    }

    static String ultimaPantalla(Context c) {
        return p(c).getString("ultima_pantalla", "");
    }

    static void setUltimaPantalla(Context c, String texto) {
        p(c).edit().putString("ultima_pantalla", texto).apply();
    }
}
