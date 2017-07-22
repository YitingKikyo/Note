package com.kikyo.note.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kikyo.note.module.File;
import com.kikyo.note.module.FileSQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;

import java.util.List;

/**
 * Created by 婷 on 2017/7/22.
 */

public class FileService {
    public static final String FILE_TABLE_NAME = "files";
    private StorIOSQLite mFileStor;

    public FileService(Context context) {
        mFileStor = DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(new FileService.FileSQLOpenHelper(context))
                .addTypeMapping(File.class, new FileSQLiteTypeMapping())
                .build();
    }

    public List<File> getAllFiles(){
        return mFileStor.get()
                .listOfObjects(File.class)
                .withQuery(Query.builder()
                        .table(FILE_TABLE_NAME)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    public List<File> searchFileByTitle(String title){
        return mFileStor.get()
                .listOfObjects(File.class)
                .withQuery(Query.builder()
                        .table(FILE_TABLE_NAME)
                        .where("fileName LIKE ?")
                        .whereArgs("%" + title + "%")
                        .build())
                .prepare()
                .executeAsBlocking();
    }


    public boolean deleteFile(File file){
        return deleteFileByName(file.getFileName());
    }

    public boolean deleteFileByName(String name) {
        return mFileStor.delete()
                .byQuery(DeleteQuery.builder()
                        .table(FILE_TABLE_NAME)
                        .where("fileName = ?")
                        .whereArgs("fileName")
                        .build())
                .prepare()
                .executeAsBlocking()
                .numberOfRowsDeleted() > 0;
    }

    public boolean insertFile(File file){
        return mFileStor.put()
                .object(file)
                .prepare()
                .executeAsBlocking()
                .wasInserted();
    }

    public boolean updateFile(File file){
        return mFileStor.put()
                .object(file)
                .prepare()
                .executeAsBlocking()
                .wasUpdated();
    }


    //這是一個SQLite數據庫的輔助類。如果我們不用第三方庫的話，直接用android内置的api來讀寫SQL的話，就會用這個類去打開數據庫連接。現在我們用的這個庫要求 我們提供這個類。
    private class FileSQLOpenHelper extends SQLiteOpenHelper {

        public FileSQLOpenHelper(Context context) {
            //參數分別是Context，表名，CursorFactory(不管他）,和數據庫版本
            super(context, FILE_TABLE_NAME, null, 1);
            //數據庫版本的用處在於，比如，你更新了應用，如果數據庫的設計和之前不一樣了（比如增刪了字段），那麽可以填入一共新版本號，這樣他發現版本
            //更新了遍會調用下面的onUpgrade函數去更新,遷移數據庫
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            //創建表。AUTOINCREMENT是自增，只对整数有用。还有文件名不好做键，可能有重复的；不过设计成不重复的也可以
            //这里有一个表的设计问题，就是要如何设计这个表以保存各个Note在哪个文件夹~。比如在这个表记录，或者在Note的表里记录。后面你写的时候再看一下。
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS files (\n"
                    + "fileName TEXT PRIMARY KEY NOT NULL, "
                    + "fileNum INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }

}
