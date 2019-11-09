package com.rejectliu.offscreendemo;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.rejectliu.offscreendemo.cache.FrameBufferCache;
import com.rejectliu.offscreendemo.util.IFrameBuffer;
import com.rejectliu.offscreendemo.util.IProgram;
import com.rejectliu.offscreendemo.util.IRenderer;
import com.rejectliu.offscreendemo.util.ITexture;
import com.rejectliu.offscreendemo.util.ProgramFactory;
import com.rejectliu.offscreendemo.util.ShaderUtil;
import com.rejectliu.offscreendemo.util.Texture;
import com.rejectliu.offscreendemo.util.TextureFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by yuxfzju on 16/8/10.
 */

public class OffScreenBlurRenderer implements IRenderer<Texture> {
    private final static String TAG = OffScreenBlurRenderer.class.getSimpleName();

    private static final String vertexShaderCode =
            "attribute vec2 aTexCoord;   \n" +
                    "attribute vec4 aPosition;  \n" +
                    "varying vec2 vTexCoord;  \n" +
                    "void main() {              \n" +
                    "  gl_Position = aPosition; \n" +
                    "  vTexCoord = aTexCoord; \n" +
                    "}  \n";

    private static final int COORDS_PER_VERTEX = 3;
    private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;

    private static final float[] squareCoords = {
            -1f, 1f, 0.0f,   // top left
            -1f, -1f, 0.0f,   // bottom left
            1f, -1f, 0.0f,   // bottom right
            1f, 1f, 0.0f    // top right
    };

    private static final float[] mTexHorizontalCoords = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f
    };

    private static final short[] drawOrder = {0, 1, 2, 0, 2, 3};

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;
    private FloatBuffer mTexCoordBuffer;

    private IProgram mProgram;

    private int mRadius;

    private int mMode;

    private volatile boolean mNeedRelink;

    public OffScreenBlurRenderer() {

        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(squareCoords);
        mVertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer.put(drawOrder);
        mDrawListBuffer.position(0);

        ByteBuffer tcb = ByteBuffer.allocateDirect(mTexHorizontalCoords.length * 4);
        tcb.order(ByteOrder.nativeOrder());
        mTexCoordBuffer = tcb.asFloatBuffer();
        mTexCoordBuffer.put(mTexHorizontalCoords);
        mTexCoordBuffer.position(0);

    }

    @Override
    public void onDrawFrame(Texture texture) {

        BlurContext blurContext = null;
        try {
            blurContext = prepare(texture.width(),texture.height(),texture.mTextureId);
            draw(blurContext);
        } finally {
            onPostBlur(blurContext);
        }
    }


    private BlurContext prepare(int w, int h , int texture) {
        EGLContext context = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
        if (context.equals(EGL10.EGL_NO_CONTEXT)) {
            throw new IllegalStateException("This thread has no EGLContext.");
        }

        if (mNeedRelink || mProgram == null) {
            deletePrograms();
            mProgram = ProgramFactory.create(vertexShaderCode, ShaderUtil.getFragmentShaderCode(mMode));
            mNeedRelink = false;
        }

        if (mProgram.id() == 0) {
            throw new IllegalStateException("Failed to create program.");
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, w, h);

        return new BlurContext(texture, w, h);

    }


    private void draw(BlurContext blurContext) {
        drawOneDimenBlur(blurContext, true);
        drawOneDimenBlur(blurContext, false);

    }

    private void drawOneDimenBlur(BlurContext blurContext, boolean isHorizontal) {
        try {
            GLES20.glUseProgram(mProgram.id());

            int positionId = GLES20.glGetAttribLocation(mProgram.id(), "aPosition");
            GLES20.glEnableVertexAttribArray(positionId);
            GLES20.glVertexAttribPointer(positionId, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

            int texCoordId = GLES20.glGetAttribLocation(mProgram.id(), "aTexCoord");
            GLES20.glEnableVertexAttribArray(texCoordId);
            GLES20.glVertexAttribPointer(texCoordId, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

            if (isHorizontal) {
                blurContext.getBlurFrameBuffer().bindSelf();
            }

            int textureUniformId = GLES20.glGetUniformLocation(mProgram.id(), "uTexture");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, isHorizontal ? blurContext.getInputTexture().id() : blurContext.getHorizontalTexture().id());
            GLES20.glUniform1i(textureUniformId, 0);

            int radiusId = GLES20.glGetUniformLocation(mProgram.id(), "uRadius");
            int widthOffsetId = GLES20.glGetUniformLocation(mProgram.id(), "uWidthOffset");
            int heightOffsetId = GLES20.glGetUniformLocation(mProgram.id(), "uHeightOffset");
            GLES20.glUniform1i(radiusId, mRadius);
            GLES20.glUniform1f(widthOffsetId, isHorizontal ? 0 : 1f / blurContext.getBitmap().getWidth());
            GLES20.glUniform1f(heightOffsetId, isHorizontal ? 1f / blurContext.getBitmap().getHeight() : 0);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

            if (!isHorizontal) {
                GLES20.glDisableVertexAttribArray(positionId);
                GLES20.glDisableVertexAttribArray(texCoordId);
            }
        } finally {
            resetAllBuffer();
        }

    }

    private void resetAllBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
        mVertexBuffer.rewind();
        mTexCoordBuffer.rewind();
        mDrawListBuffer.rewind();
    }

    private void onPostBlur(BlurContext blurContext) {
        if (blurContext != null) {
            blurContext.finish();
        }
    }


    private void deletePrograms() {
        if (mProgram != null) {
            mProgram.delete();
        }
    }

    public void free() {
        mNeedRelink = true;
        deletePrograms();
    }

    void setBlurMode( int mode) {
        mNeedRelink = true;
        mMode = mode;
    }

    void setBlurRadius(int radius) {
        mRadius = radius;
    }

    private static class BlurContext {
        private ITexture inputTexture;
        private ITexture horizontalTexture;
        private IFrameBuffer blurFrameBuffer;
        private Bitmap bitmap;

        private BlurContext(int inputTexture,int width,int height) {
            //todo Textures share problem is not solved. Here create a new texture directly, not get from the texture cache
            //It doesn't affect performance seriously.
            Texture texture = new SimpleTexture(width, height);
            horizontalTexture = TextureFactory.create(bitmap.getWidth(), bitmap.getHeight());
            blurFrameBuffer = FrameBufferCache.getInstance().getFrameBuffer();
            if (blurFrameBuffer != null) {
                blurFrameBuffer.bindTexture(horizontalTexture);
            } else {
                throw new IllegalStateException("Failed to create framebuffer.");
            }
        }

        private ITexture getInputTexture() {
            return inputTexture;
        }

        private ITexture getHorizontalTexture() {
            return horizontalTexture;
        }

        private IFrameBuffer getBlurFrameBuffer() {
            return blurFrameBuffer;
        }

        private Bitmap getBitmap() {
            return bitmap;
        }

        private void finish() {
            if (inputTexture != null) {
                inputTexture.delete();
            }
            if (horizontalTexture != null) {
                horizontalTexture.delete();
            }
            FrameBufferCache.getInstance().recycleFrameBuffer(blurFrameBuffer);
        }
    }

}
