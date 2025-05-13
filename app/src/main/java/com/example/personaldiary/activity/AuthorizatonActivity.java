package com.example.personaldiary.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import com.example.personaldiary.HTTPHelper;
import com.example.personaldiary.R;
import com.example.personaldiary.SHA256;
import com.example.personaldiary.model.AuthorizationInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AuthorizatonActivity extends BaseActivity {
    protected ArrayList<AuthorizationInfo> authorizationInformation = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.authorization);

        // Открытие экрана регистрации
        Button registrationButton = findViewById(R.id.registration_button);
        registrationButton.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), RegistrationActivity.class);
            view.getContext().startActivity(intent);
        });

        EditText emailInput = findViewById(R.id.email_input);
        EditText passwordInput = findViewById(R.id.password_input);
        TextView errorText = findViewById(R.id.error_text);

        // Установка темы
        setTheme();

        // Получаем пароли и логины пользователей
        new GetAuthorizationTask().execute(HTTPHelper.baseUrl + "/users");

        // При нажатии на кнопку авторизоваться
        Button authorizationButton = findViewById(R.id.authorization_button);
        authorizationButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString();
            // Переводим пароль в хэш для проверки
            String password = SHA256.calculateSHA256(passwordInput.getText().toString());

            boolean findUser = false;
            // Сравниваем логин и пароль со всеми логинами и паролями пользователей
            for (AuthorizationInfo authorizationInfo: authorizationInformation) {
                if (email.equals(authorizationInfo.email) && password.equals(authorizationInfo.password)) {
                    findUser = true;
                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    // Сохраняем id вошедшего в приложение пользователя
                    intent.putExtra("user_id", authorizationInfo.client_id);
                    view.getContext().startActivity(intent);
                    break;
                }
            }
            if (!findUser) { errorText.setText("Неверный логин или пароль!"); }
        });

    }

    // Получение авторизационных данных
    public class GetAuthorizationTask extends AsyncTask<String, Void, String> {
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

                    // Для каждого пользователя получаем его id, почту и пароль
                    int user_id = jsonObject.getInt("user_id");
                    String email = jsonObject.getString("email");
                    String password = jsonObject.getString("password");

                    AuthorizationInfo authorizationInfo = new AuthorizationInfo(user_id, email, password);
                    authorizationInformation.add(authorizationInfo);
                }
            } catch (JSONException e) { showErrorMessage(AuthorizatonActivity.this ); }
        }
    }
}