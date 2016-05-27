package com.demo.vita.vitamusicplayer;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sjk on 2016/3/19.
 */
public class ActivityCollector {

    private static List<BaseActivity> activities = new ArrayList<BaseActivity>();

    public static void addActivity(BaseActivity activity) {
        activities.add(activity);
    }

    public static void removeActivity(BaseActivity activity) {
        activities.remove(activity);
    }

    /**
     * 退出程序时用，不知道会不会造成memory leak
     */
    public static void finishAll() {
        for (BaseActivity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }
}
