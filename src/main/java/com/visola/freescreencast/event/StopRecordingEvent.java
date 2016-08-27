package com.visola.freescreencast.event;

import org.springframework.context.ApplicationEvent;

public class StopRecordingEvent extends ApplicationEvent {

  private static final long serialVersionUID = 1L;

  public StopRecordingEvent(Object source) {
    super(source);
  }

}
