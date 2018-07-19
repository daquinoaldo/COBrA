package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.panels.ContentPanel;
import com.aldodaquino.cobra.main.CatalogManager;
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

    private final CatalogManager catalogManager;

    public ContentList(CatalogManager catalogManager, List<Content> contents) {
        super(prepareRows(contents));
        this.catalogManager = catalogManager;

        // double-click listener
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) return;
                int index = locationToIndex(e.getPoint());
                showContentInfo(contents.get(index).address, contents.get(index).name);
            }
        });
    }

    private void showContentInfo(String address, String title) {
        Utils.createWindow(title, new ContentPanel(catalogManager, address), false);
    }

}
