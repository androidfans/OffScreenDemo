package com.rejectliu.offscreendemo;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class VirtualViewRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "VirtualViewRenderer";
    private static final boolean VERBOSE = false;
    private final Drawable2d mRectDrawable = new Drawable2d(Drawable2d.Prefab.FULL_RECTANGLE);


    private final float[] mSTMatrix = new float[16];
    private int mTextureId;

    private SurfaceTexture mSurfaceTexture;
    private int mFrameCount;

    // width/height of the incoming camera preview frames
    RenderNotifier notifier;
    private GLImageGaussianBlurFilter mGlImageGaussianBlurFilter;
    private GLImageOESInputFilter mGlImageOESInputFilter;

    public void setNotifier(RenderNotifier notifier) {
        this.notifier = notifier;
    }

    public VirtualViewRenderer(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
        mTextureId = -1;
        mFrameCount = -1;


    }


    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");
        mTextureId = OpenGLUtils.createOESTexture();

        mGlImageGaussianBlurFilter = new GLImageGaussianBlurFilter(null);
        mGlImageGaussianBlurFilter.setBlurSize(0f);
        mGlImageOESInputFilter = new GLImageOESInputFilter(null);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        Log.d(TAG, "onSurfaceChanged " + width + "x" + height);
        mGlImageGaussianBlurFilter.onInputSizeChanged(MainActivity.Width, MainActivity.Height);
        mGlImageGaussianBlurFilter.initFrameBuffer(MainActivity.Width, MainActivity.Height);
        mGlImageGaussianBlurFilter.onDisplaySizeChanged(MainActivity.Width, MainActivity.Height);

        mGlImageOESInputFilter.onInputSizeChanged(MainActivity.Width, MainActivity.Height);
        mGlImageOESInputFilter.initFrameBuffer(MainActivity.Width, MainActivity.Height);
        mGlImageOESInputFilter.onDisplaySizeChanged(MainActivity.Width, MainActivity.Height);
    }


    @Override
    public void onDrawFrame(GL10 unused) {
        if (VERBOSE) Log.d(TAG, "onDrawFrame tex=" + mTextureId);
        boolean showBox = false;

        // Latch the latest frame.  If there isn't anything new, we'll just re-use whatever
        // was there before.
        mSurfaceTexture.attachToGLContext(mTextureId);
        mSurfaceTexture.updateTexImage();

        // Draw the video frame.
        mSurfaceTexture.getTransformMatrix(mSTMatrix);

        long l = SystemClock.elapsedRealtime();
        mGlImageOESInputFilter.setTextureTransformMatrix(mSTMatrix);
        int textureId = mGlImageOESInputFilter.drawFrameBuffer(mTextureId, mRectDrawable.getVertexArray(), mRectDrawable.getTexCoordArray());
        mGlImageGaussianBlurFilter.drawFrame(textureId, mRectDrawable.getVertexArray(), mRectDrawable.getTexCoordArray());
        long cost = SystemClock.elapsedRealtime() - l;
        if (notifier != null) {
            notifier.onRenderTime(cost);
        }

//        mFullScreen.drawFrame(mTextureId, mSTMatrix);

        // Draw a flashing box if we're recording.  This only appears on screen.
        showBox = true;
        if (showBox && (++mFrameCount & 0x04) == 0) {
            drawBox();
        }
        mSurfaceTexture.detachFromGLContext();
    }

    /**
     * Draws a red box in the corner.
     */
    private void drawBox() {
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(0, 0, 100, 100);
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }

    public interface RenderNotifier {
        void onRenderTime(long cost);
    }

    public void setBlurSize(float size) {
        if (mGlImageGaussianBlurFilter != null) {
            mGlImageGaussianBlurFilter.setBlurSize(size);
            mGlImageGaussianBlurFilter.onInputSizeChanged(MainActivity.Width, MainActivity.Height);
        }
    }
}
