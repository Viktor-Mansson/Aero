package com.aero.android.aero;

import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ObstacleAnimator {
    private final ImageView[] obstacles;
    private final float[] initialYPositions;
    private final ConstraintLayout layout;
    private final float obstacle_speed = 5f;


    public ObstacleAnimator(ImageView[] c, ConstraintLayout l) {
        obstacles = c.clone();
        layout = l;

        initialYPositions = new float[obstacles.length];
        for (int i = 0; i < obstacles.length; i++) {
            initialYPositions[i] = obstacles[i].getTranslationY();
        }

        for (int i = 0; i < obstacles.length; i++) {
            obstacles[i].setTranslationX((float) (Math.random() * (l.getWidth() - obstacles[i].getWidth()*2)));

        }
    }

    public void animateObstacles() {
        for (ImageView cloud: obstacles) {
            cloud.setTranslationY(cloud.getTranslationY() + obstacle_speed);
            checkBoundary(cloud);
        }
    }

    private void checkBoundary(ImageView cloud) {
        if (cloud.getTranslationY() > layout.getHeight()) {
            cloud.setTranslationY(cloud.getTranslationY() - layout.getHeight() - cloud.getHeight());
        }

    }

    public void resetObstacles() {
        for (int i = 0; i < obstacles.length; i++) {
            obstacles[i].setTranslationY(initialYPositions[i]);
        }
    }
}
