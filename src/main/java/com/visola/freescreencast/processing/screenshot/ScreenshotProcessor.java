package com.visola.freescreencast.processing.screenshot;

import java.awt.image.BufferedImage;
import java.io.File;

public interface ScreenshotProcessor {

  void processImage(long time, BufferedImage image);

  void setInputDirectory(File inputDirectory);

}
