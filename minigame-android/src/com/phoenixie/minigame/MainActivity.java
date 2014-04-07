package com.phoenixie.minigame;

import android.os.Bundle;
import android.view.Window;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainActivity extends AndroidApplication {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = false;
        cfg.useAccelerometer = false;
        cfg.useCompass = false;
        cfg.useWakelock = false;
        cfg.hideStatusBar = false;
        
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        RelativeLayout layout = new RelativeLayout(this);
        layout.addView(initializeForView(new MiniGame(), cfg));
        setContentView(layout);
    }
}