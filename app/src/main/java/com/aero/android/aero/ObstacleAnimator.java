package com.aero.android.aero;

import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Random;

public class ObstacleAnimator {
    private final ImageView[] obstacles;
    private final float[] initialYPositions;
    private final ConstraintLayout layout;
    private final Random r = new Random();
    private final float[] obstacle_speeds = {8f, 13f, 6f, 8f};
    private int[] obstacle_indexes = {0,0,0,0};
    private int[] obstacle_probs = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private int difficulty = 0;
    private double[] kite_movements = {0d, Math.PI/2, Math.PI, 3*Math.PI/2};


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

    public Rect getCustomHitbox(View v, int marginX, int marginY) {
        Rect rect = new Rect();
        v.getGlobalVisibleRect(rect);
        rect.left += marginX;
        rect.right -= marginX;
        rect.top += marginY;
        rect.bottom -= marginY;
        return rect;
    }
    public boolean isCollision(View v1) {
        Rect rect1 = getCustomHitbox(v1, 40, 40);

        for (ImageView obstacle : obstacles) {
            Rect rect2 = getCustomHitbox(obstacle, 30, 58);

            if (Rect.intersects(rect1, rect2)) {
                obstacle.setTranslationY(obstacle.getTranslationY() - layout.getHeight() - obstacle.getHeight());
                return true;
            }
        }
        return false;
    }

    public void animateObstacles(long score) {
        int index = 0;
        for (ImageView obstacle: obstacles) {
            obstacle.setTranslationY(obstacle.getTranslationY() + obstacle_speeds[obstacle_indexes[index]] + score * 0.001f);
            checkBoundary(obstacle, index);
            if (obstacle_indexes[index] == 3) {
                obstacle.setTranslationX(obstacle.getTranslationX() + (float)(6*Math.sin(kite_movements[index])));
            }
            index += 1;
        }
        handle_kite_states();
    }

    private void checkBoundary(ImageView obstacle, int index) {
        if (obstacle.getTranslationY() > layout.getHeight()) {
            obstacle.setTranslationY(obstacle.getTranslationY() - layout.getHeight() - obstacle.getHeight());
            float maxX = layout.getWidth() - obstacle.getWidth();
            obstacle.setX((float) (Math.random() * maxX));
            //Code to change enemy type
            //int idx = r.nextInt(obstacle_range);
            int idx = obstacle_probs[(int)(Math.random() * obstacle_probs.length)];
            switch (idx) {
                case 0:
                    obstacle.setImageResource(R.drawable.bird);
                    obstacle.setScaleX(1f);
                    obstacle.setScaleY(1f);
                    break;
                case 1:
                    obstacle.setImageResource(R.drawable.bird2);
                    obstacle.setScaleX(0.7f);
                    obstacle.setScaleY(0.7f);
                    break;
                case 2:
                    obstacle.setImageResource(R.drawable.bird3);
                    obstacle.setScaleX(1.2f);
                    obstacle.setScaleY(1.2f);
                    break;
                case 3:
                    obstacle.setImageResource(R.drawable.kite);
                    obstacle.setScaleX(1f);
                    obstacle.setScaleY(1f);
                    break;
            }
            obstacle_indexes[index] = idx;
        }

    }

    public void increaseRange() {
        switch (difficulty) {
            case 0:
                for (int i = 0; i < 6; i++) {
                    obstacle_probs[i] = 1;
                }
                break;
            case 1:
                for (int i = 4; i < 8; i++) {
                    obstacle_probs[i] = 2;
                }
                break;
            case 2:
                for (int i = 7; i < 10; i++) {
                    obstacle_probs[i] = 3;
                }
                break;
        }
        difficulty += 1;
    }

    private void handle_kite_states() {
        for (int i = 0; i < kite_movements.length; i++) {
            kite_movements[i] = kite_movements[i] + 0.03 % (2*Math.PI);
        }
    }

    public void resetObstacles() {
        for (int i = 0; i < obstacles.length; i++) {
            obstacles[i].setTranslationY(initialYPositions[i]);
        }
        difficulty = 0;
        obstacle_probs = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        kite_movements = new double[]{0d, Math.PI / 2, Math.PI, 3 * Math.PI / 2};
        obstacle_indexes = new int[]{0, 0, 0, 0};
    }
}
