package com.example.mate_pc.game1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.content.Intent;
import android.media.MediaPlayer;

import com.example.mate_pc.game1.graphical_stuff.Ricardo;
import com.example.mate_pc.game1.network_stuff.ConnectorClass;
import com.example.mate_pc.game1.network_stuff.WebSocketClass;
import com.example.mate_pc.game1.sound_stuff.BackgroundSoundHandler;

import static com.example.mate_pc.game1.Constants.CONNECTOR_IP_CODE;
import static com.example.mate_pc.game1.Constants.MY_SETTINGS;
import static com.example.mate_pc.game1.Constants.RESULT_CODE_SETTINGS_MAY_CHANGED;
import static com.example.mate_pc.game1.Constants.SHOW_JOYSTICK_SETTINGS_KEY;
import static com.example.mate_pc.game1.Constants.START_CONNECTOR_CODE;
import static com.example.mate_pc.game1.Constants.CONTROL_SETTINGS_KEY;
import static com.example.mate_pc.game1.Constants.SOUND_SETTINGS_KEY;

public class MainActivity extends AppCompatActivity {

    GameSurface gameSurface;

    WebSocketClass webSocket;

    Button shootButton;
    Button left;
    Button right;
    Button up;
    Button playButton;

    LinearLayout joystickSurface;

    private final static int INTERVAL = 200;
    Handler mHandler = new Handler();

    @SuppressLint("ClickableViewAccessibility") // Silencing "performClick" warning. Delete line to see affect.
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameSurface = new GameSurface(this, this);

        // Passing gameSurface as GameSurface and as OnConnectionChangedListener.
        webSocket = new WebSocketClass(gameSurface, gameSurface);
        gameSurface.setWebSocket(webSocket);

        LinearLayout surface = findViewById(R.id.gameSurface);
        surface.addView(gameSurface);

        Intent intent = new Intent(MainActivity.this, ConnectorClass.class);
        startActivityForResult(intent, START_CONNECTOR_CODE);


        joystickSurface = findViewById(R.id.joystickSurface);
        joystickSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gameSurface.onJoystickTouchListener(motionEvent);
                return true;
            }
        });


        playButton = findViewById(R.id.play);
        playButton.setVisibility(View.INVISIBLE);       //Should be set to visible upon winning the game
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent playIntent = new Intent(MainActivity.this, Ricardo.class);
                startActivity(playIntent);
            }
        });

        shootButton = findViewById(R.id.shoot);
        shootButton.setOnClickListener(onClickListener);

        alreadyShooting = false;

        up = findViewById(R.id.button);
        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                handleTouch(view, motionEvent);
                return true;
            }
        });

        left = findViewById(R.id.button2);
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                handleTouch(view, motionEvent);
                return true;
            }
        });
        right = findViewById(R.id.button3);
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                handleTouch(view, motionEvent);
                return true;
            }
        });

        new BackgroundSoundHandler(this);
        setControl();
        //setSound();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        setControl();
        //setSound();

        if (data != null) {
            webSocket.setIP(data.getStringExtra(CONNECTOR_IP_CODE));
            webSocket.connect();
        }
        else {
            Log.i("MyTAG", "EMPTY intent data");
        }
        new CountDown(MainActivity.this, webSocket).execute();

    }




    private void setControl(){
        SharedPreferences prefs = getSharedPreferences(MY_SETTINGS, MODE_PRIVATE);
        boolean isJoystick = prefs.getBoolean(CONTROL_SETTINGS_KEY, false);
        boolean showJoystick = prefs.getBoolean(SHOW_JOYSTICK_SETTINGS_KEY, true);
        if(isJoystick){
            up.setVisibility(View.INVISIBLE);
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.INVISIBLE);
        }
        else{
            up.setVisibility(View.VISIBLE);
            left.setVisibility(View.VISIBLE);
            right.setVisibility(View.VISIBLE);
        }
        gameSurface.setJoystickUsage(isJoystick, showJoystick);
    }


    private void handleTouch(View view, MotionEvent event) {
        int action = event.getAction();
        if (view.getId() == R.id.button){
            if (action == MotionEvent.ACTION_DOWN){
                jump();
                up.setBackground(getDrawable(R.drawable.ic_arrow_upward_black_24dp_pressed));
            }
            else if (action == MotionEvent.ACTION_UP) {
                up.setBackground(getDrawable(R.drawable.ic_arrow_upward_black_24dp));
            }
        }
        if (view.getId() == R.id.button2){
            if (action == MotionEvent.ACTION_DOWN){
                setMovementX(-1, false);
                left.setBackground(getDrawable(R.drawable.ic_arrow_back_black_24dp_pressed));
            }
            else if (action == MotionEvent.ACTION_UP) {
                setMovementX(0, true);
                left.setBackground(getDrawable(R.drawable.ic_arrow_back_black_24dp));
            }
        }
        if (view.getId() == R.id.button3){
            if (action == MotionEvent.ACTION_DOWN){
                setMovementX(1, false);
                right.setBackground(getDrawable(R.drawable.ic_arrow_forward_black_24dp_pressed));
            }
            else if (action == MotionEvent.ACTION_UP) {
                setMovementX(0, true);
                right.setBackground(getDrawable(R.drawable.ic_arrow_forward_black_24dp));
            }
        }
    }


    private void setMovementX(int x, boolean stopMovement) {
        gameSurface.setCharacterHorizontalAcceleration(x, stopMovement);
    }

    private void jump() {
        gameSurface.setCharacterVerticalAcceleration(-1);
    }

    boolean alreadyShooting;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if(!alreadyShooting) {
                alreadyShooting = true;
                gameSurface.createBullet();

                startRepeatingTask();
            }
        }
    };


    int counter = 0;
    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            if(counter == 4){
                stopRepeatingTask();
                counter=0;
                alreadyShooting = false;
                return;
            }

            updateShootButtonState();
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    public void updateShootButtonState() {
        switch (counter) {
            case 0: {
                shootButton.setBackground(getDrawable(R.drawable.pistol0));
                counter++;
                break;

            }
            case 1: {
                shootButton.setBackground(getDrawable(R.drawable.pistol1));
                counter++;
                break;

            }
            case 2: {
                shootButton.setBackground(getDrawable(R.drawable.pistol2));
                counter++;
                break;

            }
            case 3: {
                shootButton.setBackground(getDrawable(R.drawable.pistol3));
                counter++;
                break;

            }
        }
    }

    void startRepeatingTask()
    {
        mHandlerTask.run();
    }

    void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mHandlerTask);
    }

    @SuppressLint("MissingSuperCall")
    protected void onStop () {
        super.onStop();
        Log.i("MyTAG", "stop");
        //StopBackgroundAudio();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("MyTAG", "destroy");
        //StopBackgroundAudio();
        //this.mediaPlayer.stop();
        //this.mediaPlayer.release();
        gameSurface.destroy();

        webSocket.destroySocket();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MyTAG", "resume");

        gameSurface.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("MyTAG", "pause");

        gameSurface.pause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("MyTAG", "restart");

        gameSurface.restart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("MyTAG", "start");
    }

}
