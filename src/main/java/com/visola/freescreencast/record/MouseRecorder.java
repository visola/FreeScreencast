package com.visola.freescreencast.record;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.visola.freescreencast.MouseFrame;
import com.visola.freescreencast.event.InputModifier;
import com.visola.freescreencast.record.event.StartRecordingEvent;
import com.visola.freescreencast.record.event.StopRecordingEvent;

@Component
public class MouseRecorder implements NativeMouseInputListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(MouseRecorder.class);

  private StartRecordingEvent startRecordingEvent = null;
  private Set<InputModifier> mouseModifiers = null;
  private DataOutputStream dataOut = null;

  @EventListener
  public void startRecording(StartRecordingEvent e) throws FileNotFoundException {
    this.startRecordingEvent = e;
    dataOut = new DataOutputStream(new FileOutputStream(new File(e.getInputDirectory(), "mouseEvents.bin")));
  }

  @EventListener
  public void stopRecording(StopRecordingEvent e) {
    this.startRecordingEvent = null;

    if (dataOut != null) {
      try {
        dataOut.close();
      } catch (IOException ex) {
        LOGGER.warn("Error while closing mouse output stream.", ex);
      }
      this.dataOut = null;
    }
  }

  @Override
  public void nativeMouseClicked(NativeMouseEvent e) {
    // Do nothing
  }

  @Override
  public void nativeMousePressed(NativeMouseEvent e) {
    mouseModifiers = InputModifier.getModifiers(e.getModifiers());
    recordMouseEvent(e);
  }

  @Override
  public void nativeMouseReleased(NativeMouseEvent e) {
    mouseModifiers = null;
    recordMouseEvent(e);
  }

  @Override
  public void nativeMouseMoved(NativeMouseEvent e) {
    recordMouseEvent(e);
  }

  @Override
  public void nativeMouseDragged(NativeMouseEvent e) {
    recordMouseEvent(e);
  }

  private boolean isRecording() {
    return startRecordingEvent != null;
  }

  private void recordMouseEvent(NativeMouseEvent e) {
    if (isRecording()) {
      try {
        MouseFrame.Builder frameBuilder = MouseFrame.newBuilder()
            .setTime(System.currentTimeMillis() - startRecordingEvent.getStartedAt())
            .setMouseX(e.getX())
            .setMouseY(e.getY());

        LOGGER.trace("Recording mouse position at: ({},{})", e.getX(), e.getY());
        if (mouseModifiers != null) {
          frameBuilder.setMouseDown(true)
            .addAllModifiers(mouseModifiers.stream().map(InputModifier::name).collect(Collectors.toSet()));
        }

        byte [] bytes = frameBuilder.build().toByteArray();
        dataOut.writeInt(bytes.length);
        dataOut.write(bytes);
      } catch (IOException ex) {
        LOGGER.error("Error while writing to mouse output stream.", ex);
      }
    }
  }

}
