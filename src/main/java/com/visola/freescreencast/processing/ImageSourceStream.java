package com.visola.freescreencast.processing;

import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;

import com.visola.freescreencast.Frame;

public class ImageSourceStream implements PullBufferStream {

  private boolean finished = false;
  private final DataInputStream dataIn;
  private final VideoFormat format;

  public ImageSourceStream(int width, int height, float frameRate, File readFrom) throws FileNotFoundException {
    format = new VideoFormat(VideoFormat.JPEG, new Dimension(width, height), Format.NOT_SPECIFIED, Format.byteArray, frameRate);
    dataIn = new DataInputStream(new FileInputStream(readFrom));
  }

  @Override
  public boolean endOfStream() {
    return finished;
  }

  @Override
  public ContentDescriptor getContentDescriptor() {
    return new ContentDescriptor(ContentDescriptor.RAW);
  }

  @Override
  public long getContentLength() {
    return 0;
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
  public Format getFormat() {
    return format;
  }

  @Override
  public void read(Buffer buffer) throws IOException {
    try {
      int size = dataIn.readInt();
      byte [] bytes = new byte[size];
      dataIn.read(bytes);

      Frame frame = Frame.parseFrom(bytes);
      byte [] screenshot = frame.getScreenshot().toByteArray();

      buffer.setTimeStamp(frame.getTime());
      buffer.setData(screenshot);
      buffer.setOffset(0);
      buffer.setLength(screenshot.length);
      buffer.setFormat(format);
      buffer.setFlags(buffer.getFlags() | Buffer.FLAG_KEY_FRAME);

    } catch (EOFException eof) {
      buffer.setEOM(true);
      buffer.setOffset(0);
      buffer.setLength(0);

      finished = true;
      dataIn.close();
      return;
    }
    
  }

  @Override
  public boolean willReadBlock() {
    return true;
  }

}
