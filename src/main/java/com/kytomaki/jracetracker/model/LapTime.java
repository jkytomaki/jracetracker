package com.kytomaki.jracetracker.model;

import java.time.LocalTime;

/**
 * Created by Janne Kytömäki on 18.5.2016.
 */
public class LapTime {

    final private LocalTime created;

    private int ponder;

    private double lapTime;

    private double timeSinceBeginning;

    private int lapNumber;

    private Heat heat;

    public LapTime() {
      created = LocalTime.now();
    }

    public LapTime(int ponder, double lapTime, double timeSinceBeginning) {
        created = LocalTime.now();
        this.ponder = ponder;
        this.lapTime = lapTime;
        this.timeSinceBeginning = timeSinceBeginning;
    }

    public LapTime(LocalTime created, int ponder, double lapTime, double timeSinceBeginning) {
      this.created = created;
      this.ponder = ponder;
      this.lapTime = lapTime;
      this.timeSinceBeginning = timeSinceBeginning;
    }

    public double getLapTime() {
        return lapTime;
    }

    public void setLapTime(double lapTime) {
        this.lapTime = lapTime;
    }

    public double getTimeSinceBeginning() {
        return timeSinceBeginning;
    }

    public void setTimeSinceBeginning(double timeSinceBeginning) {
        this.timeSinceBeginning = timeSinceBeginning;
    }

    public void setPonder(final int ponder) {
        this.ponder = ponder;
    }

    public int getPonder() {
        return ponder;
    }

    public LocalTime getCreated() {
        return created;
    }

    public int getLapNumber() {
        return lapNumber;
    }

    public void setLapNumber(int lapNumber) {
        this.lapNumber = lapNumber;
    }

    public String asCsv(){
        return created+"\t"+ponder+"\t"+lapNumber+"\t"+lapTime;
    }

    @Override
    public String toString() {
        return "LapTime{" +
                "created=" + created +
                ", ponder=" + ponder +
                ", lapTime=" + lapTime +
                ", timeSinceBeginning=" + timeSinceBeginning +
                ", lapNumber=" + lapNumber +
                '}';
    }

  public Heat getHeat() {
    return heat;
  }

  public void setHeat(Heat heat) {
    this.heat = heat;
  }
}
