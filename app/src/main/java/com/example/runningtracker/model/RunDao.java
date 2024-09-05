package com.example.runningtracker.model;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

// this generates the interface that lets us interact with the database
@Dao
public interface RunDao {

    // if you put 2 of the same run, it will abort
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(Run run);

    @Query("UPDATE run_table SET note = :note WHERE id = :id")
    void updateRunNote(int id, String note);

    @Query("SELECT * FROM run_table ORDER BY id DESC")
    LiveData<List<Run>> getAllRunsByIdDesc();

    @Query("SELECT * FROM run_table")
    Cursor getAllRuns();

    @Query("SELECT * FROM run_table WHERE id = :id")
    Cursor getRunById(int id);
}
