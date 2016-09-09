package com.visola.freescreencast.event;

import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class GlobalMouseEventListener implements NativeMouseInputListener {

  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public GlobalMouseEventListener(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Override
  public void nativeMouseClicked(NativeMouseEvent e) {
  }

  @Override
  public void nativeMousePressed(NativeMouseEvent e) {
    eventPublisher.publishEvent(new MousePressedEvent(this, e.getX(), e.getY(), InputModifier.getModifiers(e.getModifiers())));
  }

  @Override
  public void nativeMouseReleased(NativeMouseEvent e) {
    eventPublisher.publishEvent(new MouseReleasedEvent(this, e.getX(), e.getY(), InputModifier.getModifiers(e.getModifiers())));
  }

  @Override
  public void nativeMouseMoved(NativeMouseEvent e) {
    eventPublisher.publishEvent(new MouseMovedEvent(this, e.getX(), e.getY(), InputModifier.getModifiers(e.getModifiers())));
  }

  @Override
  public void nativeMouseDragged(NativeMouseEvent e) {
    eventPublisher.publishEvent(new MouseMovedEvent(this, e.getX(), e.getY(), InputModifier.getModifiers(e.getModifiers())));
  }

}
