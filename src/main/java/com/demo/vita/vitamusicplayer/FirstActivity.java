package com.demo.vita.vitamusicplayer;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by sjk on 2016/3/19.
 * 作为活动，主要是管理碎片的初始化工作、NavigationView对碎片的切换
 * 至于碎片和Service（MediaPlayer）的交互，最好还是不写在这里，否则会多很多麻烦的数据传递工作
 */
public class FirstActivity extends BaseActivity {

    public static final int FRAME_LAYOUT_ID = R.id.frame_content;


    Fragment curFragment;              // 现在显示的Fragment
    FragmentLike fragmentLike;
    FragmentMain fragmentMain;
    FragmentAbout fragmentAbout;
    TextView tv_first_head;
    Button btn_pop_drawer;

    MenuItem lastItemChecked;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    Button btn_choose_mode;
    PopupMenu popupMenu;

    Intent intentService;
    /*
    RecyclerView recyclerView;
    SeekBar seekBar;
    MyRecyclerViewAdapter adapter;
    List<MusicBean> musics;
    TextView tv_head;
    Toolbar toolbar;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        // 以下两个重要的操作，次序问题要考虑……
        prepareFragments();
        initViews();

        // 默认的视图，即一开始看到的初始视图的处理
        lastItemChecked = navigationView.getMenu().getItem(0);
        lastItemChecked.setChecked(true);
        tv_first_head.setText("歌曲列表");
        btn_choose_mode.setVisibility(View.VISIBLE);
    }

    private void initViews() {
        tv_first_head = (TextView) findViewById(R.id.tv_head);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new MyNavigationListener());
        btn_pop_drawer=(Button)findViewById(R.id.btn_pop_drawer);
        btn_pop_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        btn_choose_mode=(Button)findViewById(R.id.btn_choose_mode);
        popupMenu = new PopupMenu(this, btn_choose_mode);
        popupMenu.getMenuInflater().inflate(R.menu.menu_main, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                FragmentMain fm = (FragmentMain) getFragmentManager().findFragmentByTag("main");// find by TAG
                switch (item.getItemId()) {
                    case R.id.play_order:
                        fm.setMode(FragmentMain.MODE_ORDER);
                        break;
                    case R.id.play_loop:
                        fm.setMode(FragmentMain.MODE_LOOP);
                        break;
                    case R.id.play_random:
                        fm.setMode(FragmentMain.MODE_RANDOM);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        btn_choose_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    /**
     * 事先加载好全部fragment
     * 也许会造成卡顿
     * 求更好的方法，即让碎片能够加载view还有其动作，比如其中某个按钮可以跳转
     */
    private void prepareFragments() {
        FragmentTransaction t = getFragmentManager().beginTransaction();
        fragmentLike = new FragmentLike();
        fragmentMain = new FragmentMain();
        fragmentAbout = new FragmentAbout();
        t.add(FRAME_LAYOUT_ID, fragmentLike, "like");   // 附带上碎片的tag
        t.add(FRAME_LAYOUT_ID, fragmentMain, "main");
        t.add(FRAME_LAYOUT_ID, fragmentAbout, "about");
        t.hide(fragmentLike)
                .hide(fragmentAbout)
                .commit();
        curFragment = fragmentMain;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void switchFragment(Fragment from, Fragment to) {
        //MyLogger.log(from.getId() + "--->" + to.getId());
        //MyLogger.log(String.format(", (%s)", from == to ? "same" : "different") + "in object");
        //MyLogger.log(String.format(", (%s)", from.getId() == to.getId() ? "same" : "different") + "in getId()");
        if (from != to) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.hide(from).show(to).commit();
            curFragment = to;
        }
    }


    class MyNavigationListener implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.option_main:
                    switchFragment(curFragment, fragmentMain);
                    tv_first_head.setText("歌曲列表");
                    btn_choose_mode.setVisibility(View.VISIBLE);
                    break;
                case R.id.option_about:
                    switchFragment(curFragment, fragmentAbout);
                    tv_first_head.setText("关于作者");
                    btn_choose_mode.setVisibility(View.GONE);
                    break;
                case R.id.option_finish:
                    // some view animation...
                    ActivityCollector.finishAll();  // terminal the App
                    break;
                case R.id.option_like:
                    switchFragment(curFragment, fragmentLike);
                    tv_first_head.setText("我喜欢");
                    btn_choose_mode.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
            lastItemChecked.setChecked(false);
            lastItemChecked = item;
            lastItemChecked.setChecked(true);
            drawerLayout.closeDrawers();
            return true;
        }
    }
}
