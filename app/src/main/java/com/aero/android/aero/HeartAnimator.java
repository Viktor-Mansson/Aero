package com.aero.android.aero;

import android.graphics.Rect;
import android.media.Image;
import android.view.View;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class HeartAnimator {
    private final ImageView heart;
    private final float initialYPosition;
    private final ConstraintLayout layout;
    private float heart_speed = 5f;


    public HeartAnimator(ImageView h, ConstraintLayout l) {
        heart = h;
        layout = l;

        initialYPosition = heart.getTranslationY();

        layout.post(() -> {
            float maxX = layout.getWidth() - heart.getWidth();
            heart.setX((float) (Math.random() * maxX));
        });
    }

    private Rect getCustomHitbox(View v, int marginX, int marginY) {
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
        Rect rect2 = getCustomHitbox(heart, 20, 20);

        if (Rect.intersects(rect1, rect2)) {
            resetHeart();
            return true;
        }
        return false;
    }

    public void animateHeart(long score) {
        heart_speed = 5f + score * 0.0005f;
        heart.setTranslationY(heart.getTranslationY() + heart_speed);
        checkBoundary(heart);
    }

    private void checkBoundary(ImageView heart) {
        if (heart.getTranslationY() > layout.getHeight()) {
            heart.setTranslationY(heart.getTranslationY() - layout.getHeight() - heart.getHeight());

            float maxX = layout.getWidth() - heart.getWidth();
            heart.setX((float) (Math.random() * maxX));
        }
    }

    public void resetHeart() {
        heart.setTranslationY(initialYPosition);
    }
}