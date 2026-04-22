package com.aero.android.aero;

import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ObstacleAnimator {
    private final ImageView[] obstacles;
    private final float[] initialYPositions;
    private final ConstraintLayout layout;
    private final float obstacle_speed = 10f;


    public ObstacleAnimator(ImageView[] c, ConstraintLayout l) {
        obstacles = c.clone();
        layout = l;

        initialYPositions = new float[obstacles.length];
        for (int i = 0; i < obstacles.length; i++) {
            initialYPositions[i] = obstacles[i].getTranslationY();
        }

        layout.post(() -> {
            for (ImageView obstacle : obstacles) {
                float maxX = layout.getWidth() - obstacle.getWidth();
                obstacle.setX((float) (Math.random() * maxX));
            }
        });
    }

    public void animateObstacles() {
        for (ImageView cloud: obstacles) {
            cloud.setTranslationY(cloud.getTranslationY() + obstacle_speed);
            checkBoundary(cloud);
        }
    }

    private void checkBoundary(ImageView obstacle) {
        if (obstacle.getTranslationY() > layout.getHeight()) {
            obstacle.setTranslationY(obstacle.getTranslationY() - layout.getHeight() - obstacle.getHeight());

            float maxX = layout.getWidth() - obstacle.getWidth();
            obstacle.setX((float) (Math.random() * maxX));
        }

    }

    public void resetObstacles() {
        for (int i = 0; i < obstacles.length; i++) {
            obstacles[i].setTranslationY(initialYPositions[i]);
        }
    }
}
