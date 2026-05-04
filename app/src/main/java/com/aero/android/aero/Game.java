package com.aero.android.aero;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;


public class Game extends AppCompatActivity implements SensorEventListener {

    //sensors
    private SensorManager sensorManager;
    private Sensor accelerometer;
    //sound effects
    private SoundPool soundPool;
    //background music
    private MediaPlayer mediaPlayer;

    //xml references
    private TextView score_view;
    private TextView throw_instruction_view;
    private ImageView plane_view, heart1, heart2, heart3;
    private ConstraintLayout layout;
    private ImageView[] clouds;

    private Deque<ImageView> deadHearts = new ArrayDeque<ImageView>();
    private Deque<ImageView> aliveHearts = new ArrayDeque<ImageView>();

    private ImageView[] obstacles;
    private TextView finalScoreText;
    private TextView scoreboardScoresText;
    private ConstraintLayout victoryMenu;

    private Vibrator vib;

    private float ALPHA = 0.8f;
    //change according to how hard you have to throw
    private double FORCE_THRESHHOLD = 30;

    private int birdSound;


    private float[] gravity = new float[3];
    private float x_prev = 0f;
    private float y_prev = 0f;
    private float z_prev = 0f;
    private float x_max = 0f;
    private float y_max = 0f;
    private float z_max = 0f;
    private boolean game_started = false;
    private boolean game_over = false;
    private long start_time = 0;

    private int health = 3;

    //Game references
    private ScoreManager scoreManager;
    private BackgroundAnimator backgroundAnimator;

    private ObstacleAnimator obstacleAnimator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Manages so that the volume buttons changes the correct sound stream.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Sets up soundpool for sound effects.
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        soundPool = new SoundPool.Builder().setMaxStreams(3).setAudioAttributes(audioAttributes).build();

        //Initiates background music and sets it to be looping
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.seekTo(0);
        mediaPlayer.start();

        birdSound = soundPool.load(this,R.raw.birdhit,1);
        vib = this.getSystemService(Vibrator.class);

        //xml refrences
        score_view = findViewById(R.id.score);
        throw_instruction_view = findViewById(R.id.throw_instruction);
        plane_view = findViewById(R.id.plane);
        layout = findViewById(R.id.main);
        clouds = new ImageView[5];
        clouds[0] = findViewById(R.id.cloud1);
        clouds[1] = findViewById(R.id.cloud2);
        clouds[2] = findViewById(R.id.cloud3);
        clouds[3] = findViewById(R.id.cloud4);
        clouds[4] = findViewById(R.id.cloud5);
        obstacles = new ImageView[4];
        obstacles[0] = findViewById(R.id.obstacle1);
        obstacles[1] = findViewById(R.id.obstacle2);
        obstacles[2] = findViewById(R.id.obstacle3);
        obstacles[3] = findViewById(R.id.obstacle4);
        heart1 = findViewById(R.id.heart1);
        heart2 = findViewById(R.id.heart2);
        heart3 = findViewById(R.id.heart3);

        aliveHearts.addLast(heart1);
        aliveHearts.addLast(heart2);
        aliveHearts.addLast(heart3);

        finalScoreText = findViewById(R.id.timeText);
        scoreboardScoresText = findViewById(R.id.scoreboardScores);
        victoryMenu = findViewById(R.id.victoryMenuConstraint);

        //Initialize Buttons
        ImageButton startButton = findViewById(R.id.restartButton);
        startButton.setOnClickListener(v -> {
            resetGame();
        });
        ImageButton homeButton = findViewById(R.id.homeButtonScoreboard);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(Game.this, MainActivity.class);
            startActivity(intent);
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.pause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Time code
            long current_time = System.currentTimeMillis();
            long instanceTime = current_time - start_time;

            //tilt of phone
            float x_value = event.values[0];
            float y_value = event.values[1];
            float z_value = event.values[2];

