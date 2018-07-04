package com.aldodaquino.cobra.gui;

import com.aldodaquino.cobra.gui.constants.Strings;
import com.aldodaquino.cobra.gui.panels.StarterPanel;

import javax.swing.*;

/**
 * The GUI Main. Starts the GUI with the login panel.
 */
public class MainGUI {
    
    public static void main(String[] args) {

        // Show login window
        JPanel loginPanel = new StarterPanel();
        JFrame loginWindow = Utils.createFixedWindow(Strings.appName, loginPanel, null, true);


        /*User.addLoginListener(isLoggedIn -> {
            if (isLoggedIn) {
                loginWindow.dispose();

                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (IllegalAccessException | UnsupportedLookAndFeelException | InstantiationException |
                        ClassNotFoundException e) {
                    e.printStackTrace();
                }
                JPanel mainPanel = new MainPanel();
                mainWindow = Utils.createFixedWindow("Social Gossip", mainPanel, true, false);

                // Every 1s refresh the room list
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.scheduleAtFixedRate(Json::chatList, 0, 5, TimeUnit.SECONDS);
            }
        });

        User.addUsernameListener(username -> {
            System.out.println("Logged in as " + username);
            if (mainWindow != null)
                mainWindow.setTitle(username + " - Social Gossip");
            RMIManager.registerCallback();
        });*/
    }
}