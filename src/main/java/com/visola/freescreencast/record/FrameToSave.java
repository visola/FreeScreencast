package com.visola.freescreencast.record;

import java.awt.image.BufferedImage;
import java.util.Set;

public class FrameToSave {

  private final BufferedImage screenshot;
  private final long time;
  private final int mouseX;
  private final int mouseY;
  private final Set<String> modifiers;

  public FrameToSave(BufferedImage screenshot, long time, int mouseX, int mouseY, Set<String> modifiers) {
    super();
    this.screenshot = screenshot;
    this.time = time;
    this.mouseX = mouseX;
    this.mouseY = mouseY;
    this.modifiers = modifiers;
  }

  public BufferedImage getScreenshot() {
    return screenshot;
  }

  public long getTime() {
    return time;
  }

  public int getMouseX() {
    return mouseX;
  }

  public int getMouseY() {
    return mouseY;
  }

  public boolean isMouseDown() {
    return modifiers != null;
  }

  public Set<String> getModifiers() {
    return modifiers;
  }

}
