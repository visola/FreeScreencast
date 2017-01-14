package com.visola.freescreencast.processing.screenshot;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ScreenshotSourceStreamFactory {

  private final ApplicationContext applicationContext;
  private final String baseDataDirectory;
  private final String inputDirectory;

  @Autowired
  public ScreenshotSourceStreamFactory(ApplicationContext applicationContext,
                                       @Value("${data.baseDir}") String baseDataDirectory,
                                       @Value("${data.inputDir}") String inputDirectory) {
    this.applicationContext = applicationContext;
    this.baseDataDirectory = baseDataDirectory;
    this.inputDirectory = inputDirectory;
  }

  public ScreenshotSourceStream createSourceStream(long startedAt) throws IOException {
    File inputDirectoryFile = new File(String.format("%s/%d/%s", baseDataDirectory, startedAt, inputDirectory));
    File inputFile = new File(inputDirectoryFile, "screenRecording.bin");

    Collection<ScreenshotProcessor> screenshotProcessors = applicationContext.getBeansOfType(ScreenshotProcessor.class).values();
    screenshotProcessors.forEach(sp -> sp.setInputDirectory(inputDirectoryFile));
    return new ScreenshotSourceStream(screenshotProcessors, inputFile);
  }

}
