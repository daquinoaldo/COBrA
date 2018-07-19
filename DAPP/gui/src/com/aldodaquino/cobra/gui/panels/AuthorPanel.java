package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.components.AsyncPanel;
import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.gui.components.AuthorContentTable;
import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.Content;
import com.aldodaquino.cobra.gui.Status;

import java.math.BigInteger;
import java.util.List;
import javax.naming.OperationNotSupportedException;
import javax.swing.*;

public class AuthorPanel extends AsyncPanel {

    private final Status status;
    private final CatalogManager catalogManager;

    private final JScrollPane tableContainer;
    private JTable table;

    public AuthorPanel(Status status) {
        this.status = status;
        this.catalogManager = status.getCatalogManager();

        // table container
        tableContainer = new JScrollPane();
        List<Content> contents = catalogManager.getFullContentList();
        table = new AuthorContentTable(catalogManager, contents);
        tableContainer.setViewportView(table);

        // buttons
        JPanel buttonsPad = new JPanel();
        buttonsPad.setLayout(new BoxLayout(buttonsPad, BoxLayout.X_AXIS));
        JButton deployButton = ComponentFactory.newButton("Deploy a new content", e -> deployContent());
        JButton updateButton = ComponentFactory.newButton("Update table", e -> updateTable());
        JButton withdrawButton = ComponentFactory.newButton("Withdraw selected", e -> withdrawSelected());
        buttonsPad.add(deployButton);
        buttonsPad.add(updateButton);
        buttonsPad.add(withdrawButton);

        // assemble the panel
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(tableContainer);
        add(buttonsPad);
    }

    private void deployContent() {
        JPanel deployContentPanel = new DeployContentPanel(status, this::updateTable);
        Utils.createFixedWindow("Deploy new content", deployContentPanel, false);
    }

    private void updateTable() {
        doAsync(() -> {
            try {
                List<Content> contents = catalogManager.getAuthorContents(status.getUserAddress());
                table = new AuthorContentTable(catalogManager, contents);
                tableContainer.setViewportView(table);
            } catch (OperationNotSupportedException e) {
                e.printStackTrace();
                Utils.showErrorDialog(e.getMessage());
                System.exit(-1);
            }
        });
    }

    private void withdrawSelected() {
        doAsync(() -> {
            String address = table.getValueAt(table.getSelectedRow(), 0).toString();
            BigInteger amount = catalogManager.withdraw(address);
            if (amount.equals(BigInteger.ZERO))
                Utils.showErrorDialog("There is no payout available for this contract.");
            else Utils.showMessageDialog(amount + " wei collected.");
        });
    }

}