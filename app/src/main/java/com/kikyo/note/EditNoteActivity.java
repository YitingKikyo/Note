package com.kikyo.note;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.kikyo.note.EditText.CustomEditText;
import com.kikyo.note.module.Note;
import com.kikyo.note.service.NoteService;

/**
 * Created by 婷 on 2017/7/27.
 */

public class EditNoteActivity extends AppCompatActivity {

    //不要把内容标题什么的一起传过來，Intent的extra是有大小限制的，Id就够了
    public static final String EXTRA_NOTE_ID = "note_id";
    public static final String EXTRA_STATUS = "status";
    public static final int STATUS_DELETED = 1;
    public static final String EXTRA_EDIT_TEXT = "edit_text";


    private int id;
    private Note mNote;
    private NoteService mNoteService = NoteService.getInstance();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        getIntentFromMainFragment();
        setUpToolbar();
        setEditText();

    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mNote.getTitle());
        toolbar.setTitleTextAppearance(getApplicationContext(), R.style.MenuItemTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getIntentFromMainFragment() {
        Intent intent = getIntent();
        id = intent.getIntExtra(EXTRA_NOTE_ID, -1);
        mNote = mNoteService.getNoteById(id);
    }

    private void setEditText() {
        TextView dateText = (TextView) findViewById(R.id.edit_date);
        dateText.setText( mNote.getDate());

        CustomEditText editText = (CustomEditText) findViewById(R.id.edit_note);
        editText.setText(mNote.getContent());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.toolbar_delete:
                deleteNote();
                return true;
            case R.id.toolbar_save:
                saveNote();
                return true;
            case R.id.toolbar_undo:
                return true;
            case R.id.toolbar_redo:
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        String text = ((CustomEditText)findViewById(R.id.edit_note)).getText().toString();
        mNote.setContent(text);
        if(mNoteService.updateNote(mNote)){
            setResult(RESULT_OK, new Intent()
            .putExtra(EXTRA_EDIT_TEXT, text));
            Toast.makeText(EditNoteActivity.this, R.string.edit_succeed, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(EditNoteActivity.this, R.string.edit_failed, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void deleteNote() {
        //这里不能像刚才那么写的...怎么说 不要轻易地把变量弄成public的，甚至是static的。。
        //应该是通过NoteServices删掉Note并通知MainFragment删掉这个Note
        //第一种办法，在NoteService那里弄一个OnNoteDeleteListener然后注册和通知
        //第二種方法，通過ActivityResult來獲取筆記是否揹刪除等。
        if(mNoteService.deleteNote(mNote)){
            setResult(RESULT_OK, new Intent()
                .putExtra(EXTRA_STATUS, STATUS_DELETED));
            Toast.makeText(EditNoteActivity.this, R.string.delete_succeed, Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(EditNoteActivity.this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
        }
        //筆記刪除后應該退出編輯界面
        finish();
    }
}


