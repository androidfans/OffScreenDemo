package com.rejectliu.offscreendemo.util;

import androidx.annotation.IntDef;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({FILTER_TYPE.GAUSSIAN_BLUR_OLD, FILTER_TYPE.GAUSSIAN_BLUR_GPU_IMAGE,FILTER_TYPE.STACK_BLUR,FILTER_TYPE.BOX_BLUR})
@Retention(RetentionPolicy.SOURCE)
public @interface FILTER_TYPE {
    int GAUSSIAN_BLUR_OLD = 0;
    int GAUSSIAN_BLUR_GPU_IMAGE = 1;
    int STACK_BLUR = 2;
    int BOX_BLUR = 3;
}