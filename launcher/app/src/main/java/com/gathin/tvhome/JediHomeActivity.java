package com.gathin.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
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

/** Lightweight Android TV front door. The original MainActivity remains available from "Apps". */
public class JediHomeActivity extends Activity {
    private static final String YOUTUBE = "com.google.android.youtube.tv";
    private static final String NETFLIX = "com.netflix.ninja";
    private static final String PRIME = "com.amazon.amazonvideo.livingroom";
    private static final String PLAY = "com.android.vending";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_jedi_home);
        linkApp(R.id.jediYouTube, R.id.jediYouTubeIcon, YOUTUBE);
        linkApp(R.id.jediNetflix, R.id.jediNetflixIcon, NETFLIX);
        linkApp(R.id.jediPrime, R.id.jediPrimeIcon, PRIME);
        linkApp(R.id.jediPlayStore, R.id.jediPlayStoreIcon, PLAY);
        findViewById(R.id.jediNavHome).setOnClickListener(v -> findViewById(R.id.jediYouTube).requestFocus());
        findViewById(R.id.jediNavApps).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.jediNavSettings).setOnClickListener(v -> openLauncherSettings());
        findViewById(R.id.jediNavMaintenance).setOnClickListener(v -> openSystem(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
        findViewById(R.id.jediNavInfo).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("CleanYourTV · Jedi Edition")
                .setMessage("Edición de prueba para Android TV. El launcher clásico sigue disponible en Apps. No elimina aplicaciones del sistema.")
                .setPositiveButton("Cerrar", (d,w) -> d.dismiss()).show());
        findViewById(R.id.jediMeditation).setOnClickListener(v -> openMeditation());
        findViewById(R.id.jediAmbiences).setOnClickListener(v -> openSystem(Settings.ACTION_DISPLAY_SETTINGS));
        findViewById(R.id.jediConfig).setOnClickListener(v -> openLauncherSettings());
        findViewById(R.id.jediMaintenance).setOnClickListener(v -> openSystem(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
        findViewById(R.id.jediYouTube).requestFocus();
    }

    @Override protected void onResume() {
        super.onResume();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Buenos días" : hour < 18 ? "Buenas tardes" : "Buenas noches";
        ((TextView)findViewById(R.id.jediGreeting)).setText(greeting + ", Carlos");
        try {
            StatFs s = new StatFs(Environment.getDataDirectory().getAbsolutePath());
            double freeGB = s.getAvailableBytes() / (1024.0 * 1024.0 * 1024.0);
            ((TextView)findViewById(R.id.jediStorage)).setText(String.format(new Locale("es", "CO"), "%.1f GB libres", freeGB));
        } catch (RuntimeException ignored) { ((TextView)findViewById(R.id.jediStorage)).setText("Almacenamiento"); }
        try {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm == null ? null : cm.getActiveNetworkInfo();
            ((TextView)findViewById(R.id.jediConnectivity)).setText(info != null && info.isConnected() ? "● En línea" : "○ Sin conexión");
        } catch (RuntimeException ignored) { ((TextView)findViewById(R.id.jediConnectivity)).setText("Red"); }
    }

    private void linkApp(int tileId, int iconId, String pkg) {
        ImageView icon = findViewById(iconId);
        try { Drawable d = getPackageManager().getApplicationIcon(pkg); icon.setImageDrawable(d); }
        catch (PackageManager.NameNotFoundException ignored) { icon.setVisibility(View.GONE); }
        findViewById(tileId).setOnClickListener(v -> openPackage(pkg));
    }
    private void openPackage(String pkg) {
        PackageManager pm = getPackageManager();
        Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
        if (i == null) i = pm.getLaunchIntentForPackage(pkg);
        if (i == null) {
            Toast.makeText(this, "No se encontró " + pkg + " en el televisor", Toast.LENGTH_LONG).show();
            return;
        }
        try { startActivity(i); }
        catch (Exception e) { Toast.makeText(this, "No se pudo abrir la aplicación", Toast.LENGTH_SHORT).show(); }
    }
    private void openLauncherSettings() {
        try { startActivity(new Intent(this, LauncherSettingsActivity.class)); }
        catch (Exception e) { openSystem(Settings.ACTION_SETTINGS); }
    }
    private void openSystem(String action) {
        try { startActivity(new Intent(action)); }
        catch (ActivityNotFoundException e) {
            try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
            catch (Exception ignored) { Toast.makeText(this, "Ajuste no disponible", Toast.LENGTH_SHORT).show(); }
        }
    }
    private void openMeditation() {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=meditacion+guiada"));
            i.setPackage(YOUTUBE);
            startActivity(i);
        } catch (Exception e) { openPackage(YOUTUBE); }
    }
}
