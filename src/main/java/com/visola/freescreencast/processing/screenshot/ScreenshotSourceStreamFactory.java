package com.visola.freescreencast.processing.screenshot;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ScreenshotSourceStreamFactory {

  private final ApplicationContext applicationContext;

  @Autowired
  public ScreenshotSourceStreamFactory(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public ScreenshotSourceStream createSourceStream(int width, int height, float frameRate, File screenShotsFile) throws IOException {
    return new ScreenshotSourceStream(applicationContext.getBeansOfType(ScreenshotProcessor.class).values(), width, height, frameRate, screenShotsFile);
  }

}
