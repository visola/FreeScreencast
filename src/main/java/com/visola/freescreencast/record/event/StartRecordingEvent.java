package com.visola.freescreencast.record.event;

import org.springframework.context.ApplicationEvent;

public class StartRecordingEvent extends ApplicationEvent {

  private static final long serialVersionUID = 1L;
  
  private final long started = System.currentTimeMillis();

  public StartRecordingEvent(Object source) {
    super(source);
  }

  public long getStarted() {
    return started;
  }

}
