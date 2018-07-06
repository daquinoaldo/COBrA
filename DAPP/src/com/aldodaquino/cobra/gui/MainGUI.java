package com.aldodaquino.cobra.gui;

import com.aldodaquino.cobra.gui.constants.Strings;
import com.aldodaquino.cobra.gui.panels.AuthorPanel;
import com.aldodaquino.cobra.gui.panels.CustomerPanel;
import com.aldodaquino.cobra.gui.panels.StarterPanel;
import com.aldodaquino.cobra.main.Status;

import javax.swing.*;

/**
 * The GUI Main. Starts the GUI with the login panel.
 */
public class MainGUI {

    private static JFrame window;

    private static void showMainPanel(Status status) {
        JPanel newPanel;

        switch (status.getRole()) {
            case (Status.ROLE_CUSTOMER):
                newPanel = new CustomerPanel(status);
                break;
            case (Status.ROLE_AUTHOR):
                newPanel = new AuthorPanel(status);
                break;
            default:
                throw new IllegalArgumentException("Status role property has an invalid value. " +
                        "Should be one of the Status.ROLE_X constants.");
        }
        setContent(newPanel);
    }

    public static void main(String[] args) {

        // Create the starter panel
        JPanel starterPanel = new StarterPanel(MainGUI::showMainPanel);

        // Create the window
        window = Utils.createFixedWindow(Strings.appName, starterPanel, true);
        /*window = new JFrame(Strings.appName);                               // set the app title
        window.setIconImage(Images.logo.getImage());                        // set logo as application icon
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);     // exit program when window gets closed
        window.setContentPane(starterPanel);    // put a panel inside the window
        window.pack();                          // resize the window based on content size
        window.setLocationRelativeTo(null);     // center the window
        window.setVisible(true);                // show it*/

    }

    private static void setContent(JPanel replacement) {
        window.setContentPane(replacement);
        window.revalidate();
        window.repaint();
        window.pack();
        window.setLocationRelativeTo(null);
    }
}