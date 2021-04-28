package com.thecode.emplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.service.controls.actions.CommandAction;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class MusicService extends Service {
    IBinder iBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<Song> songList;
    int position;
    String mSongname;
    String mArtistname;
    long mAlbumId;
    private NotificationPlayer mNotificationPlayer;

    class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: 바인드서비스 구현하기
        return iBinder;
    }

//    //Seekbar를 위한 메소드
//    public int getPosition(){
//        return mediaPlayer.getCurrentPosition();
//    }
//
//    public int getTime(){
//        return mediaPlayer.getDuration();
//    }

    @Override
    public void onCreate() {
        //서비스가 실행될때 단 한번만 호출됨
        mNotificationPlayer = new NotificationPlayer(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("onStartCommand", "Start");
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        songList = (ArrayList<Song>) intent.getSerializableExtra("songs");
        position = intent.getIntExtra("position", 0);
        mSongname = songList.get(position).getmTitle();
        mArtistname = songList.get(position).getmArtist();
        mAlbumId = songList.get(position).getmAlbumId();

        Uri uri = Uri.parse(songList.get(position).getmDataPath());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        //노래가 끝나면 다음노래재생
//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                position = ((position + 1) % songList.size());
//                Uri uri = Uri.parse(songList.get(position).getmDataPath());
//                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
//                mediaPlayer.start();
//            }
//        });
        return super.onStartCommand(intent, flags, startId);
    }
    private void updateNotification(){
        if (mNotificationPlayer != null){
            mNotificationPlayer.updateNotification();
        }
    }

    private void removeNotification(){
        if (mNotificationPlayer != null){
            mNotificationPlayer.removeNotification();
        }
    }
    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }
}