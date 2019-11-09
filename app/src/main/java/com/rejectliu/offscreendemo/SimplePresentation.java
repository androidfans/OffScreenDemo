package com.rejectliu.offscreendemo;

import android.app.Presentation;
import android.content.Context;
import android.view.Display;

public class SimplePresentation extends Presentation {
    public SimplePresentation(Context outerContext, Display display) {
        super(outerContext, display);
        setContentView(R.layout.demo_projection);
    }
}
