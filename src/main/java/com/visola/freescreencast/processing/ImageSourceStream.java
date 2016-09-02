package com.visola.freescreencast.processing;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
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
  private final BufferedImage cursorImage;

  public ImageSourceStream(int width, int height, float frameRate, File readFrom) throws IOException {
    format = new VideoFormat(VideoFormat.JPEG, new Dimension(width, height), Format.NOT_SPECIFIED, Format.byteArray, frameRate);
    dataIn = new DataInputStream(new FileInputStream(readFrom));

    cursorImage = ImageIO.read(getClass().getResourceAsStream("/cursor.png"));
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
      byte [] originalScreenshot = frame.getScreenshot().toByteArray();
      ByteArrayInputStream imageInput = new ByteArrayInputStream(originalScreenshot);

      BufferedImage image = ImageIO.read(imageInput);
      Graphics2D g2 = (Graphics2D) image.getGraphics();
      g2.drawImage(cursorImage, frame.getMouseX(), frame.getMouseY(), 20, 30, null);

      ByteArrayOutputStream processedImageOutputStream = new ByteArrayOutputStream();
      ImageIO.write(image, "jpg", processedImageOutputStream);
      byte [] processedImage = processedImageOutputStream.toByteArray();

      buffer.setTimeStamp(frame.getTime());
      buffer.setData(processedImage);
      buffer.setOffset(0);
      buffer.setLength(processedImage.length);
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
