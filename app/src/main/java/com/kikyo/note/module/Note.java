package com.kikyo.note.module;

import java.util.Date;

/**
 * Created by 婷 on 2017/7/19.
 */

public class Note {

    private String mTitle;
    private String mContent;
    private Date mDate;

    public Note(String title, String content, Date date) {
        mTitle = title;
        mContent = content;
        mDate = date;
    }

    public Note(String title, String content) {
        mTitle = title;
        mContent = content;
        mDate = new Date();
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

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }
}
