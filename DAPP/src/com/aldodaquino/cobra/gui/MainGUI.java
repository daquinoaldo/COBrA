package com.aldodaquino.cobra.gui;

import com.aldodaquino.cobra.gui.constants.Strings;
import com.aldodaquino.cobra.gui.panels.StarterPanel;
import com.aldodaquino.cobra.main.Status;

import javax.swing.*;

/**
 * The GUI Main. Starts the GUI with the login panel.
 */
public class MainGUI {

    private static JFrame starterWindow;

    private static void showMainWindow(Status status) {
        starterWindow.dispose();
    }
    
    public static void main(String[] args) {

        // Show login window
        JPanel starterPanel = new StarterPanel(MainGUI::showMainWindow);
        starterWindow = Utils.createFixedWindow(Strings.appName, starterPanel, null, true);

    }
}