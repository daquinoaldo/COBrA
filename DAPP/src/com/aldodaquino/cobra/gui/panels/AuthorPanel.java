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
        // get the content list
        List<Content> contents = catalogManager.getAuthorContents(status.getUserAddress());

        // prepare the table headers and rows
        String[] colNames = {"Address", "Name", "Genre", "Views", "Enjoy", "Price fairness", "Content meaning",
                "Price"};
        Object[][] rows = new Object[contents.size()][colNames.length];
        for (int i = 0; i < contents.size(); i++) {
            String address = contents.get(i).address;
            rows[i][0] = address;
            rows[i][1] = contents.get(i).name;
            rows[i][2] = contents.get(i).genre;
            rows[i][3] = contents.get(i).views;
            rows[i][4] = contents.get(i).enjoy;
            rows[i][5] = contents.get(i).priceFairness;
            rows[i][6] = contents.get(i).contentMeaning;
            rows[i][7] = contents.get(i).price;
        }

        // prepare the table and add it to the container
        table = new JTable(rows, colNames);
        tableContainer.setViewportView(table);
        validate();
        repaint();
    }

    private void withdrawSelected() {
        String address = table.getValueAt(table.getSelectedRow(), 0).toString();
        BigInteger amount = catalogManager.withdraw(address);
        if (amount.equals(BigInteger.ZERO))
            Utils.showErrorDialog("There is no payout available for this contract.");
        else Utils.showMessageDialog(amount + " wei collected.");
    }

}