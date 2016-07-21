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

public class ScreenRecorder implements Runnable {

  private int frameCount = 0;
  private long totalTime;
  private long lastScreenshot = -1;
  private boolean running = false;
  private Thread runningThread;

  public float getFrameRate() {
    return ( (float) frameCount ) * 1000 / ( (float) (totalTime) );
  }

  public void start() {
    running = true;

    runningThread = new Thread(this, "Screen Recorder");
    runningThread.start();
  }

  public void stop() throws InterruptedException {
    running = false;
    if (runningThread != null) {
      runningThread.join();
      runningThread = null;
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
    try (DataOutputStream dataOut = new DataOutputStream(new FileOutputStream("test.bin"))) {
      while (running) {
        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(screenSize));
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ImageIO.write(screenShot, "jpg", bytesOut);

        dataOut.writeInt(bytesOut.size());
        dataOut.writeLong(System.currentTimeMillis() - start);
        dataOut.write(bytesOut.toByteArray());

        frameCount++;
        long now = System.currentTimeMillis();
        if (lastScreenshot > -1) {
          System.err.printf("Since last frame: %d ms%n", lastScreenshot - now);
        }
        lastScreenshot = System.currentTimeMillis();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    long end = System.currentTimeMillis();
    totalTime = end - start;
    System.err.printf("Total frames: %d, Total time: %d ms, Frames per second: %e%n",
        frameCount,
        end - start,
        ( (double) frameCount ) * 1000 / ( (double) (end - start) )
    );
  }

}
