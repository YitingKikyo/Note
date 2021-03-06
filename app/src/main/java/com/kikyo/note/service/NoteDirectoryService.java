package com.kikyo.note.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.kikyo.note.module.NoteDirectory;
import com.kikyo.note.module.NoteDirectorySQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.put.PutResult;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;

import java.util.List;

/**
 * Created by 婷 on 2017/7/22.
 */
//命名問題最好在項目早期就改好，否則後面重命名的問題更大。其實有時候我的命名也不好，比如Operation。不過至少讓人誤解想表達的意思。命名不要怕長~
//現在重命名已經要改很多東西了，如果是項目中後期，就衹能放任不管了，否則風險太大。
public class NoteDirectoryService {
    public static final String TABLE_NAME = "note_directory";
    private static final String KEY_MAX_ID = NoteDirectoryService.class.getName() + ".max_id";
    private StorIOSQLite mDirectoryStor;
    //SharedPreferences是一个轻量级的保存数据的东西~
    private SharedPreferences mSharedPreferences;

    public NoteDirectoryService(Context context) {
        mDirectoryStor = DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(new NoteDirectorySQLOpenHelper(context))
                .addTypeMapping(NoteDirectory.class, new NoteDirectorySQLiteTypeMapping())
                .build();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public List<NoteDirectory> getAllDirectories(){
        return mDirectoryStor.get()
                .listOfObjects(NoteDirectory.class)
                .withQuery(Query.builder()
                        .table(TABLE_NAME)
                        .build())
                .prepare()
                .executeAsBlocking();
    }


    public boolean deleteDirectory(NoteDirectory noteDirectory){
        return deleteDirectoryById(noteDirectory.getId());
    }

    public boolean deleteDirectoryById(int id) {
        return mDirectoryStor.delete()
                .byQuery(DeleteQuery.builder()
                        .table(TABLE_NAME)
                        .where("id = ?")
                        .whereArgs(id)
                        .build())
                .prepare()
                .executeAsBlocking()
                .numberOfRowsDeleted() > 0;
    }

    public boolean insertDirectory(NoteDirectory noteDirectory){
        noteDirectory.setId(getAndIncreaseMaxId());
        return mDirectoryStor.put()
                .object(noteDirectory)
                .prepare()
                .executeAsBlocking()
                .wasInserted();
    }

    private int getAndIncreaseMaxId() {
        //这个key只要是一个不会重复的字符串就好了
        //一般为了避免重复，我们都是命名成"类全名.名称"。比如"com.kikyo.note.NoteDirectoryService.max_id"。
        int maxId = mSharedPreferences.getInt(KEY_MAX_ID, 0);
        mSharedPreferences.edit()
                .putInt(KEY_MAX_ID, maxId + 1)
                .apply();
        return maxId;
    }

    public boolean updateDirectory(NoteDirectory noteDirectory){
        PutResult putResult = mDirectoryStor.put()
                .object(noteDirectory)
                .prepare()
                .executeAsBlocking();
        return putResult.wasUpdated();
    }


    //這是一個SQLite數據庫的輔助類。如果我們不用第三方庫的話，直接用android内置的api來讀寫SQL的話，就會用這個類去打開數據庫連接。現在我們用的這個庫要求 我們提供這個類。
    private class NoteDirectorySQLOpenHelper extends SQLiteOpenHelper {

        public NoteDirectorySQLOpenHelper(Context context) {
            //參數分別是Context，表名，CursorFactory(不管他）,和數據庫版本
            super(context, TABLE_NAME, null, 1);
            //數據庫版本的用處在於，比如，你更新了應用，如果數據庫的設計和之前不一樣了（比如增刪了字段），那麽可以填入一共新版本號，這樣他發現版本
            //更新了遍會調用下面的onUpgrade函數去更新,遷移數據庫
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            //創建表。AUTOINCREMENT是自增，只对整数有用。还有文件名不好做键，可能有重复的；不过设计成不重复的也可以
            //这里有一个表的设计问题，就是要如何设计这个表以保存各个Note在哪个文件夹~。比如在这个表记录，或者在Note的表里记录。后面你写的时候再看一下。
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (\n"
                    + "id INTEGER PRIMARY KEY NOT NULL, "
                    + "name TEXT NOT NULL, "
                    + "note_count INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        }
    }

}
