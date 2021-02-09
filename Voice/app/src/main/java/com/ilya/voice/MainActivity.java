package com.ilya.voice;

import android.Manifest;
import android.app.Notification;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecognitionListener, PhrasesFragment.onSomeEventListenerMain {
    //переменные
    EditText editText_text_to_speech;
    Button button_text_to_speech, button_select_language, button_pause, button_to_settings;
    Button button_wave;
//    Button button_fast_word_1, button_fast_word_2, button_fast_word_3, button_fast_word_4, button_fast_word_5;
    public String[] fast_words = new String[10];
    public String[] keywords = new String[10];
    SQLWords sqlWords;
    LinearLayout container_journal, main_layout;
    ConstraintLayout main_container;
    ScrollView scrollView;
    View tableRow;
    TextView textView_my_message, textView_outside_message, textView_time_separator,
            textView_partial_result;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    public static boolean readText = false;
    public static String DATE = "", TIME = "", DEAD_DATE = "";
    public static final String DATE_FORMAT = "yyyy/MM/dd", TIME_FORMAT = "H:mm";
    public static boolean BEGIN_SESSION = true;
    public static long LAST_MILLIS = 0;
    public static Calendar CALENDAR_MILLIS;
    TextToSpeech textToSpeech;
    SQLJournal sqlJournal;
    SQLiteDatabase db;
    Cursor cursor;
    ContentValues addRow;
    public String[] columns_journal = {"_ID",
            SQLJournal.COLUMN_WHAT_IS_IT,
            SQLJournal.COLUMN_DATE,
            SQLJournal.COLUMN_CONTENT};
    public String[] columns_words = {"_ID",
            SQLWords.COLUMN_WHAT_IS_IT,
            SQLWords.COLUMN_WORD,
            SQLWords.COLUMN_RATING};
    SharedPreferences sharedPreferences = null;
    public static float PITCH = 1.3f;
    public static float SPEECH_RATE = 0.7f;
    public static int TEXT_SIZE = 10;
    public static boolean VIBRO_AT_LOAD_SOUND = false;
    public static boolean VIBRO_AFTER_PAUSE = false;
    public static boolean KEYWORDS = false;
    public static boolean VOICING_EMOTICONS = false;

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";

    public static boolean HOME_LANGUAGE = false;
    public static String SECOND_LANGUAGE = "";

    public static boolean PAUSE = false;
    public static int countRmsChanged = 0;

    GestureDetector gestureDetector;
    public static final int SWIPE_MIN_DISTANCE = 120;
    public static final int SWIPE_THRESHOLD_VELOCITY = 200;
    PhrasesFragment phrasesFragment;
    public static boolean OPEN_FRAGMENT = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String requiredPermission = Manifest.permission.RECORD_AUDIO;
                if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{requiredPermission}, 101);
                }
            }
            gestureDetector = new GestureDetector(new GestureListener());

            speech = SpeechRecognizer.createSpeechRecognizer(getApplicationContext(),
                    ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/com.google.android.voicesearch.serviceapi.GoogleRecognitionService"));
            speech.setRecognitionListener(this);
            recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
//        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,Locale.getDefault());
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, new Long(3000));
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.forLanguageTag("de-DE").toString());

            sharedPreferences = getSharedPreferences(SettingsActivity.PATH_TO_SETTINGS, MODE_PRIVATE);
            sqlJournal = new SQLJournal(this, sqlJournal.NAME_TABLE, null,
                    sqlJournal.VERSION_TABLE);
            sqlWords = new SQLWords(this, SQLWords.NAME_TABLE, null,
                    SQLWords.VERSION_TABLE);
            button_text_to_speech = (Button) findViewById(R.id.btn_text_to_speech);
            button_select_language = (Button) findViewById(R.id.btn_select_language);
            button_pause = (Button) findViewById(R.id.btn_pause);
            button_to_settings = (Button) findViewById(R.id.btn_to_settings);
            button_wave = (Button)findViewById(R.id.btn_wave);
