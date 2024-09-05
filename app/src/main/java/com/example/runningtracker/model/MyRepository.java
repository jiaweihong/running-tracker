package com.example.runningtracker.model;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

// abstraction interface for the  database, repository is the object to access to get the db
public class MyRepository {
    private RunDatabase runDb;
    private RunDao runDao;
    private LiveData<List<Run>> allRuns;

    public MyRepository(Application application) {
        // db
        runDb = RunDatabase.getDatabase(application);
        runDao = runDb.runDao();

        allRuns = runDao.getAllRunsByIdDesc();
    }

    public LiveData<List<Run>> getAllRunsByIdDesc() {
        return allRuns;
    }

    public void insert(Run run) {
        runDb.databaseWriteExecutor.execute(() -> {
            runDao.insert(run);
        });
    }

    public void updateRunNote(int id, String note) {
        runDb.databaseWriteExecutor.execute(() -> {
            runDao.updateRunNote(id, note);
        });
    }
}
