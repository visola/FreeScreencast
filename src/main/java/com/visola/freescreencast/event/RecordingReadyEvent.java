package com.visola.freescreencast.event;

import org.springframework.context.ApplicationEvent;

public class RecordingReadyEvent extends ApplicationEvent {

  private static final long serialVersionUID = 1L;

  private final float frameRate;
  private final String inputFile;

  public RecordingReadyEvent(Object source, String inputFile, Float frameRate) {
    super(source);
    this.frameRate = frameRate;
    this.inputFile = inputFile;
  }

  public float getFrameRate() {
    return frameRate;
  }

  public String getInputFile() {
    return inputFile;
  }

}
