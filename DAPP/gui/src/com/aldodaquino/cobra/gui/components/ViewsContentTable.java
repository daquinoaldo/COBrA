package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.gui.panels.ContentInfoPanel;
import com.aldodaquino.cobra.main.Content;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ViewsContentTable extends JTable {

    private static final String[] colNames = {"Name", "Views"};

    private static Object[][] prepareRows(List<Content> contents) {
        Object[][] rows = new Object[contents.size()][colNames.length];
        for (int i = 0; i < contents.size(); i++) {
            rows[i][0] = contents.get(i).name;
            rows[i][1] = contents.get(i).views;
        }
        return rows;
    }

    public ViewsContentTable(Status status, List<Content> contents) {
        super(prepareRows(contents), colNames);

        // double-click listener
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) return;
                int row = rowAtPoint(new Point(e.getX(), e.getY()));
                ContentInfoPanel.newWindow(status, contents.get(row).address);
            }
        });
    }

    // Make cells not editable
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}
