package com.kikyo.note.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;

import com.kikyo.note.module.Note;
import com.kikyo.note.module.NoteSQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * Created by 婷 on 2017/7/20.
 */

public class NoteService {

    public static class NoteDeleteEvent {
        public Note note;

        public NoteDeleteEvent(Note note) {
            this.note = note;
        }
    }

    public static class NoteUpdateContentEvent{
        public Note note;

        public NoteUpdateContentEvent(Note note){
            this.note = note;
        }
    }

    public static final String NOTE_TABLE_NAME = "notes";
    private static final String KEY_MAX_ID = NoteService.class.getName() + ".max_id";
    private static NoteService sInstance;
    private List<Note> mNotes = getAllNotes();

    private StorIOSQLite mNoteStor;
    private SharedPreferences mSharedPreferences;


    public NoteService(Context context) {
        mNoteStor = DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(new NoteSQLOpenHelper(context))
                .addTypeMapping(Note.class, new NoteSQLiteTypeMapping())
                .build();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void initInstance(Context context){
        sInstance = new NoteService(context);
    }

    // 改成单粒模式。注意因为他需要一个Context來构造，所以我们要调用一次initInstance才能使用getInstance()
    public static NoteService getInstance() {
        return sInstance;
    }

    public List<Note> getAllNotes(){
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
        if(deleteNoteById(note.getId())){
            //发布一个Note揹删除的事件
            EventBus.getDefault().post(new NoteDeleteEvent(note));
            return true;
        }
        return false;
    }

    private boolean deleteNoteById(Integer id) {
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
        note.setId(getAndIncreaseMaxId());
        return mNoteStor.put()
                .object(note)
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

    public boolean updateNote(Note note){
        if( mNoteStor.put()
                .object(note)
                .prepare()
                .executeAsBlocking()
                .wasUpdated()){
            //发布一个Note揹更新的事件
            EventBus.getDefault().post(new NoteUpdateContentEvent(note));
            return true;
        }
        return false;
    }

    public Note getNoteById(int id) {
        return mNoteStor.get()
                .object(Note.class)
                .withQuery(Query.builder()
                    .table(NOTE_TABLE_NAME)
                    .where("id = ?")
                    .whereArgs(id)
                    .build())
                .prepare()
                .executeAsBlocking();
    }

    public List<Note> filter(List<Note> notes, String query) {
        query = query.toLowerCase();

        final List<Note> filteredModelList = new ArrayList<>();
        for (Note note : notes) {

            final String titleEn = note.getTitle().toLowerCase();
            final String contentEn = note.getContent().toLowerCase();
            final String title = note.getTitle();
            final String content = note.getContent();

            if (titleEn.contains(query) || contentEn.contains(query) || title.contains(query) || content.contains(query)) {

                filteredModelList.add(note);
            }
        }
        return filteredModelList;
    }

    public void animateTo(List<Note> models, RecyclerView r) {
        applyAndAnimateRemovals(models, r);
        applyAndAnimateAdditions(models, r);
        applyAndAnimateMovedItems(models, r);
    }

    private void applyAndAnimateRemovals(List<Note> newModels, RecyclerView r) {
        for (int i = mNotes.size() - 1; i >= 0; i--) {
            final Note model = mNotes.get(i);
            if (!newModels.contains(model)) {
                removeItem(i, r);
            }
        }
    }

     private void applyAndAnimateAdditions(List<Note> newModels, RecyclerView r) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final Note model = newModels.get(i);
            if (!mNotes.contains(model)) {
                addItem(i, model, r);
            }
        }
    }

     private void applyAndAnimateMovedItems(List<Note> newModels, RecyclerView r) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final Note model = newModels.get(toPosition);
            final int fromPosition = mNotes.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition, r);
            }
        }
    }

    public Note removeItem(int position, RecyclerView r) {
        final Note model = mNotes.remove(position);
        r.getAdapter().notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, Note model, RecyclerView r) {
        mNotes.add(position, model);
        r.getAdapter().notifyItemInserted(position);
    }

   public void moveItem(int fromPosition, int toPosition, RecyclerView r) {
       final Note model = mNotes.remove(fromPosition);
        mNotes.add(toPosition, model);
       r.getAdapter().notifyItemMoved(fromPosition, toPosition);
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
