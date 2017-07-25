package com.kikyo.note.module;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;

/**
 * Created by 婷 on 2017/7/22.
 */
@StorIOSQLiteType(table = "files")

public class File {

    @StorIOSQLiteColumn(name = "id", key = true)
    int mId;

    @StorIOSQLiteColumn(name = "fileName")
    String mFileName;

    @StorIOSQLiteColumn(name = "fileNum")
    Integer mFileNum;

    public File(){}

    public File(String name, int num){
        mFileName = name;
        mFileNum = num;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }
    public String getFileName() {
        return mFileName;
    }


    public void setFileNum(int fileNum) {
        this.mFileNum = fileNum;
    }
    public int getFileNum() {
        return mFileNum;
    }

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }
}
