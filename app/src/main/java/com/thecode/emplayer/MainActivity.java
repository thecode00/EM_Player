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
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    RecyclerView songListView;

    String[] items;
    ArrayList<Song> songList = new ArrayList<Song>();
    //TODO MediaPlayer 백그라운드에서 사용구현하기
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songListView = (RecyclerView) findViewById(R.id.songlist);

        runtimePermission();

    }

    //권한요청
    public void runtimePermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        //권한을 얻었을때
                        displaySongs();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        //권한을 못얻었을때

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    //노래찾는 메소드
    public ArrayList<File> findSong(File file) {
        ArrayList<File> arrayList = new ArrayList<>();

        File[] files = file.listFiles();

        for (File singleFile : files) {
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                arrayList.addAll(findSong(singleFile));
            } else {
                if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                    arrayList.add(singleFile);
                }
            }
        }
        return arrayList;
    }

    void displaySongs() {
        findSongList();

        //ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        //songListView.setAdapter(myAdapter);
        songListView = (RecyclerView) findViewById(R.id.songlist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        songListView.setLayoutManager(layoutManager);

        CustomAdapter customAdapter = new CustomAdapter();
        songListView.setAdapter(customAdapter);

    }

    class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.Holder> {


        @NonNull
        @Override
        public CustomAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
            Holder holder = new Holder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.Holder holder, int position) {
            holder.tv_songname.setText(songList.get(position).getmTitle().replace(".mp3", "")
                .replace(".wav", ""));

            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("click", Integer.toString(position));
                    Intent intent = new Intent(getApplicationContext(), PlayerActivity.class)
                            .putExtra("songs", songList).putExtra("position", position);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            Log.e("size", Integer.toString(songList.size()));
            return songList.size();
        }

        public class Holder extends RecyclerView.ViewHolder{
            protected ImageView iv_album;
            protected TextView tv_songname;
            public Holder(@NonNull View itemView) {
                super(itemView);
                this.iv_album = (ImageView) itemView.findViewById(R.id.imgsong);
                this.tv_songname = (TextView) itemView.findViewById(R.id.songname);
            }
        }
    }

    private void findSongList(){
        ContentResolver contentResolver = getContentResolver();
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
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, sortOrder);
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
//                Log.e("123",song_title);
//                Log.e("song", song_id + " * " + album_id + " * " + song_title + " * " + song_album + " * " + song_artist + " * " + mDuration + " * " + dataPath);
                songList.add(new Song(song_id, album_id, song_title, song_album, song_artist, mDuration, dataPath));

            } while (cursor.moveToNext());
        }

    }
}