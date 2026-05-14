package com.aero.android.aero;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.animation.Animation;
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
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
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
    private ImageView plane_view, heart1, heart2, heart3, crown, parkBackground, talkBubble;
    private ConstraintLayout layout;
    private ImageView[] clouds;

    private Deque<ImageView> deadHearts = new ArrayDeque<ImageView>();
    private Deque<ImageView> aliveHearts = new ArrayDeque<ImageView>();
    private ImageView heart;
    private View hitScreen;

    private ImageView[] obstacles;
    private TextView finalScoreText;
    private TextView scoreboardScoresText;
    private ConstraintLayout victoryMenu;
    private ConstraintLayout instructionsMenu;
    private ConstraintLayout pauseMenu;
    private ImageButton pauseButton;
    private ImageButton infoButton;
    private ImageView newHighScoreImage;

    private Vibrator vib;

    private float ALPHA = 0.8f;
    //change according to how hard you have to throw
    private double FORCE_THRESHHOLD = 30;

    private int birdSound, swooshSound, loseSound, highScoreSound;
    private int heartSound;


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
    private boolean startAnimationDone = false;
    private boolean last_heart_shake = true;
    private boolean newHighScore = false;
    private boolean hasShownInstructions = false;
    private long start_time = 0;
    private long highScoreTime = 0;
    private YoYo.YoYoString heartShakeAnimation;

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.seekTo(0);
        mediaPlayer.start();

        birdSound = soundPool.load(this,R.raw.hurt2,1);
        swooshSound = soundPool.load(this,R.raw.swoosh,1);
        loseSound = soundPool.load(this,R.raw.lose,1);
        highScoreSound = soundPool.load(this,R.raw.high_score,1);


        heartSound = soundPool.load(this, R.raw.collect_heart, 1);

        vib = this.getSystemService(Vibrator.class);

        //xml refrences
        hitScreen = findViewById(R.id.hitscreen);
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
        parkBackground = findViewById(R.id.parkBackground);
        talkBubble = findViewById(R.id.talkBubble);

        aliveHearts.addLast(heart1);
        aliveHearts.addLast(heart2);
        aliveHearts.addLast(heart3);

        finalScoreText = findViewById(R.id.timeText);
        scoreboardScoresText = findViewById(R.id.scoreboardScores);
        victoryMenu = findViewById(R.id.victoryMenuConstraint);
        newHighScoreImage = findViewById(R.id.newHighScore);
        pauseMenu = findViewById(R.id.pauseMenuConstraint);
        instructionsMenu = findViewById(R.id.instructionsMenuConstraint);

        scoreManager = new ScoreManager(this, 0, score_view);

        //settings alphas for intro transition
        heart1.setAlpha(0f);
        heart2.setAlpha(0f);
        heart3.setAlpha(0f);
        score_view.setAlpha(0f);


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
        pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(v -> {
            if (!game_paused) {
                game_paused = true;
                pauseMenu.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
                infoButton.setVisibility(View.GONE);
                if (!game_started) {
                    throw_instruction_view.setVisibility(TextView.GONE);
                } else {
                    mediaPlayer.pause();
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
            infoButton.setVisibility(View.VISIBLE);
            game_paused = false;
            if (!game_started) {
                throw_instruction_view.setVisibility(TextView.VISIBLE);
            } else {
                mediaPlayer.start();
            }
        });

        infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(v -> {
            showInstructions();
        });

        // show instructions if game is played for the first time
        if (!game_started && scoreManager.hasNoScores() && !hasShownInstructions) {
            showInstructions();
        }

        //Hides system bar for the phone
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            // Hide both the status bar (top) and navigation bar (bottom)
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());

            // This is the "magic" line: it makes the bars only appear
            // with a swipe, and they'll fade away automatically.
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    void showInstructions() {
        game_paused = true;
        hasShownInstructions = true;
        pauseButton.setVisibility(View.GONE);
        infoButton.setVisibility(View.GONE);
        instructionsMenu.setVisibility(View.VISIBLE);

        Button closeInstructionsButton = findViewById(R.id.homeButtonInstructions);
        closeInstructionsButton.setOnClickListener(v2 -> {
            pauseButton.setVisibility(View.VISIBLE);
            infoButton.setVisibility(View.VISIBLE);
            instructionsMenu.setVisibility(View.GONE);
            game_paused = false;
        });

        //Hide Status & Navigation Bar https://developer.android.com/training/system-ui/status
        /*View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/

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
            } else if (instanceTime > 0 && health > 0) { //The game has now started and this part handles that, makes the cloud start directly
                backgroundAnimator.animateClouds(scoreManager.getScore());

                if(instanceTime > 2000){ //delays birds and hearts
                    scoreManager.addScore(1L);
                    mediaPlayer.start();
                    handle_plane_tilt(x_value);
                    handle_score_checkpoints();

                    obstacleAnimator.animateObstacles(scoreManager.getScore());
                    heartAnimator.animateHeart(scoreManager.getScore());

                    if(aliveHearts.size() == 1 && last_heart_shake) {
                        ImageView heart = aliveHearts.peek();
                        heartShakeAnimation = YoYo.with(Techniques.Shake)
                                .duration(1000)
                                .repeat(Animation.INFINITE)
                                .playOn(heart);
                        last_heart_shake = false;
                    }

                    if (obstacleAnimator.isCollision(plane_view)) {

                        hitScreen.animate().withStartAction(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        hitScreen.setVisibility(View.VISIBLE);
                                    }
                                }
                        ).setDuration(50).withEndAction(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        hitScreen.setVisibility(View.GONE);
                                    }
                                }
                        ).start();

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
                        last_heart_shake = true;
                        if(heartShakeAnimation != null) {
                            heartShakeAnimation.stop();
                        }
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
                    newHighScore = true;
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
                pauseButton.setVisibility(ImageButton.GONE);
                mediaPlayer.stop();
                soundPool.play(loseSound,1,1,0,0,1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createOneShot(500,250));
                }
                finalScoreText.setText(String.format(Locale.US, "Score: %d", scoreManager.getScore()));
                scoreManager.saveHighScores();
                post_highScore = false;
                scoreboardScoresText.setText(scoreManager.getHighScores());
                victoryMenu.setVisibility(View.VISIBLE);
                if (newHighScore) {
                    newHighScoreImage.setVisibility(View.VISIBLE);
                    findViewById(R.id.sadSmiley).setVisibility(View.GONE);
                }
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
            infoButton.setVisibility(ImageButton.GONE);
            start_time = System.currentTimeMillis();
            scoreManager = new ScoreManager(this, (int) (Math.sqrt(x_max*x_max + y_max*y_max + z_max*z_max)*10), score_view);
            backgroundAnimator = new BackgroundAnimator(clouds, layout);
            obstacleAnimator = new ObstacleAnimator(obstacles, layout);
            heartAnimator = new HeartAnimator(heart, layout);
            x_prev = x_value;
            y_prev = y_value;
            z_prev = z_value;
            throwAnimator();
        }
    }

    private void throwAnimator() {
        int startValue = 0;
        int endValue = (int) scoreManager.getScore();

        soundPool.play(swooshSound, 1, 1, 0,0, 1);
        ValueAnimator animator = ValueAnimator.ofInt(startValue, endValue);
        animator.setDuration(3000); // 1.5 seconds
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // Get the current number for this specific frame
                int currentValue = (int) animation.getAnimatedValue();
                // Update the TextView
                throw_instruction_view.setText("SCORE: \n" + String.valueOf(currentValue));
            }
        });
        animator.start();

        talkBubble.animate()
                .alpha(0f)
                .setStartDelay(3000)
                .setDuration(1500)   // Take 1.5 seconds to fade out
                .setInterpolator(new DecelerateInterpolator())
                .start();

        throw_instruction_view.animate()
                .alpha(0f)
                .setStartDelay(3000) // Wait exactly 1.5 seconds before starting
                .setDuration(1500)   // Take 1.5 seconds to fade out
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        throw_instruction_view.setText("THROW THE \nPLANE");
                    }
                })
                .start();

        parkBackground.animate()
                .translationY(6000)
                .alpha(0f)// Move down
                .scaleX(4f)        // Zoom in horizontally (1.5x normal size)
                .scaleY(4f)        // Zoom in vertically (1.5x normal size)
                .setDuration(6000)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        heart1.animate()
                .alpha(1f)
                .setDuration(6000)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        heart2.animate()
                .alpha(1f)
                .setDuration(6000)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        heart3.animate()
                .alpha(1f)
                .setDuration(6000)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        score_view.animate()
                .alpha(1f)
                .setDuration(6000)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        pauseButton.animate()
                .alpha(1f)
                .setDuration(6000)
                .setInterpolator(new DecelerateInterpolator())
                .start();

    }

    private void handle_plane_tilt(float x_value) {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) plane_view.getLayoutParams();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        //can change the code slightly to avoid changing position when the change is very small to remove stuttering
        constraintSet.setHorizontalBias(R.id.plane, Math.min(1f, Math.max(lp.horizontalBias - x_value/80.0f, 0f)));
        constraintSet.applyTo(layout);
    }

    private void handle_score_checkpoints() {
        long score = scoreManager.getScore();
        if (score == 2000L || score == 4000L || score == 6000L) {
            obstacleAnimator.increaseRange();
        }
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
        heartShakeAnimation.stop();

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        constraintSet.setHorizontalBias(R.id.plane, 0.5f);
        constraintSet.applyTo(layout);

        backgroundAnimator.resetClouds();
        obstacleAnimator.resetObstacles();
        heartAnimator.resetHeart();

        victoryMenu.setVisibility(View.GONE);
        throw_instruction_view.setAlpha(1f);
        talkBubble.setAlpha(1f);
        if (newHighScore) {
            newHighScoreImage.setVisibility(View.GONE);
            findViewById(R.id.sadSmiley).setVisibility(View.VISIBLE);
            newHighScore = false;
        }

        throw_instruction_view.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(ImageButton.VISIBLE);
        infoButton.setVisibility(View.VISIBLE);
    }
}