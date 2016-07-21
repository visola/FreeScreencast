package com.visola.freescreencast;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.media.MediaLocator;

import com.visola.freescreencast.processing.ImagesToVideo;
import com.visola.freescreencast.record.ScreenRecorder;

public class Main {

  public static void main(String[] args) throws InterruptedException, IOException {
    ScreenRecorder recorder = new ScreenRecorder();
    recorder.start();
    Thread.sleep(10000);
    recorder.stop();

    MediaLocator ml = new MediaLocator("file:" + new File("tmp/out.mov").getCanonicalPath());

    ImagesToVideo iTV = new ImagesToVideo();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    iTV.imagesToVideo(screenSize.width, screenSize.height, recorder.getFrameRate(), new File("test.bin"), ml);
  }

}
