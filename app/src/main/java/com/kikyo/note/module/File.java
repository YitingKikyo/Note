package com.kikyo.note.module;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;

/**
 * Created by 婷 on 2017/7/22.
 */
@StorIOSQLiteType(table = "files")

public class File {

    @StorIOSQLiteColumn(name = "fileName", key = true, ignoreNull = true)
    String mfileName;

    @StorIOSQLiteColumn(name = "fileNum")
    Integer mfileNum;

    public File(){}

    public File(String name, int num){
        mfileName = name;
        mfileNum = num;
    }

    public void setFileName(String fileName) {
        this.mfileName = fileName;
    }
    public String getFileName() {
        return mfileName;
    }


    public void setFileNum(int fileNum) {
        this.mfileNum = fileNum;
    }
    public int getFileNum() {
        return mfileNum;
    }
}
