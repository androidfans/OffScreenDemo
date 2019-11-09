package com.rejectliu.offscreendemo;

import android.opengl.GLES20;

import com.rejectliu.offscreendemo.util.FrameBuffer;
import com.rejectliu.offscreendemo.util.IFrameBuffer;

/**
 * Created by yuxfzju on 2017/1/22.
 */

public class FrameBufferFactory {

    public static IFrameBuffer create() {
        return new FrameBuffer();
    }

    public static IFrameBuffer create(int id) {
        return new FrameBuffer(id);
    }

    public static IFrameBuffer getDisplayFrameBuffer() {
        // Get the bound FBO (On Screen)
        final int[] displayFbo = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, displayFbo, 0);
        return create(displayFbo[0]);
    }
}