//        button_fast_word_1 = (Button) findViewById(R.id.btn_fast_word1);
//        button_fast_word_2 = (Button) findViewById(R.id.btn_fast_word2);
//        button_fast_word_3 = (Button) findViewById(R.id.btn_fast_word3);
//        button_fast_word_4 = (Button) findViewById(R.id.btn_fast_word4);
//        button_fast_word_5 = (Button) findViewById(R.id.btn_fast_word5);
            editText_text_to_speech = (EditText) findViewById(R.id.et_text_to_speech);
            textView_partial_result = (TextView) findViewById(R.id.tv_my_partial_result);
            textView_partial_result.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0));
            container_journal = (LinearLayout) findViewById(R.id.container_journal);
            main_container = (ConstraintLayout) findViewById(R.id.main_container);
            main_layout = (LinearLayout) findViewById(R.id.main_layout);
            scrollView = (ScrollView) findViewById(R.id.sv_journal);
            scrollView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
            //устанавливаю текущую дату и время сеанса
            setDateAndTime();
            removeOldJournal();
//        fillFastWords();

            main_layout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });

            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int initStatus) {
                    if (initStatus == TextToSpeech.SUCCESS) {
                        if (textToSpeech.isLanguageAvailable(new Locale(Locale.getDefault().getLanguage()))
                                == TextToSpeech.LANG_AVAILABLE) {
                            textToSpeech.setLanguage(new Locale(Locale.getDefault().getLanguage()));
                        } else {
                            textToSpeech.setLanguage(Locale.US);
                        }
                        textToSpeech.setPitch(PITCH);
                        textToSpeech.setSpeechRate(SPEECH_RATE);
                        readText = true;
                    } else if (initStatus == TextToSpeech.ERROR) {
                        Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                        readText = false;
                    }
                }
            });

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.btn_text_to_speech:
                            String text = editText_text_to_speech.getText().toString();
                            speakText(text);
                            break;
                        case R.id.btn_to_settings:
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                            break;
//                    case R.id.btn_fast_word1:
//                        fastWord(fast_words[fast_words.length - 1]);
//                        break;
//                    case R.id.btn_fast_word2:
//                        fastWord(fast_words[fast_words.length - 2]);
//                        break;
//                    case R.id.btn_fast_word3:
//                        fastWord(fast_words[fast_words.length - 3]);
//                        break;
//                    case R.id.btn_fast_word4:
//                        fastWord(fast_words[fast_words.length - 4]);
//                        break;
//                    case R.id.btn_fast_word5:
//                        fastWord(fast_words[fast_words.length - 5]);
//                        break;
                        case R.id.btn_select_language:
                            if (HOME_LANGUAGE) {
                                HOME_LANGUAGE = false;
                            } else {
                                HOME_LANGUAGE = true;
                            }
                            selectLanguage();
                            speech.startListening(recognizerIntent);
                            break;
                        case R.id.btn_pause:
                            if (PAUSE) {
                                //показываю всё то, что не показывал

                                PAUSE = false;
                            } else {
                                PAUSE = true;
                            }
                            break;
                    }
                }
            };
            button_text_to_speech.setOnClickListener(listener);
            button_select_language.setOnClickListener(listener);
            button_pause.setOnClickListener(listener);
            button_to_settings.setOnClickListener(listener);
