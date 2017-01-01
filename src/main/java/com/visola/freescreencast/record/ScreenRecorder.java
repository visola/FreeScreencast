package com.visola.freescreencast.record;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class ScreenRecorder {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScreenRecorder.class);

  private final ApplicationEventPublisher eventPublisher;
  private final String baseDataDirectory;
  private final String inputDirectory;

  private String inputFile;
  private Long startedAt = null;
  private Long finishedAt = null;
  private int frameCount = 0;
  private Long lastScreenshot = null;

  private BlockingQueue<FrameToSave> framesToSave = new LinkedBlockingQueue<>();
  private Thread savingThread;
  private Thread screenshotThread;

  private int mouseX = 0;
  private int mouseY = 0;
  private Set<InputModifier> mouseModifiers = null;

  private final Rectangle recordingSize;

  @Autowired
  public ScreenRecorder(ApplicationEventPublisher eventPublisher,
                        @Value("${data.baseDir}") String baseDataDirectory,
                        @Value("${data.inputDir}") String inputDirectory) {
    this.eventPublisher = eventPublisher;
    this.baseDataDirectory = baseDataDirectory;
    this.inputDirectory = inputDirectory;

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    recordingSize = new Rectangle(screenSize);
  }

  @EventListener
  public void start(StartRecordingEvent startRecordingEvent) {
    startedAt = startRecordingEvent.getStarted();

    String fullInputPath = baseDataDirectory + "/" + startRecordingEvent.getStarted() + "/" + inputDirectory + "/";
    inputFile = fullInputPath + "screenRecording.bin";

    // Make sure parent directories exist
    new File(fullInputPath).mkdirs();

    screenshotThread = new Thread(this::takeScreenshot, "Screen Shooter");
    screenshotThread.start();

    savingThread = new Thread(this::saveImages, "Image Saver");
    savingThread.start();
  }

  @EventListener(StopRecordingEvent.class)
  public void stop() throws InterruptedException {
    finishedAt = System.currentTimeMillis();
    if (screenshotThread != null) {
      screenshotThread.join();
      screenshotThread = null;

      savingThread.join();
      savingThread = null;

      eventPublisher.publishEvent(new RecordingReadyEvent(this, startedAt));
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

  private void saveEndOfStream() {
    try (RandomAccessFile randomAccessFile = new RandomAccessFile(inputFile, "rw")) {
      randomAccessFile.writeLong(finishedAt- startedAt);
      randomAccessFile.writeInt(frameCount);
    } catch (IOException ioe) {
      LOGGER.error("Error while recording screen capture metadata.", ioe);
    }

    LOGGER.info("Total frames: {}, Total time: {} ms, Frames per second: {}",
        frameCount,
        finishedAt - startedAt,
        ( (double) frameCount ) * 1000 / ( (double) (finishedAt - startedAt) )
    );
  }

  public void saveImages() {
    try (DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(inputFile))) {
      dataOut.writeLong(0); // Placeholder for duration
      dataOut.writeInt(0); // Placeholder for frame count
      dataOut.writeInt(recordingSize.width);
      dataOut.writeInt(recordingSize.height);

      while (finishedAt == null) {
        FrameToSave toSave = framesToSave.poll();
        if (framesToSave.size() >= 10) {
          // Drop frame if queue is too large
          continue;
        }
        if (toSave != null) {
          saveFrame(dataOut, toSave);
        }
      }

      // Flush the queue if new frames were added
      for (FrameToSave toSave : framesToSave) {
        saveFrame(dataOut, toSave);
      }

      saveEndOfStream();
    } catch (IOException ioe) {
      LOGGER.error("Error while capturing screens.", ioe);
    }
  }

  public void takeScreenshot() {
    Java2DFrameConverter converter = new Java2DFrameConverter();

    FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber("1");
    frameGrabber.setFormat("avfoundation");
    frameGrabber.setFrameRate(30);
    frameGrabber.setImageWidth(recordingSize.width);
    frameGrabber.setImageHeight(recordingSize.height);

    try {
      frameGrabber.start();
  
      while (finishedAt == null) {
        try {
          long startScreenshot = System.currentTimeMillis();
          org.bytedeco.javacv.Frame frame = frameGrabber.grab();
          LOGGER.trace("Screen grabbed in {}ms", System.currentTimeMillis() - startScreenshot);

          long convertFrameToImage = System.currentTimeMillis();
          BufferedImage image = converter.convert(frame);
          LOGGER.trace("Converted frame to image in {}ms", System.currentTimeMillis() - convertFrameToImage);

          framesToSave.put(new FrameToSave(image,
              System.currentTimeMillis() - startedAt,
              mouseX,
              mouseY,
              mouseModifiers == null ? null : mouseModifiers.stream().map(InputModifier::name).collect(Collectors.toSet())));
        } catch (InterruptedException e) {
          LOGGER.warn("Screenshot thread interrupted.", e);
        }
      }
    } catch (FFmpegFrameGrabber.Exception fge) {
      LOGGER.error("FFmpeg error.", fge);
    }
  }

  private void saveFrame(DataOutputStream dataOut, FrameToSave toSave) throws IOException {
    long startImageWrite = System.currentTimeMillis();
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    ImageIO.write(toSave.getScreenshot(), "bmp", bytesOut);
    LOGGER.trace("Image writing time: {}ms", System.currentTimeMillis() - startImageWrite);

    long startFrameWrite = System.currentTimeMillis();
    Frame.Builder frameBuilder = Frame.newBuilder()
      .setTime(toSave.getTime())
      .setMouseX(toSave.getMouseX())
      .setMouseY(toSave.getMouseY())
      .setScreenshot(ByteString.copyFrom(bytesOut.toByteArray()));

    if (toSave.isMouseDown()) {
      frameBuilder.setMouseDown(true)
        .addAllModifiers(toSave.getModifiers());
    }

    byte [] frameBytes = frameBuilder.build().toByteArray();
    dataOut.writeInt(frameBytes.length);
    dataOut.write(frameBytes);
    LOGGER.trace("Frame writing time: {}ms", System.currentTimeMillis() - startFrameWrite);

    frameCount++;
    long now = System.currentTimeMillis();
    if (lastScreenshot != null) {
      LOGGER.trace("Since last frame: {} ms, queue size: {}", now - lastScreenshot, framesToSave.size());
    }
    lastScreenshot = System.currentTimeMillis();
  }

  private void setMousePosition(AbstractMouseEvent e) {
    mouseX = e.getX();
    mouseY = e.getY();
  }

}
