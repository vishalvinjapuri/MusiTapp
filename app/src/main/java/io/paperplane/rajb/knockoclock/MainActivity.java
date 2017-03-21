package io.paperplane.rajb.knockoclock;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ListView list;
    ArrayList<Model> modelList;
    private static TextToSpeech t1;
    public static SensorManager sm;
    public static AudioManager manager;
    public static Button help;
    public NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView myTextView=(TextView)findViewById(R.id.textView);
        Typeface typeFace= Typeface.createFromAsset(getAssets(),"RobotoCondensed-Light.ttf");
        myTextView.setTypeface(typeFace);

        help = (Button)findViewById(R.id.help);


        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }

    });

    }

    public void runHelp(View v){
        Intent helpIntent = new Intent(MainActivity.this,Help.class);
        startActivity(helpIntent);
    }

    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");
            //int id = intent.getIntExtra("icon",0);

            Context remotePackageContext = null;
            try {
//                remotePackageContext = getApplicationContext().createPackageContext(pack, 0);
//                Drawable icon = remotePackageContext.getResources().getDrawable(id);
//                if(icon !=null) {
//                    ((ImageView) findViewById(R.id.imageView)).setBackground(icon);
//                }
                byte[] byteArray =intent.getByteArrayExtra("icon");
                Bitmap bmp = null;
                if(byteArray !=null) {
                    bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                }
                Model model = new Model();
                model.setName(title +" " +text);
                model.setImage(bmp);
                t1.speak("Message from" + " " + title + " saying " + " " + text, TextToSpeech.QUEUE_FLUSH, null);
                //t1.speak("Message from" + " " + title, TextToSpeech.QUEUE_FLUSH, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void sendMediaButton(Context context, int keyCode) {


        KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        context.sendOrderedBroadcast(intent, null);

        keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        context.sendOrderedBroadcast(intent, null);
    }

    /*public static void readTime(){
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
    }*/

    public void createNotification(){
        Bitmap bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width),
                getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height),
                true);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 01, intent, Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle("MusiTapp Service");
        builder.setContentText("Service is running in the background");
        builder.setSubText("Background Service");
        builder.setNumber(101);
        builder.setContentIntent(pendingIntent);
        builder.setTicker("MusiTapp Service - Running in the background");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(bm);
        builder.setAutoCancel(true);
        builder.setPriority(0);
        builder.setOngoing(true);
        Notification notification = builder.build();
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(01, notification);
    }

    @Override
    public void onPause(){
        super.onPause();
       // dk.stopAccSensing();
    }

    @Override
    public void onResume(){
        super.onResume();
      //  dk.resumeAccSensing();
    }

    public void run(View v){

        sendMediaButton(getApplicationContext(), KeyEvent.KEYCODE_MEDIA_PAUSE);
        Toast.makeText(this,"Media Controls have been granted permission",Toast.LENGTH_SHORT).show();
        createNotification();
        Intent intent = new Intent(this, ListenService.class);
        startService(intent);
    }

    public void stop(View v){
        sendMediaButton(getApplicationContext(), KeyEvent.KEYCODE_MEDIA_PAUSE);
        Toast.makeText(this,"Media Controls have been revoked permission",Toast.LENGTH_SHORT).show();
        notificationManager.cancel(01);
        Intent intent = new Intent(this, ListenService.class);
        stopService(intent);
    }


}




