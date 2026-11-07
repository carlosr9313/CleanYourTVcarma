package com.gathin.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Locale;

/** Jedi 2.0: native TV home, intentionally light on memory and dependent on no internet UI calls. */
public class JediHomeActivity extends Activity {
    private static final String YOUTUBE = "com.google.android.youtube.tv";
    private static final String NETFLIX = "com.netflix.ninja";
    private static final String PRIME = "com.amazon.amazonvideo.livingroom";
    private static final String PLAY = "com.android.vending";
    private static final String PREFS = "jedi_home_2";
    private static final String BACKGROUND = "background";
    private ImageView backdrop;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_jedi_home);
        backdrop = findViewById(R.id.jediBackdrop);
        applyBackdrop();
        bindApp(R.id.jediYouTube, R.id.jediYouTubeIcon, YOUTUBE);
        bindApp(R.id.jediNetflix, R.id.jediNetflixIcon, NETFLIX);
        bindApp(R.id.jediPrime, R.id.jediPrimeIcon, PRIME);
        bindApp(R.id.jediPlayStore, R.id.jediPlayStoreIcon, PLAY);
        findViewById(R.id.jediNavHome).setOnClickListener(v -> findViewById(R.id.jediYouTube).requestFocus());
        findViewById(R.id.jediNavApps).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.jediNavSettings).setOnClickListener(v -> openSettings());
        findViewById(R.id.jediNavMaintenance).setOnClickListener(v -> showMaintenance());
        findViewById(R.id.jediNavInfo).setOnClickListener(v -> showInfo());
        findViewById(R.id.jediMeditation).setOnClickListener(v -> chooseMeditation());
        findViewById(R.id.jediAmbiences).setOnClickListener(v -> chooseAmbience());
        findViewById(R.id.jediConfig).setOnClickListener(v -> openSettings());
        findViewById(R.id.jediMaintenance).setOnClickListener(v -> showMaintenance());
        findViewById(R.id.jediWeather).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Clima")
                .setMessage("Bogotá · La información meteorológica en directo se incorporará en una próxima actualización.")
                .setPositiveButton("Entendido", null).show());
        setupDpad();
        findViewById(R.id.jediYouTube).requestFocus();
    }

    @Override protected void onResume() {
        super.onResume();
        updateStatus();
        applyBackdrop();
    }

    private void updateStatus() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Buenos días" : hour < 18 ? "Buenas tardes" : "Buenas noches";
        ((TextView)findViewById(R.id.jediGreeting)).setText(greeting + ", Carlos");
        ((TextView)findViewById(R.id.jediStorage)).setText(storageText());
        try {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm == null ? null : cm.getActiveNetworkInfo();
            ((TextView)findViewById(R.id.jediConnectivity)).setText(
                    info != null && info.isConnected() ? "●  Conectado" : "○  Sin red");
        } catch (RuntimeException e) {
            ((TextView)findViewById(R.id.jediConnectivity)).setText("Estado de red");
        }
    }

    private String storageText() {
        try {
            StatFs stats = new StatFs(Environment.getDataDirectory().getAbsolutePath());
            double freeGB = stats.getAvailableBytes() / (1024.0 * 1024.0 * 1024.0);
            return String.format(new Locale("es", "CO"), "%.1f GB libres", freeGB);
        } catch (RuntimeException e) { return "Almacenamiento"; }
    }

    private void applyBackdrop() {
        if (backdrop == null) return;
        int choice = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(BACKGROUND, 0);
        backdrop.setImageResource(choice == 0 ? R.drawable.jedi_wallpaper : R.drawable.jedi_wallpaper_night);
    }

    private void chooseAmbience() {
        String[] modes = {"Atardecer Jedi", "Noche serena"};
        int current = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(BACKGROUND, 0);
        new AlertDialog.Builder(this).setTitle("Ambientes · Fondo de pantalla")
                .setSingleChoiceItems(modes, current, (dialog, selected) -> {
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(BACKGROUND, selected).apply();
                    applyBackdrop();
                    dialog.dismiss();
                }).setNegativeButton("Cancelar", null).show();
    }

    private void chooseMeditation() {
        final int[] minutes = {5, 10, 20, 30};
        String[] options = {"5 minutos", "10 minutos", "20 minutos", "30 minutos"};
        new AlertDialog.Builder(this).setTitle("Meditación Jedi · Duración")
                .setItems(options, (dialog, index) -> {
                    Intent intent = new Intent(this, JediMeditationActivity.class);
                    intent.putExtra("minutes", minutes[index]);
                    startActivity(intent);
                }).setNegativeButton("Cancelar", null).show();
    }

    private void showMaintenance() {
        new AlertDialog.Builder(this).setTitle("Mantenimiento del Challenger")
                .setMessage("Espacio disponible: " + storageText() +
                        "\n\nPuedes administrar las aplicaciones desde los ajustes de Android. " +
                        "Esta pantalla no borra servicios ni elimina paquetes automáticamente.")
                .setPositiveButton("Administrar aplicaciones", (d, w) -> openSystem(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
                .setNegativeButton("Cerrar", null).show();
    }

    private void showInfo() {
        new AlertDialog.Builder(this).setTitle("CleanYourTV · Jedi Edition 2.0")
                .setMessage("Prototipo nativo para Android TV.\n" +
                        "Fondos: Atardecer y Noche serena.\n" +
                        "Meditación: temporizador integrado.\n" +
                        "Tu launcher Jedi Edition anterior permanece instalado como respaldo.")
                .setPositiveButton("Cerrar", null).show();
    }

    private void bindApp(int tile, int iconId, String pkg) {
        ImageView icon = findViewById(iconId);
        try {
            Drawable image = getPackageManager().getApplicationIcon(pkg);
            icon.setImageDrawable(image);
        } catch (PackageManager.NameNotFoundException e) {
            icon.setVisibility(View.GONE);
        }
        findViewById(tile).setOnClickListener(v -> openPackage(pkg));
    }

    private void openPackage(String pkg) {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLeanbackLaunchIntentForPackage(pkg);
        if (intent == null) intent = pm.getLaunchIntentForPackage(pkg);
        if (intent == null) {
            Toast.makeText(this, "Aplicación no disponible: " + pkg, Toast.LENGTH_LONG).show();
            return;
        }
        try { startActivity(intent); }
        catch (Exception e) { Toast.makeText(this, "No se pudo abrir la aplicación", Toast.LENGTH_SHORT).show(); }
    }

    private void openSettings() {
        try { startActivity(new Intent(this, LauncherSettingsActivity.class)); }
        catch (Exception e) { openSystem(Settings.ACTION_SETTINGS); }
    }

    private void openSystem(String action) {
        try { startActivity(new Intent(action)); }
        catch (ActivityNotFoundException e) {
            try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
            catch (Exception ignored) { Toast.makeText(this, "Configuración no disponible", Toast.LENGTH_SHORT).show(); }
        }
    }

    private void connect(int left, int right, int up, int down) {
        View v = findViewById(left);
        v.setNextFocusRightId(right); v.setNextFocusUpId(up); v.setNextFocusDownId(down);
    }

    private void focusEffect(int id) {
        View v = findViewById(id);
        v.setOnFocusChangeListener((view, active) -> {
            view.animate().cancel();
            view.animate().scaleX(active ? 1.025f : 1f)
                    .scaleY(active ? 1.025f : 1f).setDuration(110).start();
        });
    }

    private void setupDpad() {
        int[] rail = {R.id.jediNavHome, R.id.jediNavApps, R.id.jediNavSettings,
                R.id.jediNavMaintenance, R.id.jediNavInfo};
        int[] apps = {R.id.jediYouTube,R.id.jediNetflix,R.id.jediPrime,R.id.jediPlayStore};
        int[] explore = {R.id.jediMeditation,R.id.jediAmbiences,R.id.jediConfig,R.id.jediMaintenance};
        for (int i=0; i<rail.length; i++) {
            View v=findViewById(rail[i]);
            v.setNextFocusRightId(apps[Math.min(i,3)]);
            v.setNextFocusUpId(rail[Math.max(0,i-1)]);
            v.setNextFocusDownId(rail[Math.min(rail.length-1,i+1)]);
            focusEffect(rail[i]);
        }
        for (int i=0;i<apps.length;i++) {
            View a=findViewById(apps[i]);
            a.setNextFocusLeftId(i==0?R.id.jediNavHome:apps[i-1]);
            a.setNextFocusRightId(apps[Math.min(i+1,apps.length-1)]);
            a.setNextFocusDownId(explore[i]);
            a.setNextFocusUpId(R.id.jediNavHome);
            focusEffect(apps[i]);
            View e=findViewById(explore[i]);
            e.setNextFocusUpId(apps[i]);
            e.setNextFocusDownId(explore[i]);
            e.setNextFocusLeftId(i==0?R.id.jediNavMaintenance:explore[i-1]);
            e.setNextFocusRightId(explore[Math.min(i+1,explore.length-1)]);
            focusEffect(explore[i]);
        }
        findViewById(R.id.jediWeather).setNextFocusDownId(R.id.jediYouTube);
    }
}
