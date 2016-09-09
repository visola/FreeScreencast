package com.visola.freescreencast.event;

import java.util.Set;

public class MousePressedEvent extends AbstractMouseEvent {

  private static final long serialVersionUID = 1L;

  public MousePressedEvent(Object source, int x, int y, Set<InputModifier> modifiers) {
    super(source, x, y, modifiers);
  }

}
