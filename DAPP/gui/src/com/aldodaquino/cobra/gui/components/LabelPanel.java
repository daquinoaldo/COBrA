package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.constants.Images;

import javax.swing.*;
import java.awt.*;

public class LabelPanel extends UpgradablePanel {

    private JLabel loader = new JLabel(new ImageIcon(Images.loading.getImage()), JLabel.CENTER);
    private GridBagConstraints replacingPosition = newGBC(2, 1);

    LabelPanel(String label) {
        add(new JLabel(label), newGBC(1, 1));
        //add(loader, replacingPosition);
    }

    public void update(String[] info) {
        JLabel link = new JLabel(info == null ? "" : "<html><a href=\"about:" + info[1] + "\">" + info[0] + "</a>");
        replaceComponent(loader, link, replacingPosition);
    }

}
