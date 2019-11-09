package com.rejectliu.offscreendemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.content.Context.DISPLAY_SERVICE;


public class MyRender implements GLSurfaceView.Renderer {

    private final Activity mContext;
    public SurfaceTexture mSurfaceTexture;

    private final String vertexShaderCode =
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "\n" +
            "varying vec2 vTextureCoord;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_Position = aPosition;\n" +
            "    vTextureCoord = aTextureCoord.xy;\n" +
            "}";

    private final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float; //指定默认精度\n" +
                    "\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES uTexture;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(uTexture, vTextureCoord);\n" +
                    "}";

    private FloatBuffer mPosBuffer;
    private FloatBuffer mTexBuffer;
    private float[] mPosCoordinate = {-1, -1, -1, 1, 1, -1, 1, 1};
    private float[] mTexCoordinateBackRight = {1, 1, 0, 1, 1, 0, 0, 0};//顺时针转90并沿Y轴翻转  后摄像头正确，前摄像头上下颠倒
    private float[] mTexCoordinateForntRight = {0, 1, 1, 1, 0, 0, 1, 0};//顺时针旋转90  后摄像头上下颠倒了，前摄像头正确

    public int mProgramHandle;
    private int mTextureLoc;
    private int maPositionLoc;
    private int maTextureCoordLoc;
    private int mTextureId = -1;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        mTextureId = GlUtil.createTextureObject(getTextureTarget());
        creatProgram();
        activeProgram();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.scaleM(mMVPMatrix,0,1,-1,1);
        float ratio = (float) width / height;
        Matrix.orthoM(mProjectMatrix, 0, -1, 1, -ratio, ratio, 1, 7);// 3和7代表远近视点与眼睛的距离，非坐标点
        Matrix.setLookAtM(mCameraMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);// 3代表眼睛的坐标点
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mCameraMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mSurfaceTexture != null) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            mSurfaceTexture.attachToGLContext(mTextureId);
            mSurfaceTexture.updateTexImage();
            float[] floats = new float[16];
            mSurfaceTexture.getTransformMatrix(floats);
            bindTexture(mTextureId);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mPosCoordinate.length / 2);
            mSurfaceTexture.detachFromGLContext();
            unbindTexture();
        }
        Log.d("rejectliu", GLES20.glGetProgramInfoLog(mProgramHandle));
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        // 添加上面编写的着色器代码并编译它
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    private void creatProgram() {
        //通常做法
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        // 创建空的OpenGL ES程序
        mProgramHandle = GLES20.glCreateProgram();

        // 添加顶点着色器到程序中
        GLES20.glAttachShader(mProgramHandle, vertexShader);

        // 添加片段着色器到程序中
        GLES20.glAttachShader(mProgramHandle, fragmentShader);

        // 创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(mProgramHandle);

        // 释放shader资源
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
    }

    private FloatBuffer convertToFloatBuffer(float[] buffer) {
        FloatBuffer fb = ByteBuffer.allocateDirect(buffer.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        fb.put(buffer);
        fb.position(0);
        return fb;
    }

    private float[] mProjectMatrix = new float[16];
    private float[] mCameraMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mTempMatrix = new float[16];

    //添加程序到ES环境中
    private void activeProgram() {
        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(mProgramHandle);


        mTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexture");
        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");

        mPosBuffer = convertToFloatBuffer(mPosCoordinate);
        int camera_status = 0;
        if(camera_status == 0){
            mTexBuffer = convertToFloatBuffer(mTexCoordinateBackRight);
        }else{
            mTexBuffer = convertToFloatBuffer(mTexCoordinateForntRight);
        }

        GLES20.glVertexAttribPointer(maPositionLoc, 2, GLES20.GL_FLOAT, false, 0, mPosBuffer);
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer);


        // 启用顶点位置的句柄
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
    }

    private int getTextureTarget() {
        return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
    }

    protected void bindTexture(int textureId) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(getTextureTarget(), textureId);
        GLES20.glUniform1i(mTextureLoc, 0);
    }

    protected void unbindTexture() {
        GLES20.glBindTexture(getTextureTarget(), 0);
    }

    public MyRender(Context context,SurfaceTexture surfaceTexture) {
        mContext = (Activity) context;
        mSurfaceTexture = surfaceTexture;
        Matrix.setIdentityM(mProjectMatrix, 0);
        Matrix.setIdentityM(mCameraMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.setIdentityM(mTempMatrix, 0);
    }



}
