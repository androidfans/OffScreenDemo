package com.rejectliu.offscreendemo;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class VirtualViewRender implements GLSurfaceView.Renderer {

    private final SurfaceTexture mSurfaceTexture;
    private MainFrameRect mMainFrameRect;
    private int mTextureObject;
    private float[] mTransform;

    public VirtualViewRender(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mMainFrameRect = new MainFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
        mTextureObject = mMainFrameRect.createTextureObject();
        mTransform = new float[16];
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mSurfaceTexture.attachToGLContext(mTextureObject);
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mTransform);
        mMainFrameRect.drawFrame(mTextureObject, mTransform);
        mSurfaceTexture.detachFromGLContext();
    }
}
