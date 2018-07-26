package com.aldodaquino.cobra.gui.components;

import java.awt.*;

/**
 * An {@link AsyncPanel} with {@link GridBagLayout}. Implements method to easy replace panels.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class UpgradablePanel extends AsyncPanel {

    /**
     * Constructor. Set the GridBagLayout. Can be called only by its children.
     */
    protected UpgradablePanel() {
        setLayout(new GridBagLayout());
    }

    /**
     * Replace a component with another one. Can be called only by its children.
     * @param toBeReplaced the old component.
     * @param replacement the new component.
     * @param position the position of the component to be replaced.
     */
    protected void replaceComponent(Component toBeReplaced, Component replacement, GridBagConstraints position) {
        if (replacement == null) return;
        if (toBeReplaced != null) remove(toBeReplaced);
        add(replacement, position);
        if (window != null) {
            window.revalidate();
            window.repaint();
            window.pack();
        }
    }

    /**
     * A public method that help to create GridBagConstraints in less time.
     * @param x the gridx property of the GridBagConstraints object.
     * @param y the gridy property of the GridBagConstraints object.
     * @return a GridBagConstraints object.
     */
    public static GridBagConstraints newGBC(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        return gbc;
    }
}
