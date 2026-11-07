package com.gathin.tvhome;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** A real, customisable eight-by-two Android TV home, designed for low-memory hardware. */
public class CafeHomeActivity extends Activity {
    private static final String PREFS = "cafe_edition_settings";
    private static final String SLOT = "slot_";
    private static final String WALLPAPER = "cafe_wallpaper";
    private static final String YOUTUBE = "com.google.android.youtube.tv";
    private static final String NETFLIX = "com.netflix.ninja";
    private static final String PRIME = "com.amazon.amazonvideo.livingroom";
    private static final String PLAY = "com.android.vending";
    private static final String[] FIRST_APPS = {YOUTUBE, NETFLIX, PRIME, PLAY};
    private static final String[] TOOLS = {"meditation", "ambience", "coffee", "network", "bluetooth",
            "settings", "maintenance", "allapps", "customize", "storage", "display", "info"};

    private final ArrayList<LaunchItem> installed = new ArrayList<>();
    private final ArrayList<View> tiles = new ArrayList<>();
    private final ArrayList<View> navButtons = new ArrayList<>();
    private final ArrayList<View> experiences = new ArrayList<>();
    private LinearLayout rowOne, rowTwo;
    private ImageView wallpaper;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable statusRefresh = new Runnable() {
        @Override public void run() { updateStatus(); handler.postDelayed(this, 60000L); }
    };
    private static final class LaunchItem {
        final String code;
        final String label;
        LaunchItem(String c, String n) { code = c; label = n; }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_cafe_home);
        wallpaper = findViewById(R.id.cafeWallpaper);
        rowOne = findViewById(R.id.cafeRowOne);
        rowTwo = findViewById(R.id.cafeRowTwo);
        discoverApps();
        createNavigation();
        createExperiences();
        createTools();
        fillGrid();
        findViewById(R.id.cafeStorage).setOnClickListener(v -> storageDialog());
        findViewById(R.id.cafeRam).setOnClickListener(v -> storageDialog());
        findViewById(R.id.cafeNetwork).setOnClickListener(v -> networkDialog());
        findViewById(R.id.cafeCustomize).setOnClickListener(v -> customizeGrid());
        applyWallpaper();
        if (!tiles.isEmpty()) tiles.get(0).requestFocus();
    }

    @Override protected void onResume() {
        super.onResume();
        handler.removeCallbacks(statusRefresh);
        statusRefresh.run();
    }
    @Override protected void onPause() { handler.removeCallbacks(statusRefresh); super.onPause(); }
    @Override protected void onDestroy() { handler.removeCallbacks(statusRefresh); super.onDestroy(); }

    private int dp(float x) { return Math.round(x * getResources().getDisplayMetrics().density); }

    private void discoverApps() {
        installed.clear();
        PackageManager pm = getPackageManager();
        Map<String, String> labels = new LinkedHashMap<>();
        for (String category : Arrays.asList(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            Intent finder = new Intent(Intent.ACTION_MAIN).addCategory(category);
            List<ResolveInfo> results = pm.queryIntentActivities(finder, 0);
            for (ResolveInfo ri : results) {
                if (ri.activityInfo == null) continue;
                String pkg = ri.activityInfo.packageName;
                if (pkg == null || pkg.startsWith("com.gathin.tvhome") ||
                        pkg.equals("com.google.android.tvlauncher")) continue;
                String label = String.valueOf(ri.loadLabel(pm));
                if (label.trim().isEmpty()) label = pkg;
                if (!labels.containsKey(pkg)) labels.put(pkg, label);
            }
        }
        for (String pkg : FIRST_APPS) {
            if (!labels.containsKey(pkg)) {
                try { labels.put(pkg, String.valueOf(pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)))); }
                catch (PackageManager.NameNotFoundException ignored) { /* the package may be absent */ }
            }
        }
        for (Map.Entry<String, String> entry : labels.entrySet())
            installed.add(new LaunchItem("pkg:" + entry.getKey(), entry.getValue()));
        installed.sort(Comparator.comparing(i -> i.label.toLowerCase(new Locale("es", "CO"))));
    }

    private String initialSlot(int index) {
        if (index < FIRST_APPS.length) return "pkg:" + FIRST_APPS[index];
        ArrayList<String> options = new ArrayList<>();
        for (LaunchItem item : installed) {
            boolean alreadyPinned = false;
            for (String pkg : FIRST_APPS) if (item.code.equals("pkg:" + pkg)) alreadyPinned = true;
            if (!alreadyPinned) options.add(item.code);
        }
        int remainingIndex = index - FIRST_APPS.length;
        if (remainingIndex < options.size()) return options.get(remainingIndex);
        return "action:" + TOOLS[(remainingIndex - options.size()) % TOOLS.length];
    }

    private String codeForSlot(int index) {
        return getSharedPreferences(PREFS, MODE_PRIVATE).getString(SLOT + index, initialSlot(index));
    }
    private String actionLabel(String action) {
        switch (action) {
            case "meditation": return "Meditación";
            case "ambience": return "Ambientes";
            case "coffee": return "Café y origen";
            case "network": return "Redes";
            case "bluetooth": return "Bluetooth";
            case "settings": return "Ajustes del TV";
            case "maintenance": return "Mantenimiento";
            case "allapps": return "Todas las apps";
            case "customize": return "Personalizar";
            case "storage": return "Memoria";
            case "display": return "Pantalla";
            case "info": return "Información";
            default: return "Herramienta";
        }
    }
    private String actionSymbol(String action) {
        switch (action) {
            case "meditation": return "✦";
            case "ambience": return "◈";
            case "coffee": return "☕";
            case "network": return "◎";
            case "bluetooth": return "ᛒ";
            case "settings": return "⚙";
            case "maintenance": return "☷";
            case "allapps": return "▦";
            case "customize": return "✎";
            case "storage": return "▰";
            case "display": return "▣";
            default: return "ⓘ";
        }
    }
    private String labelForCode(String code) {
        if (code.startsWith("action:")) return actionLabel(code.substring(7));
        for (LaunchItem item : installed) if (item.code.equals(code)) return item.label;
        if (code.equals("pkg:" + YOUTUBE)) return "YouTube";
        if (code.equals("pkg:" + NETFLIX)) return "Netflix";
        if (code.equals("pkg:" + PRIME)) return "Prime Video";
        if (code.equals("pkg:" + PLAY)) return "Play Store";
        return "App no disponible";
    }

    private void fillGrid() {
        rowOne.removeAllViews(); rowTwo.removeAllViews(); tiles.clear();
        for (int index = 0; index < 16; index++) {
            final int slot = index;
            String code = codeForSlot(slot);
            LinearLayout card = new LinearLayout(this);
            card.setId(View.generateViewId());
            card.setBackgroundResource(R.drawable.cafe_tile);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER);
            card.setFocusable(true); card.setClickable(true); card.setLongClickable(true);
            card.setPadding(dp(3), dp(3), dp(3), dp(3));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
            if ((index % 8) != 7) lp.rightMargin = dp(5);
            (index < 8 ? rowOne : rowTwo).addView(card, lp);
            if (code.startsWith("pkg:")) {
                ImageView icon = new ImageView(this);
                icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(31), dp(31));
                card.addView(icon, iconLp);
                try {
                    Drawable drawable = getPackageManager().getApplicationIcon(code.substring(4));
                    icon.setImageDrawable(drawable);
                } catch (PackageManager.NameNotFoundException ignored) {
                    icon.setImageResource(android.R.drawable.sym_def_app_icon);
                }
            } else {
                TextView symbol = new TextView(this);
                symbol.setText(actionSymbol(code.substring(7)));
                symbol.setTextSize(25);
                symbol.setTextColor(0xFFFFBA6B);
                symbol.setGravity(Gravity.CENTER);
                card.addView(symbol, new LinearLayout.LayoutParams(-1, dp(33)));
            }
            TextView label = new TextView(this);
            label.setText(labelForCode(code));
            label.setTextColor(0xFFFFFFFF); label.setTextSize(10);
            label.setGravity(Gravity.CENTER); label.setMaxLines(2);
            label.setEllipsize(TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(-1, dp(30));
            labelLp.topMargin = dp(2);
            card.addView(label, labelLp);
            card.setContentDescription(labelForCode(code) + ". Mantén OK para personalizar.");
            card.setOnClickListener(v -> runCode(code));
            card.setOnLongClickListener(v -> { chooseSlot(slot); return true; });
            focusScale(card);
            tiles.add(card);
        }
        for (int i=0; i<16; i++) {
            View v = tiles.get(i);
            v.setNextFocusLeftId(i % 8 == 0 ? navButtons.get(0).getId() : tiles.get(i-1).getId());
            v.setNextFocusRightId(i % 8 == 7 ? v.getId() : tiles.get(i+1).getId());
            v.setNextFocusUpId(i < 8 ? R.id.cafeCustomize : tiles.get(i-8).getId());
            v.setNextFocusDownId(i < 8 ? tiles.get(i+8).getId() : experiences.get(Math.min(3, i % 8 / 2)).getId());
        }
        for (View nav : navButtons) nav.setNextFocusRightId(tiles.get(0).getId());
        for (int i=0; i<experiences.size(); i++) {
            View v = experiences.get(i);
            v.setNextFocusUpId(tiles.get(8 + Math.min(7, i*2)).getId());
            v.setNextFocusLeftId(i == 0 ? navButtons.get(0).getId() : experiences.get(i-1).getId());
            v.setNextFocusRightId(i == experiences.size()-1 ? v.getId() : experiences.get(i+1).getId());
        }
    }

    private void createNavigation() {
        LinearLayout rail = findViewById(R.id.cafeNav);
        rail.removeAllViews(); navButtons.clear();
        addNav(rail, "⌂  Inicio", () -> tiles.get(0).requestFocus());
        addNav(rail, "✦  Meditación", this::chooseMeditation);
        addNav(rail, "◈  Ambientes", this::chooseWallpaper);
        addNav(rail, "☕  Café y origen", this::coffeeDialog);
        addNav(rail, "▦  Aplicaciones", this::allAppsDialog);
        addNav(rail, "◎  Redes", this::networkDialog);
        addNav(rail, "☷  Mantenimiento", this::maintenanceDialog);
        addNav(rail, "⚙  Configuración", () -> openSystem(Settings.ACTION_SETTINGS));
        addNav(rail, "✎  Personalizar", this::customizeGrid);
        for (int i=0; i<navButtons.size(); i++) {
            View v = navButtons.get(i);
            v.setNextFocusUpId(navButtons.get(Math.max(0,i-1)).getId());
            v.setNextFocusDownId(navButtons.get(Math.min(navButtons.size()-1,i+1)).getId());
            v.setNextFocusLeftId(v.getId());
        }
    }
    private void addNav(LinearLayout rail, String title, Runnable action) {
        TextView v = new TextView(this);
        v.setId(View.generateViewId());
        v.setText(title); v.setTextSize(13); v.setTextColor(0xFFFFE1BA);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(10), 0, dp(4), 0);
        v.setFocusable(true); v.setClickable(true);
        v.setBackgroundResource(R.drawable.cafe_nav);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(37));
        lp.bottomMargin = dp(3); rail.addView(v, lp);
        v.setOnClickListener(view -> action.run());
        focusScale(v); navButtons.add(v);
    }

    private void createExperiences() {
        LinearLayout root = findViewById(R.id.cafeExperiences);
        root.removeAllViews(); experiences.clear();
        addExperience(root, "✦ Meditación", "Temporizador 5–30 min", this::chooseMeditation);
        addExperience(root, "☕ Café y origen", "Preparaciones y cultura", this::coffeeDialog);
        addExperience(root, "◈ Ambientes", "Cambia el fondo", this::chooseWallpaper);
        addExperience(root, "☷ Mantenimiento", "Memoria y aplicaciones", this::maintenanceDialog);
    }
    private void addExperience(LinearLayout root, String title, String subtitle, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setId(View.generateViewId()); card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER); card.setBackgroundResource(R.drawable.cafe_chip);
        card.setFocusable(true); card.setClickable(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
        lp.rightMargin = dp(6); root.addView(card, lp);
        TextView header = new TextView(this);
        header.setText(title); header.setTextColor(0xFFFFFFFF); header.setTextSize(15);
        header.setGravity(Gravity.CENTER); header.setTypeface(null, 1);
        card.addView(header, new LinearLayout.LayoutParams(-1, dp(30)));
        TextView sub = new TextView(this);
        sub.setText(subtitle); sub.setTextColor(0xFFEFD2AA); sub.setTextSize(10);
        sub.setGravity(Gravity.CENTER); sub.setSingleLine(true);
        card.addView(sub, new LinearLayout.LayoutParams(-1, dp(21)));
        card.setOnClickListener(v -> action.run());
        focusScale(card); experiences.add(card);
    }
    private void createTools() {
        LinearLayout tools = findViewById(R.id.cafeTools);
        tools.removeAllViews();
        addTool(tools, "◎ Redes", this::networkDialog);
        addTool(tools, "▰ Memoria", this::storageDialog);
        addTool(tools, "✎ Cambiar apps", this::customizeGrid);
        addTool(tools, "◈ Fondo", this::chooseWallpaper);
        addTool(tools, "⚙ Ajustes", () -> openSystem(Settings.ACTION_SETTINGS));
    }
    private void addTool(LinearLayout tools, String title, Runnable action) {
        TextView t = new TextView(this);
        t.setText(title); t.setTextColor(0xFFFFFFFF); t.setTextSize(11);
        t.setGravity(Gravity.CENTER); t.setBackgroundResource(R.drawable.cafe_chip);
        t.setFocusable(true); t.setClickable(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
        lp.rightMargin = dp(6); tools.addView(t, lp);
        t.setOnClickListener(v -> action.run()); focusScale(t);
    }
    private void focusScale(View v) {
        v.setOnFocusChangeListener((view, focused) -> {
            view.animate().cancel();
            view.animate().scaleX(focused ? 1.035f : 1f).scaleY(focused ? 1.035f : 1f)
                    .setDuration(100).start();
        });
    }

    private void updateStatus() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        String hello = hour < 12 ? "Buenos días" : hour < 18 ? "Buenas tardes" : "Buenas noches";
        ((TextView)findViewById(R.id.cafeGreeting)).setText(hello + ", Carlos");
        String date = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("es", "CO")).format(now.getTime());
        ((TextView)findViewById(R.id.cafeSubtitle)).setText("Café Edition  ·  " + date + "  ·  16 accesos personalizables");
        ((TextView)findViewById(R.id.cafeStorage)).setText("Almac. " + storageFree());
        ((TextView)findViewById(R.id.cafeRam)).setText("RAM " + ramFree());
        ((TextView)findViewById(R.id.cafeNetwork)).setText("◎ " + networkLabel());
    }
    private String size(long bytes) {
        double gb = bytes / 1073741824.0;
        if (gb >= 1.0) return String.format(Locale.US, "%.1f GB", gb);
        return String.format(Locale.US, "%.0f MB", bytes / 1048576.0);
    }
    private String storageFree() {
        try { return size(new StatFs(Environment.getDataDirectory().getAbsolutePath()).getAvailableBytes()); }
        catch (RuntimeException e) { return "N/D"; }
    }
    private String ramFree() {
        try {
            ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            if (manager == null) return "N/D";
            manager.getMemoryInfo(info);
            return size(info.availMem);
        } catch (RuntimeException e) { return "N/D"; }
    }
    private String networkLabel() {
        try {
            ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            Network active=cm == null ? null : cm.getActiveNetwork();
            NetworkCapabilities nc=active == null ? null : cm.getNetworkCapabilities(active);
            if (nc == null) return "Sin red";
            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return "Ethernet";
            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "Wi-Fi";
            return "Conectado";
        } catch (RuntimeException e) { return "Red"; }
    }

    private void storageDialog() {
        String message;
        try {
            StatFs storage=new StatFs(Environment.getDataDirectory().getAbsolutePath());
            ActivityManager mgr=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info=new ActivityManager.MemoryInfo();
            if (mgr != null) mgr.getMemoryInfo(info);
            message="Almacenamiento interno disponible: " + size(storage.getAvailableBytes()) +
                    "\nAlmacenamiento interno total: " + size(storage.getTotalBytes()) +
                    "\n\nRAM disponible: " + size(info.availMem) +
                    "\nRAM total: " + size(info.totalMem) +
                    "\n\nEl espacio libre y la RAM se actualizan automáticamente.";
        } catch (RuntimeException e) { message="Los datos de memoria no están disponibles."; }
        new AlertDialog.Builder(this).setTitle("Memoria y almacenamiento")
                .setMessage(message)
                .setPositiveButton("Almacenamiento", (d, w) -> openSystem("android.settings.INTERNAL_STORAGE_SETTINGS"))
                .setNeutralButton("Gestionar apps", (d, w) -> openSystem(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
                .setNegativeButton("Cerrar", null).show();
    }
    private void networkDialog() {
        new AlertDialog.Builder(this).setTitle("Redes · " + networkLabel())
                .setItems(new String[] {"Configurar Wi-Fi", "Configurar Bluetooth", "Opciones de red", "Ver estado"},
                (d, index) -> {
                    switch (index) {
                        case 0: openSystem(Settings.ACTION_WIFI_SETTINGS); break;
                        case 1: openSystem(Settings.ACTION_BLUETOOTH_SETTINGS); break;
                        case 2: openSystem(Settings.ACTION_WIRELESS_SETTINGS); break;
                        default: new AlertDialog.Builder(this).setTitle("Estado de red")
                                .setMessage("Conexión detectada: " + networkLabel() +
                                        "\nPara cambiar de red, abre los ajustes de Wi-Fi.")
                                .setPositiveButton("Cerrar", null).show();
                    }
                }).setNegativeButton("Cerrar", null).show();
    }
    private void chooseWallpaper() {
        String[] names={"Café colombiano", "Meditación Jedi", "Noche serena"};
        int chosen=getSharedPreferences(PREFS, MODE_PRIVATE).getInt(WALLPAPER, 0);
        new AlertDialog.Builder(this).setTitle("Cambiar ambiente y fondo")
                .setSingleChoiceItems(names, chosen, (d, value) -> {
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(WALLPAPER, value).apply();
                    applyWallpaper(); d.dismiss();
                }).setNegativeButton("Cancelar", null).show();
    }
    private void applyWallpaper() {
        if (wallpaper == null) return;
        int value=getSharedPreferences(PREFS, MODE_PRIVATE).getInt(WALLPAPER, 0);
        wallpaper.setImageResource(value == 1 ? R.drawable.jedi_wallpaper :
                value == 2 ? R.drawable.jedi_wallpaper_night : R.drawable.cafe_wallpaper);
    }
    private void chooseMeditation() {
        final int[] times={5,10,20,30};
        new AlertDialog.Builder(this).setTitle("Meditación guiada")
                .setItems(new String[]{"5 minutos", "10 minutos", "20 minutos", "30 minutos"},
                        (d, i) -> {
                            Intent intent=new Intent(this, JediMeditationActivity.class);
                            intent.putExtra("minutes", times[i]); startActivity(intent);
                        }).setNegativeButton("Cancelar", null).show();
    }
    private void coffeeDialog() {
        new AlertDialog.Builder(this).setTitle("Café de Colombia")
                .setMessage("Explora preparaciones, métodos de filtrado y cultura cafetera.\n\n" +
                        "Esta opción abre resultados educativos en YouTube; no hace compras ni activa suscripciones.")
                .setPositiveButton("Explorar videos", (d,w) -> {
                    try { startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/results?search_query=cafe+colombiano+metodos+de+preparacion"))); }
                    catch (Exception e) { toast("No se pudo abrir YouTube"); }
                })
                .setNegativeButton("Cerrar", null).show();
    }
    private void maintenanceDialog() {
        new AlertDialog.Builder(this).setTitle("Mantenimiento seguro")
                .setMessage("Disponible: " + storageFree() + " de almacenamiento y " + ramFree() +
                        " de RAM.\n\nLa limpieza se hace desde Android, sin deshabilitar servicios esenciales.")
                .setPositiveButton("Administrar apps", (d,w) -> openSystem(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
                .setNeutralButton("Memoria", (d,w) -> storageDialog())
                .setNegativeButton("Cerrar", null).show();
    }
    private void customizeGrid() {
        String[] titles=new String[16];
        for (int i=0;i<titles.length;i++) titles[i] = (i+1) + ". " + labelForCode(codeForSlot(i));
        new AlertDialog.Builder(this).setTitle("Personalizar los 16 accesos")
                .setItems(titles, (d, index) -> chooseSlot(index))
                .setPositiveButton("Restablecer todos", (d,w) -> new AlertDialog.Builder(this)
                        .setTitle("Restaurar accesos")
                        .setMessage("¿Deseas volver a los accesos detectados del televisor?")
                        .setPositiveButton("Restaurar", (confirm, which) -> {
                            SharedPreferences.Editor e=getSharedPreferences(PREFS,MODE_PRIVATE).edit();
                            for(int i=0;i<16;i++) e.remove(SLOT+i);
                            e.apply(); fillGrid(); tiles.get(0).requestFocus();
                        }).setNegativeButton("Cancelar",null).show())
                .setNegativeButton("Cerrar", null).show();
    }
    private void chooseSlot(int position) {
        ArrayList<LaunchItem> choices=new ArrayList<>(installed);
        for (String tool:TOOLS) choices.add(new LaunchItem("action:" + tool, "Herramienta · " + actionLabel(tool)));
        String[] labels=new String[choices.size()];
        for(int i=0;i<choices.size();i++) labels[i]=choices.get(i).label;
        new AlertDialog.Builder(this).setTitle("Acceso " + (position+1) + " · elegir app o herramienta")
                .setItems(labels, (d, index) -> {
                    getSharedPreferences(PREFS,MODE_PRIVATE).edit()
                            .putString(SLOT+position, choices.get(index).code).apply();
                    fillGrid(); tiles.get(position).requestFocus();
                }).setNegativeButton("Cancelar", null).show();
    }
    private void allAppsDialog() {
        if (installed.isEmpty()) { toast("No se encontraron aplicaciones disponibles"); return; }
        String[] labels=new String[installed.size()];
        for(int i=0;i<labels.length;i++)labels[i]=installed.get(i).label;
        new AlertDialog.Builder(this).setTitle("Aplicaciones instaladas")
                .setItems(labels,(d,index)->runCode(installed.get(index).code))
                .setNegativeButton("Cerrar",null).show();
    }
    private void runCode(String code) {
        if (code.startsWith("pkg:")) { openPackage(code.substring(4)); return; }
        String action=code.substring(7);
        switch (action) {
            case "meditation": chooseMeditation(); break;
            case "ambience": chooseWallpaper(); break;
            case "coffee": coffeeDialog(); break;
            case "network": networkDialog(); break;
            case "bluetooth": openSystem(Settings.ACTION_BLUETOOTH_SETTINGS); break;
            case "settings": openSystem(Settings.ACTION_SETTINGS); break;
            case "maintenance": maintenanceDialog(); break;
            case "allapps": allAppsDialog(); break;
            case "customize": customizeGrid(); break;
            case "storage": storageDialog(); break;
            case "display": openSystem(Settings.ACTION_DISPLAY_SETTINGS); break;
            case "info": new AlertDialog.Builder(this).setTitle("CleanYourTV Café Edition")
                    .setMessage("Versión de prueba · 16 accesos personalizables.\n" +
                            "El launcher Jedi Edition anterior permanece disponible.")
                    .setPositiveButton("Cerrar",null).show(); break;
            default: toast("Acción no disponible");
        }
    }
    private void openPackage(String pkg) {
        PackageManager pm=getPackageManager();
        Intent launch=pm.getLeanbackLaunchIntentForPackage(pkg);
        if (launch == null) launch=pm.getLaunchIntentForPackage(pkg);
        if (launch != null) {
            try { startActivity(launch); return; }
            catch (Exception ignored) { /* Try the app store below. */ }
        }
        if (!pkg.equals(PLAY)) {
            new AlertDialog.Builder(this).setTitle("Aplicación no disponible")
                    .setMessage("No se encontró " + labelForCode("pkg:" + pkg) +
                            ". ¿Quieres buscarla en Play Store?")
                    .setPositiveButton("Buscar", (d,w) -> {
                        try { startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + pkg)).setPackage(PLAY)); }
                        catch (Exception e) { toast("No se pudo abrir Play Store"); }
                    }).setNegativeButton("Cancelar",null).show();
        } else toast("Play Store no está disponible en el televisor");
    }
    private void openSystem(String action) {
        try { startActivity(new Intent(action)); }
        catch (ActivityNotFoundException e) {
            try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
            catch (Exception ignored) { toast("Este ajuste no está disponible en el Challenger"); }
        } catch (SecurityException e) { toast("Android restringe este ajuste"); }
    }
    private void toast(String text) { Toast.makeText(this,text,Toast.LENGTH_LONG).show(); }
}
