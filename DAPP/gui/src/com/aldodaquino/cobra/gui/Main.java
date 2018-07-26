package com.aldodaquino.cobra.gui;

import com.aldodaquino.cobra.gui.constants.Strings;
import com.aldodaquino.cobra.gui.panels.AuthorPanel;
import com.aldodaquino.cobra.gui.panels.CustomerPanel;
import com.aldodaquino.cobra.gui.panels.StarterPanel;

import javax.swing.*;

/**
 * The GUI Main. Starts the GUI with the {@link StarterPanel}.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class Main {

    private static JFrame window;

    /**
     * Main method.
     * @param args an empty array, no parameters are required.
     */
    public static void main(String[] args) {

        // Create the starter panel
        JPanel starterPanel = new StarterPanel(Main::showMainPanel);

        // Create the window
        window = Utils.newWindow(Strings.appName, starterPanel, true);
        window.setMinimumSize(starterPanel.getMinimumSize());

    }

    private static void setContent(JPanel replacement) {
        window.setContentPane(replacement);
        window.revalidate();
        window.repaint();
        window.pack();
        window.setLocationRelativeTo(null);
        window.setMinimumSize(replacement.getMinimumSize());
    }

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
}