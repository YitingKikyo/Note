package com.kikyo.note;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kikyo.note.module.File;
import com.kikyo.note.module.Note;
import com.kikyo.note.service.FileService;
import com.kikyo.note.service.NoteService;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

//首先，MainActivity太乱啦，先把他分成几个Fragment
//这样的話MainActivity和几个Fragment就都比较少啦，各自负责自己的部分。
public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";
    private Toolbar mToolbar;
    //Toolbar包括上面一栏的全部东西，比如标题，菜单图标, 搜索图标都是Toolbar的内容，不用自己用TextView, ImageView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpViews();
    }


    private void setUpViews() {
        setContentView(R.layout.activity_main);
        setUpToolbar();
        setUpDrawer();

    }

    private void setUpToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpDrawer() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //創建返回鍵，并且實現打開/關閉監聽
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, mToolbar, R.string.open, R.string.close);
        drawerToggle.syncState();

        drawerLayout.addDrawerListener(drawerToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }


}
