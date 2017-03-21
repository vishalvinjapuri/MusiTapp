package io.paperplane.rajb.knockoclock;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;

import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ListenService extends Service implements SensorEventListener {

    public volatile boolean spikeDetected = false;
    private SensorManager mSensorManager;
    final public float thresholdZ = 2;
    final public float threshholdX = 8;
    final public float threshholdY = 8;
    final public int updateFrequency = 100;
    private Context mContext;
    private static TextToSpeech t1,t2;
    private ServiceConnection sc;


    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";

    public String artistname = "";
    public String trackname = "";

    private float prevZVal = 0;
    private float currentZVal = 0;
    private float diffZ = 0;

    private float prevXVal = 0;
    private float currentXVal = 0;
    private float diffX = 0;

    private float prevYVal = 0;
    private float currentYVal = 0;
    private float diffY = 0;

    private static String title, text;
    private  NotificationManager nm;
    public static AudioManager manager;
    public Timer timer;
    public int knocksdetected = 0;

    public BroadcastReceiver mReceiver;

    IntentFilter iF = new IntentFilter();

    public static void readTime(String track, String artist){
        /*int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        Log.d("DEBUG", hour + ":" + minute);
        String toSpeak;
        String weather;
        String ampm;
        if(hour - 12 > 0){
            hour -= 12;
            ampm = "P M";
        }else{
            ampm = "A M";
        }
        if(minute < 10 && minute != 0){
            toSpeak = "The time is " + hour + " o " + minute + " " + ampm;
        }else if(minute == 0){
            toSpeak = "The time is " + hour + " o clock" + " " + ampm;
        }
        else{
            toSpeak = "The time is " + hour + " " + minute + " " + ampm +".";
        }
        weather=" In San Hose A, it is around 54 degrees fahrenheit with no chance of rain";*/
        String toSpeak;
        toSpeak = "Now playing: " + track + "bai " + artist;
        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    private static void sendMediaButton(Context context, int keyCode) {
        KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        context.sendOrderedBroadcast(intent, null);

        keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        context.sendOrderedBroadcast(intent, null);
    }


    public static void readNotifications(){

    }


    public void stopAccSensing(){
        mSensorManager.unregisterListener(this);
    }

    public void resumeAccSensing(){
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 1000000/updateFrequency);
    }

    public void onSensorChanged(SensorEvent event) {
        prevXVal = currentXVal;
        currentXVal = abs(event.values[0]); // X-axis
        diffX = currentXVal - prevXVal;

        prevYVal = currentYVal;
        currentYVal = abs(event.values[1]); // Y-axis
        diffY = currentYVal - prevYVal;

        prevZVal = currentZVal;
        currentZVal = abs(event.values[2]); // Z-axis
        diffZ = currentZVal - prevZVal;

        //Z force must be above some limit, the other forces below some limit to filter out shaking motions
        if (currentZVal > prevZVal && diffZ > thresholdZ && diffX < threshholdX && diffY < threshholdY && currentXVal < 1 && currentYVal < 1){
            Log.d("DEBUG", "got a knockk");
            if(knocksdetected == 0) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("DEBUG", "knocks detected: " + knocksdetected);

                        accTapEvent(knocksdetected);
                    }
                }, 700);
            }
            knocksdetected++;

        }

    }

        private void accTapEvent(int knocks){

            manager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);


            if(knocks <= 3) {

                Log.d("acceltap","single tap event detected!");



                if (manager.isMusicActive()) {
                    sendMediaButton(getApplicationContext(), KeyEvent.KEYCODE_MEDIA_PAUSE);
                } else if (!manager.isMusicActive()) {
                    sendMediaButton(getApplicationContext(), KeyEvent.KEYCODE_MEDIA_PLAY);
                }

            }
            else if(knocks >= 4 && knocks < 8){
                sendMediaButton(getApplicationContext(), KeyEvent.KEYCODE_MEDIA_NEXT);
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("DEBUG", "knocks detected: " + knocksdetected);

                        readTime(trackname, artistname);
                    }
                }, 500);

            }
            else if(knocks >= 8){

                sendMediaButton(getApplicationContext(), KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                sendMediaButton(getApplicationContext(), KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("DEBUG", "knocks detected: " + knocksdetected);

                        readTime(trackname, artistname);
                    }
                }, 500);

            }
            knocksdetected = 0;
        }

    private float abs(float f) {
        if (f<0){
            return -f;
        }
        return f;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("DEBUG","OnCreate");
        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.android.music.playstatechanged");
        iF.addAction("com.android.music.playbackcomplete");
        iF.addAction("com.android.music.queuechanged");

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String cmd = intent.getStringExtra("command");
                Log.d("DEBUG", action + " / " + cmd);
                String artist = intent.getStringExtra("artist");
                String album = intent.getStringExtra("album");
                String track = intent.getStringExtra("track");
                trackname = track;
                artistname = artist;
                Log.d("Music",artist+":"+album+":"+track);
            }
        };
        registerReceiver(mReceiver, iF);

        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });
        t2 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t2.setLanguage(Locale.US);

                }
            }
        });

        mSensorManager = MainActivity.sm;
        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        resumeAccSensing();

    }

    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // String pack = intent.getStringExtra("package");
            title = intent.getStringExtra("title");
            text = intent.getStringExtra("text");
            Log.d("DEBUG",title + " ghg" + text);
            //int id = intent.getIntExtra("icon",0);

            Context remotePackageContext = null;
            try {
//                remotePackageContext = getApplicationContext().createPackageContext(pack, 0);
//                Drawable icon = remotePackageContext.getResources().getDrawable(id);
//                if(icon !=null) {
//                    ((ImageView) findViewById(R.id.imageView)).setBackground(icon);
//                }
                t2.speak("Message from" + " " + title + " saying " + " " + text, TextToSpeech.QUEUE_FLUSH, null);
                //t1.speak("Message from" + " " + title, TextToSpeech.QUEUE_FLUSH, null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAccSensing();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


