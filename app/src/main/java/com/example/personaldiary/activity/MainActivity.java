package com.example.personaldiary.activity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.personaldiary.HTTPHelper;
import com.example.personaldiary.R;
import com.example.personaldiary.model.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends BaseActivity {
    protected ArrayList<Note> notes = new ArrayList<>();
    protected ArrayList<Note> favourityNotes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findListener(notes, R.id.search_note_input, R.id.notes);

        onBottomButtonClick(R.id.favourites_button, FavourityNotesActivity.class);
        onBottomButtonClick(R.id.settings_button, SettingActivity.class);
        onBottomButtonClick(R.id.profile_button, ProfileActivity.class);

        // При нажатии на кнопку добавления открываем экран добавления заметки
        FloatingActionButton newNoteButton = findViewById(R.id.new_note_button);
        newNoteButton.setColorFilter(Color.rgb(236, 219, 204));
        onBottomButtonClick(R.id.new_note_button, NewNoteActivity.class);

        new GetFavourityNotesTask().execute(HTTPHelper.baseUrl + "/favourity/" + getIntent().getIntExtra("user_id", 0));
    }

    // Получение всех заметок
    public class GetNotesTask extends AsyncTask<String, Void, String> {
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
                createNotesCards(R.id.notes, notes, MainActivity.class);
                setTheme();
            } catch (JSONException e) { showErrorMessage(MainActivity.this); }
        }
    }

    // Получение избранных заметок
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
                    favourityNotes.add(HTTPHelper.createNote(jsonObject));
                }
                new GetNotesTask().execute(HTTPHelper.baseUrl + "/notes/" + getIntent().getIntExtra("user_id", 0));
            } catch (JSONException e) { showErrorMessage(MainActivity.this); }
        }
    }

    // Создаем карточки
    protected void createNotesCards(int id, ArrayList<Note> notes, Class<?> activity) {
        LinearLayout notesLayout = findViewById(id);
        notesLayout.removeAllViews();
        for (Note note: notes) {
            View card = createNoteCard(note);
            boolean isFavourity = false;
            for (Note n: favourityNotes) {
                if (note.note_id == n.note_id) {
                    isFavourity = true;
                    break;
                }
            }

            // Если не избранная, то скрываем иконку избранного
            if (!isFavourity) {
                ImageView bookmark = card.findViewById(R.id.bookmark);
                bookmark.setVisibility(View.GONE);
            }
            card.setOnLongClickListener(v -> {
                showPopupMenu(v, note.note_id, activity);
                return true;
            });
            notesLayout.addView(card);
        }
    }
}
