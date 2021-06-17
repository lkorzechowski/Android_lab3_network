package com.orzechowski.lab3;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class NewIntentService extends IntentService {

    private static final String ACT = "com.orzechowski.lab3.action.zadanie1";
    private static final String PAR = "com.orzechowski.lab3.extra.parametr1";
    public static final String ALERT = "com.orzechowski.lab3.intent_service.receiver";
    private static final int ID = 1;
    private NotificationManager mNotificationManager;
    private int mProgress = 0;
    private int mTotalDownload = 0;

    public NewIntentService(){
        super("NewIntentService");
    }

    public static void launchService(Context context, String stringUrl){
        Intent intent = new Intent(context, NewIntentService.class);
        intent.setAction(ACT);
        intent.putExtra(PAR, stringUrl);
        context.startService(intent);
    }

    private void prepareNotificationChannel(){
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        CharSequence name = getString(R.string.app_name);
        NotificationChannel channel = new NotificationChannel("123456789", name, NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.createNotificationChannel(channel);
    }

    private Notification createNotification(){
        Intent notificationIntent = new Intent(ALERT);
        notificationIntent.putExtra("download", mTotalDownload);
        LocalBroadcastManager.getInstance(this).sendBroadcast(notificationIntent);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.i("Download progress", String.valueOf(mProgress));
        Notification.Builder notificationBuilder = new Notification.Builder(this, "123456789");
        notificationBuilder
                .setContentTitle("Downloading")
                .setProgress(100, mProgress, false)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setWhen(System.currentTimeMillis());

        notificationBuilder.setOngoing(mProgress < 100);
        return notificationBuilder.build();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        prepareNotificationChannel();
        startForeground(ID, createNotification());

        if(intent != null){
            final String action = intent.getAction();
            if(ACT.equals(action)){
                final String param1 = intent.getStringExtra(PAR);
                performTask(param1);
            } else {
                Log.e("intent_service", "nieznana_akcja");
            }
        }
    }

    private void performTask(String stringUrl){
        HttpURLConnection connection = null;
        try{
            URL url = new URL(stringUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.connect();
            int fileSize = connection.getContentLength();
            byte[] buffer = new byte[100];


            DataInputStream dataInputStream = new DataInputStream(connection.getInputStream());


            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File file = new File(cw.getDir(
                    Environment.DIRECTORY_DOWNLOADS, Context.MODE_PRIVATE)
                            + File.separator
                            + new File(url.getFile()).getName());
            Log.w("HERE", file.getAbsolutePath());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int downloaded = dataInputStream.read(buffer, 0, 100);
            while (downloaded != -1) {
                fileOutputStream.write(buffer, 0, downloaded);
                mTotalDownload += downloaded;
                mProgress += downloaded * 100/ fileSize;
                downloaded = dataInputStream.read(buffer, 0, 100);
                mNotificationManager.notify(ID, createNotification());
            } if(fileOutputStream.getFD()!=null) {
                Log.i("Download", "completed");
                mTotalDownload = fileSize;
                mProgress = 100;
                mNotificationManager.notify(ID, createNotification());
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            dataInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
