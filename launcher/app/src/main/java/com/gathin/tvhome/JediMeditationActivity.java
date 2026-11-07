package com.gathin.tvhome;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Locale;

/** Offline meditation timer; no external permissions or background service. */
public class JediMeditationActivity extends Activity {
    private long remainingMs;
    private CountDownTimer timer;
    private boolean running;
    private TextView timeView;
    private Button toggleButton;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_jedi_meditation);
        int minutes = Math.max(1, Math.min(120, getIntent().getIntExtra("minutes", 10)));
        remainingMs = state != null ? state.getLong("remaining", minutes*60000L) : minutes*60000L;
        timeView = findViewById(R.id.jediMeditationTime);
        toggleButton = findViewById(R.id.jediMeditationToggle);
        toggleButton.setOnClickListener(v -> { if (running) pauseTimer(); else startTimer(); });
        findViewById(R.id.jediMeditationFinish).setOnClickListener(v -> finish());
        renderTimer();
        startTimer();
        toggleButton.requestFocus();
    }

    private void startTimer() {
        if (remainingMs <= 0) return;
        running = true;
        toggleButton.setText("Pausar");
        timer = new CountDownTimer(remainingMs, 1000) {
            @Override public void onTick(long ms) { remainingMs = ms; renderTimer(); }
            @Override public void onFinish() {
                remainingMs = 0; running = false; renderTimer();
                toggleButton.setText("Terminado"); toggleButton.setEnabled(false);
            }
        }.start();
    }

    private void pauseTimer() {
        if (timer != null) timer.cancel();
        running = false;
        toggleButton.setText("Continuar");
    }

    private void renderTimer() {
        long seconds = (remainingMs+999)/1000;
        timeView.setText(String.format(Locale.US,"%02d:%02d",seconds/60,seconds%60));
    }

    @Override protected void onPause() { super.onPause(); pauseTimer(); }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state); state.putLong("remaining",remainingMs);
    }
    @Override protected void onDestroy() { if (timer!=null) timer.cancel(); super.onDestroy(); }
}
