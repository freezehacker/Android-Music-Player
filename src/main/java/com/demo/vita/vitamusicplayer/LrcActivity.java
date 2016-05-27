package com.demo.vita.vitamusicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sjk on 2016/4/17.
 */
public class LrcActivity extends AppCompatActivity {

    LrcView lrcView;
    List<LyricsBean> lrcList;
    TextView tv_header;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_lrc);

        initViews();
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
    }

    private void showLrcFile(File file) {
        if (file.isFile()) {
            if (file.getName().toLowerCase().contains(".lrc")) {
                MyLogger.log(file.getAbsolutePath());
            }
        } else if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                showLrcFile(f);
            }
        }
    }

    private void initViews() {
        tv_header = (TextView) findViewById(R.id.lrc_header);
        lrcView = (LrcView) findViewById(R.id.lrcview);
    }
}
