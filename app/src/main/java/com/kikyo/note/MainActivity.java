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

import com.kikyo.note.module.File;
import com.kikyo.note.module.Note;
import com.kikyo.note.service.FileService;
import com.kikyo.note.service.NoteService;
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
    private NoteService mNoteService;

    private RecyclerView mFileList;
    private List<File> mFiles = new ArrayList<>();
    private FileService mFileService;

    //colorPrimaryDark不是用來做背景色的~他是狀態欄的眼色
    //Toolbar包括上面一栏的全部东西，比如标题，菜单图标, 搜索图标都是Toolbar的内容，不用自己用TextView, ImageView


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNoteService = new NoteService(this);
        mFileService = new FileService(this);
        setUpViews();

    }

    private void setUpViews() {
        View view = View.inflate(this, R.layout.activity_main, null);
        setContentView(view);

        setUpToolbar();
        setUpDrawer();

        initNoteList();
        initFileList();
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
        mNotes = new ArrayList<>(mNoteService.getAllNotss());
        if(mNotes.isEmpty()){
            //在数据库加两个测试~
            mNoteService.insertNote(new Note("啦啦啦", "嘿嘿嘿"));
            mNoteService.insertNote(new Note("略略", "嘤嘤嘤"));
            //我们以他返回的列表新创建一个列表，新的列表就是可以修改的。额..感觉讲的不明不白
            mNotes = new ArrayList<>(mNoteService.getAllNotss());
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

    //最后一个。为什么RecyclerView要这样设计成ViewHolder和Adapter。
    //首先，一个列表控件不能把所有列表里的项全部都加载进来，不能像LinearLayout一样把一项一项推进来就构成一个列表。
    //因为列表可能有几十,几百项，全部都加载的話，效率很低。因此事实上RecyclerView一直只有几个项显示在屏幕上，
    //而当我们滑动他的时候，他是这样做的：比如一开始显示123456这些项，我们往下滑，1就不在屏幕里了而7进来了。这时RecyclerView并不是
    //把1的View销毁再创建一个7的View，而是把2的数据绑定到第一个View上，把3的数据绑定到第二个View上，。。。，把7绑定到第六个Viewshang。
    //之前的View还是那些View，只不过每一个绑定的数据更改了而已。因为创建一个View的开销是很大的，而更改绑定的数据很容易。
    //那么为什么需要ViewHolder呢。一个项的View要更改数据的地方也就几个，比如标题，图标等，如果每次更改都用findViewById去找，由于数据更改很频繁
    //而findViewById效率并不高，因此如果把要更改的那些先找出来保存为变量，效率就能高很多了。
    //因此，我们可以看到，整个Adapter的流程是：
    //1. 根据屏幕能显示多少个项而创建多少个ViewHolder，也就是onCreateViewHolder
    //2. ViewHolder在他的构造函数里，把要更改数据的View先找出来存为字段
    //3. 根据当前显示第几项到第几项，把这些项的数据绑定的对应的ViewHolder上，就是onBindViewHolder。
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
            holder.date.setText(note.getDate());
            holder.first.setText(note.getTitle().substring(0, 1));
            //這裏用了他的
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
                    //他的异常是Collections$UnmodifiableList，不可修改的列表。
                    //这时因为查询操作返回的列表是不可修改的。呃，感觉这种异常要有经验才能一看就知道是上面。
                    Note removedNote = mNotes.remove(pos);
                    mNoteService.deleteNote(removedNote);
                    mNoteList.getAdapter().notifyItemRemoved(pos);
                }
            });
            title = (TextView) itemView.findViewById(R.id.title);
            date =  (TextView) itemView.findViewById(R.id.date_time);
            summary =  (TextView) itemView.findViewById(R.id.summary);
            first = (TextView)  itemView.findViewById(R.id.first);
        }
    }


    private void initFileList() {
        mFileList = (RecyclerView) findViewById(R.id.drawer_layout_recycler_view);
        mFileList.setLayoutManager(new LinearLayoutManager(this));
        mFiles = new ArrayList<>(mFileService.getAllFiles());
        if(mFiles.isEmpty()){
            //在数据库加两个测试~
            mFileService.insertFile(new File("嘿嘿文件", 2));
            mFileService.insertFile(new File("略略文件", 1));
            //我们以他返回的列表新创建一个列表，新的列表就是可以修改的。额..感觉讲的不明不白
            mFiles = new ArrayList<>(mFileService.getAllFiles());
        }
        mFileList.setAdapter(new FileListAdapter(mFiles));
        mFileList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this)
                .color(R.color.divider)
                .sizeResId(R.dimen.divider)
                .build());
    }

    //这里的泛型参数是FileViewHolder，下面的onCreateViewHolder的参数也应该是FileViewHolder
    private class FileListAdapter extends RecyclerView.Adapter<FileViewHolder> {

        private List<File> mFiles;

        public FileListAdapter(List<File> files) {
            mFiles = files;
        }

        @Override
        public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(FileViewHolder holder, int position) {
            File file = mFiles.get(position);
            //我记得你当时是这样写的。然后，关键问题是导错包了，就是import了java.io.File，然后你就找不到那个函数了。
            //同名的类比较容易出现这种差错~。记得导包的时候看一下~一般代码提示的时候，发现函数不是自己的那些，就知道是导错了
            holder.file_name.setText(file.getFileName());
            holder.file_num.setText("2");
        }



        //复制代码的时候很容易出现这种问题。忘了改一些地方~所以复制代码要小心一点。
        @Override
        public int getItemCount() {
            return mFiles.size();
        }
    }

    private class FileViewHolder extends RecyclerView.ViewHolder {

        public TextView file_name, file_num;

        public FileViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    Toast.makeText(view.getContext(), "点击了" + pos, Toast.LENGTH_SHORT).show();
                    //他的异常是Collections$UnmodifiableList，不可修改的列表。
                    //这时因为查询操作返回的列表是不可修改的。呃，感觉这种异常要有经验才能一看就知道是上面。
                    File removedFile = mFiles.remove(pos);
                    mFileService.deleteFile(removedFile);
                    //导错包了[笑cry]。java里有一个File类，和你自己写的重复了，import的时候弄错了
                    mFileList.getAdapter().notifyItemRemoved(pos);
                }
            });
            file_name = (TextView) itemView.findViewById(R.id.file_name);
            file_num =  (TextView) itemView.findViewById(R.id.file_num);

        }
    }


}
