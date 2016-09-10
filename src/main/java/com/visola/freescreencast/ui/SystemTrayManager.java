package com.visola.freescreencast.ui;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visola.freescreencast.record.RecordingStateHolder;

@Component
public class SystemTrayManager implements ActionListener {

  private static final String ACTION_RECORD = "Start/Stop Recoding";

  private PopupMenu popupMenu = new PopupMenu();
  private MenuItem miRecordControl = new MenuItem("Start Recording");

  private final RecordingStateHolder recordingStateHolder;

  @Autowired
  public SystemTrayManager(RecordingStateHolder recordingStateHolder) {
    this.recordingStateHolder = recordingStateHolder;

    miRecordControl.addActionListener(this);
    miRecordControl.setActionCommand(ACTION_RECORD);
    popupMenu.add(miRecordControl);
  }

  public void start() {
    SystemTray tray = SystemTray.getSystemTray();

    try {
      TrayIcon icon = new TrayIcon(ImageIO.read(SystemTrayManager.class.getResourceAsStream("/camera.png")));
      tray.add(icon);

      icon.setPopupMenu(popupMenu);
    } catch (IOException | AWTException e) {
      throw new RuntimeException("Error while loading and setting tray icon.", e);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    switch (e.getActionCommand()) {
      case ACTION_RECORD:
        if (recordingStateHolder.isRecording()) {
          miRecordControl.setLabel("Start Recording");
          recordingStateHolder.stopRecording(this);
        } else {
          miRecordControl.setLabel("Stop Recording");
          recordingStateHolder.startRecording(this);
        }
    }
  }

}
