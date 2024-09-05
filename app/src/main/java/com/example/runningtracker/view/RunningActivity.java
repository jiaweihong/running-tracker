package com.example.runningtracker.view;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.runningtracker.viewmodel.RunViewModel;
import com.example.runningtracker.miscellaneous.Helper;
import com.example.runningtracker.service.ICallback;
import com.example.runningtracker.R;
import com.example.runningtracker.model.Run;
import com.example.runningtracker.service.RunningTracker;
import com.example.runningtracker.service.RunningTrackerService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RunningActivity extends AppCompatActivity {
    private TextView distanceTxt;
    private TextView durationTxt;
    private TextView avgPaceTxt;
    private Button startPauseBtn;
    private Button sessionNoteBtn;
    private Button endBtn;

    private RunningTrackerService.MyBinder myIBinder;

    private EditText dialogInputTxt;

    private RunViewModel runViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        this.startService(new Intent(this, RunningTrackerService.class));
        this.bindService(new Intent(this, RunningTrackerService.class
        ), serviceConnection, Context.BIND_AUTO_CREATE);

        // assign UI
        distanceTxt = findViewById(R.id.distanceTxt);
        durationTxt = findViewById(R.id.durationTxt);
        avgPaceTxt = findViewById(R.id.avgPaceTxt);
        startPauseBtn = findViewById(R.id.startPauseBtn);
        sessionNoteBtn = findViewById(R.id.sessionNoteBtn);
        endBtn = findViewById(R.id.endBtn);

        // the dialogInputTxt needs to be created here because if not, onServiceConnected it would
        // be null which would throw an exception.
        dialogInputTxt = new EditText(this);
        dialogInputTxt.setInputType(InputType.TYPE_CLASS_TEXT);

        runViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(RunViewModel.class);
    }

    // connect to service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myIBinder = (RunningTrackerService.MyBinder) iBinder;
            myIBinder.registerCallback(callback);
            myIBinder.registerActivity(RunningActivity.this);

            // When activity is recreated, and it is still tracking a run.
            // Restore UI state to the tracking form
            if (myIBinder.getState() == RunningTracker.RunningTrackerState.TRACKING) {
                startPauseBtn.setText("PAUSE");
                endBtn.setVisibility(View.VISIBLE);
                sessionNoteBtn.setVisibility(View.VISIBLE);
            } else if (myIBinder.getState() == RunningTracker.RunningTrackerState.PAUSED) {
                startPauseBtn.setText("START");
                endBtn.setVisibility(View.VISIBLE);
                sessionNoteBtn.setVisibility(View.VISIBLE);
            } else if (myIBinder.getState() == RunningTracker.RunningTrackerState.ENDED) {
                startPauseBtn.setText("START");
                endBtn.setVisibility(View.GONE);
                sessionNoteBtn.setVisibility(View.GONE);
            }

            //  the text is set here because myIBinder must exist first
            dialogInputTxt.setText(myIBinder.getSessionNote());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myIBinder.unregisterCallback(callback);
            myIBinder = null;
        }
    };


    private ICallback callback = new ICallback() {
        @Override
        public void updateDurationEvent(int duration) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    durationTxt.setText((Helper.secToHourMinSecString(duration)));
                }
            });
        }

        @Override
        public void updateDistanceEvent(double distance) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    distanceTxt.setText(Math.round(distance) + " m");
                }
            });
        }

        @Override
        public void updateAvgPaceEvent(double avgPace) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // rounds to 2 dp
                    avgPaceTxt.setText(Math.round(avgPace * 100.0) / 100.0 + " m/s");
                }
            });
        }

        @Override
        public void saveRunEvent(double distance, int duration, double avgPace, LocalDateTime startDateTime, LocalDateTime endDateTime, String sessionNote) {

            String startDateTimeStr = null;
            startDateTimeStr = startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String endDateTimeStr = null;
            endDateTimeStr = endDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


            // id is set to 0 because, this means that db will auto increment it
            // start/endDatetime are converted to strings before saving because sqllite is not able to save LocalDateTime format.
            Run run = new Run(0, distance, duration, Math.round(avgPace * 100.0) / 100.0, startDateTimeStr, endDateTimeStr, sessionNote);

            runViewModel.insert(run);

            isAndShowNewDistanceMilestone(run);

        }

        @Override
        public void updateSessionNoteEvent(String sessionNote) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogInputTxt.setText(sessionNote);
                }
            });
        }
    };


    public void onStartPauseBtnClick(View v) {
        RunningTracker.RunningTrackerState state = myIBinder.getState();
        if (state == RunningTracker.RunningTrackerState.PAUSED || state == RunningTracker.RunningTrackerState.ENDED) {
            myIBinder.start();
            startPauseBtn.setText("PAUSE");
        } else if (state == RunningTracker.RunningTrackerState.TRACKING) {
            myIBinder.pause();
            startPauseBtn.setText("START");
        }

        sessionNoteBtn.setVisibility(View.VISIBLE);
        endBtn.setVisibility(View.VISIBLE);
    }

    public void onEndBtnClick(View v) {
        myIBinder.pause();

        // Create popup to see if user wants to save run or not
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        // Set up the buttons
        dialog.setPositiveButton("Save run", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myIBinder.endAndSave();
            }
        });
        dialog.setNegativeButton("Delete run", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                myIBinder.endAndDontSave();
            }
        });

        // prevents the dialog from being cancelled unless a button is pressed
        dialog.setCancelable(false);

        dialog.show();

        startPauseBtn.setText("START");
        endBtn.setVisibility(View.GONE);
        sessionNoteBtn.setVisibility(View.GONE);
    }

    public void onNoteBtnClick(View v) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Session notes");

        dialogInputTxt = new EditText(this);
        dialogInputTxt.setInputType(InputType.TYPE_CLASS_TEXT);
        dialogInputTxt.setText(myIBinder.getSessionNote());
        dialog.setView(dialogInputTxt);

        // Set up the buttons
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myIBinder.setSessionNote(dialogInputTxt.getText().toString());
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public void onProfileBtnClick(View v){
        Intent intent = new Intent(RunningActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    public void onRunningBtnClick(View v){
        // do nothing
    }

    public void isAndShowNewDistanceMilestone(Run newRun){
        runViewModel.getAllRunsByIdDesc().observe(this, new Observer<List<Run>>() {
            @Override
            public void onChanged(List<Run> runs) {
                if (runs.size() >= 1) {
                    Run longestRun = runs.get(0);

                    //get current highest run ( this list includes the newly added 'newRun')
                    for (Run r: runs){
                        if (r.getDistance() > longestRun.getDistance()) {
                            longestRun = r;
                        }
                    }

                    if (newRun.getDistance() > 0) {
                        // if new run is the longest run in the list then it is a pb
                        if (newRun.getDistance() == longestRun.getDistance()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(RunningActivity.this);
                            builder.setTitle("Congratulations! You just hit a new distance PB of " + Math.round(newRun.getDistance() * 100.0) / 100.0 + " m");

                            // Set up the buttons
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    myIBinder.setSessionNote(dialogInputTxt.getText().toString());
                                }
                            });

                            builder.show();
                        }
                    }
                }
            }
        });
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, RunningTrackerService.class));
        unbindService(serviceConnection);
    }
}