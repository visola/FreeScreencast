package com.visola.freescreencast;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import com.visola.freescreencast.event.GlobalMouseEventListener;
import com.visola.freescreencast.ui.SystemTrayManager;

@SpringBootApplication
public class Main {

  public static void main(String[] args) throws InterruptedException, IOException, NativeHookException {
    Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
    logger.setLevel(Level.OFF);

    ApplicationContext context = new SpringApplicationBuilder(Main.class)
        .headless(false)
        .web(false)
        .run(args);

    GlobalScreen.registerNativeHook();
    GlobalScreen.addNativeMouseListener(context.getBean(GlobalMouseEventListener.class));
    GlobalScreen.addNativeMouseMotionListener(context.getBean(GlobalMouseEventListener.class));

    context.getBean(SystemTrayManager.class).start();
  }

}
