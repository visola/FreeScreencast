package com.visola.freescreencast.record;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class RandomAccessFileWrapperOutputStream extends OutputStream {

  private final RandomAccessFile randomAccessFile;

  public RandomAccessFileWrapperOutputStream(RandomAccessFile randomAccessFile) {
    this.randomAccessFile = randomAccessFile;
  }

  @Override
  public void write(int b) throws IOException {
    randomAccessFile.writeByte(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    randomAccessFile.write(b, off, len);
  }

}
