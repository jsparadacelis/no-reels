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

    // ───── Horario de bloqueo (minutos desde medianoche) ─────

    static boolean horarioActivo(Context c) {
        return p(c).getBoolean("horario_activo", true);
    }

    static void setHorarioActivo(Context c, boolean on) {
        p(c).edit().putBoolean("horario_activo", on).apply();
    }

    static int inicioMin(Context c) {
        return p(c).getInt("inicio_min", 18 * 60); // 18:00
    }

    static int finMin(Context c) {
        return p(c).getInt("fin_min", 6 * 60); // 06:00
    }

    static void setInicioMin(Context c, int m) {
        p(c).edit().putInt("inicio_min", m).apply();
    }

    static void setFinMin(Context c, int m) {
        p(c).edit().putInt("fin_min", m).apply();
    }

    /** true si ahora toca bloquear. Si el horario está desactivado, bloquea siempre. */
    static boolean tocaBloquearAhora(Context c) {
        if (!horarioActivo(c)) return true;
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int ahora = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE);
        return dentroDe(ahora, inicioMin(c), finMin(c));
    }

    /** Comprueba si "ahora" cae en [inicio, fin), aunque el tramo cruce la medianoche. */
    static boolean dentroDe(int ahora, int inicio, int fin) {
        if (inicio == fin) return true; // mismo inicio y fin = todo el día
        if (inicio < fin) return ahora >= inicio && ahora < fin;
        return ahora >= inicio || ahora < fin; // p. ej. 18:00 → 06:00
    }

    static String hhmm(int min) {
        return String.format(java.util.Locale.ROOT, "%02d:%02d", min / 60, min % 60);
    }

    static String ultimaPantalla(Context c) {
        return p(c).getString("ultima_pantalla", "");
    }

    static void setUltimaPantalla(Context c, String texto) {
        p(c).edit().putString("ultima_pantalla", texto).apply();
    }
}
