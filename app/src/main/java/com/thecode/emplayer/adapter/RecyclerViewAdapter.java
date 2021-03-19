package com.thecode.emplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.thecode.emplayer.MainActivity;
import com.thecode.emplayer.PlayerActivity;
import com.thecode.emplayer.R;
import com.thecode.emplayer.Song;
import com.thecode.emplayer.SongContract;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<Song> mSongs;
    private Context mContext;

    private String ImageUri = "content://media/external/audio/albumart";

    public RecyclerViewAdapter(ArrayList<Song> mSongs, Context context) {
        this.mSongs = mSongs;
        this.mContext = context;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        LayoutInflater inflater =LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_song_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
            String sName = mSongs.get(position).getmTitle();
            String sArtist = mSongs.get(position).getmArtist();
            long sAlbumId = mSongs.get(position).getmAlbumId();

            //앨범아트 uri를 만든다
            Uri uri = Uri.parse(ImageUri);
            uri.withAppendedPath(uri, Long.toString(sAlbumId));

            try{
                Glide.with(mContext).load(uri)
                        .override(40, 40)
                        .into(holder.album);
            } catch (Exception e){
                Log.wtf("11111111111111111111", "1111111111111111111;");
            }


            //노래정보를 담을 칸에 노래 정보들을 넣는다.
            holder.songname.setText(sName);
            holder.artist.setText(sArtist);
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView album;
        TextView songname;
        TextView artist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //노래의 정보들을 알려주는 뷰에연결결
            album = (ImageView) itemView.findViewById(R.id.album);
            songname = (TextView) itemView.findViewById(R.id.songname);
            artist = (TextView) itemView.findViewById(R.id.artistname);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            try {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(mSongs.get(0).getmDataPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (Exception e){
                e.printStackTrace();
            }


        }
    }
}
