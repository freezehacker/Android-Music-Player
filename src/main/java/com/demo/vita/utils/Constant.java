package com.demo.vita.utils;

/**
 * Created by sjk on 2016/3/21.
 */
public class Constant {

    // 定义几个String常亮，目的是让服务发送广播到碎片（或其所在活动）中，意图改变播放器的view，比如滚动条前进
    public static final String BROADCAST_SEEKBAR_CHANGE = "com.demo.vita.change_seek_bar";

    public static final String BROADCAST_PLAY_BTN_CHANGE = "com.demo.vita.change_play_btn";

    public static final String BROADCAST_SONGLIST_SELECTED = "com.demo.vita.change_songlist_selected";

    public static final String BROADCAST_AUTO_NEXT = "com.demo.vita.auto_next";

    public static final String BROADCAST_COMPLETE = "com.demo.vita.complete_music";

    // remote view
    public static final int REMOTE_NOTIFICATION_ID = 1;

}
