package com.kytomaki.jracetracker;

import com.kytomaki.jracetracker.model.LapTime;

/**
 * Created by Janne Kytömäki on 20.5.2016.
 */
public interface LapListener {

    void onLap(LapTime lapTime);

    void reset();
}
