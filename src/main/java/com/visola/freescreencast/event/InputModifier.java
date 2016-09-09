package com.visola.freescreencast.event;

import java.util.HashSet;
import java.util.Set;

import org.jnativehook.NativeInputEvent;

public enum InputModifier {

  SHIFT_LEFT(NativeInputEvent.SHIFT_L_MASK),
  SHIFT_RIGHT(NativeInputEvent.SHIFT_R_MASK),
  SHIFT(NativeInputEvent.SHIFT_MASK),

  ALT_LEFT(NativeInputEvent.ALT_L_MASK),
  ALT_RIGHT(NativeInputEvent.ALT_R_MASK),
  ALT(NativeInputEvent.ALT_MASK),

  CONTROL_LEFT(NativeInputEvent.CTRL_L_MASK),
  CONTROL_RIGHT(NativeInputEvent.CTRL_R_MASK),
  CONTROL(NativeInputEvent.CTRL_MASK),

  META_LEFT(NativeInputEvent.META_L_MASK),
  META_RIGHT(NativeInputEvent.META_R_MASK),
  META(NativeInputEvent.META_MASK),

  BUTTON1(NativeInputEvent.BUTTON1_MASK, true),
  BUTTON2(NativeInputEvent.BUTTON2_MASK, true),
  BUTTON3(NativeInputEvent.BUTTON3_MASK, true),
  BUTTON4(NativeInputEvent.BUTTON4_MASK, true),
  BUTTON5(NativeInputEvent.BUTTON5_MASK, true),
  ;

  public static Set<InputModifier> getModifiers(int modifiers) {
    Set<InputModifier> result = new HashSet<>();
    for (InputModifier modifier : InputModifier.values()) {
      if (modifier.hasModifier(modifiers)) {
        result.add(modifier);
      }
    }
    return result;
  }

  private final boolean mouseButton;
  private final int modifierMask;

  private InputModifier(int modifierMask) {
    this(modifierMask, false);
  }

  private InputModifier(int modifierMask, boolean mouseButton) {
    this.mouseButton = mouseButton;
    this.modifierMask = modifierMask;
  }

  public boolean isMouseButton() {
    return mouseButton;
  }

  public boolean hasModifier(int mask) {
    return (mask & modifierMask) != 0;
  }

}
