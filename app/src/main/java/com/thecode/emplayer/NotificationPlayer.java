package com.thecode.emplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.service.controls.actions.CommandAction;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.squareup.picasso.Picasso;

//TODO notification 아이콘설정하기
public class NotificationPlayer {
    private final int Notification_ID = 222;
    private MusicService mService;
    private NotificationManager notificationManager;
    private NotificationManagerBuilder notificationManagerBuilder;
    private Boolean isForeground;

    public NotificationPlayer(MusicService service) {
        mService = service;
        notificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void updateNotification() {
        cancel();
        notificationManagerBuilder = new NotificationManagerBuilder();
        notificationManagerBuilder.execute();
    }

    public void removeNotification(){
        cancel();
        mService.stopForeground(true);
        isForeground = false;
    }
    private void cancel() {
        if (notificationManagerBuilder != null){
            notificationManagerBuilder.cancel(true);
            notificationManagerBuilder = null;
        }
    }

    private class NotificationManagerBuilder extends AsyncTask<Void, Void, Notification> {
        private RemoteViews remoteViews;
        private NotificationCompat.Builder mNotificationBuilder;
        private PendingIntent pendingIntent;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Intent main = new Intent(mService, MainActivity.class);
            pendingIntent = PendingIntent.getActivity(mService, 0, main, 0);
            remoteViews = createRemoteViews(R.layout.notification);
            mNotificationBuilder = new NotificationCompat.Builder(mService);
            mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(true).setContentIntent(pendingIntent).setContent(remoteViews);
            Notification notification = mNotificationBuilder.build();
            notification.priority = Notification.PRIORITY_MAX;
            notification.contentIntent = pendingIntent;
            if (!isForeground) {
                isForeground = true;
                mService.startForeground(Notification_ID, notification);
            }
        }

        @Override
        protected Notification doInBackground(Void... voids) {
            mNotificationBuilder.setContent(remoteViews);
            mNotificationBuilder.setContentIntent(pendingIntent);
            mNotificationBuilder.setPriority(Notification.PRIORITY_MAX);
            Notification notification = mNotificationBuilder.build();
            updateRemote(remoteViews, notification);
            return notification;
        }

        private RemoteViews createRemoteViews(int id){
            RemoteViews remote = new RemoteViews(mService.getPackageName(), id);
            Intent previousAction = new Intent("previous");
            Intent playpauseAction = new Intent("playpause");
            Intent nextAction = new Intent("next");
            Intent closeAction = new Intent("close");
            PendingIntent previousPedding = PendingIntent.getService(mService, 0, previousAction, 0);
            PendingIntent playpausePedding = PendingIntent.getService(mService, 0, playpauseAction, 0);
            PendingIntent nextPedding = PendingIntent.getService(mService, 0, nextAction, 0);
            PendingIntent closePedding = PendingIntent.getService(mService, 0, closeAction, 0);

            remote.setOnClickPendingIntent(R.id.nt_previous, previousPedding);
            remote.setOnClickPendingIntent(R.id.nt_startpause, playpausePedding);
            remote.setOnClickPendingIntent(R.id.nt_next, nextPedding);
            remote.setOnClickPendingIntent(R.id.nt_close, closePedding);
            return remote;
        }

        private void updateRemote(RemoteViews remoteViews, Notification notification){
            if (mService.isPlaying()){
                remoteViews.setImageViewResource(R.id.nt_startpause, R.drawable.ic_ntpause);
            } else {
                remoteViews.setImageViewResource(R.id.nt_startpause, R.drawable.ic_ntplay);
            }
            String title = mService.mSongname;
            String artist = mService.mArtistname;
            remoteViews.setTextViewText(R.id.tv_ntartistname, artist);
            remoteViews.setTextViewText(R.id.tv_ntsongname, title);
            Uri albumart = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), mService.mAlbumId);
            Picasso.get().load(albumart).into(remoteViews, R.id.nt_image, Notification_ID, notification);
        }
    }
}
