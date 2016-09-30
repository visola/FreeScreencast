package com.visola.freescreencast.record.event;

import org.springframework.context.ApplicationEvent;

public class StopRecordingEvent extends ApplicationEvent {

  private static final long serialVersionUID = 1L;

  public StopRecordingEvent(Object source) {
    super(source);
  }

}
