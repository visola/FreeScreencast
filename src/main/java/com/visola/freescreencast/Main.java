package com.visola.freescreencast;

import java.io.IOException;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import com.visola.freescreencast.ui.SystemTrayManager;

@SpringBootApplication
public class Main {

  public static void main(String[] args) throws InterruptedException, IOException {
    ApplicationContext context = new SpringApplicationBuilder(Main.class)
      .headless(false)
      .web(false)
      .run(args);

    context.getBean(SystemTrayManager.class).start();
  }

}
