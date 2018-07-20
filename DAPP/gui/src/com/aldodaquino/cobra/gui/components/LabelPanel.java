package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.gui.constants.Images;
import com.aldodaquino.cobra.gui.panels.ContentInfoPanel;
import com.aldodaquino.cobra.main.Content;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LabelPanel extends UpgradablePanel {

    private final JLabel loader = new JLabel(new ImageIcon(Images.loading.getImage()), JLabel.CENTER);
    private final GridBagConstraints replacingPosition = newGBC(2, 1);

    private final Status status;

    LabelPanel(Status status, String label) {
        this.status = status;
        add(new JLabel(label), newGBC(1, 1));
        add(loader, replacingPosition);
    }

    public void update(Content content) {
        JLabel link = new JLabel(content == null ? ""
                : "<html><a href=\"about:" + content.address + "\">" + content.name + "</a>");
        // onClick show content panel
        if (content != null) link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ContentInfoPanel.newWindow(status, content.address);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        replaceComponent(loader, link, replacingPosition);
    }

}
