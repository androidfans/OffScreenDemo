package com.rejectliu.offscreendemo.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;

/**
 * 外部纹理(OES纹理)输入
 * Created by cain on 2017/7/9.
 */

public class GLImageOESInputFilter extends GLImageFilter {

    private static String fragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 textureCoordinate;\n" +
                    "uniform samplerExternalOES inputTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(inputTexture, textureCoordinate);\n" +
                    "}";

    private static String vertexShader =
            "// GL_OES_EGL_image_external 格式纹理输入滤镜，其中transformMatrix是SurfaceTexture的transformMatrix\n" +
                    "uniform mat4 transformMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "\n" +
                    "varying vec2 textureCoordinate;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_Position = aPosition;\n" +
                    "    textureCoordinate = (transformMatrix * aTextureCoord).xy;\n" +
                    "}\n";


    private int mTransformMatrixHandle;
    private float[] mTransformMatrix;

    public GLImageOESInputFilter(Context context) {
        this(context, vertexShader, fragmentShader);
    }

    public GLImageOESInputFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mTransformMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "transformMatrix");
    }

    @Override
    public int getTextureType() {
        return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glUniformMatrix4fv(mTransformMatrixHandle, 1, false, mTransformMatrix, 0);
    }

    /**
     * 设置SurfaceTexture的变换矩阵
     * @param transformMatrix
     */
    public void setTextureTransformMatrix(float[] transformMatrix) {
        mTransformMatrix = transformMatrix;
    }

}
