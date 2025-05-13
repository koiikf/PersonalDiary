package com.example.personaldiary.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.personaldiary.HTTPHelper;
import com.example.personaldiary.R;
import com.example.personaldiary.model.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FavourityNotesActivity extends BaseActivity {
    protected ArrayList<Note> notes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourity_notes);

        findListener(notes, R.id.search_note_input, R.id.notes);

        onBottomButtonClick(R.id.notes_button, MainActivity.class);
        onBottomButtonClick(R.id.settings_button, SettingActivity.class);
        onBottomButtonClick(R.id.profile_button, ProfileActivity.class);

        // При нажатии на кнопку добавления заметки открываем экрна добавления заметки
        FloatingActionButton newNoteButton = findViewById(R.id.new_note_button);
        newNoteButton.setColorFilter(Color.rgb(236, 219, 204));
        newNoteButton.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), NewNoteActivity.class);
            intent.putExtra("user_id", getIntent().getIntExtra("user_id", 0));
            // Если нажади на экране избрнанных заметок (чтобы новая заметка сразу стала избранной)
            intent.putExtra("is_favourity", true);
            view.getContext().startActivity(intent);
        });

        new GetFavourityNotesTask().execute(HTTPHelper.baseUrl + "/favourity/" + getIntent().getIntExtra("user_id", 0));
    }

    // Получение списка избранных заметок
    public class GetFavourityNotesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return HTTPHelper.createConnectionAndReadData(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    notes.add(HTTPHelper.createNote(jsonObject));
                }
                createNotesCards(R.id.notes, notes, FavourityNotesActivity.class);
                // После отображения меняем их цвет в соответствии с избранным
                setTheme();
            } catch (JSONException e) { showErrorMessage(FavourityNotesActivity.this); }
        }
    }
}