package com.aero.android.aero;

import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class BackgroundAnimator {
    private final ImageView[] clouds;
    private final float[] initialYPositions;
    private final ConstraintLayout layout;
    private final float cloud_speed = 5f;

    public BackgroundAnimator(ImageView[] c, ConstraintLayout l) {
        clouds = c.clone();
        layout = l;

        initialYPositions = new float[clouds.length];
        for (int i = 0; i < clouds.length; i++) {
            initialYPositions[i] = clouds[i].getTranslationY();
        }
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

    public void resetClouds() {
        for (int i = 0; i < clouds.length; i++) {
            clouds[i].setTranslationY(initialYPositions[i]);
        }
    }
}
