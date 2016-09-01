package com.visola.freescreencast.record;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;
import com.visola.freescreencast.Frame;
import com.visola.freescreencast.event.RecordingReadyEvent;
import com.visola.freescreencast.event.StartRecordingEvent;
import com.visola.freescreencast.event.StopRecordingEvent;

@Component
public class ScreenRecorder implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScreenRecorder.class);

  private final ApplicationEventPublisher eventPublisher;

  private String inputFile;
  private int frameCount = 0;
  private long totalTime;
  private long lastScreenshot = -1;
  private boolean running = false;
  private Thread runningThread;

  @Autowired
  public ScreenRecorder(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  public float getFrameRate() {
    return ( (float) frameCount ) * 1000 / ( (float) (totalTime) );
  }

  @EventListener(StartRecordingEvent.class)
  public void start() {
    running = true;

    runningThread = new Thread(this, "Screen Recorder");
    runningThread.start();
  }

  @EventListener(StopRecordingEvent.class)
  public void stop() throws InterruptedException {
    running = false;
    if (runningThread != null) {
      runningThread.join();
      runningThread = null;
      eventPublisher.publishEvent(new RecordingReadyEvent(this, inputFile, getFrameRate()));
    }
  }

  @Override
  public void run() {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Robot robot;
    try {
      robot = new Robot();
    } catch (AWTException e) {
      e.printStackTrace();
      return;
    }

    long start = System.currentTimeMillis();
    inputFile = "tmp/" + start + ".bin";
    try (DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(inputFile))) {
      while (running) {
        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(screenSize));
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ImageIO.write(screenShot, "jpg", bytesOut);

        Frame frame = Frame.newBuilder()
          .setTime(System.currentTimeMillis() - start)
          .setScreenshot(ByteString.copyFrom(bytesOut.toByteArray()))
          .build();

        byte [] frameBytes = frame.toByteArray();
        dataOut.writeInt(frameBytes.length);
        dataOut.write(frameBytes);

        frameCount++;
        long now = System.currentTimeMillis();
        if (lastScreenshot > -1) {
          LOGGER.info("Since last frame: {} ms", now - lastScreenshot);
        }
        lastScreenshot = System.currentTimeMillis();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    long end = System.currentTimeMillis();
    totalTime = end - start;
    LOGGER.debug("Total frames: {}, Total time: {} ms, Frames per second: {}",
        frameCount,
        end - start,
        ( (double) frameCount ) * 1000 / ( (double) (end - start) )
    );
  }

}
