package com.example.runningtracker.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;

import androidx.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class RunningTrackerService extends Service {
    RunningTracker runningTracker;
    Timer timer;

    public RunningTrackerService() {
        runningTracker = new RunningTracker();
        timer = new Timer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        TimerTask t = new TimerTask() {
            @Override
            public void run() {
                doDurationCallback(runningTracker.getDuration());
                doDistanceCallback(runningTracker.getDistance());
                doAvgPaceCallback(runningTracker.getAvgPace());
            }
        };

        timer.schedule(t, 0, 500);

        return new MyBinder();
    }

    RemoteCallbackList<MyBinder> remoteCallbackList = new RemoteCallbackList<MyBinder>();

    private void doAvgPaceCallback(double avgPace) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i=0; i<n; i++) {
            remoteCallbackList.getBroadcastItem(i).callback.updateAvgPaceEvent(avgPace);
        }
        remoteCallbackList.finishBroadcast();
    }

    private void doSessionNoteCallback(String sessionNote) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i=0; i<n; i++) {
            remoteCallbackList.getBroadcastItem(i).callback.updateSessionNoteEvent(sessionNote);
        }
        remoteCallbackList.finishBroadcast();
    }

    private void doDurationCallback(int duration) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i=0; i<n; i++) {
            remoteCallbackList.getBroadcastItem(i).callback.updateDurationEvent(duration);
        }
        remoteCallbackList.finishBroadcast();
    }

    private void doDistanceCallback(double distance) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i=0; i<n; i++) {
            remoteCallbackList.getBroadcastItem(i).callback.updateDistanceEvent(distance);
        }
        remoteCallbackList.finishBroadcast();
    }

    private void doSaveRunCallback(double distance, int time, double avgPace, LocalDateTime startDateTime, LocalDateTime endDateTime, String sessionNote) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i=0; i<n; i++) {
            remoteCallbackList.getBroadcastItem(i).callback.saveRunEvent(distance, time, avgPace, startDateTime, endDateTime, sessionNote);
        }
        remoteCallbackList.finishBroadcast();
    }

    // Binder class
    public class MyBinder extends Binder implements IInterface {
        @Override
        public IBinder asBinder(){
            return this;
        }

        ICallback callback;

        public void registerCallback(ICallback callback) {
            this.callback = callback;
            remoteCallbackList.register(MyBinder.this);
        }

        public void unregisterCallback(ICallback callback) {
            remoteCallbackList.unregister(MyBinder.this);
        }

        public void registerActivity(Activity activity) {
            RunningTrackerService.this.registerActivity(activity);
        }

        public void start() {
            RunningTrackerService.this.start();
        }

        public void pause() {
            RunningTrackerService.this.pause();
        }

        public void setSessionNote(String sessionNote) {
            RunningTrackerService.this.setSessionNote(sessionNote);
        }

        public String getSessionNote() {
            return RunningTrackerService.this.getSessionNote();
        }

        public void endAndSave() {

            RunningTrackerService.this.setEndDateTime(LocalDateTime.now());

            // must call doSaveRunCallback first because end() will reset all values
            RunningTrackerService.this.doSaveRunCallback(runningTracker.getDistance(), runningTracker.getDuration(), runningTracker.getAvgPace(), runningTracker.getStartDateTime(), runningTracker.getEndDateTime(), runningTracker.getSessionNote());
            RunningTrackerService.this.end();
        }

        public void endAndDontSave() {
            RunningTrackerService.this.end();
        }

        public RunningTracker.RunningTrackerState getState() {
            return RunningTrackerService.this.getState();
        }
    }

    private void start() {
        runningTracker.start();
    }

    private void registerActivity(Activity activity) {
        runningTracker.registerActivity(activity);
    }

    public void end() {
        runningTracker.end();
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        runningTracker.setEndDateTime(endDateTime);
    }

    public void pause() {
        runningTracker.pause();
    }

    public void setSessionNote(String sessionNote) {
        runningTracker.setSessionNote(sessionNote);
        doSessionNoteCallback(runningTracker.getSessionNote());
    }

    public String getSessionNote() {
        return runningTracker.getSessionNote();
    }

    public RunningTracker.RunningTrackerState getState() {
        return runningTracker.getState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
}
