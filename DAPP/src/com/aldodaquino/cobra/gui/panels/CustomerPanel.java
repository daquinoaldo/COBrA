package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.components.*;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.Content;
import com.aldodaquino.cobra.main.Status;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CustomerPanel extends AsyncPanel {

    private final CatalogManager catalogManager;

    private final JScrollPane tableContainer;
    private JTable table;
    private final UserInfo userInfo;

    public CustomerPanel(Status status) {
        this.catalogManager = status.getCatalogManager();

        // table container
        tableContainer = new JScrollPane();
        List<Content> contents = catalogManager.getContents();
        table = new ContentTable(contents);
        tableContainer.setViewportView(table);

        // lateral bar
        userInfo = new UserInfo(status);
        JButton updateButton = ComponentFactory.newButton("Update table", e -> updateTable());
        JButton buySelectedButton = ComponentFactory.newButton("Buy selected", e -> buySelected());
        JButton giftSelectedButton = ComponentFactory.newButton("Gift selected", e -> giftSelected());
        JButton accessSelectedButton = ComponentFactory.newButton("Access selected", e -> accessSelected());
        JButton buyPremiumButton = ComponentFactory.newButton("Buy premium", e -> buyPremium());
        JButton giftPremiumButton = ComponentFactory.newButton("Gift premium", e -> giftPremium());

        JPanel lateralBar = new JPanel(new GridBagLayout());
        lateralBar.add(userInfo, UpgradablePanel.newGBC(1, 1));
        lateralBar.add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L), UpgradablePanel.newGBC(1, 2));
        lateralBar.add(updateButton, UpgradablePanel.newGBC(1, 3));
        lateralBar.add(buySelectedButton, UpgradablePanel.newGBC(1, 4));
        lateralBar.add(giftSelectedButton, UpgradablePanel.newGBC(1, 5));
        lateralBar.add(accessSelectedButton, UpgradablePanel.newGBC(1, 6));
        lateralBar.add(buyPremiumButton, UpgradablePanel.newGBC(1, 7));
        lateralBar.add(giftPremiumButton, UpgradablePanel.newGBC(1, 8));

        // assemble the panel
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(tableContainer);
        add(lateralBar);
    }

    private void updateTable() {
        doAsync(() -> {
            List<Content> contents = catalogManager.getContents();
            table = new ContentTable(contents);
            tableContainer.setViewportView(table);
        });
    }

    private void buySelected() {
        doAsync(() -> {
            String address = table.getValueAt(table.getSelectedRow(), 0).toString();
            if (catalogManager.buyContent(address)) Utils.showMessageDialog("Content bought.");
            else Utils.showErrorDialog("Cannot buy this content. You may have bought it previously.");
        });
    }

    private void giftSelected() {
        JPanel pickUserPanel = new PickUserPanel((String user) ->
            doAsync(() -> {
                String address = table.getValueAt(table.getSelectedRow(), 0).toString();
                if (catalogManager.giftContent(address, user)) Utils.showMessageDialog("Content gifted.");
                else Utils.showErrorDialog("Cannot gift this content. The user may have already bought it.");
            }));
        Utils.createFixedWindow("Gift content", pickUserPanel, false);
    }

    private void buyPremium() {
        doAsync(() -> {
            if (catalogManager.buyPremium()) Utils.showMessageDialog("Premium bought.");
            else Utils.showErrorDialog("UNKNOWN ERROR: cannot buy a premium subscription.");
        });
        userInfo.update();
    }

    private void giftPremium() {
        JPanel pickUserPanel = new PickUserPanel((String user) ->
                doAsync(() -> {
                    if (catalogManager.giftPremium(user)) Utils.showMessageDialog("Premium gifted.");
                    else Utils.showErrorDialog("UNKNOWN ERROR: cannot gift a premium subscription.");
                }));
        Utils.createFixedWindow("Gift premium", pickUserPanel, false);
    }

    private void accessSelected() {
        // TODO
        Utils.showErrorDialog("TODO");
    }

}