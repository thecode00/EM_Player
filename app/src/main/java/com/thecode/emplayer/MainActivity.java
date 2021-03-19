package com.thecode.emplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.thecode.emplayer.adapter.RecyclerViewAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity{

    @BindView(R.id.songlist) RecyclerView songListView;

    private ArrayList<Song> songList;
    private RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        songList = new ArrayList<Song>();

        //권한확인용
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED){
            Log.e("권한확익", "권한있음");
        }
        //노래들을 찾는다.
        findSongList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        songListView.setLayoutManager(layoutManager);
        //TODO 사용방법 알아보기
        //songListView.setHasFixedSize(true);

        mAdapter = new RecyclerViewAdapter(songList, this);
        songListView.setAdapter(mAdapter);

    }

    //노래를 찾을때 사용
    private void findSongList(){
        ContentResolver contentResolver = getContentResolver();

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        //아스키코드순으로 정렬
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };

        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, sortOrder);
        cursor.moveToFirst();
        Log.d("노래갯수", Integer.toString(cursor.getCount()));



        //각 노래의 정보를 Song객체에 담아 리스트에 저장한다.
        //while이 먼저 안오고 do가 먼저오는 이유는 cursor의 갯수가 1개일때 cursor.moveToNext가 false가 되기때문
        //TODO albumart 부분 해결하기
        if (cursor != null && cursor.getCount() > 0){
            do {
                long song_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                long album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String song_title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String song_album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String song_artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                long mDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String dataPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                Log.e("123",song_title);
                songList.add(new Song(song_id, album_id, song_title, song_album, song_artist, mDuration, dataPath));

            } while (cursor.moveToNext());
        }

    }


}