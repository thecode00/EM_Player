package com.thecode.emplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerActivity extends AppCompatActivity {

    @BindView(R.id.SeekBar) SeekBar seekBar;
    @BindView(R.id.next_button) Button btn_next;
    @BindView(R.id.previous_button) Button btn_previous;
    @BindView(R.id.play_pause_button) Button btn_play_pause;
    @BindView(R.id.song_name_textview) TextView songname_textview;

    static MediaPlayer mMediaPlayer;
    int positon;
    ArrayList<File> mSongs;
    Thread updateSeekBar;
    String songName;

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

        ButterKnife.bind(this);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        updateSeekBar = new Thread(){
            @Override
            public void run() {
                int totalTime = mMediaPlayer.getDuration();
                int currentTime = 0;

                //TODO Seekbar 구현방법 찾아보기
                while (currentTime < totalTime){
                    try {
                        sleep(250);
                        currentTime = mMediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentTime);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        //노래가 끝난다음 뮤직플레이어를 멈춘다.
        if (mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mSongs = (ArrayList)bundle.getParcelableArrayList("song");
        songName = mSongs.get(positon).getName().toString();

        String song_name = intent.getStringExtra("songname");
        songname_textview.setText(song_name);
        //TODO setSelected뭔지 찾아보기
        songname_textview.setSelected(true);

        positon = bundle.getInt("position", 0);

        Uri uri = Uri.parse(mSongs.get(positon).toString());

        mMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mMediaPlayer.start();
        seekBar.setMax(mMediaPlayer.getDuration());

        //TODO Seekbar 디자인 관련부분 고쳐야함.
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.design_default_color_primary),
                PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.design_default_color_primary),
                PorterDuff.Mode.SRC_IN);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        btn_play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setMax(mMediaPlayer.getDuration());

                if (mMediaPlayer.isPlaying()){
                    //노래를 멈출때
                    btn_play_pause.setBackgroundResource(R.drawable.ic_play);
                    mMediaPlayer.pause();
                } else {
                    //노래를 다시 시작할때
                    btn_play_pause.setBackgroundResource(R.drawable.ic_pause);
                    mMediaPlayer.start();
                }
            }
        });

        //다음노래로 바꿀때
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //먼저 재생되고있는 노래를 멈춘다.
                mMediaPlayer.stop();
                mMediaPlayer.release();

                if (positon < mSongs.size() - 1){
                    positon += 1;
                } else {
                    positon = 0;
                }

                Uri u = Uri.parse(mSongs.get(positon).toString());
                mMediaPlayer = MediaPlayer.create(getApplicationContext(), u);

                //노래이름을 바꾼다.
                songName = mSongs.get(positon).getName().toString();
                songname_textview.setText(songName);

                mMediaPlayer.start();
            }
        });

        //이전노래로 바꿀때
        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //먼저 재생되고있는 노래를 멈춘다.
                mMediaPlayer.stop();
                mMediaPlayer.release();

                //만약 position에서 1을뺀 결과가 0보다 낮다면 Index에러가 발생하기때문에 맨 마지막 Index로 초기화한다.
                if (positon - 1 < 0){
                    positon = mSongs.size() - 1;
                } else {
                    positon -= 1;
                }

                Uri u = Uri.parse(mSongs.get(positon).toString());
                mMediaPlayer = MediaPlayer.create(getApplicationContext(), u);

                //노래이름을 바꾼다.
                songName = mSongs.get(positon).getName().toString();
                songname_textview.setText(songName);

                mMediaPlayer.start();
            }
        });


    }
}