            if (!game_started) {
                //lowpass filter to filter out gravity (taken from a previous course (BMEN20))
                gravity[0] = ALPHA * gravity[0] + (1- ALPHA) * x_value;
                gravity[1] = ALPHA * gravity[1] + (1- ALPHA) * y_value;
                gravity[2] = ALPHA * gravity[2] + (1- ALPHA) * z_value;
                x_value = x_value - gravity[0];
                y_value = y_value - gravity[1];
                z_value = z_value - gravity[2];

                handle_throw(x_value, y_value, z_value);
            } else if (instanceTime > 2000 && health > 0) { //The game has now started and this part handles that
                scoreManager.addScore(1);
                handle_plane_tilt(x_value);
                backgroundAnimator.animateClouds();
                obstacleAnimator.animateObstacles();

                if (obstacleAnimator.isCollision(plane_view)) {
                    health -=1;
                    soundPool.play(birdSound,1,1,0,0,1);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createOneShot(100,180));
                    }
                    ImageView heart = aliveHearts.pop();
                    heart.setVisibility(View.GONE); // takes away a heart when collision
                    deadHearts.addLast(heart); //adds the heart to a deadstack that hearts can be taken from when flying into one
                }

            } else if ( health == 0) { //REMOVE LATER
                game_over = true;
                finalScoreText.setText(String.format(Locale.US, "Score: %d", scoreManager.getScore()));
                scoreManager.saveHighScores();
                scoreboardScoresText.setText(scoreManager.getHighScores());
                victoryMenu.setVisibility(View.VISIBLE);
            }
        }
    }

    private void handle_throw(float x_value, float y_value, float z_value) {
        //checks force of movement for throw
        double force = Math.sqrt(x_value*x_value + y_value*y_value + z_value*z_value);
        if (force > FORCE_THRESHHOLD) {
            x_max = Math.max(x_max, x_value);
            y_max = Math.max(y_max, y_value);
            z_max = Math.max(z_max, z_value);
            x_prev = x_value;
            y_prev = y_value;
            z_prev = z_value;
        }
        //the end of the throw
        else if (Math.sqrt(x_prev*x_prev + y_prev*y_prev + z_prev*z_prev) > FORCE_THRESHHOLD) {
            //point gain for force
            game_started = true;
            start_time = System.currentTimeMillis();
            throw_instruction_view.setVisibility(TextView.GONE);
            scoreManager = new ScoreManager(this, (int) (Math.sqrt(x_max*x_max + y_max*y_max + z_max*z_max)*10), score_view);
            backgroundAnimator = new BackgroundAnimator(clouds, layout);
            obstacleAnimator = new ObstacleAnimator(obstacles, layout);
            x_prev = x_value;
            y_prev = y_value;
            z_prev = z_value;
        }
    }

    private void handle_plane_tilt(float x_value) {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) plane_view.getLayoutParams();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        //can change the code slightly to avoid changing position when the change is very small to remove stuttering
        constraintSet.setHorizontalBias(R.id.plane, Math.min(1f, Math.max(lp.horizontalBias - x_value/80.0f, 0f)));
        constraintSet.applyTo(layout);
    }


    private void resetGame() {
        gravity = new float[3];
        x_prev = 0f;
        y_prev = 0f;
        z_prev = 0f;
        x_max = 0f;
        y_max = 0f;
        z_max = 0f;
        game_started = false;
        game_over = false;
        start_time = 0;
        health = 3;

        aliveHearts.addLast(heart1);
        aliveHearts.addLast(heart2);
        aliveHearts.addLast(heart3);
        heart1.setVisibility(View.VISIBLE);
        heart2.setVisibility(View.VISIBLE);
        heart3.setVisibility(View.VISIBLE);
        deadHearts.clear();
        scoreManager.addScore(0);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        constraintSet.setHorizontalBias(R.id.plane, 0.5f);
        constraintSet.applyTo(layout);

        backgroundAnimator.resetClouds();
        obstacleAnimator.resetObstacles();

        victoryMenu.setVisibility(View.GONE);
        throw_instruction_view.setVisibility(View.VISIBLE);
    }
}