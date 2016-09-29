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

import com.un4seen.bass.BASS;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import ru.sigil.fantasyradio.BackgroundService.Bitrate;
import ru.sigil.fantasyradio.BackgroundService.IPlayer;
import ru.sigil.fantasyradio.BackgroundService.IPlayerEventListener;
import ru.sigil.fantasyradio.BackgroundService.PlayState;
import ru.sigil.fantasyradio.dagger.Bootstrap;
import ru.sigil.fantasyradio.utils.AlarmReceiever;
import ru.sigil.fantasyradio.widget.FantasyRadioWidgetProvider;

public class RadioFragment extends Fragment {

    private int hour;
    private int minute;
    private AlarmManager am;
    private PendingIntent sender;
    private CheckBox cb1;
    private View mainFragmentView;
    public final int TIME_DIALOG_ID = 999;
    private static final int AD_SHOW_PROBABILITY_REC = 25;
    private static final int AD_SHOW_PROBABILITY_URL = 4;
    private static final int AD_SHOW_PROBABILITY_PLAY = 5;

    private static final Map<Integer, Bitrate> bitratesMap;

    static {
        bitratesMap = new HashMap<>();
        bitratesMap.put(0, Bitrate.aac_16);
        bitratesMap.put(1, Bitrate.mp3_32);
        bitratesMap.put(3, Bitrate.aac_112);
        bitratesMap.put(4, Bitrate.mp3_96);
    }

