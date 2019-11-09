package com.rejectliu.offscreendemo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener, TextureView.SurfaceTextureListener, VirtualViewRenderer.RenderNotifier, SeekBar.OnSeekBarChangeListener {

    private GLSurfaceView surfaceView;
    private TextureView textView;
    private SurfaceTexture mSurfaceTexture;
    private SeekBar seekBar;
    private TextView mTextView;
    public static int Width = 0;
    public static int Height = 0;
    private VirtualViewRenderer viewRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        mTextView = findViewById(R.id.time_indicator);




        mSurfaceTexture = new SurfaceTexture(0);
        mSurfaceTexture.detachFromGLContext();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        DisplayManager displayManager = (DisplayManager)getSystemService(DISPLAY_SERVICE);
        Width = dm.widthPixels;
        Height = dm.heightPixels;

        mSurfaceTexture.setDefaultBufferSize(Width, Height);
        VirtualDisplay offscreenDisplay = displayManager.createVirtualDisplay("offscreenDisplay", Width, Height, dm.densityDpi, new Surface(mSurfaceTexture), 0);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        SimplePresentation simplePresentation = new SimplePresentation(this, offscreenDisplay.getDisplay());
        simplePresentation.show();

        surfaceView = findViewById(R.id.surface_view);
        surfaceView.setEGLContextClientVersion(2);
        viewRenderer = new VirtualViewRenderer(mSurfaceTexture);
        surfaceView.setRenderer(viewRenderer);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        viewRenderer.setNotifier(this);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (surfaceView != null) {
            surfaceView.requestRender();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onRenderTime(final long cost) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.setText("cost : " + cost);
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (viewRenderer != null && fromUser) {
            viewRenderer.setBlurSize(progress / 10);
            surfaceView.requestRender();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
