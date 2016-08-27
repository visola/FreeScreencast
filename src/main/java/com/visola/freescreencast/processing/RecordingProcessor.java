package com.visola.freescreencast.processing;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.media.MediaLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.visola.freescreencast.event.RecordingReadyEvent;

@Component
public class RecordingProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecordingProcessor.class);

  @EventListener
  public void processRecording(RecordingReadyEvent recordingReadyEvent) throws IOException {
    LOGGER.info("Started processing recording...");
    try {
      long started = System.currentTimeMillis();
      MediaLocator ml = new MediaLocator("file:" + new File(recordingReadyEvent.getInputFile() + ".mov").getCanonicalPath());
  
      ImagesToVideo iTV = new ImagesToVideo();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      iTV.imagesToVideo(screenSize.width, screenSize.height, recordingReadyEvent.getFrameRate(), new File(recordingReadyEvent.getInputFile()), ml);
      LOGGER.info("Processing recording finished. Took {}ms", System.currentTimeMillis() - started);
    } catch (IOException e) {
      LOGGER.error("Error while processing recording...", e);
      throw e;
    }
  }

}
