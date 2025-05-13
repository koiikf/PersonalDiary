package com.example.personaldiary.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.personaldiary.HTTPHelper;
import com.example.personaldiary.R;

import org.json.JSONException;
import org.json.JSONObject;

public class NewNoteActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_note);

        onBottomButtonClick(R.id.notes_button, MainActivity.class);
        onBottomButtonClick(R.id.favourites_button, FavourityNotesActivity.class);
        onBottomButtonClick(R.id.settings_button, SettingActivity.class);
        onBottomButtonClick(R.id.profile_button, ProfileActivity.class);

        changeImage(R.id.add_photo_button, R.id.note_photo);

        setTheme();

        Button saveNoteButton = findViewById(R.id.save_note_button);
        saveNoteButton.setOnClickListener(view -> {
            EditText titleInput = findViewById(R.id.title_input);
            EditText descriptionInput = findViewById(R.id.description_input);

            String title = titleInput.getText().toString();
            String text = descriptionInput.getText().toString();

            if (!title.isEmpty() && !text.isEmpty()) {
                JSONObject noteInformation = new JSONObject();
                try {
                    // Добавление новой заметки
                    noteInformation.put("title", title);
                    noteInformation.put("text", text);
                    noteInformation.put("datetime", setTime());
                    noteInformation.put("image", encodeImage(R.id.note_photo));
                    noteInformation.put("author_id", getIntent().getIntExtra("user_id", 0));

                    showMessage(this, "Запись успешно добавлена!");

                    HTTPHelper.PostJsonRequestTask task = new HTTPHelper.PostJsonRequestTask(noteInformation);
                    task.execute(HTTPHelper.baseUrl + "/note");

                    // Если открыли экран добавления с экрана избранных, то дополнительно делаем заметку избранной
                    if (getIntent().getBooleanExtra("is_favourity", false)) {
                        String result = task.get();
                        JSONObject jsonObj = new JSONObject(result);
                        if (jsonObj != null) {
                            int id = jsonObj.getInt("id");
                            JSONObject object = new JSONObject();
                            try {
                                object.put("user_id", getIntent().getIntExtra("user_id", 0));
                                object.put("note_id", id);
                                new HTTPHelper.PostJsonRequestTask(object).execute(HTTPHelper.baseUrl + "/favourity");
                            } catch (JSONException e) { showErrorMessage(this); }
                        }
                    }
                } catch (Exception e) { showErrorMessage(this); }
            } else { showMessage(this,"Заполните все поля!"); }
        });
    }
}