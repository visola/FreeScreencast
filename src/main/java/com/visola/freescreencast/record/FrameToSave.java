package com.visola.freescreencast.record;

import java.awt.image.BufferedImage;

public class FrameToSave {

  private final BufferedImage screenshot;
  private final long time;

  public FrameToSave(BufferedImage screenshot, long time) {
    super();
    this.screenshot = screenshot;
    this.time = time;
  }

  public BufferedImage getScreenshot() {
    return screenshot;
  }

  public long getTime() {
    return time;
  }

}
