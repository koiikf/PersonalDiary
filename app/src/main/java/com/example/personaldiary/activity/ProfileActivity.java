package com.example.personaldiary.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.personaldiary.HTTPHelper;
import com.example.personaldiary.R;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        onBottomButtonClick(R.id.notes_button, MainActivity.class);
        onBottomButtonClick(R.id.favourites_button, FavourityNotesActivity.class);
        onBottomButtonClick(R.id.settings_button, SettingActivity.class);

        changeImage(R.id.add_photo_button2, R.id.profile_user_image);

        setTheme();

        // обновление информации профиля
        Button saveProfile = findViewById(R.id.save_profile_button);
        saveProfile.setOnClickListener(view -> {
            EditText nikInput = findViewById(R.id.nik_input);

            String nik = nikInput.getText().toString();

            JSONObject userInformation = new JSONObject();
            try {
                userInformation.put("nik", nik);
                userInformation.put("image", encodeImage(R.id.profile_user_image));
                userInformation.put("id", getIntent().getIntExtra("user_id", 0));

                new HTTPHelper.PatchJsonRequestTask(userInformation).execute(HTTPHelper.baseUrl + "/user");
                showMessage(this, "Профиль обновлен!");
            } catch (Exception e) { showErrorMessage(this); }
        });

        new GetUserTask().execute(HTTPHelper.baseUrl + "/users/" + getIntent().getIntExtra("user_id", 0));
    }

    // Получение данных о пользоваиеля
    private class GetUserTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return HTTPHelper.createConnectionAndReadData(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);

                if (!jsonObject.getString("nik").equals("null")) {
                    setTextInEditText(R.id.nik_input, jsonObject.getString("nik"));
                }

                setTextInEditText(R.id.profile_email_input, jsonObject.getString("email"));

                if (!jsonObject.getString("photo_path").equals("null")) {
                    ImageView userImage = findViewById(R.id.profile_user_image);
                    Glide.with(getApplicationContext()).load(jsonObject.getString("photo_path") + "?raw=true").into(userImage);
                }
            } catch (JSONException ex) { showErrorMessage(ProfileActivity.this ); }
        }
    }

    private void setTextInEditText(int id, String text) {
        try {
            EditText editText = findViewById(id);
            editText.setText(text);
        } catch (Exception ex) { showErrorMessage(this); }
    }
}