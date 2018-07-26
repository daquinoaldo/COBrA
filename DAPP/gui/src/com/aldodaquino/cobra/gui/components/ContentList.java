package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.gui.panels.ContentInfoPanel;
import com.aldodaquino.cobra.main.Content;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * a JList of Content objects.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class ContentList extends JList<String> {

    private static String[] prepareRows(List<Content> contents) {
        String[] rows = new String[contents.size()];
        for (int i = 0; i < contents.size(); i++)
            rows[i] = contents.get(i).name;
        return rows;
    }

    /**
     * Constructor.
     * @param status the Status object.
     * @param contents a List of Content objects.
     */
    public ContentList(Status status, List<Content> contents) {
        super(prepareRows(contents));

        // double-click listener
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) return;
                int index = locationToIndex(e.getPoint());
                ContentInfoPanel.newWindow(status, contents.get(index).address);
            }
        });
    }

}
