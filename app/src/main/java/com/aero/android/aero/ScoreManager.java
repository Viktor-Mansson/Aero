package com.aero.android.aero;


import android.widget.TextView;

//Imports for scoreboard
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import android.content.SharedPreferences;
import android.content.Context;

//manages all score related code
public class ScoreManager {
    private long score;
    private final TextView score_view;

    //For Leaderboards
    private final SharedPreferences sp;

    public ScoreManager(Context context, int init_score, TextView s_view) {
        score = init_score;
        score_view = s_view;
        updateVisualScore();

        //For Leaderboards
        sp = context.getSharedPreferences("Leaderboard", Context.MODE_PRIVATE);
    }

    public void addScore(int s) {
        score += s;
        updateVisualScore();
    }

    private void updateVisualScore() {
        score_view.setText("score: " + score);
    }

    public long getScore() {
        return score;
    }

    //For Leaderboards
    public String getHighScores() {
        String savedString = sp.getString("highscores", "");
        String[] parts = savedString.isEmpty() ? new String[0] : savedString.split(",");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            sb.append(i + 1).append(". ");
            if (i < parts.length) {
                sb.append(String.format(Locale.US, "%d", Long.parseLong(parts[i])));
            } else {
                sb.append("---");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    //For Leaderboards
    public void saveHighScores() {
        String savedScoresString = sp.getString("highscores", "");
        List<Long> scores = new ArrayList<>();

        if (!savedScoresString.isEmpty()) {
            String[] parts = savedScoresString.split(",");
            for (String s : parts) {
                long val = Long.parseLong(s);
                if (val > 0) scores.add(val);
            }
        }

        scores.add(score);
        resetScore();
        Collections.sort(scores, Collections.reverseOrder());

        //Only keep top 10
        if (scores.size() > 10) scores = scores.subList(0, 10);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scores.size(); i++) {
            sb.append(scores.get(i));
            if (i < scores.size() - 1) sb.append(",");
        }

        sp.edit().putString("highscores", sb.toString()).apply();
    }

    private void resetScore() {
        score = 0;
    }
}