    @Inject
    IPlayer player;

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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().finish();
                    }
                });
            } else {
                BASS.BASS_Free();
            }
        }
    };

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
        if (BuildConfig.FLAVOR.equals("free") && getRandom().nextInt(100) < AD_SHOW_PROBABILITY_PLAY) {
            if (((TabHoster) getActivity()).getmInterstitialAd().isLoaded()) {
                ((TabHoster) getActivity()).getmInterstitialAd().show();
            }
        }
        if (player.currentState() != PlayState.PLAY) {
            if (player.currentBitrate() == Bitrate.aac_16) {
                player.playAAC(getString(R.string.stream_url_AAC16), Bitrate.aac_16);
            }
            if (player.currentBitrate() == Bitrate.aac_112) {
                player.playAAC(getString(R.string.stream_url_AAC112), Bitrate.aac_112);
            }
            if (player.currentBitrate() == Bitrate.mp3_32) {
                player.play(getString(R.string.stream_url_MP332), Bitrate.mp3_32);
            }
            if (player.currentBitrate() == Bitrate.mp3_96) {
                player.play(getString(R.string.stream_url_MP396), Bitrate.mp3_96);
            }
        } else {
            player.stop();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bootstrap.INSTANCE.getBootstrap().inject(this);
        player.addEventListener(eventListener);
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
        return mainFragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        player.removeEventListener(eventListener);
    }

    /**
     * Выбор битрэйта
     *
     * @param v Вьюха, в тэге содержится строка с битрэйтом (URL)
     */

    private OnClickListener bitrateClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (BuildConfig.FLAVOR.equals("free") && getRandom().nextInt(100) < AD_SHOW_PROBABILITY_URL) {
                if (((TabHoster) getActivity()).getmInterstitialAd().isLoaded()) {
                    ((TabHoster) getActivity()).getmInterstitialAd().show();
                }
            }
            mainFragmentView.findViewById(R.id.bitrateText0).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
            mainFragmentView.findViewById(R.id.bitrateText1).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
            mainFragmentView.findViewById(R.id.bitrateText3).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
            mainFragmentView.findViewById(R.id.bitrateText4).setBackgroundColor(
                    getResources().getColor(R.color.bitrate_element));
            v.setBackgroundColor(getResources().getColor(
                    R.color.bitrate_element_active));
            //TODO
            player.setBitrate(bitratesMap.get(Integer.valueOf(v.getTag().toString())));
            if (player.currentState() == PlayState.PLAY) {
                ImageView b = (ImageView) mainFragmentView.findViewById(R.id.streamButton);
                b.performClick();
                b.performClick();
            }
        }
    };

    @Override
    public void onResume() {
        SeekBar sb = (SeekBar) mainFragmentView.findViewById(R.id.mainVolumeSeekBar);
        sb.setProgress((int) (player.getVolume() * 100));
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

        mainFragmentView.findViewById(R.id.bitrateText0).setBackgroundColor(
                getResources().getColor(R.color.bitrate_element));
        mainFragmentView.findViewById(R.id.bitrateText1).setBackgroundColor(
                getResources().getColor(R.color.bitrate_element));
        mainFragmentView.findViewById(R.id.bitrateText3).setBackgroundColor(
                getResources().getColor(R.color.bitrate_element));
        mainFragmentView.findViewById(R.id.bitrateText4).setBackgroundColor(
                getResources().getColor(R.color.bitrate_element));
        switch (player.currentBitrate()) {
            case aac_16:
                mainFragmentView.findViewById(R.id.bitrateText0).setBackgroundColor(
                        getResources().getColor(R.color.bitrate_element_active));
                break;
            case mp3_32:
                mainFragmentView.findViewById(R.id.bitrateText1).setBackgroundColor(
                        getResources().getColor(R.color.bitrate_element_active));
                break;
            case mp3_96:
                mainFragmentView.findViewById(R.id.bitrateText4).setBackgroundColor(
                        getResources().getColor(R.color.bitrate_element_active));
                break;
            case aac_112:
                mainFragmentView.findViewById(R.id.bitrateText3).setBackgroundColor(
                        getResources().getColor(R.color.bitrate_element_active));
                break;

        }

        //TODO--------------------------------------------------------------------------------------
        if (player.currentState() != PlayState.STOP) {
            ImageView iv = (ImageView) mainFragmentView.findViewById(R.id.streamButton);
            iv.setImageResource(R.drawable.pause_states);
            ImageView rib = (ImageView) mainFragmentView.findViewById(R.id.recordButton);
            if (player.isRecActive()) {
                rib.setImageResource(R.drawable.rec_active);
            } else {
                rib.setImageResource(R.drawable.rec);
            }
            TextView tv1 = (TextView) mainFragmentView.findViewById(R.id.textView1);
            if (player.currentState() != PlayState.STOP) {
                tv1.setText(player.currentTitle());
            }
        }
        //------------------------------------------------------------------------------------------
        super.onResume();
    }

    /**
     * Запись потока.
     */
    private void streamRecordClick() {
        if (BuildConfig.FLAVOR.equals("free") && getRandom().nextInt(100) < AD_SHOW_PROBABILITY_REC) {
            if (((TabHoster) getActivity()).getmInterstitialAd().isLoaded()) {
                ((TabHoster) getActivity()).getmInterstitialAd().show();
            }
        }
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

    private final IPlayerEventListener eventListener = new IPlayerEventListener() {
        @Override
        public void onTitleChanged(final String title) {
            RadioEntity re = new RadioEntity();
            re.setTitle(title);
            re.setArtist(re.getArtist());
            re.setStation(re.getStation());
            getActivity().runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) mainFragmentView.findViewById(R.id.textView1)).setText(title);
                        }
                    }
            );
        }

        @Override
        public void onAuthorChanged(final String author) {
            getActivity().runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (author != null || !author.isEmpty()) {
                                ((TextView) mainFragmentView.findViewById(R.id.textView1))
                                        .setText(author + " - " + player.currentTitle());
                            } else {
                                ((TextView) mainFragmentView.findViewById(R.id.textView1))
                                        .setText(player.currentTitle());
                            }
                        }
                    }
            );
        }

        @Override
        public void onPlayStateChanged(PlayState state) {
            ImageView iv = (ImageView) mainFragmentView.findViewById(R.id.streamButton);
            switch (state) {
                case PLAY:
                    iv.setImageResource(R.drawable.pause_states);
                    break;
                case PAUSE:
                case STOP:
                    iv.setImageResource(R.drawable.play_states);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onRecStateChanged(final boolean isRec) {
            getActivity().runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            ImageView rib = (ImageView) mainFragmentView.findViewById(R.id.recordButton);
                            if (isRec) {
                                rib.setImageResource(R.drawable.rec_active);
                            } else {
                                rib.setImageResource(R.drawable.rec);
                            }
                        }
                    }
            );
        }

        @Override
        public void onBitrateChanged(Bitrate bitrate) {

        }

        @Override
        public void onBufferingProgress(final long progress) {
            getActivity().runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) mainFragmentView.findViewById(R.id.textView1)).setText(String.format("BUFFERING... %d%%", progress));
                        }
                    }
            );
        }

        @Override
        public void onStop() {
            //TODO
        }

        @Override
        public void endSync()
        {

        }
    };

    private void updateWidget() {
        Intent intent = new Intent(getActivity().getApplicationContext(), FantasyRadioWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(getActivity().getApplicationContext()).getAppWidgetIds(
                new ComponentName(getActivity().getApplicationContext(), FantasyRadioWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        getActivity().getApplicationContext().sendBroadcast(intent);
    }
}