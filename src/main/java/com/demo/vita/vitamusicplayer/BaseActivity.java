package com.demo.vita.vitamusicplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by sjk on 2016/3/19.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(BaseActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(BaseActivity.this);
    }
}
