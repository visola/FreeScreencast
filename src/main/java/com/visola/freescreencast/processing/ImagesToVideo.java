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

public class ImagesToVideo implements ControllerListener, DataSinkListener {

  public void imagesToVideo(int width, int height, float frameRate, File screenShotsFile, MediaLocator outML) throws FileNotFoundException {
    OutputDataSource dataSource = new OutputDataSource(width, height, frameRate, screenShotsFile);

    System.out.println("Creating processor...");
    final Processor p;
    try {
      p = Manager.createProcessor(dataSource);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    p.addControllerListener(this);

    System.out.println("Configuring processor...");
    p.configure();
    if (!waitForState(p, Processor.Configured)) {
      System.out.println("Failed to configure the processor.");
      return;
    }

    p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));

    TrackControl tcs[] = p.getTrackControls();
    Format f[] = tcs[0].getSupportedFormats();
    if (f == null || f.length <= 0) {
      System.out.println("The mux does not support the input format: " + tcs[0].getFormat());
      return;
    }

    System.out.printf("Using format %s.%n", f[0].getEncoding());
    tcs[0].setFormat(f[0]);

    System.out.println("Realizing processing...");
    p.realize();
    if (!waitForState(p, Controller.Realized)) {
      System.out.println("Failed to realize the processor.");
      return;
    }

    System.out.println("Creating data sink...");
    DataSink dsink;
    if ((dsink = createDataSink(p, outML)) == null) {
      System.out.println("Failed to create a DataSink for the given output MediaLocator: " + outML);
      return;
    }

    dsink.addDataSinkListener(this);
    fileDone = false;

    System.out.println("Start processing...");

    // OK, we can now start the actual transcoding.
    try {
      p.start();
      dsink.start();
    } catch (IOException e) {
      System.out.println("IO error during processing");
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

    System.out.println("...done processing.");

    return;
  }

  Object waitSync = new Object();
  boolean stateTransitionOK = true;

  /**
   * Block until the processor has transitioned to the given state.
   * Return false if the transition failed.
   */
  private boolean waitForState(Processor p, int state) {
    System.out.printf("Waiting for state %s%n", state);
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
      System.out.println("Something is really wrong: the processor does not have an output DataSource");
      return null;
    }

    DataSink dsink;

    try {
      System.out.println("- create DataSink for: " + outML);
      dsink = Manager.createDataSink(ds, outML);
      dsink.open();
    } catch (Exception e) {
      System.out.println("Cannot create the DataSink: " + e);
      return null;
    }

    return dsink;
  }

  @Override
  public void controllerUpdate(ControllerEvent event) {
    System.out.printf("Controller event received: %s%n", event.getClass().getName());
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
