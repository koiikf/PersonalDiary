package com.example.personaldiary.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.example.personaldiary.R;

public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        onBottomButtonClick(R.id.notes_button, MainActivity.class);
        onBottomButtonClick(R.id.favourites_button, FavourityNotesActivity.class);
        onBottomButtonClick(R.id.profile_button, ProfileActivity.class);

        setRadioButtonListener(R.id.default_theme);
        setRadioButtonListener(R.id.dark_theme);
        setRadioButtonListener(R.id.light_theme);
        setRadioButtonListener(R.id.space_theme);
        setRadioButtonListener(R.id.modern_theme);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String savedTheme = prefs.getString("app_theme", "");
        if (savedTheme.isEmpty()) {
            prefs.edit().putString("app_theme", "default").apply();
        }
        setTheme();
    }

    // Сохранение темы при нажатии на кнопку
    private void clickRadio(View view) {
        View rootView = findViewById(R.id.content);
        RadioButton rb = (RadioButton)view;
        if (rb.getId() == R.id.default_theme) {
            saveTheme("default");
            traverseViews(rootView, R.color.popup_background_color, R.color.popup_text_color, true);
        } else if (rb.getId() == R.id.dark_theme) {
            saveTheme("dark");
            traverseViews(rootView, R.color.popup_background_color_dark, R.color.popup_text_color_dark, false);
        } else if (rb.getId() == R.id.light_theme) {
            saveTheme("light");
            traverseViews(rootView, R.color.popup_background_color_dark, R.color.popup_text_color_dark, true);
        } else if (rb.getId() == R.id.space_theme) {
            saveTheme("space");
            traverseViews(rootView, R.color.popup_background_color_space, R.color.popup_text_color_space, false);
        } else if (rb.getId() == R.id.modern_theme) {
            saveTheme("modern");
            traverseViews(rootView, R.color.popup_background_color_modern, R.color.popup_text_color_modern, false);
        }
    }

    private void setRadioButtonListener(int id) {
        RadioButton theme = findViewById(id);
        theme.setOnClickListener(this::clickRadio);
    }

    private void saveTheme(String themeKey) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putString("app_theme", themeKey).apply();
    }
}