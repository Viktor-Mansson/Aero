package com.aero.android.aero;


import static java.lang.Long.parseLong;

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

    public void addScore(long s) {
        score += s;
        updateVisualScore();
    }

    private void updateVisualScore() {
        score_view.setText("score: " + score);
    }

    public long getScore() {
        return score;
    }
    public boolean checkHighScore() {
        String rawScores = getHighScores();
        //if no score
        if (rawScores == null || rawScores.trim().isEmpty()) {
            return getScore() > 0;
        }

        try {
            String[] highScores = rawScores.split("\n");
            String[] firstLineParts = highScores[0].split("\\. ");
            //safety check
            if (firstLineParts.length < 2) {
                return getScore() > 0;
            }
            long highScore = Long.parseLong(firstLineParts[1].trim());
            return getScore() > highScore;

        } catch (NumberFormatException e) {
            // 5. Catch the error if the parsed text isn't a valid number
            return false;
        }
    }
    //For Leaderboards
    public String getHighScores() {
        String savedString = sp.getString("highscores", "");
        String[] parts = savedString.isEmpty() ? new String[0] : savedString.split(",");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            sb.append(i + 1).append(". ");
            if (i < parts.length) {
                sb.append(String.format(Locale.US, "%d", parseLong(parts[i])));
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
                long val = parseLong(s);
                if (val > 0) scores.add(val);
            }
        }

        scores.add(score);
        Collections.sort(scores, Collections.reverseOrder());
        resetScore();

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
        score = 0L;
    }

    public boolean hasNoScores() {
        //sp.edit().remove("highscores").apply();
        return sp.getString("highscores", "").isEmpty();
    }
}
