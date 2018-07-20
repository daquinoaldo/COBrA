package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.gui.panels.ContentInfoPanel;
import com.aldodaquino.cobra.main.Content;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ContentList extends JList<String> {

    private static String[] prepareRows(List<Content> contents) {
        String[] rows = new String[contents.size()];
        for (int i = 0; i < contents.size(); i++)
            rows[i] = contents.get(i).name;
        return rows;
    }

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
