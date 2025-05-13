package com.example.personaldiary.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.personaldiary.HTTPHelper;
import com.example.personaldiary.R;

import org.json.JSONObject;

public class EditNoteActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);

        // Установка обработчиков нажатия на нижние кнопки
        onBottomButtonClick(R.id.notes_button, MainActivity.class);
        onBottomButtonClick(R.id.favourites_button, FavourityNotesActivity.class);
        onBottomButtonClick(R.id.settings_button, SettingActivity.class);
        onBottomButtonClick(R.id.profile_button, ProfileActivity.class);

        // Выбор фото
        changeImage(R.id.add_photo_button, R.id.note_photo);

        // Установка темы
        setTheme();

        // Сохранение введнных данных
        Button saveNoteButton = findViewById(R.id.save_note_button);
        saveNoteButton.setOnClickListener(view -> {
            EditText titleInput = findViewById(R.id.title_input);
            EditText descriptionInput = findViewById(R.id.description_input);

            String title = titleInput.getText().toString();
            String text = descriptionInput.getText().toString();

            if (!title.isEmpty() && !text.isEmpty()) {
                JSONObject noteInformation = new JSONObject();
                try {
                    noteInformation.put("title", title);
                    noteInformation.put("text", text);
                    noteInformation.put("datetime", setTime());
                    noteInformation.put("image", encodeImage(R.id.note_photo));
                    noteInformation.put("id", getIntent().getIntExtra("note_id", 0));

                    new HTTPHelper.PatchJsonRequestTask(noteInformation).execute(HTTPHelper.baseUrl + "/note");
                    showMessage(this, "Заметка успешно изменена!");
                } catch (Exception e) { showErrorMessage(this); }
            } else { showMessage(this, "Заполните все поля!"); }
        });

        // Получение данных о исходной заметке
        new GetNoteTask(R.id.title_input, R.id.description_input, R.id.note_photo)
                .execute(HTTPHelper.baseUrl + "/note/" + getIntent().getIntExtra("note_id", 0));
    }
}
