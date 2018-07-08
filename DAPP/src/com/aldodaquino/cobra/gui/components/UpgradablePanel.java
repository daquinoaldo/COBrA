package com.aldodaquino.cobra.gui.components;

import java.awt.*;

public class UpgradablePanel extends AsyncPanel {

    protected UpgradablePanel() {
        setLayout(new GridBagLayout());
    }

    protected void replaceComponent(Component toBeReplaced, Component replacement, GridBagConstraints position) {
        if (replacement == null) return;
        if(toBeReplaced != null) remove(toBeReplaced);
        add(replacement, position);
        if (window != null) {
            window.revalidate();
            window.repaint();
            window.pack();
        }
    }

    public static GridBagConstraints newGBC(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        return gbc;
    }
}
