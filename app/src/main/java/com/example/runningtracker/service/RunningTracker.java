package com.example.runningtracker.service;


import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class RunningTracker {
    private final String permissionDialogTitle = "Location permission not granted";
    private final String permissionDialogMessage = "The location permission is required to track your running statistics";
    private int duration;
    private double distance;
    private double avgPace;
    private Timer timer;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private RunningTrackerState state;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Activity activity;
    private Location prevLocation;
    private Boolean isStartToStop;
    private String sessionNote;
    private Boolean isNewRunSession;

    public RunningTracker() {
        duration = 0;
        distance = 0.0;
        avgPace = 0.0;
        sessionNote = "";
        state = RunningTrackerState.ENDED;
        isStartToStop = true;
        isNewRunSession = true;
    }

    public void registerActivity(Activity activity) {
        this.activity = activity;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (isStartToStop == true) {
                    for (Location location : locationResult.getLocations()) {
                        Log.d(TAG, "onLocationResult: SET PREVLOCATION to current location");
                        prevLocation = location;
                    }
                    isStartToStop = false;
                } else {
                    for (Location location : locationResult.getLocations()) {

                        float distance = prevLocation.distanceTo(location);
                        Log.d(TAG, "onLocationResult: SET NEW LOCATION = " + distance);
                        RunningTracker.this.distance += distance;
                        prevLocation = location;
                    }
                }
            }
        };

        // when activity is registered, checks for permissions
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog permissionDialog = new AlertDialog.Builder(activity).create();
            permissionDialog.setTitle(permissionDialogTitle);
            permissionDialog.setMessage(permissionDialogMessage);
            permissionDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) -> {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            });
            permissionDialog.show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    public void setSessionNote(String sessionNote) {
        this.sessionNote = sessionNote;
    }

    public enum RunningTrackerState {
        TRACKING,
        PAUSED,
        ENDED,
    }

    public void start() {
        // when start is pressed, make sure that location permission is granted
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog permissionDialog = new AlertDialog.Builder(activity).create();
            permissionDialog.setTitle(permissionDialogTitle);
            permissionDialog.setMessage(permissionDialogMessage);
            permissionDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) -> {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            });
            permissionDialog.show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (state == RunningTrackerState.PAUSED || state == RunningTrackerState.ENDED) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }

            state = RunningTrackerState.TRACKING;
            // increment timer every second
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    duration++;
                    avgPace = distance / duration;
                }
            }, 0, 1000);

            // only sets the startDateTime, the first time a run session starts
            if (isNewRunSession == true) {

                startDateTime = LocalDateTime.now();

                isNewRunSession = false;
            }
        }
    }

    public void pause() {
        if (state == RunningTrackerState.TRACKING) {
            state =  RunningTrackerState.PAUSED;
            // Need to set isStartToStop true so that the tracker will start tracking from the
            // position when start button is pressed.
            isStartToStop = true;

            fusedLocationClient.removeLocationUpdates(locationCallback);
            timer.cancel();
        }
    }

    public void end() {
        duration = 0;
        distance = 0.0;
        avgPace = 0.0;
        startDateTime = null;
        endDateTime = null;
        sessionNote = "";
        state = RunningTrackerState.ENDED;
        // Need to set isFirstTime true so that the tracker will start tracking from the
        // position when start is pressed.
        isStartToStop = true;
        isNewRunSession = true;
        fusedLocationClient.removeLocationUpdates(locationCallback);
        timer.cancel();
    }

    public double getDistance() {
        return distance;
    }

    public int getDuration() {
        return duration;
    }

    public double getAvgPace() {
        return avgPace;
    }

    public LocalDateTime getStartDateTime() {return startDateTime;}

    public LocalDateTime getEndDateTime() {return endDateTime;}

    public String getSessionNote() {return sessionNote;}

    public RunningTrackerState getState() {
        return state;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }
}
