package com.visola.freescreencast.processing.screenshot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.visola.freescreencast.MouseFrame;

@Component
@Scope("prototype")
public class MouseImageScreenshotProcessor implements ScreenshotProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MouseImageScreenshotProcessor.class);
  private static final float RELEASE_ANIMATION_LENGTH = 250f;
  private static final float MAX_ALPHA = 0.7f;
  private static final Color CLICK_MARKER_COLOR = new Color(1f, 1f, 0f, MAX_ALPHA);
  private static final int CLICK_MARKER_SIZE = 30;
  private static final int HALF_CLICK_MARKER_SIZE = CLICK_MARKER_SIZE / 2;

  private RandomAccessFile inputFile;
  private final BufferedImage cursorImage;
  private long mouseDownAt = -1;
  private long mouseUpAt = -1;
  private MouseFrame currentMouseFrame;

  public MouseImageScreenshotProcessor() throws IOException {
    cursorImage = ImageIO.read(getClass().getResourceAsStream("/cursor.png"));
  }

  @Override
  public void processImage(long time, BufferedImage image) {
    Graphics2D g2 = (Graphics2D) image.getGraphics();

    MouseFrame frame = getCurrentFrame(time);
    LOGGER.trace("Buffer time: {}, mouse frame time: {}", time, frame.getTime());

    if (frame.getMouseDown()) {
      mouseDownAt = System.currentTimeMillis();
      mouseUpAt = -1;
    } else {
      if (mouseDownAt != -1) {
        mouseUpAt = System.currentTimeMillis();
      }
      mouseDownAt = -1;
    }

    if (mouseDownAt != -1 && mouseUpAt == -1) {
      g2.setColor(CLICK_MARKER_COLOR);
      g2.fillOval(frame.getMouseX() - HALF_CLICK_MARKER_SIZE, frame.getMouseY() - HALF_CLICK_MARKER_SIZE, CLICK_MARKER_SIZE, CLICK_MARKER_SIZE);
    }

    if (mouseUpAt != -1) {
      float t = (RELEASE_ANIMATION_LENGTH - (System.currentTimeMillis() - mouseUpAt)) / RELEASE_ANIMATION_LENGTH;
      if (t <= 0) {
        t = 0;
        mouseUpAt = -1;
      }

      g2.setColor(new Color(1f, 1f, 0f, t * MAX_ALPHA));
      g2.fillOval(frame.getMouseX() - HALF_CLICK_MARKER_SIZE, frame.getMouseY() - HALF_CLICK_MARKER_SIZE, CLICK_MARKER_SIZE, CLICK_MARKER_SIZE);
    }

    g2.drawImage(cursorImage, frame.getMouseX(), frame.getMouseY(), 20, 30, null);
  }

  @Override
  public void setInputDirectory(File inputFileDirectory) {
    File inputFile = new File(inputFileDirectory, "mouseEvents.bin");
    try {
      this.inputFile = new RandomAccessFile(inputFile, "r");
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Found with mouse events not found. Expected to be a file at: " + inputFile.getAbsolutePath());
    }
  }

  private MouseFrame getCurrentFrame(long time) {
    try {
      if (currentMouseFrame == null) {
        currentMouseFrame = readNextFrame();
      }
      while (time > currentMouseFrame.getTime()) {
        MouseFrame nextFrame = readNextFrame();
        if (nextFrame == null) {
          break;
        } else {
          currentMouseFrame = nextFrame;
        }
      }
    } catch (IOException ioe) {
      throw new RuntimeException("Error while reading next frame.", ioe);
    }

    return currentMouseFrame;
  }

  private MouseFrame readNextFrame() throws IOException {
    if (inputFile.getFilePointer() == inputFile.length()) {
      return null;
    }

    int frameSize = inputFile.readInt();
    byte [] bytes = new byte[frameSize];
    inputFile.read(bytes);
    return MouseFrame.parseFrom(bytes);
  }

}
