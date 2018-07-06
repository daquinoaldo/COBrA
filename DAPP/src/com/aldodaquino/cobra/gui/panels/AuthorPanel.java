package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.gui.components.ContentTable;
import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.Content;
import com.aldodaquino.cobra.main.Status;

import java.math.BigInteger;
import java.util.List;
import javax.swing.*;

public class AuthorPanel extends UpgradablePanel {

    private final Status status;
    private final CatalogManager catalogManager;

    private final JScrollPane tableContainer;
    private JTable table;

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
        JButton updateButton = ComponentFactory.newButton("Update table", e -> updateTable());
        JButton withdraw = ComponentFactory.newButton("Withdraw selected", e -> withdrawSelected());
        buttonsPad.add(deployButton);
        buttonsPad.add(updateButton);
        buttonsPad.add(withdraw);

        // assemble the panel
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(tableContainer);
        add(buttonsPad);
    }

    private void deployContent() {
        JPanel deployContentPanel = new DeployContentPanel((String name, String genre, BigInteger price) ->
                Utils.doAsync(() -> {
                    status.deployContent(name, genre, price);
                    updateTable();
                }, window));
        Utils.createFixedWindow("Deploy new content", deployContentPanel, false);
    }

    private void updateTable() {
        List<Content> contents = catalogManager.getAuthorContents(status.getUserAddress());
        table = new ContentTable(contents);
        tableContainer.setViewportView(table);
    }

    private void withdrawSelected() {
        String address = table.getValueAt(table.getSelectedRow(), 0).toString();
        BigInteger amount = catalogManager.withdraw(address);
        if (amount.equals(BigInteger.ZERO))
            Utils.showErrorDialog("There is no payout available for this contract.");
        else Utils.showMessageDialog(amount + " wei collected.");
    }

}