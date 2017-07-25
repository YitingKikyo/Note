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
import com.kikyo.note.module.File;
import com.kikyo.note.service.FileService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by 婷 on 2017/7/25.
 */
//但是一些代码还是比较乱，比如对话框。要么这个对话框写成一个类，要么用可以用的第三方库。我们用MaterialDialog代替输入文件名部分
public class DrawerFragment extends Fragment {

    private View mView;
    private RecyclerView mFileList;
    private List<File> mFiles = new ArrayList<>();
    private FileService mFileService;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileService = new FileService(getContext());
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
        mFileList = (RecyclerView) mView.findViewById(R.id.drawer_layout_recycler_view);
        mFileList.setLayoutManager(new LinearLayoutManager(getContext()));
        mFiles = new ArrayList<>(mFileService.getAllFiles());
        mFileList.setAdapter(new FileListAdapter(mFiles));
    }


    public void setButtonListener() {
        //点击效果一般是点击一整列都会弹出对话框，而不是只能点击那个图标~
        mView.findViewById(R.id.new_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewFileDialog();
            }
        });
    }

    //另外，不要因为函数名太长就用单词的前几个字母。除非是众所周知的在、缩写。
    public void showNewFileDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.create___)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .input(getString(R.string.please_input_name), "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        createNewFile(input.toString());
                    }
                }).show();
    }

    private void createNewFile(String name) {
        File f = new File(name, 0);
        //新建文件夹
        boolean success = mFileService.insertFile(f);
        if (success) {
            //这里增加了一个File，重新设置Adapter的話效率低，直接通知他有新文件就好了
            //新建的文件，id是数据自动生成的，那么，我们加进mFiles的那个File是没有id的。
            //解决办法是，插入以后从数据库再重新获取整个文件列表，或者自己生成id。前者效率低但方便，先用这种方法。
            //我们只让mFiles等于另一个列表，但是传进adapter的那个列表并没有揹更改...我们还是采用第2把。
            mFiles.add(f);
            mFileList.getAdapter().notifyItemInserted(mFiles.size() - 1);
        }
        Toast.makeText(getContext(), success ? R.string.create_successfully : R.string.creat_failed, Toast.LENGTH_SHORT).show();

    }


    private class FileListAdapter extends RecyclerView.Adapter<FileViewHolder> {

        private List<File> mFiles;

        FileListAdapter(List<File> files) {
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

        TextView file_name, file_num;

        FileViewHolder(final View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    int pos = getAdapterPosition();
                    File file = mFiles.get(pos);
                    showOperationDialog(file, pos);
                    return true;
                }
            });
            file_name = (TextView) itemView.findViewById(R.id.file_name);
            file_num = (TextView) itemView.findViewById(R.id.file_num);

        }
    }

    private void showOperationDialog(final File file, final int pos) {
        new OperationMenuDialogBuilder(getContext())
                .operations(Arrays.asList(getString(R.string.rename), getString(R.string.delete)))
                .selectListener(new OperationMenuDialogBuilder.OnOperationSelectListener() {
                    @Override
                    public void onSelect(String text, int position) {
                        if (position == 0) {
                            showRenameFileDialog(file, pos);
                        } else {
                            delete(file, pos);
                        }
                    }
                })
                .show();
    }

    private void showRenameFileDialog(final File file, final int pos) {
        new MaterialDialog.Builder(getContext())
                .title(R.string.rename)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .input(getString(R.string.please_input_name), file.getFileName(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        rename(file, input.toString(), pos);
                    }
                }).show();
    }

    private void rename(File f, String newName, int pos) {
        f.setFileName(newName);
        if (mFileService.updateFile(f)) {
            //直接传一个位置就好了。至于那个有另一个参数的是什么意思，我们可以看看文档。一时看不懂，可以去网上查查
            mFileList.getAdapter().notifyItemChanged(pos);
            Toast.makeText(getContext(), R.string.rename_succeed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), R.string.rename_failed, Toast.LENGTH_SHORT).show();
        }
    }


    private void delete(File file, int pos) {
        mFileService.deleteFile(file);
        mFiles.remove(pos);
        mFileList.getAdapter().notifyItemRemoved(pos);
    }

}
