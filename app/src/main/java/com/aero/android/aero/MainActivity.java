package com.aero.android.aero;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            Intent startAccActivityIntent = new Intent(MainActivity.this,
                    Game.class);
            startActivity(startAccActivityIntent);
        });

        Button instructionsButton = findViewById(R.id.instructionsButton);
        instructionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InstructionsActivity.class);
            startActivity(intent);
        });

        Button scoreboardButton = findViewById(R.id.scoreboardButton);
        scoreboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScoreboardActivity.class);
            startActivity(intent);
        });

    }


}