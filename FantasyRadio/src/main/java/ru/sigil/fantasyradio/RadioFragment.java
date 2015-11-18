package ru.sigil.fantasyradio;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_AAC;

import java.util.Calendar;
import java.util.Random;

import ru.sigil.fantasyradio.saved.CurrentControls;
import ru.sigil.fantasyradio.saved.MP3Entity;
import ru.sigil.fantasyradio.saved.MP3Saver;
import ru.sigil.fantasyradio.utils.AlarmReceiever;
import ru.sigil.fantasyradio.utils.BASSUtil;
import ru.sigil.fantasyradio.utils.PlayerState;

public class RadioFragment extends Fragment {

    private int hour;
    private int minute;
    private AlarmManager am;
    private PendingIntent sender;
    private CheckBox cb1;
    private final int MIN_SCREEN_HEIGHT = 350;
    private View mainFragmentView;
    public final int TIME_DIALOG_ID = 999;
    private static final int AD_SHOW_PROBABILITY_REC = 25;
    private static final int AD_SHOW_PROBABILITY_URL = 5;
    private Random random;

    /**
     * Хэндлер, устанавливающий название трека
     */
    private Handler setTitleHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                ((TextView) mainFragmentView.findViewById(R.id.textView1)).setText(msg.getData().getString("title"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Хэндлер, закрывающий приложение
     *
     * @see ru.sigil.fantasyradio.utils.AlarmReceiever
     */
    private Handler sleepTimerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().finish();
                }
            });

        }
    };

    /**
     * Хэндлер окончания записи.
     * Пересохраняет временный файл, добавляет информацию в базу данных.
     *
     * @see ru.sigil.fantasyradio.saved.MP3Saver
     */
    private Handler recordFinishedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // А тут мы пишем инфу о скачанном
            // файле в базу
            MP3Entity mp3Entity = new MP3Entity();
            Bundle b = msg.getData();
            if (b != null) {
                mp3Entity.setArtist(b.getString("artist"));
            }
            mp3Entity.setTitle(b.getString("title"));
            mp3Entity.setDirectory(b.getString("directory"));
            mp3Entity.setTime(b.getString("time"));
            MP3Saver.getMp3c()
                    .removeEntityByDirectory(mp3Entity.getDirectory());
            MP3Saver.getMp3c().add(mp3Entity);
            // ====================================================================
            // --------------------------------OLOLO----------------------------------
        }
    };

    /**
     * Хэндлер обращающий контролы в неактивное состояние
     */
    private Handler disablePlayerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ((ImageView) mainFragmentView.findViewById(R.id.streamButton))
                    .setImageResource(R.drawable.play_states);
            ((ImageView) mainFragmentView.findViewById(R.id.recordButton))
                    .setImageResource(R.drawable.rec);
            PlayerState.getInstance().setCurrentRadioEntity(null);
            TextView tv1 = (TextView) mainFragmentView.findViewById(R.id.textView1);
            tv1.setText("");
        }
    };

    private Random getRandom() {
        if (random == null)
            random = new Random();
        return random;
    }

    /**
     * Магия BASS.dll
     */
    class OpenURL implements Runnable {
        String url;

        public OpenURL(String p) {
            url = p;
        }

        public void run() {
            int r;
            synchronized (lock) { // make sure only 1 thread at a time can
                // do
                // the following
                r = ++req; // increment the request counter for this request
            }
            BASS.BASS_StreamFree(BASSUtil.getChan()); // close old stream
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    ((TextView) mainFragmentView.findViewById(R.id.textView1))
                            .setText(R.string.connecting);
                }
            });
            int c = BASS.BASS_StreamCreateURL(url, 0, BASS.BASS_STREAM_BLOCK
                            | BASS.BASS_STREAM_STATUS | BASS.BASS_STREAM_AUTOFREE,
                    BASSUtil.StatusProc, r); // open URL
            synchronized (lock) {
                if (r != req) { // there is a newer request, discard this
                    // stream
                    if (c != 0)
                        BASS.BASS_StreamFree(c);
                    return;
                }
                BASSUtil.setChan(c); // this is now the current stream
            }

            if (BASSUtil.getChan() != 0) { // failed to open
                handler.postDelayed(timer, 50); // start prebuffer
                // monitoring
            }
        }
    }

    /**
     * Магия BASS.dll для AAC потока
     */
    class OpenURLAAC implements Runnable {
        String url;

        public OpenURLAAC(String p) {
            url = p;
        }

        public void run() {
            int r;
            synchronized (lock) { // make sure only 1 thread at a time can
                // do
                // the following
                r = ++req; // increment the request counter for this request
            }
            BASS.BASS_StreamFree(BASSUtil.getChan()); // close old stream
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    ((TextView) mainFragmentView.findViewById(R.id.textView1))
                            .setText(R.string.connecting);
                }
            });
            int c = BASS_AAC
                    .BASS_AAC_StreamCreateURL(url, 0, BASS.BASS_STREAM_BLOCK
                            | BASS.BASS_STREAM_STATUS
                            | BASS.BASS_STREAM_AUTOFREE, BASSUtil.StatusProc, r); // open
            // URL
            synchronized (lock) {
                if (r != req) { // there is a newer request, discard this
                    // stream
                    if (c != 0)
                        BASS.BASS_StreamFree(c);
                    return;
                }
                BASSUtil.setChan(c); // this is now the current stream
            }

            if (BASSUtil.getChan() != 0) {
                handler.postDelayed(timer, 50);
            } // start prebuffer
            // monitoring
        }
    }

    /**
     * Начинаем играть поток
     *
     * @param url URL потока. Не AAC
     */
    void Play(String url) {
        BASS.BASS_SetConfigPtr(BASS.BASS_CONFIG_NET_PROXY, null);
        try {
            new Thread(new OpenURL(url)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Начинаем играть поток
     *
     * @param url URL потока. AAC
     */
    void PlayAAC(String url) {
        try {
            new Thread(new OpenURLAAC(url)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Кликныли на кнопку Play. Начинаем проигрывать выбранный поток.
     */
    public void streamButtonClick(View v) {
        if (PlayerState.getInstance().getCurrentRadioEntity() == null) {
            if (PlayerState.getInstance().getCurrent_stream() == PlayerState.AAC16)
                PlayAAC(getString(R.string.stream_url_AAC16));
            if (PlayerState.getInstance().getCurrent_stream() == PlayerState.AAC112)
                PlayAAC(getString(R.string.stream_url_AAC112));
            if (PlayerState.getInstance().getCurrent_stream() == PlayerState.MP332)
                Play(getString(R.string.stream_url_MP332));
            if (PlayerState.getInstance().getCurrent_stream() == PlayerState.MP364)
                Play(getString(R.string.stream_url_MP364));
            if (PlayerState.getInstance().getCurrent_stream() == PlayerState.MP396)
                Play(getString(R.string.stream_url_MP396));
            ImageView iv = (ImageView) v;
            iv.setImageResource(R.drawable.pause_states);
            RadioEntity ent = new RadioEntity();
            ent.setArtist("");
            ent.setTitle("");
            PlayerState.getInstance().setCurrentRadioEntity(new RadioEntity());
        } else {
            ImageView iv = (ImageView) v;
            iv.setImageResource(R.drawable.play_states);
            ImageView rib = (ImageView) mainFragmentView.findViewById(R.id.recordButton);
            rib.setImageResource(R.drawable.rec);
            if (PlayerState.getInstance().isRecActive())
                PlayerState.getInstance().setRecActive(false);
            PlayerState.getInstance().setCurrentRadioEntity(null);
            BASS.BASS_StreamFree(BASSUtil.getChan());
            TextView tv1 = (TextView) mainFragmentView.findViewById(R.id.textView1);
            tv1.setText("");
        }
    }

    /**
     * Получаем метаданные (название, исполнитель и т.д.)
     */
    private BASS.SYNCPROC MetaSync = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            new Thread(new Runnable() {
                public void run() {
                    DoMeta();
                }
            }).start();
        }
    };

    /**
     * Выполняется после завершения проигрывания. В данный момент не используестя
     */
    private BASS.SYNCPROC EndSync = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainFragmentView = inflater.inflate(R.layout.activity_main, container, false);
        //------------------------------------------------------------------------------------------
        ImageView streamButton = (ImageView) mainFragmentView.findViewById(R.id.streamButton);
        streamButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                streamButtonClick(v);
            }
        });
        mainFragmentView.findViewById(R.id.bitrateText0).setOnClickListener(bitrateClick);
        mainFragmentView.findViewById(R.id.bitrateText1).setOnClickListener(bitrateClick);
        mainFragmentView.findViewById(R.id.bitrateText2).setOnClickListener(bitrateClick);
        mainFragmentView.findViewById(R.id.bitrateText3).setOnClickListener(bitrateClick);
        mainFragmentView.findViewById(R.id.bitrateText4).setOnClickListener(bitrateClick);
        ImageView recordButton = (ImageView) mainFragmentView.findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                streamRecordClick();
            }
        });
        mainFragmentView.findViewById(R.id.tvChangeTime).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimerClick(v);
            }
        });
        //------------------------------------------------------------------------------------------
        //setContentView(R.layout.activity_main);
        AlarmReceiever.setSleepHandler(sleepTimerHandler);
        cb1 = (CheckBox) mainFragmentView.findViewById(R.id.checkBox1);
        Intent intent = new Intent(getActivity().getBaseContext(), AlarmReceiever.class);
        sender = PendingIntent.getBroadcast(getActivity().getBaseContext(), 192837, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am = (AlarmManager) getActivity().getBaseContext().getSystemService(Context.ALARM_SERVICE);
        cb1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calNow = Calendar.getInstance();
                Calendar cal = Calendar.getInstance();
                // add 5 minutes to the calendar object
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.SECOND, 0);
                Long alarmMillis = cal.getTimeInMillis();
                // is chkIos checked?
                if (calNow.after(cal)) {
                    alarmMillis += 86400000L; // Add 1 day if time selected
                    // before now
                    cal.setTimeInMillis(alarmMillis);
                }
                if (((CheckBox) v).isChecked()) {
                    am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                            sender);
                } else {
                    am.cancel(sender);
                }
            }
        });
        setTimer(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar
                .getInstance().get(Calendar.MINUTE));
        PlayerState.getInstance().setDisablePlayerHandler(disablePlayerHandler);
        PlayerState.getInstance().setRecordFinishedHandler(recordFinishedHandler);
        timer = new Runnable() {
            public void run() {
                // monitor prebuffering progress
                long progress = BASS.BASS_StreamGetFilePosition(
                        BASSUtil.getChan(), BASS.BASS_FILEPOS_BUFFER)
                        * 100
                        / BASS.BASS_StreamGetFilePosition(BASSUtil.getChan(),
                        BASS.BASS_FILEPOS_END); // percentage of buffer
                // filled
                if (progress > 75
                        || BASS.BASS_StreamGetFilePosition(BASSUtil.getChan(),
                        BASS.BASS_FILEPOS_CONNECTED) == 0) { // over 75%
                    // full
                    // (or
                    // end
                    // of
                    // download)
                    // get the broadcast name and URL
                    String[] icy = (String[]) BASS.BASS_ChannelGetTags(
                            BASSUtil.getChan(), BASS.BASS_TAG_ICY);
                    if (icy == null)
                        icy = (String[]) BASS.BASS_ChannelGetTags(
                                BASSUtil.getChan(), BASS.BASS_TAG_HTTP); // no
                    // ICY
                    // tags,
                    // try
                    // HTTP
                    // get the stream title and set sync for subsequent titles
                    DoMeta();
                    BASS.BASS_ChannelSetSync(BASSUtil.getChan(),
                            BASS.BASS_SYNC_META, 0, MetaSync, 0); // Shoutcast
                    BASS.BASS_ChannelSetSync(BASSUtil.getChan(),
                            BASS.BASS_SYNC_OGG_CHANGE, 0, MetaSync, 0); // Icecast/OGG
                    // set sync for end of stream
                    BASS.BASS_ChannelSetSync(BASSUtil.getChan(),
                            BASS.BASS_SYNC_END, 0, EndSync, 0);
                    // play it!
                    BASS.BASS_ChannelPlay(BASSUtil.getChan(), false);
                } else {
                    ((TextView) mainFragmentView.findViewById(R.id.textView1)).setText(String
                            .format("buffering... %d%%", progress));
                    handler.postDelayed(this, 50);
                }
            }
        };
        return mainFragmentView;
    }

    private int req; // request number/counter

    private Handler handler = new Handler();
    private Runnable timer;
    private final Object lock = new Object();

    class RunnableParam implements Runnable {
        Object param;

        RunnableParam(Object p) {
            param = p;
        }

        public void run() {

        }
    }

    /**
     * Показываем Toast с сообщением об ошибке
     *
     * @param es Текст сообщения
     */
    void Error(String es) {
        // get error code in current thread for display in UI thread
        String s = String.format("%s\n", es);
        getActivity().runOnUiThread(new RunnableParam(s) {
            public void run() {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        (String) param, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }


    /**
     * update stream title from metadata
     */
    void DoMeta() {
        RadioEntity re = new RadioEntity();
        String meta = (String) BASS.BASS_ChannelGetTags(BASSUtil.getChan(),
                BASS.BASS_TAG_META);
        if (meta != null) { // got Shoutcast metadata
            int ti = meta.indexOf("StreamTitle='");
            if (ti >= 0) {
                String title = "No title";
                try {
                    title = meta.substring(ti + 13, meta.indexOf("'", ti + 13));
                    //noinspection InjectedReferences
                    title = new String(title.getBytes("cp-1252"), "cp-1251");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    re.setTitle(title);
                    Bundle bundle = new Bundle();
                    bundle.putString("title", title);
                    Message msg = new Message();
                    msg.setData(bundle);
                    setTitleHandler.sendMessage(msg);
                }
            } else {
                String[] ogg = (String[]) BASS.BASS_ChannelGetTags(
                        BASSUtil.getChan(), BASS.BASS_TAG_OGG);
                if (ogg != null) { // got Icecast/OGG tags
                    String artist = null, title = null;
                    for (String s : ogg) {
                        if (s.regionMatches(true, 0, "artist=", 0, 7)) {
                            artist = s.substring(7);
                            re.setArtist(artist);
                        } else if (s.regionMatches(true, 0, "title=", 0, 6)) {
                            title = s.substring(6);
                            re.setTitle(title);
                        }
                    }
                    if (title != null) {
                        if (artist != null)
                            ((TextView) mainFragmentView.findViewById(R.id.textView1))
                                    .setText(title + " - " + title);
                        else
                            ((TextView) mainFragmentView.findViewById(R.id.textView1))
                                    .setText(title);
                    }
                }
            }
        } else {
            ((TextView) mainFragmentView.findViewById(R.id.textView1)).setText("");
        }
        PlayerState.getInstance().setCurrentRadioEntity(re);
    }

    /**
     * Выбор битрэйта
     *
     * @param v Вьюха, в тэге содержится строка с битрэйтом (URL)
     */

    private OnClickListener bitrateClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(BuildConfig.FLAVOR.equals("free") && getRandom().nextInt(100) < AD_SHOW_PROBABILITY_URL) {
                if (((TabHoster)getActivity()).getmInterstitialAd().isLoaded()) {
                    ((TabHoster)getActivity()).getmInterstitialAd().show();
                }
            }
            mainFragmentView.findViewById(R.id.bitrateText0).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
            mainFragmentView.findViewById(R.id.bitrateText1).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
            mainFragmentView.findViewById(R.id.bitrateText2).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
            mainFragmentView.findViewById(R.id.bitrateText3).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
            mainFragmentView.findViewById(R.id.bitrateText4).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
            v.setBackgroundColor(getResources().getColor(
                    R.color.bitrate_element_active));
            PlayerState.getInstance().setCurrent_stream(Integer.parseInt(v.getTag().toString()));
            if (PlayerState.getInstance().getCurrentRadioEntity() != null) {
                ImageView b = (ImageView) mainFragmentView.findViewById(R.id.streamButton);
                b.performClick();
                b.performClick();
            }
        }
    };

    @Override
    public void onResume() {
        SeekBar sb = (SeekBar) mainFragmentView.findViewById(R.id.mainVolumeSeekBar);
        sb.setProgress((int) (CurrentControls.getCurrentVolume() * 100));
        sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser)
                    BASS.BASS_SetVolume(((float) progress) / 100);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        if (Integer.parseInt(mainFragmentView.findViewById(R.id.bitrateText0).getTag()
                .toString()) != PlayerState.getInstance().getCurrent_stream()) {
            mainFragmentView.findViewById(R.id.bitrateText0).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
        } else {
            mainFragmentView.findViewById(R.id.bitrateText0).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element_active));
        }
        if (Integer.parseInt(mainFragmentView.findViewById(R.id.bitrateText1).getTag()
                .toString()) != PlayerState.getInstance().getCurrent_stream()) {
            mainFragmentView.findViewById(R.id.bitrateText1).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
        } else {
            mainFragmentView.findViewById(R.id.bitrateText1).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element_active));
        }
        if (Integer.parseInt(mainFragmentView.findViewById(R.id.bitrateText2).getTag()
                .toString()) != PlayerState.getInstance().getCurrent_stream()) {
            mainFragmentView.findViewById(R.id.bitrateText2).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
        } else {
            mainFragmentView.findViewById(R.id.bitrateText2).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element_active));
        }
        if (Integer.parseInt(mainFragmentView.findViewById(R.id.bitrateText3).getTag()
                .toString()) != PlayerState.getInstance().getCurrent_stream()) {
            mainFragmentView.findViewById(R.id.bitrateText3).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
        } else {
            mainFragmentView.findViewById(R.id.bitrateText3).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element_active));
        }
        if (Integer.parseInt(mainFragmentView.findViewById(R.id.bitrateText4).getTag()
                .toString()) != PlayerState.getInstance().getCurrent_stream()) {
            mainFragmentView.findViewById(R.id.bitrateText4).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
        } else {
            mainFragmentView.findViewById(R.id.bitrateText4).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element_active));
        }

        //TODO--------------------------------------------------------------------------------------
        if (PlayerState.getInstance().getCurrentRadioEntity() != null) {
            ImageView iv = (ImageView) mainFragmentView.findViewById(R.id.streamButton);
            iv.setImageResource(R.drawable.pause_states);
            ImageView rib = (ImageView) mainFragmentView.findViewById(R.id.recordButton);
            if (PlayerState.getInstance().isRecActive()) {
                rib.setImageResource(R.drawable.rec_active);
            } else {
                rib.setImageResource(R.drawable.rec);
            }
            TextView tv1 = (TextView) mainFragmentView.findViewById(R.id.textView1);
            if (PlayerState.getInstance().getCurrentRadioEntity() != null) {
                tv1.setText(PlayerState.getInstance().getCurrentRadioEntity().getTitle());
            }
        }
        //------------------------------------------------------------------------------------------
        super.onResume();
    }

    /**
     * Запись потока.
     */
    private void streamRecordClick() {
        if(BuildConfig.FLAVOR.equals("free") && getRandom().nextInt(100) < AD_SHOW_PROBABILITY_REC) {
            if (((TabHoster)getActivity()).getmInterstitialAd().isLoaded()) {
                ((TabHoster)getActivity()).getmInterstitialAd().show();
            }
        }
        if (PlayerState.getInstance().getCurrentRadioEntity() != null) {
            ImageView rib = (ImageView) mainFragmentView.findViewById(R.id.recordButton);
            if (PlayerState.getInstance().isRecActive()) {
                PlayerState.getInstance().setRecActive(false);
                rib.setImageResource(R.drawable.rec);
            } else {
                PlayerState.getInstance().setRecActive(true);
                rib.setImageResource(R.drawable.rec_active);
            }
        }
    }

    /**
     * Установка времени в вьюху
     *
     * @param h Часы
     * @param m Минуты
     */
    void setTimer(int h, int m) {
        hour = h;
        minute = m;
        TextView timeTv = (TextView) mainFragmentView.findViewById(R.id.tvChangeTime);
        String str = "";
        if (minute < 10)
            str = "0";
        timeTv.setText(hour + ":" + str + minute);
    }

    public TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            am.cancel(sender);
            setTimer(hourOfDay, minute);
            Calendar calNow = Calendar.getInstance();
            Calendar cal = Calendar.getInstance();
            // add 5 minutes to the calendar object
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.SECOND, 0);
            Long alarmMillis = cal.getTimeInMillis();
            // is chkIos checked?
            if (calNow.after(cal)) {
                alarmMillis += 86400000L; // Add 1 day if time selected
                // before now
                cal.setTimeInMillis(alarmMillis);
            }
            if (cb1.isChecked()) {
                am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
            }
        }
    };

    protected void showDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
                // set time picker as current time
                new TimePickerDialog(getActivity(), timePickerListener, Calendar
                        .getInstance().get(Calendar.HOUR_OF_DAY), Calendar
                        .getInstance().get(Calendar.MINUTE), true).show();
                break;
        }
    }

    public void onTimerClick(View v) {
        showDialog(TIME_DIALOG_ID);
    }
}