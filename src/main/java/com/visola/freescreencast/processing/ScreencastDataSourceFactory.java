package com.visola.freescreencast.processing;

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

  public ScreencastDataSource createScreencastDataSource(long startedAt) throws IOException {
    ScreenshotSourceStream screenshotSourceStream = screenshotSourceStreamFactory.createSourceStream(startedAt);
    return new ScreencastDataSource(screenshotSourceStream);
  }

}
