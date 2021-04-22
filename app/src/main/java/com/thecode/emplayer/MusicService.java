package com.thecode.emplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songList;
    private int position;
    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        //서비스가 실행될때 단 한번만 호출됨
        Toast.makeText(this, "Service start", Toast.LENGTH_SHORT).show();
        mediaPlayer.start();
    }


}