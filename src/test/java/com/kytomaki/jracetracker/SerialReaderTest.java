package com.kytomaki.jracetracker;

import org.junit.Test;

import java.io.InputStream;

/**
 * Created by Janne Kytömäki on 17.5.2016.
 */
public class SerialReaderTest {

  @Test
  public void doTest(){

    InputStream in = getClass().getResourceAsStream("/hextest.dat");
    OpenLapSerialListener openLapSerialListener = new OpenLapSerialListener();
    SerialReader sr = new SerialReader(in, openLapSerialListener);
    sr.run();

  }

}