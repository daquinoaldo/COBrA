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

public class CustomerPanel extends UpgradablePanel {

    private final CatalogManager catalogManager;

    private final JScrollPane tableContainer;
    private JTable table;
    private JPanel lateralBar;
    private final UserInfo userInfo;
    private ChartWidget chartWidget;
    private GridBagConstraints chartBannerPosition = newGBC(1, 10);

    public CustomerPanel(Status status) {
        this.catalogManager = status.getCatalogManager();

        // table container
        tableContainer = new JScrollPane();
        List<Content> contents = catalogManager.getContents();
        table = new CustomerContentTable(catalogManager, contents);
        tableContainer.setViewportView(table);

        // lateral bar
        userInfo = new UserInfo(status);
        JButton updateButton = ComponentFactory.newButton("Refresh", e -> update());
        JButton buySelectedButton = ComponentFactory.newButton("Buy selected", e -> buySelected());
        JButton giftSelectedButton = ComponentFactory.newButton("Gift selected", e -> giftSelected());
        JButton accessSelectedButton = ComponentFactory.newButton("Access selected", e -> accessSelected());
        JButton buyPremiumButton = ComponentFactory.newButton("Buy premium", e -> buyPremium());
        JButton giftPremiumButton = ComponentFactory.newButton("Gift premium", e -> giftPremium());
        chartWidget = new ChartWidget(catalogManager);
        JPanel newContentWidget = new newContentWidget(catalogManager);

        lateralBar = new JPanel(new GridBagLayout());
        lateralBar.add(userInfo, newGBC(1, 1));
        lateralBar.add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L), newGBC(1, 2));
        lateralBar.add(updateButton, newGBC(1, 3));
        lateralBar.add(buySelectedButton, newGBC(1, 4));
        lateralBar.add(giftSelectedButton, newGBC(1, 5));
        lateralBar.add(accessSelectedButton, newGBC(1, 6));
        lateralBar.add(buyPremiumButton, newGBC(1, 7));
        lateralBar.add(giftPremiumButton, newGBC(1, 8));
        lateralBar.add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L), newGBC(1, 9));
        lateralBar.add(chartWidget, chartBannerPosition);
        lateralBar.add(newContentWidget, newGBC(1, 11));

        // assemble the panel
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(tableContainer);
        add(lateralBar);
    }

    private void update() {
        doAsync(() -> {
            // update table
            List<Content> contents = catalogManager.getContents();
            table = new CustomerContentTable(catalogManager, contents);
            tableContainer.setViewportView(table);

            // update user info
            userInfo.update();

            // update charts
            lateralBar.remove(chartWidget);
            chartWidget = new ChartWidget(catalogManager);
            lateralBar.add(chartWidget, chartBannerPosition);
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