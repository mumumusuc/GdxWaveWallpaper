package com.mumumusuc;

import android.graphics.PixelFormat;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;
import com.badlogic.gdx.backends.android.AndroidWallpaperListener;

public class GLWallpaperService extends AndroidLiveWallpaperService {
    @Override
    public void onCreateApplication() {
        super.onCreateApplication();
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        getSurfaceHolder().setFormat(PixelFormat.TRANSLUCENT);
        initialize(new AndroidWave(), cfg);
        this.app.getApplicationListener();
    }

    class AndroidWave extends Wave implements AndroidWallpaperListener {
        @Override
        public void offsetChange(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            Gdx.app.log("wallpaper", "offsetX = " + xPixelOffset + ", xOffset = " + xOffset + " , xOffsetStep = " + xOffsetStep);
                    screenOffsetX = xPixelOffset;
        }

        @Override
        public void previewStateChange(boolean isPreview) {

        }

        @Override
        public void iconDropped(int x, int y) {

        }
    }
}