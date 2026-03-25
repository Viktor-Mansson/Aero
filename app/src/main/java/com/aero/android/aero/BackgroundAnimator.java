package com.aero.android.aero;

import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class BackgroundAnimator {
    private ImageView[] clouds;
    private ConstraintLayout layout;
    private float cloud_speed = 5f;


    public BackgroundAnimator(ImageView[] c, ConstraintLayout l) {
        clouds = c.clone();
        layout = l;
    }

    public void animateClouds() {
        for (ImageView cloud: clouds) {
            cloud.setTranslationY(cloud.getTranslationY() + cloud_speed);
            checkBoundrary(cloud);
        }
    }

    private void checkBoundrary(ImageView cloud) {
        if (cloud.getTranslationY() > layout.getHeight()) {
            cloud.setTranslationY(cloud.getTranslationY() - layout.getHeight() - cloud.getHeight());
        }
    }
}
