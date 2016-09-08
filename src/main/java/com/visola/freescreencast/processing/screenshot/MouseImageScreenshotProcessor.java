package com.visola.freescreencast.processing.screenshot;

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

  private final BufferedImage cursorImage;

  public MouseImageScreenshotProcessor() throws IOException {
    cursorImage = ImageIO.read(getClass().getResourceAsStream("/cursor.png"));
  }

  @Override
  public void processImage(Frame frame, BufferedImage image) {
    Graphics2D g2 = (Graphics2D) image.getGraphics();
    g2.drawImage(cursorImage, frame.getMouseX(), frame.getMouseY(), 20, 30, null);
  }

}
