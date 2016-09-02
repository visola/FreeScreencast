package com.visola.freescreencast.event;

public class MouseMovedEvent extends AbstractMouseEvent {

  private static final long serialVersionUID = 1L;

  public MouseMovedEvent(Object source, int x, int y) {
    super(source, x, y);
  }

}
