package com.thecode.emplayer;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.service.controls.actions.CommandAction;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class MusicService extends Service{
    IBinder iBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<Song> songList;
    boolean isLoop = false;
    private Song mSong;
    private boolean isPrepared = false;
    int position;
    String mSongname;
    String mArtistname;
    long mAlbumId;

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

    @Override
    public void onCreate() {
        //서비스가 실행될때 단 한번만 호출됨
        Log.e("oncreate", "start");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;
                mediaPlayer.start();
                Log.e("onprepare","ppp");
                sendBroadcast(new Intent("prepared"));
            }
        });
        //노래가 끝났을때
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.reset();
                isPrepared = false;
                Log.e("oncomple","comple");
                SharedPreferences sharedPreferences = getSharedPreferences("loop", MODE_PRIVATE);
                isLoop = sharedPreferences.getBoolean("isLoop", false);
                //TODO 반복재생일때구현
                if (isLoop){
                    position = ((position + 1) % songList.size());
                }
                prepare();
                sendBroadcast(new Intent("songend"));
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                isPrepared = false;
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
//        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
//            @Override
//            public void onSeekComplete(MediaPlayer mp) {
//
//            }
//        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("onStartCommand", "Start");
        if (mediaPlayer != null) {
            Log.e("Reset", "Start");
            isPrepared = false;
            mediaPlayer.reset();
        }
        songList = (ArrayList<Song>) intent.getSerializableExtra("songs");
        position = intent.getIntExtra("position", 0);
        mSongname = songList.get(position).getmTitle();
        mArtistname = songList.get(position).getmArtist();
        mAlbumId = songList.get(position).getmAlbumId();

        prepare();
        return super.onStartCommand(intent, flags, startId);
    }
    public boolean isPlaying(){
        if (mediaPlayer != null){
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    private void prepare(){
        try {
            mediaPlayer.setDataSource(songList.get(position).getmDataPath());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void play(int position, int curSeek){
        mSong = songList.get(position);
        if (mediaPlayer != null){
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    Log.e("play", "pp");
                    mediaPlayer.seekTo(curSeek);
                    mediaPlayer.start();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public int getPosition(){
        if (isPrepared){
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("onunbind", "unbind");
        return super.onUnbind(intent);
    }

    public void setLoop(){
        if (isLoop){
            isLoop = false;
        } else {
            isLoop = true;
        }
    }
}