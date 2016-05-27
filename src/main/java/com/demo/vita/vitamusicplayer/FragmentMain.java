package com.demo.vita.vitamusicplayer;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.vita.utils.Constant;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sjk on 2016/3/29.
 */
public class FragmentMain extends Fragment {

    // 定义3种播放模式：顺序、单曲循环、随机
    public static final int MODE_ORDER = 0x13, MODE_LOOP = 0x14, MODE_RANDOM = 0x15;
    int mode;
    int curSong;
    SharedPreferences sharedPreferences;    // 定制用户的“上一次播放的歌的序号”和“播放模式”
    public static final String PREF_NAME = "my_pref_v1";

    // 一开始进去的时候是true状态，表示没有在播放。一旦播放了，这一次打开应用就一直是false
    boolean isMusicStoping = true;

    RecyclerView rv;
    MyRecyclerViewAdapter adapter;
    List<MusicBean> musicList;

    Button btn_next, btn_last, btn_play;

    public SeekBar seekBar;
    TextView tv_title, tv_artist;
    ImageView iv_cover;

    PlayerViewChangeReceiver mReceiver;

    //--------------------------------------------------
    ImprovedMusicService.MyBinder binder;
    Runnable runnable;
    Handler handler = new Handler();
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 类似于获取了static对象那样获取了service的binder
            // 接下来就可以随意操纵MediaPlayer了
            binder = (ImprovedMusicService.MyBinder)service;

