package com.visola.freescreencast.event;

public class MouseReleasedEvent extends AbstractMouseEvent {

  private static final long serialVersionUID = 1L;

  public MouseReleasedEvent(Object source, int x, int y) {
    super(source, x, y);
  }

}
