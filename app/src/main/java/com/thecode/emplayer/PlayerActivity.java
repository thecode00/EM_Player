package com.thecode.emplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button btn_previous, btn_playpause, btn_next;
    SeekBar seekBar;
    TextView tv_songname, tv_starttime, tv_endtime;

    private MediaPlayer mediaPlayer;
    private int position;
    private ArrayList<File> mySongs;
    private String sname;

    Thread updateSeekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
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

        //만약 노래가 재생중이라면


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        position = bundle.getInt("position", 0);
        tv_songname.setSelected(true);

        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        tv_songname.setText(sname);

        //노래시작
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        if (mediaPlayer.isPlaying()){
            Log.e("1","1");
        }
        mediaPlayer.start();

        //SeekBar구현
        updateSeekbar = new Thread() {
            @Override
            public void run() {
                int duration = mediaPlayer.getDuration();
                int currentPosition = 0;
                while (currentPosition < duration) {
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.teal_700), PorterDuff.Mode.SRC_IN);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //createTime(mediaPlayer.getDuration());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        tv_endtime.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                tv_starttime.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);

        //일시정지 버튼 구현
        btn_playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //isPlaying으로 현재 재생중인지 알수있다.
                if (mediaPlayer.isPlaying()) {
                    btn_playpause.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                } else {
                    btn_playpause.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });

        //다음노래버튼 구현
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.reset();
                position = ((position + 1) % mySongs.size());
                Uri uriNext = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uriNext);
                sname = mySongs.get(position).getName();
                mediaPlayer.start();
                tv_songname.setText(sname);
                btn_playpause.setBackgroundResource(R.drawable.ic_pause);
            }
        });

        //이전노래버튼 구현
        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.reset();
                if ((position - 1) < 0) {
                    position = mySongs.size() - 1;
                } else {
                    position -= 1;
                }
                Uri uriPrevious = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uriPrevious);
                sname = mySongs.get(position).getName();
                mediaPlayer.start();
                tv_songname.setText(sname);
                btn_playpause.setBackgroundResource(R.drawable.ic_pause);
            }
        });

        //노래가 끝나면 다음노래로 이동
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btn_next.performClick();
            }
        });
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