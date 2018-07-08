import connections.Connection;
import connections.Json;
import connections.RMIManager;
import state.User;
import gui.Utils;
import gui.panels.LoginPanel;
import gui.panels.MainPanel;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Client Main. Starts things.
 */
class MainClient {
    private static JFrame mainWindow = null;
    
    public static void main(String[] args) {
        // Establish TCP connections
        Connection.init();
        
        // Show login window
        JPanel loginPanel = new LoginPanel();
        JFrame loginWindow = Utils.createFixedWindow("Social Gossip", loginPanel, true, false);
        
        User.addLoginListener(isLoggedIn -> {
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
        });
    }
}