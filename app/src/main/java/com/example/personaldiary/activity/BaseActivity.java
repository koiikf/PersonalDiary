package com.example.personaldiary.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.personaldiary.HTTPHelper;
import com.example.personaldiary.R;
import com.example.personaldiary.model.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity {

    // Обработка нажатиия на кнопку из нижней панели (получает id кнопки и экран, с которого произошло нажатие)
    protected void onBottomButtonClick(int viewId, Class<?> activity) {
        View button = findViewById(viewId);
        if (button != null) {
            button.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), activity);
                intent.putExtra("user_id", getIntent().getIntExtra("user_id", 0));
                startActivity(intent);
            });
        }
    }

    // Кождируем изображение для последующего отображения на экране
    protected String encodeImage(int id) {
        Bitmap bitmap = ((BitmapDrawable) ((ImageView)(findViewById(id))).getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBytes = stream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    // Выбираем изображение из хранилища телефона
    protected void changeImage(int id, int image) {
        Button changeImage = findViewById(id);
        changeImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, image);
        });
    }

    // Отображаем выбранное фото
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView photoImage = findViewById(requestCode);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                photoImage.setImageBitmap(bitmap);
            } catch (Exception e) {}
        }
    }

    // Метод упрощающий создание всплывающих сообщений (принимает место где нужно отобразить и текст сообщения)
    protected void showMessage(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    // Метод для отображения сообщения об ошибке
    protected void showErrorMessage(Context context) {
        showMessage(context, "Произошла ошибка!");
    }

    // Получение текущего времени в часовм поясе телефона
    protected String setTime() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return now.format(formatter);
        }
        return "";
    }

    // Добавление текстовой информации в поля
    protected void setTextInTextView(int id, String text) {
        TextView textView = findViewById(id);
        textView.setText(text);
    }

    // Отображение заметки на экране
    protected void setNote(String result, int id1, int id2, int image) throws JSONException {
        JSONObject jsonObject = new JSONObject(result);
        setTextInTextView(id1, jsonObject.getString("title"));
        setTextInTextView(id2, jsonObject.getString("text"));
        ImageView userImage = findViewById(image);
        Glide.with(getApplicationContext()).load(jsonObject.getString("photo_path") + "?raw=true").into(userImage);
    }

    // Полуучение и отображение информации о заметке (для экрана просмотра заметки и экрана редактирования заметки)
    public class GetNoteTask extends AsyncTask<String, Void, String> {
        private final int id1, id2, image;
        public GetNoteTask(int id1, int id2, int image) {
            this.id1 = id1;
            this.id2 = id2;
            this.image = image;
        }
        @Override
        protected String doInBackground(String... urls) {
            return HTTPHelper.createConnectionAndReadData(urls[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            try { setNote(result, id1, id2, image); }
            catch (JSONException ex) { showErrorMessage(BaseActivity.this ); }
        }
    }

    // Отображение карточки заметки на экране
    protected View createNoteCard(Note note) {
        // Создаем карточку из шаблона
        LayoutInflater inflater = LayoutInflater.from(this);
        View card = inflater.inflate(R.layout.note_card, null);

        // Заетм в этом шаблоне меняем данные
        TextView cardTitle = card.findViewById(R.id.note_title);
        cardTitle.setText(note.title);

        TextView noteText = card.findViewById(R.id.note_description);
        noteText.setText(
                (note.text.length() > 100)
                        ? note.text.substring(0, 100) + "..."
                        : note.text);

        TextView noteDatetime = card.findViewById(R.id.note_time);
        noteDatetime.setText(note.datetime);

        ImageView imageView = card.findViewById(R.id.note_image);
        Glide.with(this).load(note.photoPath + "?raw=true").into(imageView);

        // При нажатии на карточку заметки открывается экран заметки
        card.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), NoteActivity.class);
            intent.putExtra("user_id", getIntent().getIntExtra("user_id", 0));
            // Сохраняем id карточки для полученя информации о ней
            intent.putExtra("note_id", note.note_id);
            view.getContext().startActivity(intent);});

        return card;
    }

    // Поиск карточек по названию и тектсу
    protected void findByTitle(String text, ArrayList<Note> notes, int id) {
        int count = 0;

        // Удаляем все кароки с экрана
        LinearLayout notesLayout = findViewById(id);
        notesLayout.removeAllViews();
        if (!notes.isEmpty()) {
            // Для всех карточек проверяем если название или текст содержат выбраную последовательность символов, то оттображаем карточку на экране
            for (Note note: notes) {
                if (note.title.toLowerCase().contains(text.toLowerCase()) || note.text.toLowerCase().contains(text.toLowerCase())) {
                    count += 1;
                    notesLayout.addView(createNoteCard(note));
                }
            }
            // Обновляем темы карточек
            setTheme();
        }
        if (count == 0) { showMessage(this, "Записей не найдено!"); }
    }


    // При выборе пункта "Редактрировать" открывается экрна редактирования и сохраняется id выбранной карточки
    protected void edit(View view, int id) {
        Intent intent = new Intent(view.getContext(), EditNoteActivity.class);
        intent.putExtra("user_id", getIntent().getIntExtra("user_id", 0));
        intent.putExtra("note_id", id);
        view.getContext().startActivity(intent);
    }

    // При выборе "Удалить" делаем запрос на удаление карточки и удалем ее с экрана
    protected void delete(View view, int id) {
        JSONObject object = new JSONObject();
        new HTTPHelper.DeleteJsonRequestTask(object).execute(HTTPHelper.baseUrl + "/note/" + id);
        ((ViewManager)view.getParent()).removeView(view);
    }

    // При добавлении карточки в избранные создаем запрос на добавление карточки в избранное
    protected void addFavourity(View view, int id) {
        JSONObject object = new JSONObject();
        try {
            object.put("user_id", getIntent().getIntExtra("user_id", 0));
            object.put("note_id", id);
            new HTTPHelper.PostJsonRequestTask(object).execute(HTTPHelper.baseUrl + "/favourity");

            // Делаем иконку закладки видимой
            ImageView bookmark = view.findViewById(R.id.bookmark);
            bookmark.setVisibility(View.VISIBLE);
        } catch (JSONException e) { showErrorMessage(this); }
    }

    // При удалении карточки из избранного делаем запрос на удаление из избранного
    protected void deleteFavourity(View view, int id) {
        JSONObject object = new JSONObject();
        try {
            object.put("user_id", getIntent().getIntExtra("user_id", 0));
            object.put("note_id", id);
            new HTTPHelper.DeleteJsonRequestTask(object).execute(HTTPHelper.baseUrl + "/favourity");
        } catch (JSONException e) { showErrorMessage(this); }
    }

    // Обработка действий из всплывающего меню
    protected void handleMenuAction(MenuItem menuItem, View view, int id, Class<?> activity) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.menu_edit) { edit(view, id); }
        else if (itemId == R.id.menu_delete) { delete(view, id); }
        else if (itemId == R.id.menu_add_favourity) { addFavourity(view, id); }
        else if (itemId == R.id.menu_delete_favourity) {
            // Если удаляем из избранного на главном экране, то просто скрываем иконку закладки, иначе удаляем карточку с экрана
            deleteFavourity(view, id);
            if (activity == FavourityNotesActivity.class) {
                ((ViewManager)view.getParent()).removeView(view);
            } else {
                ImageView bookmark = view.findViewById(R.id.bookmark);
                bookmark.setVisibility(View.GONE);
            }
        }
    }

    // Отображение менб выбора опреации с карточкой
    protected void showPopupMenu(View view, int id, Class<?> activity) {
        // Получение сохраненной темы прилдожения
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String savedTheme = prefs.getString("app_theme", "default");

        int themeId;
        // Устанавливаем тему из theme.xml
        if (savedTheme.equals("modern")) { themeId = R.style.Modern; }
        else if (savedTheme.equals("dark")) { themeId = R.style.Dark; }
        else if (savedTheme.equals("light")) { themeId = R.style.Light; }
        else if (savedTheme.equals("space")) { themeId = R.style.Space; }
        else { themeId = R.style.BasePopupMenuStyle; }

        // Создаем объект меню с выбранной темой
        Context themedContext = new ContextThemeWrapper(view.getContext(), themeId);
        PopupMenu popupMenu = new PopupMenu(themedContext, view);

        // Меню создается из шаблона
        if (activity == MainActivity.class) {
            popupMenu.getMenuInflater().inflate(R.menu.card_menu, popupMenu.getMenu());
        } else {
            popupMenu.getMenuInflater().inflate(R.menu.card_menu2, popupMenu.getMenu());
        }
        // При нажати на мню обрабатываем выбранный вариант
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            handleMenuAction(menuItem, view, id, activity);
            return true;
        });

        popupMenu.show();
    }


    // При нажатии Enter в поле поиска карточки ищем все карточки с нужным текстом
    protected boolean onKeyPush(View v, int keyCode, KeyEvent event, EditText findText, ArrayList<Note> notes, int id) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            String searchText = findText.getText().toString();
            findByTitle(searchText, notes, id);
            return true;
        }
        return false;
    }

    // обрабочик для полей поиска карточек
    protected void findListener(ArrayList<Note> notes, int id1, int id2) {
        EditText findText = findViewById(id1);
        findText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return onKeyPush(v, keyCode, event, findText, notes, id2);
            }
        });
    }

    // Создание и отображение карточек на экране
    protected void createNotesCards(int id, ArrayList<Note> notes, Class<?> activity) {
        LinearLayout notesLayout = findViewById(id);
        notesLayout.removeAllViews();
        for (Note note: notes) {
            View card = createNoteCard(note);
            card.setOnLongClickListener(v -> {
                showPopupMenu(v, note.note_id, activity);
                return true;
            });
            notesLayout.addView(card);
        }
    }

    // Рекурсивно проходимя по всем элементам на экране и если свойство tag равно first_color или second_color то меняем его цвет
    protected void traverseViews(View view, int color1, int color2, boolean reverse) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (child.getTag() != null) {
                    if (!reverse) {
                        if (child.getTag().toString().equals("first_color")) {
                            checkViewType(child, color1, color2);
                        } else if (child.getTag().toString().equals("second_color")) {
                            checkViewType(child, color2, color1);
                        }
                    } else {
                        if (child.getTag().toString().equals("second_color")) {
                            checkViewType(child, color1, color2);
                        } else if (child.getTag().toString().equals("first_color")) {
                            checkViewType(child, color2, color1);
                        }
                    }
                }
                traverseViews(child, color1, color2, reverse);
            }
        }
    }

    // Проврека на тип элемента при изменении цвета
    @SuppressLint("UseCompatTextViewDrawableApis")
    private void checkViewType(View child, int color1, int color2) {
        // Если это кнорпка выбора, то меняем цвет текста и цвет кружка
        if (child instanceof RadioButton) {
            ((RadioButton) child).setTextColor(getColor(color1));
            ((RadioButton) child).setButtonTintList(ColorStateList.valueOf(getColor(color1)));
        }
        // Если кнопка, то берем шаблон кнопки и меняем цвет заливки и цвет границы
        else if (child instanceof Button) {
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.rounded_corner_view_button2);
            child.setBackground(drawable);
            GradientDrawable shapeDrawable = (GradientDrawable) drawable;
            assert shapeDrawable != null;
            shapeDrawable.setColor(ContextCompat.getColor(this, color1));
            shapeDrawable.setStroke(2, ContextCompat.getColor(this, color2));

            ((Button) child).setTextColor(getColor(color2));
        }
        // Если это поле ввода информации
        else if (child instanceof EditText) {
            EditText et = (EditText) child;

            // Изменяем цвета шаблона (как для кнопки)
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.rounded_corner_view);
            et.setBackground(drawable);
            GradientDrawable shapeDrawable = (GradientDrawable) drawable;
            assert shapeDrawable != null;
            shapeDrawable.setColor(ContextCompat.getColor(this, color1));
            shapeDrawable.setStroke(2, ContextCompat.getColor(this, color2));

            // Устанавливаем цвет текста подсказки, цвет текста и цвет иконки
            et.setHintTextColor(getColor(color2));
            et.setTextColor(getColor(color2));
            et.setCompoundDrawableTintList(ColorStateList.valueOf(getColor(color2)));
        }
        // Если обычный текст, то меняем только цвет текста
        else if (child instanceof TextView) {
            ((TextView) child).setTextColor(getColor(color1));
        }
        // Если карточка, то меняем цвет текста
        else if (child instanceof CardView) {
            ((CardView) child).setCardBackgroundColor(getColor(color1));
        }
        // Если кнопка "+"
        else if (child instanceof FloatingActionButton) {
            // То меняем ее цвет
            child.setBackgroundTintList(ColorStateList.valueOf(getColor(color1)));
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            String savedTheme = prefs.getString("app_theme", "default");

            // В зависимости от темы выбираем нужное изображение
            switch (savedTheme) {
                case "default":
                    ((FloatingActionButton) child).setImageResource(R.drawable.plus_icon);
                    break;
                case "dark":
                    ((FloatingActionButton) child).setImageResource(R.drawable.plus_icon_dark);
                    break;
                case "light":
                    ((FloatingActionButton) child).setImageResource(R.drawable.plus_icon_light);
                    break;
                case "space":
                    ((FloatingActionButton) child).setImageResource(R.drawable.plus_icon_space);
                    break;
                case "modern":
                    ((FloatingActionButton) child).setImageResource(R.drawable.plus_icon_modern);
                    break;
            }
        }
        // Если это изображение (нижние иконки кнопок), то выбираем изображения исходя из выбранной темы приложения
        else if (child instanceof ImageView) {
            if (child.getId() == R.id.notes_image) {
                setBottomBarImages(child, R.drawable.notes_icon_dark, R.drawable.notes_icon_light,
                        R.drawable.notes_icon_space, R.drawable.notes_icon_modern,
                        R.drawable.notes_icon);
            } else if (child.getId() == R.id.settings_image) {
                setBottomBarImages(child, R.drawable.settings_dark, R.drawable.settings_light,
                        R.drawable.settings_space, R.drawable.settings_modern,
                        R.drawable.settings);
            } else if (child.getId() == R.id.profile_image) {
                setBottomBarImages(child, R.drawable.user_icon_dark, R.drawable.user_icon_light,
                        R.drawable.user_icon_space, R.drawable.user_icon_modern,
                        R.drawable.user_icon);
            } else {
                setBottomBarImages(child, R.drawable.bookmark_icon_dark, R.drawable.bookmark_icon_light,
                        R.drawable.bookmark_icon_space, R.drawable.bookmark_icon_modern,
                        R.drawable.bookmark_icon);
            }
        }
        else {
            child.setBackgroundColor(getColor(color1));
        }
    }

    // Устанавливаем тему приложения (выхываем метод для измения цвета всех элементов (находится выше)
    protected void setTheme() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String savedTheme = prefs.getString("app_theme", "default");
        // Получаем корневой элемент
        View rootView = findViewById(R.id.content);
        switch (savedTheme) {
            case "default":
                // Если true, то меняется порядок color1 и color2
                traverseViews(rootView, R.color.popup_background_color, R.color.popup_text_color, true);
                break;
            case "dark":
                traverseViews(rootView, R.color.popup_background_color_dark, R.color.popup_text_color_dark, false);
                break;
            case "light":
                traverseViews(rootView, R.color.popup_background_color_dark, R.color.popup_text_color_dark, true);
                break;
            case "space":
                traverseViews(rootView, R.color.popup_background_color_space, R.color.popup_text_color_space, false);
                break;
            case "modern":
                traverseViews(rootView, R.color.popup_background_color_modern, R.color.popup_text_color_modern, false);
                break;
        }
    }

    // Выбор изображений для нижней панели
    private void setBottomBarImages(View child, int idDark, int idLight, int idSpace, int idModern,
                                    int idBase) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String savedTheme = prefs.getString("app_theme", "default");
        if (savedTheme.equals("dark")) { setImageDrawable(child, idDark); }
        else if (savedTheme.equals("light")) { setImageDrawable(child, idLight); }
        else if  (savedTheme.equals("space")) { setImageDrawable(child, idSpace); }
        else if (savedTheme.equals("modern")) { setImageDrawable(child, idModern); }
        else { setImageDrawable(child, idBase); }
    }

    // Установка самого изображения
    private void setImageDrawable(View child, int id) {
        Drawable drawable = ContextCompat.getDrawable(this, id);
        ((ImageView) child).setImageDrawable(drawable);
    }
}
