package ru.sigil.fantasyradio;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.un4seen.bass.BASS;

import java.util.Calendar;
import java.util.Random;

import javax.inject.Inject;

import ru.sigil.bassplayerlib.IPlayer;
import ru.sigil.bassplayerlib.PlayState;
import ru.sigil.bassplayerlib.listeners.IAuthorChangedListener;
import ru.sigil.bassplayerlib.listeners.IBufferingProgressListener;
import ru.sigil.bassplayerlib.listeners.IPlayStateChangedListener;
import ru.sigil.bassplayerlib.listeners.IRecStateChangedListener;
import ru.sigil.bassplayerlib.listeners.ITitleChangedListener;
import ru.sigil.bassplayerlib.listeners.IVolumeChangedListener;
import ru.sigil.fantasyradio.ad.AdService;
import ru.sigil.fantasyradio.currentstreraminfo.CurrentStreamInfoService;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.utils.AlarmReceiever;
import ru.sigil.fantasyradio.utils.Bitrate;
import ru.sigil.fantasyradio.utils.RadioStream;
import ru.sigil.fantasyradio.utils.RadioStreamFactory;
import ru.sigil.fantasyradio.widget.FantasyRadioWidgetProvider;
import ru.sigil.log.LogManager;

public class RadioFragment extends Fragment {
    private static final String TAG = RadioFragment.class.getSimpleName();
    private int hour;
    private int minute;
    private AlarmManager am;
    private PendingIntent sender;
    private CheckBox cb1;
    private View mainFragmentView;
    private String currentStreamAbout = "";
    private String currentStreamImageUrl = "";
    public final int TIME_DIALOG_ID = 999;
    private static final int AD_SHOW_PROBABILITY_REC = 25;
    private static final int AD_SHOW_PROBABILITY_URL = 4;
    private static final int AD_SHOW_PROBABILITY_PLAY = 5;

    private static final SparseArray<Bitrate> bitrates;

    static {
        bitrates = new SparseArray<>();
        bitrates.put(0, Bitrate.aac_16);
        bitrates.put(1, Bitrate.mp3_32);
        bitrates.put(2, Bitrate.mp3_96);
        bitrates.put(3, Bitrate.aac_112);
    }

    @Inject
    IPlayer<RadioStream> player;
    @Inject
    RadioStreamFactory radioStreamFactory;
    @Inject
    CurrentStreamInfoService currentStreamInfoService;
    @Inject
    AdService adService;


    private Random random;