            binder.setOnPreparedCallback(new PreparedCallback() {
                @Override
                public void afterPrepare(int duration) {
                    seekBar.setMax(duration);
                    handler.removeCallbacksAndMessages(null);   // remove all to release memory!
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(binder.getProgress());
                            handler.postDelayed(runnable, 1000);
                        }
                    };
                    handler.postDelayed(runnable, 0);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 恢复用户数据（如果不是第一次使用）
        restoreUserData();
        isMusicStoping = true;

        // 添加service绑定
        Intent intentToBindService = new Intent(getActivity(), ImprovedMusicService.class);
        getActivity().bindService(intentToBindService, serviceConnection, Context.BIND_AUTO_CREATE);

        // 注册local广播
        if (mReceiver == null) {
            mReceiver = new PlayerViewChangeReceiver();
        }
        IntentFilter filter = new IntentFilter();
        //filter.addAction(Constant.BROADCAST_AUTO_NEXT);
        //filter.addAction(Constant.BROADCAST_PLAY_BTN_CHANGE);
        //filter.addAction(Constant.BROADCAST_SEEKBAR_CHANGE);
        //filter.addAction(Constant.BROADCAST_SONGLIST_SELECTED);
        filter.addAction(Constant.BROADCAST_COMPLETE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销local广播
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        // 取消service绑定
        getActivity().unbindService(serviceConnection);
        // 保存用户数据
        saveUserData();
    }

    public void setMode(int _mode) {
        mode = _mode;
        if (mode == MODE_LOOP) {
            binder.setMusicLooping(true);
        } else {
            binder.setMusicLooping(false);
        }
    }

    /* 保存用户的定制化数据，比如听到了哪一首歌，保存起来，下次继续。用在onDestroy() */
    public void saveUserData() {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt("curSong", curSong);
        editor.putInt("mode", mode);
        editor.commit();
    }

    /* 恢复数据，用在onCreate() */
    public void restoreUserData() {
        SharedPreferences pref = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mode = pref.getInt("mode", MODE_ORDER);
        curSong = pref.getInt("curSong", 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_main, container, false);
        rv = (RecyclerView) ret.findViewById(R.id.recycler_view);

        LinearLayout jumpToLrc = (LinearLayout) ret.findViewById(R.id.jumpToLrc);
        jumpToLrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyLogger.log("click on the layout, jump to lrc activity");
                Intent intent = new Intent(getActivity(), LrcActivity.class);
                intent.putExtra("path", musicList.get(curSong).getUrl());
                startActivity(intent);
            }
        });

        // 播放器view
        tv_artist = (TextView) ret.findViewById(R.id.cur_song_artist);
        tv_title = (TextView) ret.findViewById(R.id.cur_song_title);
        iv_cover = (ImageView) ret.findViewById(R.id.cur_album_pic);
        seekBar = (SeekBar) ret.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new MySeekBarListener());
        btn_play = (Button) ret.findViewById(R.id.btn_play_or_pause);
        btn_last = (Button) ret.findViewById(R.id.btn_last);
        btn_next = (Button) ret.findViewById(R.id.btn_next);
        MyClickListener listener = new MyClickListener();
        btn_play.setOnClickListener(listener);
        btn_last.setOnClickListener(listener);
        btn_next.setOnClickListener(listener);


        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);

        rv.setItemAnimator(new DefaultItemAnimator());

        // 以ContentProvider的方式从手机媒体数据库中加载歌曲数据
        // 因为是读取数据库，有可能是慢操作，所以开启子线程
        // 可能要考虑设置'回调'来显示RecyclerView，参照<<第一行代码>>的HttpCallback
        fillDataFromScanning();
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                //fillDataFromScanning();
                fillCoversFromScanning();
                MyLogger.log("子线程结束");
            }
        }).start();
        */
        //fillCoversFromScanning();

        adapter = new MyRecyclerViewAdapter(getActivity(), musicList);
        adapter.setOnItemClickListener(new RvOnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 播放该歌曲
                playSongAt(position);
            }
        });
        rv.setAdapter(adapter);

        // 底部控制栏默认显示第一首歌，免得空白，不好看
        tv_title.setText(musicList.get(curSong).getTitle());
        tv_artist.setText(musicList.get(curSong).getArtist());

        return ret;
    }

    // 加载收藏的歌曲
    private void fillDataFromDB() {
        musicList = DataSupport.findAll(MusicBean.class);
    }

    // 加载全部歌曲的基本信息：title和artist等
    private void fillDataFromScanning() {
        musicList = new ArrayList<>();    // 注意，这里是新的引用了
        Cursor cursor = null;
        try {
            cursor = getActivity().getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                    if (size > 1000 * 1024) {    // 条件？大于1M
                        int songId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                        String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                        int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                        MusicBean m = new MusicBean();
                        m.setTitle(title);
                        m.setArtist(artist);
                        m.setAlbum(album);
                        m.setDucation(duration);
                        m.setUrl(url);
                        m.setSongId(songId);
                        m.setAlbumId(albumId);
                        musicList.add(m);
                    }
                }
            } else {
                MyLogger.log("No song found!");
                Snackbar.make(getActivity().findViewById(R.id.drawer_layout),
                        "找不到歌曲！",
                        Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /* 加载全部歌曲的图片信息(cover) */
    private void fillCoversFromScanning() {
        /*
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            for (MusicBean m : musicList) {
                String filePath = m.getUrl();
                retriever.setDataSource(filePath);
                byte[] embedPic = retriever.getEmbeddedPicture();
                bitmap = BitmapFactory.decodeByteArray(embedPic, 0, embedPic.length);
                m.setCover(bitmap);
                m.setCover(getBitmapFromSongPath_1(m.getUrl()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        */
        Cursor cursor = null;
        for (MusicBean m: musicList) {
            cursor = getActivity().getContentResolver()
                    .query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                        MediaStore.Audio.Albums._ID + "=?",
                        new String[] {String.valueOf(m.getAlbumId())},
                        null);
            String albumUrl = null;
            if (cursor.moveToFirst()) {
                albumUrl = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
            }
            if (albumUrl != null) {
                m.setCover(BitmapFactory.decodeFile(albumUrl));
                MyLogger.log("album url is OK: " + m.getTitle());
            } else {
                m.setCover(null);
                //MyLogger.log("album url is NULL...");
            }
        }
        MyLogger.log("可以有");
        cursor.close();
    }

    // 根据歌曲的路径，找到该歌曲的专辑封面
    private Bitmap getBitmapFromSongPath_v2(String dataSource) {
        return null;
    }

    // 下一首歌的序号，可用于顺序播放模式中
    public int getCurIncrement() {
        ++curSong;
        if (curSong >= musicList.size()) {
            curSong = 0;
        }
        return curSong;
    }

    // 上一首歌的序号
    public int getCurDecrement() {
        --curSong;
        if (curSong < 0) {
            curSong = musicList.size() - 1;
        }
        return curSong;
    }

    // 得到随机的歌曲序号，用于随机播放模式中
    public int getRandomIndex() {
        double random = Math.random();
        return (int)(random * musicList.size());
    }

    /**
     * 播放音乐的总的操作，包括：
     * (1)播放服务的进行
     * (2)播放器界面标题、图片、进度条的更新，还有item高亮
     * (3)进度条的定时前进TimerTask
     * (4)curSong记录变量的更新
     *
     * */
    public void playSongAt(int position) {
        MyLogger.log("playSongAt(...)");
        isMusicStoping = false;
        curSong = position;
        MusicBean music = musicList.get(curSong);

        refreshNotification(music.getTitle());
        binder.playMusicAt(music.getUrl());

        tv_artist.setText(music.getArtist());
        tv_title.setText(music.getTitle());
        if (music.getCover() != null) {
            iv_cover.setImageBitmap(music.getCover());
        } else {
            iv_cover.setImageResource(R.drawable.album1);
        }
        //seekBar.setMax(music.getDucation());    // 进度条最大值设为歌曲的时长，方便和MediaPlayer同步
        btn_play.setBackgroundResource(R.drawable.ic_pause_black_24dp); // 给用户点击以暂停
        adapter.highlightItem(position);    // 高亮该歌曲item

        //MyLogger.log(music.getUrl());

        // DEBUG
        MyLogger.log("Ready to play: " + position + "." + music.getTitle() + " duration=" + music.getDucation());
        MyLogger.log("Song url = " + music.getUrl());
    }

    /* 放完一首歌之后，会发送一个广播，来调用以下这个函数，根据某种模式自动播放 */
    public void autoPlaySongAfterStoping() {
        switch (mode) {
            case MODE_LOOP:
                // Is doing nothing OK? If wrong, I can define it(looping mode) myself.
                MyLogger.log("mode: loop");
                //playSongAt(curSong);
                break;
            case MODE_ORDER:
                MyLogger.log("mode: order");
                playSongAt(getCurIncrement());
                break;
            case MODE_RANDOM:
                MyLogger.log("mode: random");
                playSongAt(getRandomIndex());
                break;
            default:
                break;
        }
    }

    public void refreshNotification(String ticker) {
        MyLogger.log("refresh Notification");

        RemoteViews remoteViews = new RemoteViews(getActivity().getPackageName(), R.layout.remote_player);
        remoteViews.setTextViewText(R.id.remote_title, ticker);
        Intent intent = new Intent(getActivity(), FirstActivity.class); // weird
        PendingIntent pIntent = PendingIntent.getActivity(getActivity(), 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(getActivity())
                .setContent(remoteViews)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setTicker(ticker);
        Notification note = builder.build();
        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(Constant.REMOTE_NOTIFICATION_ID, note);
    }

    // 广播：操作
    class PlayerViewChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MyLogger.log("收到广播：" + action);

            if (action.equals(Constant.BROADCAST_COMPLETE)) {    // 收到‘歌曲放完’的广播之后要进行的操作
                MyLogger.log("BROADCAST_COMPLETE");
                seekBar.setProgress(0); // 先把进度条设为0
                autoPlaySongAfterStoping(); // 然后在这个函数里再判断mode
            }
        }
    }

    // 拖动进度条的监听，让用户可以改变歌曲播放进度
    class MySeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            binder.seekTo(seekBar.getProgress());   // trick!
        }
    }

    // 播放器界面的几个按键：播放\暂停键，上一首，下一首
    class MyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_next:
                    playSongAt(getCurIncrement());
                    break;
                case R.id.btn_last:
                    playSongAt(getCurDecrement());
                    break;
                case R.id.btn_play_or_pause:
                    // 如果是一开始还没有选歌曲，就直接点播放键
                    if (isMusicStoping) {
                        if (musicList.size() == 0) {
                            Toast.makeText(getActivity(), "No song found!", Toast.LENGTH_SHORT).show();
                        } else {
                            playSongAt(curSong);
                        }
                        return;
                    }
                    if (binder.isMusicPlaying()) {  // 如果在播放音乐ing，那就暂停
                        btn_play.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                        binder.pauseMusic();
                    } else {                        // 如果是在暂停，那就继续播放
                        btn_play.setBackgroundResource(R.drawable.ic_pause_black_24dp);
                        binder.resumeMusic();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
