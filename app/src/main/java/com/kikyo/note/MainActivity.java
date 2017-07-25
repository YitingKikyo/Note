package com.kikyo.note;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
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


public class MainActivity extends AppCompatActivity  {

    private static final String LOG_TAG = "MainActivity";
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

        //这里已经有一个set***了
        setContentView(view);

        setBottleButtonLis();

        setUpToolbar();
        setUpDrawer();

        initNoteList();
        initFileList();

        setlistenners();

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
        mFileList.setAdapter(new FileListAdapter(mFiles));
    }

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
            holder.file_name.setText(file.getFileName());
            holder.file_num.setText(String.valueOf(file.getFileNum()));
        }


        @Override
        public int getItemCount() {
            return mFiles.size();
        }
    }

    private class FileViewHolder extends RecyclerView.ViewHolder {

        public TextView file_name, file_num;

        public FileViewHolder(final View itemView) {

            super(itemView);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {

                    final int pos = getAdapterPosition();
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    //通过布局填充器获login_layout
                    View view = getLayoutInflater().inflate(R.layout.rename_or_delete_dia,null);

                    TextView t1 = (TextView) view.findViewById(R.id.renameFile);
                    TextView t2 = (TextView) view.findViewById(R.id.deleteFile);

                    builder.setView(view);//设置login_layout为对话提示框
                    final AlertDialog dialog = builder.show(); //显示Dialog对话框

                    t1.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                            //通过布局填充器获login_layout
                            View view1 = getLayoutInflater().inflate(R.layout.rename_file_dia,null);

                            final EditText newFileNameTextView = (EditText) view1.findViewById(R.id.new_rename_file_name);
                            newFileNameTextView.setText( mFiles.get(pos).getFileName());

                            TextView tt1 = (TextView) view1.findViewById(R.id.sure_rename);
                            TextView tt2 = (TextView) view1.findViewById(R.id.cancel_rename);

                            builder1.setView(view1);//设置login_layout为对话提示框
                            final AlertDialog dialog1 = builder1.show(); //显示Dialog对话框

                            tt1.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    //这个报错的意思，执行了EditText的getText()，但是这个EditText为null。也就是newFileFile为null。
                                    String newName = newFileNameTextView.getText().toString().trim();
                                    //刚刚debug发现id是null。正常来说不应该的。这是因为~
                                    File f = mFiles.get(pos);
                                    f.setFileName(newName);
                                    if(mFileService.updateFile(f)){
                                        //直接传一个位置就好了。至于那个有另一个参数的是什么意思，我们可以看看文档。一时看不懂，可以去网上查查
                                        mFileList.getAdapter().notifyItemChanged(pos);
                                        Toast.makeText(MainActivity.this, R.string.rename_succeed, Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(MainActivity.this, R.string.rename_failed, Toast.LENGTH_SHORT).show();
                                    }
                                    dialog1.dismiss();
                                    dialog.dismiss();
                                }
                            });
                            tt2.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    dialog1.dismiss();
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                    t2.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            File removedFile = mFiles.remove(pos);
                            mFileService.deleteFile(removedFile);
                            mFileList.getAdapter().notifyItemRemoved(pos);
                            dialog.dismiss();
                        }
                    });

                    return true;
                }
            });
            file_name = (TextView) itemView.findViewById(R.id.file_name);
            file_num =  (TextView) itemView.findViewById(R.id.file_num);

        }
    }

    public void setBottleButtonLis() {
        //点击效果一般是点击一整列都会弹出对话框，而不是只能点击那个图标~
        findViewById(R.id.new_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打个日志以确定这个函数揹执行了，从而确定问题是OnClickListener没有用还是下面的buildNewFileDiaShow没有用。
                Log.d(LOG_TAG, "new file button clicked!");
                buildNewFileDiaShow();
            }
        });
    }

    public void buildNewFileDiaShow(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //通过布局填充器获login_layout
        View view = getLayoutInflater().inflate(R.layout.new_file_dia,null);

        final EditText newFileName = (EditText) view.findViewById(R.id.new_file_name);

        TextView t1 = (TextView) view.findViewById(R.id.ok);
        TextView t2 = (TextView) view.findViewById(R.id.cancel);

        //id 弄错了，取消的id变成ensure_to_create了..倒错了
        builder.setView(view);//设置login_layout为对话提示框
        final AlertDialog dialog = builder.show(); //显示Dialog对话框

        t1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "ok clicked");
                //现在这个报错的意思是，执行了EditText的getText()，但是这个EditText为null。也就是newFileFile为null。
                String a = newFileName.getText().toString().trim();

                File f = new File(a, 0);
                //新建文件夹
                boolean success = mFileService.insertFile(f);
                if(success) {
                    //这里增加了一个File，重新设置Adapter的話效率低，直接通知他有新文件就好了
                    //新建的文件，id是数据自动生成的，那么，我们加进mFiles的那个File是没有id的。
                    //解决办法是，插入以后从数据库再重新获取整个文件列表，或者自己生成id。前者效率低但方便，先用这种方法。
                    //我们只让mFiles等于另一个列表，但是传进adapter的那个列表并没有揹更改...我们还是采用第2把。
                    mFiles.add(f);
                    mFileList.getAdapter().notifyItemInserted(mFiles.size() - 1);
                }
                dialog.dismiss();
                Toast.makeText(MainActivity.this, success ? R.string.create_successfully : R.string.creat_failed, Toast.LENGTH_SHORT).show();
            }
        });
        t2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "cancel clicked");
                dialog.dismiss();
            }
        });

    }

    private void setlistenners() {
        FloatingActionButton fb = (FloatingActionButton)findViewById(R.id.floatButton_newNote);
        fb.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Note n = new Note("新文件", "1");
                mNotes.add(n);
                mNoteService.insertNote(n);
                mNoteList.getAdapter().notifyItemInserted(mNotes.size()-1);
            }
        });
    }

}
