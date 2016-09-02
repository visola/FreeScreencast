package com.visola.freescreencast.processing;

import java.io.IOException;

import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

import com.visola.freescreencast.processing.screenshot.ScreenshotSourceStream;

public class ScreencastDataSource extends PullBufferDataSource {

  private final PullBufferStream[] streams;

  public ScreencastDataSource(ScreenshotSourceStream screenshotSourceStream) throws IOException {
    streams = new PullBufferStream [] {screenshotSourceStream};
  }

  @Override
  public PullBufferStream[] getStreams() {
    return streams;
  }

  @Override
  public void connect() throws IOException {
  }

  @Override
  public void disconnect() {
  }

  @Override
  public String getContentType() {
    return ContentDescriptor.RAW;
  }

  @Override
  public Object getControl(String type) {
    return null;
  }

  @Override
  public Object[] getControls() {
    return new Object[0];
  }

  @Override
  public Time getDuration() {
    return DURATION_UNKNOWN;
  }

  @Override
  public void start() throws IOException {
  }

  @Override
  public void stop() throws IOException {
  }

}
