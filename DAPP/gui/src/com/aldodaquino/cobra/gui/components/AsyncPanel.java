package com.aldodaquino.cobra.gui.components;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;

/**
 * A JPanel that listen for ancestor changes. If it is added to an ancestor it saves it a protected variable window.
 * Include a doAsync method that runs the Runnable in another Thread and set the glass panel of the window in a loading
 * state to prevent actions from the user.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class AsyncPanel extends JPanel {

    protected JFrame window;

    /**
     * Constructor.
     */
    protected AsyncPanel() {

        addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                Component ancestor = event.getAncestor();
                if (ancestor.getClass() == JFrame.class)
                    window = (JFrame) ancestor;
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                window = null;
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                ancestorAdded(event);
            }

        });
    }

    /**
     * Run a runnable asynchronously. Shows a loading panel during the loading.
     * @param runnable to be run.
     */
    protected void doAsync(Runnable runnable) {
        startLoading(window);
        new Thread(() -> {
            runnable.run();
            stopLoading(window);
        }).start();
    }

    private static void startLoading(JFrame window) {
        if (window == null) return;
        window.setGlassPane(ComponentFactory.newSpinner());
        window.getGlassPane().setVisible(true);
    }

    private static void stopLoading(JFrame window) {
        if (window == null) return;
        window.getGlassPane().setVisible(false);
    }
}
