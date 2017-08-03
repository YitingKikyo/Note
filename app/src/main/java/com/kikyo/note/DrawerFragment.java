package com.kikyo.note;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kikyo.note.dialog.OperationMenuDialogBuilder;
import com.kikyo.note.module.NoteDirectory;
import com.kikyo.note.service.NoteDirectoryService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by 婷 on 2017/7/25.
 */
//但是一些代码还是比较乱，比如对话框。要么这个对话框写成一个类，要么用可以用的第三方库。我们用MaterialDialog代替输入文件名部分
public class DrawerFragment extends Fragment {

    private View mView;
    private RecyclerView mNoteDirectoryList;
    private List<NoteDirectory> mNoteDirectories = new ArrayList<>();
    private NoteDirectoryService mNoteDirectoryService;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNoteDirectoryService = new NoteDirectoryService(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mView = view;
        setButtonListener();
        initFileList();
    }

    private void initFileList() {
        mNoteDirectoryList = (RecyclerView) mView.findViewById(R.id.drawer_layout_recycler_view);
        mNoteDirectoryList.setLayoutManager(new LinearLayoutManager(getContext()));
        mNoteDirectories = new ArrayList<>(mNoteDirectoryService.getAllDirectories());
        mNoteDirectoryList.setAdapter(new NoteDirectoryListAdapter(mNoteDirectories));
    }


    public void setButtonListener() {
        //点击效果一般是点击一整列都会弹出对话框，而不是只能点击那个图标~
        mView.findViewById(R.id.new_directory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewDirectoryDialog();
            }
        });
    }

    //另外，不要因为函数名太长就用单词的前几个字母。除非是众所周知的在、缩写。
    public void showNewDirectoryDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.create___)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .input(getString(R.string.please_input_name), "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        createNewDirectory(input.toString());
                    }
                }).show();
    }

    private void createNewDirectory(String name) {
        NoteDirectory f = new NoteDirectory(name, 0);
        //新建文件夹
        boolean success = mNoteDirectoryService.insertDirectory(f);
        if (success) {
            //这里增加了一个File，重新设置Adapter的話效率低，直接通知他有新文件就好了
            //新建的文件，id是数据自动生成的，那么，我们加进mFiles的那个File是没有id的。
            //解决办法是，插入以后从数据库再重新获取整个文件列表，或者自己生成id。前者效率低但方便，先用这种方法。
            //我们只让mFiles等于另一个列表，但是传进adapter的那个列表并没有揹更改...我们还是采用第2把。
            mNoteDirectories.add(f);
            mNoteDirectoryList.getAdapter().notifyItemInserted(mNoteDirectories.size() - 1);
        }
        Toast.makeText(getContext(), success ? R.string.create_successfully : R.string.creat_failed, Toast.LENGTH_SHORT).show();

    }


    private class NoteDirectoryListAdapter extends RecyclerView.Adapter<NoteDirectoryViewHolder> {

        private List<NoteDirectory> mNoteDirectories;

        NoteDirectoryListAdapter(List<NoteDirectory> noteDirectories) {
            mNoteDirectories = noteDirectories;
        }

        @Override
        public NoteDirectoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new NoteDirectoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(NoteDirectoryViewHolder holder, int position) {
            NoteDirectory noteDirectory = mNoteDirectories.get(position);
            holder.mDirectoryName.setText(noteDirectory.getName());
            holder.mNoteCount.setText(String.valueOf(noteDirectory.getNoteCount()));
        }


        @Override
        public int getItemCount() {
            return mNoteDirectories.size();
        }
    }

    private class NoteDirectoryViewHolder extends RecyclerView.ViewHolder {

        //還有這個。一般來説在Java或者安卓裏變量命名是駝峰式的。這樣和其他Java本來的代碼就不會個格格不入。除非有强烈的個人喜好。
        TextView mDirectoryName, mNoteCount;

        NoteDirectoryViewHolder(final View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    int pos = getAdapterPosition();
                    NoteDirectory noteDirectory = mNoteDirectories.get(pos);
                    showOperationDialog(noteDirectory, pos);
                    return true;
                }
            });
            mDirectoryName = (TextView) itemView.findViewById(R.id.directory_name);
            mNoteCount = (TextView) itemView.findViewById(R.id.note_count);

        }
    }

    private void showOperationDialog(final NoteDirectory noteDirectory, final int pos) {
        //这里有个问题。我们要获取到这个Dialog然后调用他的dismiss才能消失。
        //当我们想在onSelect里调用dismiss就有问题了。
        //因为在selectListener那里dialog变量还没有创建完成（dialog变量是show返回的，而selectListener那里还没调用到show)，是不能使用的。
        //但是我们知道当他被选中，也就是onSelect被调用的时候dialog肯定创建了。一种解决方法是。
        new OperationMenuDialogBuilder(getContext())
                .operations(Arrays.asList(getString(R.string.rename), getString(R.string.delete)))
                .selectListener(new OperationMenuDialogBuilder.OnOperationSelectListener() {
                    @Override
                    public void onSelect(MaterialDialog dialog, String text, int position) {
                        if (position == 0) {
                            showRenameDirectoryDialog(noteDirectory, pos);
                        } else {
                            delete(noteDirectory, pos);
                        }
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showRenameDirectoryDialog(final NoteDirectory noteDirectory, final int pos) {
        new MaterialDialog.Builder(getContext())
                .title(R.string.rename)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .input(getString(R.string.please_input_name), noteDirectory.getName(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        rename(noteDirectory, input.toString(), pos);
                    }
                }).show();
    }

    private void rename(NoteDirectory f, String newName, int pos) {
        f.setName(newName);
        if (mNoteDirectoryService.updateDirectory(f)) {
            //直接传一个位置就好了。至于那个有另一个参数的是什么意思，我们可以看看文档。一时看不懂，可以去网上查查
            mNoteDirectoryList.getAdapter().notifyItemChanged(pos);
            Toast.makeText(getContext(), R.string.rename_succeed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), R.string.rename_failed, Toast.LENGTH_SHORT).show();
        }
    }


    private void delete(NoteDirectory noteDirectory, int pos) {
        if (mNoteDirectoryService.deleteDirectory(noteDirectory)) {
            mNoteDirectories.remove(pos);
            mNoteDirectoryList.getAdapter().notifyItemRemoved(pos);
            Toast.makeText(getContext(), R.string.delete_succeed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), R.string.delete_failed, Toast.LENGTH_SHORT).show();
        }
    }

}
