package com.visola.freescreencast.processing.screenshot;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;

public class ScreenshotSourceStream implements PullBufferStream {

  private boolean finished = false;
  private final long frameDuration;
  private final DataInputStream dataIn;
  private final VideoFormat format;
  private final Collection<ScreenshotProcessor> screenshotProcessors;

  public ScreenshotSourceStream(Collection<ScreenshotProcessor> collection, File readFrom) throws IOException {
    dataIn = new DataInputStream(new FileInputStream(readFrom));
    long duration = dataIn.readLong();
    int frameCount = dataIn.readInt();
    int width = dataIn.readInt();
    int height = dataIn.readInt();

    this.frameDuration = Math.round( (double) duration / (double) frameCount);
    this.screenshotProcessors = collection;

    float frameRate = ((float) 1000 * frameCount) / (float) duration;
    format = new VideoFormat(VideoFormat.JPEG, new Dimension(width, height), Format.NOT_SPECIFIED, Format.byteArray, frameRate);
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
      int size = (int) dataIn.readLong();
      long time = dataIn.readLong();

      byte [] bytes = new byte[size];
      dataIn.read(bytes);

      ByteArrayInputStream imageInput = new ByteArrayInputStream(bytes);
      BufferedImage image = ImageIO.read(imageInput);

      for (ScreenshotProcessor processor : screenshotProcessors) {
        processor.processImage(time, image);
      }

      ByteArrayOutputStream processedImageOutputStream = new ByteArrayOutputStream();
      ImageIO.write(image, "jpg", processedImageOutputStream);
      byte [] processedImage = processedImageOutputStream.toByteArray();

      buffer.setDuration(frameDuration);
      buffer.setTimeStamp(time);
      buffer.setData(processedImage);
      buffer.setLength(processedImage.length);
      buffer.setOffset(0);
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
