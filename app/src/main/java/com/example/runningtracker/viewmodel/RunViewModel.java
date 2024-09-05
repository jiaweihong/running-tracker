package com.example.runningtracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.runningtracker.model.MyRepository;
import com.example.runningtracker.model.Run;

import java.util.List;

// RunViewModel is the first point of contact to access database
public class RunViewModel extends AndroidViewModel {
    private MyRepository repository;
    private LiveData<List<Run>> allRuns;

    public RunViewModel(@NonNull Application application) {
        super(application);
        repository = new MyRepository(application);
        allRuns = repository.getAllRunsByIdDesc();
    }

    public LiveData<List<Run>> getAllRunsByIdDesc() {
        return allRuns;
    }

    public void insert(Run run) {
        repository.insert(run);
    }


    public void updateRunNote(int id, String note) { repository.updateRunNote(id, note); };
}
