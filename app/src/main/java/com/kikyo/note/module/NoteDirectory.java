package com.kikyo.note.module;

import com.kikyo.note.service.NoteDirectoryService;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;

/**
 * Created by 婷 on 2017/7/22.
 */
@StorIOSQLiteType(table = NoteDirectoryService.TABLE_NAME)

public class NoteDirectory {

    @StorIOSQLiteColumn(name = "id", key = true)
    int mId;

    @StorIOSQLiteColumn(name = "name")
    String mName;

    @StorIOSQLiteColumn(name = "note_count")
    Integer mNoteCount;

    public NoteDirectory(){}

    public NoteDirectory(String name, int num){
        mName = name;
        mNoteCount = num;
    }

    public void setName(String name) {
        this.mName = name;
    }
    public String getName() {
        return mName;
    }


    public void setNoteCount(int noteCount) {
        this.mNoteCount = noteCount;
    }

    public int getNoteCount() {
        return mNoteCount;
    }

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }
}
