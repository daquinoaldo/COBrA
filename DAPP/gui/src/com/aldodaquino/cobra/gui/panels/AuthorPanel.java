package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.components.AsyncPanel;
import com.aldodaquino.cobra.gui.components.AuthorContentTable;
import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.Content;
import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.main.ContentManager;

import java.math.BigInteger;
import java.util.List;
import javax.naming.OperationNotSupportedException;
import javax.swing.*;

/**
 * The author panel, a main panel showed after the starter panel if the user have chosen the author role.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class AuthorPanel extends AsyncPanel {

    private final Status status;
    private final CatalogManager catalogManager;

    private final JScrollPane tableContainer;
    private JTable table;

    /**
     * Constructor.
     * @param status the Status object.
     */
    public AuthorPanel(Status status) {
        this.status = status;
        catalogManager = status.getCatalogManager();

        // get the content list
        List<Content> contents;
        try {
            contents = catalogManager.getAuthorContents(status.getUserAddress());
        } catch (OperationNotSupportedException e) {
            throw new RuntimeException(e);
        }

        // listen events
        catalogManager.listenCatalogClosed(() -> Utils.newExitDialog("Catalog closed."));
        for (Content content : contents)
            catalogManager.listenPaymentAvailable(content.address,
                    (addr, name) -> Utils.newMessageDialog("Payment available for content " + name + "."));

        // table container
        table = new AuthorContentTable(status, contents);
        tableContainer = new JScrollPane();
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
        JPanel deployContentPanel = new DeployContentPanel(status, this::onDeployed);
        Utils.newWindow("Deploy new content", deployContentPanel, false);
    }

    private void onDeployed(String address) {
        // when deployed listen for payment available on this content
        ContentManager contentManager = new ContentManager(status.credentials, address);
        contentManager.listenContentPublished(() -> {
            catalogManager.listenPaymentAvailable(address, (addr, name) ->
                    Utils.newMessageDialog("Payment available for content " + name + "."));
            updateTable();
        });
    }

    private void updateTable() {
        doAsync(() -> {
            try {
                List<Content> contents = catalogManager.getAuthorContents(status.getUserAddress());
                table = new AuthorContentTable(status, contents);
                tableContainer.setViewportView(table);
            } catch (OperationNotSupportedException e) {
                e.printStackTrace();
                Utils.newErrorDialog(e.getMessage());
                System.exit(-1);
            }
        });
    }

    private void withdrawSelected() {
        doAsync(() -> {
            String address = table.getValueAt(table.getSelectedRow(), 0).toString();
            BigInteger amount = catalogManager.withdraw(address);
            if (amount.equals(BigInteger.ZERO))
                Utils.newErrorDialog("There is no payout available for this contract.");
            else Utils.newMessageDialog(amount + " wei collected.");
        });
    }

}