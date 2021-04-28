package com.thecode.emplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button btn_previous, btn_playpause, btn_next;
    SeekBar seekBar;
    TextView tv_songname, tv_starttime, tv_endtime, tv_artistname;

    private int position;
    private ArrayList<Song> mySongs;
    private String sname;
//    MediaPlayer mediaPlayer;

    //SeekBar구현 변수
    Thread updateSeekbar;
    MusicService musicService;
    boolean isService = false;


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);


        btn_previous = (Button) findViewById(R.id.previous_button);
        btn_playpause = (Button) findViewById(R.id.play_pause_button);
        btn_next = (Button) findViewById(R.id.next_button);

        seekBar = (SeekBar) findViewById(R.id.SeekBar);

        tv_songname = (TextView) findViewById(R.id.song_name_textview);
        tv_starttime = (TextView) findViewById(R.id.starttime);
        tv_endtime = (TextView) findViewById(R.id.endtime);
        tv_artistname = (TextView) findViewById(R.id.artistname);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        position = bundle.getInt("position", 0);
        tv_songname.setSelected(true);

        //musicservice시작
        Intent musicIntent = new Intent(getApplicationContext(), MusicService.class);
        musicIntent.putExtra("songs", mySongs).putExtra("position", position);
        startService(musicIntent);

        sname = mySongs.get(position).getmTitle();
        tv_songname.setText(sname);
        tv_artistname.setText(mySongs.get(position).getmArtist());
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MyBinder mb = (MusicService.MyBinder) service;
                musicService = mb.getService();
                isService = true;
                Toast.makeText(PlayerActivity.this, "11", Toast.LENGTH_SHORT).show();
                Log.e("12312313", "1412414141");
                //SeekBar구현
                updateSeekbar = new Thread() {
                    @Override
                    public void run() {
                        int duration = musicService.mediaPlayer.getDuration();
                        int currentPosition = 0;
                        while (currentPosition < duration) {
                            try {
                                sleep(500);
                                currentPosition = musicService.mediaPlayer.getCurrentPosition();
                                seekBar.setProgress(currentPosition);
                            } catch (InterruptedException | IllegalStateException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                tv_artistname.setText(mySongs.get(position).getmArtist());
                seekBar.setMax(musicService.mediaPlayer.getDuration());
                updateSeekbar.start();
                seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.MULTIPLY);
                seekBar.getThumb().setColorFilter(getResources().getColor(R.color.teal_700), PorterDuff.Mode.SRC_IN);

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //TODO
                        musicService.mediaPlayer.seekTo(seekBar.getProgress());
                        String currentTime = createTime(musicService.mediaPlayer.getCurrentPosition());
                        tv_starttime.setText(currentTime);
                    }
                });
                String endTime = createTime(musicService.mediaPlayer.getDuration());
                tv_endtime.setText(endTime);

                final Handler handler = new Handler();
                final int delay = 1000;

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String currentTime = createTime(musicService.mediaPlayer.getCurrentPosition());
                        tv_starttime.setText(currentTime);
                        handler.postDelayed(this, delay);
                    }
                }, delay);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isService = false;
            }
        };
        bindService(musicIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        //노래시작
//        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
//        if (mediaPlayer.isPlaying()){
//            Log.e("1","1");
//        }
//        mediaPlayer.start();


        //일시정지 버튼 구현
        btn_playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //isPlaying으로 현재 재생중인지 알수있다.
                if (musicService.mediaPlayer.isPlaying()) {
                    btn_playpause.setBackgroundResource(R.drawable.ic_play);
                    musicService.mediaPlayer.pause();
                } else {
                    btn_playpause.setBackgroundResource(R.drawable.ic_pause);
                    musicService.mediaPlayer.start();
                }
            }
        });

        //다음노래버튼 구현
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindService(serviceConnection);
                position = ((position + 1) % mySongs.size());
                Log.e("positon", Integer.toString(position));
                Intent nextIntent = new Intent(getApplicationContext(), MusicService.class);
                nextIntent.putExtra("songs", mySongs).putExtra("position", position);
                startService(nextIntent);
                sname = mySongs.get(position).getmTitle();
                tv_songname.setText(sname);
                btn_playpause.setBackgroundResource(R.drawable.ic_pause);
                tv_starttime.setText("0:00");
                bindService(musicIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                String endTime = createTime(musicService.mediaPlayer.getDuration());
                Log.e("endtime", Integer.toString(musicService.mediaPlayer.getDuration()));
                tv_endtime.setText(endTime);

            }
        });

        //이전노래버튼 구현
        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((position - 1) < 0) {
                    position = mySongs.size() - 1;
                } else {
                    position -= 1;
                }
                unbindService(serviceConnection);
                Log.e("positon", Integer.toString(position));
                Intent nextIntent = new Intent(getApplicationContext(), MusicService.class);
                nextIntent.putExtra("songs", mySongs).putExtra("position", position);
                startService(nextIntent);
                sname = mySongs.get(position).getmTitle();
                tv_songname.setText(sname);
                btn_playpause.setBackgroundResource(R.drawable.ic_pause);
                tv_starttime.setText("0:00");
                bindService(musicIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                String endTime = createTime(musicService.mediaPlayer.getDuration());
                Log.e("endtime", Integer.toString(musicService.mediaPlayer.getDuration()));
                tv_endtime.setText(endTime);

            }
        });

        //노래가 끝나면 다음노래로 이동
    }

    public String createTime(int duration) {
        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        time += min + ":";

        if (sec < 10) {
            time += "0";
        }
        time += sec;
        return time;
    }
}