package com.aero.android.aero;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
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
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.text.TextPaint;
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
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class Game extends AppCompatActivity implements SensorEventListener {

    //sensors
    private SensorManager sensorManager;
    private Sensor accelerometer;
    //sound effects
    private SoundPool soundPool;
    //background music
    private MediaPlayer mediaPlayer;
    //vibrations
    private long[] timings = {0, 80, 50, 80};
    private int[] amplitudes = {0, 150, 0, 255};

    //xml references
    private TextView score_view, highScore_view;
    private TextView throw_instruction_view;
    private ImageView plane_view, heart1, heart2, heart3, crown;
    private ConstraintLayout layout;
    private ImageView[] clouds;

    private Deque<ImageView> deadHearts = new ArrayDeque<ImageView>();
    private Deque<ImageView> aliveHearts = new ArrayDeque<ImageView>();
    private ImageView heart;

    private ImageView[] obstacles;
    private TextView finalScoreText;
    private TextView scoreboardScoresText;
    private ConstraintLayout victoryMenu;
    private ConstraintLayout pauseMenu;

    private Vibrator vib;

    private float ALPHA = 0.8f;
    //change according to how hard you have to throw
    private double FORCE_THRESHHOLD = 30;

    private int birdSound, swooshSound, loseSound, highScoreSound;
    private int heartSound;

    private int countDown;

    private TextView countDownText;


    private float[] gravity = new float[3];
    private float x_prev = 0f;
    private float y_prev = 0f;
    private float z_prev = 0f;
    private float x_max = 0f;
    private float y_max = 0f;
    private float z_max = 0f;
    private boolean game_started = false;
    private boolean game_over = false;
    private boolean game_paused = false;
    private boolean post_highScore = false;
    private long start_time = 0;
    private long highScoreTime = 0;

    private List<Handler> countdownHandlers = new ArrayList<>();
    private int countdownStreamId = 0;

    private int health = 3;

    //Game references
    private ScoreManager scoreManager;
    private BackgroundAnimator backgroundAnimator;
    private ObstacleAnimator obstacleAnimator;
    private HeartAnimator heartAnimator;


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
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(3).setAudioAttributes(audioAttributes).build();

        //Initiates background music and sets it to be looping
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.seekTo(0);
        mediaPlayer.start();


        birdSound = soundPool.load(this,R.raw.hurt2,1);
        swooshSound = soundPool.load(this,R.raw.swoosh,1);
        loseSound = soundPool.load(this,R.raw.lose,1);
        highScoreSound = soundPool.load(this,R.raw.high_score,1);


        heartSound = soundPool.load(this, R.raw.collect_heart, 1);
        countDown = soundPool.load(this, R.raw.mario_start,1);
        countDownText = findViewById(R.id.countdownText);

        vib = this.getSystemService(Vibrator.class);

        //xml refrences
        score_view = findViewById(R.id.score);
        highScore_view = findViewById(R.id.highScore);
        highScore_view.setVisibility(View.GONE);
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
        heart = findViewById(R.id.heart);
        heart1 = findViewById(R.id.heart1);
        heart2 = findViewById(R.id.heart2);
        heart3 = findViewById(R.id.heart3);
        crown = findViewById(R.id.crown);
        crown.setVisibility(View.GONE);

        aliveHearts.addLast(heart1);
        aliveHearts.addLast(heart2);
        aliveHearts.addLast(heart3);

        finalScoreText = findViewById(R.id.timeText);
        scoreboardScoresText = findViewById(R.id.scoreboardScores);
        victoryMenu = findViewById(R.id.victoryMenuConstraint);
        pauseMenu = findViewById(R.id.pauseMenuConstraint);

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
        ImageButton pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(v -> {
            if (!game_paused) {
                game_paused = true;
                cancelCountdown();
                pauseMenu.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
                if (!game_started) {
                    throw_instruction_view.setVisibility(TextView.GONE);
                }
            }
        });
        ImageButton homeButtonPause = findViewById(R.id.homeButtonPause);
        homeButtonPause.setOnClickListener(v -> {
            Intent intent = new Intent(Game.this, MainActivity.class);
            startActivity(intent);
        });
        ImageButton resumeButton = findViewById(R.id.resumeButton);
        resumeButton.setOnClickListener(v -> {
            pauseMenu.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            game_paused = false;
            if (!game_started) {
                throw_instruction_view.setVisibility(TextView.VISIBLE);
            }
        });
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.pause();
        cancelCountdown();
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
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !game_paused) {
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
                soundPool.play(swooshSound, 1, 1, 0,0, 1);
            } else if (instanceTime > 0 && health > 0) { //The game has now started and this part handles that, makes the cloud start directly
                handle_plane_tilt(x_value);
                if(instanceTime > 4800){
                    backgroundAnimator.animateClouds(scoreManager.getScore());

                }

                if(instanceTime > 7800) { //delays birds and hearts
                    scoreManager.addScore(1L);
                    handle_plane_tilt(x_value);


                    obstacleAnimator.animateObstacles(scoreManager.getScore());
                    heartAnimator.animateHeart(scoreManager.getScore());


                    if (obstacleAnimator.isCollision(plane_view)) {
                        health -= 1;
                        soundPool.play(birdSound, 1, 1, 0, 0, 1);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vib.vibrate(VibrationEffect.createOneShot(100, 180));
                        }
                        ImageView heart = aliveHearts.pop();
                        heart.setVisibility(View.GONE); // takes away a heart when collision
                        deadHearts.addFirst(heart); //adds the heart to a deadstack that hearts can be taken from when flying into one
                    }

                    if (heartAnimator.isCollision(plane_view) && health != 0) {
                        health = Math.min(health + 1, 3);
                        soundPool.play(heartSound, 1, 1, 0, 0, 1);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
                        }
                        if (!deadHearts.isEmpty()) {
                            ImageView heart = deadHearts.pop();
                            heart.setVisibility(View.VISIBLE);
                            aliveHearts.addFirst(heart);
                        }
                    }
                }
                if(scoreManager.checkHighScore() && !post_highScore) {
                    post_highScore = true;
                    soundPool.play(highScoreSound, 1, 1, 0,0, 1);
                    highScore_view.setVisibility(View.VISIBLE);
                    crown.setVisibility(View.VISIBLE);
                    highScoreTime = instanceTime;
                    YoYo.with(Techniques.Tada)
                            .duration(1000)
                            .repeat(1)
                            .playOn(findViewById(R.id.highScore));
                }
                if(instanceTime > highScoreTime + 3000 && instanceTime < highScoreTime + 3100) {
                    highScore_view.setVisibility(View.GONE);
                    crown.setVisibility(View.GONE);
                }

            } else if ( health == 0 && !game_over) {
                game_over = true;
                soundPool.play(loseSound,1,1,0,0,1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createOneShot(500,250));
                }
                finalScoreText.setText(String.format(Locale.US, "Score: %d", scoreManager.getScore()));
                scoreManager.saveHighScores();
                post_highScore = false;
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
            mediaPlayer.pause(); // pause music during the sound
            countDownText.setVisibility(View.VISIBLE);
            countDownText.setText("3");

            Handler h0 = new Handler(Looper.getMainLooper());
            h0.postDelayed(() -> {
                countdownStreamId = soundPool.play(countDown, 1, 1, 0, 0, 1);
            }, 100);
            countdownHandlers.add(h0);

            Handler h1 = new Handler(Looper.getMainLooper());
            h1.postDelayed(() -> countDownText.setText("2"), 1400);
            countdownHandlers.add(h1);

            Handler h2 = new Handler(Looper.getMainLooper());
            h2.postDelayed(() -> countDownText.setText("1"), 2500);
            countdownHandlers.add(h2);

            Handler h3 = new Handler(Looper.getMainLooper());
            h3.postDelayed(() -> {
                countDownText.setVisibility(View.GONE);
                mediaPlayer.start();
            }, 4800);
            countdownHandlers.add(h3);
            scoreManager = new ScoreManager(this, (int) (Math.sqrt(x_max*x_max + y_max*y_max + z_max*z_max)*10), score_view);
            backgroundAnimator = new BackgroundAnimator(clouds, layout);
            obstacleAnimator = new ObstacleAnimator(obstacles, layout);
            heartAnimator = new HeartAnimator(heart, layout);
            x_prev = x_value;
            y_prev = y_value;
            z_prev = z_value;
        }
    }

    private void cancelCountdown() {
        for (Handler h : countdownHandlers) {
            h.removeCallbacksAndMessages(null);
        }
        countdownHandlers.clear();
        countDownText.setVisibility(View.GONE);
        soundPool.stop(countdownStreamId);
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
        mediaPlayer.start();
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
        scoreManager.addScore(0L);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        constraintSet.setHorizontalBias(R.id.plane, 0.5f);
        constraintSet.applyTo(layout);

        backgroundAnimator.resetClouds();
        obstacleAnimator.resetObstacles();
        heartAnimator.resetHeart();
        countDownText.setVisibility(View.GONE);

        victoryMenu.setVisibility(View.GONE);
        throw_instruction_view.setVisibility(View.VISIBLE);
    }
}