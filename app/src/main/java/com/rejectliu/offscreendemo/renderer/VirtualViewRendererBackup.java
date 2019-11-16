package com.rejectliu.offscreendemo.renderer;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;

import com.rejectliu.offscreendemo.MainActivity;
import com.rejectliu.offscreendemo.filter.GLImageFilter;
import com.rejectliu.offscreendemo.filter.GLImageGaussianBlurOldFilter;
import com.rejectliu.offscreendemo.filter.GLImageOESInputFilter;
import com.rejectliu.offscreendemo.util.Drawable2d;
import com.rejectliu.offscreendemo.util.OpenGLUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class VirtualViewRendererBackup implements GLSurfaceView.Renderer {

    private static final String TAG = "VirtualViewRenderer";
    private static final boolean VERBOSE = false;
    private final Drawable2d mRectDrawable = new Drawable2d(Drawable2d.Prefab.FULL_RECTANGLE);


    private final float[] mSTMatrix = new float[16];
    private int mTextureId;

    private SurfaceTexture mSurfaceTexture;
    private int mFrameCount;

    // width/height of the incoming camera preview frames
    RenderNotifier notifier;
    private GLImageGaussianBlurOldFilter mGlImageGaussianBlurFilter;
    private GLImageOESInputFilter mGlImageOESInputFilter;
    private GLImageFilter mGlImageFilter;

    public void setNotifier(RenderNotifier notifier) {
        this.notifier = notifier;
    }

    public VirtualViewRendererBackup(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
        mTextureId = -1;
        mFrameCount = -1;
    }


    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");
        mTextureId = OpenGLUtils.createOESTexture();

        mGlImageGaussianBlurFilter = new GLImageGaussianBlurOldFilter(null);
        mGlImageGaussianBlurFilter.setBlurSize(1f);
        mGlImageOESInputFilter = new GLImageOESInputFilter(null);
        mGlImageFilter = new GLImageFilter(null);
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

        mGlImageFilter.onInputSizeChanged(MainActivity.Width, MainActivity.Height);
        mGlImageFilter.initFrameBuffer(MainActivity.Width, MainActivity.Height);
        mGlImageFilter.onDisplaySizeChanged(MainActivity.Width, MainActivity.Height);
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

        mGlImageOESInputFilter.setTextureTransformMatrix(mSTMatrix);
        int textureId = mTextureId;

        textureId = mGlImageOESInputFilter.drawFrameBuffer(textureId, mRectDrawable.getVertexArray(), mRectDrawable.getTexCoordArray());
        GLES20.glFinish();
        long l = SystemClock.elapsedRealtime();
        textureId = mGlImageGaussianBlurFilter.drawFrameBuffer(textureId, mRectDrawable.getVertexArray(), mRectDrawable.getTexCoordArray());
        GLES20.glFinish();
        long cost = SystemClock.elapsedRealtime() - l;

        mGlImageFilter.drawFrame(textureId, mRectDrawable.getVertexArray(), mRectDrawable.getTexCoordArray());

//        mGlImageOESInputFilter.drawFrame(textureId, mRectDrawable.getVertexArray(), mRectDrawable.getTexCoordArray());
        mSurfaceTexture.detachFromGLContext();

        Log.d("rejectliu", cost + "");
        if (notifier != null) {
            notifier.onRenderTime(cost);
        }
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
//            mGlImageGaussianBlurFilter.setBlurSize(size);
//            mGlImageGaussianBlurFilter.onInputSizeChanged(MainActivity.Width, MainActivity.Height);
//            mGlImageGaussianBlurFilter.setRadius((int) size);
        }
    }
}
