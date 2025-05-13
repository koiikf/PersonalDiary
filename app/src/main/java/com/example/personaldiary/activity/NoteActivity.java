package com.example.personaldiary.activity;

import android.os.Bundle;

import com.example.personaldiary.HTTPHelper;
import com.example.personaldiary.R;

public class NoteActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);

        onBottomButtonClick(R.id.notes_button, MainActivity.class);
        onBottomButtonClick(R.id.favourites_button, FavourityNotesActivity.class);
        onBottomButtonClick(R.id.settings_button, SettingActivity.class);
        onBottomButtonClick(R.id.profile_button, ProfileActivity.class);

        setTheme();

        new GetNoteTask(R.id.note_title_text, R.id.note_text, R.id.note_photo)
                .execute(HTTPHelper.baseUrl + "/note/" + getIntent().getIntExtra("note_id", 0));
    }
}
