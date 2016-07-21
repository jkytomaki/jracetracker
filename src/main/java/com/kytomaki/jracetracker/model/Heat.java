package com.kytomaki.jracetracker.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Created by Janne Kytömäki on 14.6.2016.
 */
public class Heat {

  private String name;

  private ObservableList<LapTime> lapTimes;

  private boolean running = true;

  public Heat(String name) {
    this.name = name;
    this.lapTimes = FXCollections.observableArrayList();
    this.running = true;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ObservableList<LapTime> getLapTimes() {
    return lapTimes;
  }

  public void setLapTimes(ObservableList<LapTime> lapTimes) {
    this.lapTimes = lapTimes;
  }

  @Override
  public String toString() {
    return "Heat{" +
            "name='" + name + '\'' +
            '}';
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }
}
