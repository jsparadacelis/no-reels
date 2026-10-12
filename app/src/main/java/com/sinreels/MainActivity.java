package com.sinreels;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/** Pantalla de la app: estado del bloqueo, acceso a ajustes y modo diagnóstico. */
public class MainActivity extends Activity {

    private TextView estado;
    private TextView contador;
    private TextView diagnostico;
    private Switch modoDiagnostico;
    private EditText idsExtra;
    private Switch horarioActivo;
    private Button botonInicio;
    private Button botonFin;
    private TextView resumenHorario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        col.setPadding(pad, pad, pad, pad);
        scroll.addView(col);

        TextView titulo = texto("Sin Reels", 26);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(titulo);
        col.addView(texto("Cada vez que entres en Reels dentro de Instagram, te devuelve al inicio.", 15));

        estado = texto("", 17);
        estado.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(estado);

        Button ajustes = boton("Abrir ajustes de accesibilidad");
        ajustes.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        col.addView(ajustes);

        contador = texto("", 15);
        col.addView(contador);

        // ───── Horario ─────
        TextView subHorario = texto("Horario", 19);
        subHorario.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(subHorario);

        horarioActivo = new Switch(this);
        horarioActivo.setText("Bloquear solo en este horario");
        horarioActivo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        horarioActivo.setPadding(0, dp(8), 0, dp(4));
        horarioActivo.setOnCheckedChangeListener((b, on) -> {
            Prefs.setHorarioActivo(this, on);
            actualizarHorario();
        });
        col.addView(horarioActivo);

        botonInicio = boton("");
        botonInicio.setOnClickListener(v -> elegirHora(true));
        col.addView(botonInicio);

        botonFin = boton("");
        botonFin.setOnClickListener(v -> elegirHora(false));
        col.addView(botonFin);

        resumenHorario = texto("", 14);
        col.addView(resumenHorario);

        TextView sub = texto("Si deja de funcionar", 19);
        sub.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(sub);
        col.addView(texto("Instagram cambia su app de vez en cuando. Activa el modo diagnóstico, "
                + "abre Reels en Instagram, vuelve aquí y copia la lista. Con ella se puede "
                + "ajustar la detección añadiendo IDs abajo.", 14));

        modoDiagnostico = new Switch(this);
        modoDiagnostico.setText("Modo diagnóstico");
        modoDiagnostico.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        modoDiagnostico.setPadding(0, dp(12), 0, dp(4));
        modoDiagnostico.setOnCheckedChangeListener((b, on) -> Prefs.setDiagnostico(this, on));
        col.addView(modoDiagnostico);

        Button copiar = boton("Copiar IDs de la última pantalla");
        copiar.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("IDs Instagram", Prefs.ultimaPantalla(this)));
            Toast.makeText(this, "Copiado", Toast.LENGTH_SHORT).show();
        });
        col.addView(copiar);

        diagnostico = texto("", 12);
        diagnostico.setTypeface(Typeface.MONOSPACE);
        diagnostico.setTextIsSelectable(true);
        col.addView(diagnostico);

        col.addView(texto("IDs extra que cuentan como Reels (uno por línea):", 15));
        idsExtra = new EditText(this);
        idsExtra.setMinLines(3);
        idsExtra.setGravity(Gravity.TOP | Gravity.START);
        idsExtra.setTypeface(Typeface.MONOSPACE);
        col.addView(idsExtra);

        Button guardar = boton("Guardar IDs extra");
        guardar.setOnClickListener(v -> {
            Prefs.setIdsExtra(this, idsExtra.getText().toString());
            Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show();
        });
        col.addView(guardar);

        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        estado.setText(servicioActivo()
                ? "✅ Bloqueo activado"
                : "❌ Bloqueo desactivado: actívalo en Accesibilidad → Sin Reels");
        contador.setText("Reels bloqueados hasta ahora: " + Prefs.bloqueos(this));
        modoDiagnostico.setChecked(Prefs.diagnostico(this));
        String ultima = Prefs.ultimaPantalla(this);
        diagnostico.setText(ultima.isEmpty() ? "(sin datos todavía)" : ultima);
        idsExtra.setText(Prefs.idsExtra(this));
        horarioActivo.setChecked(Prefs.horarioActivo(this));
        actualizarHorario();
    }

    private void elegirHora(boolean esInicio) {
        int actual = esInicio ? Prefs.inicioMin(this) : Prefs.finMin(this);
        new TimePickerDialog(this, (picker, h, m) -> {
            if (esInicio) Prefs.setInicioMin(this, h * 60 + m);
            else Prefs.setFinMin(this, h * 60 + m);
            actualizarHorario();
        }, actual / 60, actual % 60, true).show();
    }

    private void actualizarHorario() {
        boolean on = Prefs.horarioActivo(this);
        String ini = Prefs.hhmm(Prefs.inicioMin(this));
        String fin = Prefs.hhmm(Prefs.finMin(this));
        botonInicio.setText("Empieza a bloquear: " + ini);
        botonFin.setText("Deja de bloquear: " + fin);
        botonInicio.setEnabled(on);
        botonFin.setEnabled(on);
        if (!on) {
            resumenHorario.setText("Reels bloqueado a todas horas.");
        } else {
            resumenHorario.setText("Reels bloqueado de " + ini + " a " + fin + ". Ahora mismo: "
                    + (Prefs.tocaBloquearAhora(this) ? "🔒 bloqueado" : "🔓 permitido"));
        }
    }

    private boolean servicioActivo() {
        String activos = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (activos == null) return false;
        ComponentName yo = new ComponentName(this, ReelsBlockerService.class);
        for (String s : activos.split(":")) {
            if (yo.equals(ComponentName.unflattenFromString(s))) return true;
        }
        return false;
    }

    private TextView texto(String s, int sp) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setPadding(0, dp(8), 0, dp(8));
        return t;
    }

    private Button boton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        return b;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
