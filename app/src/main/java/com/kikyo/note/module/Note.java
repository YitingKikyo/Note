package com.kikyo.note.module;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by 婷 on 2017/7/19.
 */
@StorIOSQLiteType(table = "notes")
public class Note {
    //对了 有时候我们看一共类有那些函数 以及函数的用法的话 可以直接按住ctrl键点击，比如~gg还是看不了代码和文档，下次应该就可以了，先不管他

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

    @StorIOSQLiteColumn(name = "id", key = true, ignoreNull = true)
    Integer mId;

    @StorIOSQLiteColumn(name = "title")
     String mTitle;
    @StorIOSQLiteColumn(name = "content")
     String mContent;
    @StorIOSQLiteColumn(name = "date")
     String mDate;

    public Note() {
    }

    //因为我们用字符串表示日期时间的话，就要指定一种日期时间格式了
    public Note(String title, String content, Date date) {
        mTitle = title;
        mContent = content;
        mDate = DATE_FORMAT.format(date);
    }

    public Note(String title, String content) {
       this(title, content, new Date());
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }
}