    /**
     * Хэндлер, закрывающий приложение
     *
     * @see ru.sigil.fantasyradio.utils.AlarmReceiever
     */
    private Handler sleepTimerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> getActivity().finish());
            } else {
                BASS.BASS_Free();
            }
        }
    };

    private final static int CURRENT_INFO_UPDATE_INTERVAL = 1000 * 60; //1 minute
    Handler currentInfoUpdateHandler = new Handler();

    Runnable currentInfoUpdateHandlerTask = new Runnable() {
        @Override
        public void run() {
            //doSomething();
            LogManager.d(TAG, "UPDATE STREAM INFO");
            //TODO Передавать это в функцию коллбэком?
            currentStreamInfoService.updateInfo((about, imageUrl) -> getActivity().runOnUiThread(() ->
                    {
                        currentStreamAbout = about;
                        currentStreamImageUrl = imageUrl;
                        updateCurrentStreamInfo(about, imageUrl);
                    }
            ));
            currentInfoUpdateHandler.postDelayed(currentInfoUpdateHandlerTask, CURRENT_INFO_UPDATE_INTERVAL);
        }
    };

    private void updateCurrentStreamInfo(String about, String imageUrl) {
        ((TextView) mainFragmentView.findViewById(R.id.currentInfoAbout)).setText(about);
        if (imageUrl.length() > 0) {
            ImageLoader.getInstance().displayImage("http://fantasyradio.ru/" + imageUrl, (ImageView) mainFragmentView.findViewById(R.id.currentInfoImage));
        } else {
            ((ImageView) mainFragmentView.findViewById(R.id.currentInfoImage)).setImageDrawable(null);
        }
    }

    void startRepeatingCurrentInfoUpdatingTask() {
        currentInfoUpdateHandlerTask.run();
    }

    void stopRepeatingCurrentInfoUpdatingTask() {
        currentInfoUpdateHandler.removeCallbacks(currentInfoUpdateHandlerTask);
    }

    private Random getRandom() {
        if (random == null)
            random = new Random();
        return random;
    }

    /**
     * Кликныли на кнопку PLAY. Начинаем проигрывать выбранный поток.
     */
    public void streamButtonClick(View v) {
        updateWidget();
        adService.showAd(AD_SHOW_PROBABILITY_PLAY);
        if (player.currentState() != PlayState.PLAY) {
            RadioStream stream = radioStreamFactory.createStreamWithBitrate(player.currentStream().getBitrate());
            switch (player.currentStream().getBitrate()) {
                case aac_16:
                case aac_112:
                    player.playAAC(stream);
                    break;
                case mp3_32:
                case mp3_96:
                    player.play(stream);
                    break;
            }
        } else {
            player.stop();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainFragmentView = inflater.inflate(R.layout.activity_main, container, false);
        Bootstrap.INSTANCE.getBootstrap().inject(this);
        player.addTitleChangedListener(titleChangedListener);
        player.addAuthorChangedListener(authorChangedListener);
        player.addPlayStateChangedListener(playStateChangedListener);
        player.addRecStateChangedListener(recStateChangedListener);
        player.addBufferingProgressChangedListener(bufferingProgressListener);
        player.addVolumeChangedListener(volumeChangedListener);
        //------------------------------------------------------------------------------------------
        ImageView streamButton = mainFragmentView.findViewById(R.id.streamButton);
        streamButton.setOnClickListener(this::streamButtonClick);
        ImageView recordButton = mainFragmentView.findViewById(R.id.recordButton);
        recordButton.setOnClickListener(v -> streamRecordClick());
        mainFragmentView.findViewById(R.id.tvChangeTime).setOnClickListener(this::onTimerClick);
        //------------------------------------------------------------------------------------------
        //setContentView(R.layout.activity_main);
        AlarmReceiever.setSleepHandler(sleepTimerHandler);
        cb1 = mainFragmentView.findViewById(R.id.checkBox1);
        Intent intent = new Intent(getActivity().getBaseContext(), AlarmReceiever.class);
        sender = PendingIntent.getBroadcast(getActivity().getBaseContext(), 192837, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am = (AlarmManager) getActivity().getBaseContext().getSystemService(Context.ALARM_SERVICE);
        cb1.setOnClickListener(v -> {
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
        });
        setTimer(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar
                .getInstance().get(Calendar.MINUTE));
        return mainFragmentView;
    }

    private void initView() {
        Spinner spinner = mainFragmentView.findViewById(R.id.stream_quality_spinner);
        spinner.setSelection(bitrates.keyAt(bitrates.indexOfValue(player.currentStream().getBitrate())));
        spinner.setOnItemSelectedListener(bitrateSelected);
        SeekBar sb = mainFragmentView.findViewById(R.id.mainVolumeSeekBar);
        sb.setProgress((int) (player.getVolume() * 100));
        sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    player.setVolume(((float) progress) / 100);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        ImageView rib = mainFragmentView.findViewById(R.id.recordButton);
        if (player.isRecActive()) {
            rib.setImageResource(R.drawable.rec_active);
        } else {
            rib.setImageResource(R.drawable.rec);
        }
        ImageView iv = mainFragmentView.findViewById(R.id.streamButton);
        switch (player.currentState()) {
            case PLAY:
                if (player.currentArtist() != null && !player.currentArtist().isEmpty()) {
                    ((TextView) mainFragmentView.findViewById(R.id.textView1))
                            .setText(player.currentArtist() + " - " + player.currentTitle());
                } else {
                    ((TextView) mainFragmentView.findViewById(R.id.textView1))
                            .setText(player.currentTitle());
                }
                iv.setImageResource(R.drawable.pause_states);
                break;
            case BUFFERING:
                iv.setImageResource(R.drawable.pause_states);
                break;
            case PAUSE:
            case PLAY_FILE:
                ((TextView) mainFragmentView.findViewById(R.id.textView1))
                        .setText("");
                iv.setImageResource(R.drawable.play_states);
                break;
            case STOP:
                ((TextView) mainFragmentView.findViewById(R.id.textView1))
                        .setText("");
                iv.setImageResource(R.drawable.play_states);
                break;
            default:
                break;
        }
        updateCurrentStreamInfo(currentStreamAbout, currentStreamImageUrl);
    }

    @Override
    public void onDestroyView() {
        player.removeTitleChangedListener(titleChangedListener);
        player.removeAuthorChangedListener(authorChangedListener);
        player.removePlayStateChangedListener(playStateChangedListener);
        player.removeRecStateChangedListener(recStateChangedListener);
        player.removeBufferingProgressChangedListener(bufferingProgressListener);
        player.removeVolumeChangedListener(volumeChangedListener);
        super.onDestroyView();
    }

    private AdapterView.OnItemSelectedListener bitrateSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position != bitrates.keyAt(bitrates.indexOfValue(player.currentStream().getBitrate()))) {
                adService.showAd(AD_SHOW_PROBABILITY_URL);
                player.setStream(radioStreamFactory.createStreamWithBitrate(bitrates.get(position)));
                if (player.currentState() == PlayState.PLAY) {
                    ImageView b = mainFragmentView.findViewById(R.id.streamButton);
                    b.performClick();
                    b.performClick();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    public void onResume() {
        initView();
        startRepeatingCurrentInfoUpdatingTask();
        super.onResume();
    }

    @Override
    public void onPause() {
        stopRepeatingCurrentInfoUpdatingTask();
        super.onPause();
    }

    /**
     * Запись потока.
     */
    private void streamRecordClick() {
        adService.showAd(AD_SHOW_PROBABILITY_REC);
        if (player.isRecActive()) {
            player.rec(false);
        } else {
            player.rec(true);
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
        TextView timeTv = mainFragmentView.findViewById(R.id.tvChangeTime);
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

    private final ITitleChangedListener titleChangedListener = (title) -> {
        RadioEntity re = new RadioEntity();
        re.setTitle(title);
        re.setArtist(re.getArtist());
        re.setStation(re.getStation());
        getActivity().runOnUiThread(
                () -> ((TextView) mainFragmentView.findViewById(R.id.textView1)).setText(title)
        );
    };

    private final IAuthorChangedListener authorChangedListener = (author) -> getActivity().runOnUiThread(
            () -> {
                if (author != null && !author.isEmpty()) {
                    ((TextView) mainFragmentView.findViewById(R.id.textView1))
                            .setText(author + " - " + player.currentTitle());
                } else {
                    ((TextView) mainFragmentView.findViewById(R.id.textView1))
                            .setText(player.currentTitle());
                }
            }
    );

    private final IPlayStateChangedListener playStateChangedListener = (state) -> getActivity().runOnUiThread(
            () -> {
                ImageView iv = mainFragmentView.findViewById(R.id.streamButton);
                switch (state) {
                    case PLAY:
                    case BUFFERING:
                        iv.setImageResource(R.drawable.pause_states);
                        break;
                    case PAUSE:
                    case PLAY_FILE:
                        ((TextView) mainFragmentView.findViewById(R.id.textView1))
                                .setText("");
                    case STOP:
                        iv.setImageResource(R.drawable.play_states);
                        break;
                    default:
                        break;
                }
            }
    );

    private final IRecStateChangedListener recStateChangedListener = (isRec) -> getActivity().runOnUiThread(
            () -> {
                ImageView rib = mainFragmentView.findViewById(R.id.recordButton);
                if (isRec) {
                    rib.setImageResource(R.drawable.rec_active);
                } else {
                    rib.setImageResource(R.drawable.rec);
                }
            }
    );

    private final IBufferingProgressListener bufferingProgressListener = (progress) -> getActivity().runOnUiThread(
            () -> ((TextView) mainFragmentView.findViewById(R.id.textView1)).setText(String.format("BUFFERING... %d%%", progress))
    );

    private final IVolumeChangedListener volumeChangedListener = (volume) -> getActivity().runOnUiThread(
            () -> ((SeekBar) mainFragmentView.findViewById(R.id.mainVolumeSeekBar)).setProgress((int) (volume * 100))
    );

    private void updateWidget() {
        Intent intent = new Intent(getActivity().getApplicationContext(), FantasyRadioWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(getActivity().getApplicationContext()).getAppWidgetIds(
                new ComponentName(getActivity().getApplicationContext(), FantasyRadioWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        getActivity().getApplicationContext().sendBroadcast(intent);
    }
}