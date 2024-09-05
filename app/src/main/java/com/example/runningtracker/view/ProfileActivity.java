package com.example.runningtracker.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.runningtracker.R;
import com.example.runningtracker.model.Run;
import com.example.runningtracker.viewmodel.RunViewModel;
import com.example.runningtracker.miscellaneous.Helper;
import com.example.runningtracker.miscellaneous.RunAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private RunViewModel runViewModel;
    private TextView totalDistanceTxt;
    private TextView totalDurationTxt;
    private final int TODAY = 0;
    private final int ALL_TIME = 1;
    Spinner dropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        totalDistanceTxt = findViewById(R.id.totalDistanceTxt);
        totalDurationTxt = findViewById(R.id.totalDurationTxt);

        RunAdapter runAdapter = new RunAdapter(this.getApplication(), this);
        recyclerView.setAdapter(runAdapter);


        dropdown = findViewById(R.id.allTimeOrTodaySpinner);
        String[] items = new String[]{"Today", "All time"};
        // describes how items are displayed
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        // set a listener so that the onitemselected and onnothingselected will be called for that dropdown
        dropdown.setOnItemSelectedListener(this);

        runViewModel = ViewModelProviders.of(this).get(RunViewModel.class);
        runViewModel.getAllRunsByIdDesc().observe(this, new Observer<List<Run>>() {
            @Override
            public void onChanged(List<Run> runs) {
                runAdapter.setRuns(runs);

                dropdown.setSelection(TODAY);
                List<Run> runsToSum = new ArrayList<Run>();

                if (dropdown.getSelectedItemPosition() == ALL_TIME){
                    runsToSum = runs;
                } else if (dropdown.getSelectedItemPosition() == TODAY) {
                    // add only runs that are today
                    for (Run run:runs) {
                        if (isRunToday(run) == true) {
                            runsToSum.add(run);
                        }
                    }
                }

                double totalDistance = sumTotalRunDistances(runsToSum);
                int totalDuration = sumTotalRunDurations(runsToSum);

                totalDistanceTxt.setText(Helper.mToKmString(totalDistance) + " km");
                totalDurationTxt.setText(Helper.secToHourMinSecString(totalDuration));
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        runViewModel.getAllRunsByIdDesc().observe(this, new Observer<List<Run>>() {
            @Override
            public void onChanged(List<Run> runs) {
                List<Run> runsToSum = new ArrayList<Run>();

                if (pos == ALL_TIME ){
                    runsToSum = runs;
                } else if (pos == TODAY){
                    // add only runs that are today
                    for (Run run:runs) {
                        if (isRunToday(run) == true) {
                            runsToSum.add(run);
                        }
                    }
                }

                double totalDistance = sumTotalRunDistances(runsToSum);
                int totalDuration = sumTotalRunDurations(runsToSum);

                totalDistanceTxt.setText(Helper.mToKmString(totalDistance) + " km");
                totalDurationTxt.setText(Helper.secToHourMinSecString(totalDuration));
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // do nothing
    }

    public boolean isRunToday(Run run) {
        // convert the current run's datetime to date object so it can be compared to todays date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime runDateTime = LocalDateTime.parse(run.getStartDateTimeStr(), formatter);
        LocalDate runDate = runDateTime.toLocalDate();
        return runDate.isEqual(LocalDate.now());
    }

    public void onProfileBtnClick(View v){
        // do nothing
    }

    public void onRunningBtnClick(View v){
        Intent intent = new Intent(ProfileActivity.this, RunningActivity.class);
        startActivity(intent);
    }

    public double sumTotalRunDistances(List<Run> runs) {
        double sum = 0.0;


        for (Run r:runs) {
            double distance = r.getDistance();
            sum += distance;
        }

        return sum;
    }

    public int sumTotalRunDurations(List<Run> runs) {
        int sum = 0;

        for (Run r:runs) {
            int duration = r.getDuration();
            sum += duration;
        }

        return sum;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putInt("selectedDropdownPos", dropdown.getSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle bundle) {
        super.onRestoreInstanceState(bundle);

        int selectedDropdownPos = (int) bundle.get("selectedDropdownPos");
        dropdown.setSelection(selectedDropdownPos);
    }
}