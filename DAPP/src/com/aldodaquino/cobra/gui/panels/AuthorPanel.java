package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.Content;
import com.aldodaquino.cobra.main.Status;

import java.math.BigInteger;
import java.util.List;
import javax.swing.*;

public class AuthorPanel extends UpgradablePanel {

    private Status status;
    private CatalogManager catalogManager;

    JScrollPane tableContainer;

    public AuthorPanel(Status status) {
        this.status = status;
        this.catalogManager = status.getCatalogManager();

        // table container
        tableContainer = new JScrollPane();
        updateTable();

        // buttons
        JPanel buttonsPad = new JPanel();
        buttonsPad.setLayout(new BoxLayout(buttonsPad, BoxLayout.X_AXIS));
        JButton deployButton = ComponentFactory.newButton("Deploy a new content", e -> deployContent());
        JButton updateButton = ComponentFactory.newButton("Update", e -> updateTable());
        buttonsPad.add(deployButton);
        buttonsPad.add(updateButton);

        // assemble the panel
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(tableContainer);
        add(buttonsPad);
    }

    private void updateTable() {
        // get the content list
        List<Content> contents = catalogManager.getAuthorContents(status.getUserAddress());

        // prepare the table headers and rows
        String[] colNames = {"Address", "Name", "Genre", "Views", "Collect payout"};
        Object[][] rows = new Object[contents.size()][colNames.length];
        for (int i = 0; i < contents.size(); i++) {
            String address = contents.get(i).getAddress();
            rows[i][0] = address;
            rows[i][1] = contents.get(i).getName();
            rows[i][2] = contents.get(i).getGenre();
            rows[i][3] = contents.get(i).getViews();
            rows[i][4] = new WithdrawButton(address);
        }

        // add the table to the table container
        tableContainer.setViewportView(new JTable(rows, colNames));
        validate();
        repaint();
    }

    private void deployContent() {
        Utils.doAsync(() -> {
            status.deployContent("name", "genre", new BigInteger("0"));
            updateTable();
        }, window);
    }

    private class WithdrawButton extends JButton {
        private String address;
        WithdrawButton(String address) {
            super("withdraw");
            this.address = address;
            addActionListener(e -> withdraw());
        }
        private void withdraw() {
            catalogManager.withdraw(address);
        }
    }
}