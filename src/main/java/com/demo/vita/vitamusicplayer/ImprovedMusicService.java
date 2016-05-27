package com.demo.vita.vitamusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.demo.vita.utils.Constant;

import java.io.IOException;

/**
 * Created by sjk on 2016/3/29.
 */
public class ImprovedMusicService extends Service {

    public static final String TAG = "ImprovedMusicService";
    private MyBinder binder = new MyBinder();   // key point!
    private MediaPlayer mediaPlayer;
    Notification.Builder builder;   // 前台服务。问题：怎么根据播放的歌曲，随时改变标题和内容？
    Context context;
    private PreparedCallback preparedCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this; // 获取上下文，为了以后的操作更方便
        mediaPlayer = new MediaPlayer();

        // 设置一首歌播放完成之后的动作
        // 因为可能要寻找下一首歌继续播放，所以暂时以广播的形式来处理
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 发送一个广播，告诉FragmentMain，"歌曲放完啦~"
                MyLogger.log("回调onCompletion(), 进入PlaybackCompleted状态");
                if (mediaPlayer.isLooping()) return;
                Intent intent = new Intent(Constant.BROADCAST_COMPLETE);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                MyLogger.log("刚刚发了广播");
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                MyLogger.log("onError()!Params:(what, extra)=(" + what + "," + extra + ")");
                return false;
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();

                // 回调,等到player就绪完毕后,才把执行权交给main碎片
                if (preparedCallback != null) {
                    preparedCallback.afterPrepare(mp.getDuration());
                }
            }
        });

        //Notification notification = new Notification(R.mipmap.ic_launcher, null, System.currentTimeMillis());
        RemoteViews removeView = new RemoteViews(getPackageName(), R.layout.remote_player);
        Intent intent = new Intent(this, FirstActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder = new Notification.Builder(this)
                .setTicker("音乐启动ing")
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setContent(removeView);
        Notification notification = builder.build();
        //startForeground(1, notification);    // 启动前台服务，提高交互同时也在一定程度上避免被kill
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(Constant.REMOTE_NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;   // 保证service不被系统kill?
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLogger.log(TAG + " onDestroy!");
        if (mediaPlayer != null) {
            // 释放播放器资源
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }



    /**
     *  package-private，在同一个包内可访问该类
     *  Binder，用来操控MediaPlayer
     *  定义了各种public操作，供Fragment来调用
     *
     * */
    class MyBinder extends Binder {

        /** 播放某一首、上一首、下一首歌曲
        * 注意这里的播放函数只负责和MediaPlayer有关的逻辑
        * 如果是一些控件比如进度条的逻辑，需要在fragment里再进一步封装函数 **/
        public void playMusicAt(String dataSource) {
            try {
                mediaPlayer.reset();    // Q: reset()究竟在什么时候需要写？MediaPlayer要重新设置路径的时候？
                mediaPlayer.setDataSource(dataSource);  // 然后再播放新来的歌曲
                mediaPlayer.prepareAsync();
                MyLogger.log("prepareAsync");
            } catch (Exception e) {
                MyLogger.log("captured----" + e.getMessage());
            }
        }

        public void setOnPreparedCallback(PreparedCallback pc) {
            preparedCallback = pc;
        }

        // 暂停
        public void pauseMusic() {
            if (mediaPlayer.isPlaying())
                mediaPlayer.pause();
        }

        // 继续
        public void resumeMusic() {
            if (!mediaPlayer.isPlaying())
                mediaPlayer.start();
        }

        // 停止播放
        public void stopMusic() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }

        // 改变歌曲播放进度
        public void seekTo(int msec) {
            mediaPlayer.seekTo(msec);
        }

        // 取得歌曲进度
        public int getProgress() {
            return mediaPlayer.getCurrentPosition();
        }

        // 是否在播放
        public boolean isMusicPlaying() {
            return mediaPlayer.isPlaying();
        }

        // 是否是单曲循环模式
        public boolean isMusicLooping() {
            return mediaPlayer.isLooping();
        }

        // 设置是否单曲循环
        public void setMusicLooping(boolean isLooping) {
            mediaPlayer.setLooping(isLooping);
        }
    }
}
