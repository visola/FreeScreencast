package com.visola.freescreencast.record.event;

import org.springframework.context.ApplicationEvent;

public class StartRecordingEvent extends ApplicationEvent {

  private static final long serialVersionUID = 1L;

  public StartRecordingEvent(Object source) {
    super(source);
  }

}
