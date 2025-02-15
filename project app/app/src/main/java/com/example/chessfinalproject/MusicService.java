package com.example.chessfinalproject;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    private static final String TAG = "MusicService";

    private MediaPlayer mediaPlayer;
    private boolean isPaused = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.dragonforce);
        mediaPlayer.setOnCompletionListener(this);
        // set the music to loop continuously
        mediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mediaPlayer.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // handle music completion event
    }

    //pause or resume the music
    public void pauseResume() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
        } else {
            mediaPlayer.start();
            isPaused = false;
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    //change the music
    public void setMusicSource(int resId) {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        isPaused = false;
    }


    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}

