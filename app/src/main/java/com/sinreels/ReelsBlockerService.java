package com.sinreels;

import android.accessibilityservice.AccessibilityService;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

/**
 * Vigila la app de Instagram. Si detecta la pestaña o el visor de Reels,
 * te devuelve al inicio (o pulsa "atrás").
 */
public class ReelsBlockerService extends AccessibilityService {

    private static final String IG = "com.instagram.android";

    // IDs de la pantalla de Reels conocidos. Si Instagram los cambia,
    // se pueden añadir otros desde la app sin recompilar.
    private static final String[] IDS_REELS = {
            IG + ":id/clips_viewer_view_pager",
            IG + ":id/clips_viewer_container",
            IG + ":id/clips_viewer_root",
    };
    private static final String ID_TAB_REELS = IG + ":id/clips_tab";
    private static final String ID_TAB_INICIO = IG + ":id/feed_tab";

    private long ultimaAccion = 0;
    private long ultimoDiagnostico = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        CharSequence pkg = event.getPackageName();
        if (pkg == null || !IG.contentEquals(pkg)) return;

        AccessibilityNodeInfo raiz = getRootInActiveWindow();
        if (raiz == null) return;

        long ahora = SystemClock.uptimeMillis();

        if (Prefs.diagnostico(this) && ahora - ultimoDiagnostico > 1000) {
            ultimoDiagnostico = ahora;
            Prefs.setUltimaPantalla(this, volcarIds(raiz));
        }

        // Evita reaccionar varias veces seguidas mientras Instagram cambia de pantalla
        if (ahora - ultimaAccion < 800) return;

        boolean enPestanaReels = algunoSeleccionado(raiz, ID_TAB_REELS);
        if (enPestanaReels || hayVisorDeReels(raiz)) {
            ultimaAccion = ahora;
            boolean volvioAlInicio = enPestanaReels && pulsar(raiz, ID_TAB_INICIO);
            if (!volvioAlInicio) {
                performGlobalAction(GLOBAL_ACTION_BACK);
            }
            Prefs.sumarBloqueo(this);
            Toast.makeText(this, "Reels bloqueado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInterrupt() {
        // Nada que hacer
    }

    private boolean hayVisorDeReels(AccessibilityNodeInfo raiz) {
        for (String id : idsAComprobar()) {
            List<AccessibilityNodeInfo> nodos = raiz.findAccessibilityNodeInfosByViewId(id);
            if (nodos == null) continue;
            for (AccessibilityNodeInfo n : nodos) {
                if (n != null && n.isVisibleToUser()) return true;
            }
        }
        return false;
    }

    private List<String> idsAComprobar() {
        List<String> ids = new ArrayList<>();
        for (String id : IDS_REELS) ids.add(id);
        for (String linea : Prefs.idsExtra(this).split("\n")) {
            String id = linea.trim();
            if (id.isEmpty()) continue;
            if (!id.contains(":")) id = IG + ":id/" + id;
            ids.add(id);
        }
        return ids;
    }

    private boolean algunoSeleccionado(AccessibilityNodeInfo raiz, String id) {
        List<AccessibilityNodeInfo> nodos = raiz.findAccessibilityNodeInfosByViewId(id);
        if (nodos == null) return false;
        for (AccessibilityNodeInfo n : nodos) {
            if (n != null && n.isSelected()) return true;
        }
        return false;
    }

    private boolean pulsar(AccessibilityNodeInfo raiz, String id) {
        List<AccessibilityNodeInfo> nodos = raiz.findAccessibilityNodeInfosByViewId(id);
        if (nodos == null) return false;
        for (AccessibilityNodeInfo n : nodos) {
            AccessibilityNodeInfo t = n;
            while (t != null && !t.isClickable()) t = t.getParent();
            if (t != null && t.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true;
        }
        return false;
    }

    /** Lista los IDs visibles en pantalla, para poder ajustar la detección si Instagram cambia. */
    private String volcarIds(AccessibilityNodeInfo raiz) {
        TreeSet<String> ids = new TreeSet<>();
        ArrayDeque<AccessibilityNodeInfo> cola = new ArrayDeque<>();
        cola.add(raiz);
        int vistos = 0;
        while (!cola.isEmpty() && vistos < 3000) {
            AccessibilityNodeInfo n = cola.poll();
            vistos++;
            if (n == null) continue;
            String id = n.getViewIdResourceName();
            if (id != null && n.isVisibleToUser()) {
                ids.add(n.isSelected() ? id + "  (seleccionado)" : id);
            }
            for (int i = 0; i < n.getChildCount(); i++) {
                AccessibilityNodeInfo hijo = n.getChild(i);
                if (hijo != null) cola.add(hijo);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Pantalla capturada: ")
                .append(DateFormat.getTimeInstance().format(new Date()))
                .append("\n\n");
        for (String id : ids) sb.append(id.replace(IG + ":id/", "")).append('\n');
        return sb.toString();
    }
}
