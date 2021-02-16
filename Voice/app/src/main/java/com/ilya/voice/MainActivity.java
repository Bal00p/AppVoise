package com.ilya.voice;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
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
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements RecognitionListener,
        TextToSpeech.OnInitListener,
        PhrasesFragment.onSomeEventListenerMain {
    //переменные
    EditText editText_text_to_speech;
    Button button_text_to_speech, button_select_language, button_pause, button_to_settings, button_rotation;
    Button button_wave;public String[] fast_words = new String[10];
    public String[] keywords = new String[10];
    SQLWords sqlWords;
    LinearLayout container_journal, main_layout, main_layout2;
    ConstraintLayout main_container;
    ScrollView scrollView;
    View tableRow;
    TextView textView_my_message, textView_outside_message, textView_time_separator,
            textView_partial_result;
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
    public static int TEXT_SIZE = 10;
    public static boolean VIBRO_AT_LOAD_SOUND = false;
    public static boolean VIBRO_AFTER_PAUSE = false;
    public static boolean KEYWORDS = false;
    public static boolean VOICING_EMOTICONS = false;
    public static boolean SHOW_GUIDE = false;
    public static boolean MALE_GENDER = false;

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";

    public static boolean HOME_LANGUAGE = false;
    public static String SECOND_LANGUAGE = "";

    public static boolean PAUSE = false;
    public static float lastRmsChanged = 0;

    GestureDetector gestureDetector;
    public static final int SWIPE_MIN_DISTANCE = 120;
    public static final int SWIPE_THRESHOLD_VELOCITY = 200;
    PhrasesFragment phrasesFragment;
    GuideFragment guideFragment;
    public static boolean OPEN_FRAGMENT = false;
    public static boolean REVERSE_ORIENTATION = false;
    public static boolean SPEAKING = false;

    TelephonyManager mTelephonyManager;
    PhoneStateListener mPhoneStateListener;
    public static final int NOTIFY_ID = 101;
    DialogPhone dialogPhone;
    public static int VALUE_SOUND = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String requiredPermission = Manifest.permission.RECORD_AUDIO;
                if (checkCallingOrSelfPermission(requiredPermission) ==
                        PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{requiredPermission}, 101);
                }
            }
            Intent resultIntent = new Intent(this, MainActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this,
                    0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    getApplicationContext(),"default")
                    .setSmallIcon(android.R.drawable.ic_menu_view)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle(getString(R.string.notify_title))
                    .setOngoing(true)
                    .setChannelId(getString(R.string.channel_notify_open_app))
                    .setShowWhen(false)
                    .setNotificationSilent()
                    .setColor(getColor(R.color.notify_color))
                    .setColorized(true)
                    .setContentIntent(resultPendingIntent);

            NotificationManager notificationManager = (NotificationManager)
                    getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    getString(R.string.channel_notify_open_app),
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(NOTIFY_ID,builder.build());

            mTelephonyManager = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
            gestureDetector = new GestureDetector(new GestureListener());

            recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            startListening();

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
            button_rotation = (Button)findViewById(R.id.btn_rotation);
            editText_text_to_speech = (EditText) findViewById(R.id.et_text_to_speech);
            textView_partial_result = (TextView) findViewById(R.id.tv_my_partial_result);
            textView_partial_result.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0));
            container_journal = (LinearLayout) findViewById(R.id.container_journal);
            main_container = (ConstraintLayout) findViewById(R.id.main_container);
            main_layout = (LinearLayout) findViewById(R.id.main_layout);
            main_layout2 = (LinearLayout) findViewById(R.id.main_layout2);
            scrollView = (ScrollView) findViewById(R.id.sv_journal);
            scrollView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
            //устанавливаю текущую дату и время сеанса
            setDateAndTime();
            removeOldJournal();

            main_layout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });
            main_layout2.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.btn_text_to_speech:
                            if (SPEAKING){
                                SPEAKING = false;
                                textToSpeech.stop();
                                button_text_to_speech.setText(getString(R.string.speech));
                                startListening();
                            }else{
                                String text = editText_text_to_speech.getText().toString();
                                speakText(text);
                            }
                            break;
                        case R.id.btn_to_settings:
                            Intent intent = new Intent(MainActivity.this,
                                    SettingsActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.btn_rotation:
                            //переворачиваю экран на 180
                            setOrientation();
                            break;
                        case R.id.btn_select_language:
                            if (HOME_LANGUAGE) {
                                HOME_LANGUAGE = false;
                            } else {
                                HOME_LANGUAGE = true;
                            }
                            selectLanguageAndUpTTSAndSTT();
                            speech.startListening(recognizerIntent);
                            break;
                        case R.id.btn_pause:
                            if (PAUSE) {
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
            button_rotation.setOnClickListener(listener);

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

            mPhoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    super.onCallStateChanged(state, incomingNumber);

                    switch (state) {
                        case TelephonyManager.CALL_STATE_IDLE:
//                            Toast.makeText(MainActivity.this, "ожидание звонка", Toast.LENGTH_SHORT).show();
                            AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                            audioManager.setMode(AudioManager.MODE_IN_CALL);
                            if(VALUE_SOUND!=-1){
                                audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, VALUE_SOUND, 0);
                                audioManager.setSpeakerphoneOn(false);
                                VALUE_SOUND=-1;
                            }
                            break;
                        case TelephonyManager.CALL_STATE_RINGING:
//                            Toast.makeText(MainActivity.this, "входящий звонок", Toast.LENGTH_SHORT).show();
                            break;
                        case TelephonyManager.CALL_STATE_OFFHOOK:
//                            Toast.makeText(MainActivity.this, "на удержании", Toast.LENGTH_SHORT).show();
                            dialogPhone = new DialogPhone();
                            FragmentManager manager = getSupportFragmentManager();
                            dialogPhone.show(manager, "addPhrase");
                            break;
                    }
                }
            };

        }catch (Exception e){}
    }

    UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            new Thread(){
                public void run(){
                    MainActivity.this.runOnUiThread(new Runnable(){
                        public void run(){
                            SPEAKING=true;
                            button_text_to_speech.setText(getString(R.string.stop));
                        }
                    });
                }
            }.start();
        }
        @Override
        public void onDone(String utteranceId) {
            new Thread(){
                public void run(){
                    MainActivity.this.runOnUiThread(new Runnable(){
                        public void run(){
                            SPEAKING=false;
                            button_text_to_speech.setText(getString(R.string.speech));
                            startListening();
                        }
                    });
                }
            }.start();
        }
        @Override
        public void onError(String utteranceId) {}
    };

    @Override
    public void onInit(int initStatus) {
        String language_tag;
        if(HOME_LANGUAGE){
            language_tag = SECOND_LANGUAGE;
        }else{
            language_tag = Locale.getDefault().toLanguageTag();
        }
        if (initStatus == TextToSpeech.SUCCESS) {
            if (textToSpeech.isLanguageAvailable(new Locale(
                    Locale.forLanguageTag(language_tag).getLanguage()))
                    == TextToSpeech.LANG_AVAILABLE) {
                textToSpeech.setLanguage(new Locale(Locale.forLanguageTag(language_tag).getLanguage()));
            } else {
                textToSpeech.setLanguage(Locale.US);
            }
            String l = Locale.forLanguageTag(language_tag).getLanguage();
            String gender2 = (MALE_GENDER? "male_1-local" : "female_1-local");
            String gender5 = (MALE_GENDER? l+"f" : l+"c");
            for(Voice x: textToSpeech.getVoices()){
                String[] v = x.getName().split("#");
                String[] r = v[0].split("-");
                if(v.length>1){
                    if(v[1].equals(gender2)&&r[0].equals(l)){
                        Voice voice = new Voice(x.getName(),Locale.forLanguageTag(language_tag),
                                Voice.QUALITY_VERY_HIGH, Voice.LATENCY_VERY_LOW,
                                true,null);
                        textToSpeech.setVoice(voice);
                    }
                }else{
                    if(r.length==5){
                        //ruf - male, ruc - female
                        if(r[0].equals(l)&&r[4].equals("local")&&r[3].equals(gender5)){
                            Voice voice = new Voice(x.getName(),Locale.forLanguageTag(language_tag),
                                    Voice.QUALITY_VERY_HIGH, Voice.LATENCY_VERY_LOW,
                                    true,null);
                            textToSpeech.setVoice(voice);
                        }
                    }
                }
            }
            readText = true;
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
            readText = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        //применяю настройки
        loadSettings();

        checkShowGuide();
        //заполняю прошедшим
        fillJournal();

        fillKeyWords();

        selectLanguageAndUpTTSAndSTT();
        startListening();

        if(OPEN_FRAGMENT){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, phrasesFragment).commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }
    }

    public void startListening(){
        speech = SpeechRecognizer.createSpeechRecognizer(getApplicationContext(),
                ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/com.google.android.voicesearch.serviceapi.GoogleRecognitionService"));
        speech.setRecognitionListener(this);
        speech.startListening(recognizerIntent);
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
        if(!partialResult.equals("")){
            addOutsideMessage(partialResult);
            textView_partial_result.setText("");
            textView_partial_result.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,0));
        }
        speech.destroy();
        try {
            if (!text.equals("")) {
                Bundle params = new Bundle();
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
                textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, params, "UniqueID");
                editText_text_to_speech.setText("");
                addMyMessage(checkRequest(text));
            }
        } catch (Exception e) {}
    }

    public String checkRequest(String text){
        String[] arr = text.split(" ");
        String upText = "";
        for (int i=0; i<arr.length; i++){
            if (arr[i].equals("DELETE")){
                arr[i]="DЕLЕТЕ";//ВЕДЕТЕ
            }
            if (arr[i].equals("FROM")){
                arr[i]="FRОМ";//АКОМ
            }
            upText+=(arr[i]+" ");
        }
        return upText;
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
            VIBRO_AT_LOAD_SOUND = sharedPreferences.getBoolean(SettingsActivity.SETTINGS_VIBRO_AT_LOAD_SOUND, false);
            VIBRO_AFTER_PAUSE = sharedPreferences.getBoolean(SettingsActivity.SETTINGS_VIBRO_AFTER_PAUSE, false);
            KEYWORDS = sharedPreferences.getBoolean(SettingsActivity.SETTINGS_KEYWORDS, false);
            SECOND_LANGUAGE = sharedPreferences.getString(SettingsActivity.SETTINGS_LANGUAGE,"NO");
            SHOW_GUIDE = sharedPreferences.getBoolean(SettingsActivity.SETTINGS_SHOW_GUIDE, true);
            MALE_GENDER = sharedPreferences.getBoolean(SettingsActivity.SETTINGS_GENDER, false);

            button_text_to_speech.setTextSize(TEXT_SIZE);
            editText_text_to_speech.setTextSize(TEXT_SIZE);
            button_to_settings.setTextSize(TEXT_SIZE);
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

    public void selectLanguageAndUpTTSAndSTT(){
        speech.stopListening();
        if(HOME_LANGUAGE){
            //изменяю на второй
            if(SECOND_LANGUAGE.equals("NO")){
                Intent intent = new Intent(MainActivity.this, LanguageActivity.class);
                startActivity(intent);
            }else{
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.forLanguageTag(SECOND_LANGUAGE).toString());
                textToSpeech = new TextToSpeech(this, this);
                textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
                button_select_language.setText(Locale.forLanguageTag(SECOND_LANGUAGE).getDisplayLanguage());
            }
        }else{
            //возвращаю родной
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString());
            textToSpeech = new TextToSpeech(this, this);
            textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
            button_select_language.setText(Locale.getDefault().getDisplayLanguage());
        }
    }

    public void onLoadSound(){
        Vibrator vibratorManager = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibratorManager != null) {
            VibrationEffect effect = VibrationEffect.createOneShot(
                    300, VibrationEffect.DEFAULT_AMPLITUDE);
            vibratorManager.vibrate(effect);
        }
    }
    public void onLongLoadSound(){
        Vibrator vibratorManager = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibratorManager != null) {
            VibrationEffect effect = VibrationEffect.createOneShot(
                    700, VibrationEffect.DEFAULT_AMPLITUDE);
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

    public String partialResult = "";
    @Override
    public void onPartialResults(Bundle arg0) {
        if(!PAUSE){
            Log.i(LOG_TAG, "onPartialResults");
            textView_partial_result.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ArrayList commandList = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            textView_partial_result.setText(commandList.get(0).toString());
            scrollViewDown();
            partialResult = commandList.get(0).toString();
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

        if(VIBRO_AT_LOAD_SOUND){
            if (rmsdB==10.0 && lastRmsChanged <=0) {
                onLoadSound();
            }
            if (rmsdB>=9.5 && lastRmsChanged >=9.5) {
                onLongLoadSound();
            }
        }
        lastRmsChanged=rmsdB;
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
            case "cloSe_guiDe":
                getSupportFragmentManager().beginTransaction().remove(guideFragment).commit();
                break;
            case "cloSe_phrAses":
                getSupportFragmentManager().beginTransaction().remove(phrasesFragment).commit();
                OPEN_FRAGMENT = false;
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
    public void setOrientation(){
        if (REVERSE_ORIENTATION){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            REVERSE_ORIENTATION=false;
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            REVERSE_ORIENTATION=true;
        }
    }
    public void checkShowGuide(){
        if (SHOW_GUIDE){
            //запускаю фрагмент с гайдом
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            guideFragment = new GuideFragment();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            ft.add(R.id.main_container, guideFragment);
            ft.commit();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(SettingsActivity.SETTINGS_SHOW_GUIDE, false);
            editor.commit();
        }
    }
}
//Toast.makeText(getApplicationContext(), "YES", Toast.LENGTH_SHORT).show();