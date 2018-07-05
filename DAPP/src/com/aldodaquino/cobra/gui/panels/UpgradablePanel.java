package com.aldodaquino.cobra.gui.panels;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;

class UpgradablePanel extends JPanel {

    JFrame window;

    UpgradablePanel() {

        this.addAncestorListener(new AncestorListener() {

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
}
