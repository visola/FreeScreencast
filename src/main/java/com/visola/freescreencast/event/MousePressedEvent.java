package com.visola.freescreencast.event;

public class MousePressedEvent extends AbstractMouseEvent {

  private static final long serialVersionUID = 1L;

  public MousePressedEvent(Object source, int x, int y) {
    super(source, x, y);
  }

}
