package com.visola.freescreencast.processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.media.ConfigureCompleteEvent;
import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImagesToVideo implements ControllerListener, DataSinkListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImagesToVideo.class);

  public void imagesToVideo(int width, int height, float frameRate, File screenShotsFile, MediaLocator outML) throws FileNotFoundException {
    OutputDataSource dataSource = new OutputDataSource(width, height, frameRate, screenShotsFile);

    LOGGER.debug("Creating processor...");
    final Processor p;
    try {
      p = Manager.createProcessor(dataSource);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    p.addControllerListener(this);

    LOGGER.debug("Configuring processor...");
    p.configure();
    if (!waitForState(p, Processor.Configured)) {
      LOGGER.error("Failed to configure the processor.");
      return;
    }

    p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));

    TrackControl tcs[] = p.getTrackControls();
    Format f[] = tcs[0].getSupportedFormats();
    if (f == null || f.length <= 0) {
      LOGGER.error("The mux does not support the input format: " + tcs[0].getFormat());
      return;
    }

    LOGGER.debug("Using format %s.%n", f[0].getEncoding());
    tcs[0].setFormat(f[0]);

    LOGGER.debug("Realizing processing...");
    p.realize();
    if (!waitForState(p, Controller.Realized)) {
      System.out.println("Failed to realize the processor.");
      return;
    }

    LOGGER.debug("Creating data sink...");
    DataSink dsink;
    if ((dsink = createDataSink(p, outML)) == null) {
      LOGGER.error("Failed to create a DataSink for the given output MediaLocator: {}", outML);
      return;
    }

    dsink.addDataSinkListener(this);
    fileDone = false;

    LOGGER.debug("Start processing...");

    // OK, we can now start the actual transcoding.
    try {
      p.start();
      dsink.start();
    } catch (IOException e) {
      LOGGER.error("IO error during processing", e);
      return;
    }

    // Wait for EndOfStream event.
    waitForFileDone();

    // Cleanup.
    try {
      dsink.close();
    } catch (Exception e) {
    }
    p.removeControllerListener(this);

    LOGGER.debug("...done processing.");

    return;
  }

  Object waitSync = new Object();
  boolean stateTransitionOK = true;

  /**
   * Block until the processor has transitioned to the given state.
   * Return false if the transition failed.
   */
  private boolean waitForState(Processor p, int state) {
    LOGGER.trace("Waiting for state {}", state);
    synchronized (waitSync) {
      try {
        while (p.getState() < state && stateTransitionOK) {
          waitSync.wait();
        }
      } catch (Exception e) {
      }
    }
    return stateTransitionOK;
  }

  private DataSink createDataSink(Processor p, MediaLocator outML) {
    DataSource ds;
    if ((ds = p.getDataOutput()) == null) {
      LOGGER.error("Something is really wrong: the processor does not have an output DataSource");
      return null;
    }

    DataSink dsink;

    try {
      LOGGER.debug("- create DataSink for: " + outML);
      dsink = Manager.createDataSink(ds, outML);
      dsink.open();
    } catch (Exception e) {
      LOGGER.error("Cannot create the DataSink", e);
      return null;
    }

    return dsink;
  }

  @Override
  public void controllerUpdate(ControllerEvent event) {
    LOGGER.debug("Controller event received: {}", event.getClass().getName());
    if (event instanceof ConfigureCompleteEvent
        || event instanceof RealizeCompleteEvent
        || event instanceof PrefetchCompleteEvent) {
      synchronized (waitSync) {
        stateTransitionOK = true;
        waitSync.notifyAll();
      }
    } else if (event instanceof ResourceUnavailableEvent) {
      synchronized (waitSync) {
        stateTransitionOK = false;
        waitSync.notifyAll();
      }
    } else if (event instanceof EndOfMediaEvent) {
      event.getSourceController().stop();
      event.getSourceController().close();
    }
  }

  Object waitFileSync = new Object();
  boolean fileDone = false;
  boolean fileSuccess = true;

  /**
   * Block until file writing is done.
   */
  private boolean waitForFileDone() {
    synchronized (waitFileSync) {
      try {
        while (!fileDone)
          waitFileSync.wait();
      } catch (Exception e) {
      }
    }
    return fileSuccess;
  }

  @Override
  public void dataSinkUpdate(DataSinkEvent event) {
    if (event instanceof EndOfStreamEvent) {
      synchronized (waitFileSync) {
        fileDone = true;
        waitFileSync.notifyAll();
      }
    } else if (event instanceof DataSinkErrorEvent) {
      synchronized (waitFileSync) {
        fileDone = true;
        fileSuccess = false;
        waitFileSync.notifyAll();
      }
    }
  }

}
