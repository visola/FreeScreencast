package com.visola.freescreencast.event;

import java.util.Set;

public class MouseMovedEvent extends AbstractMouseEvent {

  private static final long serialVersionUID = 1L;

  public MouseMovedEvent(Object source, int x, int y, Set<InputModifier> modifiers) {
    super(source, x, y, modifiers);
  }

}
