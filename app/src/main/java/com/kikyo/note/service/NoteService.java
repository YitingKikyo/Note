package com.kikyo.note.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kikyo.note.module.Note;
import com.kikyo.note.module.NoteSQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;

import java.util.List;

/**
 * Created by 婷 on 2017/7/20.
 */

public class NoteService {


    public static final String NOTE_TABLE_NAME = "notes";

    //这样就可以了。虽然就是我一直在打。不过你应该可以看懂把？其实只是API的搬运而已，就是看github上这个库怎么用，然后照着用
    private StorIOSQLite mNoteStor;

    public NoteService(Context context) {
        mNoteStor = DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(new NoteSQLOpenHelper(context))
                .addTypeMapping(Note.class, new NoteSQLiteTypeMapping())
                .build();
    }

    public List<Note> getAllNotss(){
        return mNoteStor.get()
                .listOfObjects(Note.class)
                .withQuery(Query.builder()
                        .table(NOTE_TABLE_NAME)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    public List<Note> searchNoteByTitle(String title){
        return mNoteStor.get()
                .listOfObjects(Note.class)
                .withQuery(Query.builder()
                        .table(NOTE_TABLE_NAME)
                        .where("title LIKE ?")
                        .whereArgs("%" + title + "%")
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    public List<Note> searchNoteByContent(String content){
        return mNoteStor.get()
                .listOfObjects(Note.class)
                .withQuery(Query.builder()
                        .table(NOTE_TABLE_NAME)
                        .where("content LIKE ?")
                        .whereArgs("%" + content + "%")
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    public List<Note> searchNote(String keywords){
        keywords = "%" + keywords + "%";
        return mNoteStor.get()
                .listOfObjects(Note.class)
                .withQuery(Query.builder()
                        .table(NOTE_TABLE_NAME)
                        .where("title LIKE ? OR content LIKE ? OR date LIKE ?")
                        .whereArgs(keywords, keywords, keywords)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    public boolean deleteNote(Note note){
        return deleteNoteById(note.getId());
    }

    public boolean deleteNoteById(Integer id) {
        return mNoteStor.delete()
                .byQuery(DeleteQuery.builder()
                        .table(NOTE_TABLE_NAME)
                        .where("id = ?")
                        .whereArgs(id)
                        .build())
                .prepare()
                .executeAsBlocking()
                .numberOfRowsDeleted() > 0;
    }

    public boolean insertNote(Note note){
        return mNoteStor.put()
                .object(note)
                .prepare()
                .executeAsBlocking()
                .wasInserted();
    }

    public boolean updateNote(Note note){
        return mNoteStor.put()
                .object(note)
                .prepare()
                .executeAsBlocking()
                .wasUpdated();
    }


    //這是一個SQLite數據庫的輔助類。如果我們不用第三方庫的話，直接用android内置的api來讀寫SQL的話，就會用這個類去打開數據庫連接。現在我們用的這個庫要求 我們提供這個類。
    private class NoteSQLOpenHelper extends SQLiteOpenHelper {

        public NoteSQLOpenHelper(Context context) {
            //參數分別是Context，表名，CursorFactory(不管他）,和數據庫版本
            super(context, NOTE_TABLE_NAME, null, 1);
            //數據庫版本的用處在於，比如，你更新了應用，如果數據庫的設計和之前不一樣了（比如增刪了字段），那麽可以填入一共新版本號，這樣他發現版本
            //更新了遍會調用下面的onUpgrade函數去更新,遷移數據庫
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            //創建表
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS notes (\n"
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "title TEXT, "
                    + "content TEXT, "
                    + "date TEXT "
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }
}