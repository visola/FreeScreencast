package com.visola.freescreencast.processing;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Component;

import com.visola.freescreencast.processing.screenshot.ScreenshotSourceStream;
import com.visola.freescreencast.processing.screenshot.ScreenshotSourceStreamFactory;

@Component
public class ScreencastDataSourceFactory {

  private final ScreenshotSourceStreamFactory screenshotSourceStreamFactory;

  public ScreencastDataSourceFactory(ScreenshotSourceStreamFactory screenshotSourceStreamFactory) {
    this.screenshotSourceStreamFactory = screenshotSourceStreamFactory;
  }

  public ScreencastDataSource createScreencastDataSource(int width, int height, float frameRate, File screenShotsFile) throws IOException {
    ScreenshotSourceStream screenshotSourceStream = screenshotSourceStreamFactory.createSourceStream(width, height, frameRate, screenShotsFile);
    return new ScreencastDataSource(screenshotSourceStream);
  }

}
