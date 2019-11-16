package com.rejectliu.offscreendemo.filter;

import android.content.Context;
import android.opengl.GLES20;


/**
 * 某个通道的高斯模糊
 */
class GLImageStackPassFilter extends GLImageFilter {

    protected float mBlurSize = 1f;

    private int mRadiusHandle;
    private int mTexelWidthOffsetHandle;
    private int mTexelHeightOffsetHandle;

    private float mTexelWidth;
    private float mTexelHeight;

    private static String fragmentShader =
                            "precision mediump float;   \n" +
                            "varying vec2 vTexCoord;   \n" +
                            "uniform sampler2D inputTexture;   \n" +
                            "uniform int uRadius;   \n" +
                            "uniform float texelWidthOffset;  \n" +
                            "uniform float texelHeightOffset;  \n" +
                            "void main() {   \n" +
                            "int diameter = 2 * uRadius + 1;  \n" +
                            "   vec4 sampleTex = vec4(0, 0, 0, 0);\n" +
                            "   vec3 col = vec3(0, 0, 0);  \n" +
                            "   float weightSum = 0.0; \n" +
                            "   for(int i = 0; i < diameter; i++) {\n" +
                            "       vec2 offset = vec2(float(i - uRadius) * texelWidthOffset, float(i - uRadius) * texelHeightOffset);  \n" +
                            "       sampleTex = vec4(texture2D(inputTexture, vTexCoord.st+offset));\n" +
                            "       float index = float(i); \n" +
                            "       float boxWeight = float(uRadius) + 1.0 - abs(index - float(uRadius)); \n" +
                            "       col += sampleTex.rgb * boxWeight; \n" +
                            "       weightSum += boxWeight;\n" +
                            "   }   \n" +
                            "   gl_FragColor = vec4(col / weightSum, sampleTex.a);   \n" +
                            "}   \n";


    private static String vertexShader =
            "attribute vec4 aTextureCoord;   \n" +
                    "attribute vec4 aPosition;  \n" +
                    "varying vec2 vTexCoord;  \n" +
                    "void main() {              \n" +
                    "  gl_Position = aPosition; \n" +
                    "  vTexCoord = aTextureCoord.xy; \n" +
                    "}  \n";

    public GLImageStackPassFilter(Context context) {
        this(context, vertexShader,
                fragmentShader);

    }

    public GLImageStackPassFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mTexelWidthOffsetHandle = GLES20.glGetUniformLocation(mProgramHandle, "texelWidthOffset");
        mTexelHeightOffsetHandle = GLES20.glGetUniformLocation(mProgramHandle, "texelHeightOffset");
        mRadiusHandle = GLES20.glGetUniformLocation(mProgramHandle, "uRadius");
    }

    /**
     * 设置模糊半径大小，默认为1.0f
     * @param blurSize
     */
    public void setBlurSize(float blurSize) {
        mBlurSize = blurSize;
    }

    public void setRadius(int radius) {
        setInteger(mRadiusHandle, radius);
    }

    /**
     * 设置高斯模糊的宽高
     * @param width
     * @param height
     */
    public void setTexelOffsetSize(float width, float height) {
        mTexelWidth = width;
        mTexelHeight = height;
        if (mTexelWidth != 0) {
            setFloat(mTexelWidthOffsetHandle, mBlurSize / mTexelWidth);
        } else {
            setFloat(mTexelWidthOffsetHandle, 0.0f);
        }
        if (mTexelHeight != 0) {
            setFloat(mTexelHeightOffsetHandle, mBlurSize / mTexelHeight);
        } else {
            setFloat(mTexelHeightOffsetHandle, 0.0f);
        }
    }
}
