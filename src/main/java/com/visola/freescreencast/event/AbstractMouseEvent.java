package com.visola.freescreencast.event;

import java.util.Set;

import org.springframework.context.ApplicationEvent;

public class AbstractMouseEvent extends ApplicationEvent {

  private static final long serialVersionUID = 1L;

  private final int x;
  private final int y;
  private final Set<InputModifier> modifiers;

  public AbstractMouseEvent(Object source, int x, int y, Set<InputModifier> modifiers) {
    super(source);
    this.x = x;
    this.y = y;
    this.modifiers = modifiers;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public Set<InputModifier> getModifiers() {
    return modifiers;
  }

}
