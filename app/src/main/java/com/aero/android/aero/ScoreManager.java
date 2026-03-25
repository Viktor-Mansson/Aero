package com.aero.android.aero;

import android.widget.TextView;

//manages all score related code
public class ScoreManager {
    private int score = 0;
    private final TextView score_view;

    public ScoreManager(int init_score, TextView s_view) {
        score = init_score;
        score_view = s_view;
        updateVisualScore();
    }

    public void addScore(int s) {
        score += s;
        updateVisualScore();
    }

    private void updateVisualScore() {
        score_view.setText("score: " + score);
    }

    public int getScore() {
        return score;
    }
}
