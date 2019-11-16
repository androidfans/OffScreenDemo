package com.rejectliu.offscreendemo.view;

import android.app.Presentation;
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.rejectliu.offscreendemo.R;

public class SimplePresentation extends Presentation {
    public SimplePresentation(Context outerContext, Display display) {
        super(outerContext, display);
        setContentView(R.layout.demo_projection);
        View viewById = findViewById(R.id.textview);
//        viewById.setAnimation();
        TranslateAnimation animation = new TranslateAnimation(0, 1000, 0, 0);
        animation.setDuration(2000);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(Animation.INFINITE);
        viewById.setAnimation(animation);
        animation.start();
    }
}
