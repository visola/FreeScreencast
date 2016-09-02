package com.visola.freescreencast.processing;

import java.io.File;
import java.io.IOException;

import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

public class OutputDataSource extends PullBufferDataSource {

  private final PullBufferStream[] streams;

  public OutputDataSource(int width, int height, float frameRate, File screenShotsFile) throws IOException {
    streams = new PullBufferStream[1];
    streams[0] = new ImageSourceStream(width, height, frameRate, screenShotsFile);
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
