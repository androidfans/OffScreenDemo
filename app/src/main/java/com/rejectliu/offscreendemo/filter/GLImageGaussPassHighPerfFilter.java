package com.rejectliu.offscreendemo.filter;

import android.content.Context;
import android.opengl.GLES20;


/**
 * 某个通道的高斯模糊
 */
class GLImageGaussPassHighPerfFilter extends GLImageFilter {

    protected float mBlurSize = 1f;

    private int mTexelWidthOffsetHandle;
    private int mTexelHeightOffsetHandle;

    private float mTexelWidth;
    private float mTexelHeight;

    private static String fragmentShader =
                    "// 优化后的高斯模糊\n" +
                            "precision mediump float;\n" +
                            "varying vec2 textureCoordinate;\n" +
                            "uniform sampler2D inputTexture;\n" +
                            "// 高斯算子左右偏移值，当偏移值为2时，高斯算子为5 x 5\n" +
                            "const int SHIFT_SIZE = 2;\n" +
                            "varying vec4 blurShiftCoordinates[SHIFT_SIZE];\n" +
                            "void main() {\n" +
                            "    // 计算当前坐标的颜色值\n" +
                            "    vec4 currentColor = texture2D(inputTexture, textureCoordinate);\n" +
                            "    mediump vec3 sum = currentColor.rgb;\n" +
                            "    // 计算偏移坐标的颜色值总和\n" +
                            "    for (int i = 0; i < SHIFT_SIZE; i++) {\n" +
                            "        sum += texture2D(inputTexture, blurShiftCoordinates[i].xy).rgb;\n" +
                            "        sum += texture2D(inputTexture, blurShiftCoordinates[i].zw).rgb;\n" +
                            "    }\n" +
                            "    // 求出平均值\n" +
                            "    gl_FragColor = vec4(sum * 1.0 / float(2 * SHIFT_SIZE + 1), currentColor.a);\n" +
                            "}";

    private static String vertexShader =
            "\n" +
                    "// 优化后的高斯模糊\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "\n" +
                    "// 高斯算子左右偏移值，当偏移值为2时，高斯算子为5 x 5\n" +
                    "const int SHIFT_SIZE = 2;\n" +
                    "\n" +
                    "uniform highp float texelWidthOffset;\n" +
                    "uniform highp float texelHeightOffset;\n" +
                    "\n" +
                    "varying vec2 textureCoordinate;\n" +
                    "varying vec4 blurShiftCoordinates[SHIFT_SIZE];\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_Position = aPosition;\n" +
                    "    textureCoordinate = aTextureCoord.xy;\n" +
                    "    // 偏移步距\n" +
                    "    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n" +
                    "    // 记录偏移坐标\n" +
                    "    for (int i = 0; i < SHIFT_SIZE; i++) {\n" +
                    "        blurShiftCoordinates[i] = vec4(textureCoordinate.xy - float(i + 1) * singleStepOffset,\n" +
                    "                                       textureCoordinate.xy + float(i + 1) * singleStepOffset);\n" +
                    "    }\n" +
                    "}";

    public GLImageGaussPassHighPerfFilter(Context context) {
        this(context, vertexShader,
                fragmentShader);

    }

    public GLImageGaussPassHighPerfFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mTexelWidthOffsetHandle = GLES20.glGetUniformLocation(mProgramHandle, "texelWidthOffset");
        mTexelHeightOffsetHandle = GLES20.glGetUniformLocation(mProgramHandle, "texelHeightOffset");
    }

    /**
     * 设置模糊半径大小，默认为1.0f
     * @param blurSize
     */
    public void setBlurSize(float blurSize) {
        mBlurSize = blurSize;
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
