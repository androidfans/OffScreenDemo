package com.rejectliu.offscreendemo.renderer;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;


import com.rejectliu.offscreendemo.filter.GLImageFilter;
import com.rejectliu.offscreendemo.filter.GLImageGaussianBlurGPUImageFilter;
import com.rejectliu.offscreendemo.filter.GLImageGaussianBlurOldFilter;
import com.rejectliu.offscreendemo.filter.GLImageOESInputFilter;
import com.rejectliu.offscreendemo.filter.GLImageStackBlurFilter;
import com.rejectliu.offscreendemo.util.Drawable2d;
import com.rejectliu.offscreendemo.util.FILTER_TYPE;
import com.rejectliu.offscreendemo.util.OpenGLUtils;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class VirtualViewRenderer implements GLSurfaceView.Renderer {


    private static final String TAG = "VirtualViewRenderer";
    private final Drawable2d mRectDrawable = new Drawable2d(Drawable2d.Prefab.FULL_RECTANGLE);
    private GLImageFilter currentFilter = null;

    private final float[] mSTMatrix = new float[16];
    private int mTextureId;
    private List<GLImageFilter> filters = new ArrayList<>();

    private SurfaceTexture mSurfaceTexture;

    // width/height of the incoming camera preview frames
    RenderNotifier notifier;
    private GLImageGaussianBlurOldFilter mGaussianBlurFilter;
    private GLImageGaussianBlurGPUImageFilter mGaussianBlurGPUImageFilter;
    private GLImageStackBlurFilter mStackBlurFilter;
    private GLImageOESInputFilter mGlImageOESInputFilter;
    private GLImageFilter mGlImageFilter;

    public void setNotifier(RenderNotifier notifier) {
        this.notifier = notifier;
    }

    public VirtualViewRenderer(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
        mTextureId = -1;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");
        mTextureId = OpenGLUtils.createOESTexture();

        mGaussianBlurFilter = new GLImageGaussianBlurOldFilter(null);
        mGaussianBlurGPUImageFilter = new GLImageGaussianBlurGPUImageFilter(null);
        mStackBlurFilter = new GLImageStackBlurFilter(null);
        mGlImageOESInputFilter = new GLImageOESInputFilter(null);
        mGlImageFilter = new GLImageFilter(null);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        mGaussianBlurFilter.onInputSizeChanged(width, height);
        mGaussianBlurFilter.initFrameBuffer(width,height);
        mGaussianBlurFilter.onDisplaySizeChanged(width, height);

        mGlImageOESInputFilter.onInputSizeChanged(width, height);
        mGlImageOESInputFilter.initFrameBuffer(width, height);
        mGlImageOESInputFilter.onDisplaySizeChanged(width, height);

        mGaussianBlurGPUImageFilter.onInputSizeChanged(width, height);
        mGaussianBlurGPUImageFilter.initFrameBuffer(width, height);
        mGaussianBlurGPUImageFilter.onDisplaySizeChanged(width, height);

        mGlImageFilter.onInputSizeChanged(width, height);
        mGlImageFilter.initFrameBuffer(width, height);
        mGlImageFilter.onDisplaySizeChanged(width, height);

        mStackBlurFilter.onInputSizeChanged(width, height);
        mStackBlurFilter.initFrameBuffer(width, height);
        mStackBlurFilter.onDisplaySizeChanged(width, height);
    }


    @Override
    public void onDrawFrame(GL10 unused) {
        long l = SystemClock.elapsedRealtime();
        mSurfaceTexture.attachToGLContext(mTextureId);
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mGlImageOESInputFilter.setTextureTransformMatrix(mSTMatrix);
        int textureId = mTextureId;
        textureId = mGlImageOESInputFilter.drawFrameBuffer(textureId, mRectDrawable.getVertexArray(), mRectDrawable.getTexCoordArray());
        if (currentFilter != null) {
            textureId = currentFilter.drawFrameBuffer(textureId, mRectDrawable.getVertexArray(), mRectDrawable.getTexCoordArray());
        }
        mGlImageFilter.drawFrame(textureId, mRectDrawable.getVertexArray(), mRectDrawable.getTexCoordArray());
        mSurfaceTexture.detachFromGLContext();
        long cost = SystemClock.elapsedRealtime() - l;
        Log.d(TAG, cost + "");
        if (notifier != null) {
            notifier.onRenderTime(cost);
        }
    }


    public interface RenderNotifier {
        void onRenderTime(long cost);
    }

    public void setBlurSize(float size) {
        mGaussianBlurFilter.setBlurSize((int) size);
        mGaussianBlurGPUImageFilter.setBlurSize(size);
        mStackBlurFilter.setBlurSize(size);
    }

    public void changeAlgorithm(@FILTER_TYPE int filterType) {
        switch (filterType) {
            case FILTER_TYPE.GAUSSIAN_BLUR_OLD:
                currentFilter = mGaussianBlurFilter;
                break;
            case FILTER_TYPE.GAUSSIAN_BLUR_GPU_IMAGE:
                currentFilter = mGaussianBlurGPUImageFilter;
                break;
            case FILTER_TYPE.STACK_BLUR:
                currentFilter = mStackBlurFilter;
                break;
            default:
                currentFilter = null;
        }
    }
}
