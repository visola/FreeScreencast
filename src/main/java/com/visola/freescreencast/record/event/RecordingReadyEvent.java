package com.visola.freescreencast.record.event;

import org.springframework.context.ApplicationEvent;

public class RecordingReadyEvent extends ApplicationEvent {

  private static final long serialVersionUID = 1L;

  private final long startedAt;

  public RecordingReadyEvent(Object source, long startedAt) {
    super(source);
    this.startedAt = startedAt;
  }

  public long getStartedAt() {
    return startedAt;
  }

}
