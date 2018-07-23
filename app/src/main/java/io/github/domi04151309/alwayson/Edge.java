package io.github.domi04151309.alwayson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.BatteryManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class Edge extends AppCompatActivity {

    private View mContentView;
    public static boolean running;

    //Battery
    private TextView batteryTxt;
    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batteryTxt.setText(String.valueOf(level) + "%");

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        running=true;
        setContentView(R.layout.activity_edge);

        //Show on lockscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        //Hide UI
        mContentView = findViewById(R.id.fullscreen_content);
        hide();
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            hide();
                        }
                    }
                });

        //Battery
        batteryTxt = this.findViewById(R.id.batteryTxt);
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        //Time
        String date = new SimpleDateFormat("EEEE, MMM d").format(Calendar.getInstance());
        TextView dtTxt = this.findViewById(R.id.dTxt);
        dtTxt.setText(date);
        String hour = new SimpleDateFormat("HH:mm").format(Calendar.getInstance());
        TextView htTxt = this.findViewById(R.id.hTxt);
        htTxt.setText(hour);

        //Time updates
        time();

        //DoubleTap
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mContentView.setOnTouchListener(new View.OnTouchListener() {
            private final GestureDetector gestureDetector = new GestureDetector(Edge.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    int duration = preferences.getInt("ao_vibration", 64);
                    v.vibrate(duration);
                    Edge.this.finish();
                    return super.onDoubleTap(e);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                v.performClick();
                return true;
            }
        });
    }

    //Hide UI
    private void hide() {
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    //Time updates
    private void time() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String date = new SimpleDateFormat("EEEE, MMM d").format(Calendar.getInstance());
                                TextView dtTxt = findViewById(R.id.dTxt);
                                dtTxt.setText(date);
                                String hour = new SimpleDateFormat("HH:mm").format(Calendar.getInstance());
                                TextView htTxt = findViewById(R.id.hTxt);
                                htTxt.setText(hour);
                            }
                        });
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        };
        t.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mBatInfoReceiver);
        running=false;
    }
}