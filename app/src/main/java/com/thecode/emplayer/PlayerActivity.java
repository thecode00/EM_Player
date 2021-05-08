package com.thecode.emplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    ImageView iv_album;
    Button btn_previous, btn_playpause, btn_next, btn_repeat;
    SeekBar seekBar;
    TextView tv_songname, tv_starttime, tv_endtime, tv_artistname;

    private int position;
    private ArrayList<Song> mySongs;
    //SeekBar구현 변수
    Thread updateSeekbar;
    MusicService musicService;
    boolean isService = false;
    boolean isLoop;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder mb = (MusicService.MyBinder) service;
            musicService = mb.getService();
            isService = true;
            Log.e("Service coneect", "connecta");
            //SeekBar구현
            updateSeekbar = new Thread() {
                @Override
                public void run() {
                    int duration = (int) mySongs.get(position).getmDuration();
                    int currentPosition = 0;
                    while (currentPosition < duration) {
                        try {
                            sleep(500);
                            currentPosition = musicService.getPosition();
                            seekBar.setProgress(currentPosition);
                        } catch (InterruptedException | IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
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
                    if (isService) {
                        musicService.mediaPlayer.seekTo(seekBar.getProgress());
                        String currentTime = createTime(musicService.getPosition());
                        tv_starttime.setText(currentTime);
                    }

                }
            });
            String endTime = createTime(mySongs.get(position).getmDuration());
            tv_endtime.setText(endTime);

            final Handler handler = new Handler();
            final int delay = 1000;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isService) {
                        String currentTime = createTime(musicService.getPosition());
                        tv_starttime.setText(currentTime);
                    }
                    handler.postDelayed(this, delay);
                }
            }, delay);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //노래가 끝났을때 받는 리시버
//            btn_playpause.setBackgroundResource(R.drawable.ic_play);
            String act = intent.getAction();
            if (act == "songend") {
                Log.e("arriveBroad", "comple");
                seekBar.setProgress(0);
                tv_starttime.setText("0:00");
                if (musicService.isLoop) {
                    position = ((position + 1) % mySongs.size());
                }
                settingView();
            } else if (act == "prepared") {
                Log.e("arriveBraod", "prepare");
                Intent musicIntent = new Intent(getApplicationContext(), MusicService.class);
                bindService(musicIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                if (isLoop) {
                    btn_repeat.setBackgroundResource(R.drawable.ic_repeat);
                } else {
                    btn_repeat.setBackgroundResource(R.drawable.ic_norepeat);
                }
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        //노래가 끝났을때
        intentFilter.addAction("songend");
        //노래가 준비되었을때
        intentFilter.addAction("prepared");
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void unregisterBroadcast() {
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //노래가끝나면 브로드캨스트를 받음
        registerBroadcast();
        iv_album = (ImageView) findViewById(R.id.iv_playeralbum);

        btn_previous = (Button) findViewById(R.id.previous_button);
        btn_playpause = (Button) findViewById(R.id.play_pause_button);
        btn_next = (Button) findViewById(R.id.next_button);
        btn_repeat = (Button) findViewById(R.id.repeat_button);

        seekBar = (SeekBar) findViewById(R.id.SeekBar);

        tv_songname = (TextView) findViewById(R.id.song_name_textview);
        tv_starttime = (TextView) findViewById(R.id.starttime);
        tv_endtime = (TextView) findViewById(R.id.endtime);
        tv_artistname = (TextView) findViewById(R.id.artistname);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        position = bundle.getInt("position", 0);
        Log.e("position", Integer.toString(position));
        tv_songname.setSelected(true);

        //musicservice시작
        startIntent();
        //view정보 바인딩
        settingView();


        SharedPreferences sharedPreferences = getSharedPreferences("loop", MODE_PRIVATE);
        isLoop = sharedPreferences.getBoolean("isLoop", false);
        Log.e("isLoop", Boolean.toString(isLoop));

        Intent musicIntent = new Intent(getApplicationContext(), MusicService.class);


        //일시정지 버튼 구현
        btn_playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("btn_play", "play");
                Log.e("isservice", Boolean.toString(isService));
                if (isService) {
                    Log.e("seekbatpro", Integer.toString(seekBar.getProgress()));
                    musicService.play(position, seekBar.getProgress());
                    btnChange();
                } else {
                    startIntent();
                }

            }
        });

        //다음노래버튼 구현
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = ((position + 1) % mySongs.size());
                startIntent();
                settingView();
                btn_playpause.setBackgroundResource(R.drawable.ic_pause);
                bindService(musicIntent, serviceConnection, Context.BIND_AUTO_CREATE);
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
                startIntent();
                settingView();
                btn_playpause.setBackgroundResource(R.drawable.ic_pause);
                bindService(musicIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            }
        });

        btn_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("btnchange", Boolean.toString(isLoop));
                setBtn_repeat();
            }
        });
    }

    public String createTime(long duration) {
        String time = "";
        long min = duration / 1000 / 60;
        long sec = duration / 1000 % 60;

        time += min + ":";

        if (sec < 10) {
            time += "0";
        }
        time += sec;
        return time;
    }

    private void startIntent() {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.putExtra("songs", mySongs).putExtra("position", position);
        startService(intent);
    }

    private void settingView() {
        Log.e("SettingView", "set");
        Uri u = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(u, mySongs.get(position).getmAlbumId());
        Glide.with(this)
                .asBitmap()
                .error(R.drawable.ic_baseline_music_note_24)
                .load(uri)
                .into(iv_album);
        tv_songname.setText(mySongs.get(position).getmTitle());
        tv_artistname.setText(mySongs.get(position).getmArtist());
        tv_starttime.setText("0:00");
        String endTime = createTime(mySongs.get(position).getmDuration());
        tv_endtime.setText(endTime);
        seekBar.setMax((int) mySongs.get(position).getmDuration());
        seekBar.setProgress(0);
    }

    @Override
    protected void onStop() {
        editShare();
        super.onStop();
    }

    //버튼리소스변경
    private void btnChange() {
        if (musicService.isPlaying()) {
            btn_playpause.setBackgroundResource(R.drawable.ic_pause);
        } else {
            btn_playpause.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private void setBtn_repeat() {
        Log.e("setBtn_repeat", Boolean.toString(isLoop));
        if (isLoop) {
            btn_repeat.setBackgroundResource(R.drawable.ic_norepeat);
            isLoop = false;
        } else {
            btn_repeat.setBackgroundResource(R.drawable.ic_repeat);
            isLoop = true;
        }
        Log.e("setBtn_repeat_end", Boolean.toString(isLoop));
        editShare();
    }

    private void editShare() {
        Log.e("Editshare", "edit");
        SharedPreferences sharedPreferences = getSharedPreferences("loop", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoop", isLoop);
        editor.commit();
    }

}