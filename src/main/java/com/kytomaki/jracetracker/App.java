package com.kytomaki.jracetracker;

import com.kytomaki.jracetracker.model.Heat;
import com.kytomaki.jracetracker.model.LapTime;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.InputStream;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Janne Kytömäki on 16.5.2016.
 */
public class App extends Application implements LapListener {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private SerialPort serialPort;
  private OpenLapSerialListener openLapSerialListener;


  private TableView<LapTime> lapTimeView;

  private Map<String, Heat> heats = new HashMap<>();

  private Heat currentHeat;

  public static void main(String[] args) throws PortInUseException, IOException, NoSuchPortException,
          UnsupportedCommOperationException {
    if (args != null && args.length > 1) {
      launch(args);
    } else {
      System.out.println("usage:\njava App [portname] [baud rate] \n" +
              "e.g. java -Djava.library.path=/usr/lib/jni -cp " +
              "jracetracker-1.0-SNAPSHOT-jar-with-dependencies.jar com.kytomaki.jracetracker.App " +
              "/dev/ttyUSB0 9600");
    }
  }

  public App()
          throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
  }

  @Override
  public void init() throws Exception {
    Parameters params = getParameters();
    String port = params.getUnnamed().get(0);
    int baudrate = Integer.valueOf(params.getUnnamed().get(1));

    CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
    CommPort commPort = portIdentifier.open(this.getClass().getName(), 0);

    if (commPort instanceof SerialPort) {
      serialPort = (SerialPort) commPort;
      serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
              SerialPort.PARITY_NONE);


      InputStream in = serialPort.getInputStream();

      openLapSerialListener = new OpenLapSerialListener();
      openLapSerialListener.setLapListener(this);
      new Thread(new SerialReader(in, openLapSerialListener)).start();

    }
  }

  @Override
  public void start(final Stage primaryStage) throws Exception {
    try {
      primaryStage.setTitle("Lap reader");

      Pane myPane = (Pane) FXMLLoader.load(getClass().getResource("/layout.fxml"));
      Scene myScene = new Scene(myPane, 1200, 800);
      primaryStage.setScene(myScene);
      primaryStage.show();

      lapTimeView = (TableView<LapTime>) myScene.lookup("#tableView");

      lapTimeView.setRowFactory(new Callback<TableView<LapTime>, TableRow<LapTime>>() {
        @Override
        public TableRow<LapTime> call(TableView<LapTime> param) {
          final TableRow<LapTime> row = new TableRow<>();
          final ContextMenu contextMenu = new ContextMenu();
          final MenuItem removeMenuItem = new MenuItem("Split");
          removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
              if (!currentHeat.isRunning()) {
                LapTime lt = row.getItem();

                LapTime split = new LapTime(lt.getCreated().minus((long) (lt.getLapTime() / 2 * 1000),
                        ChronoUnit.MILLIS),
                        lt.getPonder(),
                        lt.getLapTime() / 2,
                        lt.getTimeSinceBeginning() - lt.getLapTime() / 2);

                split.setLapNumber(lt.getLapNumber());
                lt.setLapTime(lt.getLapTime() / 2);

                lapTimeView.getItems()
                        .stream()
                        .filter(lapTime -> lapTime.getPonder() == lt.getPonder()
                                && lapTime.getLapNumber() > lt.getLapNumber())
                        .forEach(lapTime -> lapTime.setLapNumber(lapTime.getLapNumber() + 1));

                lt.setLapNumber(lt.getLapNumber() + 1);

                lapTimeView.getItems().add(split);

                lapTimeView.sort();
              }
            }
          });

          contextMenu.getItems().add(removeMenuItem);
          // Set context menu on row, but use a binding to make it only show for non-empty rows:
          row.contextMenuProperty().bind(
                  Bindings.when(row.emptyProperty())
                          .then((ContextMenu)null)
                          .otherwise(contextMenu)
          );
          return row ;
        }
      });

      Label runningIndicatorLabel = (Label) myScene.lookup("#runningIndicator");

      Button button = (Button) myScene.lookup("#resetButton");
      button.setOnAction(event -> {
        logger.debug("Resetting race");
        openLapSerialListener.reset();
      });

      Button finishButton = (Button) myScene.lookup("#finishButton");
      finishButton.setOnAction(event -> {
        logger.debug("Finishing race");
        currentHeat.setRunning(false);
        runningIndicatorLabel.setText("Finished");
      });

      Button copyButton = (Button) myScene.lookup("#copyButton");
      copyButton.setOnAction(event -> {
        logger.debug("Copying results to clipboard");
        StringBuffer sb = new StringBuffer();
        for (LapTime lt : lapTimeView.getItems()) {
          sb.append(lt.asCsv() + "\n");
        }
        StringSelection selection = new StringSelection(sb.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
      });

      ComboBox heatSelector = (ComboBox) myScene.lookup("#heatSelector");

      TextField heatNameInput = (TextField) myScene.lookup("#heatNameInput");

      heatNameInput.textProperty().addListener((observable, oldValue, newValue) -> {
        this.currentHeat.setName(newValue);

        // The dropdown doesn't get updated unless an ObservableList change event is fired,
        // so we must replace the current item with itself
        int indexOfCurrentHeat = heatSelector.getItems().indexOf(currentHeat);
        heatSelector.getItems().set(indexOfCurrentHeat, currentHeat);
      });


      heatSelector.setConverter(new StringConverter() {
        @Override
        public String toString(Object object) {
          if (object == null) {
            logger.debug("toString returns null for {}", object);
            return null;
          }
          Heat heat = (Heat) object;
          logger.debug("toString returns {} for {}", heat.getName(), object);
          return heat.getName();
        }

        @Override
        public Object fromString(String string) {
          if (currentHeat != null){
            currentHeat.setName(string);
          }
          return currentHeat;
        }
      });

      heatSelector.setOnAction(event -> {
        logger.debug("heatSelector value is {}", heatSelector.getValue());
        currentHeat.setRunning(false);
        currentHeat = (Heat) heatSelector.getValue();
        runningIndicatorLabel.setText("Finished");
        heatNameInput.setText(currentHeat.getName());
        lapTimeView.setItems(currentHeat.getLapTimes());
        TableColumn tc = lapTimeView.getColumns().get(0);
        lapTimeView.getSortOrder().add(tc);
      });


      Button newHeatButton = (Button) myScene.lookup("#newHeatButton");
      newHeatButton.setOnAction(event -> {
        Heat createdHeat = new Heat("New heat");
        this.heats.put(createdHeat.getName(), createdHeat);
        currentHeat = createdHeat;
        lapTimeView.setItems(createdHeat.getLapTimes());
        heatSelector.getItems().add(createdHeat);
        heatSelector.setValue(createdHeat);
        runningIndicatorLabel.setText("Running");
        currentHeat.setRunning(true);
        openLapSerialListener.reset();
      });

    } catch (Throwable e) {
      if (serialPort != null) {
        serialPort.close();
      }
      throw e;
    }
  }

  @Override
  public void onLap(final LapTime lapTime) {
    logger.debug("onLap: {}", lapTime);
    if (this.currentHeat != null) {
      logger.debug("Current heat is running: {}", this.currentHeat.isRunning());
    }
    if (this.currentHeat != null && this.currentHeat.isRunning()) {
      lapTime.setHeat(this.currentHeat);
      this.currentHeat.getLapTimes().add(lapTime);
    }

    playSound();
  }


  public static synchronized void playSound() {
    new Thread(new Runnable() {
      // The wrapper thread is unnecessary, unless it blocks on the
      // Clip finishing; see comments.
      public void run() {
        try {
          Clip clip = AudioSystem.getClip();
          AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                  App.class.getResourceAsStream("/beep-02.wav"));
          clip.open(inputStream);
          clip.start();
        } catch (Exception e) {
          System.err.println(e.getMessage());
        }
      }
    }).start();
  }

  @Override
  public void reset() {
    lapTimeView.getItems().clear();
  }


}
