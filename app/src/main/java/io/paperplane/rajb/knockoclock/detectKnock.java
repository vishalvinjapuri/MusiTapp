package io.paperplane.rajb.knockoclock;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

public class detectKnock implements SensorEventListener{

    public volatile boolean spikeDetected = false;
    private SensorManager mSensorManager;


    private static TextToSpeech t1,t2;

    //Optimization parameters accelerometer
    final public float thresholdZ = 3; //Force needed to trigger event, G = 9.81 methinks
    final public float threshholdX = 6;
    final public float threshholdY = 6;
    final public int updateFrequency = 100;
    private Context mContext;

    //For high pass filter
    private float prevZVal = 0;
    private float currentZVal = 0;
    private float diffZ = 0;

    private float prevXVal = 0;
    private float currentXVal = 0;
    private float diffX = 0;

    private float prevYVal = 0;
    private float currentYVal = 0;
    private float diffY = 0;


    public detectKnock(SensorManager sm, Context c){
        mSensorManager = sm;

        t1=new TextToSpeech(c, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });
        t2=new TextToSpeech(c, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t2.setLanguage(Locale.US);
                }
            }
        });
    }

    public static void readTime(){
        //Toast.makeText(getApplicationContext(), "handled!" ,Toast.LENGTH_LONG).show();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        Log.d("DEBUG", hour + ":" + minute);
        String toSpeak;
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
            toSpeak = "The time is " + hour + " " + minute + " " + ampm;
        }
        //Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }
    public static void readWeather(){
        String info;
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
        if (currentZVal > prevZVal && diffZ > thresholdZ && diffX < threshholdX && diffY < threshholdY){
            accTapEvent();
        }

    }

    private void accTapEvent(){
        Log.d("acceltap","single tap event detected!");
        spikeDetected = true;
        readTime();
        readWeather();
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
}
