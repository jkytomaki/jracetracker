package com.kytomaki.jracetracker;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.kytomaki.jracetracker.model.LapTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Janne Kytömäki on 18.5.2016.
 */
public class OpenLapSerialListener implements SerialEventListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, List<LapTime>> laps = new HashMap<>();

    private LapListener lapListener;

    public OpenLapSerialListener() {

    }

    public synchronized void reset(){
        laps = new HashMap<>();
        if (lapListener != null){
            lapListener.reset();
        }
    }

    public void onReadEvent(String line) {
        parseLine(line);
    }


    private synchronized void parseLine(String line) {
        logger.debug("Read line '{}'", line);
        List<String> parts = Splitter.on(CharMatcher.BREAKING_WHITESPACE).splitToList(line);
        if (parts == null || parts.size() != 4) {
            logger.error("Error parsing line: " + line);
        }
        int ponder = Integer.parseInt(parts.get(2));
        double timeSinceStart = Double.parseDouble(parts.get(3));

        final LapTime lapTime = new LapTime();
        lapTime.setPonder(ponder);
        List<LapTime> lapTimes = laps.get(ponder);
        if (lapTimes == null) {
            lapTimes = new ArrayList<>();
            laps.put(ponder, lapTimes);
            lapTime.setTimeSinceBeginning(timeSinceStart);
            lapTime.setLapTime(0);
        } else {
            LapTime previousLaptime = lapTimes.get(lapTimes.size() - 1);
            lapTime.setTimeSinceBeginning(timeSinceStart);
            lapTime.setLapTime(timeSinceStart - previousLaptime.getTimeSinceBeginning());
        }
        lapTimes.add(lapTime);

        lapTime.setLapNumber(lapTimes.size() - 1);

        if (lapListener != null) {
            lapListener.onLap(lapTime);
        }
        logger.info("ponder: " + ponder + " lap: " + lapTimes.size() + " laptime: " +
                           lapTime.getLapTime() + " time since first read: " + lapTime.getTimeSinceBeginning());
//    System.out.println(ponder + ": " + lapTime);

    /*for (String part : ){
      System.out.print(part + ";");
    }
    System.out.println();*/
    }

    public void setLapListener(final LapListener lapListener) {
        this.lapListener = lapListener;
    }
}
