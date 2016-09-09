package com.visola.freescreencast.processing.screenshot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.visola.freescreencast.Frame;

@Component
@Scope("prototype")
public class MouseImageScreenshotProcessor implements ScreenshotProcessor {

  private static final float RELEASE_ANIMATION_LENGTH = 250f;
  private static final float MAX_ALPHA = 0.7f;
  private static final Color CLICK_MARKER_COLOR = new Color(1f, 1f, 0f, MAX_ALPHA);
  private static final int CLICK_MARKER_SIZE = 30;
  private static final int HALF_CLICK_MARKER_SIZE = CLICK_MARKER_SIZE / 2;

  private final BufferedImage cursorImage;
  private long mouseDownAt = -1;
  private long mouseUpAt = -1;

  public MouseImageScreenshotProcessor() throws IOException {
    cursorImage = ImageIO.read(getClass().getResourceAsStream("/cursor.png"));
  }

  @Override
  public void processImage(Frame frame, BufferedImage image) {
    Graphics2D g2 = (Graphics2D) image.getGraphics();

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

}
