package skynetlabz.com.simplestworkouttimer;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "WORKOUT_HELPER";
    RingProgressBar rpb;
    private static boolean running = false;
    Snackbar sb;
    ToneGenerator toneGen;

    EditText et_sets;
    EditText et_time_workout;
    EditText et_time_rest;
    TextView tv_status;

    PowerManager pm;


    PowerManager.WakeLock wl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_sets = (EditText) findViewById(R.id.et_sets);
        et_time_workout = (EditText) findViewById(R.id.et_time_workout);
        et_time_rest = (EditText) findViewById(R.id.et_time_rest);
        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_status.setText("");

        rpb = (RingProgressBar) findViewById(R.id.timer_progress_bar);
        rpb.setProgress(0);
        rpb.setShow_symbol(false);
        rpb.setRingColor(R.color.timer_green);
        rpb.setRingProgressColor(R.color.timer_green);
        rpb.setTextColor(R.color.timer_text_color);

        toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
    }


    //    @OnClick(R.id.btn_start_timer)
    public void start_timer(View v) {

        if (!running)
            timer_run();
        else {
            sb = Snackbar.make(findViewById(R.id.main_layout_view), R.string.sb_timer_running, Snackbar.LENGTH_INDEFINITE);
            sb.setAction(R.string.sb_btn_stop_current, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    running = false;
                }
            });
            sb.show();
        }
    }

    void check_and_release() {
        if (wl != null) {
            Log.v(TAG, "Releasing wakelock");
            try {
                wl.release();
            } catch (Throwable th) {
                // ignoring this exception, probably wakeLock was already released
            }
        } else {
            // should never happen during normal workflow
            Log.e(TAG, "Wakelock reference is null");
        }
    }

    private void timer_run() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wl.acquire();
                    running = true;
                    int sets = Integer.parseInt(et_sets.getText().toString());
                    int timeworkout = Integer.parseInt(et_time_workout.getText().toString());
                    int timerest = Integer.parseInt(et_time_rest.getText().toString());
                    int i = 0, j = 0;
                    rpb.setMax(5);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_status.setText(R.string.str_status_get_ready);
                        }
                    });
                    while (i < rpb.getMax()) {
                        i++;
                        rpb.setProgress(i);
                        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 90);
                        Thread.sleep(950);
                    }
                    while (j < sets && running) {
                        toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 250);
                        i = 0;
                        rpb.setMax(timeworkout);
                        rpb.setProgress(0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_status.setText(R.string.str_status_workout);
                            }
                        });
                        while (rpb.getProgress() < rpb.getMax() && running) {
                            if (i < rpb.getMax() - 3) {
                                rpb.setProgress(i);
                            } else if (i >= rpb.getMax() - 3) {
                                rpb.setProgress(i);
                                toneGen.startTone(ToneGenerator.TONE_SUP_PIP, 90);
                            }
                            i++;
                            Thread.sleep(950);
                        }
                        i = 0;
                        toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 250);

                        if (j + 1 >= sets) {
                            Thread.sleep(500);
                            toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 250);
                            Thread.sleep(500);
                            toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 250);
                            running = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_status.setText(R.string.str_status_done);
                                    check_and_release();
                                }
                            });
                            break;
                        }
                        rpb.setMax(timerest);
                        rpb.setProgress(0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_status.setText(R.string.str_status_rest);
                            }
                        });
                        while (rpb.getProgress() < rpb.getMax() && running && j + 1 < sets) {
                            if (i < rpb.getMax() - 3) {
                                rpb.setProgress(i);
                            } else if (i >= rpb.getMax() - 3) {
                                rpb.setProgress(i);
                                toneGen.startTone(ToneGenerator.TONE_SUP_PIP, 90);
                            }
                            Thread.sleep(950);
                            i++;
                        }
                        j++;
                    }
                    if (!running)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_status.setText(R.string.str_timer_stopped);
                            }
                        });
                    check_and_release();
//                    wl.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (running) {
            sb = Snackbar.make(findViewById(R.id.main_layout_view), R.string.sb_timer_running, Snackbar.LENGTH_INDEFINITE);
            sb.setAction(R.string.sb_btn_stop_current, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    running = false;
                    sb.dismiss();
                    Toast.makeText(MainActivity.this, R.string.press_again_to_exit, Toast.LENGTH_LONG).show();
                }
            });
            sb.show();
        } else
            super.onBackPressed();
    }
}
