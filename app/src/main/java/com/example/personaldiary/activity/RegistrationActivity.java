package com.example.personaldiary.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.personaldiary.HTTPHelper;
import com.example.personaldiary.R;
import com.example.personaldiary.SHA256;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends BaseActivity {
    // Регулярное выражение для проверки корректности введенной почты
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.registration);

        EditText emailInput = findViewById(R.id.email_input);
        EditText passwordInput = findViewById(R.id.password_input);

        setTheme();

        // При нажатии на кнопку зарегестрироваться
        Button registrationButton = findViewById(R.id.registration_button);
        registrationButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            // Если ввели и логин и пароль
            if (!email.isEmpty() && !password.isEmpty()) {
                // Если пароль состоит минимум из 8 символов
                if (password.length() >= 8) {
                    Matcher matcher = EMAIL_PATTERN.matcher(email);
                    // Если почта ввелдена в верном формате
                    if (matcher.matches()) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("email", email);
                            jsonObject.put("password", password);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        PostUserRequestTask task = new PostUserRequestTask(jsonObject);
                        task.execute(HTTPHelper.baseUrl + "/user");
                    } else { showMessage(this, "Почта введена в неверном формате!"); }
                } else { showMessage(this, "Пароль должен содержать минимум 8 символов!"); }
            } else { showMessage(this, "Заполните все поля!"); }
        });

        // Переход на экран авторизации
        Button authorizationButton = findViewById(R.id.authorization_button);
        authorizationButton.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), AuthorizatonActivity.class);
            view.getContext().startActivity(intent);
        });
    }

    // Добавление нового пользователся в систему
    public class PostUserRequestTask extends HTTPHelper.PostJsonRequestTask {
        public PostUserRequestTask(JSONObject jsonBody) { super(jsonBody); }
        @Override
        protected void onPostExecute(String result) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(result);
                if (jsonObject.has("message")) { showMessage(RegistrationActivity.this, "Пользователь с такой почтой уже существует!"); }
                else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("user_id", jsonObject.getInt("id"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                }
            } catch (Exception e) { showErrorMessage(RegistrationActivity.this); }
        }
    }
}