//        button_fast_word_1.setOnClickListener(listener);
//        button_fast_word_2.setOnClickListener(listener);
//        button_fast_word_3.setOnClickListener(listener);
//        button_fast_word_4.setOnClickListener(listener);
//        button_fast_word_5.setOnClickListener(listener);

            button_select_language.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (HOME_LANGUAGE) {
                        HOME_LANGUAGE = false;
                    } else {
                        HOME_LANGUAGE = true;
                    }
                    Intent intent = new Intent(MainActivity.this, LanguageActivity.class);
                    startActivity(intent);
                    return false;
                }
            });

        }catch (Exception e){}
    }

    @Override
    protected void onResume() {
        super.onResume();
        //применяю настройки
        loadSettings();
        //заполняю прошедшим
        fillJournal();

        fillKeyWords();

        selectLanguage();
        speech.startListening(recognizerIntent);

        if(OPEN_FRAGMENT){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, phrasesFragment).commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        saveFastWords();
        if (speech != null) {
            speech.stopListening();
            Log.i(LOG_TAG, "destroy");
        }
    }

    //спускаю вниз после вызова клавиатуры
    private int mLastContentHeight = 0;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int currentContentHeight = findViewById(Window.ID_ANDROID_CONTENT).getHeight();

            if (mLastContentHeight > currentContentHeight + 100) {
                if (scrollView.getBottom() <= scrollView.getScrollY()) {
                    scrollViewDown();
                }
                mLastContentHeight = currentContentHeight;
            } else if (currentContentHeight > mLastContentHeight + 100) {
                mLastContentHeight = currentContentHeight;
            }
        }
    };

    public void setDateAndTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
        DATE = dateFormat.format(c.getTime());
        TIME = timeFormat.format(c.getTime());
        switch (sharedPreferences.getInt(SettingsActivity.SETTINGS_STORE_DAYS, 4)) {
            case 0:
                c.add(Calendar.DAY_OF_YEAR, -1);
                break;
            case 1:
                c.add(Calendar.DAY_OF_YEAR, -3);
                break;
            case 2:
                c.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case 3:
                c.add(Calendar.DAY_OF_YEAR, -30);
                break;
            case 4:
                DEAD_DATE = "NO";
                break;
        }
        DEAD_DATE = dateFormat.format(c.getTime());
    }

    public void speakText(String text){
        speech.stopListening();
        try {
            if (!text.equals("")) {
                String utteranceId = this.hashCode() + "";
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                editText_text_to_speech.setText("");
//                                voicingEmoticons(text);
                addMyMessage(text);
//                wordsRating(text);
            }
        } catch (Exception e) {
        }
    }

    public void fillJournal() {
        try {
            container_journal.removeAllViews();
            db = sqlJournal.getReadableDatabase();
            /**потом для быстроты убрать все лишние столбцы, сейчас это ID и DATE*/
            cursor = db.query(sqlJournal.NAME_TABLE, columns_journal, null, null,
                    null, null, columns_journal[2]);
            int index_id = cursor.getColumnIndex("_ID");
            int index_what_is_it = cursor.getColumnIndex(columns_journal[1]);
            int index_date = cursor.getColumnIndex(columns_journal[2]);
            int index_content = cursor.getColumnIndex(columns_journal[3]);
            while (cursor.moveToNext()) {
                int what_is_it = cursor.getInt(index_what_is_it);
                String date = cursor.getString(index_date);
                String content = cursor.getString(index_content);
                LayoutInflater layoutInflater = LayoutInflater.from(this);
                switch (what_is_it) {
                    case 0:
                        tableRow = layoutInflater.inflate(R.layout.layout_time_separator, null);
                        textView_time_separator = (TextView) tableRow.findViewById(R.id.tv_time_separator);
                        textView_time_separator.setText(content);
                        textView_time_separator.setTextSize(TEXT_SIZE);
                        break;
                    case 1:
                        tableRow = layoutInflater.inflate(R.layout.layout_my_message, null);
                        textView_my_message = (TextView) tableRow.findViewById(R.id.tv_my_message);
                        textView_my_message.setText(content);
                        textView_my_message.setTextSize(TEXT_SIZE);
                        break;
                    case 2:
                        tableRow = layoutInflater.inflate(R.layout.layout_outside_message, null);
                        textView_outside_message = (TextView) tableRow.findViewById(R.id.tv_outside_message);
                        textView_outside_message.setText(content);
                        textView_outside_message.setTextSize(TEXT_SIZE);
                        break;
                }
                container_journal.addView(tableRow);
            }
            cursor.close();
            db.close();
            scrollViewDown();
        } catch (Exception e) {
        }
    }

    public void fillKeyWords() {
        if (KEYWORDS) {
            db = sqlWords.getReadableDatabase();
            cursor = db.query(sqlWords.NAME_TABLE, columns_words,
                    SQLWords.COLUMN_WHAT_IS_IT + " = ?", new String[]{"1"},null,
                    null, null);
            int index_id = cursor.getColumnIndex(columns_words[0]);
            int index_what_it_is = cursor.getColumnIndex(columns_words[1]);
            int index_word = cursor.getColumnIndex(columns_words[2]);
            int index_rating = cursor.getColumnIndex(columns_words[3]);
            for (int i = 0; i < keywords.length; i++) {
                if (cursor.moveToNext()) {
                    keywords[i] = cursor.getString(index_word);
                    //без учета регистра первой буквы
                    keywords[i] = keywords[i].substring(1);
                } else {
                    keywords[i] = "";
                }
            }
            cursor.close();
            db.close();
        }
    }

    public void addMyMessage(String content) {
        try {
            db = sqlJournal.getWritableDatabase();
            if (BEGIN_SESSION) {
                addTimeSeparator();
                BEGIN_SESSION = false;
            }
            addRow = new ContentValues();
            addRow.put(columns_journal[1], 1);
            addRow.put(columns_journal[2], DATE);
            addRow.put(columns_journal[3], content);
            db.insert(sqlJournal.NAME_TABLE, null, addRow);

            LayoutInflater layoutInflater = LayoutInflater.from(this);
            tableRow = layoutInflater.inflate(R.layout.layout_my_message, null);
            textView_my_message = (TextView) tableRow.findViewById(R.id.tv_my_message);
            textView_my_message.setText(content);
            textView_my_message.setTextSize(TEXT_SIZE);
            container_journal.addView(tableRow);
            scrollViewDown();
        } catch (Exception e) {
        }
    }

    public void addOutsideMessage(String content) {
        db = sqlJournal.getWritableDatabase();
        if (BEGIN_SESSION) {
            addTimeSeparator();
            BEGIN_SESSION = false;
        }
        addRow = new ContentValues();
        addRow.put(columns_journal[1], 2);
        addRow.put(columns_journal[2], DATE);
        addRow.put(columns_journal[3], content);
        db.insert(sqlJournal.NAME_TABLE, null, addRow);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        tableRow = layoutInflater.inflate(R.layout.layout_outside_message, null);
        textView_outside_message = (TextView) tableRow.findViewById(R.id.tv_outside_message);
        textView_outside_message.setText(content);
        textView_outside_message.setTextSize(TEXT_SIZE);
        container_journal.addView(tableRow);
        scrollViewDown();
    }

    public void addTimeSeparator() {
        addRow = new ContentValues();
        addRow.put(columns_journal[1], 0);
        addRow.put(columns_journal[2], DATE);
        addRow.put(columns_journal[3], TIME);
        db.insert(sqlJournal.NAME_TABLE, null, addRow);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        tableRow = layoutInflater.inflate(R.layout.layout_time_separator, null);
        textView_time_separator = (TextView) tableRow.findViewById(R.id.tv_time_separator);
        textView_time_separator.setText(TIME);
        textView_time_separator.setTextSize(TEXT_SIZE);
        container_journal.addView(tableRow);
    }
    //пролистываю в самый конец
    private void scrollViewDown() {
        if(!PAUSE){
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }
    }
    //заполняю переменные настройками и применяю их
    public void loadSettings() {
        try {
            switch (sharedPreferences.getInt(SettingsActivity.SETTINGS_TEXT_SIZE, 0)) {
                case 0:
                    TEXT_SIZE = 10;
                    break;
                case 1:
                    TEXT_SIZE = 20;
                    break;
                case 2:
                    TEXT_SIZE = 30;
                    break;
            }
            switch (sharedPreferences.getInt(SettingsActivity.SETTINGS_PITCH, 0)) {
                case 0:
                    PITCH = 0.5f;
                    break;
                case 1:
                    PITCH = 1.3f;
                    break;
                case 2:
                    PITCH = 2.0f;
                    break;
            }
            switch (sharedPreferences.getInt(SettingsActivity.SETTINGS_SPEECH_RATE, 0)) {
                case 0:
                    SPEECH_RATE = 0.3f;
                    break;
                case 1:
                    SPEECH_RATE = 0.7f;
                    break;
                case 2:
                    SPEECH_RATE = 1.3f;
                    break;
            }
            VIBRO_AT_LOAD_SOUND = sharedPreferences.getBoolean(SettingsActivity.SETTINGS_VIBRO_AT_LOAD_SOUND, false);
            VIBRO_AFTER_PAUSE = sharedPreferences.getBoolean(SettingsActivity.SETTINGS_VIBRO_AFTER_PAUSE, false);
            KEYWORDS = sharedPreferences.getBoolean(SettingsActivity.SETTINGS_KEYWORDS, false);
            VOICING_EMOTICONS = sharedPreferences.getBoolean(SettingsActivity.SETTINGS_VOICING_EMOTICONS, false);
            SECOND_LANGUAGE = sharedPreferences.getString(SettingsActivity.SETTINGS_LANGUAGE,"NO");

            button_text_to_speech.setTextSize(TEXT_SIZE);
            editText_text_to_speech.setTextSize(TEXT_SIZE);
            textToSpeech.setPitch(PITCH);
            textToSpeech.setSpeechRate(SPEECH_RATE);
            button_to_settings.setTextSize(TEXT_SIZE);
//            button_fast_word_1.setTextSize(TEXT_SIZE);
//            button_fast_word_2.setTextSize(TEXT_SIZE);
//            button_fast_word_3.setTextSize(TEXT_SIZE);
//            button_fast_word_4.setTextSize(TEXT_SIZE);
//            button_fast_word_5.setTextSize(TEXT_SIZE);
            button_select_language.setTextSize(TEXT_SIZE);
            button_pause.setTextSize(TEXT_SIZE);
            textView_partial_result.setTextSize(TEXT_SIZE);
        } catch (Exception e) {
            Toast.makeText(this, "NO", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeOldJournal() {
        if (!DEAD_DATE.equals("NO")) {
            db = sqlJournal.getWritableDatabase();
            db.delete(sqlJournal.NAME_TABLE, sqlJournal.COLUMN_DATE + " < ?",
                    new String[]{DEAD_DATE});
        }
    }

    public void fillFastWords() {
        try {
            db = sqlWords.getReadableDatabase();
            cursor = db.query(sqlWords.NAME_TABLE, columns_words,
                    SQLWords.COLUMN_WHAT_IS_IT + " = ?", new String[]{"0"},
                    SQLWords.COLUMN_RATING, null, null);
            int index_id = cursor.getColumnIndex(columns_words[0]);
            int index_what_it_is = cursor.getColumnIndex(columns_words[1]);
            int index_word = cursor.getColumnIndex(columns_words[2]);
            int index_rating = cursor.getColumnIndex(columns_words[3]);
            for (int i = 0; i < fast_words.length; i++) {
                if (cursor.moveToNext()) {
                    fast_words[i] = cursor.getString(index_word);
                } else {
                    fast_words[i] = "";
                }
            }
            for(int i=fast_words.length-1; i>=0; i--){
                if(i==fast_words.length-1) {fast_words[i] = getString(R.string.fast_word_1);}
                if(i==fast_words.length-2) {fast_words[i] = getString(R.string.fast_word_2);}
                if(i==fast_words.length-3) {fast_words[i] = getString(R.string.fast_word_3);}
                if(i==fast_words.length-4) {fast_words[i] = getString(R.string.fast_word_4);}
                if(i==fast_words.length-5) {fast_words[i] = getString(R.string.fast_word_5);}
            }
            cursor.close();
            db.close();
//            setFastWord();
        } catch (Exception e) {
        }
    }
    public void saveFastWords() {
        db = sqlWords.getWritableDatabase();
        db.delete(SQLWords.NAME_TABLE, SQLWords.COLUMN_WHAT_IS_IT + " = ?",
                new String[]{0 + ""});
        for (int i = 0; i < fast_words.length; i++) {
            ContentValues row = new ContentValues();
            row.put(SQLWords.COLUMN_WORD, fast_words[i]);
            row.put(SQLWords.COLUMN_RATING, i);
            row.put(SQLWords.COLUMN_WHAT_IS_IT, 0);
            db.insert(SQLWords.NAME_TABLE, null, row);
        }
        db.close();
    }
    public void fastWord(String word) {
        word = word.replace("\n", " ");
        if (!word.equals("")) {
            String utteranceId = this.hashCode() + "";
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
            addMyMessage(word);
        }
        String temp_word = word.substring(1);
        for (int i = 0; i < fast_words.length - 1; i++) {
            if(!fast_words[i].equals("")){
                if (temp_word.equals(fast_words[i].substring(1))) {
                    String temp = fast_words[i + 1];
                    fast_words[i + 1] = fast_words[i];
                    fast_words[i] = temp;
                    break;
                }
            }
        }
//        setFastWord();
    }
    public void wordsRating(String word) {
        word = word.replace("\n", " ");
        String temp_word = word.substring(1);
        boolean find = false;
        for (int i = 0; i < fast_words.length - 1; i++) {
            if(!fast_words[i].equals("")){
                if (temp_word.equals(fast_words[i].substring(1))) {
                    String temp = fast_words[i + 1];
                    fast_words[i + 1] = fast_words[i];
                    fast_words[i] = temp;
                    find = true;
                    break;
                }
            }
        }
        if (!find) {
            for (int i = fast_words.length - 1; i >= 0; i--) {
                if (fast_words[i].equals("")) {
                    fast_words[i] = word;
                    break;
                }
                if (i == 0) {
                    fast_words[i] = word;
                }
            }
        }
//        setFastWord();
    }
    public void setFastWord() {
//        button_fast_word_1.setText(fast_words[fast_words.length - 1]);
//        button_fast_word_2.setText(fast_words[fast_words.length - 2]);
//        button_fast_word_3.setText(fast_words[fast_words.length - 3]);
//        button_fast_word_4.setText(fast_words[fast_words.length - 4]);
//        button_fast_word_5.setText(fast_words[fast_words.length - 5]);
    }
    public void voicingEmoticons(String text) {
        if (VOICING_EMOTICONS) {
            switch (text) {
                case ":)":

                    break;
            }
            String utteranceId = this.hashCode() + "";
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        }
    }

    public void findKeyword(String message) {
        String[] message_words = message.split(" ");
        for (int i = 0; i < message_words.length; i++) {
            //без учета регистра первой буквы
            message_words[i] = message_words[i].substring(1);
            for (int j = 0; j < keywords.length; j++) {
                if(!keywords[j].equals("")){
                    if (message_words[i].equals(keywords[j])) {
                        //вибрация
                        Vibrator vibratorManager = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibratorManager != null) {
                            VibrationEffect effect = VibrationEffect.createOneShot(
                                    200, VibrationEffect.DEFAULT_AMPLITUDE);
                            vibratorManager.vibrate(effect);
                        }
                    }
                }
            }
        }
    }

    public void checkPause() {
        CALENDAR_MILLIS = Calendar.getInstance();
        if (LAST_MILLIS != 0 && VIBRO_AFTER_PAUSE) {
            if (CALENDAR_MILLIS.getTimeInMillis() - LAST_MILLIS >= 10000) {
                Vibrator vibratorManager = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibratorManager != null) {
                    VibrationEffect effect = VibrationEffect.createOneShot(
                            200, VibrationEffect.DEFAULT_AMPLITUDE);
                    vibratorManager.vibrate(effect);
                }
            }
        }
        LAST_MILLIS = CALENDAR_MILLIS.getTimeInMillis();
    }

    public void selectLanguage(){
        speech.stopListening();
        if(HOME_LANGUAGE){
            //изменяю на второй
            if(SECOND_LANGUAGE.equals("NO")){
                Intent intent = new Intent(MainActivity.this, LanguageActivity.class);
                startActivity(intent);
            }else{
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.forLanguageTag(SECOND_LANGUAGE).toString());
                button_select_language.setText(Locale.forLanguageTag(SECOND_LANGUAGE).getDisplayLanguage());
            }
        }else{
            //возвращаю родной
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString());
            button_select_language.setText(Locale.getDefault().getDisplayLanguage());
        }
    }

    public void onLoadSound(){
        Toast.makeText(getApplicationContext(), getString(R.string.warning_load_sound), Toast.LENGTH_SHORT).show();
        Vibrator vibratorManager = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibratorManager != null) {
            VibrationEffect effect = VibrationEffect.createOneShot(
                    500, VibrationEffect.DEFAULT_AMPLITUDE);
            vibratorManager.vibrate(effect);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        if (errorMessage.equals("No match")){
            speech.startListening(recognizerIntent);
        }
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        if(!PAUSE){
            Log.i(LOG_TAG, "onPartialResults");
            textView_partial_result.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ArrayList commandList = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            textView_partial_result.setText(commandList.get(0).toString());
            scrollViewDown();
        }
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        checkPause();
        Log.i(LOG_TAG, "onResults");
        textView_partial_result.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,0));
        ArrayList commandList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        addOutsideMessage(commandList.get(0).toString());
