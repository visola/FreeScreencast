package com.visola.freescreencast.record.event;

import java.io.File;

import org.springframework.context.ApplicationEvent;

public class StartRecordingEvent extends ApplicationEvent {

  private static final long serialVersionUID = 1L;
  private final Long startedAt;
  private final File inputDirectory;
  private final File outputDirectory;

  public StartRecordingEvent(Object source, Long startedAt, File inputDirectory, File outputDirectory) {
    super(source);
    this.inputDirectory = inputDirectory;
    this.outputDirectory = outputDirectory;
    this.startedAt = startedAt;
  }

  public File getInputDirectory() {
    return inputDirectory;
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public Long getStartedAt() {
    return startedAt;
  }

}
