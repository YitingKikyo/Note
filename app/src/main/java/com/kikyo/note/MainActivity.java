package com.kikyo.note;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.media.Image;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kikyo.note.module.Note;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private RecyclerView mNoteList;
    private List<Note> mNotes = new ArrayList<>();
    //colorPrimaryDark不是用來做背景色的~他是狀態欄的眼色
    //Toolbar包括上面一栏的全部东西，比如标题，菜单图标, 搜索图标都是Toolbar的内容，不用自己用TextView, ImageView


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpViews();


    }

    private void setUpViews() {
        View view = View.inflate(this, R.layout.activity_main, null);
        setContentView(view);

        setUpToolbar();
        setUpDrawer();



        initNoteList();
    }

    private void setUpToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void setUpDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //創建返回鍵，并且實現打開/關閉監聽
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open, R.string.close);
        mDrawerToggle.syncState();

        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void initNoteList() {
        mNoteList = (RecyclerView) findViewById(R.id.recycler_view);
        mNoteList.setLayoutManager(new LinearLayoutManager(this));
        mNotes.add(new Note("标题", "内容"));
        for(int i = 0; i < 10; i++){
            mNotes.add(new Note("标题" + i, "啦啦啦"));
        }
        mNoteList.setAdapter(new NoteListAdapter(mNotes));
        mNoteList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this)
                .color(R.color.divider)
                .sizeResId(R.dimen.divider)
                .build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    private class NoteListAdapter extends RecyclerView.Adapter<NoteViewHolder> {

        private List<Note> mNotes;

        public NoteListAdapter(List<Note> notes) {
            mNotes = notes;
        }

        @Override
        public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.note_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(NoteViewHolder holder, int position) {
            Note note = mNotes.get(position);
            holder.title.setText(note.getTitle());
            holder.summary.setText(note.getContent());
            SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
            holder.date.setText(format.format(note.getDate()));
            holder.first.setText(note.getTitle().substring(0, 1));
        }



        @Override
        public int getItemCount() {
            return mNotes.size();
        }
    }

    private class NoteViewHolder extends RecyclerView.ViewHolder {

        public TextView title, date, summary, first;

        public NoteViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    Toast.makeText(view.getContext(), "点击了" + pos, Toast.LENGTH_SHORT).show();
                    mNotes.remove(pos);
                    mNoteList.getAdapter().notifyItemRemoved(pos);
                }
            });
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date_time);
            summary = itemView.findViewById(R.id.summary);
            first = itemView.findViewById(R.id.first);
        }
    }

}
