package com.thecode.emplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class LicenseActivity extends AppCompatActivity {
    TextView tv_license;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        tv_license = (TextView) findViewById(R.id.tv_license);
        tv_license.append("Icon\n");
        tv_license.append("https://icon-icons.com/icon/multimeda-play/149250\n");
        tv_license.append("https://icon-icons.com/icon/audio-music-note-player-radio-song-sound/122513\n");
    }
}