package com.rejectliu.offscreendemo;

import androidx.annotation.ArrayRes;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.rejectliu.offscreendemo.renderer.VirtualViewRenderer;
import com.rejectliu.offscreendemo.util.FILTER_TYPE;
import com.rejectliu.offscreendemo.view.SimplePresentation;

public class MainActivity extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener, TextureView.SurfaceTextureListener, VirtualViewRenderer.RenderNotifier, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener {

    private GLSurfaceView surfaceView;
    private TextureView textureView;
    private SurfaceTexture mSurfaceTexture;
    private SeekBar seekBar;
    private TextView mTimeIndicator;
    public static int Width = 0;
    public static int Height = 0;
    private VirtualViewRenderer viewRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        mTimeIndicator = findViewById(R.id.time_indicator);


        mSurfaceTexture = new SurfaceTexture(0);
        mSurfaceTexture.detachFromGLContext();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        DisplayManager displayManager = (DisplayManager)getSystemService(DISPLAY_SERVICE);
        Width = dm.widthPixels;
        Height = dm.heightPixels / 3;

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
        surfaceView.setLayoutParams(new RelativeLayout.LayoutParams(Width,Height));
        viewRenderer.setNotifier(this);



        Spinner schemeSpinner = findViewById(R.id.scheme_spinner);
        schemeSpinner.setAdapter(makeSpinnerAdapter(R.array.blur_modes));
        schemeSpinner.setOnItemSelectedListener(this);
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
                mTimeIndicator.setText("cost : " + cost);
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (viewRenderer != null && fromUser) {
            viewRenderer.setBlurSize(progress / 3);
            surfaceView.requestRender();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    private SpinnerAdapter makeSpinnerAdapter(@ArrayRes int arrayRes) {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                arrayRes, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return spinnerAdapter;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final int spinnerId = parent.getId();
        if (spinnerId == R.id.scheme_spinner) {
            viewRenderer.changeAlgorithm(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
