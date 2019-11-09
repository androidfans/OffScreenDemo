package com.rejectliu.offscreendemo.util;

/**
 * Created by yuxfzju on 17/1/20.
 */

public class TextureFactory {
    public static Texture create(int width, int height) {
        return new SimpleTexture(width, height);
    }

}
