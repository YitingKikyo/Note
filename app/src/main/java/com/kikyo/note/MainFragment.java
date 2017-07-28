package com.kikyo.note;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kikyo.note.module.Note;
import com.kikyo.note.service.NoteService;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by 婷 on 2017/7/25.
 */

public class MainFragment extends Fragment {

    //大概就這樣。通過StartActivityForResult啓動編輯的Activity。然後EditNoteActivity通過setResult把編輯的結果設置，
    //在這個Fragment的onActivityResult的囘調裏獲取這個結果並判斷。至於這個REQUEST_CODE_EDIT_NOTE，衹是區分不同的請求的一個數字（0~65535），通常隨便設置一個就好了。
    private static final int REQUEST_CODE_EDIT_NOTE = 2154;

    private View mView;
    private RecyclerView mNoteList;
    private List<Note> mNotes = new ArrayList<>();
    private NoteService mNoteService;
    private int mEditNotePosition = -1;
    private NoteService.OnNoteDeleteListener mOnNoteDeleteListener  = new NoteService.OnNoteDeleteListener() {
        @Override
        public void onNoteDelete(Note note) {
            //剛剛因爲我們沒有重寫equals函數，比較Note説是用==，而這個Note是在EditActivity裏通過id重新構造出的
            int pos = mNotes.indexOf(note);
            if(pos >= 0){
                mNotes.remove(pos);
                mNoteList.getAdapter().notifyItemRemoved(pos);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNoteService = NoteService.getInstance();
       // mNoteService.setOnNoteDeleteListener(mOnNoteDeleteListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        initNoteList();
        setUpFAB();
    }

    private void setUpFAB() {
        FloatingActionButton fb = (FloatingActionButton) mView.findViewById(R.id.floatButton_newNote);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note n = new Note("新文件", "1");
                mNotes.add(n);
                mNoteService.insertNote(n);
                mNoteList.getAdapter().notifyItemInserted(mNotes.size() - 1);
            }
        });
    }


    private void initNoteList() {
        mNoteList = (RecyclerView) mView.findViewById(R.id.recycler_view);
        mNoteList.setLayoutManager(new LinearLayoutManager(getContext()));
        mNotes = new ArrayList<>(mNoteService.getAllNotss());
        mNoteList.setAdapter(new NoteListAdapter(mNotes));
        mNoteList.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext())
                .color(R.color.divider)
                .sizeResId(R.dimen.divider)
                .build());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_EDIT_NOTE){
            int status = data.getIntExtra(EditNoteActivity.EXTRA_STATUS, 0);
            if(status == EditNoteActivity.STATUS_DELETED && mEditNotePosition >= 0){
                mNotes.remove(mEditNotePosition);
                mNoteList.getAdapter().notifyItemRemoved(mEditNotePosition);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注意。一定要把這個Listener設置為null。因爲，NoteService的生命周期比這個MainFragment長。也就是，NoteService和整個應用的生命周期一樣長，
        //但是這個MainFragment衹有在主頁面時才存活。如果這裏不設置為null，那麽NoteService就持有這個MainFragment的NoteDeletListener的引用，導致
        //儅這個Fragment沒有再使用時，因爲NoteService仍然能引用到他，不能揹垃圾回收，從而導致内存泄漏。（如果現在不能完全理解先跳過）
        mNoteService.setOnNoteDeleteListener(null);
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

        NoteListAdapter(List<Note> notes) {
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

        TextView title, date, summary, first;

        NoteViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    mEditNotePosition = pos;
                    Note note = mNotes.get(pos);
                    //每次增加一個Activity，要在Manifest裏聲明她
                    startActivityForResult(new Intent(getActivity(), EditNoteActivity.class)
                        .putExtra(EditNoteActivity.EXTRA_NOTE_ID, note.getId()), REQUEST_CODE_EDIT_NOTE);

                }
            });

            title = (TextView) itemView.findViewById(R.id.title);
            date = (TextView) itemView.findViewById(R.id.date_time);
            summary = (TextView) itemView.findViewById(R.id.summary);
            first = (TextView) itemView.findViewById(R.id.first);
        }
    }
}
