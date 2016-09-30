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
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;
import com.visola.freescreencast.Frame;
import com.visola.freescreencast.event.AbstractMouseEvent;
import com.visola.freescreencast.event.InputModifier;
import com.visola.freescreencast.event.MouseMovedEvent;
import com.visola.freescreencast.event.MousePressedEvent;
import com.visola.freescreencast.event.MouseReleasedEvent;
import com.visola.freescreencast.record.event.RecordingReadyEvent;
import com.visola.freescreencast.record.event.StartRecordingEvent;
import com.visola.freescreencast.record.event.StopRecordingEvent;

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

  private int mouseX = 0;
  private int mouseY = 0;
  private Set<InputModifier> mouseModifiers = null;

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

  @EventListener
  public void mouseMoved(MouseMovedEvent e) {
    setMousePosition(e);
  }

  @EventListener
  public void mousePressed(MousePressedEvent e) {
    setMousePosition(e);
    mouseModifiers = e.getModifiers();
  }

  @EventListener
  public void mouseReleased(MouseReleasedEvent e) {
    setMousePosition(e);
    mouseModifiers = null;
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

        Frame.Builder frameBuilder = Frame.newBuilder()
          .setTime(System.currentTimeMillis() - start)
          .setMouseX(mouseX)
          .setMouseY(mouseY)
          .setScreenshot(ByteString.copyFrom(bytesOut.toByteArray()));

        if (mouseModifiers != null) {
          frameBuilder.setMouseDown(true)
            .addAllModifiers(mouseModifiers.stream().map(InputModifier::name).collect(Collectors.toSet()));
        }

        byte [] frameBytes = frameBuilder.build().toByteArray();
        dataOut.writeInt(frameBytes.length);
        dataOut.write(frameBytes);

        frameCount++;
        long now = System.currentTimeMillis();
        if (lastScreenshot > -1) {
          LOGGER.debug("Since last frame: {} ms", now - lastScreenshot);
        }
        lastScreenshot = System.currentTimeMillis();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    long end = System.currentTimeMillis();
    totalTime = end - start;
    LOGGER.info("Total frames: {}, Total time: {} ms, Frames per second: {}",
        frameCount,
        end - start,
        ( (double) frameCount ) * 1000 / ( (double) (end - start) )
    );
  }

  private void setMousePosition(AbstractMouseEvent e) {
    mouseX = e.getX();
    mouseY = e.getY();
  }

}
