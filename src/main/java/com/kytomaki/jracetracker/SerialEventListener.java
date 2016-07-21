package com.kytomaki.jracetracker;

/**
 * Created by Janne Kytömäki on 18.5.2016.
 */
public interface SerialEventListener {

  void onReadEvent(String line);

}
