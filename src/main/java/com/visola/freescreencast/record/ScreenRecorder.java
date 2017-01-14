package com.visola.freescreencast.record;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.visola.freescreencast.record.event.RecordingReadyEvent;
import com.visola.freescreencast.record.event.StartRecordingEvent;
import com.visola.freescreencast.record.event.StopRecordingEvent;

@Component
public class ScreenRecorder {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScreenRecorder.class);

  private final ApplicationEventPublisher eventPublisher;

  private String inputFile;
  private Long startedAt = null;
  private Long finishedAt = null;
  private int frameCount = 0;
  private Long lastScreenshot = null;

  private BlockingQueue<FrameToSave> framesToSave = new LinkedBlockingQueue<>();
  private Thread savingThread;
  private Thread screenshotThread;

  private final Rectangle recordingSize;

  @Autowired
  public ScreenRecorder(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    recordingSize = new Rectangle(screenSize);
  }

  @EventListener
  public void start(StartRecordingEvent startRecordingEvent) {
    startedAt = startRecordingEvent.getStartedAt();

    inputFile = startRecordingEvent.getInputDirectory().getPath() + "/screenRecording.bin";

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
    try (RandomAccessFile randomAccessFile = new RandomAccessFile(inputFile, "rw")) {
      randomAccessFile.writeLong(0); // Placeholder for duration
      randomAccessFile.writeInt(0); // Placeholder for frame count
      randomAccessFile.writeInt(recordingSize.width);
      randomAccessFile.writeInt(recordingSize.height);

      while (finishedAt == null) {
        FrameToSave toSave = framesToSave.poll();
        if (framesToSave.size() >= 10) {
          // Drop frame if queue is too large
          continue;
        }
        if (toSave != null) {
          saveFrame(randomAccessFile, toSave);
        }
      }

      // Flush the queue if new frames were added
      for (FrameToSave toSave : framesToSave) {
        saveFrame(randomAccessFile, toSave);
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

          framesToSave.put(new FrameToSave(image, System.currentTimeMillis() - startedAt));
        } catch (InterruptedException e) {
          LOGGER.warn("Screenshot thread interrupted.", e);
        }
      }
    } catch (FFmpegFrameGrabber.Exception fge) {
      LOGGER.error("FFmpeg error.", fge);
    }
  }

  private void saveFrame(RandomAccessFile randomAccessFile, FrameToSave toSave) throws IOException {
    long startImageWrite = System.currentTimeMillis();

    long positionBefore = randomAccessFile.getFilePointer();
    randomAccessFile.writeLong(0L); // place holder for image size
    randomAccessFile.writeLong(toSave.getTime());

    ImageIO.write(toSave.getScreenshot(), "jpg", new RandomAccessFileWrapperOutputStream(randomAccessFile));
    long positionAfter = randomAccessFile.getFilePointer();

    // Go back and write image size
    long imageSize = positionAfter - positionBefore - 8 - 8;
    randomAccessFile.seek(positionBefore);
    randomAccessFile.writeLong(imageSize);
    randomAccessFile.seek(positionAfter);

    LOGGER.trace("Image writing time: {}ms", System.currentTimeMillis() - startImageWrite);

    frameCount++;
    long now = System.currentTimeMillis();
    if (lastScreenshot != null) {
      LOGGER.trace("Since last frame: {} ms, queue size: {}", now - lastScreenshot, framesToSave.size());
    }
    lastScreenshot = System.currentTimeMillis();
  }

}
