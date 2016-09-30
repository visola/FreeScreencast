package com.visola.freescreencast.record;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.visola.freescreencast.record.event.StartRecordingEvent;
import com.visola.freescreencast.record.event.StopRecordingEvent;

@Component
public class RecordingStateHolder {

  private final ApplicationEventPublisher applicationEventPublisher;

  private boolean recording;

  @Autowired
  public RecordingStateHolder(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public void startRecording(Object source) {
    recording = true;
    applicationEventPublisher.publishEvent(new StartRecordingEvent(source));
  }

  public boolean isRecording() {
    return recording;
  }

  public void stopRecording(Object source) {
    recording = false;
    applicationEventPublisher.publishEvent(new StopRecordingEvent(source));
  }

}
