package com.rejectliu.offscreendemo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.EGLConfig;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener, TextureView.SurfaceTextureListener {

    private GLSurfaceView surfaceView;
    private TextureView textView;
    private SurfaceTexture mSurfaceTexture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mSurfaceTexture = new SurfaceTexture(0);
        mSurfaceTexture.detachFromGLContext();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        DisplayManager displayManager = (DisplayManager)getSystemService(DISPLAY_SERVICE);
        VirtualDisplay offscreenDisplay = displayManager.createVirtualDisplay("offscreenDisplay", dm.widthPixels, dm.heightPixels, dm.densityDpi, new Surface(mSurfaceTexture), 0);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        SimplePresentation simplePresentation = new SimplePresentation(this, offscreenDisplay.getDisplay());
        simplePresentation.show();

        textView = findViewById(R.id.surface_view);
        textView.setSurfaceTexture(mSurfaceTexture);

//        surfaceView = findViewById(R.id.surface_view);
//        surfaceView.setEGLContextClientVersion(2);
//        RectRender render = new RectRender(mSurfaceTexture);
//        surfaceView.setRenderer(render);
//        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
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
}
