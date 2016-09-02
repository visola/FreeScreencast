package com.visola.freescreencast.processing.screenshot;

import java.awt.image.BufferedImage;

import com.visola.freescreencast.Frame;

public interface ScreenshotProcessor {

  void processImage(Frame frame, BufferedImage image);

}
