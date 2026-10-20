package com.ludomaster.reference;

import androidx.appcompat.app.AppCompatActivity;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import java.util.Objects;

public class SplashActivity extends AppCompatActivity {
    private final java.util.ArrayList<ObjectAnimator> loaderAnimations = new java.util.ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_splash);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        View emblem = findViewById(R.id.brand_logo);
        View title = findViewById(R.id.brand_title);
        View tagline = findViewById(R.id.brand_tagline);
        emblem.setScaleX(0.84f);
        emblem.setScaleY(0.84f);
        emblem.animate().scaleX(1.06f).scaleY(1.06f).setDuration(440)
                .withEndAction(() -> emblem.animate().scaleX(1f).scaleY(1f).setDuration(260).start()).start();
        title.setAlpha(0f);
        tagline.setAlpha(0f);
        title.animate().alpha(1f).setStartDelay(280).setDuration(530).start();
        tagline.animate().alpha(1f).setStartDelay(580).setDuration(500).start();
        int[] loaderIds = {R.id.loader_red, R.id.loader_yellow, R.id.loader_blue, R.id.loader_green};
        for (int i=0; i<loaderIds.length; i++) {
            View dot = findViewById(loaderIds[i]);
            ObjectAnimator pulse = ObjectAnimator.ofFloat(dot, View.TRANSLATION_Y, 0f, -10f, 0f);
            pulse.setDuration(620);
            pulse.setStartDelay(i * 145L);
            pulse.setRepeatCount(ValueAnimator.INFINITE);
            pulse.setRepeatMode(ValueAnimator.RESTART);
            pulse.start();
            loaderAnimations.add(pulse);
        }
        new Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            finish();
        }, 1800);
    }
    @Override protected void onDestroy() {
        for (ObjectAnimator animation : loaderAnimations) animation.cancel();
        loaderAnimations.clear();
        super.onDestroy();
    }
    void setFullScreen() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            View dview = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            dview.setSystemUiVisibility(uiOptions);
        } else {
            View dview = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            dview.setSystemUiVisibility(uiOptions);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController controller = getWindow().getInsetsController();
                if (controller != null) {
                    controller.hide(WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            }
        }
    }
}