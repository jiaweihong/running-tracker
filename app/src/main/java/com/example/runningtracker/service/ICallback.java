package com.example.runningtracker.service;

import java.time.LocalDateTime;

public interface ICallback {
    public void updateDurationEvent(int duration);
    public void updateDistanceEvent(double distance);
    public void updateAvgPaceEvent(double avgPace);
    public void saveRunEvent(double distance, int duration, double avgPace, LocalDateTime startDateTime, LocalDateTime endDateTime, String sessionNote);
    public void updateSessionNoteEvent(String sessionNote);
}
