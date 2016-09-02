package com.visola.freescreencast.processing.screenshot;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScreenshotSourceStreamFactory {

  private final Set<ScreenshotProcessor> screenshotProcessors;

  @Autowired
  public ScreenshotSourceStreamFactory(Set<ScreenshotProcessor> screenshotProcessors) {
    this.screenshotProcessors = screenshotProcessors;
  }

  public ScreenshotSourceStream createSourceStream(int width, int height, float frameRate, File screenShotsFile) throws IOException {
    return new ScreenshotSourceStream(screenshotProcessors, width, height, frameRate, screenShotsFile);
  }

}
