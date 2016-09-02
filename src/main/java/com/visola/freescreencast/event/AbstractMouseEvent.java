package com.visola.freescreencast.event;

import org.springframework.context.ApplicationEvent;

public class AbstractMouseEvent extends ApplicationEvent {

  private static final long serialVersionUID = 1L;

  private final int x;
  private final int y;

  public AbstractMouseEvent(Object source, int x, int y) {
    super(source);
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

}
