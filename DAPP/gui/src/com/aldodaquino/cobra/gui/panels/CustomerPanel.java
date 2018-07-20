package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.components.*;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.gui.Status;

import javax.swing.*;
import java.awt.*;

import static com.aldodaquino.cobra.gui.constants.Dimensions.LATERAL_BAR_PADDING;

public class CustomerPanel extends UpgradablePanel {

    private final Status status;
    private final CatalogManager catalogManager;

    private final JScrollPane tableContainer;
    private Component table;
    private final JPanel lateralBar;
    private final UserInfo userInfo;
    private ChartWidget chartWidget;
    private final GridBagConstraints chartWidgetPosition;

    private boolean showViews = false;

    public CustomerPanel(Status status) {
        this.status = status;
        catalogManager = status.getCatalogManager();

        // table container
        tableContainer = new JScrollPane();
        table = getTable();
        tableContainer.setViewportView(table);

        // lateral bar
        userInfo = new UserInfo(status);
        JButton buyPremiumButton = ComponentFactory.newButton("Buy premium", e -> buyPremium());
        JButton giftPremiumButton = ComponentFactory.newButton("Gift premium", e -> giftPremium());
        JButton updateButton = ComponentFactory.newButton("Refresh", e -> update());
        JButton showHideViewsButton = ComponentFactory.newButton("Show/hide views", e -> {
            showViews = !showViews;
            update();
        });
        chartWidget = new ChartWidget(status);
        JPanel newContentWidget = new NewContentsWidget(status);

        lateralBar = new JPanel(new GridBagLayout()) {
            // prevent widely resize with window
            @Override
            public Dimension getMaximumSize() {
                Dimension dim = super.getMaximumSize();
                dim.width = getPreferredSize().width;
                return dim;
            }
            // minimum size to fit all component
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };

        lateralBar.add(userInfo, newGBC(1, 1));
        lateralBar.setBorder(ComponentFactory.newBorder(LATERAL_BAR_PADDING.width, LATERAL_BAR_PADDING.height));
        lateralBar.add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), newGBC(1, 2));
        lateralBar.add(buyPremiumButton, newGBC(1, 3));
        lateralBar.add(giftPremiumButton, newGBC(1, 4));
        lateralBar.add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L), newGBC(1, 5));
        lateralBar.add(updateButton, newGBC(1, 6));
        lateralBar.add(showHideViewsButton, newGBC(1, 7));
        lateralBar.add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L), newGBC(1, 8));
        chartWidgetPosition = newGBC(1, 9);
        lateralBar.add(chartWidget, chartWidgetPosition);
        lateralBar.add(newContentWidget, newGBC(1, 10));

        // assemble the panel
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(tableContainer);
        add(lateralBar);
    }

    private void update() {
        doAsync(() -> {
            // update table
            table = getTable();
            tableContainer.setViewportView(table);

            // update user info
            userInfo.updateStatus();

            // update charts
            lateralBar.remove(chartWidget);
            chartWidget = new ChartWidget(status);
            lateralBar.add(chartWidget, chartWidgetPosition);
        });
    }

    private Component getTable() {
        return showViews ? new ViewsContentTable(status, catalogManager.getContentListWithViews())
                : new ContentList(status, catalogManager.getContentList());
    }

    private void buyPremium() {
        doAsync(() -> {
            if (catalogManager.buyPremium()) Utils.newMessageDialog("Premium bought.");
            else Utils.newErrorDialog("UNKNOWN ERROR: cannot buy a premium subscription.");
        });
        userInfo.updateStatus();
    }

    private void giftPremium() {
        JPanel pickUserPanel = new PickUserPanel((String user) ->
                doAsync(() -> {
                    if (catalogManager.giftPremium(user)) Utils.newMessageDialog("Premium gifted.");
                    else Utils.newErrorDialog("UNKNOWN ERROR: cannot gift a premium subscription.");
                }));
        Utils.newWindow("Gift premium", pickUserPanel, false);
    }

}