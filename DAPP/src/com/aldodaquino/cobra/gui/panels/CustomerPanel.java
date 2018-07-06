package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.gui.components.ContentTable;
import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.Content;
import com.aldodaquino.cobra.main.Status;

import javax.swing.*;
import java.util.List;

public class CustomerPanel extends UpgradablePanel {

    private final CatalogManager catalogManager;

    private final JScrollPane tableContainer;

    public CustomerPanel(Status status) {
        this.catalogManager = status.getCatalogManager();

        // table container
        tableContainer = new JScrollPane();
        updateTable();

        // buttons
        JPanel buttonsPad = new JPanel();
        buttonsPad.setLayout(new BoxLayout(buttonsPad, BoxLayout.X_AXIS));
        JButton updateButton = ComponentFactory.newButton("Update table", e -> updateTable());

        // assemble the panel
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(tableContainer);
        add(updateButton);
    }

    private void updateTable() {
        List<Content> contents = catalogManager.getContents();
        JTable table = new ContentTable(contents);
        tableContainer.setViewportView(table);
    }

}