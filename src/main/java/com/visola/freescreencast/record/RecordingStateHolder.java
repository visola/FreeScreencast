package com.visola.freescreencast.record;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.visola.freescreencast.record.event.StartRecordingEvent;
import com.visola.freescreencast.record.event.StopRecordingEvent;

@Component
public class RecordingStateHolder {

  private final ApplicationEventPublisher applicationEventPublisher;

  private final String baseDataDirectory;
  private final String outputDirectoryPath;
  private final String inputDirectoryPath;

  private File inputDirectory;
  private File outputDirectory;

  private Long startedAt = null;
  private Long endedAt = null;

  @Autowired
  public RecordingStateHolder(ApplicationEventPublisher applicationEventPublisher,
                              @Value("${data.baseDir}") String baseDataDirectory,
                              @Value("${data.inputDir}") String inputDirectoryPath,
                              @Value("${data.outputDir}") String outputDirectoryPath) {
    this.applicationEventPublisher = applicationEventPublisher;
    this.baseDataDirectory = baseDataDirectory;
    this.inputDirectoryPath = inputDirectoryPath;
    this.outputDirectoryPath = outputDirectoryPath;
  }

  public File getInputDirectory() {
    return inputDirectory;
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public boolean isRecording() {
    return startedAt != null && endedAt == null;
  }

  public void startRecording(Object source) {
    startedAt = System.currentTimeMillis();
    endedAt = null;
    prepareInputAndOutputDirectories();
    applicationEventPublisher.publishEvent(new StartRecordingEvent(source, startedAt, inputDirectory, outputDirectory));
  }

  public void stopRecording(Object source) {
    endedAt = System.currentTimeMillis();
    applicationEventPublisher.publishEvent(new StopRecordingEvent(source));
  }

  private void prepareInputAndOutputDirectories() {
    this.inputDirectory = new File(baseDataDirectory + "/" + startedAt + "/" + inputDirectoryPath + "/");
    if (!inputDirectory.exists()) {
      inputDirectory.mkdirs();
    }

    this.outputDirectory = new File(baseDataDirectory + "/" + startedAt + "/" + outputDirectoryPath + "/");
    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }
  }

}
