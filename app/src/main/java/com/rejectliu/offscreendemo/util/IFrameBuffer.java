package com.rejectliu.offscreendemo.util;

/**
 * Created by yuxfzju on 2017/1/22.
 */

public interface IFrameBuffer {

    void create();

    void bindTexture(ITexture texture);

    void bindSelf();

    void delete();
}