//        findKeyword(commandList.get(0).toString());
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);

        int size = (int)(getResources().getDimension(R.dimen.wave_radius)
                -getResources().getDimension(R.dimen.wave_center)-rmsdB*10);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(getColor(R.color.wave_wave));
        gradientDrawable.setCornerRadius(getResources().getDimension(R.dimen.wave_radius));
        gradientDrawable.setStroke(size, getColor(R.color.wave_stroke));
        button_wave.setBackground(gradientDrawable);

        if (rmsdB==10.0 && countRmsChanged==0) {
            countRmsChanged++;
        }
        if (rmsdB<10.0 && rmsdB>7.0 && countRmsChanged==1){
            countRmsChanged++;
        }else{
            countRmsChanged=0;
        }
        if (rmsdB<8.0 && countRmsChanged==2){
            //резкий громкий звук
            onLoadSound();
            countRmsChanged=0;
        }else{
            countRmsChanged=0;
        }
    }
    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    public class GestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE &&
                    Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                return false; // справа налево
            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE &&
                    Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                if (!OPEN_FRAGMENT) {
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    phrasesFragment = new PhrasesFragment();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    ft.add(R.id.main_container, phrasesFragment);
                    ft.commit();
                    OPEN_FRAGMENT = true;
                }
                return false; // слева направо
            }
            return false;
        }
    }

    @Override
    public void someEvent(String s) {
        switch (s){
            case "re_Open":
                phrasesFragment = new PhrasesFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, phrasesFragment).commit();
                break;
            default:
                speakText(s);
                getSupportFragmentManager().beginTransaction().remove(phrasesFragment).commit();
                OPEN_FRAGMENT = false;
                break;
        }
    }
    @Override
    public void onBackPressed() {
        if(OPEN_FRAGMENT){
            getSupportFragmentManager().beginTransaction().remove(phrasesFragment).commit();
            OPEN_FRAGMENT = false;
        }else{
            super.onBackPressed();
        }
    }
}
//Toast.makeText(getApplicationContext(), "YES", Toast.LENGTH_SHORT).show();