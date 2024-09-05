package com.example.runningtracker.model;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// entity is defines our table and its relevant fields
@Entity(tableName = "run_table")
public class Run {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    private double distance;
    private int duration;
    private String startDateTimeStr;
    private String endDateTimeStr;
    private double avgPace;
    private String note;

    public Run(int id, double distance, int duration, double avgPace, String startDateTimeStr, String endDateTimeStr, String note) {
        // the id argument will always be passed value of 0, because autogenerate will then know
        // to auto increment
        this.id = id;
        this.startDateTimeStr = startDateTimeStr;
        this.endDateTimeStr = endDateTimeStr;
        this.avgPace = avgPace;
        this.distance = distance;
        this.duration = duration;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public double getDistance() {
        return distance;
    }

    public double getAvgPace() { return avgPace; }

    public int getDuration() {
        return duration;
    }

    public String getStartDateTimeStr() {return startDateTimeStr;}

    public String getEndDateTimeStr() {return endDateTimeStr;}

    public String getNote() {return note;}
}
